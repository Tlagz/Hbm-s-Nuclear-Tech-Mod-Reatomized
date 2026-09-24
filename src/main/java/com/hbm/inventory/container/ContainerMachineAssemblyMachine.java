package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.TileEntityMachineAssemblyMachine;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineAssemblyMachine extends ContainerBase<TileEntityMachineAssemblyMachine> {

	public ContainerMachineAssemblyMachine(int id, Inventory invPlayer, TileEntityMachineAssemblyMachine assembler) {
		super(ModMenus.ASSEMBLY_MACHINE.get(), id, assembler);

		// Battery
		this.addSlot(new SlotNonRetarded(assembler, 0, 152, 81));
		// Schematic
		this.addSlot(new SlotNonRetarded(assembler, 1, 35, 126));
		// Upgrades
		this.addSlots(assembler, 2, 152, 108, 2, 1);
		// Input
		this.addSlots(assembler, 4, 8, 18, 4, 3);
		// Output
		this.addSlot(new SlotTakeOnly(assembler, 16, 98, 45));

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
				} else if(slotOriginal.is(ModItems.blueprints.get())) {
					if(!this.mergeItemStack(slotStack, 1, 2, false)) return ItemStack.EMPTY;
				} else if(slotOriginal.getItem() instanceof ItemMachineUpgrade) {
					if(!this.mergeItemStack(slotStack, 2, 4, false)) return ItemStack.EMPTY;
				} else {
					if(!this.mergeItemStack(slotStack, 4, 16, false)) return ItemStack.EMPTY;
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
