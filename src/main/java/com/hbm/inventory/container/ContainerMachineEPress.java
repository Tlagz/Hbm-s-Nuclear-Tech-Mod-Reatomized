package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemStamp;
import com.hbm.tileentity.machine.TileEntityMachineEPress;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineEPress extends ContainerBase<TileEntityMachineEPress> {

	public ContainerMachineEPress(int id, Inventory invPlayer, TileEntityMachineEPress tedf) {
		super(ModMenus.EPRESS.get(), id, tedf);

		// Battery
		this.addSlot(new SlotNonRetarded(tedf, 0, 152, 54));
		// Stamp
		this.addSlot(new SlotNonRetarded(tedf, 1, 19, 15));
		// Input
		this.addSlot(new SlotNonRetarded(tedf, 2, 19, 51));
		// Output
		this.addSlot(new SlotTakeOnly(tedf, 3, 79, 33));
		// Upgrade
		this.addSlot(new SlotUpgrade(tedf, 4, 111, 32));

		this.addPlayerInventory(invPlayer, 8, 104);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = this.slots.get(index);

		if(var4 != null && var4.hasItem()) {
			ItemStack var5 = var4.getItem();
			var3 = var5.copy();

			if(index <= 4) {
				if(!this.mergeItemStack(var5, 5, this.slots.size(), true)) return ItemStack.EMPTY;
			} else {
				if(var3.getItem() instanceof IBatteryItem || var3.is(ModItems.battery_creative.get())) {
					if(!this.mergeItemStack(var5, 0, 1, false)) return ItemStack.EMPTY;
				} else if(var3.getItem() instanceof ItemMachineUpgrade) {
					if(!this.mergeItemStack(var5, 4, 5, false)) return ItemStack.EMPTY;
				} else if(var3.getItem() instanceof ItemStamp) {
					if(!this.mergeItemStack(var5, 1, 2, false)) return ItemStack.EMPTY;
				} else {
					if(!this.mergeItemStack(var5, 2, 3, false)) return ItemStack.EMPTY;
				}
			}

			if(var5.isEmpty()) {
				var4.setByPlayer(ItemStack.EMPTY);
			} else {
				var4.setChanged();
			}
		}

		return var3;
	}
}
