package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingType;
import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityHeatBoiler;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Boiler, 3x3 base, 4 tall, standing on a heater. Set the input fluid with a fluid identifier, looking at it
 * shows the heat and tanks. A burst boiler drops its scrap instead of the machine.
 *
 * TODO the burst boiler item (damage 1), standard tooltip
 */
public class MachineHeatBoiler extends BlockDummyable implements ILookOverlay {

	public MachineHeatBoiler(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityHeatBoiler(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).fluid();
		return null;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

		if(player.isShiftKeyDown() || !(stack.getItem() instanceof IItemFluidIdentifier id)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		if(!world.isClientSide) {
			BlockPos core = this.findCore(world, pos);
			if(core == null || !(world.getBlockEntity(core) instanceof TileEntityHeatBoiler boiler)) return ItemInteractionResult.FAIL;

			FluidType type = id.getType(world, core, stack);

			if(type.hasTrait(FT_Heatable.class) && type.getTrait(FT_Heatable.class).getEfficiency(HeatingType.BOILER) > 0) {
				boiler.tanks[0].setTankType(type);
				boiler.setChanged();
				player.sendSystemMessage(Component.literal("Changed type to ").withStyle(ChatFormatting.YELLOW)
						.append(Component.translatable(type.getConditionalName())).append(Component.literal("!")));
			}
		}
		return ItemInteractionResult.sidedSuccess(world.isClientSide);
	}

	@Override
	protected List<ItemStack> getHarvestDrops(Level world, BlockPos core) {
		if(core != null && world.getBlockEntity(core) instanceof TileEntityHeatBoiler boiler && boiler.hasExploded) {
			List<ItemStack> scrap = new ArrayList<>();
			scrap.add(new ItemStack(ModItems.ingot_steel.get(), 4));
			scrap.add(new ItemStack(ModItems.plate_copper.get(), 8));
			return scrap;
		}
		return super.getHarvestDrops(world, core);
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
		Direction rot = dir.getClockWise(); // getRotation(UP)

		this.makeExtra(world, core.relative(rot));
		this.makeExtra(world, core.relative(rot.getOpposite()));
		this.makeExtra(world, core.above(3));
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {

		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityHeatBoiler boiler)) return;
		if(boiler.hasExploded) return;

		List<String> text = new ArrayList<>();
		text.add(String.format(Locale.US, "%,d", boiler.heat) + "TU");
		text.add(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET + boiler.tanks[0].getTankType().getLocalizedName() + ": " + String.format(Locale.US, "%,d", boiler.tanks[0].getFill()) + " / " + String.format(Locale.US, "%,d", boiler.tanks[0].getMaxFill()) + "mB");
		text.add(ChatFormatting.RED + "<- " + ChatFormatting.RESET + boiler.tanks[1].getTankType().getLocalizedName() + ": " + String.format(Locale.US, "%,d", boiler.tanks[1].getFill()) + " / " + String.format(Locale.US, "%,d", boiler.tanks[1].getMaxFill()) + "mB");

		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
	}
}
