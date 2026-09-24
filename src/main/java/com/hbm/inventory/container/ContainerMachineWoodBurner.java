package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.tileentity.machine.TileEntityMachineWoodBurner;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public class ContainerMachineWoodBurner extends ContainerBase<TileEntityMachineWoodBurner> {

	public ContainerMachineWoodBurner(int id, Inventory invPlayer, TileEntityMachineWoodBurner burner) {
		super(ModMenus.WOOD_BURNER.get(), id, burner);

		//Fuel
		this.addSlot(new Slot(burner, 0, 26, 18));
		//Ashes
		this.addSlot(new SlotTakeOnly(burner, 1, 26, 54));
		//Fluid ID
		this.addSlot(new Slot(burner, 2, 98, 54));
		//Fluid Container
		this.addSlot(new Slot(burner, 3, 98, 18));
		this.addSlot(new SlotTakeOnly(burner, 4, 98, 36));
		//Battery
		this.addSlot(new Slot(burner, 5, 143, 54));

		this.addPlayerInventory(invPlayer, 8, 104);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack stack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack originalStack = slot.getItem();
			stack = originalStack.copy();

			if(index <= 5) {
				if(!this.mergeItemStack(originalStack, 6, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(originalStack, stack);

			} else {

				if(stack.getItem() instanceof IBatteryItem) {
					if(!this.mergeItemStack(originalStack, 5, 6, false)) {
						return ItemStack.EMPTY;
					}
				} else if(stack.getBurnTime(RecipeType.SMELTING) > 0) {
					if(!this.mergeItemStack(originalStack, 0, 1, false)) {
						return ItemStack.EMPTY;
					}
				} else {
					if(!this.mergeItemStack(originalStack, 3, 4, false)) {
						return ItemStack.EMPTY;
					}
				}
			}

			if(originalStack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return stack;
	}
}
