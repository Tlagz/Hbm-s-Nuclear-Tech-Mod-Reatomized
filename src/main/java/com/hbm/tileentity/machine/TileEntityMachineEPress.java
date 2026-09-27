package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineEPress;
import com.hbm.inventory.recipes.PressRecipes;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.items.machine.ItemStamp;
import com.hbm.lib.Library;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Electric press: the burner press on 100 HE/t, full speed right away, faster with speed upgrades.
 * Slots: 0 battery, 1 stamp, 2 input, 3 output, 4 upgrade.
 */
public class TileEntityMachineEPress extends TileEntityMachineBase implements IEnergyReceiverMK2, IUpgradeInfoProvider, MenuProvider {

	public long power = 0;
	public final static long maxPower = 50000;

	public int press;
	public double renderPress;
	public double lastPress;
	private int syncPress;
	private int turnProgress;
	public final static int maxPress = 200;
	boolean isRetracting = false;
	private int delay;

	public ItemStack syncStack = ItemStack.EMPTY;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public TileEntityMachineEPress(BlockPos pos, BlockState state) {
		super(ModTileEntities.EPRESS.get(), pos, state, 5);
	}

	@Override
	public String getName() {
		return "container.epress";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.updateConnections();

			power = Library.chargeTEFromItems(slots, 0, power, maxPower);

			boolean canProcess = this.canProcess();

			if((canProcess || this.isRetracting || this.delay > 0) && power >= 100) {

				power -= 100;

				if(delay <= 0) {

					upgradeManager.checkSlots(slots, 4, 4);
					int speed = 1 + upgradeManager.getLevel(UpgradeType.SPEED);

					int stampSpeed = this.isRetracting ? 20 : 45;
					stampSpeed *= (1D + (double) speed / 4D);

					if(this.isRetracting) {
						this.press -= stampSpeed;

						if(this.press <= 0) {
							this.isRetracting = false;
							this.delay = 5 - speed + 1;
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
							this.delay = 5 - speed + 1;

							this.setChanged();
						}
					} else if(this.press > 0) {
						this.isRetracting = true;
					}
				} else {
					delay--;
				}
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
		buf.writeLong(power);
		buf.writeInt(press);
		BufferUtil.writeItemStack(buf, slots.get(2), level.registryAccess());
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.syncPress = buf.readInt();
		this.syncStack = BufferUtil.readItemStack(buf, level.registryAccess());

		this.turnProgress = 2;
	}

	public boolean canProcess() {
		if(power < 100) return false;
		if(slots.get(1).isEmpty() || slots.get(2).isEmpty()) return false;

		ItemStack output = PressRecipes.getOutput(slots.get(2), slots.get(1));
		if(output == null) return false;

		ItemStack out = slots.get(3);
		if(out.isEmpty()) return true;
		return out.getCount() + output.getCount() <= out.getMaxStackSize() && ItemStack.isSameItemSameComponents(out, output);
	}

	private void updateConnections() {
		for(Direction dir : Direction.values()) this.trySubscribe(level, worldPosition.relative(dir), dir);
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		if(stack.getItem() instanceof ItemStamp) return i == 1;
		return i == 2;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] { 1, 2, 3 };
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i == 3;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		press = nbt.getInt("press");
		power = nbt.getLong("power");
		isRetracting = nbt.getBoolean("ret");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putInt("press", press);
		nbt.putLong("power", power);
		nbt.putBoolean("ret", isRetracting);
	}

	@Override public void setPower(long i) { power = i; }
	@Override public long getPower() { return power; }
	@Override public long getMaxPower() { return maxPower; }

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 3, worldPosition.getZ() + 1);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineEPress(id, inv, this);
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_epress.get()));
		if(type == UpgradeType.SPEED) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_DELAY, "-" + (50 * level / 3) + "%"));
		}
	}

	@Override
	public HashMap<UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.SPEED, 3);
		return upgrades;
	}
}
