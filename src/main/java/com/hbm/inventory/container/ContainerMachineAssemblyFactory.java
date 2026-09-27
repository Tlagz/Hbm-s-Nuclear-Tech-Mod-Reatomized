package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBlueprints;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.TileEntityMachineAssemblyFactory;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineAssemblyFactory extends ContainerBase<TileEntityMachineAssemblyFactory> {

	public ContainerMachineAssemblyFactory(int id, Inventory invPlayer, TileEntityMachineAssemblyFactory assemFac) {
		super(ModMenus.ASSEMBLY_FACTORY.get(), id, assemFac);

		// Battery
		this.addSlot(new SlotNonRetarded(assemFac, 0, 234, 112));
		// Upgrades
		this.addSlots(assemFac, 1, 214, 149, 3, 1);

		for(int i = 0; i < 4; i++) {
			// Template
			this.addSlots(assemFac, 4 + i * 14, 25 + (i % 2) * 109, 54 + (i / 2) * 56, 1, 1);
			// Solid Input
			this.addSlots(assemFac, 5 + i * 14, 7 + (i % 2) * 109, 20 + (i / 2) * 56, 2, 6, 16);
			// Solid Output
			this.addTakeOnlySlots(assemFac, 17 + i * 14, 87 + (i % 2) * 109, 54 + (i / 2) * 56, 1, 1);
		}

		this.addPlayerInventory(invPlayer, 33, 158);
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
					if(!this.mergeItemStack(slotStack, 4, 5, false) && !this.mergeItemStack(slotStack, 18, 19, false)
							&& !this.mergeItemStack(slotStack, 32, 33, false) && !this.mergeItemStack(slotStack, 46, 47, false)) return ItemStack.EMPTY;
				} else if(slotOriginal.getItem() instanceof ItemMachineUpgrade) {
					if(!this.mergeItemStack(slotStack, 1, 4, false)) return ItemStack.EMPTY;
				} else {
					if(!this.mergeItemStack(slotStack, 5, 17, false) && !this.mergeItemStack(slotStack, 19, 31, false)
							&& !this.mergeItemStack(slotStack, 33, 45, false) && !this.mergeItemStack(slotStack, 47, 59, false)) return ItemStack.EMPTY;
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
