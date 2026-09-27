package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.tileentity.machine.TileEntityMachineRockMill;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineRockMill extends ContainerBase<TileEntityMachineRockMill> {

	public ContainerMachineRockMill(int id, Inventory invPlayer, TileEntityMachineRockMill rockMill) {
		super(ModMenus.ROCK_MILL.get(), id, rockMill);

		// Battery
		this.addSlot(new SlotNonRetarded(rockMill, 0, 152, 91));
		// Schematic
		this.addSlot(new SlotNonRetarded(rockMill, 1, 35, 90));
		// Solid Input
		this.addSlots(rockMill, 2, 8, 27, 1, 3);
		// Solid Output
		this.addTakeOnlySlots(rockMill, 5, 80, 27, 1, 3);

		this.addPlayerInventory(invPlayer, 8, 138);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack slotOriginal = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack slotStack = slot.getItem();
			slotOriginal = slotStack.copy();

			if(index <= tile.getContainerSize() - 1) {
				if(!this.mergeItemStack(slotStack, tile.getContainerSize(), this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {

				if(slotOriginal.getItem() instanceof IBatteryItem || slotOriginal.is(ModItems.battery_creative.get())) {
					if(!this.mergeItemStack(slotStack, 0, 1, false)) return ItemStack.EMPTY;
				} else if(slotOriginal.getItem() instanceof ItemBlueprints) {
					if(!this.mergeItemStack(slotStack, 1, 2, false)) return ItemStack.EMPTY;
				} else {
					if(!this.mergeItemStack(slotStack, 2, 5, false)) return ItemStack.EMPTY;
				}
			}

			if(slotStack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			slot.onTake(player, slotStack);
		}

		return slotOriginal;
	}
}
