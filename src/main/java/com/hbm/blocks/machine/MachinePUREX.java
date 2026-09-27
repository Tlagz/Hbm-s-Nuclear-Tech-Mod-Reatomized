package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachinePUREX;

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

/** PUREX, 5x5 and 5 tall, the whole bottom ring are item/fluid/power ports */
public class MachinePUREX extends BlockDummyable implements ITooltipProvider {

	public MachinePUREX(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachinePUREX(pos, state);
		if(meta >= 6) return new TileEntityProxyCombo(pos, state).inventory().power().fluid();
		return null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override public int[] getDimensions() { return new int[] {4, 0, 2, 2, 2, 2}; }
	@Override public int getOffset() { return 2; }

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);

		BlockPos core = pos.relative(dir, o);
		for(int i = -2; i <= 2; i++) for(int j = -2; j <= 2; j++) {
			if(Math.abs(i) == 2 || Math.abs(j) == 2) this.makeExtra(world, core.offset(i, 0, j));
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		this.addStandardInfo(list);
	}
}
