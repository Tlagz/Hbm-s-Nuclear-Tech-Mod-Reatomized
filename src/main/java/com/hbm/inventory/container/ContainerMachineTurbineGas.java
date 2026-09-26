package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FT_Combustible.FuelGrade;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.TileEntityMachineTurbineGas;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMachineTurbineGas extends ContainerBase<TileEntityMachineTurbineGas> {

	public ContainerMachineTurbineGas(int id, Inventory invPlayer, TileEntityMachineTurbineGas tile) {
		super(ModMenus.TURBINE_GAS.get(), id, tile);

		//Battery
		this.addSlot(new Slot(tile, 0, 8, 109));
		//Fluid ID
		this.addSlot(new Slot(tile, 1, 36, 17));

		this.addPlayerInventory(invPlayer, 8, 141);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index <= 1) {
				if(!this.mergeItemStack(stack, 2, this.slots.size(), true)) return ItemStack.EMPTY;
			} else if(stack.getItem() instanceof IBatteryItem) {
				if(!this.mergeItemStack(stack, 0, 1, true)) return ItemStack.EMPTY;
			} else if(stack.getItem() instanceof IItemFluidIdentifier id) {
				// only gas fuels
				FluidType type = id.getType(tile.getLevel(), tile.getBlockPos(), stack);
				if(!(type.hasTrait(FT_Combustible.class) && type.getTrait(FT_Combustible.class).getGrade() == FuelGrade.GAS)) return ItemStack.EMPTY;
				if(!this.mergeItemStack(stack, 1, 2, true)) return ItemStack.EMPTY;
			} else {
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
