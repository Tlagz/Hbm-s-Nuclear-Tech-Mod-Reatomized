package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityAshpit;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** The ashpit's door opens while the menu is open */
public class ContainerAshpit extends ContainerBase<TileEntityAshpit> {

	public ContainerAshpit(int id, Inventory invPlayer, TileEntityAshpit ashpit) {
		super(ModMenus.ASHPIT.get(), id, ashpit);
		ashpit.startOpen(invPlayer.player);

		for(int i = 0; i < 5; i++) this.addSlot(new SlotTakeOnly(ashpit, i, 44 + i * 18, 27));

		this.addPlayerInventory(invPlayer, 8, 86);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack originalStack = slot.getItem();
			stack = originalStack.copy();

			if(index <= 4) {
				if(!this.mergeItemStack(originalStack, 5, this.slots.size(), true)) return ItemStack.EMPTY;
			} else {
				return ItemStack.EMPTY;
			}

			if(originalStack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return stack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.tile.stopOpen(player);
	}
}
