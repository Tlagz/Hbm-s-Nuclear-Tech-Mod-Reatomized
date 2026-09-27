package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.TileEntityMachineTurbofan;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineTurbofan extends ContainerBase<TileEntityMachineTurbofan> {

	public ContainerMachineTurbofan(int id, Inventory invPlayer, TileEntityMachineTurbofan tedf) {
		super(ModMenus.TURBOFAN.get(), id, tedf);

		this.addSlot(new SlotNonRetarded(tedf, 0, 17, 17));
		this.addSlot(new SlotTakeOnly(tedf, 1, 17, 53));
		this.addSlot(new SlotNonRetarded(tedf, 2, 98, 71));
		this.addSlot(new SlotNonRetarded(tedf, 3, 143, 71));
		this.addSlot(new SlotNonRetarded(tedf, 4, 44, 71));

		this.addPlayerInventory(invPlayer, 8, 121);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 4) {
				if(!this.mergeItemStack(stack, 5, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {

				if(rStack.getItem() instanceof IBatteryItem) {
					if(!this.mergeItemStack(stack, 3, 4, false)) return ItemStack.EMPTY;
				} else if(rStack.getItem() instanceof IItemFluidIdentifier) {
					if(!this.mergeItemStack(stack, 4, 5, false)) return ItemStack.EMPTY;
				} else if(rStack.getItem() instanceof ItemMachineUpgrade) {
					if(!this.mergeItemStack(stack, 2, 3, false)) return ItemStack.EMPTY;
				} else {
					if(!this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
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
