package api.hbm.energymk2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.hbm.uninos.UniNodespace;

class PowerNetMK2Test {

	static class Battery implements IEnergyProviderMK2, IEnergyReceiverMK2 {
		long power, max, speed = Long.MAX_VALUE;
		ConnectionPriority priority = ConnectionPriority.NORMAL;
		Battery(long power, long max) { this.power = power; this.max = max; }
		@Override public long getPower() { return power; }
		@Override public void setPower(long power) { this.power = power; }
		@Override public long getMaxPower() { return max; }
		@Override public boolean isLoaded() { return true; }
		@Override public long getProviderSpeed() { return Math.min(speed, max); }
		@Override public long getReceiverSpeed() { return Math.min(speed, max); }
		@Override public ConnectionPriority getPriority() { return priority; }
	}

	@AfterEach
	void cleanup() {
		UniNodespace.clear();
	}

	@Test
	void energyIsConserved() {
		PowerNetMK2 net = new PowerNetMK2();
		Battery a = new Battery(1000, 1000), b = new Battery(333, 1000);
		Battery r1 = new Battery(0, 400), r2 = new Battery(0, 700);
		net.addProvider(a); net.addProvider(b);
		net.addReceiver(r1); net.addReceiver(r2);

		net.update();

		assertEquals(1333, a.power + b.power + r1.power + r2.power);
		assertEquals(1100, r1.power + r2.power); // demand (1100) < supply (1333), everything gets filled
		assertEquals(1100, net.energyTracker);
	}

	@Test
	void supplyIsSplitProportionallyToDemand() {
		PowerNetMK2 net = new PowerNetMK2();
		Battery src = new Battery(300, 300);
		Battery small = new Battery(0, 100), big = new Battery(0, 500);
		net.addProvider(src);
		net.addReceiver(small); net.addReceiver(big);

		net.update();

		assertEquals(50, small.power);  // 300 * 100/600
		assertEquals(250, big.power);   // 300 * 500/600
		assertEquals(0, src.power);
	}

	@Test
	void higherPriorityIsServedFirst() {
		PowerNetMK2 net = new PowerNetMK2();
		Battery src = new Battery(500, 500);
		Battery low = new Battery(0, 1000), high = new Battery(0, 400);
		high.priority = IEnergyReceiverMK2.ConnectionPriority.HIGH;
		net.addProvider(src);
		net.addReceiver(low); net.addReceiver(high);

		net.update();

		assertEquals(400, high.power);
		assertEquals(100, low.power);
		assertEquals(0, src.power);
	}

	@Test
	void providerSpeedLimitsTransfer() {
		PowerNetMK2 net = new PowerNetMK2();
		Battery src = new Battery(1000, 1000);
		src.speed = 10;
		Battery rec = new Battery(0, 1000);
		net.addProvider(src);
		net.addReceiver(rec);

		net.update();

		assertEquals(10, rec.power);
		assertEquals(990, src.power);
	}

	@Test
	void diodeReturnsLeftover() {
		PowerNetMK2 net = new PowerNetMK2();
		Battery rec = new Battery(0, 100);
		net.addReceiver(rec);

		long leftover = net.sendPowerDiode(250);

		assertEquals(100, rec.power);
		assertEquals(150, leftover);
	}
}
