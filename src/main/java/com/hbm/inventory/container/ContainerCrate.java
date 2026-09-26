package com.hbm.inventory.container;

import com.hbm.blocks.generic.BlockStorageCrate.CrateType;
import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.storage.TileEntityCrate;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** All crates: a grid of the crate's slots above the player inventory, see CrateType for the layouts */
public class ContainerCrate extends ContainerBase<TileEntityCrate> {

	public final CrateType type;

	public ContainerCrate(int id, Inventory invPlayer, TileEntityCrate tile) {
		super(ModMenus.CRATE.get(), id, tile);
		this.type = tile.getCrateType();

		for(int i = 0; i < type.rows; i++) {
			for(int j = 0; j < type.cols; j++) {
				this.addSlot(new Slot(tile, j + i * type.cols, type.slotX + j * 18, 18 + i * 18));
			}
		}

		this.addPlayerInventory(invPlayer, type.invX, type.invY);
		tile.startOpen(invPlayer.player);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		tile.stopOpen(player);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		int size = type.slots;

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index < size) {
				if(!this.mergeItemStack(stack, size, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(!this.mergeItemStack(stack, 0, size, false)) {
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
