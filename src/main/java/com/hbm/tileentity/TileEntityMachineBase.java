package com.hbm.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base of machines with an inventory. The original's ISidedInventory methods map to WorldlyContainer,
 * with the original names kept as the methods subclasses override (isItemValidForSlot, canExtractItem,
 * getAccessibleSlotsFromSide) so machine code ports over mostly unchanged.
 * Automation (hoppers, pipes) gets access through the item handler capability (see ModCapabilities).
 */
public abstract class TileEntityMachineBase extends TileEntityLoadedBase implements WorldlyContainer {

	/** The inventory, empty slots are ItemStack.EMPTY instead of null */
	public NonNullList<ItemStack> slots;

	private String customName;

	public TileEntityMachineBase(BlockEntityType<?> type, BlockPos pos, BlockState state, int slotCount) {
		super(type, pos, state);
		slots = NonNullList.withSize(slotCount, ItemStack.EMPTY);
	}

	/** Translation key of the machine's name, e.g. "container.electricFurnace" */
	public abstract String getName();

	public String getInventoryName() {
		return customName != null && !customName.isEmpty() ? customName : getName();
	}

	public boolean hasCustomInventoryName() {
		return customName != null && !customName.isEmpty();
	}

	public void setCustomName(String name) {
		this.customName = name;
		setChanged();
	}

	/** Called every tick on both sides */
	@Override
	public abstract void updateEntity();

	/// ORIGINAL NAMES ///

	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return false;
	}

	public boolean canInsertItem(int slot, ItemStack stack, Direction side) {
		return this.isItemValidForSlot(slot, stack);
	}

	public boolean canExtractItem(int slot, ItemStack stack, Direction side) {
		return false;
	}

	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] { };
	}

	public double getUseRange() {
		return 12D;
	}

	/// CONTAINER ///

	@Override
	public int getContainerSize() {
		return slots.size();
	}

	@Override
	public boolean isEmpty() {
		for(ItemStack stack : slots) if(!stack.isEmpty()) return false;
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return slots.get(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack stack = ContainerHelper.removeItem(slots, slot, amount);
		if(!stack.isEmpty()) setChanged();
		return stack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return ContainerHelper.takeItem(slots, slot);
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		slots.set(slot, stack);
		stack.limitSize(this.getMaxStackSize(stack));
		setChanged();
	}

	@Override
	public boolean stillValid(Player player) {
		return Container.stillValidBlockEntity(this, player, (float) getUseRange());
	}

	@Override
	public void clearContent() {
		slots.clear();
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return isItemValidForSlot(slot, stack);
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return getAccessibleSlotsFromSide(side);
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
		return canInsertItem(slot, stack, side);
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
		return canExtractItem(slot, stack, side);
	}

	/// SAVING ///

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		// same layout as the original ("items" list with a "slot" byte per entry)
		slots = NonNullList.withSize(slots.size(), ItemStack.EMPTY);
		ListTag list = nbt.getList("items", Tag.TAG_COMPOUND);
		for(int i = 0; i < list.size(); i++) {
			CompoundTag entry = list.getCompound(i);
			int slot = entry.getByte("slot");
			if(slot >= 0 && slot < slots.size()) slots.set(slot, ItemStack.parseOptional(registries, entry));
		}
		if(nbt.contains("name", Tag.TAG_STRING)) this.customName = nbt.getString("name");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		ListTag list = new ListTag();
		for(int i = 0; i < slots.size(); i++) {
			if(!slots.get(i).isEmpty()) {
				CompoundTag entry = (CompoundTag) slots.get(i).save(registries, new CompoundTag());
				entry.putByte("slot", (byte) i);
				list.add(entry);
			}
		}
		nbt.put("items", list);
		if(customName != null) nbt.putString("name", customName);
	}
}
