package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerCompressor;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.CompressorRecipes;
import com.hbm.inventory.recipes.CompressorRecipes.CompressorRecipe;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.DirPos;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Compressors: raise a fluid's pressure by one step (the input pressure is picked in the GUI) or run a compression
 * recipe. Slots: 0 fluid identifier, 1 battery, 2-3 upgrades (speed, power, overdrive).
 *
 * TODO copy tool
 */
public abstract class TileEntityMachineCompressorBase extends TileEntityMachineBase implements IControlReceiver, IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, MenuProvider {

	public FluidTank[] tanks;
	public long power;
	public static final long maxPower = 100_000;
	public boolean isOn;
	public int progress;
	public int processTime = 100;
	public static final int processTimeBase = 100;
	public int powerRequirement;
	public static final int powerRequirementBase = 2_500;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public TileEntityMachineCompressorBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state, 4);
		this.tanks = new FluidTank[2];
		this.tanks[0] = new FluidTank(Fluids.NONE, 16_000);
		this.tanks[1] = new FluidTank(Fluids.NONE, 16_000).withPressure(1);
	}

	@Override
	public String getName() {
		return "container.machineCompressor";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.power = Library.chargeTEFromItems(slots, 1, power, maxPower);
			this.tanks[0].setType(0, slots);
			this.setupTanks();

			upgradeManager.checkSlots(slots, 1, 3);
			int speedLevel = upgradeManager.getLevel(UpgradeType.SPEED);
			int powerLevel = upgradeManager.getLevel(UpgradeType.POWER);
			int overLevel = upgradeManager.getLevel(UpgradeType.OVERDRIVE);

			CompressorRecipe rec = CompressorRecipes.getRecipe(tanks[0].getTankType(), tanks[0].getPressure());
			int timeBase = processTimeBase;
			if(rec != null) timeBase = rec.duration;

			if(rec == null) this.processTime = speedLevel == 3 ? 10 : speedLevel == 2 ? 20 : speedLevel == 1 ? 60 : timeBase;
			else this.processTime = timeBase / (speedLevel + 1);
			this.powerRequirement = powerRequirementBase / (powerLevel + 1);
			this.processTime = this.processTime / (overLevel + 1);
			this.powerRequirement = this.powerRequirement * ((overLevel * 2) + 1);

			if(processTime <= 0) processTime = 1;

			if(canProcess()) {
				this.progress++;
				this.isOn = true;
				this.power -= powerRequirement;

				if(progress >= this.processTime) {
					progress = 0;
					this.process();
					this.setChanged();
				}

			} else {
				this.progress = 0;
				this.isOn = false;
			}

			this.autoPort(getConPos());

			this.networkPackNT(100);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(this.progress);
		buf.writeInt(this.processTime);
		buf.writeInt(this.powerRequirement);
		buf.writeLong(this.power);
		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
		buf.writeBoolean(this.isOn);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.progress = buf.readInt();
		this.processTime = buf.readInt();
		this.powerRequirement = buf.readInt();
		this.power = buf.readLong();
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
		this.isOn = buf.readBoolean();
	}

	public abstract DirPos[] getConPos();

	public boolean canProcess() {
		if(this.power <= powerRequirement) return false;

		CompressorRecipe recipe = CompressorRecipes.getRecipe(tanks[0].getTankType(), tanks[0].getPressure());

		if(recipe == null) {
			return tanks[0].getFill() >= 1000 && tanks[1].getFill() + 1000 <= tanks[1].getMaxFill();
		}

		return tanks[0].getFill() >= recipe.inputAmount && tanks[1].getFill() + recipe.output.fill <= tanks[1].getMaxFill();
	}

	public void process() {
		CompressorRecipe recipe = CompressorRecipes.getRecipe(tanks[0].getTankType(), tanks[0].getPressure());

		if(recipe == null) {
			tanks[0].setFill(tanks[0].getFill() - 1_000);
			tanks[1].setFill(tanks[1].getFill() + 1_000);
		} else {
			tanks[0].setFill(tanks[0].getFill() - recipe.inputAmount);
			tanks[1].setFill(tanks[1].getFill() + recipe.output.fill);
		}
	}

	/** The output is the input one step higher, or whatever the recipe makes */
	protected void setupTanks() {
		CompressorRecipe recipe = CompressorRecipes.getRecipe(tanks[0].getTankType(), tanks[0].getPressure());

		if(recipe == null) {
			tanks[1].withPressure(tanks[0].getPressure() + 1).setTankType(tanks[0].getTankType());
		} else {
			tanks[1].withPressure(recipe.output.pressure).setTankType(recipe.output.type);
		}
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		power = nbt.getLong("power");
		progress = nbt.getInt("progress");
		tanks[0].readFromNBT(nbt, "0");
		tanks[1].readFromNBT(nbt, "1");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("power", power);
		nbt.putInt("progress", progress);
		tanks[0].writeToNBT(nbt, "0");
		tanks[1].writeToNBT(nbt, "1");
	}

	@Override
	public boolean hasPermission(Player player) {
		return this.stillValid(player);
	}

	/** The GUI picks the input pressure (0-4) */
	@Override
	public void receiveControl(CompoundTag data) {
		int compression = data.getInt("compression");

		if(compression != tanks[0].getPressure()) {
			tanks[0].withPressure(compression);

			CompressorRecipe recipe = CompressorRecipes.getRecipe(tanks[0].getTankType(), compression);
			if(recipe == null) {
				tanks[1].withPressure(compression + 1);
			} else {
				tanks[1].withPressure(recipe.output.pressure).setTankType(recipe.output.type);
			}

			this.setChanged();
		}
	}

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override public FluidTank[] getAllTanks() { return tanks; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] {tanks[1]}; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {tanks[0]}; }

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_compressor.get()));
		if(type == UpgradeType.SPEED) {
			info.add(ChatFormatting.GREEN + "Generic compression: " + I18nUtil.resolveKey(KEY_DELAY, "-" + (level == 3 ? 90 : level == 2 ? 80 : level == 1 ? 40 : 0) + "%"));
			info.add(ChatFormatting.GREEN + "Recipe: " + I18nUtil.resolveKey(KEY_DELAY, "-" + (100 - 100 / (level + 1)) + "%"));
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
		upgrades.put(UpgradeType.OVERDRIVE, 9);
		return upgrades;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerCompressor(id, inv, this);
	}
}
