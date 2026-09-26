package com.hbm.tileentity.machine;

import java.util.HashMap;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerMachineTurbineGas;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FT_Combustible.FuelGrade;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.main.ModSounds;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Gas turbine: burns gaseous fuels with lubricant, a start/stop sequence spins it up to idle, the throttle slider (or
 * the auto mode following the power buffer) sets the output. The exhaust heat boils water into hot steam.
 * Slots: 0 battery, 1 fluid identifier (gas fuels only).
 *
 * TODO copy tool, OpenComputers, RoR
 */
public class TileEntityMachineTurbineGas extends TileEntityMachineBase implements IFluidStandardTransceiverMK2, IEnergyProviderMK2, IControlReceiver, MenuProvider {

	public long power;
	public static final long maxPower = 1000000L;

	public int rpm; //0-100, crescent moon gauge, used for calculating the amount of power generated, starts past 10%
	public int temp; //0-800, used for figuring out how much water to boil, starts boiling at 300°C
	public int rpmIdle = 10;
	public int tempIdle = 300;

	public int powerSliderPos; //goes from 0 to 60, 0 is idle, 60 is max power
	public int throttle; //the same thing, but goes from 0 to 100

	public boolean autoMode;
	public int state = 0; //0 is offline, -1 is startup, 1 is online

	public int counter = 0; //used to startup and shutdown
	public int instantPowerOutput;
	public double waterToBoil;

	public FluidTank[] tanks;

	private AudioWrapper audio;

	public static HashMap<FluidType, Double> fuelMaxCons = new HashMap<>(); //fuel consumption per tick at max power

	public static void registerFuels() {
		fuelMaxCons.clear();
		fuelMaxCons.put(Fluids.GAS, 50D);			// natgas doesn't burn well so it burns faster to compensate
		fuelMaxCons.put(Fluids.SYNGAS, 10D);		// syngas just fucks
		fuelMaxCons.put(Fluids.OXYHYDROGEN, 100D);	// oxyhydrogen is terrible so it needs to burn a ton for the bare minimum
		fuelMaxCons.put(Fluids.REFORMGAS, 5D);		// fuck it we ball
	}

	public TileEntityMachineTurbineGas(BlockPos pos, BlockState state) {
		super(ModTileEntities.TURBINE_GAS.get(), pos, state, 2);
		this.tanks = new FluidTank[4];
		tanks[0] = new FluidTank(Fluids.GAS, 100000);
		tanks[1] = new FluidTank(Fluids.LUBRICANT, 16000);
		tanks[2] = new FluidTank(Fluids.WATER, 16000);
		tanks[3] = new FluidTank(Fluids.HOTSTEAM, 160000);
	}

	private long powerBeforeNet;

