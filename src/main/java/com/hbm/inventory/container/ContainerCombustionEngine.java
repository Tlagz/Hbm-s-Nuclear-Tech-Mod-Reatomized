package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.items.machine.ItemPistons;
import com.hbm.tileentity.machine.TileEntityMachineCombustionEngine;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerCombustionEngine extends ContainerBase<TileEntityMachineCombustionEngine> {

	public ContainerCombustionEngine(int id, Inventory invPlayer, TileEntityMachineCombustionEngine engine) {
		super(ModMenus.COMBUSTION_ENGINE.get(), id, engine);
		engine.openInventory();

		//Fluid container in/out
		this.addSlot(new Slot(engine, 0, 17, 17));
		this.addSlot(new SlotTakeOnly(engine, 1, 17, 53));
		//Piston set
		this.addSlot(new Slot(engine, 2, 88, 71));
		//Battery
		this.addSlot(new Slot(engine, 3, 143, 71));
		//Fluid ID
		this.addSlot(new Slot(engine, 4, 35, 71));

		this.addPlayerInventory(invPlayer, 8, 121);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack originalStack = slot.getItem();
			stack = originalStack.copy();

			if(index <= 4) {
				if(!this.mergeItemStack(originalStack, 5, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {

				if(stack.getItem() instanceof IBatteryItem) {
					if(!this.mergeItemStack(originalStack, 3, 4, false)) {
						return ItemStack.EMPTY;
					}
				} else if(stack.getItem() instanceof IItemFluidIdentifier) {
					if(!this.mergeItemStack(originalStack, 4, 5, false)) {
						return ItemStack.EMPTY;
					}
				} else if(stack.getItem() instanceof ItemPistons) {
					if(!this.mergeItemStack(originalStack, 2, 3, false)) {
						return ItemStack.EMPTY;
					}
				} else {
					if(!this.mergeItemStack(originalStack, 0, 1, false)) {
						return ItemStack.EMPTY;
					}
				}
			}

			if(originalStack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return stack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.tile.closeInventory();
	}
}
