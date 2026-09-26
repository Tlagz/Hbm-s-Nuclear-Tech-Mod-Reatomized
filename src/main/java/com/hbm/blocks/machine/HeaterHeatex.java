package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityHeaterHeatex;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Heat exchanging heater, 3x3x1 with fluid ports at the corners; sneak-click with an identifier to set the hot fluid */
public class HeaterHeatex extends BlockDummyable implements ILookOverlay, ITooltipProvider {

	public HeaterHeatex(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityHeaterHeatex(pos, state);
		if(hasExtra(meta)) return new TileEntityProxyCombo(pos, state).fluid();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

		if(player.isShiftKeyDown() && stack.getItem() instanceof IItemFluidIdentifier id) {
			BlockPos core = this.findCore(world, pos);
			if(core == null || !(world.getBlockEntity(core) instanceof TileEntityHeaterHeatex heater)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

			if(!world.isClientSide) {
				FluidType type = id.getType(world, core, stack);
				heater.tanks[0].setTankType(type);
				heater.setChanged();
				player.sendSystemMessage(Component.literal("Changed type to ").withStyle(ChatFormatting.YELLOW).append(Component.translatable(type.getConditionalName())).append(Component.literal("!")));
			}
			return ItemInteractionResult.sidedSuccess(world.isClientSide);
		}

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if(player.isShiftKeyDown()) return InteractionResult.PASS;
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityHeaterHeatex heater)) return;

		List<String> text = new ArrayList<>();
		text.add(String.format(Locale.US, "%,d", heater.heatEnergy) + " TU");
		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);

		this.makeExtra(world, core.offset(1, 0, 1));
		this.makeExtra(world, core.offset(1, 0, -1));
		this.makeExtra(world, core.offset(-1, 0, 1));
		this.makeExtra(world, core.offset(-1, 0, -1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		this.addStandardInfo(list);
	}
}