	@Override
	public void updateEntity() {

		if(isServer()) {

			waterToBoil = 0; //reset

			throttle = powerSliderPos * 100 / 60;

			if(slots.get(1).getItem() instanceof IItemFluidIdentifier id) {
				FluidType fluid = id.getType(level, worldPosition, slots.get(1));
				if(fluid.hasTrait(FT_Combustible.class) && fluid.getTrait(FT_Combustible.class).getGrade() == FuelGrade.GAS) {
					tanks[0].setTankType(fluid);
				}
			}

			if(autoMode) { //power production depending on power requirement and fuel level

				int powerSliderTarget;

				if(tanks[0].getFill() * 10 > tanks[0].getMaxFill()) {
					powerSliderTarget = 60 - (int) (60 * power / maxPower); //scales the slider proportionally to the power gauge
				} else {
					powerSliderTarget = (int) (tanks[0].getFill() * 0.0001 * (60 - (int) (60 * power / maxPower)));
				}

				if(powerSliderTarget > powerSliderPos) { //makes the auto slider slide instead of snapping into position
					powerSliderPos++;
				} else if(powerSliderTarget < powerSliderPos) {
					powerSliderPos--;
				}
			}

			switch(state) { //what to do when turbine offline, starting up and online
			case 0: shutdown(); break;
			case -1: stopIfNotReady(); startup(); break;
			case 1: stopIfNotReady(); run(); break;
			default: break;
			}

			Direction dir = BlockDummyable.getRotation(getBlockState());
			Direction rot = dir.getClockWise();

			powerBeforeNet = Math.min(this.power, maxPower);
			power = Library.chargeItemsFromTE(slots, 0, power, maxPower);
			this.tryProvide(level, worldPosition.relative(rot, 5).above(), rot); //sends out power

			if(this.power > maxPower) this.power = maxPower;

			for(int i = 0; i < 2; i++) { //fuel and lube
				this.trySubscribe(tanks[i].getTankType(), level, worldPosition.relative(dir, -2).relative(rot), dir.getOpposite());
				this.trySubscribe(tanks[i].getTankType(), level, worldPosition.relative(dir, 2).relative(rot), dir);
			}

			//water
			this.trySubscribe(tanks[2].getTankType(), level, worldPosition.relative(dir, -2).relative(rot, -4), dir.getOpposite());
			this.trySubscribe(tanks[2].getTankType(), level, worldPosition.relative(dir, 2).relative(rot, -4), dir);

			if(tanks[3].getFill() > 0) this.tryProvide(tanks[3], level, worldPosition.relative(rot, -6).above(), rot.getOpposite());

			this.networkPackNT(150);

		} else { //client side, for sounds n shit

			if(rpm >= 10 && state != -1) { //if conditions are right, play the sound

				if(audio == null) { //if there is no sound playing, start it
					audio = AudioWrapper.getLoopedSound("hbm:block.turbinegasRunning", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), getVolume(1.0F), 20F, 2.0F, 20);
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio.stopSound();
					audio = AudioWrapper.getLoopedSound("hbm:block.turbinegasRunning", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), getVolume(1.0F), 20F, 2.0F, 20);
					audio.startSound();
				}

				audio.updatePitch((float) (0.55 + 0.1 * rpm / 10)); //dynamic pitch update based on rpm
				audio.updateVolume(getVolume(2F));
				audio.keepAlive();

			} else {

				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(this.powerBeforeNet);
		buf.writeInt(this.rpm);
		buf.writeInt(this.temp);
		buf.writeInt(this.state);
		buf.writeBoolean(this.autoMode);
		buf.writeInt(this.throttle);
		buf.writeInt(this.powerSliderPos);

		if(state != 1) {
			buf.writeInt(this.counter); //sent during startup and shutdown
		} else {
			buf.writeInt(this.instantPowerOutput); //sent while running
		}

		for(int i = 0; i < 4; i++) tanks[i].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.rpm = buf.readInt();
		this.temp = buf.readInt();
		this.state = buf.readInt();
		this.autoMode = buf.readBoolean();
		this.throttle = buf.readInt();
		this.powerSliderPos = buf.readInt();

		if(state != 1)
			this.counter = buf.readInt();
		else
			this.instantPowerOutput = buf.readInt(); //state 1

		for(int i = 0; i < 4; i++) tanks[i].deserialize(buf);
	}

	private void stopIfNotReady() {
		if(tanks[0].getFill() == 0 || tanks[1].getFill() == 0) state = 0;
		if(!hasAcceptableFuel()) state = 0;
	}

	public boolean hasAcceptableFuel() {
		if(tanks[0].getTankType().hasTrait(FT_Combustible.class)) {
			return tanks[0].getTankType().getTrait(FT_Combustible.class).getGrade() == FuelGrade.GAS;
		}
		return false;
	}

	private void startup() {

		counter++;

		if(counter <= 20) //rpm gauge 0-100-0
			rpm = 5 * counter;
		else if(counter > 20 && counter <= 40)
			rpm = 100 - 5 * (counter - 20);
		else if(counter > 50) {
			rpm = (int) (rpmIdle * (counter - 50) / 530); //slowly ramps up temp and RPM
			temp = (int) (tempIdle * (counter - 50) / 530);
		}

		if(counter == 50) {
			level.playSound(null, worldPosition.getX(), worldPosition.getY() + 2, worldPosition.getZ(), ModSounds.get("block.turbinegasStartup"), SoundSource.BLOCKS, getVolume(1.0F), 1.0F);
		}

		if(counter == 580) {
			counter = 225; // ensures it shuts down properly when done immediately after startup
			state = 1;
		}
	}

