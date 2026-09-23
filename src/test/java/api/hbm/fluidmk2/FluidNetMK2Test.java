package api.hbm.fluidmk2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.uninos.UniNodespace;

class FluidNetMK2Test {

	static class TankMachine implements IFluidStandardSenderMK2, IFluidStandardReceiverMK2 {
		final FluidTank tank;
		final boolean sends;
		TankMachine(FluidType type, int fill, int max, int pressure, boolean sends) {
			this.tank = new FluidTank(type, max).withPressure(pressure);
			this.tank.setFill(fill);
			this.sends = sends;
		}
		@Override public FluidTank[] getSendingTanks() { return sends ? new FluidTank[] {tank} : new FluidTank[0]; }
		@Override public FluidTank[] getReceivingTanks() { return sends ? new FluidTank[0] : new FluidTank[] {tank}; }
		@Override public FluidTank[] getAllTanks() { return new FluidTank[] {tank}; }
		@Override public boolean isLoaded() { return true; }
	}

	@BeforeAll
	static void setup() {
		if(Fluids.WATER == null) Fluids.init();
	}

	@AfterEach
	void cleanup() {
		UniNodespace.clear();
	}

	@Test
	void registryIsStable() {
		assertEquals(0, Fluids.NONE.getID());
		assertSame(Fluids.WATER, Fluids.fromName("WATER"));
		assertSame(Fluids.WATER, Fluids.fromID(Fluids.WATER.getID()));
		assertEquals(155, Fluids.FLUE.getID());
		assertTrue(Fluids.getAll().length > 150, "expected the full fluid list, got " + Fluids.getAll().length);
		assertTrue(Fluids.GAS.hasTrait(FT_Flammable.class));
	}

	@Test
	void fluidMovesFromProviderToReceiver() {
		FluidNetMK2 net = new FluidNetMK2(Fluids.WATER);
		TankMachine src = new TankMachine(Fluids.WATER, 1000, 1000, 0, true);
		TankMachine dst = new TankMachine(Fluids.WATER, 0, 600, 0, false);
		net.addProvider(src);
		net.addReceiver(dst);

		net.update();

		assertEquals(600, dst.tank.getFill());
		assertEquals(400, src.tank.getFill());
	}

	@Test
	void pressureLevelsDoNotMix() {
		FluidNetMK2 net = new FluidNetMK2(Fluids.STEAM);
		TankMachine src = new TankMachine(Fluids.STEAM, 1000, 1000, 1, true);
		TankMachine unpressurized = new TankMachine(Fluids.STEAM, 0, 1000, 0, false);
		TankMachine pressurized = new TankMachine(Fluids.STEAM, 0, 300, 1, false);
		net.addProvider(src);
		net.addReceiver(unpressurized);
		net.addReceiver(pressurized);

		net.update();

		assertEquals(0, unpressurized.tank.getFill());
		assertEquals(300, pressurized.tank.getFill());
		assertEquals(700, src.tank.getFill());
	}
}
