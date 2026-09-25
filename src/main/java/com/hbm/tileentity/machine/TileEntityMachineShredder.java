package com.hbm.tileentity.machine;

import com.hbm.inventory.container.ContainerMachineShredder;
import com.hbm.inventory.recipes.ShredderRecipes;
import com.hbm.items.machine.ItemBlades;
import com.hbm.lib.Library;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Shredder: grinds up to 9 input stacks at once into 18 output slots, one item per stack and cycle.
 * Needs two blades that wear down with every cycle. Slots: 0-8 inputs, 9-26 outputs, 27-28 blades, 29 battery.
 */
public class TileEntityMachineShredder extends TileEntityMachineBase implements IEnergyReceiverMK2, MenuProvider {

	public long power;
	public int progress;
	public int soundCycle = 0;
	public static final long maxPower = 10000;
	public static final int processingSpeed = 60;

	private static final int[] slots_io = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29 };

	public TileEntityMachineShredder(BlockPos pos, BlockState state) {
		super(ModTileEntities.SHREDDER.get(), pos, state, 30);
	}

	@Override
	public String getName() {
		return "container.machineShredder";
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		if(i < 9)
			return ShredderRecipes.getShredderResult(stack) != null && !(stack.getItem() instanceof ItemBlades);
		if(i == 29)
			return stack.getItem() instanceof IBatteryItem;
		if(i == 27 || i == 28)
			return stack.getItem() instanceof ItemBlades;

		return false;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.power = nbt.getLong("powerTime");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("powerTime", power);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return slots_io;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack itemStack, Direction side) {
		if((slot >= 9 && slot != 27 && slot != 28) || !this.isItemValidForSlot(slot, itemStack))
			return false;

		if(slots.get(slot).isEmpty())
			return true;

		int size = slots.get(slot).getCount();

		// spread inserted items over the input slots instead of filling one
		for(int k = 0; k < 9; k++) {
			if(slots.get(k).isEmpty())
				return false;

			if(ItemStack.isSameItemSameComponents(slots.get(k), itemStack) && slots.get(k).getCount() < size)
				return false;
		}

		return true;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		if(i >= 9 && i <= 26)
			return true;
		if(i >= 27 && i <= 28)
			return itemStack.getMaxDamage() > 0 && itemStack.getDamageValue() == itemStack.getMaxDamage();

		return false;
	}

	public int getDiFurnaceProgressScaled(int i) {
		return (progress * i) / processingSpeed;
	}

	public boolean hasPower() {
		return power > 0;
	}

	public boolean isProcessing() {
		return this.progress > 0;
	}

	@Override
	public void updateEntity() {
		boolean flag1 = false;

		if(isServer()) {

			this.updateConnections();

			if(this.progress == 0) this.soundCycle = 0;

			if(hasPower() && canProcess()) {
				progress++;

				power -= 5;

				if(this.progress == TileEntityMachineShredder.processingSpeed) {
					for(int i = 27; i <= 28; i++)
						if(slots.get(i).getMaxDamage() > 0)
							this.slots.get(i).setDamageValue(this.slots.get(i).getDamageValue() + 1);

					this.progress = 0;
					this.processItem();
					flag1 = true;
				}
				if(soundCycle == 0)
					level.playSound(null, worldPosition, SoundEvents.MINECART_RIDING, SoundSource.BLOCKS, getVolume(1.0F), 0.75F);
				soundCycle++;

				if(soundCycle >= 50)
					soundCycle = 0;
			} else {
				progress = 0;
			}

			power = Library.chargeTEFromItems(slots, 29, power, maxPower);

			networkPackNT(50);
		}

		if(flag1) {
			this.setChanged();
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		// the original synced the progress through the container
		buf.writeInt(progress);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		progress = buf.readInt();
	}

	private void updateConnections() {
		for(Direction dir : Direction.values())
			this.trySubscribe(level, worldPosition.relative(dir), dir);
	}

	public void processItem() {

		for(int inpSlot = 0; inpSlot < 9; inpSlot++) {
			if(!slots.get(inpSlot).isEmpty() && hasSpace(slots.get(inpSlot))) {
				ItemStack inp = slots.get(inpSlot);
				ItemStack outp = ShredderRecipes.getShredderResult(inp);

				boolean flag = false;

				for(int outSlot = 9; outSlot < 27; outSlot++) {
					ItemStack out = slots.get(outSlot);
					if(!out.isEmpty() && ItemStack.isSameItemSameComponents(out, outp) && out.getCount() + outp.getCount() <= outp.getMaxStackSize()) {

						out.grow(outp.getCount());
						inp.shrink(1);
						flag = true;
						break;
					}
				}

				if(!flag)
					for(int outSlot = 9; outSlot < 27; outSlot++) {
						if(slots.get(outSlot).isEmpty()) {
							slots.set(outSlot, outp.copy());
							inp.shrink(1);
							break;
						}
					}

				if(inp.isEmpty())
					slots.set(inpSlot, ItemStack.EMPTY);
			}
		}
	}

	public boolean canProcess() {
		if(!slots.get(27).isEmpty() && !slots.get(28).isEmpty() && this.getGearLeft() > 0 && this.getGearLeft() < 3 && this.getGearRight() > 0 && this.getGearRight() < 3) {

			for(int i = 0; i < 9; i++) {
				if(!slots.get(i).isEmpty() && hasSpace(slots.get(i))) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean hasSpace(ItemStack stack) {

		ItemStack result = ShredderRecipes.getShredderResult(stack);

		if(result != null)
			for(int i = 9; i < 27; i++) {
				ItemStack out = slots.get(i);
				if(out.isEmpty()) {
					return true;
				}

				if(out.getItem() == result.getItem() && out.getCount() + result.getCount() <= result.getMaxStackSize()) {
					return true;
				}
			}

		return false;
	}

	@Override
	public void setPower(long i) {
		this.power = i;
	}

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	@Override
	public long getPower() {
		return this.power;
	}

	@Override
	public long getMaxPower() {
		return TileEntityMachineShredder.maxPower;
	}

	/** 0 no blades, 1 fine, 2 half worn, 3 broken */
	public int getGearLeft() {
		return gearState(slots.get(27));
	}

	public int getGearRight() {
		return gearState(slots.get(28));
	}

	private static int gearState(ItemStack blades) {

		if(!blades.isEmpty() && blades.getItem() instanceof ItemBlades) {
			if(blades.getMaxDamage() == 0)
				return 1;

			if(blades.getDamageValue() < blades.getMaxDamage() / 2) {
				return 1;
			} else if(blades.getDamageValue() != blades.getMaxDamage()) {
				return 2;
			} else {
				return 3;
			}
		}

		return 0;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineShredder(id, inv, this);
	}
}
