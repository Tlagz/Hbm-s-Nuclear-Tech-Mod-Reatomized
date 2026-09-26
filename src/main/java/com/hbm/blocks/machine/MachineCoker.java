package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.oil.TileEntityMachineCoker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Coker unit: a 3x3 column 23 blocks tall on a 5x5x5 frame with four legs, heat is pulled from below the core */
public class MachineCoker extends BlockDummyable implements ITooltipProvider {

	private static final int[] FRAME = new int[] {5, 0, 2, 2, 2, 2};
	private static final int[] LEG = new int[] {0, 1, 0, 0, 0, 0};

	public MachineCoker(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineCoker(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).inventory().fluid();
		return null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override
	public int[] getDimensions() {
		return new int[] {22, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	protected boolean checkRequirement(Level world, BlockPos core, BlockPos placed, Direction dir) {
		return super.checkRequirement(world, core, placed, dir) &&
				MultiblockHandlerXR.checkSpace(world, core.above(), FRAME, placed, Direction.NORTH) &&
				MultiblockHandlerXR.checkSpace(world, core.offset(2, 1, 2), LEG, placed, Direction.NORTH) &&
				MultiblockHandlerXR.checkSpace(world, core.offset(2, 1, -2), LEG, placed, Direction.NORTH) &&
				MultiblockHandlerXR.checkSpace(world, core.offset(-2, 1, 2), LEG, placed, Direction.NORTH) &&
				MultiblockHandlerXR.checkSpace(world, core.offset(-2, 1, -2), LEG, placed, Direction.NORTH);
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);

		MultiblockHandlerXR.fillSpace(world, core.above(), FRAME, this, Direction.NORTH);
		MultiblockHandlerXR.fillSpace(world, core.offset(2, 1, 2), LEG, this, Direction.NORTH);
		MultiblockHandlerXR.fillSpace(world, core.offset(2, 1, -2), LEG, this, Direction.NORTH);
		MultiblockHandlerXR.fillSpace(world, core.offset(-2, 1, 2), LEG, this, Direction.NORTH);
		MultiblockHandlerXR.fillSpace(world, core.offset(-2, 1, -2), LEG, this, Direction.NORTH);

		this.makeExtra(world, core.offset(1, 0, 1));
		this.makeExtra(world, core.offset(1, 0, -1));
		this.makeExtra(world, core.offset(-1, 0, 1));
		this.makeExtra(world, core.offset(-1, 0, -1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		this.addStandardInfo(list);
	}
}
