package com.hbm.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Blocks that react to tools like the screwdriver or the hand drill (ItemTooling) */
public interface IToolable {

	/** @return true if the tool did something (it then loses durability) */
	public boolean onScrew(Level world, Player player, BlockPos pos, Direction side, float fX, float fY, float fZ, ToolType tool);

	public static enum ToolType {
		SCREWDRIVER,
		HAND_DRILL,
		DEFUSER,
		WRENCH,
		TORCH,
		BOLT;
	}
}
