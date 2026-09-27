package com.hbm.items.special;

import java.util.List;

import com.hbm.items.ItemEnumMulti;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Short-lived nuclear waste, one item per waste class (the original's metadata), all sharing a texture */
public class ItemWasteShort extends ItemEnumMulti {

	private final WasteClass wasteClass;

	public ItemWasteShort(Properties properties, String descriptionId, WasteClass wasteClass) {
		super(properties, descriptionId);
		this.wasteClass = wasteClass;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		list.add(Component.literal(wasteClass.name).withStyle(ChatFormatting.ITALIC));
		super.appendHoverText(stack, context, list, flag);
	}

	public enum WasteClass {

		//all decayed versions include lead-types and classic nuclear waste
		URANIUM235("Uranium-235", 0, 100),			//fresh recycling makes iodine, caesium and technetium, depleted turns into neptunium
		URANIUM233("Uranium-233", 50, 100),			//fresh recycling makes iodine, caesium and technetium, depleted turns into u235
		NEPTUNIUM("Neptunium-237", 150, 500),		//funny fission fragments + polonium and pu238 and 239 / u235
		PLUTONIUM239("Plutonium-239", 250, 1000),	//funny fission fragments + pu240 and 241 / u238 (actually u236 but fuck you)
		PLUTONIUM240("Plutonium-240", 350, 1000),	//funny fission fragments + pu241 / u238  + lead
		PLUTONIUM241("Plutonium-241", 500, 1000),	//funny fission fragments + am241 / 242 / np237 + bismuth
		AMERICIUM242("Americium-242", 750, 1000),	//funny fission fragments + californium / np237 + pu241
		SCHRABIDIUM("Schrabidium-326", 1000, 1000); //funniest fission fragments

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
