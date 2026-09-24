package com.hbm.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.tileentity.IUpgradeInfoProvider;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Reads the upgrade slots of a machine into upgrade levels, recalculated only when the slots change */
public class UpgradeManagerNT {

	public BlockEntity owner;
	public List<ItemStack> cachedSlots;

	private UpgradeType mutexType;
	public Map<UpgradeType, Integer> upgrades = new HashMap<>();

	public UpgradeManagerNT(BlockEntity te) { this.owner = te; }

	/** Checks the slots start to end (inclusive) */
	public void checkSlots(List<ItemStack> slots, int start, int end) {

		if(!(owner instanceof IUpgradeInfoProvider upgradable) || slots == null)
			return;

		List<ItemStack> upgradeSlots = new ArrayList<>();
		for(int i = start; i <= end; i++) upgradeSlots.add(slots.get(i).copy());

		if(cachedSlots != null && ItemStack.listMatches(upgradeSlots, cachedSlots))
			return;

		cachedSlots = upgradeSlots;

		upgrades.clear();
		mutexType = null;

		for(ItemStack stack : upgradeSlots) {

			if(!stack.isEmpty() && stack.getItem() instanceof ItemMachineUpgrade item) {

				if(upgradable.getValidUpgrades() == null)
					return;

				if(upgradable.getValidUpgrades().containsKey(item.type)) { // Check if upgrade can even be accepted by the machine.
					if(item.type.mutex) {
						if(mutexType == null) {
							upgrades.put(item.type, 1);
							mutexType = item.type;
						} else if(item.type.ordinal() > mutexType.ordinal()) {
							upgrades.remove(mutexType);
							upgrades.put(item.type, 1);
							mutexType = item.type;
						}
					} else {

						Integer levelBefore = upgrades.get(item.type);
						int upgradeLevel = (levelBefore == null ? 0 : levelBefore);
						upgradeLevel += item.tier;
						// Add additional check to make sure it doesn't go over the max.
						upgrades.put(item.type, Math.min(upgradeLevel, upgradable.getValidUpgrades().get(item.type)));
					}
				}
			}
		}
	}

	public int getLevel(UpgradeType type) {
		return upgrades.getOrDefault(type, 0);
	}
}
