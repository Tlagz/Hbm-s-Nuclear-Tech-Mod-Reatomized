package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.oil.TileEntityMachineCatalyticCracker;
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

/** Catalytic cracking tower, 7x6 base and a tower 15 tall; click with an identifier to set the input */
public class MachineCatalyticCracker extends BlockDummyable implements ILookOverlay {

	private static final int[][] EXTRA_DIMS = new int[][] {
		new int[] {8, -1, 3, -1, 2, 0},
		new int[] {13, 0, 0, 3, 2, 1},
		new int[] {14, -13, -1, 2, 1, 0},
		new int[] {3, -1, 2, 3, -1, 3}
	};

	public MachineCatalyticCracker(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineCatalyticCracker(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).fluid();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 3, 3, 2, 3};
	}

	@Override
	public int getOffset() {
		return 3;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if(player.isShiftKeyDown() || !(stack.getItem() instanceof IItemFluidIdentifier id)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		if(!world.isClientSide) {
			BlockPos core = this.findCore(world, pos);
			if(core == null || !(world.getBlockEntity(core) instanceof TileEntityMachineCatalyticCracker cracker)) return ItemInteractionResult.FAIL;

			FluidType type = id.getType(world, core, stack);
			cracker.tanks[0].setTankType(type);
			cracker.setChanged();
			player.sendSystemMessage(Component.literal("Changed type to ").withStyle(ChatFormatting.YELLOW)
					.append(Component.translatable(type.getConditionalName())).append(Component.literal("!")));
		}
		return ItemInteractionResult.sidedSuccess(world.isClientSide);
	}

	@Override
	protected boolean checkRequirement(Level world, BlockPos core, BlockPos placed, Direction dir) {
		if(!super.checkRequirement(world, core, placed, dir)) return false;
		for(int[] dim : EXTRA_DIMS) if(!MultiblockHandlerXR.checkSpace(world, core, dim, placed, dir)) return false;
		return true;
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);
		for(int[] dim : EXTRA_DIMS) MultiblockHandlerXR.fillSpace(world, core, dim, this, dir);

		Direction rot = dir.getClockWise();
		this.makeExtra(world, core.relative(dir, 3).relative(rot, 1));
		this.makeExtra(world, core.relative(dir, 3).relative(rot, -2));
		this.makeExtra(world, core.relative(dir, -3).relative(rot, 1));
		this.makeExtra(world, core.relative(dir, -3).relative(rot, -2));
		this.makeExtra(world, core.relative(dir, 2).relative(rot, 2));
		this.makeExtra(world, core.relative(dir, 2).relative(rot, -3));
		this.makeExtra(world, core.relative(dir, -2).relative(rot, 2));
		this.makeExtra(world, core.relative(dir, -2).relative(rot, -3));
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityMachineCatalyticCracker cracker)) return;

		List<String> text = new ArrayList<>();
		for(int i = 0; i < cracker.tanks.length; i++)
			text.add((i < 2 ? (ChatFormatting.GREEN + "-> ") : (ChatFormatting.RED + "<- ")) + ChatFormatting.RESET + cracker.tanks[i].getTankType().getLocalizedName() + ": " + cracker.tanks[i].getFill() + "/" + cracker.tanks[i].getMaxFill() + "mB");

		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
	}
}
