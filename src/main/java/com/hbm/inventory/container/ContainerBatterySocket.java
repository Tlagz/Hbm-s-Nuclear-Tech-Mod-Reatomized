package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.storage.TileEntityBatterySocket;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerBatterySocket extends ContainerBase<TileEntityBatterySocket> {

	public ContainerBatterySocket(int id, Inventory invPlayer, TileEntityBatterySocket tile) {
		super(ModMenus.BATTERY_SOCKET.get(), id, tile);

		this.addSlot(new Slot(tile, 0, 35, 35) {
			@Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof IBatteryItem; }
			@Override public int getMaxStackSize() { return 1; }
		});

		this.addPlayerInventory(invPlayer, 8, 99);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index == 0) {
				if(!this.mergeItemStack(stack, 1, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(stack.getItem() instanceof IBatteryItem) {
				if(!this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
			} else {
				return ItemStack.EMPTY;
			}

			if(stack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return rStack;
	}
}
