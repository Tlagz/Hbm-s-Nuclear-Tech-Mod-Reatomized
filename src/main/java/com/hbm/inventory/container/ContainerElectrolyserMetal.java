package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.TileEntityElectrolyser;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerElectrolyserMetal extends ContainerBase<TileEntityElectrolyser> {

	public ContainerElectrolyserMetal(int id, Inventory invPlayer, TileEntityElectrolyser tile) {
		super(ModMenus.ELECTROLYSER_METAL.get(), id, tile);

		//Battery
		this.addSlot(new Slot(tile, 0, 186, 109));
		//Upgrades
		this.addSlot(new SlotUpgrade(tile, 1, 186, 140));
		this.addSlot(new SlotUpgrade(tile, 2, 186, 158));
		//Input
		this.addSlot(new Slot(tile, 14, 10, 22));
		//Outputs
		this.addSlot(new SlotTakeOnly(tile, 15, 136, 18));
		this.addSlot(new SlotTakeOnly(tile, 16, 154, 18));
		this.addSlot(new SlotTakeOnly(tile, 17, 136, 36));
		this.addSlot(new SlotTakeOnly(tile, 18, 154, 36));
		this.addSlot(new SlotTakeOnly(tile, 19, 136, 54));
		this.addSlot(new SlotTakeOnly(tile, 20, 154, 54));

		this.addPlayerInventory(invPlayer, 8, 122);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 9) {
				if(!this.mergeItemStack(stack, 10, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof IBatteryItem) {
				if(!this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
			} else if(rStack.getItem() instanceof ItemMachineUpgrade) {
				if(!this.mergeItemStack(stack, 1, 3, false)) return ItemStack.EMPTY;
			} else if(!this.mergeItemStack(stack, 3, 4, false)) {
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
