package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineAssemblyFactory;
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
import com.hbm.tileentity.IConditionalInvAccess;
import com.hbm.tileentity.IProxyDelegateProvider;
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
 * Assembly factory: four assembly machine recipe fields in one 5x5 block, cooled with water like the chemical factory.
 * Slots: 0 battery, 1-3 upgrades, then per field (i * 14): 4 blueprint, 5-16 inputs, 17 output.
 * Two carriages with a striker and a saw each service the four pedestals, sliding over every now and then.
 *
 * TODO redstone over radio (IRORValueProvider)
 */
public class TileEntityMachineAssemblyFactory extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, IControlReceiver, IProxyDelegateProvider, IConditionalInvAccess, MenuProvider {

	public FluidTank[] allTanks;
	public FluidTank[] inputTanks;
	public FluidTank[] outputTanks;

	public FluidTank water;
	public FluidTank lps;

	public long power;
	public long maxPower = 1_000_000;
	public boolean[] didProcess = new boolean[4];

	public boolean frame = false;
	private AudioWrapper audio;

	public TragicYuri[] animations;
	public ModuleMachineAssembler[] assemblerModule;
	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	protected DelegateAssemblyFactory delegate = new DelegateAssemblyFactory();

	public TileEntityMachineAssemblyFactory(BlockPos pos, BlockState state) {
		super(ModTileEntities.ASSEMBLY_FACTORY.get(), pos, state, 60);

		animations = new TragicYuri[2];
		for(int i = 0; i < animations.length; i++) animations[i] = new TragicYuri(i);

		this.inputTanks = new FluidTank[4];
		this.outputTanks = new FluidTank[4];
		for(int i = 0; i < 4; i++) {
			this.inputTanks[i] = new FluidTank(Fluids.NONE, 4_000);
			this.outputTanks[i] = new FluidTank(Fluids.NONE, 4_000);
		}

		this.water = new FluidTank(Fluids.WATER, 4_000);
		this.lps = new FluidTank(Fluids.SPENTSTEAM, 4_000);

		this.allTanks = new FluidTank[this.inputTanks.length + this.outputTanks.length + 2];
		for(int i = 0; i < inputTanks.length; i++) this.allTanks[i] = this.inputTanks[i];
		for(int i = 0; i < outputTanks.length; i++) this.allTanks[i + this.inputTanks.length] = this.outputTanks[i];
		this.allTanks[this.allTanks.length - 2] = this.water;
		this.allTanks[this.allTanks.length - 1] = this.lps;

		this.assemblerModule = new ModuleMachineAssembler[4];
		for(int i = 0; i < 4; i++) this.assemblerModule[i] = new ModuleMachineAssembler(i, this, slots)
				.itemInput(5 + i * 14).itemOutput(17 + i * 14)
				.fluidInput(inputTanks[i]).fluidOutput(outputTanks[i]);
	}

	@Override
	public String getName() {
		return "container.machineAssemblyFactory";
	}

