package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.tileentity.network.TileEntityCraneExtractor;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerCraneExtractor extends ContainerBase<TileEntityCraneExtractor> {

	protected TileEntityCraneExtractor extractor;

	public ContainerCraneExtractor(int id, Inventory invPlayer, TileEntityCraneExtractor extractor) {
		super(ModMenus.CRANE_EXTRACTOR.get(), id, extractor);
		this.extractor = extractor;

		//filter
		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 3; j++) {
				this.addSlot(new SlotPattern(extractor, j + i * 3, 71 + j * 18, 17 + i * 18));
			}
		}

		//buffer
		addSlots(extractor, 9, 8, 17, 3, 3);

		//upgrades
		this.addSlot(new SlotUpgrade(extractor, 18, 152, 23));
		this.addSlot(new SlotUpgrade(extractor, 19, 152, 47));

		this.addPlayerInventory(invPlayer, 26, 103);
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

			if(slot <= 19) {
				if(!this.mergeItemStack(var5, 20, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {

				if(var3.getItem() instanceof ItemMachineUpgrade && TileEntityCraneExtractor.getStackAmount(var3) > 1) {
					if(!this.mergeItemStack(var5, 18, 19, false)) return ItemStack.EMPTY;
				} else if(var3.getItem() instanceof ItemMachineUpgrade && TileEntityCraneExtractor.getEjectorDelay(var3) < 20) {
					if(!this.mergeItemStack(var5, 19, 20, false)) return ItemStack.EMPTY;
				} else if(!this.mergeItemStack(var5, 9, 18, false)) {
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
			extractor.nextMode(index);
		} else {
			slot.set(held.isEmpty() ? ItemStack.EMPTY : held.copyWithCount(1));
			extractor.matcher.initPatternStandard(extractor.getLevel(), slot.getItem(), index);
		}
	}
}
