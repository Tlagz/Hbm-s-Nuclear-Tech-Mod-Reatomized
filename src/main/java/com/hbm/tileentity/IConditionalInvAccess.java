package com.hbm.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

/**
 * Masks operation such as isItemValidForSlot and getAccessibleSlotsFromSide found in ISidedInveotry
 * Intended to be used to return a different result depending on the port, assuming the port detects IConditionalInvAccess
 * (the item handler capability of multiblock proxies passes their own position, see ModCapabilities)
 *
 * @author hbm
 */
public interface IConditionalInvAccess {

	public boolean isItemValidForSlot(BlockPos pos, int slot, ItemStack stack);
	public default boolean canInsertItem(BlockPos pos, int slot, ItemStack stack, Direction side) { return isItemValidForSlot(pos, slot, stack); }
	public boolean canExtractItem(BlockPos pos, int slot, ItemStack stack, Direction side);
	public int[] getAccessibleSlotsFromSide(BlockPos pos, Direction side);
}
