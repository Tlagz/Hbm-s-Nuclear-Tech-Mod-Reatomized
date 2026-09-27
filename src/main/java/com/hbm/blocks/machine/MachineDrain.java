package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.TileEntityMachineDrain;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Drainage pipe, 3 blocks long; right click with a fluid identifier to set the fluid */
public class MachineDrain extends BlockDummyable implements ILookOverlay {

	public MachineDrain(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineDrain(pos, state);
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 2, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if(player.isShiftKeyDown() || !(held.getItem() instanceof IItemFluidIdentifier id)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityMachineDrain drain)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		if(!world.isClientSide) {
			FluidType type = id.getType(world, core, held);
			drain.tank.setTankType(type);
			drain.setChanged();
			player.sendSystemMessage(Component.literal("Changed type to ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal(type.getLocalizedName())).append(Component.literal("!")));
		}
		return ItemInteractionResult.sidedSuccess(world.isClientSide);
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityMachineDrain drain)) return;

		List<String> text = new ArrayList<>();
		text.add(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET + drain.tank.getTankType().getLocalizedName() + ": " + drain.tank.getFill() + "/" + drain.tank.getMaxFill() + "mB");
		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
	}
}
