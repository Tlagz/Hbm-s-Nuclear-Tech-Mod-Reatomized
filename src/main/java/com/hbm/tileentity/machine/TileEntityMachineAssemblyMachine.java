package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineAssemblyMachine;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.main.ModSounds;
import com.hbm.module.machine.ModuleMachineAssembler;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Assembly machine: generic recipes with 12 item inputs, one output and a fluid tank on each side, the recipe
 * is picked in the recipe selector (NBTControlPacket "index"/"selection"). Two arms and a spinning ring while working.
 * Slots: 0 battery, 1 blueprint, 2-3 upgrades, 4-15 inputs, 16 output.
 *
 * TODO redstone over radio (IRORValueProvider/IRORInteractive), the meteorite sword machining
 */
public class TileEntityMachineAssemblyMachine extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, IControlReceiver, MenuProvider {

	public FluidTank inputTank;
	public FluidTank outputTank;

	public long power;
	public long maxPower = 100_000;
	public boolean didProcess = false;

	public boolean frame = false;
	private AudioWrapper audio;

	public ModuleMachineAssembler assemblerModule;

	public AssemblerArm[] arms = new AssemblerArm[2];
	public double prevRing;
	public double ring;
	public double ringSpeed;
	public double ringTarget;
	public int ringDelay;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public TileEntityMachineAssemblyMachine(BlockPos pos, BlockState state) {
		super(ModTileEntities.ASSEMBLY_MACHINE.get(), pos, state, 17);
		this.inputTank = new FluidTank(Fluids.NONE, 4_000);
		this.outputTank = new FluidTank(Fluids.NONE, 4_000);

		for(int i = 0; i < this.arms.length; i++) this.arms[i] = new AssemblerArm();

		this.assemblerModule = new ModuleMachineAssembler(0, this, slots)
				.itemInput(4).itemOutput(16)
				.fluidInput(inputTank).fluidOutput(outputTank);
	}

	@Override
	public String getName() {
		return "container.machineAssemblyMachine";
	}

	@Override
	public void updateEntity() {

		if(maxPower <= 0) this.maxPower = 1_000_000;

		if(isServer()) {

			GenericRecipe recipe = assemblerModule.getRecipe();
			if(recipe != null) {
				this.maxPower = recipe.power * 100;
			}
			this.maxPower = BobMathUtil.max(this.power, this.maxPower, 100_000);

			this.power = Library.chargeTEFromItems(slots, 0, power, maxPower);
			upgradeManager.checkSlots(slots, 2, 3);

			this.autoPort(getConPos());

			double speed = 1D;
			double pow = 1D;

			speed += Math.min(upgradeManager.getLevel(UpgradeType.SPEED), 3) / 3D;
			speed += Math.min(upgradeManager.getLevel(UpgradeType.OVERDRIVE), 3);

			pow -= Math.min(upgradeManager.getLevel(UpgradeType.POWER), 3) * 0.25D;
			pow += Math.min(upgradeManager.getLevel(UpgradeType.SPEED), 3) * 1D;
			pow += Math.min(upgradeManager.getLevel(UpgradeType.OVERDRIVE), 3) * 10D / 3D;

			this.assemblerModule.update(speed, pow, true, slots.get(1));
			this.didProcess = this.assemblerModule.didProcess;
			if(this.assemblerModule.markDirty) this.setChanged();

			this.networkPackNT(100);

		} else {

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
				audio.updatePitch(0.75F);
				audio.updateVolume(this.getVolume(0.5F));

			} else {
				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}

			for(AssemblerArm arm : arms) {
				arm.updateInterp();
				if(didProcess) {
					arm.updateArm();
				} else {
					arm.returnToNullPos();
				}

				if(!this.muffled && arm.prevAngles[3] != arm.angles[3] && arm.angles[3] == -0.75) {
					level.playLocalSound(worldPosition, ModSounds.get("block.assemblerStrike"), SoundSource.BLOCKS, this.getVolume(0.5F), 1F, false);
				}
			}

			this.prevRing = this.ring;

			if(didProcess) {
				if(this.ring != this.ringTarget) {
					double ringDelta = Math.abs(this.ringTarget - this.ring);
					if(ringDelta <= this.ringSpeed) this.ring = this.ringTarget;
					if(this.ringTarget > this.ring) this.ring += this.ringSpeed;
					if(this.ringTarget < this.ring) this.ring -= this.ringSpeed;
					if(this.ringTarget == this.ring) {
						double sub = ringTarget >= 360 ? -360D : 360D;
						this.ringTarget += sub;
						this.ring += sub;
						this.prevRing += sub;
						this.ringDelay = 20 + level.random.nextInt(21);
					}
				} else {
					if(this.ringDelay > 0) this.ringDelay--;
					if(this.ringDelay <= 0) {
						this.ringTarget += (level.random.nextDouble() * 2 - 1) * 135;
						this.ringSpeed = 10D + level.random.nextDouble() * 5D;
						if(!this.muffled) level.playLocalSound(worldPosition, ModSounds.get("block.assemblerStart"), SoundSource.BLOCKS, this.getVolume(0.25F), 1.25F + level.random.nextFloat() * 0.25F, false);
					}
				}
			}
		}
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return AudioWrapper.getLoopedSound("hbm:block.motor", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 0.5F, 15F, 0.75F, 20);
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
		this.inputTank.serialize(buf);
		this.outputTank.serialize(buf);
		buf.writeLong(power);
		buf.writeLong(maxPower);
		buf.writeBoolean(didProcess);
		this.assemblerModule.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		boolean wasProcessing = this.didProcess;
		this.inputTank.deserialize(buf);
		this.outputTank.deserialize(buf);
		this.power = buf.readLong();
		this.maxPower = buf.readLong();
		this.didProcess = buf.readBoolean();
		this.assemblerModule.deserialize(buf);

