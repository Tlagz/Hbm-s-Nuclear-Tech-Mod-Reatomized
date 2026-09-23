package api.hbm.energymk2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.hbm.uninos.NodeNet;

import api.hbm.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import api.hbm.energymk2.Nodespace.PowerNode;

/**
 * Technically MK3 since it's now UNINOS compatible, although UNINOS was build out of 95% nodespace code
 *
 * @author hbm
 */
public class PowerNetMK2 extends NodeNet<IEnergyReceiverMK2, IEnergyProviderMK2, PowerNode> {

	private record Share<T>(T handler, long amount) { }

	public long energyTracker = 0L;

	protected static int timeout = 3_000;

	@Override public void resetTrackers() { this.energyTracker = 0; }

	@Override
	public void update() {

		if(providerEntries.isEmpty()) return;
		if(receiverEntries.isEmpty()) return;

		long timestamp = System.currentTimeMillis();

		List<Share<IEnergyProviderMK2>> providers = new ArrayList<>();
		long powerAvailable = 0;

		// sum up available power
		Iterator<Entry<IEnergyProviderMK2, Long>> provIt = providerEntries.entrySet().iterator();
		while(provIt.hasNext()) {
			Entry<IEnergyProviderMK2, Long> entry = provIt.next();
			if(timestamp - entry.getValue() > timeout || isBadLink(entry.getKey())) { provIt.remove(); continue; }
			long src = Math.min(entry.getKey().getPower(), entry.getKey().getProviderSpeed());
			if(src > 0) {
				providers.add(new Share<>(entry.getKey(), src));
				powerAvailable += src;
			}
		}

		// sum up total demand, categorized by priority
		List<Share<IEnergyReceiverMK2>>[] receivers = newPriorityLists();
		long[] demand = new long[ConnectionPriority.values().length];
		long totalDemand = 0;

		Iterator<Entry<IEnergyReceiverMK2, Long>> recIt = receiverEntries.entrySet().iterator();

		while(recIt.hasNext()) {
			Entry<IEnergyReceiverMK2, Long> entry = recIt.next();
			if(timestamp - entry.getValue() > timeout || isBadLink(entry.getKey())) { recIt.remove(); continue; }
			long rec = Math.min(entry.getKey().getMaxPower() - entry.getKey().getPower(), entry.getKey().getReceiverSpeed());
			if(rec > 0) {
				int p = entry.getKey().getPriority().ordinal();
				receivers[p].add(new Share<>(entry.getKey(), rec));
				demand[p] += rec;
				totalDemand += rec;
			}
		}

		long toTransfer = Math.min(powerAvailable, totalDemand);
		long energyUsed = 0;

		// add power to receivers, ordered by priority
		for(int i = ConnectionPriority.values().length - 1; i >= 0; i--) {
			List<Share<IEnergyReceiverMK2>> list = receivers[i];
			long priorityDemand = demand[i];

			for(Share<IEnergyReceiverMK2> entry : list) {
				double weight = (double) entry.amount() / (double) (priorityDemand);
				long toSend = (long) Math.min(Math.max(toTransfer * weight, 0D), entry.amount());
				energyUsed += (toSend - entry.handler().transferPower(toSend)); //leftovers are subtracted from the intended amount to use up
			}

			toTransfer -= energyUsed;
		}

		this.energyTracker += energyUsed;
		long leftover = energyUsed;

		// remove power from providers
		for(Share<IEnergyProviderMK2> entry : providers) {
			double weight = (double) entry.amount() / (double) powerAvailable;
			long toUse = (long) Math.max(energyUsed * weight, 0D);
			entry.handler().usePower(toUse);
			leftover -= toUse;
		}

		// rounding error compensation, detects surplus that hasn't been used and removes it from random providers
		int iterationsLeft = 100; // whiles without emergency brakes are a bad idea
		while(iterationsLeft > 0 && leftover > 0 && providers.size() > 0) {
			iterationsLeft--;

			IEnergyProviderMK2 scapegoat = providers.get(rand.nextInt(providers.size())).handler();

			long toUse = Math.min(leftover, scapegoat.getPower());
			scapegoat.usePower(toUse);
			leftover -= toUse;
		}
	}

	public long sendPowerDiode(long power) {

		if(receiverEntries.isEmpty()) return power;

		long timestamp = System.currentTimeMillis();

		List<Share<IEnergyReceiverMK2>>[] receivers = newPriorityLists();
		long[] demand = new long[ConnectionPriority.values().length];
		long totalDemand = 0;

		Iterator<Entry<IEnergyReceiverMK2, Long>> recIt = receiverEntries.entrySet().iterator();

		while(recIt.hasNext()) {
			Entry<IEnergyReceiverMK2, Long> entry = recIt.next();
			if(timestamp - entry.getValue() > timeout) { recIt.remove(); continue; }
			long rec = Math.min(entry.getKey().getMaxPower() - entry.getKey().getPower(), entry.getKey().getReceiverSpeed());
			int p = entry.getKey().getPriority().ordinal();
			receivers[p].add(new Share<>(entry.getKey(), rec));
			demand[p] += rec;
			totalDemand += rec;
		}

		long toTransfer = Math.min(power, totalDemand);
		long energyUsed = 0;

		for(int i = ConnectionPriority.values().length - 1; i >= 0; i--) {
			List<Share<IEnergyReceiverMK2>> list = receivers[i];
			long priorityDemand = demand[i];

			for(Share<IEnergyReceiverMK2> entry : list) {
				double weight = (double) entry.amount() / (double) (priorityDemand);
				long toSend = (long) Math.max(toTransfer * weight, 0D);
				energyUsed += (toSend - entry.handler().transferPower(toSend)); //leftovers are subtracted from the intended amount to use up
			}

			toTransfer -= energyUsed;
		}

		this.energyTracker += energyUsed;

		return power - energyUsed;
	}

	@SuppressWarnings("unchecked")
	private static List<Share<IEnergyReceiverMK2>>[] newPriorityLists() {
		List<Share<IEnergyReceiverMK2>>[] lists = new List[ConnectionPriority.values().length];
		for(int i = 0; i < lists.length; i++) lists[i] = new ArrayList<>();
		return lists;
	}
}
