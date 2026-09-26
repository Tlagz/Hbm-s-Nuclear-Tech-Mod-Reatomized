package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityCondenserPowered;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** High-power condenser, 7x3x3 with a fan on each end, ports on the ends and the long sides */
public class MachineCondenserPowered extends BlockDummyable implements ILookOverlay {

	public MachineCondenserPowered(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityCondenserPowered(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).power().fluid();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 0, 1, 1, 3, 3};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o).above();
		Direction rot = dir.getClockWise();
		this.makeExtra(world, core.relative(rot, 3));
		this.makeExtra(world, core.relative(rot, -3));
		this.makeExtra(world, core.relative(dir).relative(rot));
		this.makeExtra(world, core.relative(dir).relative(rot, -1));
		this.makeExtra(world, core.relative(dir, -1).relative(rot));
		this.makeExtra(world, core.relative(dir, -1).relative(rot, -1));
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityCondenserPowered tower)) return;

		List<String> text = new ArrayList<>();
		text.add(BobMathUtil.getShortNumber(tower.power) + "HE / " + BobMathUtil.getShortNumber(TileEntityCondenserPowered.maxPower) + "HE");

		for(int i = 0; i < tower.tanks.length; i++)
			text.add((i < 1 ? (ChatFormatting.GREEN + "-> ") : (ChatFormatting.RED + "<- ")) + ChatFormatting.RESET + tower.tanks[i].getTankType().getLocalizedName() + ": " + String.format(Locale.US, "%,d", tower.tanks[i].getFill()) + "/" + String.format(Locale.US, "%,d", tower.tanks[i].getMaxFill()) + "mB");

		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
	}
}
