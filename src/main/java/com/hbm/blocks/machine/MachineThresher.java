package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.IToolable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.machine.TileEntityMachineAutosaw;
import com.hbm.tileentity.machine.TileEntityMachineThresher;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/** Thresher: the arm reaches out in FACING (the way the player looked when placing), same fuels as the buzz saw */
public class MachineThresher extends BlockMachineTile implements ILookOverlay, ITooltipProvider, IToolable {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public MachineThresher(Properties properties) {
		super(properties.noOcclusion(), ModTileEntities.THRESHER);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	/** Ticks on both sides, the client spins the wheel and smooths the arm */
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return type == ModTileEntities.THRESHER.get() ? TileEntityLoadedBase.ticker() : null;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

		if(!player.isShiftKeyDown() && stack.getItem() instanceof IItemFluidIdentifier id && world.getBlockEntity(pos) instanceof TileEntityMachineThresher thresher) {
			if(world.isClientSide) return ItemInteractionResult.SUCCESS;

			FluidType type = id.getType(world, pos, stack);
			if(TileEntityMachineAutosaw.isAcceptedFuel(type)) {
				thresher.tank.setTankType(type);
				thresher.setChanged();
				player.sendSystemMessage(Component.literal("Changed type to ").withStyle(ChatFormatting.YELLOW).append(Component.translatable(type.getConditionalName())).append(Component.literal("!")));
				return ItemInteractionResult.SUCCESS;
			}
		}

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public boolean onScrew(Level world, Player player, BlockPos pos, Direction side, float fX, float fY, float fZ, ToolType tool) {
		if(tool != ToolType.SCREWDRIVER) return false;
		if(!(world.getBlockEntity(pos) instanceof TileEntityMachineThresher thresher)) return false;
		if(world.isClientSide) return true;

		thresher.isSuspended = !thresher.isSuspended;
		thresher.setChanged();
		return true;
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		if(!(world.getBlockEntity(pos) instanceof TileEntityMachineThresher thresher)) return;

		List<String> text = new ArrayList<>();
		text.add(thresher.tank.getTankType().getLocalizedName() + ": " + thresher.tank.getFill() + "/" + thresher.tank.getMaxFill() + "mB");

		if(thresher.isSuspended) {
			text.add(ChatFormatting.RED + "! " + I18nUtil.resolveKey("tile.machine_thresher.suspended") + " !");
		}

		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		this.addStandardInfo(list);
	}
}
