package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.container.ContainerBlastFurnace;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType;
import com.hbm.inventory.recipes.BlastFurnaceRecipesNT;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes.IOutput;
import com.hbm.module.ModuleBurnTime;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.DirPos;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Blast furnace: burns solid fuel to make steel (and slag) from iron and sand/flux, red copper, firebricks.
 * Air blast (from a compressor) speeds it up to 5x, flue gas comes out of the top and the ports.
 * Slots: 0 fuel, 1-2 inputs, 3-4 outputs.
 *
 * TODO tilting (checkTilt, floor), the original's smoke tower particle instead of campfire smoke, copy/paste
 */
public class TileEntityMachineBlastFurnace extends TileEntityMachineBase implements IFluidStandardTransceiverMK2, MenuProvider {

	public FluidTank[] tanks;

	public boolean isProgressing;
	public float progress;
	public float speed;
	public int fuel;
	public static final int FUEL_COAL = 200 * 8;
	public static final int FUEL_RATE = 200 * 4; // half coal per operation
	public static final int MAX_FUEL = FUEL_COAL * 24; // 24 pieces of coal, can also fit a bit more than a coal coke block
	/**8 mb per tick burnt makes for 200tu/t in a fluid heater, giving about the same bonus as a firebox burning coal*/
	public static final int FLUE_GAS = 8; // per tick

	public ModuleBurnTime burnModule = new ModuleBurnTime()
			.setWoodHeatMod(0D);

	public TileEntityMachineBlastFurnace(BlockPos pos, BlockState state) {
		super(ModTileEntities.BLAST_FURNACE.get(), pos, state, 5);
		this.tanks = new FluidTank[2];
		this.tanks[0] = new FluidTank(Fluids.AIRBLAST, 4_000);
		this.tanks[1] = new FluidTank(Fluids.FLUE, 1_000);
	}

