package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.TileEntityMachineCyclotron;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineCyclotron extends ContainerBase<TileEntityMachineCyclotron> {

	public ContainerMachineCyclotron(int id, Inventory invPlayer, TileEntityMachineCyclotron tile) {
		super(ModMenus.CYCLOTRON.get(), id, tile);

		//Input
		this.addSlot(new SlotNonRetarded(tile, 0, 11, 18));
		this.addSlot(new SlotNonRetarded(tile, 1, 11, 36));
		this.addSlot(new SlotNonRetarded(tile, 2, 11, 54));
		//Targets
		this.addSlot(new SlotNonRetarded(tile, 3, 101, 18));
		this.addSlot(new SlotNonRetarded(tile, 4, 101, 36));
		this.addSlot(new SlotNonRetarded(tile, 5, 101, 54));
		//Output
		this.addSlot(new SlotTakeOnly(tile, 6, 131, 18));
		this.addSlot(new SlotTakeOnly(tile, 7, 131, 36));
		this.addSlot(new SlotTakeOnly(tile, 8, 131, 54));
		//Battery
		this.addSlot(new SlotNonRetarded(tile, 9, 168, 83));
		//Upgrades
		this.addSlot(new SlotUpgrade(tile, 10, 60, 81));
		this.addSlot(new SlotUpgrade(tile, 11, 78, 81));

		this.addPlayerInventory(invPlayer, 15, 133);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {

		ItemStack var3 = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			var3 = stack.copy();

			// the original checked index <= 15 for its 12 machine slots
			if(index <= 11) {
				if(!this.mergeItemStack(stack, 12, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}

			} else {

				if(stack.getItem() instanceof IBatteryItem || stack.is(ModItems.battery_creative.get())) {
					if(!this.mergeItemStack(stack, 9, 10, true))
						return ItemStack.EMPTY;

				} else if(stack.getItem() instanceof ItemMachineUpgrade) {
					if(!this.mergeItemStack(stack, 10, 11, true))
						if(!this.mergeItemStack(stack, 11, 12, true))
							return ItemStack.EMPTY;

				} else {

					if(stack.is(ModItems.part_lithium.get()) || stack.is(ModItems.part_beryllium.get()) || stack.is(ModItems.part_carbon.get()) ||
							stack.is(ModItems.part_copper.get()) || stack.is(ModItems.part_plutonium.get())) {

						if(!this.mergeItemStack(stack, 0, 3, true))
							return ItemStack.EMPTY;
					} else {

						if(!this.mergeItemStack(stack, 3, 6, true))
							return ItemStack.EMPTY;
					}
				}
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
