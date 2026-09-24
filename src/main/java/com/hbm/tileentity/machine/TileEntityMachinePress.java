package com.hbm.tileentity.machine;

import com.hbm.inventory.container.ContainerMachinePress;
import com.hbm.inventory.recipes.PressRecipes;
import com.hbm.items.machine.ItemStamp;
import com.hbm.lib.RefStrings;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Burner press: burns furnace fuel (200 ticks of burn time per operation) and presses the input with the stamp.
 * The press speeds up while working (4x as fast next to a press preheater).
 * Slots: 0 fuel, 1 stamp, 2 input, 3 output, 4-12 stamp storage.
 */
public class TileEntityMachinePress extends TileEntityMachineBase implements MenuProvider {

	public int speed = 0; // speed ticks up once (or four times if preheated) when operating
	public static final int maxSpeed = 400; // max speed ticks for acceleration
	public static final int progressAtMax = 25; // max progress speed when hot
	public int burnTime = 0; // burn ticks of the loaded fuel, 200 ticks equal one operation

	public int press; // extension of the press, operation is completed if maxPress is reached
	public double renderPress; // client-side version of the press var, a double for smoother rendering
	public double lastPress; // for interp
	private int syncPress; // for interp
	private int turnProgress; // for interp 3: revenge of the sith
	public final static int maxPress = 200; // max tick count per operation assuming speed is 1
	boolean isRetracting = false; // direction the press is currently going
	private int delay; // delay between direction changes to look a bit more appealing

	public ItemStack syncStack = ItemStack.EMPTY;

	public TileEntityMachinePress(BlockPos pos, BlockState state) {
		super(ModTileEntities.PRESS.get(), pos, state, 13);
	}

	@Override
	public String getName() {
		return "container.press";
	}

	private static int getBurnTime(ItemStack stack) {
		return stack.isEmpty() ? 0 : stack.getBurnTime(RecipeType.SMELTING);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			boolean preheated = false;
			Block preheater = BuiltInRegistries.BLOCK.get(RefStrings.loc("press_preheater"));

			for(Direction dir : Direction.values()) {
				if(level.getBlockState(worldPosition.relative(dir)).is(preheater) && preheater != net.minecraft.world.level.block.Blocks.AIR) {
					preheated = true;
					break;
				}
			}

			boolean canProcess = this.canProcess();

			if((canProcess || this.isRetracting) && this.burnTime >= 200) {
				this.speed += preheated ? 4 : 1;

				if(this.speed > maxSpeed) {
					this.speed = maxSpeed;
				}
			} else {
				this.speed -= 1;
				if(this.speed < 0) {
					this.speed = 0;
				}
			}

			if(delay <= 0) {

				int stampSpeed = speed * progressAtMax / maxSpeed;

				if(this.isRetracting) {
					this.press -= stampSpeed;

					if(this.press <= 0) {
						this.isRetracting = false;
						this.delay = 5;
					}
				} else if(canProcess) {
					this.press += stampSpeed;

					if(this.press >= maxPress) {
						this.level.playSound(null, worldPosition, ModSounds.get("block.pressOperate"), SoundSource.BLOCKS, getVolume(1.5F), 1.0F);
						ItemStack output = PressRecipes.getOutput(slots.get(2), slots.get(1));
						if(slots.get(3).isEmpty()) {
							slots.set(3, output.copy());
						} else {
							slots.get(3).grow(output.getCount());
						}
						this.removeItem(2, 1);

						ItemStack stamp = slots.get(1);
						if(stamp.isDamageableItem()) {
							stamp.setDamageValue(stamp.getDamageValue() + 1);
							if(stamp.getDamageValue() >= stamp.getMaxDamage()) {
								slots.set(1, ItemStack.EMPTY);
							}
						}

						this.isRetracting = true;
						this.delay = 5;
						if(this.burnTime >= 200) {
							this.burnTime -= 200; // only subtract fuel if operation was actually successful
						}

						this.setChanged();
					}
				} else if(this.press > 0) {
					this.isRetracting = true;
				}
			} else {
				delay--;
			}

			ItemStack fuel = slots.get(0);
			if(!fuel.isEmpty() && burnTime < 200 && getBurnTime(fuel) > 0) { // less than one operation stored? burn more fuel!
				burnTime += getBurnTime(fuel);

				if(fuel.getCount() == 1 && fuel.hasCraftingRemainingItem()) {
					slots.set(0, fuel.getCraftingRemainingItem().copy());
				} else {
					this.removeItem(0, 1);
				}
				this.markChanged();
			}

			this.networkPackNT(50);

		} else {

			// approach-based interpolation, GO!
			this.lastPress = this.renderPress;

			if(this.turnProgress > 0) {
				this.renderPress = this.renderPress + ((this.syncPress - this.renderPress) / (double) this.turnProgress);
				--this.turnProgress;
			} else {
				this.renderPress = this.syncPress;
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(this.speed);
		buf.writeInt(this.burnTime);
		buf.writeInt(this.press);
		BufferUtil.writeItemStack(buf, slots.get(2), level.registryAccess());
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.speed = buf.readInt();
		this.burnTime = buf.readInt();
		this.syncPress = buf.readInt();
		this.syncStack = BufferUtil.readItemStack(buf, level.registryAccess());

		this.turnProgress = 2;
	}

	public boolean canProcess() {
		if(burnTime < 200) return false;
		if(slots.get(1).isEmpty() || slots.get(2).isEmpty()) return false;

		ItemStack output = PressRecipes.getOutput(slots.get(2), slots.get(1));

		if(output == null) return false;

		ItemStack out = slots.get(3);
		if(out.isEmpty()) return true;
		return out.getCount() + output.getCount() <= out.getMaxStackSize() && ItemStack.isSameItemSameComponents(out, output);
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {

		if(stack.getItem() instanceof ItemStamp)
			return i == 1;

		if(getBurnTime(stack) > 0 && i == 0)
			return true;

		return i == 2;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] { 0, 1, 2, 3 };
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i == 3;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		press = nbt.getInt("press");
		burnTime = nbt.getInt("burnTime");
		speed = nbt.getInt("speed");
		isRetracting = nbt.getBoolean("ret");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putInt("press", press);
		nbt.putInt("burnTime", burnTime);
		nbt.putInt("speed", speed);
		nbt.putBoolean("ret", isRetracting);
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 3, worldPosition.getZ() + 1);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachinePress(id, inv, this);
	}
}
