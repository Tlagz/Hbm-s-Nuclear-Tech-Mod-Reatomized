package com.hbm.tileentity.machine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.tileentity.ModTileEntities;

import api.hbm.block.ICrucibleAcceptor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Foundry channel: moves molten material sideways, into molds and outlets first, otherwise spreading it over the
 * neighboring channels. A network of connected channels only carries one material at a time; the original kept track
 * of that with a node network, here pouring checks the connected channels directly.
 */
public class TileEntityFoundryChannel extends TileEntityFoundryBase {

	public int nextUpdate;
	/** Direction the material came from, tried last so it doesn't flow back (0 = none) */
	public int lastFlow = 0;

	private static final Direction[] HORIZONTALS = { Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST };

	public TileEntityFoundryChannel(BlockPos pos, BlockState state) {
		super(ModTileEntities.FOUNDRY_CHANNEL.get(), pos, state);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			if(this.type == null && this.amount != 0) {
				this.amount = 0;
			}

			nextUpdate--;

			if(nextUpdate <= 0 && this.amount > 0 && this.type != null) {

				boolean hasOp = false;
				nextUpdate = 5;

				List<Direction> dirs = new ArrayList<>(List.of(HORIZONTALS));
				Collections.shuffle(dirs);
				if(lastFlow > 0) {
					Direction last = Direction.from3DDataValue(lastFlow);
					dirs.remove(last);
					dirs.add(last);
				}

				for(Direction dir : dirs) {
					BlockPos target = worldPosition.relative(dir);
					Block b = level.getBlockState(target).getBlock();

					if(b instanceof ICrucibleAcceptor acc && b != ModBlocks.foundry_channel.get()) {

						if(acc.canAcceptPartialFlow(level, target, dir.getOpposite(), new MaterialStack(this.type, this.amount))) {
							MaterialStack left = acc.flow(level, target, dir.getOpposite(), new MaterialStack(this.type, this.amount));
							if(left == null) {
								this.type = null;
								this.amount = 0;
							} else {
								this.amount = left.amount;
							}
							hasOp = true;
							break;
						}
					}
				}

				if(!hasOp) {
					for(Direction dir : dirs) {
						if(level.getBlockEntity(worldPosition.relative(dir)) instanceof TileEntityFoundryChannel acc) {

							if(acc.type == null || acc.type == this.type || acc.amount == 0) {
								acc.type = this.type;
								acc.lastFlow = dir.getOpposite().get3DDataValue();

								if(level.random.nextInt(5) == 0 || this.amount == 1) { //force swap operations with single quanta to keep them moving
									//1:4 chance that the fill states are simply swapped
									//this promotes faster spreading and prevents spread limits
									int buf = this.amount;
									this.amount = acc.amount;
									acc.amount = buf;

								} else {
									//otherwise, equalize the neighbors
									int diff = this.amount - acc.amount;

									if(diff > 0) {
										diff /= 2;
										this.amount -= diff;
										acc.amount += diff;
									}
								}
							}
						}
					}
				}
			}

			if(this.amount == 0) {
				this.type = null;
				this.lastFlow = 0;
				this.nextUpdate = 5;
			}
		}

		super.updateEntity();
	}

	/** Whether the channels connected to this one only carry the given material (or nothing) */
	public boolean networkAccepts(MaterialStack stack) {
		Set<BlockPos> visited = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		queue.add(worldPosition);
		visited.add(worldPosition);

		while(!queue.isEmpty() && visited.size() < 1024) {
			BlockPos pos = queue.poll();
			if(!(level.getBlockEntity(pos) instanceof TileEntityFoundryChannel channel)) continue;
			if(channel.type != null && channel.amount > 0 && channel.type != stack.material) return false;
			for(Direction dir : HORIZONTALS) {
				BlockPos next = pos.relative(dir);
				if(visited.add(next) && level.getBlockEntity(next) instanceof TileEntityFoundryChannel) queue.add(next);
			}
		}
		return true;
	}

	@Override
	public int getCapacity() {
		return MaterialShapes.INGOT.q(2);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.lastFlow = nbt.getByte("flow");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putByte("flow", (byte) this.lastFlow);
	}

	@Override
	public boolean canAcceptPartialPour(Level world, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
		if(!networkAccepts(stack)) return false;
		return super.canAcceptPartialPour(world, pos, dX, dY, dZ, side, stack);
	}
}
