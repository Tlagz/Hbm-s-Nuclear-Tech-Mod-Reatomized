package com.hbm.items.special;

import java.util.List;

import com.hbm.items.ItemEnumMulti;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Long-lived nuclear waste, one item per waste class. Like the original's ItemNuclearWaste, dropped stacks never
 * despawn and can't be destroyed (the original swapped in an invulnerable EntityItemWaste).
 */
public class ItemWasteLong extends ItemEnumMulti {

	private final WasteClass wasteClass;

	public ItemWasteLong(Properties properties, String descriptionId, WasteClass wasteClass) {
		super(properties, descriptionId);
		this.wasteClass = wasteClass;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		list.add(Component.literal(wasteClass.name).withStyle(ChatFormatting.ITALIC));
		super.appendHoverText(stack, context, list, flag);
	}

	@Override
	public boolean canBeHurtBy(ItemStack stack, DamageSource source) {
		return false;
	}

	@Override
	public int getEntityLifespan(ItemStack stack, Level level) {
		return Integer.MAX_VALUE;
	}

	public enum WasteClass {

		//all decayed versions include lead-types and classic nuclear waste
		URANIUM235("Uranium-235", 0, 0),	//plutonium 239 and 240, neptunium 237 / -
		URANIUM233("Uranium-233", 0, 50),	//uranium 235, plutonium 239, neptunium 237 / -
		NEPTUNIUM("Neptunium-237", 0, 100),	//plutonium 239 and uranium 238 / -
		THORIUM("Thorium-232", 0, 0),		//uranium 233 and uranium 235 / -
		SCHRABIDIUM("Schrabidium-326", 0, 250); //tantalum, neodymium, solinium, euphemium, ghiorsium-336 / -

		public final String name;
		public final int liquid;
		public final int gas;

		private WasteClass(String name, int liquid, int gas) {
			this.name = name;
			this.liquid = liquid;
			this.gas = gas;
		}
	}
}
