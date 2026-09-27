package com.hbm.items.tool;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.network.BlockConveyorBase;
import com.hbm.blocks.network.BlockConveyorBendable;
import com.hbm.items.ItemEnumMulti;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Conveyor belts come as this item: click two points to build a belt between them (turns, lifts and chutes
 * included), sneak-click to place a single one. The start point is kept on the stack as custom data.
 *
 * TODO the original's route preview (RenderOverhead action preview with a WorldInAJar), the look overlay
 */
public class ItemConveyorWand extends ItemEnumMulti {

	public static enum ConveyorType {
		REGULAR,
		EXPRESS,
		DOUBLE,
		TRIPLE
	}

	private final ConveyorType type;

	public ItemConveyorWand(Properties properties, String descriptionId, ConveyorType type) {
		super(properties, descriptionId);
		this.type = type;
	}

	public ConveyorType getConveyorType() {
		return type;
	}

	public static BlockConveyorBase getConveyorBlock(ConveyorType type) {
		switch(type) {
		case EXPRESS: return ModBlocks.conveyor_express.get();
		case DOUBLE: return ModBlocks.conveyor_double.get();
		case TRIPLE: return ModBlocks.conveyor_triple.get();
		default: return ModBlocks.conveyor.get();
		}
	}

	public static boolean hasSnakesAndLadders(ConveyorType type) {
		return type == ConveyorType.REGULAR;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {

		if(Screen.hasShiftDown()) {
			for(String s : I18nUtil.resolveKeyArray("item.conveyor_wand.desc")) {
				list.add(Component.literal(s).withStyle(ChatFormatting.YELLOW));
			}
			if(hasSnakesAndLadders(type)) {
				list.add(Component.literal(I18nUtil.resolveKey("item.conveyor_wand.vertical.desc")).withStyle(ChatFormatting.AQUA));
			}
		} else {
			list.add(Component.literal("Hold <").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
					.append(Component.literal("LSHIFT").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC))
					.append(Component.literal("> to display more info").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
		}
	}

	private static CompoundTag getRoute(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data == null || !data.contains("side") ? null : data.copyTag();
	}

	private static boolean replaceable(BlockGetter world, BlockPos pos) {
		return world.getBlockState(pos).canBeReplaced();
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {

		ItemStack stack = context.getItemInHand();
		Player player = context.getPlayer();
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Direction side = context.getClickedFace();
		CompoundTag route = getRoute(stack);
		if(player == null) return InteractionResult.PASS;

		if(player.isShiftKeyDown() && route == null) {
			if(world.isClientSide) return InteractionResult.SUCCESS;

			Block onBlock = world.getBlockState(pos).getBlock();

			// sneak-clicking the top or bottom of a regular belt turns it into a lift or chute to build on
			if(hasSnakesAndLadders(type) && onBlock == ModBlocks.conveyor.get() && world.getBlockState(pos).getValue(BlockConveyorBendable.CURVE) == BlockConveyorBendable.Curve.STRAIGHT) {
				int onMeta = ModBlocks.conveyor.get().getMeta(world.getBlockState(pos));
				if(side == Direction.UP) {
					onBlock = ModBlocks.conveyor_lift.get();
					world.setBlock(pos, ModBlocks.conveyor_lift.get().getPlacementState(world, pos, onMeta), 3);
				} else if(side == Direction.DOWN) {
					onBlock = ModBlocks.conveyor_chute.get();
					world.setBlock(pos, ModBlocks.conveyor_chute.get().getPlacementState(world, pos, onMeta), 3);
				}
			}

			BlockConveyorBase toPlace = getConveyorBlock(type);
			if(hasSnakesAndLadders(type)) {
				if(onBlock == ModBlocks.conveyor_lift.get() && side == Direction.UP) toPlace = ModBlocks.conveyor_lift.get();
				if(onBlock == ModBlocks.conveyor_chute.get() && side == Direction.DOWN) toPlace = ModBlocks.conveyor_chute.get();
			}

			BlockPos place = pos.relative(side);

			if(replaceable(world, place)) {
				world.setBlock(place, toPlace.getPlacementState(world, place, BlockConveyorBase.getFacingMeta(player.getYRot())), 3);
				if(!player.isCreative()) stack.shrink(1);
			}

			return InteractionResult.SUCCESS;
		}

		// If placing on top of a conveyor block, auto-snap to edge if possible
		// this makes it easier to connect without having to click the small edge of a conveyor
		BlockState onState = world.getBlockState(pos);
		if(onState.getBlock() instanceof BlockConveyorBendable bendable) {
			Direction moveDir = route != null ? bendable.getInputDirection(world, pos) : bendable.getOutputDirection(world, pos);

			if(replaceable(world, pos.relative(moveDir))) {
				side = moveDir;
			}
		}

		if(route == null) {
			// Starting placement
			CompoundTag nbt = new CompoundTag();
			nbt.putInt("x", pos.getX());
			nbt.putInt("y", pos.getY());
			nbt.putInt("z", pos.getZ());
			nbt.putInt("side", side.get3DDataValue());

			int count = 0;
			if(player.isCreative()) {
				count = 256;
			} else {
				for(ItemStack inventoryStack : player.getInventory().items) {
					if(inventoryStack.is(this)) count += inventoryStack.getCount();
				}
			}

			nbt.putInt("count", count);
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
		} else {
			// Constructing conveyor
			int sx = route.getInt("x");
			int sy = route.getInt("y");
			int sz = route.getInt("z");
			int sSide = route.getInt("side");
			int count = route.getInt("count");

			if(!world.isClientSide) {

				// pretend to construct, if it doesn't fail, actually construct
				int constructCount = construct(world, false, type, player, sx, sy, sz, sSide, pos.getX(), pos.getY(), pos.getZ(), side.get3DDataValue(), count);
				if(constructCount > 0) {
					int toRemove = construct(world, true, type, player, sx, sy, sz, sSide, pos.getX(), pos.getY(), pos.getZ(), side.get3DDataValue(), count);

					if(!player.isCreative()) {
						for(ItemStack inventoryStack : player.getInventory().items) {
							if(inventoryStack.is(this)) {
								int removing = Math.min(toRemove, inventoryStack.getCount());
								inventoryStack.shrink(removing);
								toRemove -= removing;
							}

							if(toRemove <= 0) break;
						}

						player.containerMenu.broadcastChanges();
					}

					player.sendSystemMessage(Component.literal("Conveyor built!"));
				} else if(constructCount == 0) {
					player.sendSystemMessage(Component.literal("Not enough conveyors, build cancelled"));
				} else {
					player.sendSystemMessage(Component.literal("Conveyor obstructed, build cancelled"));
				}
			}

			stack.remove(DataComponents.CUSTOM_DATA);
		}

		return InteractionResult.SUCCESS; // always eat interactions
	}

	/** Forgets the start point when the wand isn't held anymore */
	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean inHand) {
		if(!(entity instanceof Player player)) return;

		if(!inHand && getRoute(stack) != null) {
			ItemStack held = player.getMainHandItem();
			if(!held.is(this)) {
				stack.remove(DataComponents.CUSTOM_DATA);
			}
		}
	}

	private static boolean breakingLine = false;

	/** In creative, sneak-breaking a belt removes the whole line (the original used onBlockStartBreak) */
	@Override
	public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player playerEntity) {
		if(breakingLine) return true;
		if(!playerEntity.isShiftKeyDown()) return true;
		if(!playerEntity.isCreative()) return true;
		if(!(playerEntity instanceof ServerPlayer player)) return true;

		if(state.getBlock() instanceof BlockConveyorBase conveyor) {
			Direction input = conveyor.getInputDirection(world, pos);
			Direction output = conveyor.getOutputDirection(world, pos);
			breakingLine = true;
			try {
				breakExtra(world, player, pos.relative(input), 32);
				breakExtra(world, player, pos.relative(output), 32);
			} finally {
				breakingLine = false;
			}
		}

		return true;
	}

	private void breakExtra(Level world, ServerPlayer player, BlockPos pos, int depth) {
		depth--;
		if(depth <= 0) return;

		if(!(world.getBlockState(pos).getBlock() instanceof BlockConveyorBase conveyor)) return;

		Direction input = conveyor.getInputDirection(world, pos);
		Direction output = conveyor.getOutputDirection(world, pos);

		if(!player.gameMode.destroyBlock(pos)) return;

		breakExtra(world, player, pos.relative(input), depth);
		breakExtra(world, player, pos.relative(output), depth);
	}

	/**
	 * Attempts to construct a conveyor between two points, including bends, lifts, and chutes.
	 * @return the amount of belts used, 0 if there weren't enough, -1 if something is in the way
	 */
	public static int construct(Level world, boolean build, ConveyorType type, Player player, int x1, int y1, int z1, int side1, int x2, int y2, int z2, int side2, int max) {
		Direction dir = Direction.from3DDataValue(side1);
		Direction targetDir = Direction.from3DDataValue(side2);

		// if placing within a single block, we have to handle rotation specially, treating it like a manual placement with player facing
		if(x1 == x2 && y1 == y2 && z1 == z2 && side1 == side2 && (dir == Direction.UP || dir == Direction.DOWN)) {
			int meta = BlockConveyorBase.getFacingMeta(player.getYRot());

			y1 += dir.getStepY();
			BlockPos p = new BlockPos(x1, y1, z1);

			if(!replaceable(world, p)) return -1;

			BlockConveyorBase block = getConveyorBlock(type);
			if(build) world.setBlock(p, block.getPlacementState(world, p, meta), 3);

			return 1;
		}

		boolean hasVertical = hasSnakesAndLadders(type);

		int tx = x2 + targetDir.getStepX();
		int ty = y2 + targetDir.getStepY();
		int tz = z2 + targetDir.getStepZ();

		int x = x1 + dir.getStepX();
		int y = y1 + dir.getStepY();
		int z = z1 + dir.getStepZ();

		if(dir == Direction.UP || dir == Direction.DOWN) {
			dir = getTargetDirection(x, y, z, x2, y2, z2, hasVertical);
		}

		Block targetBlock = world.getBlockState(new BlockPos(x2, y2, z2)).getBlock();
		boolean isTargetHorizontal = targetDir != Direction.UP && targetDir != Direction.DOWN;
		// TODO the original also turned towards cranes (BlockCraneBase)
		boolean shouldTurnToTarget = isTargetHorizontal || targetBlock == ModBlocks.conveyor_lift.get() || targetBlock == ModBlocks.conveyor_chute.get();

		Direction horDir = dir == Direction.UP || dir == Direction.DOWN ? Direction.from3DDataValue(BlockConveyorBase.getFacingMeta(player.getYRot())).getOpposite() : dir;

		// Initial dropdown to floor level, if possible
		if(hasVertical && y > ty) {
			if(replaceable(world, new BlockPos(x, y - 1, z))) {
				dir = Direction.DOWN;
			}
		}

		for(int loopDepth = 1; loopDepth <= max; loopDepth++) {
			BlockPos p = new BlockPos(x, y, z);
			if(!replaceable(world, p)) return -1;

			BlockConveyorBase block = getConveyorForDirection(type, dir);
			int meta = getConveyorMetaForDirection(block, dir, targetDir, horDir);

			int ox = x + dir.getStepX();
			int oy = y + dir.getStepY();
			int oz = z + dir.getStepZ();

			// check if we should turn before continuing
			int fromDistance = taxiDistance(x, y, z, tx, ty, tz);
			int toDistance = taxiDistance(ox, oy, oz, tx, ty, tz);
			int finalDistance = taxiDistance(ox, oy, oz, x2, y2, z2);
			boolean notAtTarget = (shouldTurnToTarget ? finalDistance : fromDistance) > 0;
			boolean willBeObstructed = notAtTarget && !replaceable(world, new BlockPos(ox, oy, oz));
			boolean shouldTurn = (toDistance >= fromDistance && notAtTarget) || willBeObstructed;

			if(shouldTurn) {
				Direction newDir = getTargetDirection(x, y, z, shouldTurnToTarget ? x2 : tx, shouldTurnToTarget ? y2 : ty, shouldTurnToTarget ? z2 : tz, tx, ty, tz, dir, willBeObstructed, hasVertical);

				if(newDir == Direction.UP) {
					block = ModBlocks.conveyor_lift.get();
				} else if(newDir == Direction.DOWN) {
					block = ModBlocks.conveyor_chute.get();
				// the original's vertical rotations returned the direction itself, so only horizontal belts bend
				} else if(dir.getAxis().isHorizontal() && dir.getClockWise() == newDir) {
					meta += 8;
				} else if(dir.getAxis().isHorizontal() && dir.getCounterClockWise() == newDir) {
					meta += 4;
				}

				dir = newDir;
				if(dir != Direction.UP && dir != Direction.DOWN) horDir = dir;
			}

			if(build) world.setBlock(p, block.getPlacementState(world, p, meta), 3);

			if(x == tx && y == ty && z == tz) return loopDepth;

			x += dir.getStepX();
			y += dir.getStepY();
			z += dir.getStepZ();
		}

		return 0;
	}

	private static int getConveyorMetaForDirection(Block block, Direction dir, Direction targetDir, Direction horDir) {
		if(block != ModBlocks.conveyor_chute.get() && block != ModBlocks.conveyor_lift.get()) return dir.getOpposite().get3DDataValue();
		if(targetDir == Direction.UP || targetDir == Direction.DOWN) return horDir.getOpposite().get3DDataValue();
		return targetDir.get3DDataValue();
	}

	private static BlockConveyorBase getConveyorForDirection(ConveyorType type, Direction dir) {
		if(dir == Direction.UP) return ModBlocks.conveyor_lift.get();
		if(dir == Direction.DOWN) return ModBlocks.conveyor_chute.get();
		return getConveyorBlock(type);
	}

	private static Direction getTargetDirection(int x1, int y1, int z1, int x2, int y2, int z2, boolean hasVertical) {
		return getTargetDirection(x1, y1, z1, x2, y2, z2, x2, y2, z2, null, false, hasVertical);
	}

	private static Direction getTargetDirection(int x1, int y1, int z1, int x2, int y2, int z2, int tx, int ty, int tz, Direction heading, boolean willBeObstructed, boolean hasVertical) {
		if(hasVertical && (y1 != y2 || y1 != ty) && (willBeObstructed || (x1 == x2 && z1 == z2) || (x1 == tx && z1 == tz))) return y1 > y2 ? Direction.DOWN : Direction.UP;

		if(Math.abs(x1 - x2) > Math.abs(z1 - z2)) {
			if(heading == Direction.EAST || heading == Direction.WEST) return z1 > z2 ? Direction.NORTH : Direction.SOUTH;
			return x1 > x2 ? Direction.WEST : Direction.EAST;
		} else {
			if(heading == Direction.NORTH || heading == Direction.SOUTH) return x1 > x2 ? Direction.WEST : Direction.EAST;
			return z1 > z2 ? Direction.NORTH : Direction.SOUTH;
		}
	}

	private static int taxiDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
		return Math.abs(x1 - x2) + Math.abs(y1 - y2) + Math.abs(z1 - z2);
	}
}
