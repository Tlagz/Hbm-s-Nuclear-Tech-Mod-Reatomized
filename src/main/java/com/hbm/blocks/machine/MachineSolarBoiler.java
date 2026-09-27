package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntitySolarBoiler;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Solar tower boiler, 3x3 and 3 high, the top center is a fluid port like the core below */
public class MachineSolarBoiler extends BlockDummyable implements ILookOverlay {

	public MachineSolarBoiler(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntitySolarBoiler(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).fluid();
		return null;
	}

	@Override public int[] getDimensions() { return new int[] {2, 0, 1, 1, 1, 1}; }
	@Override public int getOffset() { return 1; }

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		this.makeExtra(world, pos.relative(dir, o).above(2));
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntitySolarBoiler boiler)) return;

		List<String> text = new ArrayList<>();
		FluidTank[] tanks = boiler.getAllTanks();

		for(int i = 0; i < tanks.length; i++)
			text.add((i < 1 ? (ChatFormatting.GREEN + "-> ") : (ChatFormatting.RED + "<- ")) + ChatFormatting.RESET + tanks[i].getTankType().getLocalizedName() + ": " + tanks[i].getFill() + "/" + tanks[i].getMaxFill() + "mB");

		if(boiler.display < 1) {
			text.add("&[" + (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00) + "&]Too cold!");
		}

		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
	}
}
