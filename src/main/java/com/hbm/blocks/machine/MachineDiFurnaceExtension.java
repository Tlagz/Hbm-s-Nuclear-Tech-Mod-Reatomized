package com.hbm.blocks.machine;

import com.hbm.blocks.IProxyController;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityDiFurnace;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The chimney on top of the alloy furnace: triples its speed, forwards items (and smoke) to the furnace below and opens
 * its GUI. Rendered with difurnace_extension.obj.
 */
public class MachineDiFurnaceExtension extends Block implements EntityBlock, IProxyController {

	private static final VoxelShape SHAPE = Shapes.or(Block.box(0, 0, 0, 16, 4, 16), Block.box(2, 4, 2, 14, 10, 14), Block.box(4, 10, 4, 12, 16, 12));

	public MachineDiFurnaceExtension(Properties properties) {
		super(properties.noOcclusion());
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		TileEntityProxyCombo proxy = new TileEntityProxyCombo(pos, state);
		proxy.inventory = true;
		proxy.fluid = true;
		return proxy;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if(player.isShiftKeyDown()) return InteractionResult.PASS;
		if(!(world.getBlockEntity(pos.below()) instanceof TileEntityDiFurnace furnace)) return InteractionResult.PASS;

		if(!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
			serverPlayer.openMenu(furnace, buf -> buf.writeBlockPos(furnace.getBlockPos()));
		}
		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	@Override
	public BlockEntity getCore(Level world, BlockPos pos) {
		BlockEntity tile = world.getBlockEntity(pos.below());
		return tile instanceof TileEntityDiFurnace ? tile : null;
	}
}
