package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.ModDataComponents;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.storage.TileEntityMachineFluidTank;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Fluid tank, 5x3x3: fluid ports at the four corners. Explosions rupture the tank instead of destroying it, a second
 * explosion takes it out. Keeps its contents when broken.
 *
 * TODO blowtorch repair (IRepairable overlay)
 */
public class MachineFluidTank extends BlockDummyable {

	public MachineFluidTank(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineFluidTank(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).fluid();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 0, 1, 1, 2, 2};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	private TileEntityMachineFluidTank getTank(Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		return core != null && world.getBlockEntity(core) instanceof TileEntityMachineFluidTank tank ? tank : null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if(player.isShiftKeyDown()) return InteractionResult.PASS;
		TileEntityMachineFluidTank tank = getTank(world, pos);
		if(tank == null || tank.hasExploded) return InteractionResult.PASS;
		return this.standardOpenBehavior(world, pos, player);
	}

	/** Sneak click with a fluid identifier sets the type directly */
	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

		if(player.isShiftKeyDown() && stack.getItem() instanceof IItemFluidIdentifier id) {
			TileEntityMachineFluidTank tank = getTank(world, pos);
			if(tank == null || tank.hasExploded) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

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
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);
		this.makeExtra(world, core.offset(1, 0, 1));
		this.makeExtra(world, core.offset(1, 0, -1));
		this.makeExtra(world, core.offset(-1, 0, 1));
		this.makeExtra(world, core.offset(-1, 0, -1));
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
	public boolean canDropFromExplosion(BlockState state, BlockGetter world, BlockPos pos, Explosion explosion) {
		return false;
	}

	/** The first explosion ruptures the tank, the next one destroys it */
	@Override
	public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion) {
		TileEntityMachineFluidTank tank = getTank(world, pos);
		if(tank == null) {
			super.onBlockExploded(state, world, pos, explosion);
			return;
		}

		if(tank.lastExplosion == explosion) return;
		tank.lastExplosion = explosion;

		if(!tank.hasExploded) {
			tank.explode();
		} else {
			world.removeBlock(tank.getBlockPos(), false);
		}
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
		if(getMeta(state) < extra) return 0;
		TileEntityMachineFluidTank tank = getTank(world, pos);
		return tank == null ? 0 : tank.getComparatorPower();
	}
}
