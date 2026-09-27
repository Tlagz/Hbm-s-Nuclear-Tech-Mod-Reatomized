package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.TileEntitySILEX;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerSILEX extends ContainerBase<TileEntitySILEX> {

	public ContainerSILEX(int id, Inventory invPlayer, TileEntitySILEX te) {
		super(ModMenus.SILEX.get(), id, te);

		// Input
		this.addSlot(new SlotNonRetarded(te, 0, 80, 12));
		// Fluid ID
		this.addSlot(new SlotNonRetarded(te, 1, 8, 24));
		// Fluid Container
		this.addSlot(new SlotNonRetarded(te, 2, 8 + 18, 24));
		this.addSlot(new SlotTakeOnly(te, 3, 8 + 18 * 2, 24));
		// Output
		this.addSlot(new SlotTakeOnly(te, 4, 116, 90));
		// Output Queue
		this.addSlot(new SlotTakeOnly(te, 5, 134, 72));
		this.addSlot(new SlotTakeOnly(te, 6, 152, 72));
		this.addSlot(new SlotTakeOnly(te, 7, 134, 90));
		this.addSlot(new SlotTakeOnly(te, 8, 152, 90));
		this.addSlot(new SlotTakeOnly(te, 9, 134, 108));
		this.addSlot(new SlotTakeOnly(te, 10, 152, 108));

		this.addPlayerInventory(invPlayer, 8, 140);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = this.slots.get(index);

		if(var4 != null && var4.hasItem()) {
			ItemStack var5 = var4.getItem();
			var3 = var5.copy();

			if(index <= 10) {
				if(!this.mergeItemStack(var5, 11, this.slots.size(), true)) return ItemStack.EMPTY;
			} else {
				if(var3.getItem() instanceof IItemFluidIdentifier) {
					if(!this.mergeItemStack(var5, 1, 2, false)) return ItemStack.EMPTY;
				} else if(!this.mergeItemStack(var5, 0, 1, false) && !this.mergeItemStack(var5, 2, 3, false)) {
					return ItemStack.EMPTY;
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
