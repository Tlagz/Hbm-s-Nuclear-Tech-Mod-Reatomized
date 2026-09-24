package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.inventory.fluid.trait.FT_Combustible.FuelGrade;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.machine.TileEntityMachineDiesel;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/** Diesel generator, rendered by RenderDieselGen (the engine shakes while running) */
public class MachineDiesel extends BlockMachineTile {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public MachineDiesel(Properties properties) {
		super(properties.noOcclusion(), ModTileEntities.DIESEL);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	/** Faces the player, the original's BlockMachineBase rotatable metadata */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {

		if(world.getBlockEntity(pos) instanceof TileEntityMachineDiesel diesel) {

			if(diesel.isOn && diesel.hasAcceptableFuel() && diesel.tank.getFill() > 0) {

				Direction dir = state.getValue(FACING);
				Direction rot = dir.getClockWise(); // ForgeDirection.getRotation(UP)
				world.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5 - dir.getStepX() * 0.6 + rot.getStepX() * 0.1875, pos.getY() + 0.3125, pos.getZ() + 0.5 - dir.getStepZ() * 0.6 + rot.getStepZ() * 0.1875, 0, 0, 0);
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {

		list.add(Component.literal("Fuel efficiency:").withStyle(ChatFormatting.YELLOW));
		for(FuelGrade grade : FuelGrade.values()) {
			Double efficiency = TileEntityMachineDiesel.fuelEfficiency.get(grade);

			if(efficiency != null) {
				int eff = (int) (efficiency * 100);
				list.add(Component.literal("-" + grade.getLocalizedName() + ": ").withStyle(ChatFormatting.YELLOW).append(Component.literal(eff + "%").withStyle(ChatFormatting.RED)));
			}
		}
	}
}
