package com.hbm.tileentity.machine;

import java.util.HashMap;

import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.container.ContainerMachineDiesel;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FT_Combustible.FuelGrade;
import com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachinePolluting;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Diesel generator: burns combustible fluids (not LOW grade) at 1mB/t, the HE per mB depends on the fuel grade.
 * Slots: 0/1 fuel containers in/out, 2 battery, 3 fluid identifier.
 *
 * TODO machine config file (IConfigurableMachine: powerCap, fuelCap, efficiency), EnergyControl info, copy/paste
 */
public class TileEntityMachineDiesel extends TileEntityMachinePolluting implements IEnergyProviderMK2, IFluidStandardTransceiverMK2, IControlReceiver, MenuProvider {

	public boolean isOn = false;
	public long power;
	public long powerCap = maxPower;
	public FluidTank tank;

	public boolean wasOn = false;
	private AudioWrapper audio;

	/* CONFIGURABLE CONSTANTS */
	public static long maxPower = 50_000;
	public static int fuelCap = 16_000;
	public static HashMap<FuelGrade, Double> fuelEfficiency = new HashMap<>();
	static {
		fuelEfficiency.put(FuelGrade.MEDIUM,	0.5D);
		fuelEfficiency.put(FuelGrade.HIGH,		0.75D);
		fuelEfficiency.put(FuelGrade.AERO,		0.1D);
	}

	private static final int[] slots_top = new int[] { 0 };
	private static final int[] slots_bottom = new int[] { 1, 2 };
	private static final int[] slots_side = new int[] { 2 };

	public TileEntityMachineDiesel(BlockPos pos, BlockState state) {
		super(ModTileEntities.DIESEL.get(), pos, state, 4, 100);
		tank = new FluidTank(Fluids.DIESEL, fuelCap);
	}

	@Override
	public String getName() {
		return "container.machineDiesel";
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		if(i == 0) return FluidContainerRegistry.getFluidContent(stack, tank.getTankType()) > 0;
		if(i == 2) return stack.getItem() instanceof IBatteryItem;
		return false;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.isOn = nbt.getBoolean("isOn");
		this.power = nbt.getLong("powerTime");
		this.powerCap = nbt.getLong("powerCap");
		if(this.powerCap <= 0) this.powerCap = maxPower;
		tank.readFromNBT(nbt, "fuel");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putBoolean("isOn", isOn);
		nbt.putLong("powerTime", power);
		nbt.putLong("powerCap", powerCap);
		tank.writeToNBT(nbt, "fuel");
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return side == Direction.DOWN ? slots_bottom : (side == Direction.UP ? slots_top : slots_side);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack stack, Direction side) {
		if(i == 1) return stack.is(ModItems.canister_empty.get()) || stack.is(ModItems.tank_steel.get());
		if(i == 2) return stack.getItem() instanceof IBatteryItem battery && battery.getCharge(stack) == battery.getMaxCharge(stack);
		return false;
	}

	public long getPowerScaled(long i) { return (power * i) / powerCap; }

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.wasOn = false;

			tank.setType(3, slots);
			tank.loadTank(0, 1, slots);

			this.autoPort(this.allAround());

			power = Library.chargeItemsFromTE(slots, 2, power, powerCap);
			if(isOn) generate();

			this.networkPackNT(50);
		} else {

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

	@Override
	public AudioWrapper createAudioLoop() {
		return AudioWrapper.getLoopedSound("hbm:block.engine", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 1.0F, 10F, 1.0F, 10);
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
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt((int) power);
		buf.writeInt((int) powerCap);
		buf.writeBoolean(isOn);
		buf.writeBoolean(wasOn);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readInt();
		this.powerCap = buf.readInt();
		this.isOn = buf.readBoolean();
		this.wasOn = buf.readBoolean();
		tank.deserialize(buf);
	}

	public boolean hasAcceptableFuel() { return getHEFromFuel() > 0; }
	public long getHEFromFuel() { return getHEFromFuel(tank.getTankType()); }

	public static long getHEFromFuel(FluidType type) {

		if(type.hasTrait(FT_Combustible.class)) {
			FT_Combustible fuel = type.getTrait(FT_Combustible.class);
			FuelGrade grade = fuel.getGrade();
			double efficiency = fuelEfficiency.containsKey(grade) ? fuelEfficiency.get(grade) : 0;

			if(fuel.getGrade() != FuelGrade.LOW) {
				return (long) (fuel.getCombustionEnergy() / 1000L * efficiency);
			}
		}

		return 0;
	}

	public void generate() {

		if(!this.isOn) return;
		if(this.level.hasNeighborSignal(worldPosition)) return;
		if(!hasAcceptableFuel()) return;
		if(tank.getFill() <= 0) return;

		this.wasOn = true;
		tank.setFill(tank.getFill() - 1);

		if(tank.getFill() < 0) tank.setFill(0);

		if(level.getGameTime() % 5 == 0) {
			super.pollute(tank.getTankType(), FluidReleaseType.BURN, 5F);
		}

		if(power + getHEFromFuel() <= powerCap) {
			power += getHEFromFuel();
		} else {
			power = powerCap;
		}
	}

	@Override
	public boolean hasPermission(Player player) {
		return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) < 25 * 25;
	}

	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("turnOn")) this.isOn = !this.isOn;
		this.markChanged();
	}

	@Override public long getPower() { return power; }
	@Override public void setPower(long i) { this.power = i; }
	@Override public long getMaxPower() { return maxPower; }

	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {tank}; }
	@Override public FluidTank[] getAllTanks() { return new FluidTank[] { tank }; }
	@Override public FluidTank[] getSendingTanks() { return this.getSmokeTanks(); }

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineDiesel(id, inv, this);
	}
}
