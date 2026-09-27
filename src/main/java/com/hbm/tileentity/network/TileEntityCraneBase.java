package com.hbm.tileentity.network;

import com.hbm.blocks.network.BlockCraneBase;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.items.ModItems;
import com.hbm.module.ModulePatternMatcher;
import com.hbm.tileentity.TileEntityMachineBase;

import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IEnterableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

/** Cranes: the input and output sides live in the block state (the original's metadata plus the output override) */
public abstract class TileEntityCraneBase extends TileEntityMachineBase {

	public TileEntityCraneBase(BlockEntityType<?> type, BlockPos pos, BlockState state, int scount) {
		super(type, pos, state, scount);
	}

	public Direction getInputSide() {
		return BlockCraneBase.getInputSide(getBlockState());
	}

	public Direction getOutputSide() {
		return BlockCraneBase.getOutputSide(getBlockState());
	}

	/** Not facing up or down (the original's metadata > 1) */
	public boolean isHorizontal() {
		return getInputSide().getAxis().isHorizontal();
	}

	/** Ticks between two operations with an ejection speed upgrade (the original's upgrade_ejector metadata) */
	public static int getEjectorDelay(ItemStack stack) {
		if(stack.is(ModItems.upgrade_ejector_1.get())) return 10;
		if(stack.is(ModItems.upgrade_ejector_2.get())) return 5;
		if(stack.is(ModItems.upgrade_ejector_3.get())) return 2;
		return 20;
	}

	/** Items per operation with a stack ejection upgrade */
	public static int getStackAmount(ItemStack stack) {
		if(stack.is(ModItems.upgrade_stack_1.get())) return 4;
		if(stack.is(ModItems.upgrade_stack_2.get())) return 16;
		if(stack.is(ModItems.upgrade_stack_3.get())) return 64;
		return 1;
	}

	/** The inventory of the neighbor on that side; furnaces only ever hand out their result (the original's masquerade) */
	public IItemHandler getNeighborInventory(Direction side) {
		BlockPos pos = worldPosition.relative(side);
		BlockEntity te = level.getBlockEntity(pos);
		if(te instanceof AbstractFurnaceBlockEntity furnace) return new RangedWrapper(new InvWrapper(furnace), 2, 3);
		return level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side.getOpposite());
	}

	/** Puts the stack onto the belt next to the crane, straight into the block if it takes items */
	public void sendItem(ItemStack stack, IConveyorBelt belt, Direction outputSide) {
		EntityMovingItem moving = new EntityMovingItem(level);
		BlockPos beltPos = worldPosition.relative(outputSide);
		Vec3 pos = new Vec3(worldPosition.getX() + 0.5 + outputSide.getStepX() * 0.55, worldPosition.getY() + 0.5 + outputSide.getStepY() * 0.55, worldPosition.getZ() + 0.5 + outputSide.getStepZ() * 0.55);
		Vec3 snap = belt.getClosestSnappingPosition(level, beltPos, pos);
		moving.setPos(snap.x, snap.y, snap.z);
		moving.setItemStack(stack);

		if(belt instanceof IEnterableBlock enterable && enterable.canItemEnter(level, beltPos, outputSide.getOpposite(), moving)) {
			enterable.onItemEnter(level, beltPos, outputSide.getOpposite(), moving);
			return;
		}

		level.addFreshEntity(moving);
	}

	/** Whether the stack matches any of the filter slots from 0 to count */
	public boolean matchesFilter(ModulePatternMatcher matcher, int count, ItemStack stack) {
		for(int i = 0; i < count; i++) {
			ItemStack filter = slots.get(i);
			if(!filter.isEmpty() && matcher.isValidForFilter(filter, i, stack)) return true;
		}
		return false;
	}
}
