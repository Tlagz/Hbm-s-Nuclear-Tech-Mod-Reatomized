package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineChemicalFactory;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.module.machine.ModuleMachineChemplant;
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
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Chemical factory: four chemical plant recipe fields in one 5x5 block, needs water for cooling (which comes out as
 * low pressure steam). Slots: 0 battery, 1-3 upgrades, then per field (i * 7): 4 blueprint, 5-7 inputs, 8-10 outputs.
 * The four side ports each feed one field's inputs, the coolant ports only see the coolant tanks.
 *
 * TODO redstone over radio (IRORValueProvider)
 */
public class TileEntityMachineChemicalFactory extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, IControlReceiver, IProxyDelegateProvider, IConditionalInvAccess, MenuProvider {

	public FluidTank[] allTanks;
	public FluidTank[] inputTanks;
	public FluidTank[] outputTanks;

	public FluidTank water;
	public FluidTank lps;

	public long power;
	public long maxPower = 1_000_000;
	public boolean[] didProcess = new boolean[4];

	public boolean frame = false;
	public int anim;
	public int prevAnim;
	private AudioWrapper audio;

	public ModuleMachineChemplant[] chemplantModule;
	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	protected DelegateChemicalFactory delegate = new DelegateChemicalFactory();

	public TileEntityMachineChemicalFactory(BlockPos pos, BlockState state) {
		super(ModTileEntities.CHEMICAL_FACTORY.get(), pos, state, 32);

		this.inputTanks = new FluidTank[12];
		this.outputTanks = new FluidTank[12];
		for(int i = 0; i < 12; i++) {
			this.inputTanks[i] = new FluidTank(Fluids.NONE, 24_000);
			this.outputTanks[i] = new FluidTank(Fluids.NONE, 24_000);
		}

		this.water = new FluidTank(Fluids.WATER, 4_000);
		this.lps = new FluidTank(Fluids.SPENTSTEAM, 4_000);

		this.allTanks = new FluidTank[this.inputTanks.length + this.outputTanks.length + 2];
		for(int i = 0; i < inputTanks.length; i++) this.allTanks[i] = this.inputTanks[i];
		for(int i = 0; i < outputTanks.length; i++) this.allTanks[i + this.inputTanks.length] = this.outputTanks[i];
		this.allTanks[this.allTanks.length - 2] = this.water;
		this.allTanks[this.allTanks.length - 1] = this.lps;

		this.chemplantModule = new ModuleMachineChemplant[4];
		for(int i = 0; i < 4; i++) this.chemplantModule[i] = new ModuleMachineChemplant(i, this, slots)
				.itemInput(5 + i * 7, 6 + i * 7, 7 + i * 7)
				.itemOutput(8 + i * 7, 9 + i * 7, 10 + i * 7)
				.fluidInput(inputTanks[0 + i * 3], inputTanks[1 + i * 3], inputTanks[2 + i * 3])
				.fluidOutput(outputTanks[0 + i * 3], outputTanks[1 + i * 3], outputTanks[2 + i * 3]);
	}

	@Override
	public String getName() {
		return "container.machineChemicalFactory";
	}

