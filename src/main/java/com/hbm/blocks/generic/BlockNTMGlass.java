package com.hbm.blocks.generic;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * NTM glass (reinforced, boron, lead...). Whether it drops without silk touch is in the loot table,
 * the render layer (cutout/translucent) in the block model.
 *
 * TODO connected textures (the original's BlockNTMGlassCT)
 */
public class BlockNTMGlass extends TransparentBlock {

	public final boolean doesDrop;

	public BlockNTMGlass(Properties properties, boolean doesDrop) {
		super(properties);
		this.doesDrop = doesDrop;
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
		return true;
	}

	public static boolean never(BlockState state, BlockGetter level, BlockPos pos, EntityType<?> type) {
		return false;
	}

	public static boolean never(BlockState state, BlockGetter level, BlockPos pos) {
		return false;
	}
}
