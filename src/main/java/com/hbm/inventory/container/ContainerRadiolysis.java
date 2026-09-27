package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityMachineRadiolysis;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerRadiolysis extends ContainerBase<TileEntityMachineRadiolysis> {

	public ContainerRadiolysis(int id, Inventory playerInv, TileEntityMachineRadiolysis tile) {
		super(ModMenus.RADIOLYSIS.get(), id, tile);

		// RTG pellets, two columns on the right
		for(int i = 0; i < 2; i++) {
			for(int j = 0; j < 5; j++) {
				this.addSlot(new SlotNonRetarded(tile, j + i * 5, 188 + i * 18, 8 + j * 18));
			}
		}

		// Fluid IDs
		this.addSlot(new SlotNonRetarded(tile, 10, 34, 17));
		this.addSlot(new SlotTakeOnly(tile, 11, 34, 53));
		// Sterilization
		this.addSlot(new SlotNonRetarded(tile, 12, 148, 17));
		this.addSlot(new SlotTakeOnly(tile, 13, 148, 53));
		// Battery
		this.addSlot(new SlotNonRetarded(tile, 14, 8, 53));

		this.addPlayerInventory(playerInv, 8, 84);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack var3 = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			var3 = stack.copy();

			if(index <= 14) {
				if(!this.mergeItemStack(stack, 15, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(!this.mergeItemStack(stack, 0, 15, false)) {
				return ItemStack.EMPTY;
			}

			if(stack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return var3;
	}
}
