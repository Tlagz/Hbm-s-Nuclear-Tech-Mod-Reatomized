package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.machine.TileEntityMachineExposureChamber;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Menu slots: 0 particle, 1 particle container, 2 ingredient, 3 output, 4 battery, 5-6 upgrades (tile slot 1 is internal) */
public class ContainerMachineExposureChamber extends ContainerBase<TileEntityMachineExposureChamber> {

	public ContainerMachineExposureChamber(int id, Inventory invPlayer, TileEntityMachineExposureChamber tedf) {
		super(ModMenus.EXPOSURE_CHAMBER.get(), id, tedf);

		this.addSlot(new SlotNonRetarded(tedf, 0, 8, 18));
		this.addSlot(new SlotTakeOnly(tedf, 2, 8, 54));
		this.addSlot(new SlotNonRetarded(tedf, 3, 80, 36));
		this.addSlot(new SlotTakeOnly(tedf, 4, 116, 36));
		this.addSlot(new SlotNonRetarded(tedf, 5, 152, 54));
		this.addSlot(new SlotUpgrade(tedf, 6, 44, 54));
		this.addSlot(new SlotUpgrade(tedf, 7, 62, 54));

		this.addPlayerInventory(invPlayer, 8, 104);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = this.slots.get(index);

		if(var4 != null && var4.hasItem()) {
			ItemStack var5 = var4.getItem();
			var3 = var5.copy();

			if(index <= 6) {
				if(!this.mergeItemStack(var5, 7, this.slots.size(), true)) return ItemStack.EMPTY;
			} else {
				if(var3.getItem() instanceof ItemMachineUpgrade) {
					if(!this.mergeItemStack(var5, 5, 7, false)) return ItemStack.EMPTY;
				} else if(var3.getItem() instanceof IBatteryItem || var3.is(ModItems.battery_creative.get())) {
					if(!this.mergeItemStack(var5, 4, 5, false)) return ItemStack.EMPTY;
				} else {
					if(!this.mergeItemStack(var5, 0, 3, false)) return ItemStack.EMPTY;
				}
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
