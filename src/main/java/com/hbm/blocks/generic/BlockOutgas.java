package com.hbm.blocks.generic;

/**
 * Ores that release gas (radon from uranium, asbestos dust, monoxide) when broken or randomly.
 *
 * TODO the gas blocks (gas_radon, gas_asbestos, gas_monoxide...) aren't ported yet, until then this is a plain ore.
 */
public class BlockOutgas extends BlockOre {

	boolean randomTick;
	int rate;
	boolean onBreak;
	boolean onNeighbour;

	public BlockOutgas(Properties properties) {
		super(properties);
	}

	@Override
	public BlockOutgas noFortune() {
		super.noFortune();
		return this;
	}

	@Override
	public BlockOutgas setRad(float rad) {
		super.setRad(rad);
		return this;
	}

	public BlockOutgas setOutgas(boolean randomTick, int rate, boolean onBreak) {
		return setOutgas(randomTick, rate, onBreak, false);
	}

	public BlockOutgas setOutgas(boolean randomTick, int rate, boolean onBreak, boolean onNeighbour) {
		this.randomTick = randomTick;
		this.rate = rate;
		this.onBreak = onBreak;
		this.onNeighbour = onNeighbour;
		return this;
	}
}
