package com.hbm.tileentity.machine.oil;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerSolidifier;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.SolidificationRecipes;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.DirPos;
import com.hbm.util.Tuple.Pair;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
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
 * Solidifier: turns a fluid into items. Slots: 0 output, 1 battery, 2/3 upgrades (speed, power), 4 fluid identifier.
 *
 * TODO copy tool
 */
public class TileEntityMachineSolidifier extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardReceiverMK2, IUpgradeInfoProvider, MenuProvider {

	public long power;
	public static final long maxPower = 100_000;
	public static final int usageBase = 250;
	public int usage;
	public int progress;
	public static final int processTimeBase = 60;
	public int processTime;

	public FluidTank tank;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public TileEntityMachineSolidifier(BlockPos pos, BlockState state) {
		super(ModTileEntities.SOLIDIFIER.get(), pos, state, 5);
		tank = new FluidTank(Fluids.NONE, 24_000);
	}

	@Override
	public String getName() {
		return "container.machineSolidifier";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {
			this.power = Library.chargeTEFromItems(slots, 1, power, maxPower);

			for(DirPos pos : getConPos()) {
				this.trySubscribe(level, pos, pos.getDir());
				this.trySubscribe(tank.getTankType(), level, pos, pos.getDir());
			}

			tank.setType(4, slots);

			upgradeManager.checkSlots(slots, 2, 3);
			int speed = upgradeManager.getLevel(UpgradeType.SPEED);
			int power = upgradeManager.getLevel(UpgradeType.POWER);

			this.processTime = processTimeBase - (processTimeBase / 4) * speed;
			this.usage = (usageBase + (usageBase * speed)) / (power + 1);

			if(this.canProcess())
				this.process();
			else
				this.progress = 0;

			this.networkPackNT(50);
		}
	}

	/** Top, bottom and one on each side of the second layer */
	protected DirPos[] getConPos() {
		BlockPos p = worldPosition;
		return new DirPos[] {
			new DirPos(p.offset(0, 4, 0), Direction.UP),
			new DirPos(p.offset(0, -1, 0), Direction.DOWN),
			new DirPos(p.offset(2, 1, 0), Direction.EAST),
			new DirPos(p.offset(-2, 1, 0), Direction.WEST),
			new DirPos(p.offset(0, 1, 2), Direction.SOUTH),
			new DirPos(p.offset(0, 1, -2), Direction.NORTH)
		};
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, Direction side) {
		return slot == 0;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] { 0 };
	}

	public boolean canProcess() {
		if(this.power < usage) return false;

		Pair<Integer, ItemStack> out = SolidificationRecipes.getOutput(tank.getTankType());

		if(out == null) return false;

		int req = out.getKey();
		ItemStack stack = out.getValue();

		if(req > tank.getFill()) return false;

		ItemStack slot = slots.get(0);
		if(!slot.isEmpty()) {
			if(!ItemStack.isSameItemSameComponents(slot, stack)) return false;
			if(slot.getCount() + stack.getCount() > slot.getMaxStackSize()) return false;
		}

		return true;
	}

	public void process() {
		this.power -= usage;

		progress++;

		if(progress >= processTime) {

			Pair<Integer, ItemStack> out = SolidificationRecipes.getOutput(tank.getTankType());
			int req = out.getKey();
			ItemStack stack = out.getValue();
			tank.setFill(tank.getFill() - req);

			if(slots.get(0).isEmpty()) {
				slots.set(0, stack.copy());
			} else {
				slots.get(0).grow(stack.getCount());
			}

			progress = 0;
			this.setChanged();
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(this.power);
		buf.writeInt(this.progress);
		buf.writeInt(this.usage);
		buf.writeInt(this.processTime);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.progress = buf.readInt();
		this.usage = buf.readInt();
		this.processTime = buf.readInt();
		tank.deserialize(buf);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		tank.readFromNBT(nbt, "tank");
		this.power = nbt.getLong("power");
		this.progress = nbt.getInt("progress");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		tank.writeToNBT(nbt, "tank");
		nbt.putLong("power", power);
		nbt.putInt("progress", progress);
	}

	@Override public void setPower(long power) { this.power = power; }
	@Override public long getPower() { return power; }
	@Override public long getMaxPower() { return maxPower; }

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 4, worldPosition.getZ() + 2);
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] { tank };
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] { tank };
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.POWER;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_solidifier.get()));
		if(type == UpgradeType.SPEED) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_DELAY, "-" + (level * 25) + "%"));
			info.add(ChatFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + (level * 100) + "%"));
		}
		if(type == UpgradeType.POWER) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_CONSUMPTION, "-" + (100 - 100 / (level + 1)) + "%"));
		}
	}

	@Override
	public HashMap<UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.SPEED, 3);
		upgrades.put(UpgradeType.POWER, 3);
		return upgrades;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerSolidifier(id, inv, this);
	}
}
