package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityChimneyIndustrial;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Industrial smokestack, 3x3 and 23 tall, exhaust ports on the four sides of the base */
public class MachineChimneyIndustrial extends BlockDummyable implements ITooltipProvider {

	public MachineChimneyIndustrial(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityChimneyIndustrial(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).fluid();
		return null;
	}

	@Override public int[] getDimensions() { return new int[] {22, 0, 1, 1, 1, 1}; }
	@Override public int getOffset() { return 1; }

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);
		for(Direction side : Direction.Plane.HORIZONTAL) this.makeExtra(world, core.relative(side));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		this.addStandardInfo(list);
	}
}