	@Override
	public void updateEntity() {

		if(maxPower <= 0) this.maxPower = 10_000_000;

		if(isServer()) {

			long nextMaxPower = 0;
			for(int i = 0; i < 4; i++) {
				GenericRecipe recipe = chemplantModule[i].getRecipe();
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
				this.chemplantModule[i].update(speed * 2D, pow * 2D, canCool(), slots.get(4 + i * 7));
				this.didProcess[i] = this.chemplantModule[i].didProcess;
				markDirty |= this.chemplantModule[i].markDirty;

				if(this.chemplantModule[i].didProcess) {
					this.water.setFill(this.water.getFill() - 100);
					this.lps.setFill(this.lps.getFill() + 100);
				}
			}

			// internal fluid sharing, outputs of one field feed the inputs of the others
			for(FluidTank in : inputTanks) if(in.getTankType() != Fluids.NONE) for(FluidTank out : outputTanks) {
				if(out.getTankType() == Fluids.NONE) continue;
				if(out.getTankType() != in.getTankType()) continue;
				if(out.getPressure() != in.getPressure()) continue;

				int toMove = BobMathUtil.min(in.getMaxFill() - in.getFill(), out.getFill(), 50);
				if(toMove > 0) {
					in.setFill(in.getFill() + toMove);
					out.setFill(out.getFill() - toMove);
				}
			}

			if(markDirty) this.setChanged();

			this.networkPackNT(100);

		} else {

			this.prevAnim = this.anim;
			boolean didSomething = didProcess[0] || didProcess[1] || didProcess[2] || didProcess[3];
			if(didSomething) this.anim++;

			if(level.getGameTime() % 20 == 0) {
				frame = !level.getBlockState(worldPosition.above(3)).isAir();
			}

			if(didSomething) {
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

	public boolean canCool() {
		return water.getFill() >= 100 && lps.getFill() <= lps.getMaxFill() - 100;
	}

	public DirPos[] getConPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		BlockPos p = worldPosition;
		BlockPos top = p.above(3);

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
				new DirPos(top.relative(dir, 2).relative(rot, 2), Direction.UP),
				new DirPos(top.relative(dir, 1).relative(rot, 2), Direction.UP),
				new DirPos(top.relative(rot, 2), Direction.UP),
				new DirPos(top.relative(dir, -1).relative(rot, 2), Direction.UP),
				new DirPos(top.relative(dir, -2).relative(rot, 2), Direction.UP),
				new DirPos(top.relative(dir, 2).relative(rot, -2), Direction.UP),
				new DirPos(top.relative(dir, 1).relative(rot, -2), Direction.UP),
				new DirPos(top.relative(rot, -2), Direction.UP),
				new DirPos(top.relative(dir, -1).relative(rot, -2), Direction.UP),
				new DirPos(top.relative(dir, -2).relative(rot, -2), Direction.UP),
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
		for(int i = 0; i < 4; i++) this.chemplantModule[i].serialize(buf);
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
		for(int i = 0; i < 4; i++) this.chemplantModule[i].deserialize(buf);
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
		for(int i = 0; i < 4; i++) this.chemplantModule[i].readFromNBT(nbt);
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
		for(int i = 0; i < 4; i++) this.chemplantModule[i].writeToNBT(nbt);
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == 0) return true; // battery
		for(int i = 0; i < 4; i++) if(slot == 4 + i * 7 && stack.is(ModItems.blueprints.get())) return true;
		if(slot >= 1 && slot <= 3 && stack.getItem() instanceof ItemMachineUpgrade) return true; // upgrades
		for(int i = 0; i < 4; i++) if(this.chemplantModule[i].isItemValid(slot, stack)) return true; // recipe input crap
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		if(i >= 8 && i <= 10) return true;
		if(i >= 15 && i <= 17) return true;
		if(i >= 22 && i <= 24) return true;
		if(i >= 29 && i <= 31) return true;
		for(int k = 0; k < 4; k++) if(this.chemplantModule[k].isSlotClogged(i)) return true;
		return false;
	}

	private static final int[] slot_access = new int[] {
			5, 6, 7, 8, 9, 10,
			12, 13, 14, 15, 16, 17,
			19, 20, 21, 22, 23, 24,
			26, 27, 28, 29, 30, 31
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
				return new int[] {
						5 + i * 7, 6 + i * 7, 7 + i * 7,
						8, 9, 10,
						15, 16, 17,
						22, 23, 24,
						29, 30, 31
				};
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
		return new ContainerMachineChemicalFactory(id, inv, this);
	}

	@Override public boolean hasPermission(Player player) { return this.stillValid(player); }

	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("index") && data.contains("selection")) {
			int index = data.getInt("index");
			String selection = data.getString("selection");
			if(index >= 0 && index < 4) {
				this.chemplantModule[index].setRecipe(selection, false);
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
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_chemical_factory.get()));
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

	public class DelegateChemicalFactory implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2 {
		@Override public long getPower() { return TileEntityMachineChemicalFactory.this.getPower(); }
		@Override public void setPower(long power) { TileEntityMachineChemicalFactory.this.setPower(power); }
		@Override public long getMaxPower() { return TileEntityMachineChemicalFactory.this.getMaxPower(); }
		@Override public boolean isLoaded() { return TileEntityMachineChemicalFactory.this.isLoaded(); }
		@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {TileEntityMachineChemicalFactory.this.water}; }
		@Override public FluidTank[] getSendingTanks() { return new FluidTank[] {TileEntityMachineChemicalFactory.this.lps}; }
		@Override public FluidTank[] getAllTanks() { return TileEntityMachineChemicalFactory.this.getAllTanks(); }
	}
}
