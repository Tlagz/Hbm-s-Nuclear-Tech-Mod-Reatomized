package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.ModDataComponents;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.machine.storage.TileEntityBarrel;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Fluid barrels. The corroded barrel has no tile entity (and thus no GUI), like the original.
 * Breaking a filled barrel keeps the fluid on the item (PERSISTENT component, the original's IPersistentNBT).
 */
public class BlockFluidBarrel extends Block implements EntityBlock {

	public final int capacity;

	private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

	public BlockFluidBarrel(Properties properties, int capacity) {
		super(properties.noOcclusion());
		this.capacity = capacity;
	}

	private boolean hasTile() {
		return this != ModBlocks.barrel_corroded.get();
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return hasTile() ? new TileEntityBarrel(pos, state) : null;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return world.isClientSide || type != ModTileEntities.BARREL.get() ? null : TileEntityLoadedBase.ticker();
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if(!hasTile()) return InteractionResult.PASS;
		if(player.isShiftKeyDown()) return InteractionResult.PASS;

		if(!world.isClientSide && player instanceof ServerPlayer serverPlayer && world.getBlockEntity(pos) instanceof TileEntityBarrel barrel) {
			serverPlayer.openMenu(barrel, buf -> buf.writeBlockPos(pos));
		}
		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	/** Sneak click with a fluid identifier sets the type directly */
	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if(!hasTile()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		if(player.isShiftKeyDown() && stack.getItem() instanceof IItemFluidIdentifier id) {
			if(!world.isClientSide && world.getBlockEntity(pos) instanceof TileEntityBarrel barrel) {
				FluidType type = id.getType(world, pos, stack);
				barrel.tank.setTankType(type);
				barrel.setChanged();
				player.sendSystemMessage(Component.literal("Changed type to ").withStyle(ChatFormatting.YELLOW)
						.append(Component.translatable(type.getConditionalName())).append(Component.literal("!")));
			}
			return ItemInteractionResult.sidedSuccess(world.isClientSide);
		}

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean movedByPiston) {
		if(!state.is(newState.getBlock()) && world.getBlockEntity(pos) instanceof TileEntityBarrel barrel) {
			Containers.dropContents(world, pos, barrel);
			world.updateNeighbourForOutputSignal(pos, this);
		}
		super.onRemove(state, world, pos, newState, movedByPiston);
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
		return world.getBlockEntity(pos) instanceof TileEntityBarrel barrel ? barrel.getComparatorPower() : 0;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {

		CustomData persistent = stack.get(ModDataComponents.PERSISTENT.get());
		if(persistent != null) {
			FluidTank tank = new FluidTank(Fluids.NONE, 0);
			tank.readFromNBT(persistent.copyTag(), "tank");
			list.add(Component.literal(tank.getFill() + "/" + tank.getMaxFill() + "mB ").append(Component.translatable(tank.getTankType().getConditionalName())).withStyle(ChatFormatting.YELLOW));
		}

		if(this == ModBlocks.barrel_plastic.get()) {
			list.add(Component.literal("Capacity: 12,000mB").withStyle(ChatFormatting.AQUA));
			list.add(Component.literal("Cannot store hot fluids").withStyle(ChatFormatting.YELLOW));
			list.add(Component.literal("Cannot store corrosive fluids").withStyle(ChatFormatting.YELLOW));
			list.add(Component.literal("Cannot store antimatter").withStyle(ChatFormatting.YELLOW));
		}

		if(this == ModBlocks.barrel_corroded.get()) {
			list.add(Component.literal("Capacity: 6,000mB").withStyle(ChatFormatting.AQUA));
			list.add(Component.literal("Can store hot fluids").withStyle(ChatFormatting.GREEN));
			list.add(Component.literal("Can store highly corrosive fluids").withStyle(ChatFormatting.GREEN));
			list.add(Component.literal("Cannot store antimatter").withStyle(ChatFormatting.YELLOW));
			list.add(Component.literal("Leaky").withStyle(ChatFormatting.RED));
		}

		if(this == ModBlocks.barrel_steel.get()) {
			list.add(Component.literal("Capacity: 16,000mB").withStyle(ChatFormatting.AQUA));
			list.add(Component.literal("Can store hot fluids").withStyle(ChatFormatting.GREEN));
			list.add(Component.literal("Can store corrosive fluids").withStyle(ChatFormatting.GREEN));
			list.add(Component.literal("Cannot store highly corrosive fluids properly").withStyle(ChatFormatting.YELLOW));
			list.add(Component.literal("Cannot store antimatter").withStyle(ChatFormatting.YELLOW));
		}

		if(this == ModBlocks.barrel_antimatter.get()) {
			list.add(Component.literal("Capacity: 16,000mB").withStyle(ChatFormatting.AQUA));
			list.add(Component.literal("Can store hot fluids").withStyle(ChatFormatting.GREEN));
			list.add(Component.literal("Can store highly corrosive fluids").withStyle(ChatFormatting.GREEN));
			list.add(Component.literal("Can store antimatter").withStyle(ChatFormatting.GREEN));
		}

		if(this == ModBlocks.barrel_tcalloy.get()) {
			list.add(Component.literal("Capacity: 24,000mB").withStyle(ChatFormatting.AQUA));
			list.add(Component.literal("Can store hot fluids").withStyle(ChatFormatting.GREEN));
			list.add(Component.literal("Can store highly corrosive fluids").withStyle(ChatFormatting.GREEN));
			list.add(Component.literal("Cannot store antimatter").withStyle(ChatFormatting.YELLOW));
		}
	}
}