	@Override
	public String getName() {
		return "container.blastFurnace";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			for(DirPos pos : this.getConPos()) {
				this.trySubscribe(tanks[0].getTankType(), level, pos);
				if(this.tanks[1].getFill() > 0) this.tryProvide(tanks[1], level, pos);
			}

			if(!slots.get(0).isEmpty()) {
				int capacity = MAX_FUEL - fuel;
				int burnValue = getBurnTime(slots.get(0));
				if(burnValue > 0 && burnValue <= capacity) {
					this.fuel += burnValue;
					this.removeItem(0, 1);
				}
			}

			this.speed = 0F;
			GenericRecipe recipe = BlastFurnaceRecipesNT.INSTANCE.getRecipe(slots.get(1), slots.get(2));

			if(!this.tilted && recipe != null && this.fuel >= FUEL_RATE && this.hasQuantities(recipe) && this.canOutput(recipe)) {

				this.speed = Mth.clamp(1F + this.tanks[0].getFill() * 8F / this.tanks[0].getMaxFill(), 1F, 5F);

				this.isProgressing = true;
				this.progress += speed / recipe.duration;

				if(this.progress >= 1F) {
					this.process(recipe);
					this.progress = 0F;
					this.fuel -= FUEL_RATE;

					this.tanks[1].setFill((int) (tanks[1].getFill() + FLUE_GAS * (recipe.duration / speed)));
					if(this.tanks[1].getFill() > this.tanks[1].getMaxFill()) {
						int spill = this.tanks[1].getFill() - this.tanks[1].getMaxFill();
						this.tanks[1].getTankType().onFluidRelease(level, worldPosition.above(7), tanks[1], spill);
						FluidTrait.onRelease(level, worldPosition, tanks[1].getTankType(), tanks[1], FluidReleaseType.SPILL, spill);
						this.tanks[1].setFill(this.tanks[1].getMaxFill());
					}
					this.setChanged();
				}

				if(level.random.nextInt(10) == 0 && !this.muffled) {
					level.playSound(null, worldPosition, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0F, 0.5F + level.random.nextFloat() * 0.25F);
				}

			} else {
				this.isProgressing = false;
				this.progress = 0F;
			}

			if(this.tanks[0].getFill() > 0) this.tanks[0].setFill((int) (this.tanks[0].getFill() * 0.95));

			this.networkPackNT(100);
		} else {

			if(level.getBlockState(worldPosition.above(7)).isAir()) {
				if(isProgressing && level.getGameTime() % 2 == 0) {
					RandomSource rand = level.random;
					level.addParticle(ParticleTypes.LAVA, worldPosition.getX() + 0.25 + rand.nextDouble() * 0.5, worldPosition.getY() + 7.25, worldPosition.getZ() + 0.25 + rand.nextDouble() * 0.5, 0, 0, 0);

					if(tanks[1].getFill() >= 1_000) {
						level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 7, worldPosition.getZ() + 0.5, 0, 0.07, 0);
					}
				}
			}
		}
	}

	private Direction getDir() {
		return Direction.from3DDataValue(BlockDummyable.getMeta(getBlockState()) - BlockDummyable.offset);
	}

	public DirPos[] getConPos() {
		Direction dir = getDir();
		BlockPos p = worldPosition;
		return new DirPos[] {
				new DirPos(p.offset(2, 0, 0), Direction.EAST),
				new DirPos(p.offset(-2, 0, 0), Direction.WEST),
				new DirPos(p.offset(0, 0, 2), Direction.SOUTH),
				new DirPos(p.relative(dir, 2).above(3), dir),
				new DirPos(p.relative(dir, 2).above(5), dir),
				new DirPos(p.above(7), Direction.UP)
		};
	}

	public boolean hasQuantities(GenericRecipe recipe) {
		if(recipe.inputItem.length == 1) {
			return recipe.inputItem[0].matchesRecipe(slots.get(1), false) || recipe.inputItem[0].matchesRecipe(slots.get(2), false);
		}
		if(recipe.inputItem[0].matchesRecipe(slots.get(1), false) && recipe.inputItem[1].matchesRecipe(slots.get(2), false)) return true;
		if(recipe.inputItem[0].matchesRecipe(slots.get(2), false) && recipe.inputItem[1].matchesRecipe(slots.get(1), false)) return true;
		return false;
	}

	public boolean canOutput(GenericRecipe recipe) {

		for(int i = 0; i < recipe.outputItem.length; i++) {
			ItemStack slot = slots.get(3 + i);
			if(slot.isEmpty()) continue;
			IOutput out = recipe.outputItem[i];
			if(out.possibleMultiOutput()) return false;
			ItemStack stack = out.getSingle();
			if(!ItemStack.isSameItemSameComponents(stack, slot)) return false;
			if(stack.getCount() + slot.getCount() > stack.getMaxStackSize()) return false;
		}

		return true;
	}

	public void process(GenericRecipe recipe) {

		for(int i = 0; i < recipe.outputItem.length; i++) {
			IOutput out = recipe.outputItem[i];
			ItemStack stack = out.collapse();
			if(stack.isEmpty()) continue;
			if(!slots.get(3 + i).isEmpty()) slots.get(3 + i).grow(stack.getCount());
			else slots.set(3 + i, stack);
		}

		if(recipe.inputItem.length == 1) {
			if(recipe.inputItem[0].matchesRecipe(slots.get(1), false)) this.removeItem(1, recipe.inputItem[0].stacksize);
			else if(recipe.inputItem[0].matchesRecipe(slots.get(2), false)) this.removeItem(2, recipe.inputItem[0].stacksize);

		} else if(recipe.inputItem.length == 2) {
			if(recipe.inputItem[0].matchesRecipe(slots.get(1), false) && recipe.inputItem[1].matchesRecipe(slots.get(2), false)) {
				this.removeItem(1, recipe.inputItem[0].stacksize);
				this.removeItem(2, recipe.inputItem[1].stacksize);
			} else if(recipe.inputItem[0].matchesRecipe(slots.get(2), false) && recipe.inputItem[1].matchesRecipe(slots.get(1), false)) {
				this.removeItem(2, recipe.inputItem[0].stacksize);
				this.removeItem(1, recipe.inputItem[1].stacksize);
			}
		}
	}

	public int getBurnTime(ItemStack stack) {
		if(stack.hasCraftingRemainingItem()) return 0;
		return burnModule.getBurnHeat(burnModule.getBurnTime(stack, 0D), stack, 0D);
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == 0 && getBurnTime(stack) > 0) return true;
		if(slot == 1 || slot == 2) return true;
		return false;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, Direction side) {

		if(slot == 1 || slot == 2) {

			// repetition prevention
			if(slot == 1 && !slots.get(2).isEmpty() && ItemStack.isSameItem(stack, slots.get(2))) return false;
			if(slot == 2 && !slots.get(1).isEmpty() && ItemStack.isSameItem(stack, slots.get(1))) return false;

			// needs to match at least one recipe
			for(GenericRecipe recipe : BlastFurnaceRecipesNT.INSTANCE.recipeOrderedList) {
				for(AStack input : recipe.inputItem) {
					if(input.matchesRecipe(stack, true)) return true;
				}
			}

			return false;
		}

		return this.isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, Direction side) {
		return slot >= 3;
	}

	private static final int[] slot_access = new int[] { 1, 2, 0, 3, 4 };

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return slot_access;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		buf.writeBoolean(isProgressing);
		buf.writeFloat(progress);
		buf.writeFloat(speed);
		buf.writeInt(fuel);

		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		this.isProgressing = buf.readBoolean();
		this.progress = buf.readFloat();
		this.speed = buf.readFloat();
		this.fuel = buf.readInt();

		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);

		this.progress = nbt.getFloat("progress");
		this.fuel = nbt.getInt("fuel");
		tanks[0].readFromNBT(nbt, "t0");
		tanks[1].readFromNBT(nbt, "t1");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);

		nbt.putFloat("progress", progress);
		nbt.putInt("fuel", fuel);
		tanks[0].writeToNBT(nbt, "t0");
		tanks[1].writeToNBT(nbt, "t1");
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 7, worldPosition.getZ() + 2);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerBlastFurnace(id, inv, this);
	}

	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { tanks[0] }; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] { tanks[1] }; }
	@Override public FluidTank[] getAllTanks() { return tanks; }

	@Override public long getProviderSpeed(FluidType type, int pressure) { return Math.max(tanks[1].getFill() * 50 / tanks[1].getMaxFill(), 8); }
}
