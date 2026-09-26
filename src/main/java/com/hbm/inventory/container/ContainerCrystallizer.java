package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.TileEntityMachineCrystallizer;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerCrystallizer extends ContainerBase<TileEntityMachineCrystallizer> {

	public ContainerCrystallizer(int id, Inventory invPlayer, TileEntityMachineCrystallizer crystallizer) {
		super(ModMenus.CRYSTALLIZER.get(), id, crystallizer);

		//Input
		this.addSlot(new Slot(crystallizer, 0, 62, 45));
		//Battery
		this.addSlot(new Slot(crystallizer, 1, 152, 72));
		//Output
		this.addSlot(new SlotTakeOnly(crystallizer, 2, 113, 45));
		//Fluid slots
		this.addSlot(new Slot(crystallizer, 3, 17, 18));
		this.addSlot(new SlotTakeOnly(crystallizer, 4, 17, 54));
		//Upgrades
		this.addSlot(new SlotUpgrade(crystallizer, 5, 80, 18));
		this.addSlot(new SlotUpgrade(crystallizer, 6, 98, 18));
		//Fluid ID
		this.addSlot(new Slot(crystallizer, 7, 35, 72));

		this.addPlayerInventory(invPlayer, 8, 122);
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
			} else {

				if(rStack.getItem() instanceof IBatteryItem) {
					if(!this.mergeItemStack(stack, 1, 2, false))
						return ItemStack.EMPTY;
				} else if(rStack.getItem() instanceof IItemFluidIdentifier) {
					if(!this.mergeItemStack(stack, 7, 8, false))
						return ItemStack.EMPTY;
				} else if(rStack.getItem() instanceof ItemMachineUpgrade) {
					if(!this.mergeItemStack(stack, 5, 7, false))
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