	@Override
	public void updateEntity() {

		if(maxPower <= 0) this.maxPower = 10_000_000;

		if(isServer()) {

			long nextMaxPower = 0;
			for(int i = 0; i < 4; i++) {
				GenericRecipe recipe = assemblerModule[i].getRecipe();
				if(recipe != null) nextMaxPower += recipe.power * 100;
			}
			this.maxPower = BobMathUtil.max(this.power, nextMaxPower, 1_000_000);

			this.power = Library.chargeTEFromItems(slots, 0, power, maxPower);
			upgradeManager.checkSlots(slots, 1, 3);

			for(DirPos pos : getConPos()) {
				this.trySubscribe(level, pos);
				for(FluidTank tank : inputTanks) if(tank.getTankType() != Fluids.NONE) this.trySubscribe(tank.getTankType(), level, pos);
				for(FluidTank tank : outputTanks) if(tank.getFill() > 0) this.tryProvide(tank, level, pos);
			}

			for(DirPos pos : getCoolPos()) {
				delegate.trySubscribe(level, pos);
				delegate.trySubscribe(water.getTankType(), level, pos);
				delegate.tryProvide(lps, level, pos);
			}

			double speed = 1D;
			double pow = 1D;

			speed += Math.min(upgradeManager.getLevel(UpgradeType.SPEED), 3) / 3D;
			speed += Math.min(upgradeManager.getLevel(UpgradeType.OVERDRIVE), 3);

			pow -= Math.min(upgradeManager.getLevel(UpgradeType.POWER), 3) * 0.25D;
			pow += Math.min(upgradeManager.getLevel(UpgradeType.SPEED), 3) * 1D;
			pow += Math.min(upgradeManager.getLevel(UpgradeType.OVERDRIVE), 3) * 10D / 3D;
			boolean markDirty = false;

			for(int i = 0; i < 4; i++) {
				this.assemblerModule[i].update(speed * 2D, pow * 2D, canCool(), slots.get(4 + i * 14));
				this.didProcess[i] = this.assemblerModule[i].didProcess;
				markDirty |= this.assemblerModule[i].markDirty;

				if(this.assemblerModule[i].didProcess) {
					this.water.setFill(this.water.getFill() - 100);
					this.lps.setFill(this.lps.getFill() + 100);
				}
			}

			if(markDirty) this.setChanged();

			this.networkPackNT(100);

		} else {

			boolean working = didProcess[0] || didProcess[1] || didProcess[2] || didProcess[3];

			if(working) {
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

			for(TragicYuri animation : animations) animation.update(working);

			if(level.getGameTime() % 20 == 0) {
				frame = !level.getBlockState(worldPosition.above(3)).isAir();
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

	private void playSoundClient(String sound, float volume, float pitch) {
		level.playLocalSound(worldPosition, ModSounds.get(sound), SoundSource.BLOCKS, volume, pitch, false);
	}

	public boolean canCool() {
		return water.getFill() >= 100 && lps.getFill() <= lps.getMaxFill() - 100;
	}

	public DirPos[] getConPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		BlockPos p = worldPosition;

		return new DirPos[] {
				new DirPos(p.offset(3, 0, -2), Direction.EAST),
				new DirPos(p.offset(3, 0, 0), Direction.EAST),
				new DirPos(p.offset(3, 0, 2), Direction.EAST),
				new DirPos(p.offset(-3, 0, -2), Direction.WEST),
				new DirPos(p.offset(-3, 0, 0), Direction.WEST),
				new DirPos(p.offset(-3, 0, 2), Direction.WEST),
				new DirPos(p.offset(-2, 0, 3), Direction.SOUTH),
				new DirPos(p.offset(0, 0, 3), Direction.SOUTH),
				new DirPos(p.offset(2, 0, 3), Direction.SOUTH),
				new DirPos(p.offset(-2, 0, -3), Direction.NORTH),
				new DirPos(p.offset(0, 0, -3), Direction.NORTH),
				new DirPos(p.offset(2, 0, -3), Direction.NORTH),
				new DirPos(p.relative(dir, 1).relative(rot, 3), rot),
				new DirPos(p.relative(dir, -1).relative(rot, 3), rot),
				new DirPos(p.relative(dir, 1).relative(rot, -3), rot.getOpposite()),
				new DirPos(p.relative(dir, -1).relative(rot, -3), rot.getOpposite()),
		};
	}

	/** Water in, low pressure steam out, on the front and back */
	public DirPos[] getCoolPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		BlockPos p = worldPosition;

		return new DirPos[] {
				new DirPos(p.relative(rot, 1).relative(dir, 3), dir),
				new DirPos(p.relative(rot, -1).relative(dir, 3), dir),
				new DirPos(p.relative(rot, 1).relative(dir, -3), dir.getOpposite()),
				new DirPos(p.relative(rot, -1).relative(dir, -3), dir.getOpposite()),
		};
	}

	/** The item ports on the sides, one for each recipe field */
	public DirPos[] getIOPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		BlockPos p = worldPosition;

		return new DirPos[] {
				new DirPos(p.relative(dir, 1).relative(rot, 3), rot),
				new DirPos(p.relative(dir, -1).relative(rot, 3), rot),
				new DirPos(p.relative(dir, 1).relative(rot, -3), rot.getOpposite()),
				new DirPos(p.relative(dir, -1).relative(rot, -3), rot.getOpposite()),
		};
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		for(FluidTank tank : inputTanks) tank.serialize(buf);
		for(FluidTank tank : outputTanks) tank.serialize(buf);
		water.serialize(buf);
		lps.serialize(buf);
		buf.writeLong(power);
		buf.writeLong(maxPower);
		for(boolean b : didProcess) buf.writeBoolean(b);
		for(int i = 0; i < 4; i++) this.assemblerModule[i].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		for(FluidTank tank : inputTanks) tank.deserialize(buf);
		for(FluidTank tank : outputTanks) tank.deserialize(buf);
		water.deserialize(buf);
		lps.deserialize(buf);
		this.power = buf.readLong();
		this.maxPower = buf.readLong();
		for(int i = 0; i < 4; i++) this.didProcess[i] = buf.readBoolean();
		for(int i = 0; i < 4; i++) this.assemblerModule[i].deserialize(buf);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		for(int i = 0; i < inputTanks.length; i++) this.inputTanks[i].readFromNBT(nbt, "i" + i);
		for(int i = 0; i < outputTanks.length; i++) this.outputTanks[i].readFromNBT(nbt, "o" + i);
		this.water.readFromNBT(nbt, "w");
		this.lps.readFromNBT(nbt, "s");
		this.power = nbt.getLong("power");
		this.maxPower = nbt.getLong("maxPower");
		for(int i = 0; i < 4; i++) this.assemblerModule[i].readFromNBT(nbt);
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		for(int i = 0; i < inputTanks.length; i++) this.inputTanks[i].writeToNBT(nbt, "i" + i);
		for(int i = 0; i < outputTanks.length; i++) this.outputTanks[i].writeToNBT(nbt, "o" + i);
		this.water.writeToNBT(nbt, "w");
		this.lps.writeToNBT(nbt, "s");
		nbt.putLong("power", power);
		nbt.putLong("maxPower", maxPower);
		for(int i = 0; i < 4; i++) this.assemblerModule[i].writeToNBT(nbt);
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == 0) return true; // battery
		for(int i = 0; i < 4; i++) if(slot == 4 + i * 14 && stack.is(ModItems.blueprints.get())) return true;
		if(slot >= 1 && slot <= 3 && stack.getItem() instanceof ItemMachineUpgrade) return true; // upgrades
		for(int i = 0; i < 4; i++) if(this.assemblerModule[i].isItemValid(slot, stack)) return true; // recipe input crap
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		for(int k = 0; k < 4; k++) if(i == 17 + k * 14) return true;
		for(int k = 0; k < 4; k++) if(this.assemblerModule[k].isSlotClogged(i)) return true;
		return false;
	}

