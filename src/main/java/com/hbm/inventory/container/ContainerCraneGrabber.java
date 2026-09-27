package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.network.TileEntityCraneGrabber;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerCraneGrabber extends ContainerBase<TileEntityCraneGrabber> {

	protected TileEntityCraneGrabber grabber;

	public ContainerCraneGrabber(int id, Inventory invPlayer, TileEntityCraneGrabber grabber) {
		super(ModMenus.CRANE_GRABBER.get(), id, grabber);
		this.grabber = grabber;

		//filter
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				this.addSlot(new SlotPattern(grabber, j + i * 3, 40 + j * 18, 17 + i * 18));
			}
		}

		//upgrades
		this.addSlot(new SlotUpgrade(grabber, 9, 121, 23));
		this.addSlot(new SlotUpgrade(grabber, 10, 121, 47));

		this.addPlayerInventory(invPlayer, 8, 103);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int slot) {
		Slot var4 = this.slots.get(slot);

		if(var4 != null && var4.hasItem()) {
			ItemStack var5 = var4.getItem();
			ItemStack var3 = var5.copy();

			if(slot < 9) { //filters
				return ItemStack.EMPTY;
			}

			if(slot <= 10) {
				if(!this.mergeItemStack(var5, 11, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {

				if(var3.getItem() instanceof ItemMachineUpgrade && TileEntityCraneGrabber.getStackAmount(var3) > 1) {
					if(!this.mergeItemStack(var5, 9, 10, false)) return ItemStack.EMPTY;
				} else if(var3.getItem() instanceof ItemMachineUpgrade && TileEntityCraneGrabber.getEjectorDelay(var3) < 20) {
					if(!this.mergeItemStack(var5, 10, 11, false)) return ItemStack.EMPTY;
				} else {
					return ItemStack.EMPTY;
				}
			}

			if(var5.isEmpty()) {
				var4.setByPlayer(ItemStack.EMPTY);
			} else {
				var4.setChanged();
			}

			return var3;
		}

		return ItemStack.EMPTY;
	}

	/** Filter slots take a ghost copy of the held item, right-clicking a set filter cycles its mode */
	@Override
	public void clicked(int index, int button, ClickType type, Player player) {

		if(index < 0 || index > 8) {
			super.clicked(index, button, type, player);
			return;
		}

		Slot slot = this.getSlot(index);
		ItemStack held = getCarried();

		if(button == 1 && type == ClickType.PICKUP && slot.hasItem()) {
			grabber.nextMode(index);
		} else {
			slot.set(held.isEmpty() ? ItemStack.EMPTY : held.copyWithCount(1));
			grabber.matcher.initPatternStandard(grabber.getLevel(), slot.getItem(), index);
		}
	}
}
