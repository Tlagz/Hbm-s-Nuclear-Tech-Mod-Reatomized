package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.TileEntityMachineChemicalPlant;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineChemicalPlant extends ContainerBase<TileEntityMachineChemicalPlant> {

	public ContainerMachineChemicalPlant(int id, Inventory invPlayer, TileEntityMachineChemicalPlant chemicalPlant) {
		super(ModMenus.CHEMICAL_PLANT.get(), id, chemicalPlant);

		// Battery
		this.addSlot(new SlotNonRetarded(chemicalPlant, 0, 152, 81));
		// Schematic
		this.addSlot(new SlotNonRetarded(chemicalPlant, 1, 35, 126));
		// Upgrades
		this.addSlots(chemicalPlant, 2, 152, 108, 2, 1);
		// Solid Input
		this.addSlots(chemicalPlant, 4, 8, 99, 1, 3);
		// Solid Output
		this.addTakeOnlySlots(chemicalPlant, 7, 80, 99, 1, 3);
		// Fluid Input
		this.addSlots(chemicalPlant, 10, 8, 54, 1, 3);
		this.addTakeOnlySlots(chemicalPlant, 13, 8, 72, 1, 3);
		// Fluid Output
		this.addSlots(chemicalPlant, 16, 80, 54, 1, 3);
		this.addTakeOnlySlots(chemicalPlant, 19, 80, 72, 1, 3);

		this.addPlayerInventory(invPlayer, 8, 174);
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
				} else if(slotOriginal.getItem() instanceof ItemMachineUpgrade) {
					if(!this.mergeItemStack(slotStack, 2, 4, false)) return ItemStack.EMPTY;
				} else {
					if(!this.mergeItemStack(slotStack, 4, 7, false)) return ItemStack.EMPTY;
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
