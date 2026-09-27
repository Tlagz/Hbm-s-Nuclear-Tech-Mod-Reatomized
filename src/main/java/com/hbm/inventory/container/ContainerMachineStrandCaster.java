package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityMachineStrandCaster;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineStrandCaster extends ContainerBase<TileEntityMachineStrandCaster> {

	public ContainerMachineStrandCaster(int id, Inventory invPlayer, TileEntityMachineStrandCaster caster) {
		super(ModMenus.STRAND_CASTER.get(), id, caster);

		//Mold
		this.addSlot(new Slot(caster, 0, 57, 62) {
			@Override public boolean mayPlace(ItemStack stack) { return caster.isItemValidForSlot(0, stack); }
			@Override public int getMaxStackSize() { return 1; }
		});

		//Output
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 2; j++) {
				this.addSlot(new SlotTakeOnly(caster, j + i * 2 + 1, 125 + j * 18, 26 + i * 18));
			}
		}

		this.addPlayerInventory(invPlayer, 8, 132);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 6) {
				if(!this.mergeItemStack(stack, 7, this.slots.size(), true)) return ItemStack.EMPTY;
				slot.onQuickCraft(stack, rStack);
			} else if(tile.isItemValidForSlot(0, stack) && tile.getItem(0).isEmpty()) {
				if(!this.mergeItemStack(stack.split(1), 0, 1, false)) return ItemStack.EMPTY;
			} else {
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
