package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.TileEntityMachineCentrifuge;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerCentrifuge extends ContainerBase<TileEntityMachineCentrifuge> {

	public ContainerCentrifuge(int id, Inventory invPlayer, TileEntityMachineCentrifuge centrifuge) {
		super(ModMenus.CENTRIFUGE.get(), id, centrifuge);

		// Input
		this.addSlot(new Slot(centrifuge, 0, 44, 57));
		// Battery
		this.addSlot(new Slot(centrifuge, 1, 8, 57));
		// Outputs
		this.addSlot(new SlotTakeOnly(centrifuge, 2, 70, 57));
		this.addSlot(new SlotTakeOnly(centrifuge, 3, 90, 57));
		this.addSlot(new SlotTakeOnly(centrifuge, 4, 110, 57));
		this.addSlot(new SlotTakeOnly(centrifuge, 5, 130, 57));
		// Upgrades
		this.addSlot(new SlotUpgrade(centrifuge, 6, 156, 31));
		this.addSlot(new SlotUpgrade(centrifuge, 7, 156, 49));

		this.addPlayerInventory(invPlayer, 11, 107);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 7) {
				if(!this.mergeItemStack(stack, 8, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(stack, rStack);

			} else {

				if(rStack.getItem() instanceof IBatteryItem) {
					if(!this.mergeItemStack(stack, 1, 2, false))
						return ItemStack.EMPTY;
				} else if(rStack.getItem() instanceof ItemMachineUpgrade) {
					if(!this.mergeItemStack(stack, 6, 8, false))
						return ItemStack.EMPTY;
				} else if(!this.mergeItemStack(stack, 0, 1, false))
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
