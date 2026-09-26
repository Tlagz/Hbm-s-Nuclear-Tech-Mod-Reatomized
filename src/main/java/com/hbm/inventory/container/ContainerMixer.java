package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.TileEntityMachineMixer;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMixer extends ContainerBase<TileEntityMachineMixer> {

	public ContainerMixer(int id, Inventory playerInv, TileEntityMachineMixer mixer) {
		super(ModMenus.MIXER.get(), id, mixer);

		//Battery
		this.addSlot(new Slot(mixer, 0, 12, 72));
		//Solid input
		this.addSlot(new Slot(mixer, 1, 52, 72));
		//Fluid ID
		this.addSlot(new Slot(mixer, 2, 126, 72));
		//Upgrades
		this.addSlot(new SlotUpgrade(mixer, 3, 148, 18));
		this.addSlot(new SlotUpgrade(mixer, 4, 148, 36));

		this.addPlayerInventory(playerInv, 8, 122);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 4) {
				if(!this.mergeItemStack(stack, 5, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof IBatteryItem) {
				if(!this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof IItemFluidIdentifier) {
				if(!this.mergeItemStack(stack, 2, 3, false)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof ItemMachineUpgrade) {
				if(!this.mergeItemStack(stack, 3, 5, false)) return ItemStack.EMPTY;
			} else if(!this.mergeItemStack(stack, 1, 2, false)) {
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
