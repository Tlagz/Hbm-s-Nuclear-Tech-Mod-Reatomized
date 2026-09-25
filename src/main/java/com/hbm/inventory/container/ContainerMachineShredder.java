package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.ItemBlades;
import com.hbm.tileentity.machine.TileEntityMachineShredder;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineShredder extends ContainerBase<TileEntityMachineShredder> {

	public ContainerMachineShredder(int id, Inventory invPlayer, TileEntityMachineShredder tedf) {
		super(ModMenus.SHREDDER.get(), id, tedf);

		// inputs
		for(int i = 0; i < 3; i++) for(int j = 0; j < 3; j++) this.addSlot(new Slot(tedf, j + i * 3, 44 + j * 18, 18 + i * 18));
		// outputs
		for(int i = 0; i < 6; i++) for(int j = 0; j < 3; j++) this.addSlot(new SlotTakeOnly(tedf, 9 + j + i * 3, 116 + j * 18, 18 + i * 18));
		// blades, battery
		this.addSlot(new Slot(tedf, 27, 44, 108));
		this.addSlot(new Slot(tedf, 28, 80, 108));
		this.addSlot(new Slot(tedf, 29, 8, 108));

		this.addPlayerInventory(invPlayer, 8, 151);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 29) {
				if(!this.mergeItemStack(stack, 30, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if(rStack.getItem() instanceof ItemBlades) {
				if(!this.mergeItemStack(stack, 27, 29, false)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof IBatteryItem) {
				if(!this.mergeItemStack(stack, 29, 30, false)) return ItemStack.EMPTY;
			} else if(!this.mergeItemStack(stack, 0, 9, false)) {
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
