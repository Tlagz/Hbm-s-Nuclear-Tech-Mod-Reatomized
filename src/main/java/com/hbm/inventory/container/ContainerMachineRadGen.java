package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityMachineRadGen;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineRadGen extends ContainerBase<TileEntityMachineRadGen> {

	public ContainerMachineRadGen(int id, Inventory invPlayer, TileEntityMachineRadGen tedf) {
		super(ModMenus.RADGEN.get(), id, tedf);

		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 3; j++) {
				this.addSlot(new SlotNonRetarded(tedf, j + i * 3, 8 + j * 18, 17 + i * 18));
			}
		}
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 3; j++) {
				this.addSlot(new SlotTakeOnly(tedf, j + i * 3 + 12, 116 + j * 18, 17 + i * 18));
			}
		}

		this.addPlayerInventory(invPlayer, 8, 102);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = this.slots.get(index);

		if(var4 != null && var4.hasItem()) {
			ItemStack var5 = var4.getItem();
			var3 = var5.copy();

			if(index <= 23) {
				if(!this.mergeItemStack(var5, 24, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if(!this.mergeItemStack(var5, 0, 12, false)) {
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
