package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntitySteamEngine;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.hbm.blocks.ITooltipProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Steam engine, 3 wide and 7 long; steam, spent steam and power go through the three ports on one side */
public class MachineSteamEngine extends BlockDummyable implements ILookOverlay, ITooltipProvider {

	public MachineSteamEngine(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntitySteamEngine(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).power().fluid();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {1, 0, 5, 1, 1, 1};
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
		this.makeExtra(world, core.relative(rot));
		this.makeExtra(world, core.relative(rot).relative(dir));
		this.makeExtra(world, core.relative(rot).relative(dir.getOpposite()));
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntitySteamEngine engine)) return;

		List<String> text = new ArrayList<>();
		text.add(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET + engine.tanks[0].getTankType().getLocalizedName() + ": " + String.format(Locale.US, "%,d", engine.tanks[0].getFill()) + " / " + String.format(Locale.US, "%,d", engine.tanks[0].getMaxFill()) + "mB");
		text.add(ChatFormatting.RED + "<- " + ChatFormatting.RESET + engine.tanks[1].getTankType().getLocalizedName() + ": " + String.format(Locale.US, "%,d", engine.tanks[1].getFill()) + " / " + String.format(Locale.US, "%,d", engine.tanks[1].getMaxFill()) + "mB");
		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		this.addStandardInfo(list);
	}
}
