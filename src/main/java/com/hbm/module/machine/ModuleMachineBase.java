package com.hbm.module.machine;

import java.util.List;

import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.inventory.recipes.loader.GenericRecipes.IOutput;
import com.hbm.items.machine.ItemBlueprints;

import api.hbm.energymk2.IEnergyHandlerMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/**
 * The recipe processing part of generic recipe machines (assembly machine, chemical plant...): checks inputs and
 * output space, uses power, counts progress and produces the output. The machine only feeds it speed/power multipliers.
 */
public abstract class ModuleMachineBase {

	// setup
	public int index;
	public IEnergyHandlerMK2 battery;
	public NonNullList<ItemStack> slots;
	public int[] inputSlots;
	public int[] outputSlots;
	public FluidTank[] inputTanks;
	public FluidTank[] outputTanks;
	// running vars
	protected String recipe = "null";
	public double progress;
	// return signals
	public boolean didProcess = false;
	public boolean markDirty = false;
	public boolean restrictedMode = false;

	public ModuleMachineBase(int index, IEnergyHandlerMK2 battery, NonNullList<ItemStack> slots) {
		this.index = index;
		this.battery = battery;
		this.slots = slots;
	}

	/** Chances tank type and pressure based on recipe */
	public void setupTanks(GenericRecipe recipe) {
		if(recipe == null) return;
		for(int i = 0; i < inputTanks.length; i++) if(recipe.inputFluid != null && recipe.inputFluid.length > i) inputTanks[i].conform(recipe.inputFluid[i]); else inputTanks[i].resetTank();
		for(int i = 0; i < outputTanks.length; i++) if(recipe.outputFluid != null && recipe.outputFluid.length > i) outputTanks[i].conform(recipe.outputFluid[i]); else outputTanks[i].resetTank();
	}

	/** Expects the tanks to be set up correctly beforehand */
	public boolean canProcess(GenericRecipe recipe, double speed, double power) {
		if(recipe == null) return false;

		// auto switch functionality
		if(recipe.autoSwitchGroup != null && inputSlots.length > 0 && !slots.get(inputSlots[0]).isEmpty()) {
			ItemStack itemToSwitchBy = slots.get(inputSlots[0]);
			List<GenericRecipe> recipes = this.getRecipeSet().autoSwitchGroups.get(recipe.autoSwitchGroup);
			if(recipes != null) for(GenericRecipe nextRec : recipes) {
				if(nextRec.getInternalName().equals(this.recipe)) continue;
				if(nextRec.inputItem == null) continue;
				if(nextRec.inputItem[0].matchesRecipe(itemToSwitchBy, true)) { // perform the switch
					this.recipe = nextRec.getInternalName();
					return false; // cancel the recipe this tick since we need to do the previous checking all over again
				}
			}
		}

		if(power != 1 && battery.getPower() < recipe.power * power) return false; // only check with floating point numbers if mult is not 1
		if(power == 1 && battery.getPower() < recipe.power) return false;

		if(!hasInput(recipe)) return false;

		return canFitOutput(recipe);
	}

	protected boolean hasInput(GenericRecipe recipe) {

		if(recipe.inputItem != null) {
			for(int i = 0; i < Math.min(recipe.inputItem.length, inputSlots.length); i++) {
				if(!recipe.inputItem[i].matchesRecipe(slots.get(inputSlots[i]), false)) return false;
			}
		}

		if(recipe.inputFluid != null) {
			for(int i = 0; i < Math.min(recipe.inputFluid.length, inputTanks.length); i++) {
				if(inputTanks[i].getFill() < recipe.inputFluid[i].fill) return false;
			}
		}

		return true;
	}

	/** Whether the machine can hold the output produced by the recipe */
	protected boolean canFitOutput(GenericRecipe recipe) {

		if(recipe.outputItem != null) {
			for(int i = 0; i < Math.min(recipe.outputItem.length, outputSlots.length); i++) {
				ItemStack stack = slots.get(outputSlots[i]);
				if(stack.isEmpty()) continue; // always continue if output slot is free
				IOutput output = recipe.outputItem[i];
				if(output.possibleMultiOutput()) return false; // output slot needs to be empty to decide on multi outputs
				ItemStack single = output.getSingle();
				if(single == null) return false; // shouldn't be possible but better safe than sorry
				if(!ItemStack.isSameItemSameComponents(stack, single)) return false;
				if(stack.getCount() + single.getCount() > stack.getMaxStackSize()) return false;
			}
		}

		if(recipe.outputFluid != null) {
			for(int i = 0; i < Math.min(recipe.outputFluid.length, outputTanks.length); i++) {
				if(recipe.outputFluid[i].fill + outputTanks[i].getFill() > outputTanks[i].getMaxFill()) return false;
			}
		}

		return true;
	}

	public void process(GenericRecipe recipe, double speed, double power) {
		if(this.restrictedMode) speed *= 0.25; // RoR controlled machines have a speed penalty

		this.battery.setPower(this.battery.getPower() - (power == 1 ? recipe.power : (long) (recipe.power * power)));
		double step = Math.min(speed / recipe.duration, 1D); // can't do more than one recipe per tick, might look into that later
		this.progress += step;

		if(this.progress >= 1D) {
			consumeInput(recipe);
			produceItem(recipe);

			if(this.canProcess(recipe, speed, power)) this.progress -= 1D;
			else this.progress = 0D;
		}
	}

