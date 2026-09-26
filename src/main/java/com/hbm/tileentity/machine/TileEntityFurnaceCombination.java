package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.container.ContainerFurnaceCombo;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.CombinationRecipes;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachinePolluting;
import com.hbm.util.Tuple.Pair;

import api.hbm.tile.IHeatSource;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Combination furnace: heat from below cooks items into coke, charcoal etc. and a fluid byproduct (creosote, wood
 * oil, chlorine...), the flames on top set things on fire. Slots: 0 input, 1 output, 2-3 fluid container in/out.
 *
 * TODO copy tool
 */
public class TileEntityFurnaceCombination extends TileEntityMachinePolluting implements MenuProvider {

	public boolean wasOn;
	public int progress;
	public static int processTime = 20_000;

	public int heat;
	public static int maxHeat = 100_000;
	public static double diffusion = 0.25D;

	public FluidTank tank;

	public TileEntityFurnaceCombination(BlockPos pos, BlockState state) {
		super(ModTileEntities.FURNACE_COMBINATION.get(), pos, state, 4, 50);
		this.tank = new FluidTank(Fluids.NONE, 24_000);
	}

	@Override
	public String getName() {
		return "container.furnaceCombination";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.tryPullHeat();

			// every block of the outside at the sides and the top is a port
			if(level.getGameTime() % 20 == 0) {
				for(Direction dir : Direction.Plane.HORIZONTAL) {
					Direction rot = dir.getClockWise();
					for(int y = 0; y <= 1; y++) {
						for(int j = -1; j <= 1; j++) {
							BlockPos port = worldPosition.relative(dir, 2).relative(rot, j).above(y);
							if(tank.getFill() > 0) this.tryProvide(tank, level, port, dir);
							this.sendSmoke(port, dir);
						}
					}
				}

				for(int x = -1; x <= 1; x++) {
					for(int z = -1; z <= 1; z++) {
						BlockPos port = worldPosition.offset(x, 2, z);
						if(tank.getFill() > 0) this.tryProvide(tank, level, port, Direction.UP);
						this.sendSmoke(port, Direction.UP);
					}
				}
			}

			this.wasOn = false;

			tank.unloadTank(2, 3, slots);

			if(canSmelt()) {
				int burn = heat / 100;

				if(burn > 0) {
					this.wasOn = true;
					this.progress += burn;
					this.heat -= burn;

					if(progress >= processTime) {
						this.setChanged();
						progress -= processTime;

						Pair<ItemStack, FluidStack> pair = CombinationRecipes.getOutput(slots.get(0));
						ItemStack out = pair.getKey();
						FluidStack fluid = pair.getValue();

						if(!out.isEmpty()) {
							if(slots.get(1).isEmpty()) {
								slots.set(1, out.copy());
							} else {
								slots.get(1).grow(out.getCount());
							}
						}

						if(fluid != null) {
							if(tank.getTankType() != fluid.type) tank.setTankType(fluid.type);
							tank.setFill(tank.getFill() + fluid.fill);
						}

						this.removeItem(0, 1);
					}

					List<Entity> entities = level.getEntitiesOfClass(Entity.class, new AABB(worldPosition.getX() - 0.5, worldPosition.getY() + 2, worldPosition.getZ() - 0.5, worldPosition.getX() + 1.5, worldPosition.getY() + 4, worldPosition.getZ() + 1.5));
					for(Entity e : entities) e.igniteForSeconds(5);

					if(level.getGameTime() % 10 == 0) level.playSound(null, worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(), ModSounds.get("weapon.flamethrowerShoot"), SoundSource.BLOCKS, 0.25F, 0.5F);
					if(level.getGameTime() % 20 == 0) this.pollute(PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * 3);
				}

			} else {
				this.progress = 0;
			}

			this.networkPackNT(50);

		} else {

			if(this.wasOn && level.random.nextInt(15) == 0) {
				level.addParticle(ParticleTypes.LAVA, worldPosition.getX() + 0.5 + level.random.nextGaussian() * 0.5, worldPosition.getY() + 2, worldPosition.getZ() + 0.5 + level.random.nextGaussian() * 0.5, 0, 0, 0);
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(wasOn);
		buf.writeInt(heat);
		buf.writeInt(progress);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		wasOn = buf.readBoolean();
		heat = buf.readInt();
		progress = buf.readInt();
		tank.deserialize(buf);
	}

	public boolean canSmelt() {
		if(slots.get(0).isEmpty()) return false;

		Pair<ItemStack, FluidStack> pair = CombinationRecipes.getOutput(slots.get(0));
		if(pair == null) return false;

		ItemStack out = pair.getKey();
		FluidStack fluid = pair.getValue();

		if(!out.isEmpty() && !slots.get(1).isEmpty()) {
			if(!ItemStack.isSameItemSameComponents(out, slots.get(1))) return false;
			if(out.getCount() + slots.get(1).getCount() > slots.get(1).getMaxStackSize()) return false;
		}

		if(fluid != null) {
			if(tank.getTankType() != fluid.type && tank.getFill() > 0) return false;
			if(tank.getTankType() == fluid.type && tank.getFill() + fluid.fill > tank.getMaxFill()) return false;
		}

		return true;
	}

	protected void tryPullHeat() {

		if(this.heat >= maxHeat) return;

		if(level.getBlockEntity(worldPosition.below()) instanceof IHeatSource source) {
			int diff = source.getHeatStored() - this.heat;

			if(diff == 0) return;

			if(diff > 0) {
				diff = (int) Math.ceil(diff * diffusion);
				source.useUpHeat(diff);
				this.heat += diff;
				if(this.heat > maxHeat) this.heat = maxHeat;
				return;
			}
		}

		this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] { 0, 1 };
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return i == 0 && CombinationRecipes.getOutput(itemStack) != null;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i == 1;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.tank.readFromNBT(nbt, "tank");
		this.progress = nbt.getInt("prog");
		this.heat = nbt.getInt("heat");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		this.tank.writeToNBT(nbt, "tank");
		nbt.putInt("prog", progress);
		nbt.putInt("heat", heat);
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 5, worldPosition.getZ() + 2);
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] {tank};
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] {tank, smoke, smoke_leaded, smoke_poison};
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerFurnaceCombo(id, inv, this);
	}
}
