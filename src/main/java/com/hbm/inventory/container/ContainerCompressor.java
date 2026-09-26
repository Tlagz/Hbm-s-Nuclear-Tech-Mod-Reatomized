package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.TileEntityMachineCompressorBase;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerCompressor extends ContainerBase<TileEntityMachineCompressorBase> {

	public ContainerCompressor(int id, Inventory playerInv, TileEntityMachineCompressorBase tile) {
		super(ModMenus.COMPRESSOR.get(), id, tile);

		//Fluid ID
		this.addSlot(new Slot(tile, 0, 17, 72));
		//Battery
		this.addSlot(new Slot(tile, 1, 152, 72));
		//Upgrades
		this.addSlot(new SlotUpgrade(tile, 2, 52, 72));
		this.addSlot(new SlotUpgrade(tile, 3, 70, 72));

		this.addPlayerInventory(playerInv, 8, 122);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index < 4) {
				if(!this.mergeItemStack(stack, 4, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof IBatteryItem) {
				if(!this.mergeItemStack(stack, 1, 2, false)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof IItemFluidIdentifier) {
				if(!this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
			} else if(!this.mergeItemStack(stack, 2, 4, false)) {
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
