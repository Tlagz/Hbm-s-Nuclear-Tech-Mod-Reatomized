package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityDeuteriumTower;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Deuterium extraction tower, 2x2 and 10 tall with the core in one corner, the bottom blocks are power/fluid ports */
public class DeuteriumTower extends BlockDummyable implements ILookOverlay {

	public DeuteriumTower(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityDeuteriumTower(pos, state);
		if(meta >= 8) return new TileEntityProxyCombo(pos, state).power().fluid();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] { 9, 0, 1, 0, 0, 1 };
	}

	@Override
	public int getOffset() {
		return 0;
	}

	/** The three other blocks of the bottom layer */
	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);

		BlockPos core = pos.relative(dir, o);
		Direction ccw = dir.getCounterClockWise();
		this.makeExtra(world, core.relative(dir, -1));
		this.makeExtra(world, core.relative(ccw));
		this.makeExtra(world, core.relative(dir, -1).relative(ccw));
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityDeuteriumTower tower)) return;
		MachineDeuteriumExtractor.printExtractor(graphics, tower, I18nUtil.resolveKey(getDescriptionId()));
	}
}
