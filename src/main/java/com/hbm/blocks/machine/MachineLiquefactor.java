package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.oil.TileEntityMachineLiquefactor;
import com.hbm.tileentity.machine.oil.TileEntityMachineSolidifier;

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

/**
 * The original's MachineLiquefactor and MachineSolidifier, 3x3x4 with ports on top, below and on the sides of the
 * second layer.
 */
public class MachineLiquefactor extends BlockDummyable implements ITooltipProvider {

	private final boolean solidifier;

	public MachineLiquefactor(Properties properties, boolean solidifier) {
		super(properties);
		this.solidifier = solidifier;
	}

	public static MachineLiquefactor liquefactor(Properties properties) { return new MachineLiquefactor(properties, false); }
	public static MachineLiquefactor solidifier(Properties properties) { return new MachineLiquefactor(properties, true); }

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return solidifier ? new TileEntityMachineSolidifier(pos, state) : new TileEntityMachineLiquefactor(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).inventory().power().fluid();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {3, 0, 1, 1, 1, 1};
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);

		this.makeExtra(world, core.offset(0, 3, 0));

		this.makeExtra(world, core.offset(1, 1, 0));
		this.makeExtra(world, core.offset(-1, 1, 0));
		this.makeExtra(world, core.offset(0, 1, 1));
		this.makeExtra(world, core.offset(0, 1, -1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		this.addStandardInfo(list);
	}
}
