package com.hbm.tileentity.machine;

import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.machine.ItemMold;
import com.hbm.items.machine.ItemMold.Mold;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Molds and basins: slot 0 holds the mold, slot 1 the cast item. Once full the material cools off for 5 seconds and
 * turns into the mold's output, which can be taken out by hand or extracted from any side.
 */
public abstract class TileEntityFoundryCastingBase extends TileEntityFoundryBase implements WorldlyContainer {

	public NonNullList<ItemStack> slots = NonNullList.withSize(2, ItemStack.EMPTY);
	public int cooloff = 100;

	public TileEntityFoundryCastingBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if(isServer()) {

			if(this.amount > this.getCapacity()) {
				this.amount = this.getCapacity();
			}

			if(this.amount == 0) {
				this.type = null;
			}

			Mold mold = this.getInstalledMold();

			if(mold != null && this.amount == this.getCapacity() && slots.get(1).isEmpty()) {
				cooloff--;

				if(cooloff <= 0) {
					this.amount = 0;

					ItemStack out = mold.getOutput(type);

					if(out != null) {
						slots.set(1, out.copy());
					}

					cooloff = 200;
					this.sync();
				}

			} else {
				cooloff = 200;
			}
		}
	}

	/** Checks slot 0 to see what mold type is installed. Returns null if no mold is found or an incorrect size was used. */
	public Mold getInstalledMold() {
		Mold mold = ItemMold.getMold(slots.get(0));
		if(mold != null && mold.size == this.getMoldSize()) return mold;
		return null;
	}

	/** Returns the amount of quanta this casting block can hold, depending on the installed mold or 0 if no mold is found. */
	@Override
	public int getCapacity() {
		Mold mold = this.getInstalledMold();
		return mold == null ? 0 : mold.getCost();
	}

	/**
	 * Standard check for testing if this material stack can be added to the casting block. Checks:<br>
	 * - type matching<br>
	 * - amount being at max<br>
	 * - whether a mold is installed<br>
	 * - whether the mold can accept this type
	 */
	@Override
	public boolean standardCheck(Level world, BlockPos pos, Direction side, MaterialStack stack) {
		if(!super.standardCheck(world, pos, side, stack)) return false; //reject if base conditions are not met
		if(!this.slots.get(1).isEmpty()) return false; //reject if a freshly casted item is still present
		Mold mold = this.getInstalledMold();
		if(mold == null) return false;

		return mold.getOutput(stack.material) != null; //no OD match -> no pouring
	}

	/** Returns an integer determining the mold size, 0 for small molds and 1 for the basin */
	public abstract int getMoldSize();

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		slots.clear();
		ContainerHelper.loadAllItems(nbt, slots, registries);
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		ContainerHelper.saveAllItems(nbt, slots, true, registries);
	}

	/// the cast item can be extracted from every side, nothing can be inserted ///

	@Override public int getContainerSize() { return slots.size(); }
	@Override public boolean isEmpty() { return slots.stream().allMatch(ItemStack::isEmpty); }
	@Override public ItemStack getItem(int slot) { return slots.get(slot); }

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack result = ContainerHelper.removeItem(slots, slot, amount);
		if(!result.isEmpty()) this.sync();
		return result;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(slots, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		slots.set(slot, stack);
		this.sync();
	}

	@Override public boolean stillValid(Player player) { return false; }
	@Override public void clearContent() { slots.clear(); }
	@Override public boolean canPlaceItem(int slot, ItemStack stack) { return false; }

	private static final int[] OUTPUT = new int[] { 1 };

	@Override public int[] getSlotsForFace(Direction side) { return OUTPUT; }
	@Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) { return false; }
	@Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) { return slot == 1; }
}
