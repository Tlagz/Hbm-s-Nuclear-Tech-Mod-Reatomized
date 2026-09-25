package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityMachineBlastFurnace;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerBlastFurnace extends ContainerBase<TileEntityMachineBlastFurnace> {

	public ContainerBlastFurnace(int id, Inventory invPlayer, TileEntityMachineBlastFurnace tedf) {
		super(ModMenus.BLAST_FURNACE.get(), id, tedf);

		// Fuel
		this.addSlot(new SlotNonRetarded(tedf, 0, 80, 81));
		// Input
		this.addSlot(new SlotNonRetarded(tedf, 1, 80, 27));
		this.addSlot(new SlotNonRetarded(tedf, 2, 80, 45));
		// Output
		this.addSlot(new SlotTakeOnly(tedf, 3, 134, 72));
		this.addSlot(new SlotTakeOnly(tedf, 4, 134, 90));

		this.addPlayerInventory(invPlayer, 8, 140);
	}

	/** The original used ContainerBase's generic shift click: machine to player, player into the first fitting machine slot */
	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 4) {
				if(!this.mergeItemStack(stack, 5, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(!this.mergeItemStack(stack, 0, 3, false)) {
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
