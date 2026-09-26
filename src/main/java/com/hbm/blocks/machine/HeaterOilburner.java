package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.IToolable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityHeaterOilburner;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
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

/** Fluid burner, 3x3x2: fluid ports on the four sides, the heat goes to the machine on top; screwdriver sets the burn rate */
public class HeaterOilburner extends BlockDummyable implements ILookOverlay, ITooltipProvider, IToolable {

	public HeaterOilburner(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {

		if(meta >= 12)
			return new TileEntityHeaterOilburner(pos, state);

		// the side extras take fluids, the one on top in the middle passes the heat on
		if(hasExtra(meta) && meta - extra > 1)
			return new TileEntityProxyCombo(pos, state).fluid();

		if(hasExtra(meta))
			return new TileEntityProxyCombo(pos, state).heatSource();

		return null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override
	public int[] getDimensions() {
		return new int[] {1, 0, 1, 1, 1, 1};
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
		this.makeExtra(world, core.above());
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		this.addStandardInfo(list);
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityHeaterOilburner heater)) return;

		List<String> text = new ArrayList<>();
		text.add(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET + heater.setting + " mB/t");
		FluidType type = heater.tank.getTankType();
		if(type.hasTrait(FT_Flammable.class)) {
			int heat = (int) (type.getTrait(FT_Flammable.class).getHeatEnergy() * heater.setting / 1000);
			text.add(ChatFormatting.RED + "<- " + ChatFormatting.RESET + String.format(Locale.US, "%,d", heat) + " TU/t");
		}

		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
	}

	@Override
	public boolean onScrew(Level world, Player player, BlockPos pos, Direction side, float fX, float fY, float fZ, ToolType tool) {

		if(tool != ToolType.SCREWDRIVER) return false;
		if(world.isClientSide) return true;

		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityHeaterOilburner tile)) return false;

		tile.toggleSetting();
		tile.setChanged();
		return true;
	}
}
