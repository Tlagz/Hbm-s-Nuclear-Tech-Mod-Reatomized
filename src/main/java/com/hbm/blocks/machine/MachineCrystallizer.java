package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineCrystallizer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Ore acidizer, 3x3x6: the four corner blocks take items, power and fluids */
public class MachineCrystallizer extends BlockDummyable {

	public MachineCrystallizer(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineCrystallizer(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).inventory().power().fluid();
		return null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if(player.isShiftKeyDown()) return InteractionResult.PASS;
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override
	public int[] getDimensions() {
		return new int[] { 5, 0, 1, 1, 1, 1 };
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);
		this.makeExtra(world, core.offset(1, 0, 1));
		this.makeExtra(world, core.offset(-1, 0, 1));
		this.makeExtra(world, core.offset(1, 0, -1));
		this.makeExtra(world, core.offset(-1, 0, -1));
	}
}
