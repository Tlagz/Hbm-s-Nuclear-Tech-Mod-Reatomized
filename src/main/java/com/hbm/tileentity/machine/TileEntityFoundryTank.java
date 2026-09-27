package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.tileentity.ModTileEntities;

import api.hbm.block.ICrucibleAcceptor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Foundry tank: stores four blocks worth of molten material, drains into the tank below first, then into adjacent
 * acceptors like outlets (but not channels), and otherwise evens itself out with the tanks next to it.
 */
public class TileEntityFoundryTank extends TileEntityFoundryBase {

	public int nextUpdate;

	public TileEntityFoundryTank(BlockPos pos, BlockState state) {
		super(ModTileEntities.FOUNDRY_TANK.get(), pos, state);
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
				nextUpdate = level.random.nextInt(6) + 5;

				if(level.getBlockEntity(worldPosition.below()) instanceof TileEntityFoundryTank tank) {

					if((tank.type == null || tank.type == this.type) && tank.amount < tank.getCapacity()) {
						tank.type = this.type;
						int toFill = Math.min(this.amount, tank.getCapacity() - tank.amount);
						this.amount -= toFill;
						tank.amount += toFill;
						hasOp = true;
					}
				}

				List<Direction> dirs = new ArrayList<>(Direction.Plane.HORIZONTAL.stream().toList());
				Collections.shuffle(dirs);

				if(!hasOp) {

					for(Direction dir : dirs) {
						BlockPos pos = worldPosition.relative(dir);
						BlockState b = level.getBlockState(pos);

						if(b.getBlock() instanceof ICrucibleAcceptor acc && !b.is(ModBlocks.foundry_channel.get())) {

							if(acc.canAcceptPartialFlow(level, pos, dir.getOpposite(), new MaterialStack(this.type, this.amount))) {
								MaterialStack left = acc.flow(level, pos, dir.getOpposite(), new MaterialStack(this.type, this.amount));
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
				}

				if(!hasOp) {
					for(Direction dir : dirs) {

						if(level.getBlockEntity(worldPosition.relative(dir)) instanceof TileEntityFoundryTank acc) {

							if(acc.type == null || acc.type == this.type || acc.amount == 0) {
								acc.type = this.type;
								if(level.random.nextInt(5) == 0) {
									//1:4 chance that the fill states are simply swapped
									//this promotes faster spreading and prevents spread limits
									int buf = this.amount;
									this.amount = acc.amount;
									acc.amount = buf;

								} else {
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
		}

		super.updateEntity();
	}

	@Override
	public int getCapacity() {
		return MaterialShapes.BLOCK.q(4);
	}
}
