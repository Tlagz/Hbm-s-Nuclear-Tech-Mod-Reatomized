package com.hbm.tileentity.machine;

import java.util.Optional;

import com.hbm.blocks.machine.MachineElectricFurnace;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.container.ContainerElectricFurnace;
import com.hbm.lib.Library;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Slots: 0 battery, 1 input, 2 output, 3 upgrade.
 *
 * TODO machine upgrades (speed/power), the upgrade slot accepts nothing until ItemMachineUpgrade is ported
 */
public class TileEntityMachineElectricFurnace extends TileEntityMachineBase implements IEnergyReceiverMK2, MenuProvider {

	// HOLY FUCKING SHIT I SPENT 5 DAYS ON THIS SHITFUCK CLASS FILE
	// thanks Martin, vaer and Bob for the help
	public int progress;
	public long power;
	public static final long maxPower = 100000;
	public int maxProgress = 100;
	public int consumption = 50;
	private int cooldown = 0;

	private static final int[] slots_io = new int[] { 0, 1, 2 };

	public TileEntityMachineElectricFurnace(BlockPos pos, BlockState state) {
		super(ModTileEntities.ELECTRIC_FURNACE.get(), pos, state, 4);
	}

	@Override
	public String getName() {
		return "container.electricFurnace";
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		if(i == 0) {
			return itemStack.getItem() instanceof IBatteryItem;
		}

		if(i == 1) {
			return getSmeltingResult(itemStack).isPresent();
		}

		return false;
	}

	private Optional<ItemStack> getSmeltingResult(ItemStack input) {
		if(input.isEmpty() || level == null) return Optional.empty();
		Optional<RecipeHolder<SmeltingRecipe>> recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level);
		return recipe.map(r -> r.value().getResultItem(level.registryAccess()));
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.power = nbt.getLong("power");
		this.progress = nbt.getInt("progress");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("power", power);
		nbt.putInt("progress", progress);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return slots_io;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		if(i == 0)
			if(itemStack.getItem() instanceof IBatteryItem battery && battery.getCharge(itemStack) == 0)
				return true;
		if(i == 2)
			return true;

		return false;
	}

	public int getProgressScaled(int i) {
		return (progress * i) / maxProgress;
	}

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	public boolean hasPower() {
		return power >= consumption;
	}

	public boolean isProcessing() {
		return this.progress > 0;
	}

	public boolean canProcess() {

		if(slots.get(1).isEmpty() || cooldown > 0) {
			return false;
		}
		Optional<ItemStack> result = getSmeltingResult(slots.get(1));

		if(result.isEmpty() || result.get().isEmpty()) {
			return false;
		}

		ItemStack itemStack = result.get();
		ItemStack output = slots.get(2);

		if(output.isEmpty()) {
			return true;
		}

		if(!ItemStack.isSameItemSameComponents(output, itemStack)) {
			return false;
		}

		return output.getCount() + itemStack.getCount() <= Math.min(getMaxStackSize(), output.getMaxStackSize());
	}

	private void processItem() {
		if(canProcess()) {
			ItemStack itemStack = getSmeltingResult(slots.get(1)).get();

			if(slots.get(2).isEmpty()) {
				slots.set(2, itemStack.copy());
			} else {
				slots.get(2).grow(itemStack.getCount());
			}

			slots.get(1).shrink(1);
		}
	}

	@Override
	public void updateEntity() {
		boolean markDirty = false;

		if(isServer()) {

			if(cooldown > 0) {
				cooldown--;
			}

			power = Library.chargeTEFromItems(slots, 0, power, maxPower);

			this.autoPort(this.allAround());

			this.consumption = 50;
			this.maxProgress = 100;

			if(!hasPower()) {
				cooldown = 20;
			}

			if(hasPower() && canProcess()) {
				progress++;

				power -= consumption;

				if(level.getGameTime() % 20 == 0) PollutionHandler.incrementPollution(level, worldPosition, PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND);

				if(this.progress >= maxProgress) {
					this.progress = 0;
					this.processItem();
					markDirty = true;
				}
			} else {
				progress = 0;
			}

			boolean trigger = true;

			if(hasPower() && canProcess() && this.progress == 0) {
				trigger = false;
			}

			if(trigger) {
				markDirty = true;
				MachineElectricFurnace.updateBlockState(this.progress > 0, level, worldPosition);
			}

			this.networkPackNT(50);

			if(markDirty) {
				this.setChanged();
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(maxProgress);
		buf.writeInt(progress);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		maxProgress = buf.readInt();
		progress = buf.readInt();
	}

	@Override
	public void setPower(long i) {
		power = i;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	/// GUI ///

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerElectricFurnace(id, inv, this);
	}
}
