package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerCentrifuge;
import com.hbm.inventory.recipes.CentrifugeRecipes;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Centrifuge: splits one input into up to four outputs.
 * Slots: 0 input, 1 battery, 2-5 outputs, 6/7 upgrades (speed, power, overdrive).
 *
 * TODO config (IConfigurableMachine), EnergyControl info
 */
public class TileEntityMachineCentrifuge extends TileEntityMachineBase implements IEnergyReceiverMK2, IUpgradeInfoProvider, MenuProvider {

	public int progress;
	public long power;
	public boolean isProgressing;
	private int audioDuration = 0;

	private AudioWrapper audio;

	//configurable values
	public static int maxPower = 100000;
	public static int processingSpeed = 200;
	public static int baseConsumption = 200;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	/*
	 * So why do we do this now? You have a funny mekanism/thermal/whatever pipe and you want to output stuff from a side
	 * that isn't the bottom, what do? Answer: make all slots accessible from all sides and regulate in/output in the
	 * dedicated methods. Duh.
	 */
	private static final int[] slot_io = new int[] { 0, 2, 3, 4, 5 };

	public TileEntityMachineCentrifuge(BlockPos pos, BlockState state) {
		super(ModTileEntities.CENTRIFUGE.get(), pos, state, 8);
	}

	@Override
	public String getName() {
		return "container.centrifuge";
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		if(i == 0 && CentrifugeRecipes.getOutput(itemStack) != null) return true;
		if(i == 1 && itemStack.getItem() instanceof IBatteryItem) return true;
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return slot_io;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i > 1;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		power = nbt.getLong("power");
		progress = nbt.getShort("progress");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("power", power);
		nbt.putShort("progress", (short) progress);
	}

	public int getCentrifugeProgressScaled(int i) {
		return (progress * i) / processingSpeed;
	}

	public long getPowerRemainingScaled(int i) {
		return (power * i) / maxPower;
	}

	public boolean canProcess() {

		if(slots.get(0).isEmpty()) {
			return false;
		}

		ItemStack[] out = CentrifugeRecipes.getOutput(slots.get(0));

		if(out == null) {
			return false;
		}

		for(int i = 0; i < Math.min(4, out.length); i++) {

			//either the slot is empty, the output is empty or the output can be added to the existing slot
			ItemStack slot = slots.get(i + 2);
			if(slot.isEmpty())
				continue;

			if(out[i].isEmpty())
				continue;

			if(ItemStack.isSameItemSameComponents(slot, out[i]) && slot.getCount() + out[i].getCount() <= out[i].getMaxStackSize())
				continue;

			return false;
		}

		return true;
	}

	private void processItem() {
		ItemStack[] out = CentrifugeRecipes.getOutput(slots.get(0));

		for(int i = 0; i < Math.min(4, out.length); i++) {

			if(out[i].isEmpty())
				continue;

			if(slots.get(i + 2).isEmpty()) {
				slots.set(i + 2, out[i].copy());
			} else {
				slots.get(i + 2).grow(out[i].getCount());
			}
		}

		this.removeItem(0, 1);
		this.setChanged();
	}

	public boolean hasPower() {
		return power > 0;
	}

	public boolean isProcessing() {
		return this.progress > 0;
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			for(Direction dir : Direction.values()) this.trySubscribe(level, worldPosition.relative(dir), dir);

			power = Library.chargeTEFromItems(slots, 1, power, maxPower);

			int consumption = baseConsumption;
			int speed = 1;

			upgradeManager.checkSlots(slots, 6, 7);
			speed += upgradeManager.getLevel(UpgradeType.SPEED);
			consumption += upgradeManager.getLevel(UpgradeType.SPEED) * baseConsumption;

			speed *= (1 + upgradeManager.getLevel(UpgradeType.OVERDRIVE) * 5);
			consumption += upgradeManager.getLevel(UpgradeType.OVERDRIVE) * baseConsumption * 50;

			consumption /= (1 + upgradeManager.getLevel(UpgradeType.POWER));

			if(hasPower() && isProcessing()) {
				this.power -= consumption;

				if(this.power < 0) {
					this.power = 0;
				}
			}

			if(hasPower() && canProcess()) {
				isProgressing = true;
			} else {
				isProgressing = false;
			}

			if(isProgressing) {
				progress += speed;

				if(this.progress >= TileEntityMachineCentrifuge.processingSpeed) {
					this.progress = 0;
					this.processItem();
				}
			} else {
				progress = 0;
			}

			this.networkPackNT(50);
		} else {

			if(isProgressing) {
				audioDuration += 2;
			} else {
				audioDuration -= 3;
			}

			audioDuration = Mth.clamp(audioDuration, 0, 60);

			if(audioDuration > 10 && com.hbm.main.ClientHooks.distanceToPlayer(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) < 25) {

				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}

				audio.updateVolume(getVolume(1F));
				audio.updatePitch((audioDuration - 10) / 100F + 0.5F);
				audio.keepAlive();

			} else {

				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(progress);
		buf.writeBoolean(isProgressing);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		progress = buf.readInt();
		isProgressing = buf.readBoolean();
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return AudioWrapper.getLoopedSound("hbm:block.centrifugeOperate", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 1.0F, 10F, 1.0F, 20);
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 4, worldPosition.getZ() + 1);
	}

	@Override
	public void setPower(long i) {
		power = i;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_centrifuge.get()));
		if(type == UpgradeType.SPEED) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_DELAY, "-" + (100 - 100 / (level + 1)) + "%"));
			info.add(ChatFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + (level * 100) + "%"));
		}
		if(type == UpgradeType.POWER) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_CONSUMPTION, "-" + (100 - 100 / (level + 1)) + "%"));
		}
		if(type == UpgradeType.OVERDRIVE) {
			info.add((BobMathUtil.getBlink() ? ChatFormatting.RED : ChatFormatting.DARK_GRAY) + "YES");
		}
	}

	@Override
	public HashMap<UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.SPEED, 3);
		upgrades.put(UpgradeType.POWER, 3);
		upgrades.put(UpgradeType.OVERDRIVE, 3);
		return upgrades;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerCentrifuge(id, inv, this);
	}
}
