package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.IToolable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityHeaterElectric;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.hbm.blocks.ITooltipProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Electric heater, 3x4 and flat; power goes into the block it was placed from, the screwdriver changes the setting */
public class HeaterElectric extends BlockDummyable implements ILookOverlay, IToolable, ITooltipProvider {

	public HeaterElectric(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityHeaterElectric(pos, state);
		if(hasExtra(meta)) return new TileEntityProxyCombo(pos, state).power();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 1, 2, 1, 1};
	}

	@Override
	public int getOffset() {
		return 2;
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		this.makeExtra(world, pos);
	}

	private TileEntityHeaterElectric getHeater(Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		return core != null && world.getBlockEntity(core) instanceof TileEntityHeaterElectric heater ? heater : null;
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		TileEntityHeaterElectric heater = getHeater(world, pos);
		if(heater == null) return;

		List<String> text = new ArrayList<>();
		text.add(String.format(Locale.US, "%,d", heater.heatEnergy) + " TU");
		text.add(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET + heater.getConsumption() + " HE/t");
		text.add(ChatFormatting.RED + "<- " + ChatFormatting.RESET + heater.getHeatGen() + " TU/t");
		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
	}

	@Override
	public boolean onScrew(Level world, Player player, BlockPos pos, Direction side, float fX, float fY, float fZ, ToolType tool) {
		if(tool != ToolType.SCREWDRIVER) return false;
		if(world.isClientSide) return true;

		TileEntityHeaterElectric heater = getHeater(world, pos);
		if(heater == null) return false;

		heater.toggleSetting();
		heater.setChanged();
		return true;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		this.addStandardInfo(list);
	}
}
