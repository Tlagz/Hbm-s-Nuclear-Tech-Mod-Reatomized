package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.ItemRTGPellet;
import com.hbm.tileentity.machine.TileEntityRtgFurnace;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerRtgFurnace extends ContainerBase<TileEntityRtgFurnace> {

	public ContainerRtgFurnace(int id, Inventory invPlayer, TileEntityRtgFurnace tedf) {
		super(ModMenus.RTG_FURNACE.get(), id, tedf);

		this.addSlot(new SlotNonRetarded(tedf, 0, 56, 17));
		this.addSlot(new SlotNonRetarded(tedf, 1, 38, 53));
		this.addSlot(new SlotNonRetarded(tedf, 2, 56, 53));
		this.addSlot(new SlotNonRetarded(tedf, 3, 74, 53));
		this.addSlot(new SlotTakeOnly(tedf, 4, 116, 35));

		this.addPlayerInventory(invPlayer, 8, 84);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = this.slots.get(index);

		if(var4 != null && var4.hasItem()) {
			ItemStack var5 = var4.getItem();
			var3 = var5.copy();

			if(index <= 4) {
				if(!this.mergeItemStack(var5, 5, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(var5.getItem() instanceof ItemRTGPellet) {
				if(!this.mergeItemStack(var5, 1, 4, false)) return ItemStack.EMPTY;
			} else if(!this.mergeItemStack(var5, 0, 1, false)) {
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
