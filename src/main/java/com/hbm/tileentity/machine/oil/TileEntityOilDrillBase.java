package com.hbm.tileentity.machine.oil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.ModDataComponents;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Oil drills (derrick, pumpjack): drill down to an oil deposit leaving oil_pipe blocks, then pump oil (and gas)
 * out of connected ore_oil. Slots: 0 battery, 1/2 oil containers in/out, 3/4 gas containers in/out, 5-7 upgrades.
 *
 * TODO machine config file (IConfigurableMachine), copy/paste
 */
public abstract class TileEntityOilDrillBase extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider {

	public int indicator = 0;

	public long power;

	public FluidTank[] tanks;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public TileEntityOilDrillBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state, 8);
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.OIL, 64_000);
		tanks[1] = new FluidTank(Fluids.GAS, 64_000);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);

		this.power = nbt.getLong("power");
		for(int i = 0; i < this.tanks.length; i++)
			this.tanks[i].readFromNBT(nbt, "t" + i);
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);

		nbt.putLong("power", power);
		for(int i = 0; i < this.tanks.length; i++)
			this.tanks[i].writeToNBT(nbt, "t" + i);
	}

	/// IPersistentNBT: power and tanks stay with the dropped machine ///

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);

		boolean empty = power == 0;
		for(FluidTank tank : tanks) if(tank.getFill() > 0) empty = false;
		if(empty) return;

		CompoundTag nbt = new CompoundTag();
		nbt.putLong("power", power);
		for(int i = 0; i < this.tanks.length; i++) this.tanks[i].writeToNBT(nbt, "t" + i);
		components.set(ModDataComponents.PERSISTENT.get(), CustomData.of(nbt));
	}

	@Override
	protected void applyImplicitComponents(DataComponentInput input) {
		super.applyImplicitComponents(input);
		CustomData data = input.get(ModDataComponents.PERSISTENT.get());
		if(data == null) return;
		CompoundTag nbt = data.copyTag();
		this.power = nbt.getLong("power");
		for(int i = 0; i < this.tanks.length; i++) this.tanks[i].readFromNBT(nbt, "t" + i);
	}

	public int speedLevel;
	public int energyLevel;
	public int overLevel;

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.updateConnections();

			this.tanks[0].unloadTank(1, 2, slots);
			this.tanks[1].unloadTank(3, 4, slots);

			upgradeManager.checkSlots(slots, 5, 7);
			this.speedLevel = upgradeManager.getLevel(UpgradeType.SPEED);
			this.energyLevel = upgradeManager.getLevel(UpgradeType.POWER);
			this.overLevel = upgradeManager.getLevel(UpgradeType.OVERDRIVE) + 1;
			int abLevel = upgradeManager.getLevel(UpgradeType.AFTERBURN);

			int toBurn = Math.min(tanks[1].getFill(), abLevel * 10);

			if(toBurn > 0) {
				tanks[1].setFill(tanks[1].getFill() - toBurn);
				this.power += toBurn * 5;

				if(this.power > this.getMaxPower())
					this.power = this.getMaxPower();
			}

			power = Library.chargeTEFromItems(slots, 0, power, this.getMaxPower());

			for(DirPos pos : getConPos()) {
				if(tanks[0].getFill() > 0) this.tryProvide(tanks[0], level, pos, pos.getDir());
				if(tanks[1].getFill() > 0) this.tryProvide(tanks[1], level, pos, pos.getDir());
			}

			if(this.power >= this.getPowerReqEff() && this.tanks[0].getFill() < this.tanks[0].getMaxFill() && this.tanks[1].getFill() < this.tanks[1].getMaxFill()) {

				this.power -= this.getPowerReqEff();

				if(level.getGameTime() % getDelayEff() == 0) {
					this.indicator = 0;

					for(int y = worldPosition.getY() - 1; y >= getDrillDepth(); y--) {

						if(!level.getBlockState(pos(y)).is(ModBlocks.oil_pipe.get())) {

							if(trySuck(y)) {
								break;
							} else {
								tryDrill(y);
								break;
							}
						}

						if(y == getDrillDepth())
							this.indicator = 1;
					}
				}

			} else {
				this.indicator = 2;
			}

			this.networkPackNT(25);
		}
	}

	protected BlockPos pos(int y) {
		return new BlockPos(worldPosition.getX(), y, worldPosition.getZ());
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		buf.writeLong(this.power);
		buf.writeInt(this.indicator);
		for(FluidTank tank : tanks) tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		this.power = buf.readLong();
		this.indicator = buf.readInt();
		for(FluidTank tank : tanks) tank.deserialize(buf);
	}

	public boolean canPump() {
		return true;
	}

	@Override
	public void setItem(int i, ItemStack stack) {
		super.setItem(i, stack);

		if(level != null && !stack.isEmpty() && i >= 5 && i <= 7 && stack.getItem() instanceof ItemMachineUpgrade)
			level.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 1.5, worldPosition.getZ() + 0.5, ModSounds.get("item.upgradePlug"), SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	public int getPowerReqEff() {
		int req = this.getPowerReq();
		return (req + (req / 4 * this.speedLevel) - (req / 4 * this.energyLevel)) * this.overLevel;
	}

	public int getDelayEff() {
		int delay = getDelay();
		return Math.max((delay - (delay / 4 * this.speedLevel) + (delay / 10 * this.energyLevel)) / this.overLevel, 1);
	}

	public abstract int getPowerReq();
	public abstract int getDelay();

	public void tryDrill(int y) {
		BlockState b = level.getBlockState(pos(y));

		// the original compared the explosion resistance against 1000 (unbreakable blocks like bedrock)
		if(b.getBlock().getExplosionResistance() < 1000 && b.getDestroySpeed(level, pos(y)) >= 0) {
			onDrill(y);
			level.setBlock(pos(y), ModBlocks.oil_pipe.get().defaultBlockState(), Block.UPDATE_ALL);
		} else {
			this.indicator = 2;
		}
	}

	public void onDrill(int y) { }

	/** The original drilled down to y 5 (just above bedrock), relative to the world bottom here */
	public int getDrillDepth() {
		return level.getMinBuildHeight() + 5;
	}

	public boolean trySuck(int y) {

		BlockState b = level.getBlockState(pos(y));

		if(!canSuckBlock(b))
			return false;

		if(!this.canPump())
			return true;

		trace.clear();

		return suckRec(pos(y), 0);
	}

	public boolean canSuckBlock(BlockState b) {
		return b.is(ModBlocks.ore_oil.get()) || b.is(ModBlocks.ore_oil_empty.get());
	}

	protected HashSet<BlockPos> trace = new HashSet<>();

	public boolean suckRec(BlockPos pos, int layer) {

		if(trace.contains(pos))
			return false;

		trace.add(pos);

		if(layer > 64)
			return false;

		BlockState b = level.getBlockState(pos);

		if(b.is(ModBlocks.ore_oil.get()) || b.is(ModBlocks.ore_bedrock_oil.get())) {
			doSuck(pos);
			return true;
		}

		if(b.is(ModBlocks.ore_oil_empty.get())) {
			for(Direction dir : shuffledDirs(level.random)) {
				if(suckRec(pos.relative(dir), layer + 1))
					return true;
			}
		}

		return false;
	}

	/** BobMathUtil.getShuffledDirs */
	protected static List<Direction> shuffledDirs(RandomSource rand) {
		List<Direction> dirs = new java.util.ArrayList<>(List.of(Direction.values()));
		net.minecraft.Util.shuffle(dirs, rand);
		return dirs;
	}

	public void doSuck(BlockPos pos) {

		if(level.getBlockState(pos).is(ModBlocks.ore_oil.get())) {
			onSuck(pos);
		}
	}

	public abstract void onSuck(BlockPos pos);

	@Override
	public void setPower(long i) {
		this.power = i;
	}

	@Override
	public long getPower() {
		return this.power;
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[0];
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	public abstract DirPos[] getConPos();

	protected void updateConnections() {
		for(DirPos pos : getConPos()) {
			this.trySubscribe(level, pos, pos.getDir());
		}
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE || type == UpgradeType.AFTERBURN;
	}

	@Override
	public HashMap<UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.SPEED, 3);
		upgrades.put(UpgradeType.POWER, 3);
		upgrades.put(UpgradeType.AFTERBURN, 3);
		upgrades.put(UpgradeType.OVERDRIVE, 3);
		return upgrades;
	}
}
