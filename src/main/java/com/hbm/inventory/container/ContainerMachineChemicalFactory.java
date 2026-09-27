package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.TileEntityMachineChemicalFactory;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineChemicalFactory extends ContainerBase<TileEntityMachineChemicalFactory> {

	public ContainerMachineChemicalFactory(int id, Inventory invPlayer, TileEntityMachineChemicalFactory chemicalFactory) {
		super(ModMenus.CHEMICAL_FACTORY.get(), id, chemicalFactory);

		// Battery
		this.addSlot(new SlotNonRetarded(chemicalFactory, 0, 224, 88));
		// Upgrades
		this.addSlots(chemicalFactory, 1, 206, 125, 3, 1);

		for(int i = 0; i < 4; i++) {
			// Template
			this.addSlots(chemicalFactory, 4 + i * 7, 93, 20 + i * 22, 1, 1, 16);
			// Solid Input
			this.addSlots(chemicalFactory, 5 + i * 7, 10, 20 + i * 22, 1, 3, 16);
			// Solid Output
			this.addTakeOnlySlots(chemicalFactory, 8 + i * 7, 139, 20 + i * 22, 1, 3, 16);
		}

		this.addPlayerInventory(invPlayer, 26, 134);
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
					if(!this.mergeItemStack(slotStack, 4, 5, false) && !this.mergeItemStack(slotStack, 11, 12, false)
							&& !this.mergeItemStack(slotStack, 18, 19, false) && !this.mergeItemStack(slotStack, 25, 26, false)) return ItemStack.EMPTY;
				} else if(slotOriginal.getItem() instanceof ItemMachineUpgrade) {
					if(!this.mergeItemStack(slotStack, 1, 4, false)) return ItemStack.EMPTY;
				} else {
					if(!this.mergeItemStack(slotStack, 5, 8, false) && !this.mergeItemStack(slotStack, 12, 15, false)
							&& !this.mergeItemStack(slotStack, 19, 22, false) && !this.mergeItemStack(slotStack, 26, 29, false)) return ItemStack.EMPTY;
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