	/** Part 1 of the process completion, uses up input */
	protected void consumeInput(GenericRecipe recipe) {

		if(recipe.inputItem != null) {
			for(int i = 0; i < Math.min(recipe.inputItem.length, inputSlots.length); i++) {
				slots.get(inputSlots[i]).shrink(recipe.inputItem[i].stacksize);
			}
		}

		if(recipe.inputFluid != null) {
			for(int i = 0; i < Math.min(recipe.inputFluid.length, inputTanks.length); i++) {
				inputTanks[i].setFill(inputTanks[i].getFill() - recipe.inputFluid[i].fill);
			}
		}
	}

	/** Part 2 of the process completion, generated output */
	protected void produceItem(GenericRecipe recipe) {

		if(recipe.outputItem != null) {
			for(int i = 0; i < Math.min(recipe.outputItem.length, outputSlots.length); i++) {
				ItemStack collapse = recipe.outputItem[i].collapse();
				if(slots.get(outputSlots[i]).isEmpty()) {
					slots.set(outputSlots[i], collapse);
				} else {
					if(!collapse.isEmpty()) slots.get(outputSlots[i]).grow(collapse.getCount()); // we can do this because we've already established that the result slot is not null if it's a single output
				}
			}
		}

		if(recipe.outputFluid != null) {
			for(int i = 0; i < Math.min(recipe.outputFluid.length, outputTanks.length); i++) {
				outputTanks[i].setFill(outputTanks[i].getFill() + recipe.outputFluid[i].fill);
			}
		}

		this.markDirty = true;
	}

	public String getRecipeName() {
		return this.recipe;
	}

	public GenericRecipe getRecipe() {
		return this.getRecipeSet().recipeNameMap.get(this.recipe);
	}

	public void setRecipe(String name, boolean ror) {
		this.recipe = name;
		this.restrictedMode = ror;
	}

	public abstract GenericRecipes<? extends GenericRecipe> getRecipeSet();

	public void update(double speed, double power, boolean extraCondition, ItemStack blueprint) {
		GenericRecipe recipe = getRecipe();

		if(recipe != null && recipe.isPooled() && !recipe.isPartOfPool(ItemBlueprints.grabPool(blueprint))) {
			this.didProcess = false;
			this.progress = 0F;
			this.recipe = "null";
			return;
		}

		this.setupTanks(recipe);

		this.didProcess = false;
		this.markDirty = false;

		if(extraCondition && this.canProcess(recipe, speed, power)) {
			this.process(recipe, speed, power);
			this.didProcess = true;
		} else {
			this.progress = 0F;
		}
	}

	/** For item IO, instead of the TE doing all the work it only has to handle non-recipe stuff, the module does the rest */
	public boolean isItemValid(int slot, ItemStack stack) {
		GenericRecipe recipe = getRecipe();
		if(recipe == null) return false;
		if(recipe.inputItem == null) return false;

		for(int i = 0; i < Math.min(inputSlots.length, recipe.inputItem.length); i++) {
			if(inputSlots[i] == slot && recipe.inputItem[i].matchesRecipe(stack, true)) return true;
		}

		if(recipe.autoSwitchGroup != null) {
			List<GenericRecipe> recipes = this.getRecipeSet().autoSwitchGroups.get(recipe.autoSwitchGroup);
			if(recipes != null) for(GenericRecipe newRec : recipes) {
				if(newRec.inputItem == null) continue;
				if(inputSlots.length > 0 && inputSlots[0] == slot && newRec.inputItem[0].matchesRecipe(stack, true)) {
					return true;
				}
			}
		}

		return false;
	}

	/** Returns true if the supplied slot is occupied with an item that does not match the recipe */
	public boolean isSlotClogged(int slot) {
		boolean isSlotValid = false;
		for(int i : inputSlots) if(i == slot) isSlotValid = true;
		if(!isSlotValid) return false;
		ItemStack stack = slots.get(slot);
		if(stack.isEmpty()) return false;
		return !isItemValid(slot, stack); // we need to use this because it also handles autoswitch correctly, otherwise autoswitch items may be ejected instantly
	}

	public void serialize(ByteBuf buf) {
		buf.writeDouble(progress);
		buf.writeBoolean(restrictedMode);
		new FriendlyByteBuf(buf).writeUtf(recipe);
	}

	public void deserialize(ByteBuf buf) {
		this.progress = buf.readDouble();
		this.restrictedMode = buf.readBoolean();
		this.recipe = new FriendlyByteBuf(buf).readUtf();
	}

	public void readFromNBT(CompoundTag nbt) {
		this.progress = nbt.getDouble("progress" + index);
		this.recipe = nbt.getString("recipe" + index);
		if(this.recipe.isEmpty()) this.recipe = "null";
		this.restrictedMode = nbt.getBoolean("restrictedMode");
	}

	public void writeToNBT(CompoundTag nbt) {
		nbt.putDouble("progress" + index, progress);
		nbt.putString("recipe" + index, recipe);
		nbt.putBoolean("restrictedMode", restrictedMode);
	}
}
