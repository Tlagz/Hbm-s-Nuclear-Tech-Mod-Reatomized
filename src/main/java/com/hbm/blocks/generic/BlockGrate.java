package com.hbm.blocks.generic;

import java.util.List;

import com.hbm.util.i18n.I18nUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Steel grates, 2 pixel thick plates at one of eight heights in the block (0-7, from where the side was clicked).
 * Placed while sneaking on a floor or ceiling, they sit in the gap of the block below (9) or above (8) instead and
 * break when that block no longer leaves room. Items and experience fall through wide grates.
 */
public class BlockGrate extends Block {

	/** The original's metadata: 0-7 height in eighths, 8 on top of the block, 9 in the block below */
	public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 9);

	private static final VoxelShape[] SHAPES = new VoxelShape[10];
	static {
		for(int i = 0; i < 10; i++) {
			double y = getY(i) * 16;
			SHAPES[i] = Block.box(0, y, 0, 16, y + 2, 16);
		}
	}

	public final boolean wide;

	public BlockGrate(Properties properties, boolean wide) {
		super(properties.noOcclusion());
		this.wide = wide;
		this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
	}

	public static double getY(int level) {
		if(level == 9) return -0.125D;
		return level * 0.125D;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LEVEL);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction side = context.getClickedFace();
		int level;
		if(side == Direction.DOWN) level = 7;
		else if(side == Direction.UP) level = 0;
		else level = Math.clamp((int) Math.floor((context.getClickLocation().y - context.getClickedPos().getY()) * 8D), 0, 7);
		return this.defaultBlockState().setValue(LEVEL, level);
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if(placer == null || !placer.isShiftKeyDown()) return;
		int level = state.getValue(LEVEL);
		// sink into the block below or rise into the one above if its shape leaves room
		if(level == 0 && hasRoomBelow(world, pos)) world.setBlock(pos, state.setValue(LEVEL, 9), 3);
		else if(level == 7 && hasRoomAbove(world, pos)) world.setBlock(pos, state.setValue(LEVEL, 8), 3);
	}

	private static boolean hasRoomBelow(BlockGetter world, BlockPos pos) {
		VoxelShape below = world.getBlockState(pos.below()).getCollisionShape(world, pos.below());
		return below.isEmpty() || below.max(Direction.Axis.Y) < 0.95D;
	}

	private static boolean hasRoomAbove(BlockGetter world, BlockPos pos) {
		VoxelShape above = world.getBlockState(pos.above()).getCollisionShape(world, pos.above());
		return above.isEmpty() || above.min(Direction.Axis.Y) > 0.05D;
	}

	@Override
	protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
		if(world.isClientSide) return;
		int level = state.getValue(LEVEL);
		if((level == 9 && !hasRoomBelow(world, pos)) || (level == 8 && !hasRoomAbove(world, pos))) {
			world.destroyBlock(pos, true);
		}
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return SHAPES[state.getValue(LEVEL)];
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		if(wide && context instanceof EntityCollisionContext entityContext && (entityContext.getEntity() instanceof ItemEntity || entityContext.getEntity() instanceof ExperienceOrb)) {
			return Shapes.empty();
		}
		return SHAPES[state.getValue(LEVEL)];
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		String key = this == com.hbm.blocks.ModBlocks.steel_grate_wide.get() ? "tile.steel_grate_wide.desc" : "tile.steel_grate.desc";
		String desc = I18nUtil.resolveKey(key);
		if(!desc.equals(key)) list.add(Component.literal(desc));
	}
}
