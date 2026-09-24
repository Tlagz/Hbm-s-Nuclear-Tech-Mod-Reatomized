package api.hbm.energymk2;

import com.hbm.items.ModDataComponents;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Items that store HE. The charge is a data component now (the original used the "charge" NBT tag). */
public interface IBatteryItem {

	public void chargeBattery(ItemStack stack, long i);
	public void setCharge(ItemStack stack, long i);
	public void dischargeBattery(ItemStack stack, long i);
	public long getCharge(ItemStack stack);
	public long getMaxCharge(ItemStack stack);
	public long getChargeRate(ItemStack stack);
	public long getDischargeRate(ItemStack stack);

	/** Returns an empty battery stack from the passed ItemStack, the original won't be modified */
	public static ItemStack emptyBattery(ItemStack stack) {
		if(!stack.isEmpty() && stack.getItem() instanceof IBatteryItem) {
			ItemStack stackOut = stack.copy();
			stackOut.set(ModDataComponents.CHARGE.get(), 0L);
			return stackOut;
		}
		return ItemStack.EMPTY;
	}

	/** Returns an empty battery stack from the passed Item */
	public static ItemStack emptyBattery(Item item) {
		return item instanceof IBatteryItem ? emptyBattery(new ItemStack(item)) : ItemStack.EMPTY;
	}
}