	private static final int[] slot_access = new int[] {
			 5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15, 16, 17,
			19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
			33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45,
			47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59
	};

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return slot_access;
	}

	/// CONDITIONAL ACCESS ///
	@Override public boolean isItemValidForSlot(BlockPos pos, int slot, ItemStack stack) { return this.isItemValidForSlot(slot, stack); }
	@Override public boolean canExtractItem(BlockPos pos, int slot, ItemStack stack, Direction side) { return this.canExtractItem(slot, stack, side); }

	/** The side ports only insert into their own field's inputs, but every port can pull from all outputs */
	@Override
	public int[] getAccessibleSlotsFromSide(BlockPos pos, Direction side) {
		DirPos[] io = getIOPos();
		for(int i = 0; i < io.length; i++) {
			if(io[i].equals(pos.relative(io[i].getDir()))) {
				int[] access = new int[16];
				for(int j = 0; j < 12; j++) access[j] = 5 + i * 14 + j;
				for(int j = 0; j < 4; j++) access[12 + j] = 17 + j * 14;
				return access;
			}
		}
		return this.getAccessibleSlotsFromSide(side);
	}

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override public FluidTank[] getReceivingTanks() { return inputTanks; }
	@Override public FluidTank[] getSendingTanks() { return outputTanks; }
	@Override public FluidTank[] getAllTanks() { return allTanks; }

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineAssemblyFactory(id, inv, this);
	}

	@Override public boolean hasPermission(Player player) { return this.stillValid(player); }

	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("index") && data.contains("selection")) {
			int index = data.getInt("index");
			String selection = data.getString("selection");
			if(index >= 0 && index < 4) {
				this.assemblerModule[index].setRecipe(selection, false);
				this.markChanged();
			}
		}
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 2, worldPosition.getY(), worldPosition.getZ() - 2, worldPosition.getX() + 3, worldPosition.getY() + 3, worldPosition.getZ() + 3);
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_assembly_factory.get()));
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

	private DirPos[] coolantLine;

	/** The dummies in front of the coolant ports only hand out the coolant tanks (and power), not the recipe fluids */
	@Override
	public Object getDelegateForPosition(BlockPos pos) {
		if(coolantLine == null) {
			Direction dir = BlockDummyable.getRotation(getBlockState());
			Direction rot = dir.getClockWise();
			BlockPos p = worldPosition;
			coolantLine = new DirPos[] {
					new DirPos(p.relative(rot, 1).relative(dir, 2), dir),
					new DirPos(p.relative(rot, -1).relative(dir, 2), dir),
					new DirPos(p.relative(rot, 1).relative(dir, -2), dir.getOpposite()),
					new DirPos(p.relative(rot, -1).relative(dir, -2), dir.getOpposite()),
			};
		}
		for(DirPos line : coolantLine) if(line.equals(pos)) return this.delegate;
		return null;
	}

	public class DelegateAssemblyFactory implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2 {
		@Override public long getPower() { return TileEntityMachineAssemblyFactory.this.getPower(); }
		@Override public void setPower(long power) { TileEntityMachineAssemblyFactory.this.setPower(power); }
		@Override public long getMaxPower() { return TileEntityMachineAssemblyFactory.this.getMaxPower(); }
		@Override public boolean isLoaded() { return TileEntityMachineAssemblyFactory.this.isLoaded(); }
		@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {TileEntityMachineAssemblyFactory.this.water}; }
		@Override public FluidTank[] getSendingTanks() { return new FluidTank[] {TileEntityMachineAssemblyFactory.this.lps}; }
		@Override public FluidTank[] getAllTanks() { return TileEntityMachineAssemblyFactory.this.getAllTanks(); }
	}

	/**
	 * Carriage consisting of two arms - a striker and a saw
	 * Movement of both arms is inverted, one pedestal can only be serviced by one arm at a time
	 *
	 * Arms cycle through REPOSITION -> EXTEND -> CUT (if saw) -> RETRACT
	 * If transit is planned, the carriage's state will change to RETIRING
	 * If the carriage is RETIRING, each arm will enter RETIRE state after RETRACT
	 * Once the arm has returned to null position, it changes to WAIT
	 * If both arms WAIT, the carriage switches to SLIDING
	 * Once transit is done, carriage returns to WORKING
	 * If the carriage is WORKING, any arm that is in the WAIT state will return to REPOSITION
	 *
	 * @author hbm
	 */
	public class TragicYuri {

		public AssemblerArm striker;
		public AssemblerArm saw;

		Random rand = new Random();
		YuriState state = YuriState.WORKING;
		double slider = 0;
		double prevSlider = 0;
		boolean direction = false;
		int timeUntilReposition;

		public TragicYuri(int group) {
			striker = new AssemblerArm(group == 0 ? 0 : 3);
			saw = new AssemblerArm(group == 0 ? 1 : 2).yepThatsASaw();
			timeUntilReposition = 140 + rand.nextInt(161);
		}

		public void update(boolean working) {
			this.prevSlider = this.slider;

			// one of the arms must do something. doesn't matter which or what position the carriage is in
			if(didProcess[striker.recipeIndex] || didProcess[saw.recipeIndex]) switch(state) {
			case WORKING: {
				timeUntilReposition--;
				if(timeUntilReposition <= 0) {
					state = YuriState.RETIRING;
				}
			} break;
			case RETIRING: {
				if(striker.state == ArmState.WAIT && saw.state == ArmState.WAIT) { // only progress as soon as both arms are done moving
					state = YuriState.SLIDING;
					direction = !direction;
					if(!muffled) playSoundClient("block.assemblerStart", getVolume(0.25F), 1.25F + level.random.nextFloat() * 0.25F);
				}
			} break;
			case SLIDING: {
				double sliderSpeed = 1D / 10D; // 10 ticks for transit
				if(direction) {
					slider += sliderSpeed;
					if(slider >= 1) {
						slider = 1;
						state = YuriState.WORKING;
					}
				} else {
					slider -= sliderSpeed;
					if(slider <= 0) {
						slider = 0;
						state = YuriState.WORKING;
					}
				}
				if(state == YuriState.WORKING) timeUntilReposition = 140 + rand.nextInt(161); // 7 to 15 seconds
			} break;
			}

			striker.updateArm();
			saw.updateArm();
		}

		public double getSlider(float interp) {
			return this.prevSlider + (this.slider - this.prevSlider) * interp;
		}

		public class AssemblerArm {

			public double[] angles = new double[4];
			public double[] prevAngles = new double[4];
			public double[] targetAngles = new double[4];
			public double[] speed = new double[4];
			public double sawAngle;
			public double prevSawAngle;

			public int recipeIndex; // the index of which pedestal is serviced, assuming the carriage is at default position

			ArmState state = ArmState.REPOSITION;
			int actionDelay = 0;
			boolean saw = false;

			public AssemblerArm(int index) {
				this.recipeIndex = index;
				this.resetSpeed();
				this.chooseNewArmPosition();
			}

			public AssemblerArm yepThatsASaw() { this.saw = true; this.chooseNewArmPosition(); return this; }

			private void resetSpeed() {
				speed[0] = 15;	//Pivot
				speed[1] = 15;	//Arm
				speed[2] = 15;	//Piston
				speed[3] = saw ? 0.125 : 0.5;	//Striker
			}

			public void updateArm() {
				resetSpeed();

				for(int i = 0; i < angles.length; i++) {
					prevAngles[i] = angles[i];
				}
				prevSawAngle = sawAngle;

				int serviceIndex = recipeIndex;
				if(slider > 0.5) serviceIndex += (serviceIndex % 2 == 0 ? 1 : -1); // if the carriage has moved, swap the indices so they match up with the serviced pedestal
				if(!didProcess[serviceIndex]) state = ArmState.RETIRE;

				if(state == ArmState.CUT || state == ArmState.EXTEND) {
					this.sawAngle += 45D;
				}

				if(actionDelay > 0) {
					actionDelay--;
					return;
				}

				switch(state) {
				// Move. If done moving, set a delay and progress to EXTEND
				case REPOSITION: {
					if(move()) {
						actionDelay = 2;
						state = ArmState.EXTEND;
						targetAngles[3] = saw ? -0.375D : -0.75D;
					}
				} break;
				case EXTEND:
					if(move()) {
						if(saw) {
							state = ArmState.CUT;
							targetAngles[2] = -targetAngles[2];
							if(!muffled) playSoundClient("block.assemblerCut", getVolume(0.5F), 1F + rand.nextFloat() * 0.25F);
						} else {
							state = ArmState.RETRACT;
							targetAngles[3] = 0D;
							if(!muffled) playSoundClient("block.assemblerStrike", getVolume(0.5F), 1F);
						}
					}
					break;
				case CUT: {
					speed[2] = Math.abs(targetAngles[2] / 20D);
					if(move()) {
						state = ArmState.RETRACT;
						targetAngles[3] = 0D;
					}
				} break;
				case RETRACT:
					if(move()) {
						actionDelay = 2 + rand.nextInt(5);
						chooseNewArmPosition();
						state = TragicYuri.this.state == YuriState.RETIRING ? ArmState.RETIRE : ArmState.REPOSITION;
					}
					break;
				case RETIRE: {
					this.targetAngles[0] = 0;
					this.targetAngles[1] = 0;
					this.targetAngles[2] = 0;
					this.targetAngles[3] = 0;
					if(move()) {
						actionDelay = 2 + rand.nextInt(5);
						chooseNewArmPosition();
						state = ArmState.WAIT;
					}
				} break;
				case WAIT: {
					if(TragicYuri.this.state == YuriState.WORKING) this.state = ArmState.REPOSITION;
				} break;
				}
			}

			public void chooseNewArmPosition() {

				double[][] pos = !saw ? new double[][] {
					// striker
					{10, 10, -10},
					{15, 15, -15},
					{25, 10, -15},
					{30, 0, -10},
					{-10, 10, 0},
					{-20, 30, -15}
				} : new double[][] {
					// saw
					{-15, 15, -10},
					{-15, 15, -15},
					{-15, 15, 10},
					{-15, 15, 15},
					{-15, 15, 2},
					{-15, 15, -2}
				};

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
						BobMathUtil.interp(this.prevAngles[3], this.angles[3], interp),
						BobMathUtil.interp(this.prevSawAngle, this.sawAngle, interp)
				};
			}
		}
	}

	public static enum YuriState {
		WORKING,
		RETIRING, // waiting for arms to enter WAITING state
		SLIDING // transit to next position
	}

	public static enum ArmState {
		REPOSITION,
		EXTEND,
		CUT,
		RETRACT,
		RETIRE, // return to null position for carriage transit
		WAIT // either waiting for or in the middle of carriage transit
	}
}
