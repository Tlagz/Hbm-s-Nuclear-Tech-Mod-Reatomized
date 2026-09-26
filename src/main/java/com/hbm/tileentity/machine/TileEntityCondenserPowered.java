package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.util.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * High-power condenser: two fans, huge tanks, 10 HE per mB condensed.
 * TODO config (IConfigurableMachine)
 */
public class TileEntityCondenserPowered extends TileEntityCondenser implements IEnergyReceiverMK2 {

	public long power;
	public float spin;
	public float lastSpin;

	//Configurable values
	public static long maxPower = 10_000_000;
	public static int inputTankSizeP = 1_000_000;
	public static int outputTankSizeP = 1_000_000;
	public static int powerConsumption = 10;

	public TileEntityCondenserPowered(BlockPos pos, BlockState state) {
		super(ModTileEntities.CONDENSER_POWERED.get(), pos, state);
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.SPENTSTEAM, inputTankSizeP);
		tanks[1] = new FluidTank(Fluids.WATER, outputTankSizeP);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if(!isServer()) {

			this.lastSpin = this.spin;

			if(this.waterTimer > 0) {
				this.spin += 30F;

				if(this.spin >= 360F) {
					this.spin -= 360F;
					this.lastSpin -= 360F;
				}

				if(level.getGameTime() % 4 == 0) {
					Direction dir = BlockDummyable.getRotation(getBlockState());
					double x = worldPosition.getX() + 0.5, y = worldPosition.getY() + 1.5, z = worldPosition.getZ() + 0.5;
					level.addParticle(ParticleTypes.CLOUD, x + dir.getStepX() * 1.5, y, z + dir.getStepZ() * 1.5, dir.getStepX() * 0.1, 0, dir.getStepZ() * 0.1);
					level.addParticle(ParticleTypes.CLOUD, x - dir.getStepX() * 1.5, y, z - dir.getStepZ() * 1.5, dir.getStepX() * -0.1, 0, dir.getStepZ() * -0.1);
				}
			}
		}
	}

	@Override
	public boolean extraCondition(int convert) {
		return power >= (convert * powerConsumption) * 0.95; // bit of tolerance
	}

	@Override
	public void postConvert(int convert) {
		this.power -= convert * powerConsumption;
		if(this.power < 0) this.power = 0;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(this.power);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.power = nbt.getLong("power");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("power", power);
	}

	@Override
	protected void subscribeToAllAround() {
		for(DirPos pos : getConPos()) {
			this.trySubscribe(this.tanks[0].getTankType(), level, pos, pos.getDir());
			this.trySubscribe(level, pos, pos.getDir());
		}
	}

	@Override
	protected void sendFluidToAll() {
		for(DirPos pos : getConPos()) this.tryProvide(this.tanks[1], level, pos, pos.getDir());
	}

	public DirPos[] getConPos() {

		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		BlockPos p = worldPosition.above();

		return new DirPos[] {
				new DirPos(p.relative(rot, 4), rot),
				new DirPos(p.relative(rot, -4), rot.getOpposite()),
				new DirPos(p.relative(dir, 2).relative(rot, -1), dir),
				new DirPos(p.relative(dir, 2).relative(rot, 1), dir),
				new DirPos(p.relative(dir, -2).relative(rot, -1), dir.getOpposite()),
				new DirPos(p.relative(dir, -2).relative(rot, 1), dir.getOpposite())
		};
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 3, worldPosition.getY(), worldPosition.getZ() - 3, worldPosition.getX() + 4, worldPosition.getY() + 3, worldPosition.getZ() + 4);
	}

	@Override
	public long getPower() {
		return this.power;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}
}
