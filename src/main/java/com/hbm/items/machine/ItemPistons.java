package com.hbm.items.machine;

import java.util.List;

import com.hbm.inventory.fluid.trait.FT_Combustible.FuelGrade;
import com.hbm.items.ItemEnumMulti;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Piston sets for the combustion engine, each variant burns the fuel grades with a different efficiency */
public class ItemPistons extends ItemEnumMulti {

	public final EnumPistonType type;

	public ItemPistons(Properties properties, String descriptionId, EnumPistonType type) {
		super(properties, descriptionId);
		this.type = type;
	}

	/** The piston type of a stack, null if it isn't a piston set */
	public static EnumPistonType getType(ItemStack stack) {
		return stack.getItem() instanceof ItemPistons pistons ? pistons.type : null;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		list.add(Component.literal("Fuel efficiency:").withStyle(ChatFormatting.YELLOW));
		for(int i = 0; i < type.eff.length; i++) {
			list.add(Component.literal("-" + FuelGrade.values()[i].getLocalizedName() + ": ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal((int) (type.eff[i] * 100) + "%").withStyle(ChatFormatting.RED)));
		}
	}

	public static enum EnumPistonType {
		STEEL		(1.00, 0.75, 0.25, 0.00, 0.00),
		DURA		(0.50, 1.00, 0.90, 0.50, 0.00),
		DESH		(0.00, 0.50, 1.00, 0.75, 0.00),
		STARMETAL	(0.50, 0.75, 1.00, 0.90, 0.50);

		public double[] eff;

		private EnumPistonType(double... eff) {
			this.eff = new double[Math.min(FuelGrade.values().length, eff.length)];
			for(int i = 0; i < this.eff.length; i++) {
				this.eff[i] = eff[i];
			}
		}
	}
}
