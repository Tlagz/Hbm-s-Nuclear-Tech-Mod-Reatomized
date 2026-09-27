package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineCyclotron;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.CyclotronRecipes;
import com.hbm.inventory.recipes.CyclotronRecipes.Result;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.IConditionalInvAccess;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Cyclotron: three lanes each shooting a particle (slots 0-2) at a target (3-5) for an output (6-8) plus some
 * antimatter, cooled with water that comes out as spent steam. Slot 9 battery, 10-11 upgrades. The four plug
 * sockets take the balefire powder, the book of boxcars, the diamond gavel and the maskman coin (just decoration).
 * The sides of the ring are lane-specific item ports.
 */
public class TileEntityMachineCyclotron extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IConditionalInvAccess, IUpgradeInfoProvider, MenuProvider {

	public long power;
	public static final long maxPower = 100000000;
	public static int consumption = 1_000_000;

	private byte plugs;

	public int progress;
	public static final int duration = 690;

	public FluidTank[] tanks;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public TileEntityMachineCyclotron(BlockPos pos, BlockState state) {
		super(ModTileEntities.CYCLOTRON.get(), pos, state, 12);

		this.tanks = new FluidTank[3];
		this.tanks[0] = new FluidTank(Fluids.WATER, 32000);
		this.tanks[1] = new FluidTank(Fluids.SPENTSTEAM, 32000);
		this.tanks[2] = new FluidTank(Fluids.AMAT, 8000);
	}

	@Override
	public String getName() {
		return "container.cyclotron";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.power = Library.chargeTEFromItems(slots, 9, power, maxPower);
			this.autoPort(getConPos());

			upgradeManager.checkSlots(slots, 10, 11);

			if(canProcess()) {
				progress += getSpeed();
				power -= getConsumption();

				int convert = getCoolantConsumption();
				tanks[0].setFill(tanks[0].getFill() - convert);
				tanks[1].setFill(tanks[1].getFill() + convert);

				if(progress >= duration) {
					process();
					progress = 0;
					this.setChanged();
				}

			} else {
				progress = 0;
			}

			this.networkPackNT(25);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(progress);
		buf.writeByte(plugs);

		for(int i = 0; i < 3; i++)
			tanks[i].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		progress = buf.readInt();
		plugs = buf.readByte();

		for(int i = 0; i < 3; i++)
			tanks[i].deserialize(buf);
	}

	public DirPos[] getConPos() {
		BlockPos p = worldPosition;
		return new DirPos[] {
				new DirPos(p.offset(3, 0, 1), Direction.EAST),
				new DirPos(p.offset(3, 0, -1), Direction.EAST),
				new DirPos(p.offset(-3, 0, 1), Direction.WEST),
				new DirPos(p.offset(-3, 0, -1), Direction.WEST),
				new DirPos(p.offset(1, 0, 3), Direction.SOUTH),
				new DirPos(p.offset(-1, 0, 3), Direction.SOUTH),
				new DirPos(p.offset(1, 0, -3), Direction.NORTH),
				new DirPos(p.offset(-1, 0, -3), Direction.NORTH)
		};
	}

	public boolean canProcess() {

		if(power < getConsumption())
			return false;

		int convert = getCoolantConsumption();

		if(tanks[0].getFill() < convert)
			return false;

		if(tanks[1].getFill() + convert > tanks[1].getMaxFill())
			return false;

		for(int i = 0; i < 3; i++) {

			Result res = CyclotronRecipes.getOutput(slots.get(i + 3), slots.get(i));

			if(res == null)
				continue;

			ItemStack out = res.output();

			if(slots.get(i + 6).isEmpty())
				return true;

			if(ItemStack.isSameItemSameComponents(slots.get(i + 6), out) && slots.get(i + 6).getCount() < out.getMaxStackSize())
				return true;
		}

		return false;
	}

	public void process() {

		for(int i = 0; i < 3; i++) {

			Result res = CyclotronRecipes.getOutput(slots.get(i + 3), slots.get(i));

			if(res == null)
				continue;

			ItemStack out = res.output();

			if(slots.get(i + 6).isEmpty()) {

				this.removeItem(i, 1);
				this.removeItem(i + 3, 1);
				slots.set(i + 6, out);

				this.tanks[2].setFill(this.tanks[2].getFill() + res.antimatter());

				continue;
			}

			if(ItemStack.isSameItemSameComponents(slots.get(i + 6), out) && slots.get(i + 6).getCount() < out.getMaxStackSize()) {

				this.removeItem(i, 1);
				this.removeItem(i + 3, 1);
				slots.get(i + 6).grow(1);

				this.tanks[2].setFill(this.tanks[2].getFill() + res.antimatter());
			}
		}

		if(this.tanks[2].getFill() > this.tanks[2].getMaxFill())
			this.tanks[2].setFill(this.tanks[2].getMaxFill());
	}

	public int getSpeed() {
		return upgradeManager.getLevel(UpgradeType.SPEED) + 1;
	}