	int rpmLast; //used to progressively slow down and cool the turbine without immediatly setting rpm and temp to 0
	int tempLast;

	private void shutdown() {

		autoMode = false;
		instantPowerOutput = 0;

		if(powerSliderPos > 0)
			powerSliderPos--;

		if(rpm <= 10 && counter > 0) {

			if(counter == 225) {
				level.playSound(null, worldPosition.getX(), worldPosition.getY() + 2, worldPosition.getZ(), ModSounds.get("block.turbinegasShutdown"), SoundSource.BLOCKS, getVolume(1.0F), 1.0F);
				rpmLast = rpm;
				tempLast = temp;
			}

			counter--;

			rpm = (int) (rpmLast * (counter) / 225);
			temp = (int) (tempLast * (counter) / 225);

		} else if(rpm > 11) { //quickly slows down the turbine to idle before shutdown
			counter = 42069; //absolutely necessary to avoid fuckeries on shutdown
			rpm--;
		} else if(rpm == 11) {
			counter = 225;
			rpm--;
		}
	}

	/** Dynamically calculates a (hopefully) sensible burn heat from the combustion energy, scales from 300°C - 800°C */
	protected int getFluidBurnTemp(FluidType type) {
		double dFuel = type.hasTrait(FT_Combustible.class) ? type.getTrait(FT_Combustible.class).getCombustionEnergy() : 0;
		return (int) Math.floor(800D - (Math.pow(Math.E, -dFuel / 100_000D)) * 300D);
	}

	private void run() {

		if((int) (throttle * 0.9) > rpm - rpmIdle) { //simulates the rotor's moment of inertia
			if(level.getGameTime() % 5 == 0) rpm++;
		} else if((int) (throttle * 0.9) < rpm - rpmIdle) {
			if(level.getGameTime() % 2 == 0) rpm--;
		}

		int maxTemp = getFluidBurnTemp(tanks[0].getTankType());

		if(throttle * 5 * (maxTemp - tempIdle) / 500 > temp - tempIdle) { //simulates the heat exchanger's resistance to temperature variation
			if(level.getGameTime() % 2 == 0) temp++;
		} else if(throttle * 5 * (maxTemp - tempIdle) / 500 < temp - tempIdle) {
			if(level.getGameTime() % 2 == 0) temp--;
		}

		double consumption = getMaxConsumption(tanks[0].getTankType());

		if(level.getGameTime() % 20 == 0 && tanks[0].getTankType() != Fluids.OXYHYDROGEN) PollutionHandler.incrementPollution(level, worldPosition, PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * 3);

		makePower(consumption, throttle);
	}

	public static double getMaxConsumption(FluidType type) {
		return fuelMaxCons.containsKey(type) ? fuelMaxCons.get(type) : 5D;
	}

	double fuelToConsume; //used to consume 1 mb of fuel at a time when consumption is <1 mb/tick

