package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityMachineMiningLaser;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMiningLaser extends ContainerBase<TileEntityMachineMiningLaser> {

	public ContainerMiningLaser(int id, Inventory invPlayer, TileEntityMachineMiningLaser tedf) {
		super(ModMenus.MINING_LASER.get(), id, tedf);

		//Battery
		this.addSlot(new SlotNonRetarded(tedf, 0, 8, 108));
		//Upgrades
		for(int i = 0; i < 2; i++)
			for(int j = 0; j < 4; j++)
				this.addSlot(new SlotUpgrade(tedf, 1 + i * 4 + j, 98 + j * 18, 18 + i * 18));
		//Output
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 7; j++)
				this.addSlot(new SlotNonRetarded(tedf, 9 + i * 7 + j, 44 + j * 18, 72 + i * 18));

		this.addPlayerInventory(invPlayer, 8, 140);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = this.slots.get(index);

		if(var4 != null && var4.hasItem()) {
			ItemStack var5 = var4.getItem();
			var3 = var5.copy();

			if(index <= 29) {
				if(!this.mergeItemStack(var5, 30, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
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
