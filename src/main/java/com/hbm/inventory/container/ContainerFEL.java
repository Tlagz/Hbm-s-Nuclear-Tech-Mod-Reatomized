package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.TileEntityFEL;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerFEL extends ContainerBase<TileEntityFEL> {

	public ContainerFEL(int id, Inventory invPlayer, TileEntityFEL tedf) {
		super(ModMenus.FEL.get(), id, tedf);

		// battery
		this.addSlot(new SlotNonRetarded(tedf, 0, 182, 144));
		// crystal
		this.addSlot(new SlotNonRetarded(tedf, 1, 141, 23));

		this.addPlayerInventory(invPlayer, 8, 83);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 1) {
				if(!this.mergeItemStack(stack, 2, this.slots.size(), false)) return ItemStack.EMPTY;
			} else {
				if(rStack.getItem() instanceof IBatteryItem || rStack.is(ModItems.battery_creative.get())) {
					if(!this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
				} else {
					if(!this.mergeItemStack(stack, 1, 2, false)) return ItemStack.EMPTY;
				}
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
