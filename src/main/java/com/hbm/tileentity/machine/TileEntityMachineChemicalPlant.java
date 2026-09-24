package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineChemicalPlant;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.module.machine.ModuleMachineChemplant;
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
 * Chemical plant: generic recipes with 3 item and 3 fluid in- and outputs, picked in the recipe selector.
 * Slots: 0 battery, 1 blueprint, 2-3 upgrades, 4-6 item inputs, 7-9 item outputs, 10-12/13-15 input fluid
 * containers (in/out), 16-18/19-21 output fluid containers (in/out).
 *
 * TODO redstone over radio (IRORValueProvider/IRORInteractive), the meteorite sword treatment
 */
public class TileEntityMachineChemicalPlant extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, IControlReceiver, MenuProvider {

	public FluidTank[] inputTanks;
	public FluidTank[] outputTanks;

	public long power;
	public long maxPower = 100_000;
	public boolean didProcess = false;

	public boolean frame = false;
	public int anim;
	public int prevAnim;
	private AudioWrapper audio;

	public ModuleMachineChemplant chemplantModule;
	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public TileEntityMachineChemicalPlant(BlockPos pos, BlockState state) {
		super(ModTileEntities.CHEMICAL_PLANT.get(), pos, state, 22);

		this.inputTanks = new FluidTank[3];
		this.outputTanks = new FluidTank[3];
		for(int i = 0; i < 3; i++) {
			this.inputTanks[i] = new FluidTank(Fluids.NONE, 24_000);
			this.outputTanks[i] = new FluidTank(Fluids.NONE, 24_000);
		}

		this.chemplantModule = new ModuleMachineChemplant(0, this, slots)
				.itemInput(4, 5, 6)
				.itemOutput(7, 8, 9)
				.fluidInput(inputTanks[0], inputTanks[1], inputTanks[2])
				.fluidOutput(outputTanks[0], outputTanks[1], outputTanks[2]);
	}

	@Override
	public String getName() {
		return "container.machineChemicalPlant";
	}

