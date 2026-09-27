package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityMachineTurbine;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineTurbine extends ContainerBase<TileEntityMachineTurbine> {

	public ContainerMachineTurbine(int id, Inventory invPlayer, TileEntityMachineTurbine tedf) {
		super(ModMenus.TURBINE.get(), id, tedf);

		//Fluid ID
		this.addSlot(new SlotNonRetarded(tedf, 0, 8, 17));
		this.addSlot(new SlotTakeOnly(tedf, 1, 8, 53));
		//Input IO
		this.addSlot(new SlotNonRetarded(tedf, 2, 44, 17));
		this.addSlot(new SlotTakeOnly(tedf, 3, 44, 53));
		//Battery
		this.addSlot(new SlotNonRetarded(tedf, 4, 98, 53));
		//Output IO
		this.addSlot(new SlotNonRetarded(tedf, 5, 152, 17));
		this.addSlot(new SlotTakeOnly(tedf, 6, 152, 53));

		this.addPlayerInventory(invPlayer, 8, 84);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = this.slots.get(index);

		if(var4 != null && var4.hasItem()) {
			ItemStack var5 = var4.getItem();
			var3 = var5.copy();

			if(index <= 6) {
				if(!this.mergeItemStack(var5, 7, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(!this.mergeItemStack(var5, 4, 5, false)) {
				if(!this.mergeItemStack(var5, 2, 3, false)) {
					if(!this.mergeItemStack(var5, 5, 6, false)) {
						if(!this.mergeItemStack(var5, 0, 1, false)) return ItemStack.EMPTY;
					}
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
