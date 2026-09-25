package com.hbm.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * A machine's inventory as seen from one of its ports: slot access and insert/extract checks go through the
 * machine's IConditionalInvAccess with the port's position, everything else is the machine's inventory.
 */
public class ConditionalInvView implements WorldlyContainer {

	private final TileEntityMachineBase machine;
	private final IConditionalInvAccess access;
	private final BlockPos port;

	public ConditionalInvView(TileEntityMachineBase machine, IConditionalInvAccess access, BlockPos port) {
		this.machine = machine;
		this.access = access;
		this.port = port;
	}

	@Override public int[] getSlotsForFace(Direction side) { return access.getAccessibleSlotsFromSide(port, side); }
	@Override public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) { return access.canInsertItem(port, slot, stack, side); }
	@Override public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) { return access.canExtractItem(port, slot, stack, side); }
	@Override public boolean canPlaceItem(int slot, ItemStack stack) { return access.isItemValidForSlot(port, slot, stack); }

	@Override public int getContainerSize() { return machine.getContainerSize(); }
	@Override public boolean isEmpty() { return machine.isEmpty(); }
	@Override public ItemStack getItem(int slot) { return machine.getItem(slot); }
	@Override public ItemStack removeItem(int slot, int amount) { return machine.removeItem(slot, amount); }
	@Override public ItemStack removeItemNoUpdate(int slot) { return machine.removeItemNoUpdate(slot); }
	@Override public void setItem(int slot, ItemStack stack) { machine.setItem(slot, stack); }
	@Override public void setChanged() { machine.setChanged(); }
	@Override public boolean stillValid(Player player) { return machine.stillValid(player); }
	@Override public void clearContent() { machine.clearContent(); }
	@Override public int getMaxStackSize() { return machine.getMaxStackSize(); }
}
