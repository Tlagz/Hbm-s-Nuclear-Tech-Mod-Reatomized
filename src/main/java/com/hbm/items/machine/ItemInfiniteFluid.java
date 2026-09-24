package com.hbm.items.machine;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.ModItems;

import net.minecraft.world.item.Item;

/** Creative fluid source/sink: fills (or drains) a tank by amount every time the machine processes the slot */
public class ItemInfiniteFluid extends Item {

	private final FluidType type;
	private final int amount;
	private final int chance;

	public ItemInfiniteFluid(Properties properties, FluidType type, int amount) {
		this(properties, type, amount, 1);
	}

	public ItemInfiniteFluid(Properties properties, FluidType type, int amount, int chance) {
		super(properties);
		this.type = type;
		this.amount = amount;
		this.chance = chance;
	}

	/** null means any fluid */
	public FluidType getType() { return this.type; }
	public int getAmount() { return this.amount; }
	public int getChance() { return this.chance; }
	public boolean allowPressure(int pressure) { return this == ModItems.fluid_barrel_infinite.get() || pressure == 0; }
}
