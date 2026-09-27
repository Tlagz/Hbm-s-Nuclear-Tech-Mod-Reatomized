package com.hbm.tileentity.machine;

import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerMicrowave;
import com.hbm.lib.Library;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;

import api.hbm.energymk2.IEnergyReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Microwave: smelts food (and only food) with power, at an adjustable speed. Turned all the way up it explodes.
 * Slots: 0 input, 1 output, 2 battery.
 *
 * TODO the original's ExplosionVNT (smooth entity damage, weapon SFX), OpenComputers
 */
public class TileEntityMicrowave extends TileEntityMachineBase implements IEnergyReceiverMK2, IControlReceiver, MenuProvider {

	public long power;
	public static final long maxPower = 50000;
	public static final int consumption = 50;
	public static final int maxTime = 300;
	public int time;
	public int speed;
	public static final int maxSpeed = 5;

	public TileEntityMicrowave(BlockPos pos, BlockState state) {
		super(ModTileEntities.MICROWAVE.get(), pos, state, 3);
	}

	@Override
	public String getName() {
		return "container.microwave";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			for(Direction dir : Direction.values()) this.trySubscribe(level, worldPosition.relative(dir), dir);

			this.power = Library.chargeTEFromItems(slots, 2, power, maxPower);

			if(canProcess()) {

				if(speed >= maxSpeed) {
					level.destroyBlock(worldPosition, false);
					level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 5F, Level.ExplosionInteraction.BLOCK);
					return;
				}

				if(time >= maxTime) {
					process();
					time = 0;
				}

				if(canProcess()) {
					power -= consumption;
					time += speed * 2;
				}
			}

			networkPackNT(50);
		}
	}

	private ItemStack getSmeltingResult(ItemStack stack) {
		if(stack.isEmpty() || level == null) return ItemStack.EMPTY;
		SingleRecipeInput input = new SingleRecipeInput(stack);
		return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, level).map(r -> r.value().assemble(input, level.registryAccess())).orElse(ItemStack.EMPTY);
	}

	private void process() {
		ItemStack stack = getSmeltingResult(slots.get(0)).copy();

		if(slots.get(1).isEmpty()) {
			slots.set(1, stack);
		} else {
			slots.get(1).grow(stack.getCount());
		}

		this.removeItem(0, 1);
		this.setChanged();
	}

	/** Only food, and only while there's power and the dial isn't at zero */
	private boolean canProcess() {

		if(speed == 0) return false;
		if(power < consumption) return false;

		ItemStack in = slots.get(0);
		ItemStack stack = getSmeltingResult(in);

		if(!stack.isEmpty()) {
			if(!in.has(DataComponents.FOOD) && !stack.has(DataComponents.FOOD)) return false;
			if(slots.get(1).isEmpty()) return true;
			if(!ItemStack.isSameItemSameComponents(stack, slots.get(1))) return false;
			return stack.getCount() + slots.get(1).getCount() <= stack.getMaxStackSize();
		}

		return false;
	}

	/** The speed buttons */
	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("up")) speed++;
		if(data.contains("down")) speed--;
		if(speed < 0) speed = 0;
		if(speed > maxSpeed) speed = maxSpeed;
		this.setChanged();
	}

	@Override public boolean hasPermission(Player player) { return this.stillValid(player); }

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(time);
		buf.writeInt(speed);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		time = buf.readInt();
		speed = buf.readInt();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return i == 0 && !getSmeltingResult(itemStack).isEmpty();
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i == 1;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return side == Direction.DOWN ? new int[] { 1 } : new int[] { 0 };
	}

	public long getPowerScaled(int i) {
		return (power * i) / maxPower;
	}

	public int getProgressScaled(int i) {
		return (time * i) / maxTime;
	}

	public int getSpeedScaled(int i) {
		return (speed * i) / maxSpeed;
	}

	@Override public void setPower(long i) { power = i; }
	@Override public long getPower() { return power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		power = nbt.getLong("power");
		speed = nbt.getInt("speed");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("power", power);
		nbt.putInt("speed", speed);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMicrowave(id, inv, this);
	}
}
