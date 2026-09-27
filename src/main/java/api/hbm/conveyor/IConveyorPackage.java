package api.hbm.conveyor;

import net.minecraft.world.item.ItemStack;

/** A box of items riding on a conveyor */
public interface IConveyorPackage {

	public ItemStack[] getItemStacks();
}
