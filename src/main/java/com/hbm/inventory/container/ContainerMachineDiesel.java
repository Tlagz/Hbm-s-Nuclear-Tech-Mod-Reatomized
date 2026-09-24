package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityMachineDiesel;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineDiesel extends ContainerBase<TileEntityMachineDiesel> {

	public ContainerMachineDiesel(int id, Inventory invPlayer, TileEntityMachineDiesel tedf) {
		super(ModMenus.DIESEL.get(), id, tedf);

		//Fluid in
		this.addSlot(new Slot(tedf, 0, 17, 17));
		//Fluid out
		this.addSlot(new SlotTakeOnly(tedf, 1, 17, 53));
		//Battery
		this.addSlot(new Slot(tedf, 2, 141, 71));
		//Fluid ID
		this.addSlot(new Slot(tedf, 3, 35, 71));

		this.addPlayerInventory(invPlayer, 8, 121);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			// the original checked index <= 4 and merged into slot 4 (a player slot), fixed for the 4 machine slots
			if(index <= 3) {
				if(!this.mergeItemStack(stack, 4, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if(!this.mergeItemStack(stack, 0, 1, false)) {
				if(!this.mergeItemStack(stack, 2, 3, false))
					if(!this.mergeItemStack(stack, 3, 4, false))
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
