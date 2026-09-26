package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.inventory.fluid.trait.FT_Coolable.CoolingType;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.DirPos;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Steam engine: steam in, spent steam out, power from the steam's heat at 85% efficiency. The flywheel speeds up
 * while there's steam and gives a heavy thump every turn.
 *
 * TODO config (IConfigurableMachine), copy tool
 */
public class TileEntitySteamEngine extends TileEntityLoadedBase implements IEnergyProviderMK2, IFluidStandardTransceiverMK2 {

	public long powerBuffer;

	public float rotor;
	public float lastRotor;
	private float syncRotor;
	public FluidTank[] tanks;

	private int turnProgress;
	private float acceleration = 0F;

	/* CONFIGURABLE */
	private static int steamCap = 2_000;
	private static int ldsCap = 20;
	private static double efficiency = 0.85D;

	public TileEntitySteamEngine(BlockPos pos, BlockState state) {
		super(ModTileEntities.STEAM_ENGINE.get(), pos, state);
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.STEAM, steamCap);
		tanks[1] = new FluidTank(Fluids.SPENTSTEAM, ldsCap);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.powerBuffer = 0;

			tanks[0].setTankType(Fluids.STEAM);
			tanks[1].setTankType(Fluids.SPENTSTEAM);

			FT_Coolable trait = tanks[0].getTankType().getTrait(FT_Coolable.class);
			double eff = trait.getEfficiency(CoolingType.TURBINE) * efficiency;

			int inputOps = tanks[0].getFill() / trait.amountReq;
			int outputOps = (tanks[1].getMaxFill() - tanks[1].getFill()) / trait.amountProduced;
			int ops = Math.min(inputOps, outputOps);
			tanks[0].setFill(tanks[0].getFill() - ops * trait.amountReq);
			tanks[1].setFill(tanks[1].getFill() + ops * trait.amountProduced);
			this.powerBuffer += (ops * trait.heatEnergy * eff);

			if(ops > 0) {
				this.acceleration += 0.1F;
			} else {
				this.acceleration -= 0.1F;
			}

			this.acceleration = Mth.clamp(this.acceleration, 0F, 40F);
			this.rotor += this.acceleration;

			if(this.rotor >= 360D) {
				this.rotor -= 360D;
				level.playSound(null, worldPosition, ModSounds.get("block.steamEngineOperate"), SoundSource.BLOCKS, getVolume(1.0F), 0.5F + (acceleration / 80F));
			}

			for(DirPos pos : getConPos()) {
				if(this.powerBuffer > 0) this.tryProvide(level, pos);
				this.trySubscribe(tanks[0].getTankType(), level, pos, pos.getDir());
				this.tryProvide(tanks[1], level, pos, pos.getDir());
			}

			networkPackNT(150);
		} else {
			this.lastRotor = this.rotor;

			if(this.turnProgress > 0) {
				double d = Mth.wrapDegrees(this.syncRotor - (double) this.rotor);
				this.rotor = (float) ((double) this.rotor + d / (double) this.turnProgress);
				--this.turnProgress;
			} else {
				this.rotor = this.syncRotor;
			}
		}
	}

	protected DirPos[] getConPos() {
		Direction dir = Direction.from3DDataValue(BlockDummyable.getMeta(getBlockState()) - BlockDummyable.offset);
		Direction rot = dir.getClockWise();
		BlockPos p = worldPosition.above();
		return new DirPos[] {
				new DirPos(p.relative(rot, 2), rot),
				new DirPos(p.relative(rot, 2).relative(dir), rot),
				new DirPos(p.relative(rot, 2).relative(dir.getOpposite()), rot)
		};
	}

	@Override
	public void serialize(ByteBuf buf) {
		tanks[0].serialize(buf);
		buf.writeLong(this.powerBuffer);
		buf.writeFloat(this.rotor);
		tanks[1].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		this.tanks[0].deserialize(buf);
		this.powerBuffer = buf.readLong();
		this.syncRotor = buf.readFloat();
		this.tanks[1].deserialize(buf);
		this.turnProgress = 3; //use 3-ply for extra smoothness
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.powerBuffer = nbt.getLong("powerBuffer");
		this.acceleration = nbt.getFloat("acceleration");
		this.tanks[0].readFromNBT(nbt, "s");
		this.tanks[1].readFromNBT(nbt, "w");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("powerBuffer", powerBuffer);
		nbt.putFloat("acceleration", acceleration);
		tanks[0].writeToNBT(nbt, "s");
		tanks[1].writeToNBT(nbt, "w");
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition).inflate(6);
	}

	@Override
	public boolean canConnect(Direction dir) {
		return dir.getAxis().isHorizontal();
	}

	@Override
	public long getPower() {
		return powerBuffer;
	}

	@Override
	public long getMaxPower() {
		return powerBuffer;
	}

	@Override
	public void setPower(long power) {
		this.powerBuffer = power;
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] {tanks[1]};
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {tanks[0]};
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}
}
