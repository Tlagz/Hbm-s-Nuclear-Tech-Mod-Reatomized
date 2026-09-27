package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityDiFurnace;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerDiFurnace extends ContainerBase<TileEntityDiFurnace> {

	public ContainerDiFurnace(int id, Inventory invPlayer, TileEntityDiFurnace tedf) {
		super(ModMenus.DI_FURNACE.get(), id, tedf);

		this.addSlot(new SlotNonRetarded(tedf, 0, 80, 18));
		this.addSlot(new SlotNonRetarded(tedf, 1, 80, 54));
		this.addSlot(new SlotNonRetarded(tedf, 2, 8, 36));
		this.addSlot(new SlotTakeOnly(tedf, 3, 134, 36));

		this.addPlayerInventory(invPlayer, 8, 84);
	}

	/** Right clicking an empty input or fuel slot with nothing held cycles the side it accepts automation from */
	@Override
	public void clicked(int index, int button, ClickType clickType, Player player) {
		if(index >= 0 && index < 3 && button == 1 && clickType == ClickType.PICKUP) {
			Slot slot = this.getSlot(index);
			if(!slot.hasItem() && this.getCarried().isEmpty()) {
				if(!player.level().isClientSide) {
					if(index == 0) tile.sideUpper = (byte) ((tile.sideUpper + 1) % 6);
					if(index == 1) tile.sideLower = (byte) ((tile.sideLower + 1) % 6);
					if(index == 2) tile.sideFuel = (byte) ((tile.sideFuel + 1) % 6);
					tile.setChanged();
				}
				return;
			}
		}

		super.clicked(index, button, clickType, player);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack var3 = ItemStack.EMPTY;
		Slot var4 = this.slots.get(index);

		if(var4 != null && var4.hasItem()) {
			ItemStack var5 = var4.getItem();
			var3 = var5.copy();

			if(index <= 3) {
				if(!this.mergeItemStack(var5, 4, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if(!this.mergeItemStack(var5, 0, 3, false)) {
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
