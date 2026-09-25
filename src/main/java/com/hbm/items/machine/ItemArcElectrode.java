package com.hbm.items.machine;

import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ModDataComponents;

import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * Electric arc furnace electrodes, one item per type (ModItems.arc_electrode). The wear is counted up in a component
 * (the original's "durability" NBT), a fully worn electrode is replaced by its burnt variant (ModItems.arc_electrode_burnt).
 */
public class ItemArcElectrode extends ItemEnumMulti {

	public final EnumElectrodeType type;

	public ItemArcElectrode(Properties properties, String descriptionId, EnumElectrodeType type) {
		super(properties.stacksTo(1), descriptionId);
		this.type = type;
	}

	public static int getDurability(ItemStack stack) {
		return stack.getOrDefault(ModDataComponents.ELECTRODE_WEAR.get(), 0);
	}

	/** Wears the electrode down by one use, true once it's burnt out */
	public static boolean damage(ItemStack stack) {
		int durability = getDurability(stack) + 1;
		stack.set(ModDataComponents.ELECTRODE_WEAR.get(), durability);
		return durability >= getMaxDurability(stack);
	}

	public static int getMaxDurability(ItemStack stack) {
		return stack.getItem() instanceof ItemArcElectrode electrode ? electrode.type.durability : 1;
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return getDurability(stack) > 0;
	}

	/** The original's bar shrinks with the wear (getDurabilityForDisplay = wear / max) */
	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(13.0F - 13.0F * getDurability(stack) / getMaxDurability(stack));
	}

	@Override
	public int getBarColor(ItemStack stack) {
		float f = Math.max(0.0F, 1.0F - (float) getDurability(stack) / getMaxDurability(stack));
		return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
	}

	public static enum EnumElectrodeType {
		GRAPHITE(	10),
		LANTHANIUM(	100),
		DESH(		500),
		SATURNITE(	1500);

		public int durability;

		private EnumElectrodeType(int dura) {
			this.durability = dura;
		}
	}
}
