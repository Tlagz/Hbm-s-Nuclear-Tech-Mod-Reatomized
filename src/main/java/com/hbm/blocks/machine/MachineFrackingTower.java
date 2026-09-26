package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.ModDataComponents;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.oil.TileEntityMachineFrackingTower;
import com.hbm.util.BobMathUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Hydraulic fracking tower: a 7x7 platform on four legs, a tapering tower 25 blocks tall; ports around the base column */
public class MachineFrackingTower extends BlockDummyable {

	private static final int[] PLATFORM = new int[] {1, 0, 3, 3, 3, 3};
	private static final int[] LEG = new int[] {-1, 2, 0, 1, 0, 1};
	private static final int[] TOWER_LOWER = new int[] {10, -4, 2, 2, 2, 2};
	private static final int[] TOWER_UPPER = new int[] {24, -9, 1, 1, 1, 1};
	private static final int[] CRANE = new int[] {1, 0, 1, 1, -2, 3};

	public MachineFrackingTower(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineFrackingTower(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).power().fluid();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {3, 0, 0, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	protected boolean checkRequirement(Level world, BlockPos core, BlockPos placed, Direction dir) {
		if(!MultiblockHandlerXR.checkSpace(world, placed.above(2), PLATFORM, placed, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, placed.offset(-2, 2, -2), LEG, placed, Direction.NORTH)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, placed.offset(-2, 2, 3), LEG, placed, Direction.NORTH)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, placed.offset(3, 2, -2), LEG, placed, Direction.NORTH)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, placed.offset(3, 2, 3), LEG, placed, Direction.NORTH)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, placed, TOWER_LOWER, placed, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, placed, TOWER_UPPER, placed, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, placed.above(15), CRANE, placed, dir)) return false;
		return super.checkRequirement(world, core, placed, dir);
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		MultiblockHandlerXR.fillSpace(world, pos, getDimensions(), this, dir);
		MultiblockHandlerXR.fillSpace(world, pos.above(2), PLATFORM, this, dir);
		MultiblockHandlerXR.fillSpace(world, pos.offset(-2, 2, -2), LEG, this, Direction.NORTH);
		MultiblockHandlerXR.fillSpace(world, pos.offset(-2, 2, 3), LEG, this, Direction.NORTH);
		MultiblockHandlerXR.fillSpace(world, pos.offset(3, 2, -2), LEG, this, Direction.NORTH);
		MultiblockHandlerXR.fillSpace(world, pos.offset(3, 2, 3), LEG, this, Direction.NORTH);
		MultiblockHandlerXR.fillSpace(world, pos, TOWER_LOWER, this, dir);
		MultiblockHandlerXR.fillSpace(world, pos, TOWER_UPPER, this, dir);
		MultiblockHandlerXR.fillSpace(world, pos.above(15), CRANE, this, Direction.WEST);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if(player.isShiftKeyDown()) return InteractionResult.PASS;
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		CustomData persistent = stack.get(ModDataComponents.PERSISTENT.get());
		if(persistent == null) return;
		var nbt = persistent.copyTag();
		list.add(Component.literal(BobMathUtil.getShortNumber(nbt.getLong("power")) + "HE").withStyle(ChatFormatting.GREEN));
		for(int i = 0; i < 2; i++) {
			FluidTank tank = new FluidTank(Fluids.NONE, 0);
			tank.readFromNBT(nbt, "t" + i);
			list.add(Component.literal(tank.getFill() + "/" + tank.getMaxFill() + "mB ").append(Component.translatable(tank.getTankType().getConditionalName())).withStyle(ChatFormatting.YELLOW));
		}
	}
}
