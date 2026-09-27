package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.container.ContainerMachineRadGen;
import com.hbm.items.ItemEnumMulti;
import com.hbm.items.ModItems;
import com.hbm.items.special.ItemWasteLong;
import com.hbm.items.special.ItemWasteShort;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;

import api.hbm.energymk2.IEnergyProviderMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Radiation-powered engine: twelve queues each "burning" one radioactive item at a time for a fixed duration and
 * power, the decayed item comes out in the matching output slot. Slots 0-11 inputs, 12-23 outputs.
 */
public class TileEntityMachineRadGen extends TileEntityMachineBase implements IEnergyProviderMK2, MenuProvider {

	public int[] progress = new int[12];
	public int[] maxProgress = new int[12];
	public int[] production = new int[12];
	public ItemStack[] processing = new ItemStack[12];
	protected int output;

	public long power;
	public static final long maxPower = 1000000;

	public boolean isOn = false;

	public TileEntityMachineRadGen(BlockPos pos, BlockState state) {
		super(ModTileEntities.RADGEN.get(), pos, state, 24);
		for(int i = 0; i < 12; i++) processing[i] = ItemStack.EMPTY;
	}

	@Override
	public String getName() {
		return "container.radGen";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.output = 0;

			Direction dir = BlockDummyable.getRotation(getBlockState());
			this.tryProvide(level, worldPosition.relative(dir, -4), dir.getOpposite());

			//check if reload necessary for any queues
			for(int i = 0; i < 12; i++) {

				ItemStack in = slots.get(i);
				ItemStack out = slots.get(i + 12);

				if(processing[i].isEmpty() && !in.isEmpty() && getDurationFromItem(in) > 0) {
					ItemStack result = getOutputFromItem(in);

					if(result.isEmpty() || out.isEmpty() || (ItemStack.isSameItemSameComponents(result, out) && result.getCount() + out.getCount() <= out.getMaxStackSize())) {
						progress[i] = 0;
						maxProgress[i] = this.getDurationFromItem(in);
						production[i] = this.getPowerFromItem(in);
						processing[i] = in.copyWithCount(1);
						this.removeItem(i, 1);
						this.setChanged();
					}
				}
			}

			this.isOn = false;

			for(int i = 0; i < 12; i++) {

				if(!processing[i].isEmpty()) {

					this.isOn = true;
					this.power += production[i];
					this.output += production[i];
					progress[i]++;

					if(progress[i] >= maxProgress[i]) {
						progress[i] = 0;
						ItemStack out = getOutputFromItem(processing[i]);

						if(!out.isEmpty()) {

							if(slots.get(i + 12).isEmpty()) {
								slots.set(i + 12, out);
							} else {
								slots.get(i + 12).grow(out.getCount());
							}
						}

						processing[i] = ItemStack.EMPTY;
						this.setChanged();
					}
				}
			}

			if(this.power > maxPower)
				this.power = maxPower;

			this.networkPackNT(50);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		for(int i = 0; i < 12; i++) {
			buf.writeInt(progress[i]);
			buf.writeInt(maxProgress[i]);
			buf.writeInt(production[i]);
		}
		buf.writeLong(this.power);
		buf.writeBoolean(this.isOn);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		for(int i = 0; i < 12; i++) {
			progress[i] = buf.readInt();
			maxProgress[i] = buf.readInt();
			production[i] = buf.readInt();
		}
		this.power = buf.readLong();
		this.isOn = buf.readBoolean();
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.power = nbt.getLong("power");
		this.isOn = nbt.getBoolean("isOn");

		int[] p = nbt.getIntArray("progress");
		if(p.length != 12) return;
		this.progress = p;
		this.maxProgress = nbt.getIntArray("maxProgress");
		this.production = nbt.getIntArray("production");

		for(int i = 0; i < 12; i++) processing[i] = ItemStack.EMPTY;
		ListTag list = nbt.getList("progressing", Tag.TAG_COMPOUND);
		for(int i = 0; i < list.size(); i++) {
			CompoundTag nbt1 = list.getCompound(i);
			byte b0 = nbt1.getByte("slot");
			if(b0 >= 0 && b0 < processing.length) {
				processing[b0] = ItemStack.parseOptional(registries, nbt1);
			}
		}
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putIntArray("progress", this.progress);
		nbt.putIntArray("maxProgress", this.maxProgress);
		nbt.putIntArray("production", this.production);
		nbt.putLong("power", this.power);
		nbt.putBoolean("isOn", this.isOn);

		ListTag list = new ListTag();
		for(int i = 0; i < processing.length; i++) {
			if(!processing[i].isEmpty()) {
				CompoundTag nbt1 = (CompoundTag) processing[i].save(registries, new CompoundTag());
				nbt1.putByte("slot", (byte) i);
				list.add(nbt1);
			}
		}
		nbt.put("progressing", list);
	}