	private void makePower(double consMax, int throttle) {

		double idleConsumption = consMax * 0.05D;
		double consumption = idleConsumption + consMax * throttle / 100;

		fuelToConsume += consumption;

		tanks[0].setFill(tanks[0].getFill() - (int) Math.floor(fuelToConsume));
		fuelToConsume -= (int) Math.floor(fuelToConsume);

		if(level.getGameTime() % 10 == 0) //lube consumption
			tanks[1].setFill(tanks[1].getFill() - 1);

		if(tanks[0].getFill() < 0) { //avoids negative amounts of fluid
			tanks[0].setFill(0);
			state = 0;
		}
		if(tanks[1].getFill() < 0) {
			tanks[1].setFill(0);
			state = 0;
		}

		long energy = 0; //energy per mb of fuel

		if(tanks[0].getTankType().hasTrait(FT_Combustible.class)) {
			energy = tanks[0].getTankType().getTrait(FT_Combustible.class).getCombustionEnergy() / 1000L;
		}

		int rpmEff = rpm - rpmIdle; // RPM above idle level, 0-90

		//this shit avoids power rising in steps of 2000 or so HE at a time, instead it does it smoothly
		if(instantPowerOutput < (consMax * energy * rpmEff / 90)) {
			instantPowerOutput += Math.random() * 0.005 * consMax * energy;
			if(instantPowerOutput > (consMax * energy * rpmEff / 90))
				instantPowerOutput = (int) (consMax * energy * rpmEff / 90);
		} else if(instantPowerOutput > (consMax * energy * rpmEff / 90)) {
			instantPowerOutput -= Math.random() * 0.011 * consMax * energy;
			if(instantPowerOutput < (consMax * energy * rpmEff / 90))
				instantPowerOutput = (int) (consMax * energy * rpmEff / 90);
		}

		this.power += instantPowerOutput;

		double waterPerTick = (consMax * energy * (temp - tempIdle) / 220000); //it just works fuck you
		this.waterToBoil = waterPerTick;

		int heatCycles = (int) Math.floor(waterToBoil);
		int waterCycles = tanks[2].getFill();
		int steamCycles = (tanks[3].getMaxFill() - tanks[3].getFill()) / 10;
		int cycles = BobMathUtil.min(heatCycles, waterCycles, steamCycles);

		tanks[2].setFill(tanks[2].getFill() - cycles);
		tanks[3].setFill(tanks[3].getFill() + cycles * 10);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.tanks[0].readFromNBT(nbt, "gas");
		this.tanks[1].readFromNBT(nbt, "lube");
		this.tanks[2].readFromNBT(nbt, "water");
		this.tanks[3].readFromNBT(nbt, "densesteam");
		this.autoMode = nbt.getBoolean("automode");
		this.power = nbt.getLong("power");
		this.state = nbt.getInt("state");
		this.rpm = nbt.getInt("rpm");
		this.temp = nbt.getInt("temperature");
		this.powerSliderPos = nbt.getInt("slidPos");
		this.instantPowerOutput = nbt.getInt("instPwr");
		this.counter = nbt.getInt("counter");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		tanks[0].writeToNBT(nbt, "gas");
		tanks[1].writeToNBT(nbt, "lube");
		tanks[2].writeToNBT(nbt, "water");
		tanks[3].writeToNBT(nbt, "densesteam");
		nbt.putBoolean("automode", autoMode);
		nbt.putLong("power", power);

		// a running turbine keeps running, one that was starting up or shutting down is saved as cold
		if(state == 1) {
			nbt.putInt("state", this.state);
			nbt.putInt("rpm", this.rpm);
			nbt.putInt("temperature", this.temp);
			nbt.putInt("slidPos", this.powerSliderPos);
			nbt.putInt("instPwr", instantPowerOutput);
			nbt.putInt("counter", 225);
		} else {
			nbt.putInt("state", 0);
			nbt.putInt("rpm", 0);
			nbt.putInt("temperature", 20);
			nbt.putInt("slidPos", 0);
			nbt.putInt("instPwr", 0);
			nbt.putInt("counter", 0);
		}
	}

	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("slidPos")) powerSliderPos = (int) data.getDouble("slidPos");
		if(data.contains("autoMode")) autoMode = data.getBoolean("autoMode");
		if(data.contains("state")) state = data.getInt("state");
		this.setChanged();
	}

	@Override
	public boolean hasPermission(Player player) {
		return player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) < 25 * 25;
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

	@Override public void setPower(long power) { this.power = power; }
	@Override public long getPower() { return this.power; }
	@Override public long getMaxPower() { return maxPower; }

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 5, worldPosition.getY(), worldPosition.getZ() - 5, worldPosition.getX() + 6, worldPosition.getY() + 3, worldPosition.getZ() + 6);
	}

	@Override
	public String getName() {
		return "container.turbinegas";
	}

	@Override public FluidTank[] getAllTanks() { return tanks; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { tanks[0], tanks[1], tanks[2] }; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] { tanks[3] }; }

	@Override
	public boolean canConnect(Direction dir) {
		return dir != Direction.DOWN;
	}

	@Override
	public boolean canConnect(FluidType type, Direction dir) {
		return dir != Direction.DOWN;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineTurbineGas(id, inv, this);
	}
}
