package com.hbm.items.machine;

/**
 * Chicago pile rods, one item per type (ModItems.pile_rod).
 *
 *  _____
 * /     \
 * \_____/
 * |     |
 * |     |
 * |     |
 * |     |
 * \_____/
 *
 * Not terribly sophisticated,
 * it's a cylinder.
 */
public class ItemPileRodMK2 {

	public static final String KEY_NBT_DEPLETION = "depletion";

	public static enum EnumPileRod {
		/* 0 */ RA226BE(1D),
		/* 1 */ PO210BE(1D),
		/* 2 */ ZR(		0D,      0D, 0D,    2),
		/* 3 */ NU(		1D, 25_000D, 0.25D, 4),
		/* 4 */ PU239(	1D,    500D, 0.5D,  5),
		/* 5 */ RGP(	1D,  1_000D, 0.5D,  6),
		/* 6 */ WASTE(	1D,      0D, 1.5D,  6),
		/* 7 */ THORIUM(1D, 35_000D, 0.25D, 8),
		/* 8 */ THORIUM_FUEL(1D,  2_000D, 0.5D,  6);

		public double reactionMult = 1.0D;
		public double life = 1_000D;
		public double heatMult = 0.0D;
		public double neutronSource = 0D;
		public int turnsInto;

		private EnumPileRod(double neutronSource) {
			this.neutronSource = neutronSource;
			this.reactionMult = 0;
			this.life = 0;
			this.heatMult = 0;
		}

		private EnumPileRod(double reaction, double life, double heat, int turnsInto) {
			this.reactionMult = reaction;
			this.life = life;
			this.heatMult = heat;
			this.turnsInto = turnsInto;
		}
	}
}
