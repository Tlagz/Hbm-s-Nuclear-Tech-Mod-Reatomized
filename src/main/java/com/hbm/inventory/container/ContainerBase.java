package com.hbm.inventory.container;

import com.hbm.tileentity.TileEntityMachineBase;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Base of machine menus. The tile's state is synced separately (networkPackNT), the menu only handles slots.
 * mergeItemStack/transferStackInSlot are the original names of moveItemStackTo/quickMoveStack.
 */
public abstract class ContainerBase<T extends TileEntityMachineBase> extends AbstractContainerMenu {

	public final T tile;

	protected ContainerBase(MenuType<?> type, int id, T tile) {
		super(type, id);
		this.tile = tile;
	}

	/** The standard player inventory, top left corner of the 3x9 grid, hotbar 58 pixels below */
	protected void addPlayerInventory(Inventory inv, int x, int y) {
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlot(new Slot(inv, j + i * 9 + 9, x + j * 18, y + i * 18));
			}
		}
		for(int i = 0; i < 9; i++) {
			this.addSlot(new Slot(inv, i, x + i * 18, y + 58));
		}
	}

	protected boolean mergeItemStack(ItemStack stack, int start, int end, boolean reverse) {
		return this.moveItemStackTo(stack, start, end, reverse);
	}

	/** Shift click, the original's transferStackInSlot. Return ItemStack.EMPTY where the original returned null. */
	public abstract ItemStack transferStackInSlot(Player player, int index);

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return transferStackInSlot(player, index);
	}

	@Override
	public boolean stillValid(Player player) {
		return tile.stillValid(player);
	}
}
