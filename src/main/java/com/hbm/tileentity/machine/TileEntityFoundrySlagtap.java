package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockDynamicSlag;
import com.hbm.blocks.machine.FoundryOutlet;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.tileentity.ModTileEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Slag tap: an outlet that dumps whatever comes through it on the ground below (up to 15 blocks down) as dynamic
 * slag blocks instead of pouring into a casting block.
 *
 * TODO the pouring stream particle ("foundry" effect)
 */
public class TileEntityFoundrySlagtap extends TileEntityFoundryOutlet {

	public TileEntityFoundrySlagtap(BlockPos pos, BlockState state) {
		super(ModTileEntities.FOUNDRY_SLAGTAP.get(), pos, state);
	}

	private Direction getFacing() {
		return getBlockState().getValue(FoundryOutlet.FACING);
	}

	/** The ground below: stops on liquids, passes through blocks without collision like grass */
	private BlockHitResult findGround(Level world, BlockPos pos) {
		Vec3 start = new Vec3(pos.getX() + 0.5, pos.getY() - 0.125, pos.getZ() + 0.5);
		Vec3 end = new Vec3(pos.getX() + 0.5, pos.getY() + 0.125 - 15, pos.getZ() + 0.5);
		BlockHitResult mop = world.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, CollisionContext.empty()));
		return mop.getType() == HitResult.Type.BLOCK ? mop : null;
	}

	@Override
	public boolean canAcceptPartialFlow(Level world, BlockPos pos, Direction side, MaterialStack stack) {
		if(filter != null && (filter != stack.material ^ invertFilter)) return false;
		if(isClosed()) return false;
		if(side != getFacing().getOpposite()) return false;
		return findGround(world, pos) != null;
	}

	@Override
	public MaterialStack flow(Level world, BlockPos pos, Direction side, MaterialStack stack) {

		if(stack == null || stack.material == null || stack.amount <= 0) {
			return null;
		}

		BlockHitResult mop = findGround(world, pos);
		if(mop == null) return null;

		BlockPos hitPos = mop.getBlockPos();
		BlockState hit = world.getBlockState(hitPos);
		BlockPos abovePos = hitPos.above();

		if(hit.is(ModBlocks.slag.get())) {
			if(world.getBlockEntity(hitPos) instanceof TileEntitySlag tile && tile.mat == stack.material) {
				int transfer = Math.min(TileEntitySlag.maxAmount - tile.amount, stack.amount);
				tile.amount += transfer;
				stack.amount -= transfer;
				tile.sync();
				world.scheduleTick(hitPos, ModBlocks.slag.get(), 1);
			}
		} else if(hit.canBeReplaced()) {
			int transfer = Math.min(TileEntitySlag.maxAmount, stack.amount);
			BlockDynamicSlag.place(world, hitPos, stack, transfer);
			stack.amount -= transfer;
		}

		if(stack.amount > 0 && world.getBlockState(abovePos).canBeReplaced()) {
			int transfer = Math.min(TileEntitySlag.maxAmount, stack.amount);
			BlockDynamicSlag.place(world, abovePos, stack, transfer);
			stack.amount -= transfer;
		}

		if(stack.amount <= 0) {
			stack = null;
		}

		return stack;
	}
}
