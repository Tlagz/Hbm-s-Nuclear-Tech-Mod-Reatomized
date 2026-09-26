package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.storage.TileEntityMachineFluidTank;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineFluidTank extends ContainerBase<TileEntityMachineFluidTank> {

	public ContainerMachineFluidTank(int id, Inventory invPlayer, TileEntityMachineFluidTank tank) {
		super(ModMenus.FLUID_TANK.get(), id, tank);

		//Fluid ID
		this.addSlot(new Slot(tank, 0, 8, 17));
		this.addSlot(new SlotTakeOnly(tank, 1, 8, 53));
		//Input IO
		this.addSlot(new Slot(tank, 2, 53 - 18, 17));
		this.addSlot(new SlotTakeOnly(tank, 3, 53 - 18, 53));
		//Output IO
		this.addSlot(new Slot(tank, 4, 125, 17));
		this.addSlot(new SlotTakeOnly(tank, 5, 125, 53));

		this.addPlayerInventory(invPlayer, 8, 84);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 5) {
				if(!this.mergeItemStack(stack, 6, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if(!this.mergeItemStack(stack, 0, 6, false)) {
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
