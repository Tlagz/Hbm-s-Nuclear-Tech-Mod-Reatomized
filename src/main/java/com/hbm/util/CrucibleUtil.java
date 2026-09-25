package com.hbm.util;

import java.util.List;

import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;

import api.hbm.block.ICrucibleAcceptor;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/** Pouring molten material straight down onto crucible acceptors (molds, channels, basins) */
public class CrucibleUtil {

	/** The impact position of a pour, the original filled a Vec3 passed by the caller */
	public static class Impact {
		public double x, y, z;
		public boolean hit;

		void set(Vec3 vec) {
			x = vec.x; y = vec.y; z = vec.z; hit = true;
		}
	}

	/**
	 * Standard pouring, casting a hitscan straight down at the given coordinates with the given range. Returns the leftover material, just like ICrucibleAcceptor's pour.
	 * The method directly modifies the original stack, so be careful and make a copy beforehand if you don't want that.
	 */
	public static MaterialStack pourSingleStack(Level world, double x, double y, double z, double range, boolean safe, MaterialStack stack, int quanta, Impact impact) {

		BlockHitResult[] hitHolder = new BlockHitResult[1];
		ICrucibleAcceptor acc = getPouringTarget(world, new Vec3(x, y, z), new Vec3(x, y - range, z), hitHolder);
		BlockHitResult hit = hitHolder[0];

		if(acc == null) {
			spill(hit, safe, stack, quanta, impact);
			return stack;
		}

		MaterialStack ret = tryPourStack(world, acc, hit, stack, impact);

		if(ret != null) {
			return ret;
		}

		spill(hit, safe, stack, quanta, impact);
		return stack;
	}

	/**
	 * Standard pouring, casting a hitscan straight down at the given coordinates with the given range. Returns the materialStack that has been removed.
	 * The method doesn't make copies of the MaterialStacks in the list, so the materials being subtracted or outright removed will apply to the original list.
	 */
	public static MaterialStack pourFullStack(Level world, double x, double y, double z, double range, boolean safe, List<MaterialStack> stacks, int quanta, Impact impact) {

		if(stacks.isEmpty()) return null;

		BlockHitResult[] hitHolder = new BlockHitResult[1];
		ICrucibleAcceptor acc = getPouringTarget(world, new Vec3(x, y, z), new Vec3(x, y - range, z), hitHolder);
		BlockHitResult hit = hitHolder[0];

		if(acc == null) {
			return spill(hit, safe, stacks, quanta, impact);
		}

		for(MaterialStack stack : stacks) {
			if(stack.material == null) continue;

			int amountToPour = Math.min(stack.amount, quanta);
			MaterialStack toPour = new MaterialStack(stack.material, amountToPour);
			MaterialStack left = tryPourStack(world, acc, hit, toPour, impact);

			if(left != null) {
				stack.amount -= (amountToPour - left.amount);
				return new MaterialStack(stack.material, stack.amount - left.amount);
			}
		}

		return spill(hit, safe, stacks, quanta, impact);
	}

	/**
	 * Tries to pour the stack onto the supplied crucible acceptor instance, the impact position is written into the impact holder.
	 * Returns whatever is left of the stack when successful or null when unsuccessful (potential spillage).
	 */
	public static MaterialStack tryPourStack(Level world, ICrucibleAcceptor acc, BlockHitResult hit, MaterialStack stack, Impact impact) {
		Vec3 pos = hit.getLocation();

		if(stack.material.smeltable != SmeltingBehavior.SMELTABLE) {
			return null;
		}

		if(acc.canAcceptPartialPour(world, hit.getBlockPos(), pos.x, pos.y, pos.z, hit.getDirection(), stack)) {
			MaterialStack left = acc.pour(world, hit.getBlockPos(), pos.x, pos.y, pos.z, hit.getDirection(), stack);
			if(left == null) {
				left = new MaterialStack(stack.material, 0);
			}

			if(impact != null) impact.set(pos);
			return left;
		}

		return null;
	}

	/** Hitscan from start (the top) to end (the bottom) for the target of the pour, the hit is written into the holder's first cell */
	public static ICrucibleAcceptor getPouringTarget(Level world, Vec3 start, Vec3 end, BlockHitResult[] hitHolder) {

		BlockHitResult hit = world.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, CollisionContext.empty()));

		if(hitHolder != null) {
			hitHolder[0] = hit;
		}

		if(hit == null || hit.getType() != HitResult.Type.BLOCK) {
			return null;
		}

		Block b = world.getBlockState(hit.getBlockPos()).getBlock();
		return b instanceof ICrucibleAcceptor acceptor ? acceptor : null;
	}

	/** Regular spillage routine but accepts a stack list instead of a stack. simply uses the first available stack from the list. Assumes list is not empty. */
	public static MaterialStack spill(BlockHitResult hit, boolean safe, List<MaterialStack> stacks, int quanta, Impact impact) {
		//simply use the first available material
		MaterialStack top = stacks.get(0);
		MaterialStack ret = spill(hit, safe, top, quanta, impact);
		//remove all stacks with no content
		stacks.removeIf(o -> o.amount <= 0);

		return ret;
	}

	/** The routine used for then there is no valid crucible acceptor found. Will NOP with safe mode on. Returns the MaterialStack that was lost. */
	public static MaterialStack spill(BlockHitResult hit, boolean safe, MaterialStack stack, int quanta, Impact impact) {

		//do nothing if safe mode is on
		if(safe) {
			return null;
		}

		MaterialStack toWaste = new MaterialStack(stack.material, Math.min(stack.amount, quanta));
		stack.amount -= toWaste.amount;

		if(impact != null && hit != null && hit.getType() != HitResult.Type.MISS) {
			impact.set(hit.getLocation());
		}

		return toWaste;
	}
}
