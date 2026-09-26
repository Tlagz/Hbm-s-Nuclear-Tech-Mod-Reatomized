package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityTowerLarge;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Large cooling tower, 9x9 and 13 tall, three fluid ports on each side */
public class MachineTowerLarge extends BlockDummyable implements ILookOverlay {

	public MachineTowerLarge(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityTowerLarge(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).fluid();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {12, 0, 4, 4, 4, 4};
	}

	@Override
	public int getOffset() {
		return 4;
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);
		for(Direction dr2 : Direction.Plane.HORIZONTAL) {
			Direction rot = dr2.getClockWise();
			BlockPos side = core.relative(dr2, 4);
			this.makeExtra(world, side);
			this.makeExtra(world, side.relative(rot, 3));
			this.makeExtra(world, side.relative(rot, -3));
		}
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityTowerLarge tower)) return;

		List<String> text = new ArrayList<>();
		for(int i = 0; i < tower.tanks.length; i++)
			text.add((i < 1 ? (ChatFormatting.GREEN + "-> ") : (ChatFormatting.RED + "<- ")) + ChatFormatting.RESET + tower.tanks[i].getTankType().getLocalizedName() + ": " + tower.tanks[i].getFill() + "/" + tower.tanks[i].getMaxFill() + "mB");

		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
	}
}
