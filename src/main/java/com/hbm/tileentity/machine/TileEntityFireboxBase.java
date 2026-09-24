package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.ItemEnums.EnumAshType;
import com.hbm.module.ModuleBurnTime;
import com.hbm.tileentity.TileEntityMachinePolluting;

import api.hbm.tile.IHeatSource;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Solid fuel heaters: burn fuel into heat (TU) for the machine above. The door opens while a player has the
 * GUI open. Slots 0 and 1 are fuel.
 *
 * TODO ash pit below (TileEntityAshpit)
 */
public abstract class TileEntityFireboxBase extends TileEntityMachinePolluting implements IHeatSource {

	public int maxBurnTime;
	public int burnTime;
	public int burnHeat;
	public boolean wasOn = false;
	private int playersUsing = 0;

	public float doorAngle = 0;
	public float prevDoorAngle = 0;

	public int heatEnergy;

	public TileEntityFireboxBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state, 2, 50);
	}

	@Override
	public void startOpen(Player player) {
		if(level != null && !level.isClientSide) this.playersUsing++;
	}

	@Override
	public void stopOpen(Player player) {
		if(level != null && !level.isClientSide) this.playersUsing--;
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			for(Direction dir : Direction.Plane.HORIZONTAL) {
				Direction rot = dir.getClockWise(); // ForgeDirection.getRotation(UP)

				for(int j = -1; j <= 1; j++) {
					this.sendSmoke(worldPosition.relative(dir, 2).relative(rot, j), dir);
				}
			}

			wasOn = false;

			if(burnTime <= 0) {

				for(int i = 0; i < 2; i++) {
					ItemStack stack = slots.get(i);
					if(!stack.isEmpty()) {

						int baseTime = getModule().getBurnTime(stack);

						if(baseTime > 0) {
							int fuel = (int) (baseTime * getTimeMult());

							this.maxBurnTime = this.burnTime = fuel;
							this.burnHeat = getModule().getBurnHeat(getBaseHeat(), stack);

							ItemStack container = stack.getCraftingRemainingItem();
							stack.shrink(1);
							if(stack.isEmpty()) slots.set(i, container);

							this.wasOn = true;
							break;
						}
					}
				}
			} else {

				if(this.heatEnergy < getMaxHeat()) {
					burnTime--;
					if(level.getGameTime() % 20 == 0) this.pollute(PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * 3);
				}
				this.wasOn = true;

				if(level.random.nextInt(15) == 0 && !this.muffled) {
					level.playSound(null, worldPosition, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0F, 0.5F + level.random.nextFloat() * 0.5F);
				}
			}

			if(wasOn) {
				this.heatEnergy = Math.min(this.heatEnergy + this.burnHeat, getMaxHeat());
			} else {
				this.heatEnergy = Math.max(this.heatEnergy - Math.max(this.heatEnergy / 1000, 1), 0);
				this.burnHeat = 0;
			}

			this.networkPackNT(50);
		} else {
			this.prevDoorAngle = this.doorAngle;
			float swingSpeed = (doorAngle / 10F) + 3;

			if(this.playersUsing > 0) {
				this.doorAngle += swingSpeed;
			} else {
				this.doorAngle -= swingSpeed;
			}

			this.doorAngle = Mth.clamp(this.doorAngle, 0F, 135F);

			if(wasOn && level.getGameTime() % 5 == 0) {
				Direction dir = BlockDummyable.getRotation(getBlockState());
				double x = worldPosition.getX() + 0.5 + dir.getStepX();
				double y = worldPosition.getY() + 0.25;
				double z = worldPosition.getZ() + 0.5 + dir.getStepZ();
				level.addParticle(ParticleTypes.FLAME, x + level.random.nextDouble() * 0.5 - 0.25, y + level.random.nextDouble() * 0.25, z + level.random.nextDouble() * 0.5 - 0.25, 0, 0, 0);
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(maxBurnTime);
		buf.writeInt(burnTime);
		buf.writeInt(burnHeat);
		buf.writeInt(heatEnergy);
		buf.writeInt(playersUsing);
		buf.writeBoolean(wasOn);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		maxBurnTime = buf.readInt();
		burnTime = buf.readInt();
		burnHeat = buf.readInt();
		heatEnergy = buf.readInt();
		playersUsing = buf.readInt();
		wasOn = buf.readBoolean();
	}

	public static EnumAshType getAshFromFuel(ItemStack stack) {
		return TileEntityMachineWoodBurner.getAshFromFuel(stack);
	}

	public abstract ModuleBurnTime getModule();
	public abstract int getBaseHeat();
	public abstract double getTimeMult();
	public abstract int getMaxHeat();

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] { 0, 1 };
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return getModule().getBurnTime(itemStack) > 0;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.maxBurnTime = nbt.getInt("maxBurnTime");
		this.burnTime = nbt.getInt("burnTime");
		this.burnHeat = nbt.getInt("burnHeat");
		this.heatEnergy = nbt.getInt("heatEnergy");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putInt("maxBurnTime", maxBurnTime);
		nbt.putInt("burnTime", burnTime);
		nbt.putInt("burnHeat", burnHeat);
		nbt.putInt("heatEnergy", heatEnergy);
	}

	@Override
	public int getHeatStored() {
		return heatEnergy;
	}

	@Override
	public void useUpHeat(int heat) {
		this.heatEnergy = Math.max(0, this.heatEnergy - heat);
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 1, worldPosition.getZ() + 2);
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[0];
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return this.getSmokeTanks();
	}

	@Override
	public boolean canConnect(FluidType type, Direction dir) {
		return dir != null && dir != Direction.DOWN;
	}
}
