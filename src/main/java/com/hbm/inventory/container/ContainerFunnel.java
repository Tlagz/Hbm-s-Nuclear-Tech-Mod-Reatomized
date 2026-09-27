package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityMachineFunnel;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerFunnel extends ContainerBase<TileEntityMachineFunnel> {

	public ContainerFunnel(int id, Inventory playerInv, TileEntityMachineFunnel tile) {
		super(ModMenus.FUNNEL.get(), id, tile);

		for(int i = 0; i < 9; i++) this.addSlot(new SlotNonRetarded(tile, i, 8 + 18 * i, 18));
		for(int i = 0; i < 9; i++) this.addSlot(new SlotTakeOnly(tile, i + 9, 8 + 18 * i, 54));

		this.addPlayerInventory(playerInv, 8, 86);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = this.slots.get(index);

		if(var4 != null && var4.hasItem()) {
			ItemStack var5 = var4.getItem();
			var3 = var5.copy();

			if(index <= 17) {
				if(!this.mergeItemStack(var5, 18, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(!this.mergeItemStack(var5, 0, 9, false)) {
				return ItemStack.EMPTY;
			}

			if(var5.isEmpty()) {
				var4.setByPlayer(ItemStack.EMPTY);
			} else {
				var4.setChanged();
			}
		}

		return var3;
	}
}
