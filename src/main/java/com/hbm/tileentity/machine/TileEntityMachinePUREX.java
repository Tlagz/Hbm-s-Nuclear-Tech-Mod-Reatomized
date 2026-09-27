package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachinePUREX;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.module.machine.ModuleMachinePUREX;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.DirPos;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * PUREX: nuclear fuel reprocessing, recipes with 3 items and 3 fluids in, 6 items and a fluid out.
 * Slots: 0 battery, 1 blueprint, 2-3 upgrades, 4-6 inputs, 7-12 outputs.
 *
 * TODO redstone over radio (IRORValueProvider)
 */
public class TileEntityMachinePUREX extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, IControlReceiver, MenuProvider {

	public FluidTank[] inputTanks;
	public FluidTank[] outputTanks;

	public long power;
	public long maxPower = 1_000_000;
	public boolean didProcess = false;

	public boolean frame = false;
	public int anim;
	public int prevAnim;
	private AudioWrapper audio;

	public ModuleMachinePUREX purexModule;
	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public TileEntityMachinePUREX(BlockPos pos, BlockState state) {
		super(ModTileEntities.PUREX.get(), pos, state, 13);

		this.inputTanks = new FluidTank[3];
		this.outputTanks = new FluidTank[1];
		for(int i = 0; i < 3; i++) {
			this.inputTanks[i] = new FluidTank(Fluids.NONE, 24_000);
		}
		this.outputTanks[0] = new FluidTank(Fluids.NONE, 24_000);

		this.purexModule = new ModuleMachinePUREX(0, this, slots)
				.itemInput(4).itemOutput(7)
				.fluidInput(inputTanks[0], inputTanks[1], inputTanks[2]).fluidOutput(outputTanks[0]);
	}

	@Override
	public String getName() {
		return "container.machinePUREX";
	}

	@Override
	public void updateEntity() {

		if(maxPower <= 0) this.maxPower = 1_000_000;

		if(isServer()) {

			GenericRecipe recipe = purexModule.getRecipe();
			if(recipe != null) {
				this.maxPower = recipe.power * 100;
			}
			this.maxPower = BobMathUtil.max(this.power, this.maxPower, 1_000_000);

			this.power = Library.chargeTEFromItems(slots, 0, power, maxPower);
			upgradeManager.checkSlots(slots, 2, 3);

			for(DirPos pos : getConPos()) {
				this.trySubscribe(level, pos);
				for(FluidTank tank : inputTanks) if(tank.getTankType() != Fluids.NONE) this.trySubscribe(tank.getTankType(), level, pos);
				for(FluidTank tank : outputTanks) if(tank.getFill() > 0) this.tryProvide(tank, level, pos);
			}

			double speed = 1D;
			double pow = 1D;

			speed += Math.min(upgradeManager.getLevel(UpgradeType.SPEED), 3) / 3D;
			speed += Math.min(upgradeManager.getLevel(UpgradeType.OVERDRIVE), 3);

			pow -= Math.min(upgradeManager.getLevel(UpgradeType.POWER), 3) * 0.25D;
			pow += Math.min(upgradeManager.getLevel(UpgradeType.SPEED), 3) * 1D;
			pow += Math.min(upgradeManager.getLevel(UpgradeType.OVERDRIVE), 3) * 10D / 3D;

			this.purexModule.update(speed, pow, true, slots.get(1));
			this.didProcess = this.purexModule.didProcess;
			if(this.purexModule.markDirty) this.setChanged();

			this.networkPackNT(100);

		} else {

			this.prevAnim = this.anim;

			if(level.getGameTime() % 20 == 0) {
				frame = !level.getBlockState(worldPosition.above(5)).isAir();
			}

			if(didProcess) {
				this.anim++;

				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}
				audio.keepAlive();
				audio.updateVolume(this.getVolume(1F));
				audio.updatePitch(0.75F);

			} else {
				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return AudioWrapper.getLoopedSound("hbm:block.chemicalPlant", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 1F, 15F, 0.75F, 15);
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		if(audio != null) { audio.stopSound(); audio = null; }
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if(audio != null) { audio.stopSound(); audio = null; }
	}

	/** The whole bottom ring */
	public DirPos[] getConPos() {
		DirPos[] pos = new DirPos[20];
		for(int i = -2; i <= 2; i++) {
			pos[i + 2] = new DirPos(worldPosition.offset(3, 0, i), Direction.EAST);
			pos[i + 7] = new DirPos(worldPosition.offset(-3, 0, i), Direction.WEST);
			pos[i + 12] = new DirPos(worldPosition.offset(i, 0, 3), Direction.SOUTH);
			pos[i + 17] = new DirPos(worldPosition.offset(i, 0, -3), Direction.NORTH);
		}
		return pos;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		for(FluidTank tank : inputTanks) tank.serialize(buf);
		for(FluidTank tank : outputTanks) tank.serialize(buf);
		buf.writeLong(power);
		buf.writeLong(maxPower);
		buf.writeBoolean(didProcess);
		this.purexModule.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		for(FluidTank tank : inputTanks) tank.deserialize(buf);
		for(FluidTank tank : outputTanks) tank.deserialize(buf);
		this.power = buf.readLong();
		this.maxPower = buf.readLong();
		this.didProcess = buf.readBoolean();
		this.purexModule.deserialize(buf);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		for(int i = 0; i < 3; i++) this.inputTanks[i].readFromNBT(nbt, "i" + i);
		this.outputTanks[0].readFromNBT(nbt, "o" + 0);
		this.power = nbt.getLong("power");
		this.maxPower = nbt.getLong("maxPower");
		this.purexModule.readFromNBT(nbt);
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		for(int i = 0; i < 3; i++) this.inputTanks[i].writeToNBT(nbt, "i" + i);
		this.outputTanks[0].writeToNBT(nbt, "o" + 0);
		nbt.putLong("power", power);
		nbt.putLong("maxPower", maxPower);
		this.purexModule.writeToNBT(nbt);
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == 0) return true; // battery
		if(slot == 1 && stack.is(ModItems.blueprints.get())) return true;
		if(slot >= 2 && slot <= 3 && stack.getItem() instanceof ItemMachineUpgrade) return true; // upgrades
		if(this.purexModule.isItemValid(slot, stack)) return true; // recipe input crap
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return (i >= 7 && i <= 12) || this.purexModule.isSlotClogged(i);
	}

	private static final int[] slot_access = new int[] {4, 5, 6, 7, 8, 9, 10, 11, 12};

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return slot_access;
	}

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override public FluidTank[] getReceivingTanks() { return inputTanks; }
	@Override public FluidTank[] getSendingTanks() { return outputTanks; }
	@Override public FluidTank[] getAllTanks() { return new FluidTank[] {inputTanks[0], inputTanks[1], inputTanks[2], outputTanks[0]}; }

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachinePUREX(id, inv, this);
	}

	@Override public boolean hasPermission(Player player) { return this.stillValid(player); }

	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("index") && data.contains("selection")) {
			int index = data.getInt("index");
			String selection = data.getString("selection");
			if(index == 0) {
				this.purexModule.setRecipe(selection, false);
				this.markChanged();
			}
		}
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 2, worldPosition.getY(), worldPosition.getZ() - 2, worldPosition.getX() + 3, worldPosition.getY() + 5, worldPosition.getZ() + 3);
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_purex.get()));
		if(type == UpgradeType.SPEED) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_SPEED, "+" + (level * 100 / 3) + "%"));
			info.add(ChatFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + (level * 50) + "%"));
		}
		if(type == UpgradeType.POWER) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_CONSUMPTION, "-" + (level * 25) + "%"));
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
}