	public int getConsumption() {
		int efficiency = upgradeManager.getLevel(UpgradeType.POWER);
		return consumption - 100_000 * efficiency;
	}

	public int getCoolantConsumption() {
		int efficiency = upgradeManager.getLevel(UpgradeType.EFFECT);
		//half a small tower's worth
		return 500 / (efficiency + 1) * getSpeed();
	}

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	public int getProgressScaled(int i) {
		return (progress * i) / duration;
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 2, worldPosition.getY(), worldPosition.getZ() - 2, worldPosition.getX() + 3, worldPosition.getY() + 4, worldPosition.getZ() + 3);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);

		for(int i = 0; i < 3; i++)
			tanks[i].readFromNBT(nbt, "t" + i);

		this.progress = nbt.getInt("progress");
		this.power = nbt.getLong("power");
		this.plugs = nbt.getByte("plugs");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);

		for(int i = 0; i < 3; i++)
			tanks[i].writeToNBT(nbt, "t" + i);

		nbt.putInt("progress", progress);
		nbt.putLong("power", power);
		nbt.putByte("plugs", plugs);
	}

	public void setPlug(int index) {
		this.plugs |= (1 << index);
		this.setChanged();
	}

	public boolean getPlug(int index) {
		return (this.plugs & (1 << index)) > 0;
	}

	public static Item getItemForPlug(int i) {

		switch(i) {
		case 0: return ModItems.powder_balefire.get();
		case 1: return ModItems.book_of_.get();
		case 2: return ModItems.diamond_gavel.get();
		case 3: return ModItems.coin_maskman.get();
		}

		return null;
	}

	@Override
	public void setItem(int i, ItemStack stack) {
		super.setItem(i, stack);

		// the original checked slots 14-15, which don't exist; the upgrade slots are 10 and 11
		if(!stack.isEmpty() && i >= 10 && i <= 11 && stack.getItem() instanceof ItemMachineUpgrade && level != null && !level.isClientSide)
			level.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 1.5, worldPosition.getZ() + 0.5, ModSounds.get("item.upgradePlug"), SoundSource.BLOCKS, 1.5F, 1.0F);
	}

	@Override public void setPower(long i) { this.power = i; }
	@Override public long getPower() { return this.power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] { tanks[1], tanks[2] }; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { tanks[0] }; }
	@Override public FluidTank[] getAllTanks() { return tanks; }

	/// SLOT ACCESS ///

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {

		if(slot < 3) {
			for(CyclotronRecipes.Recipe recipe : CyclotronRecipes.recipes) {
				if(recipe.key().particle().matchesRecipe(stack, true)) return true;
			}
		} else if(slot < 6) {
			for(CyclotronRecipes.Recipe recipe : CyclotronRecipes.recipes) {
				if(recipe.key().input().matchesRecipe(stack, true)) return true;
			}
		}

		return false;
	}

	@Override
	public boolean isItemValidForSlot(BlockPos pos, int slot, ItemStack stack) {
		return isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(BlockPos pos, int slot, ItemStack stack, Direction side) {
		return slot >= 6 && slot <= 8;
	}

	/** Every side of the ring feeds one lane per block, all of them can take the outputs */
	@Override
	public int[] getAccessibleSlotsFromSide(BlockPos pos, Direction side) {

		for(Direction dir : Direction.Plane.HORIZONTAL) {
			Direction rot = dir.getClockWise();
			BlockPos edge = worldPosition.relative(dir, 2);

			if(pos.getX() == edge.getX() + rot.getStepX() && pos.getZ() == edge.getZ() + rot.getStepZ()) return new int[] {0, 3, 6, 7, 8};
			if(pos.getX() == edge.getX() && pos.getZ() == edge.getZ()) return new int[] {1, 4, 6, 7, 8};
			if(pos.getX() == edge.getX() - rot.getStepX() && pos.getZ() == edge.getZ() - rot.getStepZ()) return new int[] {2, 5, 6, 7, 8};
		}

		return new int[] {6, 7, 8};
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i >= 6 && i <= 8;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] {6, 7, 8};
	}

	/// UPGRADES ///

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.EFFECT;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_cyclotron.get()));
		if(type == UpgradeType.SPEED) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_DELAY, "-" + (100 - 100 / (level + 1)) + "%"));
			info.add(ChatFormatting.RED + I18nUtil.resolveKey(KEY_COOLANT_CONSUMPTION, "+" + (level * 100) + "%"));
		}
		if(type == UpgradeType.POWER) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_CONSUMPTION, "-" + (level * 10) + "%"));
		}
		if(type == UpgradeType.EFFECT) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_COOLANT_CONSUMPTION, "-" + (100 - 100 / (level + 1)) + "%"));
		}
	}

	@Override
	public HashMap<UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.SPEED, 3);
		upgrades.put(UpgradeType.POWER, 3);
		upgrades.put(UpgradeType.EFFECT, 3);
		return upgrades;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineCyclotron(id, inv, this);
	}
}
