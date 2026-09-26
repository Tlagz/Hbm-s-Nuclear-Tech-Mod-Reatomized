package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerCombustionEngine;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType;
import com.hbm.items.machine.ItemPistons;
import com.hbm.items.machine.ItemPistons.EnumPistonType;
import com.hbm.lib.Library;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachinePolluting;
import com.hbm.util.DirPos;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Combustion engine: burns combustible fuels with a piston set, 0-30 throttle (0.2mB/t per step), the piston set
 * decides the efficiency per fuel grade. Slots: 0/1 fluid container in/out, 2 piston set, 3 battery, 4 fluid identifier.
 *
 * TODO copy tool, OpenComputers, RoR values/functions
 */
public class TileEntityMachineCombustionEngine extends TileEntityMachinePolluting implements IEnergyProviderMK2, IFluidStandardTransceiverMK2, IControlReceiver, MenuProvider {

	public boolean isOn = false;
	public static long maxPower = 2_500_000;
	public long power;
	private int playersUsing = 0;
	public int setting = 0;
	public boolean wasOn = false;

	public float doorAngle = 0;
	public float prevDoorAngle = 0;

	private AudioWrapper audio;

	public FluidTank tank;
	public int tenth = 0;

	public TileEntityMachineCombustionEngine(BlockPos pos, BlockState state) {
		super(ModTileEntities.COMBUSTION_ENGINE.get(), pos, state, 5, 50);
		this.tank = new FluidTank(Fluids.DIESEL, 24_000);
	}

	@Override
	public String getName() {
		return "container.combustionEngine";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.tank.loadTank(0, 1, slots);
			if(this.tank.setType(4, slots)) {
				this.tenth = 0;
			}

			wasOn = false;

			int fill = tank.getFill() * 10 + tenth;
			EnumPistonType piston = ItemPistons.getType(slots.get(2));
			if(isOn && setting > 0 && piston != null && fill > 0 && tank.getTankType().hasTrait(FT_Combustible.class)) {
				FT_Combustible trait = tank.getTankType().getTrait(FT_Combustible.class);
				double eff = piston.eff[trait.getGrade().ordinal()];

				if(eff > 0) {
					int speed = setting * 2;

					int toBurn = Math.min(fill, speed);
					this.power += toBurn * (trait.getCombustionEnergy() / 10_000D) * eff;
					fill -= toBurn;

					if(level.getGameTime() % 5 == 0 && toBurn > 0) {
						super.pollute(tank.getTankType(), FluidReleaseType.BURN, toBurn * 0.5F);
					}

					if(toBurn > 0) {
						wasOn = true;
					}

					tank.setFill(fill / 10);
					tenth = fill % 10;
				}
			}

			this.power = Library.chargeItemsFromTE(slots, 3, power, power);

			this.autoPort(getConPos());

			if(power > maxPower)
				power = maxPower;

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

			if(wasOn) {

				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}

				audio.keepAlive();
				audio.updateVolume(this.getVolume(1F));

			} else {

				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	/** Two ports on the front and two on the back */
	private DirPos[] getConPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		return new DirPos[] {
				new DirPos(worldPosition.relative(dir).relative(rot), dir),
				new DirPos(worldPosition.relative(dir).relative(rot, -1), dir),
				new DirPos(worldPosition.relative(dir, -2).relative(rot), dir.getOpposite()),
				new DirPos(worldPosition.relative(dir, -2).relative(rot, -1), dir.getOpposite())
		};
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return AudioWrapper.getLoopedSound("hbm:block.igeneratorOperate", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 1.0F, 10F, 1.0F, 20);
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public boolean canConnect(Direction dir) {
		return dir != Direction.DOWN;
	}

	@Override
	public boolean canConnect(FluidType type, Direction dir) {
		return dir != Direction.DOWN;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(this.playersUsing);
		buf.writeInt(this.setting);
		buf.writeLong(this.power);
		buf.writeBoolean(this.isOn);
		buf.writeBoolean(this.wasOn);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.playersUsing = buf.readInt();
		this.setting = buf.readInt();
		this.power = buf.readLong();
		this.isOn = buf.readBoolean();
		this.wasOn = buf.readBoolean();
		tank.deserialize(buf);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.setting = nbt.getInt("setting");
		this.power = nbt.getLong("power");
		this.isOn = nbt.getBoolean("isOn");
		this.tank.readFromNBT(nbt, "tank");
		this.tenth = nbt.getInt("tenth");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putInt("setting", setting);
		nbt.putLong("power", power);
		nbt.putBoolean("isOn", isOn);
		tank.writeToNBT(nbt, "tank");
		nbt.putInt("tenth", tenth);
	}

	/** Server side, the hatch opens while someone has the GUI open */
	public void openInventory() {
		if(isServer()) this.playersUsing++;
	}

	public void closeInventory() {
		if(isServer()) this.playersUsing = Math.max(0, this.playersUsing - 1);
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] {tank};
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {tank};
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return this.getSmokeTanks();
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 3, worldPosition.getY(), worldPosition.getZ() - 3, worldPosition.getX() + 4, worldPosition.getY() + 2, worldPosition.getZ() + 4);
	}

	@Override
	public boolean hasPermission(Player player) {
		return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) < 25 * 25;
	}

	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("turnOn")) this.isOn = !this.isOn;
		if(data.contains("setting")) this.setting = Mth.clamp(data.getInt("setting"), 0, 30);

		this.markChanged();
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerCombustionEngine(id, inv, this);
	}
}
