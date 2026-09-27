package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.ModItems;
import com.hbm.tileentity.machine.TileEntityMicrowave;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMicrowave extends ContainerBase<TileEntityMicrowave> {

	public ContainerMicrowave(int id, Inventory invPlayer, TileEntityMicrowave tedf) {
		super(ModMenus.MICROWAVE.get(), id, tedf);

		this.addSlot(new SlotNonRetarded(tedf, 0, 80, 35));
		this.addSlot(new SlotTakeOnly(tedf, 1, 140, 35));
		this.addSlot(new SlotNonRetarded(tedf, 2, 8, 53));

		this.addPlayerInventory(invPlayer, 8, 84);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = this.slots.get(index);

		if(var4 != null && var4.hasItem()) {
			ItemStack var5 = var4.getItem();
			var3 = var5.copy();

			if(index <= 2) {
				if(!this.mergeItemStack(var5, 3, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(var3.getItem() instanceof IBatteryItem || var3.is(ModItems.battery_creative.get())) {
				if(!this.mergeItemStack(var5, 2, 3, false)) return ItemStack.EMPTY;
			} else if(!this.mergeItemStack(var5, 0, 1, false)) {
				return ItemStack.EMPTY;
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
