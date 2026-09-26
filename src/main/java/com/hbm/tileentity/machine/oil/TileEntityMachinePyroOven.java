package com.hbm.tileentity.machine.oil;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerPyroOven;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.PyroOvenRecipes;
import com.hbm.inventory.recipes.PyroOvenRecipes.PyroOvenRecipe;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachinePolluting;
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
import net.minecraft.core.particles.ParticleTypes;
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
 * Pyrolysis oven: an optional fluid and item into an optional fluid and item, soot goes into the smoke tank and
 * vents when it's full. Slots: 0 battery, 1 input, 2 output, 3 fluid identifier, 4/5 upgrades (speed, power,
 * overdrive).
 *
 * TODO copy tool, upgrade plug sound
 */
public class TileEntityMachinePyroOven extends TileEntityMachinePolluting implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, MenuProvider {

	public long power;
	public static final long maxPower = 10_000_000;
	public boolean isVenting;
	public boolean isProgressing;
	public float progress;
	public static int consumption = 10_000;

	public int prevAnim;
	public int anim = 0;

	public FluidTank[] tanks;

	private AudioWrapper audio;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public TileEntityMachinePyroOven(BlockPos pos, BlockState state) {
		super(ModTileEntities.PYRO_OVEN.get(), pos, state, 6, 50);
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.NONE, 24_000);
		tanks[1] = new FluidTank(Fluids.NONE, 24_000);
	}

	@Override
	public String getName() {
		return "container.machinePyroOven";
	}

	@Override
	public void updateEntity() {

		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getCounterClockWise();

		if(isServer()) {

			this.power = Library.chargeTEFromItems(slots, 0, power, maxPower);
			tanks[0].setType(3, slots);

			for(DirPos pos : getConPos()) {
				this.trySubscribe(level, pos, pos.getDir());
				if(tanks[0].getTankType() != Fluids.NONE) this.trySubscribe(tanks[0].getTankType(), level, pos, pos.getDir());
				if(tanks[1].getFill() > 0) this.tryProvide(tanks[1], level, pos);
			}

			if(smoke.getFill() > 0) this.tryProvide(smoke, level, worldPosition.offset(-rot.getStepX(), 3, -rot.getStepZ()), Direction.UP);

			upgradeManager.checkSlots(slots, 4, 5);
			int speed = upgradeManager.getLevel(UpgradeType.SPEED);
			int powerSaving = upgradeManager.getLevel(UpgradeType.POWER);
			int overdrive = upgradeManager.getLevel(UpgradeType.OVERDRIVE);

			this.isProgressing = false;
			this.isVenting = false;

			if(this.canProcess()) {
				PyroOvenRecipe recipe = getMatchingRecipe();
				this.progress += 1F / Math.max((recipe.duration - speed * (recipe.duration / 4)) / (overdrive * 2 + 1), 1);
				this.isProgressing = true;
				this.power -= getConsumption(speed + overdrive * 2, powerSaving);

				if(progress >= 1F) {
					this.progress = 0F;
					this.finishRecipe(recipe);
					this.setChanged();
				}

				this.pollute(PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND);

			} else {
				this.progress = 0F;
			}

			this.networkPackNT(50);

		} else {

			this.prevAnim = this.anim;

			if(isProgressing) {
				this.anim++;

				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}

				audio.keepAlive();
				audio.updateVolume(this.getVolume(1F));

				if(com.hbm.main.ClientHooks.distanceToPlayer(worldPosition.getX() + 0.5, worldPosition.getY() + 3, worldPosition.getZ() + 0.5) < 50) {
					for(double d : new double[] {-2.375, -0.875, 0.875, 2.375}) {
						if(level.random.nextInt(20) == 0) level.addParticle(ParticleTypes.CLOUD, worldPosition.getX() + 0.5 - rot.getStepX() + dir.getStepX() * d, worldPosition.getY() + 3, worldPosition.getZ() + 0.5 - rot.getStepZ() + dir.getStepZ() * d, 0.0, 0.05, 0.0);
					}
				}

			} else {
				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}

			if(this.isVenting && level.getGameTime() % 2 == 0) {
				CompoundTag fx = new CompoundTag();
				fx.putString("type", "tower");
				fx.putFloat("lift", 10F);
				fx.putFloat("base", 0.25F);
				fx.putFloat("max", 2.5F);
				fx.putInt("life", 100 + level.random.nextInt(20));
				fx.putInt("color", 0x202020);
				fx.putDouble("posX", worldPosition.getX() + 0.5 - rot.getStepX());
				fx.putDouble("posY", worldPosition.getY() + 3);
				fx.putDouble("posZ", worldPosition.getZ() + 0.5 - rot.getStepZ());
				com.hbm.particle.ParticleEffectsNT.effectNT(fx);
			}
		}
	}

	public static int getConsumption(int speed, int powerSaving) {
		return (int) (consumption * Math.pow(speed + 1, 2)) / (powerSaving + 1);
	}

	protected PyroOvenRecipe lastValidRecipe;

	public PyroOvenRecipe getMatchingRecipe() {

		if(lastValidRecipe != null && doesRecipeMatch(lastValidRecipe)) return lastValidRecipe;

		for(PyroOvenRecipe rec : PyroOvenRecipes.recipes) {
			if(doesRecipeMatch(rec)) {
				lastValidRecipe = rec;
				return rec;
			}
		}

		return null;
	}

	public boolean doesRecipeMatch(PyroOvenRecipe recipe) {

		if(recipe.inputFluid != null) {
			if(tanks[0].getTankType() != recipe.inputFluid.type) return false; // recipe needs fluid, fluid doesn't match
		}

		if(recipe.inputItem != null) {
			if(slots.get(1).isEmpty()) return false; // recipe needs item, no item present
			if(!recipe.inputItem.matchesRecipe(slots.get(1), true)) return false; // recipe needs item, item doesn't match
		} else {
			if(!slots.get(1).isEmpty()) return false; // recipe does not need item, but item is present
		}

		return true;
	}

	public boolean canProcess() {
		int speed = upgradeManager.getLevel(UpgradeType.SPEED);
		int powerSaving = upgradeManager.getLevel(UpgradeType.POWER);
		if(power < getConsumption(speed, powerSaving)) return false; // not enough power

		PyroOvenRecipe recipe = this.getMatchingRecipe();
		if(recipe == null) return false; // no matching recipe
		if(recipe.inputFluid != null && tanks[0].getFill() < recipe.inputFluid.fill) return false; // not enough input fluid
		if(recipe.inputItem != null && slots.get(1).getCount() < recipe.inputItem.stacksize) return false; // not enough input item
		if(recipe.outputFluid != null && recipe.outputFluid.fill + tanks[1].getFill() > tanks[1].getMaxFill() && recipe.outputFluid.type == tanks[1].getTankType()) return false; // too much output fluid

		ItemStack out = slots.get(2);
		if(recipe.outputItem != null && !out.isEmpty()) {
			if(recipe.outputItem.getCount() + out.getCount() > out.getMaxStackSize()) return false; // too much output item
			if(!ItemStack.isSameItemSameComponents(recipe.outputItem, out)) return false; // output item doesn't match
		}

		return true;
	}

	public void finishRecipe(PyroOvenRecipe recipe) {
		if(recipe.outputItem != null) {
			if(slots.get(2).isEmpty()) {
				slots.set(2, recipe.outputItem.copy());
			} else {
				slots.get(2).grow(recipe.outputItem.getCount());
			}
		}
		if(recipe.outputFluid != null) {
			tanks[1].setTankType(recipe.outputFluid.type);
			tanks[1].setFill(tanks[1].getFill() + recipe.outputFluid.fill);
		}
		if(recipe.inputItem != null) {
			this.removeItem(1, recipe.inputItem.stacksize);
		}
		if(recipe.inputFluid != null) {
			tanks[0].setFill(tanks[0].getFill() - recipe.inputFluid.fill);
		}
	}

	/** Five ports along the long side opposite the chimney */
	protected DirPos[] getConPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getCounterClockWise();
		BlockPos base = worldPosition.relative(rot, 3);
		return new DirPos[] {
				new DirPos(base.relative(dir, 2), rot),
				new DirPos(base.relative(dir, 1), rot),
				new DirPos(base, rot),
				new DirPos(base.relative(dir, -1), rot),
				new DirPos(base.relative(dir, -2), rot),
		};
	}

	@Override
	public void pollute(PollutionType type, float amount) {
		FluidTank tank = type == PollutionType.SOOT ? smoke : type == PollutionType.HEAVYMETAL ? smoke_leaded : smoke_poison;

		int fluidAmount = (int) Math.ceil(amount * 100);
		tank.setFill(tank.getFill() + fluidAmount);

		if(tank.getFill() > tank.getMaxFill()) {
			int overflow = tank.getFill() - tank.getMaxFill();
			tank.setFill(tank.getMaxFill());
			PollutionHandler.incrementPollution(level, worldPosition, type, overflow / 100F);
			this.isVenting = true;
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
		buf.writeLong(power);
		buf.writeBoolean(isVenting);
		buf.writeBoolean(isProgressing);
		buf.writeFloat(progress);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
		power = buf.readLong();
		isVenting = buf.readBoolean();
		isProgressing = buf.readBoolean();
		progress = buf.readFloat();
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.tanks[0].readFromNBT(nbt, "t0");
		this.tanks[1].readFromNBT(nbt, "t1");
		this.progress = nbt.getFloat("prog");
		this.power = nbt.getLong("power");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		this.tanks[0].writeToNBT(nbt, "t0");
		this.tanks[1].writeToNBT(nbt, "t1");
		nbt.putFloat("prog", progress);
		nbt.putLong("power", power);
	}

	@Override public int[] getAccessibleSlotsFromSide(Direction side) { return new int[] { 1, 2 }; }
	@Override public boolean isItemValidForSlot(int i, ItemStack itemStack) { return i == 1; }
	@Override public boolean canExtractItem(int i, ItemStack itemStack, Direction side) { return i == 2; }

	@Override
	public AudioWrapper createAudioLoop() {
		return AudioWrapper.getLoopedSound("hbm:block.pyrooperate", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 1.0F, 15F, 1.0F, 20);
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

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 3, worldPosition.getY(), worldPosition.getZ() - 3, worldPosition.getX() + 4, worldPosition.getY() + 3.5, worldPosition.getZ() + 4);
	}

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override public FluidTank[] getAllTanks() { return new FluidTank[] { tanks[0], tanks[1], smoke }; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] { tanks[1], smoke }; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { tanks[0] }; }

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_pyrooven.get()));
		if(type == UpgradeType.SPEED) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_DELAY, "-" + (level * 25) + "%"));
			info.add(ChatFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + (Math.pow(level + 1, 2) * 100 - 100) + "%"));
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
		return new ContainerPyroOven(id, inv, this);
	}
}
