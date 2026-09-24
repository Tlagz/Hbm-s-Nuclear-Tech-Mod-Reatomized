package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.ModDataComponents;
import com.hbm.tileentity.machine.oil.TileEntityMachineOilWell;

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

/**
 * Oil derrick: a 3x3 tower, 9 blocks tall, standing on four corner legs, the core sits in the middle of the
 * bottom layer between the legs (where the pipes connect).
 *
 * TODO explosions spilling the tanks (onBlockExploded), look overlay
 */
public class MachineOilWell extends BlockDummyable {

	public MachineOilWell(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineOilWell(pos, state);
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {9, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	protected boolean checkRequirement(Level world, BlockPos core, BlockPos placed, Direction dir) {
		return MultiblockHandlerXR.checkSpace(world, core, new int[] {1, -1, 0, 0, 0, 0}, placed, dir) &&
				MultiblockHandlerXR.checkSpace(world, core.above(), new int[] {8, 0, 1, 1, 1, 1}, placed, dir) &&
				MultiblockHandlerXR.checkSpace(world, core.offset(1, 1, 1), new int[] {-1, 1, 0, 0, 0, 0}, placed, dir) &&
				MultiblockHandlerXR.checkSpace(world, core.offset(1, 1, -1), new int[] {-1, 1, 0, 0, 0, 0}, placed, dir) &&
				MultiblockHandlerXR.checkSpace(world, core.offset(-1, 1, 1), new int[] {-1, 1, 0, 0, 0, 0}, placed, dir) &&
				MultiblockHandlerXR.checkSpace(world, core.offset(-1, 1, -1), new int[] {-1, 1, 0, 0, 0, 0}, placed, dir);
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		BlockPos core = pos.relative(dir, o);
		MultiblockHandlerXR.fillSpace(world, core, new int[] {1, -1, 0, 0, 0, 0}, this, dir);
		MultiblockHandlerXR.fillSpace(world, core.above(), new int[] {8, 0, 1, 1, 1, 1}, this, dir);
		MultiblockHandlerXR.fillSpace(world, core.offset(1, 1, 1), new int[] {-1, 1, 0, 0, 0, 0}, this, dir);
		MultiblockHandlerXR.fillSpace(world, core.offset(1, 1, -1), new int[] {-1, 1, 0, 0, 0, 0}, this, dir);
		MultiblockHandlerXR.fillSpace(world, core.offset(-1, 1, 1), new int[] {-1, 1, 0, 0, 0, 0}, this, dir);
		MultiblockHandlerXR.fillSpace(world, core.offset(-1, 1, -1), new int[] {-1, 1, 0, 0, 0, 0}, this, dir);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		CustomData persistent = stack.get(ModDataComponents.PERSISTENT.get());
		if(persistent == null) return;
		var nbt = persistent.copyTag();
		list.add(Component.literal(nbt.getLong("power") + "HE").withStyle(ChatFormatting.GREEN));
		for(int i = 0; i < 2; i++) {
			FluidTank tank = new FluidTank(Fluids.NONE, 0);
			tank.readFromNBT(nbt, "t" + i);
			list.add(Component.literal(tank.getFill() + "/" + tank.getMaxFill() + "mB ").append(Component.translatable(tank.getTankType().getConditionalName())).withStyle(ChatFormatting.YELLOW));
		}
	}
}