		if(wasProcessing && !didProcess && level != null) {
			level.playLocalSound(worldPosition, ModSounds.get("block.assemblerStop"), SoundSource.BLOCKS, this.getVolume(0.25F), 1.5F, false);
		}
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.inputTank.readFromNBT(nbt, "i");
		this.outputTank.readFromNBT(nbt, "o");
		this.power = nbt.getLong("power");
		this.maxPower = nbt.getLong("maxPower");
		this.assemblerModule.readFromNBT(nbt);
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		this.inputTank.writeToNBT(nbt, "i");
		this.outputTank.writeToNBT(nbt, "o");
		nbt.putLong("power", power);
		nbt.putLong("maxPower", maxPower);
		this.assemblerModule.writeToNBT(nbt);
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == 0) return true; // battery
		if(slot == 1 && stack.is(ModItems.blueprints.get())) return true;
		if(slot >= 2 && slot <= 3 && stack.getItem() instanceof ItemMachineUpgrade) return true; // upgrades
		if(this.assemblerModule.isItemValid(slot, stack)) return true; // recipe input crap
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i == 16 || this.assemblerModule.isSlotClogged(i);
	}

	private static final int[] slot_access = new int[] {4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return slot_access;
	}

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {inputTank}; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] {outputTank}; }
	@Override public FluidTank[] getAllTanks() { return new FluidTank[] {inputTank, outputTank}; }

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineAssemblyMachine(id, inv, this);
	}

	@Override public boolean hasPermission(Player player) { return this.stillValid(player); }

	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("index") && data.contains("selection")) {
			int index = data.getInt("index");
			String selection = data.getString("selection");
			if(index == 0) {
				this.assemblerModule.setRecipe(selection, false);
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
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_assembly_machine.get()));
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

	public static class AssemblerArm {

		public double[] angles = new double[4];
		public double[] prevAngles = new double[4];
		public double[] targetAngles = new double[4];
		public double[] speed = new double[4];

		Random rand = new Random();
		ArmActionState state = ArmActionState.ASSUME_POSITION;
		int actionDelay = 0;

		public static enum ArmActionState {
			ASSUME_POSITION,
			EXTEND_STRIKER,
			RETRACT_STRIKER
		}

		public AssemblerArm() {
			this.resetSpeed();
		}

		private void updateInterp() {
			for(int i = 0; i < angles.length; i++) {
				prevAngles[i] = angles[i];
			}
		}

		private void returnToNullPos() {
			for(int i = 0; i < 4; i++) this.targetAngles[i] = 0;
			for(int i = 0; i < 3; i++) this.speed[i] = 3;
			this.speed[3] = 0.25;
			this.state = ArmActionState.RETRACT_STRIKER;

			this.move();
		}

		private void resetSpeed() {
			speed[0] = 15;	//Pivot
			speed[1] = 15;	//Arm
			speed[2] = 15;	//Piston
			speed[3] = 0.5;	//Striker
		}

		public void updateArm() {
			resetSpeed();

			if(actionDelay > 0) {
				actionDelay--;
				return;
			}

			switch(state) {
			// Move. If done moving, set a delay and progress to EXTEND
			case ASSUME_POSITION:
				if(move()) {
					actionDelay = 2;
					state = ArmActionState.EXTEND_STRIKER;
					targetAngles[3] = -0.75D;
				}
				break;
			case EXTEND_STRIKER:
				if(move()) {
					state = ArmActionState.RETRACT_STRIKER;
					targetAngles[3] = 0D;
				}
				break;
			case RETRACT_STRIKER:
				if(move()) {
					actionDelay = 2 + rand.nextInt(5);
					chooseNewArmPoistion();
					state = ArmActionState.ASSUME_POSITION;
				}
				break;

			}
		}

		private double[][] pos = new double[][] { // possible positions for the arms
			{45, -15, -5},
			{15, 15, -15},
			{25, 10, -15},
			{30, 0, -10},
			{70, -10, -25},
		}; // sure it's not truly random like with the old assemfac, but at least now the striker always hits the center and doesn't clip through the board

		public void chooseNewArmPoistion() {
			int chosen = rand.nextInt(pos.length);
			this.targetAngles[0] = pos[chosen][0];
			this.targetAngles[1] = pos[chosen][1];
			this.targetAngles[2] = pos[chosen][2];
		}

		private boolean move() {
			boolean didMove = false;

			for(int i = 0; i < angles.length; i++) {
				if(angles[i] == targetAngles[i])
					continue;

				didMove = true;

				double angle = angles[i];
				double target = targetAngles[i];
				double turn = speed[i];
				double delta = Math.abs(angle - target);

				if(delta <= turn) {
					angles[i] = targetAngles[i];
					continue;
				}

				if(angle < target) {
					angles[i] += turn;
				} else {
					angles[i] -= turn;
				}
			}

			return !didMove;
		}

		public double[] getPositions(float interp) {
			return new double[] {
					BobMathUtil.interp(this.prevAngles[0], this.angles[0], interp),
					BobMathUtil.interp(this.prevAngles[1], this.angles[1], interp),
					BobMathUtil.interp(this.prevAngles[2], this.angles[2], interp),
					BobMathUtil.interp(this.prevAngles[3], this.angles[3], interp)
			};
		}
	}
}
