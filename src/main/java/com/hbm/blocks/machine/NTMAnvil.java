package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hbm.inventory.container.ContainerAnvil;
import com.mojang.serialization.MapCodec;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * NTM anvils: tiered crafting stations for smithing and construction recipes. They fall like sand.
 * FACING is the direction the player looked when placing it (the original's metadata).
 */
public class NTMAnvil extends FallingBlock {

	public static final int TIER_IRON = 1;
	public static final int TIER_STEEL = 2;
	public static final int TIER_OIL = 3;
	public static final int TIER_NUCLEAR = 4;
	public static final int TIER_RBMK = 5;
	public static final int TIER_FUSION = 6;
	public static final int TIER_PARTICLE = 7;
	public static final int TIER_GERALD = 8;

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE_NS = Block.box(0, 0, 4, 16, 12, 12);
	private static final VoxelShape SHAPE_EW = Block.box(4, 0, 0, 12, 12, 16);

	public final int tier;
	public static final HashMap<Integer, List<NTMAnvil>> tierMap = new HashMap<>();

	public NTMAnvil(Properties properties, int tier) {
		super(properties.noOcclusion());
		this.tier = tier;
		tierMap.computeIfAbsent(tier, k -> new ArrayList<>()).add(this);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected MapCodec<? extends FallingBlock> codec() {
		return simpleCodec(p -> new NTMAnvil(p, tier));
	}

	public static List<ItemStack> getAnvilsFromTier(int tier) {
		List<ItemStack> stacks = new ArrayList<>();
		List<NTMAnvil> anvils = tierMap.get(tier);
		if(anvils != null) for(NTMAnvil anvil : anvils) stacks.add(new ItemStack(anvil));
		return stacks;
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
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return state.getValue(FACING).getAxis() == Direction.Axis.Z ? SHAPE_NS : SHAPE_EW;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if(player.isShiftKeyDown()) return InteractionResult.PASS;

		if(!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
			serverPlayer.openMenu(new SimpleMenuProvider((id, inv, p) -> new ContainerAnvil(id, inv, tier), Component.translatable("container.anvil", tier)),
					buf -> buf.writeInt(tier));
		}
		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		list.add(Component.literal("Tier " + tier + " Anvil").withStyle(ChatFormatting.GOLD));
	}

	@Override
	public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
		return state.getMapColor(level, pos).col;
	}
}
