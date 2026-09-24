package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityMachineElectricFurnace;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** TODO upgrade slot accepts machine upgrades once they exist */
public class ContainerElectricFurnace extends ContainerBase<TileEntityMachineElectricFurnace> {

	public ContainerElectricFurnace(int id, Inventory invPlayer, TileEntityMachineElectricFurnace tedf) {
		super(ModMenus.ELECTRIC_FURNACE.get(), id, tedf);

		this.addSlot(new Slot(tedf, 0, 152, 54) {
			@Override public boolean mayPlace(ItemStack stack) { return tedf.isItemValidForSlot(0, stack); }
		});
		this.addSlot(new Slot(tedf, 1, 20, 35));
		this.addSlot(new FurnaceResultSlot(invPlayer.player, tedf, 2, 80, 35));
		//Upgrades
		this.addSlot(new Slot(tedf, 3, 111, 34) {
			@Override public boolean mayPlace(ItemStack stack) { return stack.getItem() instanceof com.hbm.items.machine.ItemMachineUpgrade; }
		});

		this.addPlayerInventory(invPlayer, 8, 104);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 3) {
				if(!this.mergeItemStack(stack, 4, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(stack, rStack);
			} else {

				if(rStack.getItem() instanceof IBatteryItem) {
					if(!this.mergeItemStack(stack, 0, 1, false))
						return ItemStack.EMPTY;

				} else if(!this.mergeItemStack(stack, 1, 2, false))
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