	/** Only fuels, and a new stack only goes into a queue if there's no emptier queue of the same item */
	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {

		if(i >= 12 || getDurationFromItem(stack) <= 0)
			return false;

		if(slots.get(i).isEmpty())
			return true;

		int size = slots.get(i).getCount();

		for(int j = 0; j < 12; j++) {
			if(slots.get(j).isEmpty())
				return false;

			if(ItemStack.isSameItemSameComponents(slots.get(j), stack) && slots.get(j).getCount() < size)
				return false;
		}

		return true;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
				12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i >= 12;
	}

	/** Power per tick, duration in ticks and the decayed product (empty for none) */
	public record Fuel(int power, int duration, Supplier<ItemStack> output) { }

	private static Map<Item, Fuel> fuels;

	public static Map<Item, Fuel> getFuels() {
		if(fuels != null) return fuels;
		fuels = new HashMap<>();

		for(ItemWasteShort.WasteClass waste : ItemWasteShort.WasteClass.values()) {
			put(ModItems.nuclear_waste_short, waste, 1500, 30 * 60 * 20, ModItems.nuclear_waste_short_depleted);
			put(ModItems.nuclear_waste_short_tiny, waste, 150, 3 * 60 * 20, ModItems.nuclear_waste_short_depleted_tiny);
		}
		for(ItemWasteLong.WasteClass waste : ItemWasteLong.WasteClass.values()) {
			put(ModItems.nuclear_waste_long, waste, 500, 2 * 60 * 60 * 20, ModItems.nuclear_waste_long_depleted);
			put(ModItems.nuclear_waste_long_tiny, waste, 50, 12 * 60 * 20, ModItems.nuclear_waste_long_depleted_tiny);
		}

		fuels.put(ModItems.scrap_nuclear.get(), new Fuel(50, 5 * 60 * 20, () -> ItemStack.EMPTY));
		fuels.put(ModItems.gem_rad.get(), new Fuel(25_000, 30 * 60 * 20, () -> new ItemStack(Items.DIAMOND)));
		return fuels;
	}

	private static <E extends Enum<E>> void put(ItemEnumMulti.Variants<E> fuel, E waste, int power, int duration, ItemEnumMulti.Variants<E> depleted) {
		fuels.put(fuel.get(waste).get(), new Fuel(power, duration, () -> depleted.stack(waste)));
	}

	private static Fuel grabResult(ItemStack stack) {
		return stack.isEmpty() ? null : getFuels().get(stack.getItem());
	}

	private int getPowerFromItem(ItemStack stack) {
		Fuel result = grabResult(stack);
		return result == null ? 0 : result.power();
	}

	private int getDurationFromItem(ItemStack stack) {
		Fuel result = grabResult(stack);
		return result == null ? 0 : result.duration();
	}

	private ItemStack getOutputFromItem(ItemStack stack) {
		Fuel result = grabResult(stack);
		return result == null ? ItemStack.EMPTY : result.output().get();
	}

	@Override public long getPower() { return power; }
	@Override public long getMaxPower() { return maxPower; }
	@Override public void setPower(long i) { this.power = i; }

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition).inflate(6);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineRadGen(id, inv, this);
	}
}
