package com.hbm.inventory.container;

import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.oil.TileEntityMachineCatalyticReformer;
import com.hbm.tileentity.machine.oil.TileEntityMachineHydrotreater;
import com.hbm.tileentity.machine.oil.TileEntityOilProcessorBase;

import api.hbm.energymk2.IBatteryItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The original's ContainerMachineVacuumDistill, ContainerMachineCatalyticReformer and ContainerMachineHydrotreater,
 * same player inventory position, only the machine slots differ.
 */
public class ContainerOilProcessor extends ContainerBase<TileEntityOilProcessorBase> {

	/** {slot, x, y, take only (1)} */
	private static final int[][] VACUUM = {
			{0, 26, 90, 0}, {1, 44, 90, 0}, {2, 44, 108, 0}, {3, 80, 90, 0}, {4, 80, 108, 1}, {5, 98, 90, 0}, {6, 98, 108, 1},
			{7, 116, 90, 0}, {8, 116, 108, 1}, {9, 134, 90, 0}, {10, 134, 108, 1}, {11, 26, 108, 0}};
	private static final int[][] REFORMER = {
			{0, 17, 90, 0}, {1, 35, 90, 0}, {2, 35, 108, 1}, {3, 107, 90, 0}, {4, 107, 108, 1}, {5, 125, 90, 0}, {6, 125, 108, 1},
			{7, 143, 90, 0}, {8, 143, 108, 1}, {9, 17, 108, 0}, {10, 71, 36, 0}};
	private static final int[][] HYDROTREATER = {
			{0, 17, 90, 0}, {1, 35, 90, 0}, {2, 35, 108, 1}, {3, 53, 90, 0}, {4, 53, 108, 0}, {5, 125, 90, 0}, {6, 125, 108, 1},
			{7, 143, 90, 0}, {8, 143, 108, 1}, {9, 17, 108, 0}, {10, 89, 36, 0}};

	private final int machineSlots;
	private final int idSlot;
	private final int converterSlot;
	/** The container slots shift-clicks go to, in order */
	private final int[] fluidSlots;

	public ContainerOilProcessor(MenuType<?> type, int id, Inventory invPlayer, TileEntityOilProcessorBase tile) {
		super(type, id, tile);

		int[][] layout = tile instanceof TileEntityMachineCatalyticReformer ? REFORMER : tile instanceof TileEntityMachineHydrotreater ? HYDROTREATER : VACUUM;
		for(int[] s : layout) this.addSlot(s[3] == 1 ? new SlotTakeOnly(tile, s[0], s[1], s[2]) : new Slot(tile, s[0], s[1], s[2]));

		this.machineSlots = layout.length;
		this.idSlot = layout == VACUUM ? 11 : 9;
		this.converterSlot = layout == VACUUM ? -1 : 10;
		this.fluidSlots = layout == VACUUM ? new int[] {1, 3, 5, 7, 9} : new int[] {1, 3, 5, 7};

		this.addPlayerInventory(invPlayer, 8, 156);
	}

	@Override
	public ItemStack transferStackInSlot(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index < machineSlots) {
				if(!this.mergeItemStack(stack, machineSlots, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if(stack.getItem() instanceof IBatteryItem) {
				if(!this.mergeItemStack(stack, 0, 1, false)) return ItemStack.EMPTY;
			} else if(stack.getItem() instanceof IItemFluidIdentifier) {
				if(!this.mergeItemStack(stack, idSlot, idSlot + 1, false)) return ItemStack.EMPTY;
			} else if(converterSlot >= 0 && stack.is(ModItems.catalytic_converter.get())) {
				if(!this.mergeItemStack(stack, converterSlot, converterSlot + 1, false)) return ItemStack.EMPTY;
			} else {
				boolean moved = false;
				for(int s : fluidSlots) if(!moved && this.mergeItemStack(stack, s, s + 1, false)) moved = true;
				if(!moved) return ItemStack.EMPTY;
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
