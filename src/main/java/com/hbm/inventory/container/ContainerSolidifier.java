package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.oil.TileEntityMachineSolidifier;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerSolidifier extends ContainerBase<TileEntityMachineSolidifier> {

	public ContainerSolidifier(int id, Inventory playerInv, TileEntityMachineSolidifier tile) {
		super(ModMenus.SOLIDIFIER.get(), id, tile);

		//Output
		this.addSlot(new SlotTakeOnly(tile, 0, 71, 45));
		//Battery
		this.addSlot(new Slot(tile, 1, 134, 72));
		//Upgrades
		this.addSlot(new SlotUpgrade(tile, 2, 98, 36));
		this.addSlot(new SlotUpgrade(tile, 3, 98, 54));
		//ID
		this.addSlot(new Slot(tile, 4, 71, 72));

		this.addPlayerInventory(playerInv, 8, 122);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 4) {
				if(!this.mergeItemStack(stack, 5, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof IBatteryItem) {
				if(!this.mergeItemStack(stack, 1, 2, false)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof ItemMachineUpgrade) {
				if(!this.mergeItemStack(stack, 2, 4, false)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof IItemFluidIdentifier) {
				if(!this.mergeItemStack(stack, 4, 5, false)) return ItemStack.EMPTY;
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
