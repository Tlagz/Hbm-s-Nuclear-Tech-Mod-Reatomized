package com.hbm.blocks.network;

import com.hbm.tileentity.network.TileEntityCraneInserter;
import com.hbm.util.InventoryUtil;

import api.hbm.conveyor.IConveyorItem;
import api.hbm.conveyor.IConveyorPackage;
import api.hbm.conveyor.IEnterableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Conveyor inserter: items and packages entering through its input side go into the inventory at its output side,
 * whatever does not fit goes into its own buffer, the rest gets destroyed (or dropped with the destroyer off).
 */
public class CraneInserter extends BlockCraneBase implements IEnterableBlock {

	public CraneInserter(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityCraneInserter(pos, state);
	}

	@Override
	protected int[] getDropRange() {
		return new int[] {0, 21};
	}

	/** The item handler of the block at the output side, as seen from the inserter */
	public static IItemHandler getTarget(Level world, BlockPos pos, Direction outputDirection) {
		return world.getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(outputDirection), outputDirection.getOpposite());
	}

	/** Stacks onto matching stacks first, then empty slots; returns what is left (the original's addToInventory) */
	public static ItemStack addToInventory(IItemHandler inv, ItemStack toAdd) {
		if(inv == null || toAdd.isEmpty()) return toAdd;
		return ItemHandlerHelper.insertItemStacked(inv, toAdd, false);
	}

	@Override
	public boolean canItemEnter(Level world, BlockPos pos, Direction dir, IConveyorItem entity) {
		return getInputSide(world.getBlockState(pos)) == dir;
	}

	@Override
	public void onItemEnter(Level world, BlockPos pos, Direction dir, IConveyorItem entity) {
		if(entity == null || entity.getItemStack().isEmpty()) return;
		insert(world, pos, entity.getItemStack().copy());
	}

	private void insert(Level world, BlockPos pos, ItemStack toAdd) {
		Direction outputDirection = getOutputSide(world.getBlockState(pos));

		if(!world.hasNeighborSignal(pos)) {
			toAdd = addToInventory(getTarget(world, pos, outputDirection), toAdd);
		}

		if(!toAdd.isEmpty() && world.getBlockEntity(pos) instanceof TileEntityCraneInserter inserter) {
			toAdd = InventoryUtil.tryAddItemToInventory(inserter.slots, 0, 20, toAdd);
			inserter.setChanged();

			if(!toAdd.isEmpty() && !inserter.destroyer) {
				world.addFreshEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, toAdd.copy()));
			}
		}
	}

	@Override public boolean canPackageEnter(Level world, BlockPos pos, Direction dir, IConveyorPackage entity) { return true; }

	@Override
	public void onPackageEnter(Level world, BlockPos pos, Direction dir, IConveyorPackage entity) {
		if(entity == null || entity.getItemStacks() == null) return;
		for(ItemStack stack : entity.getItemStacks()) {
			if(stack != null && !stack.isEmpty()) insert(world, pos, stack.copy());
		}
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
		return world.getBlockEntity(pos) instanceof TileEntityCraneInserter inserter ? AbstractContainerMenu.getRedstoneSignalFromContainer(inserter) : 0;
	}
}
