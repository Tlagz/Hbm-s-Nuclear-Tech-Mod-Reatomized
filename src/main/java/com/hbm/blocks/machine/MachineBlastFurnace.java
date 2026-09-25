package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineBlastFurnace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Blast furnace, a 3x3 base with a 7 tall stack; ports around the base, on the front of the stack and on top */
public class MachineBlastFurnace extends BlockDummyable {

	public MachineBlastFurnace(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineBlastFurnace(pos, state);
		if(meta >= 6) return new TileEntityProxyCombo(pos, state).inventory().fluid();
		return null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override public int[] getDimensions() { return new int[] {6, 0, 1, 1, 1, 1}; }
	@Override public int getOffset() { return 1; }

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);

		BlockPos core = pos.relative(dir, o);

		this.makeExtra(world, core.offset(1, 0, 0));
		this.makeExtra(world, core.offset(-1, 0, 0));
		this.makeExtra(world, core.offset(0, 0, 1));
		this.makeExtra(world, core.offset(0, 0, -1));

		this.makeExtra(world, core.relative(dir).above(3));
		this.makeExtra(world, core.relative(dir).above(5));

		this.makeExtra(world, core.above(6));
	}
}
