package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineCyclotron;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Cyclotron, a 5x5 ring three blocks high; the middles of the four sides are ports, plug items go in by hand */
public class MachineCyclotron extends BlockDummyable {

	public MachineCyclotron(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineCyclotron(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).inventory().power().fluid();
		return null;
	}

	@Override public int[] getDimensions() { return new int[] {2, 0, 2, 2, 2, 2}; }
	@Override public int getOffset() { return 2; }

	/** The plug items go into their sockets */
	@Override
	protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if(player.isShiftKeyDown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityMachineCyclotron cyc)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		for(int i = 0; i < 4; i++) {
			if(held.is(TileEntityMachineCyclotron.getItemForPlug(i)) && !cyc.getPlug(i)) {
				if(!world.isClientSide) {
					held.shrink(1);
					cyc.setPlug(i);
					world.playSound(null, pos, ModSounds.get("item.upgradePlug"), SoundSource.BLOCKS, 1.5F, 1.0F);
				}
				return ItemInteractionResult.sidedSuccess(world.isClientSide);
			}
		}

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);

		BlockPos core = pos.relative(dir, o);
		for(int i = -1; i <= 1; i++) {
			this.makeExtra(world, core.offset(2, 0, i));
			this.makeExtra(world, core.offset(-2, 0, i));
			this.makeExtra(world, core.offset(i, 0, 2));
			this.makeExtra(world, core.offset(i, 0, -2));
		}
	}
}
