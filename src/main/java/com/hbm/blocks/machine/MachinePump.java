package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachinePumpBase;
import com.hbm.tileentity.machine.TileEntityMachinePumpElectric;
import com.hbm.tileentity.machine.TileEntityMachinePumpSteam;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Ground water pump, 3x3x4, steam or electric; ports in the middle of the four sides */
public class MachinePump extends BlockDummyable implements ITooltipProvider, ILookOverlay {

	/** 0 steam, 1 electric */
	public final int type;

	public MachinePump(Properties properties, int type) {
		super(properties);
		this.type = type;
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) {
			return type == 0 ? new TileEntityMachinePumpSteam(pos, state) : new TileEntityMachinePumpElectric(pos, state);
		}
		if(meta >= extra) {
			return type == 0 ? new TileEntityProxyCombo(pos, state).fluid() : new TileEntityProxyCombo(pos, state).fluid().power();
		}
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {3, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);
		this.makeExtra(world, core.east());
		this.makeExtra(world, core.west());
		this.makeExtra(world, core.south());
		this.makeExtra(world, core.north());
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		this.addStandardInfo(list);
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityMachinePumpBase te)) return;

		List<String> text = new ArrayList<>();

		if(te instanceof TileEntityMachinePumpSteam pump) {
			text.add(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET + pump.steam.getTankType().getLocalizedName() + ": " + String.format(Locale.US, "%,d", pump.steam.getFill()) + " / " + String.format(Locale.US, "%,d", pump.steam.getMaxFill()) + "mB");
			text.add(ChatFormatting.RED + "<- " + ChatFormatting.RESET + pump.lps.getTankType().getLocalizedName() + ": " + String.format(Locale.US, "%,d", pump.lps.getFill()) + " / " + String.format(Locale.US, "%,d", pump.lps.getMaxFill()) + "mB");
			text.add(ChatFormatting.RED + "<- " + ChatFormatting.RESET + pump.water.getTankType().getLocalizedName() + ": " + String.format(Locale.US, "%,d", pump.water.getFill()) + " / " + String.format(Locale.US, "%,d", pump.water.getMaxFill()) + "mB");
		}

		if(te instanceof TileEntityMachinePumpElectric pump) {
			text.add(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET + String.format(Locale.US, "%,d", pump.power) + " / " + String.format(Locale.US, "%,d", TileEntityMachinePumpElectric.maxPower) + "HE");
			text.add(ChatFormatting.RED + "<- " + ChatFormatting.RESET + pump.water.getTankType().getLocalizedName() + ": " + String.format(Locale.US, "%,d", pump.water.getFill()) + " / " + String.format(Locale.US, "%,d", pump.water.getMaxFill()) + "mB");
		}

		if(core.getY() > TileEntityMachinePumpBase.groundHeight) {
			text.add("&[" + (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00) + "&]! ! ! ALTITUDE ! ! !");
		}

		if(!te.onGround) {
			text.add("&[" + (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00) + "&]! ! ! NO VALID GROUND ! ! !");
		}

		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
	}
}
