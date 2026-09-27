package com.hbm.interfaces;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Machines with filter slots: a control packet with "slot" and "stack" sets a filter (used by recipe viewers to drag
 * ghost items in), nextMode cycles a slot's match mode.
 * TODO the copy tool settings (ICopiable)
 */
public interface IControlReceiverFilter extends IControlReceiver {

	void nextMode(int i);

	/** The start and end (start inclusive, end exclusive) of the filter slots */
	int[] getFilterSlots();

	@Override
	default void receiveControl(CompoundTag data) {
		if(data.contains("slot")) {
			setFilterContents(data);
		}
	}

	/** Expects the implementor to be a block entity and a Container */
	default void setFilterContents(CompoundTag nbt) {
		BlockEntity tile = (BlockEntity) this;
		Container inv = (Container) this;
		int slot = nbt.getInt("slot");
		int[] filterRange = this.getFilterSlots();
		if(slot < filterRange[0] || slot >= filterRange[1]) return;
		ItemStack item = ItemStack.parseOptional(tile.getLevel().registryAccess(), nbt.getCompound("stack"));
		inv.setItem(slot, item.copyWithCount(1));
		nextMode(slot);
		tile.setChanged();
	}
}