	@Override
	public void updateEntity() {

		if(maxPower <= 0) this.maxPower = 1_000_000;

		if(isServer()) {

			GenericRecipe recipe = chemplantModule.getRecipe();
			if(recipe != null) {
				this.maxPower = recipe.power * 100;
			}
			this.maxPower = BobMathUtil.max(this.power, this.maxPower, 100_000);

			this.power = Library.chargeTEFromItems(slots, 0, power, maxPower);
			upgradeManager.checkSlots(slots, 2, 3);

			inputTanks[0].loadTank(10, 13, slots);
			inputTanks[1].loadTank(11, 14, slots);
			inputTanks[2].loadTank(12, 15, slots);

			outputTanks[0].unloadTank(16, 19, slots);
			outputTanks[1].unloadTank(17, 20, slots);
			outputTanks[2].unloadTank(18, 21, slots);

			this.autoPort(getConPos());

			double speed = 1D;
			double pow = 1D;

			speed += Math.min(upgradeManager.getLevel(UpgradeType.SPEED), 3) / 3D;
			speed += Math.min(upgradeManager.getLevel(UpgradeType.OVERDRIVE), 3);

			pow -= Math.min(upgradeManager.getLevel(UpgradeType.POWER), 3) * 0.25D;
			pow += Math.min(upgradeManager.getLevel(UpgradeType.SPEED), 3) * 1D;
			pow += Math.min(upgradeManager.getLevel(UpgradeType.OVERDRIVE), 3) * 10D / 3D;

			this.chemplantModule.update(speed, pow, true, slots.get(1));
			this.didProcess = this.chemplantModule.didProcess;
			if(this.chemplantModule.markDirty) this.setChanged();

			this.networkPackNT(100);

		} else {

			this.prevAnim = this.anim;
			if(this.didProcess) this.anim++;

			if(level.getGameTime() % 20 == 0) {
				frame = !level.getBlockState(worldPosition.above(3)).isAir();
			}

			if(this.didProcess) {
				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}
				audio.keepAlive();
				audio.updateVolume(this.getVolume(1F));

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
		return AudioWrapper.getLoopedSound("hbm:block.chemicalPlant", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 1F, 15F, 1.0F, 20);
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

	public DirPos[] getConPos() {
		BlockPos p = worldPosition;
		return new DirPos[] {
				new DirPos(p.offset(2, 0, -1), Direction.EAST),
				new DirPos(p.offset(2, 0, 0), Direction.EAST),
				new DirPos(p.offset(2, 0, 1), Direction.EAST),
				new DirPos(p.offset(-2, 0, -1), Direction.WEST),
				new DirPos(p.offset(-2, 0, 0), Direction.WEST),
				new DirPos(p.offset(-2, 0, 1), Direction.WEST),
				new DirPos(p.offset(-1, 0, 2), Direction.SOUTH),
				new DirPos(p.offset(0, 0, 2), Direction.SOUTH),
				new DirPos(p.offset(1, 0, 2), Direction.SOUTH),
				new DirPos(p.offset(-1, 0, -2), Direction.NORTH),
				new DirPos(p.offset(0, 0, -2), Direction.NORTH),
				new DirPos(p.offset(1, 0, -2), Direction.NORTH),
		};
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		for(FluidTank tank : inputTanks) tank.serialize(buf);
		for(FluidTank tank : outputTanks) tank.serialize(buf);
		buf.writeLong(power);
		buf.writeLong(maxPower);
		buf.writeBoolean(didProcess);
		this.chemplantModule.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		for(FluidTank tank : inputTanks) tank.deserialize(buf);
		for(FluidTank tank : outputTanks) tank.deserialize(buf);
		this.power = buf.readLong();
		this.maxPower = buf.readLong();
		this.didProcess = buf.readBoolean();
		this.chemplantModule.deserialize(buf);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);

		for(int i = 0; i < 3; i++) {
			this.inputTanks[i].readFromNBT(nbt, "i" + i);
			this.outputTanks[i].readFromNBT(nbt, "o" + i);
		}

		this.power = nbt.getLong("power");
		this.maxPower = nbt.getLong("maxPower");
		this.chemplantModule.readFromNBT(nbt);
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);

		for(int i = 0; i < 3; i++) {
			this.inputTanks[i].writeToNBT(nbt, "i" + i);
			this.outputTanks[i].writeToNBT(nbt, "o" + i);
		}

		nbt.putLong("power", power);
		nbt.putLong("maxPower", maxPower);
		this.chemplantModule.writeToNBT(nbt);
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == 0) return true; // battery
		if(slot == 1 && stack.is(ModItems.blueprints.get())) return true;
		if(slot >= 2 && slot <= 3 && stack.getItem() instanceof ItemMachineUpgrade) return true; // upgrades
		if(slot >= 10 && slot <= 12) return true; // input fluid
		if(slot >= 16 && slot <= 18) return true; // output fluid
		if(this.chemplantModule.isItemValid(slot, stack)) return true; // recipe input crap
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return (i >= 7 && i <= 9) || this.chemplantModule.isSlotClogged(i);
	}

	private static final int[] slot_access = new int[] {4, 5, 6, 7, 8, 9};

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return slot_access;
	}

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override public FluidTank[] getReceivingTanks() { return inputTanks; }
	@Override public FluidTank[] getSendingTanks() { return outputTanks; }
	@Override public FluidTank[] getAllTanks() { return new FluidTank[] {inputTanks[0], inputTanks[1], inputTanks[2], outputTanks[0], outputTanks[1], outputTanks[2]}; }

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineChemicalPlant(id, inv, this);
	}

	@Override public boolean hasPermission(Player player) { return this.stillValid(player); }

	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("index") && data.contains("selection")) {
			int index = data.getInt("index");
			String selection = data.getString("selection");
			if(index == 0) {
				this.chemplantModule.setRecipe(selection, false);
				this.markChanged();
			}
		}
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 3, worldPosition.getZ() + 2);
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_chemical_plant.get()));
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
