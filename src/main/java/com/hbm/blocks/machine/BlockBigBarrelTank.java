package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.ModDataComponents;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.machine.storage.TileEntityBarrel;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Multiblock tanks with a barrel tile (BAT9000, Orbus): barrel GUI, identifier sneak-click, contents kept when broken */
public abstract class BlockBigBarrelTank extends BlockDummyable {

	public BlockBigBarrelTank(Properties properties) {
		super(properties);
	}

	protected TileEntityBarrel getTank(Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		return core != null && world.getBlockEntity(core) instanceof TileEntityBarrel tank ? tank : null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if(player.isShiftKeyDown()) return InteractionResult.PASS;
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

		if(player.isShiftKeyDown() && stack.getItem() instanceof IItemFluidIdentifier id) {
			TileEntityBarrel tank = getTank(world, pos);
			if(tank == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

			if(!world.isClientSide) {
				FluidType type = id.getType(world, tank.getBlockPos(), stack);
				tank.tank.setTankType(type);
				tank.setChanged();
				player.sendSystemMessage(Component.literal("Changed type to ").withStyle(ChatFormatting.YELLOW)
						.append(Component.translatable(type.getConditionalName())).append(Component.literal("!")));
			}
			return ItemInteractionResult.sidedSuccess(world.isClientSide);
		}

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		CustomData persistent = stack.get(ModDataComponents.PERSISTENT.get());
		if(persistent != null) {
			FluidTank tank = new FluidTank(Fluids.NONE, 0);
			tank.readFromNBT(persistent.copyTag(), "tank");
			list.add(Component.literal(tank.getFill() + "/" + tank.getMaxFill() + "mB ").append(Component.translatable(tank.getTankType().getConditionalName())).withStyle(ChatFormatting.YELLOW));
		}
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
		if(getMeta(state) < extra) return 0;
		TileEntityBarrel tank = getTank(world, pos);
		return tank == null ? 0 : tank.getComparatorPower();
	}
}
