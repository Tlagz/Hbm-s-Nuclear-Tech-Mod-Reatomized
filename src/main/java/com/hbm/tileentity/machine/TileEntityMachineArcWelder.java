package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineArcWelder;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.ArcWelderRecipes;
import com.hbm.inventory.recipes.ArcWelderRecipes.ArcWelderRecipe;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.IConditionalInvAccess;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.BufferUtil;
import com.hbm.util.DirPos;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Arc welder: combines up to 3 inputs (plus an optional fluid) into parts like motors, dense wires and welded plates.
 * Slots: 0-2 inputs, 3 output, 4 battery, 5 fluid identifier, 6-7 upgrades. Each input slot has its own port
 * (red/yellow/green on the model), see the IConditionalInvAccess methods.
 *
 * TODO the original's spark and hadron particles (ParticleSpark, ParticleHadron) instead of vanilla sparks, copy/paste
 */
public class TileEntityMachineArcWelder extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardReceiverMK2, IConditionalInvAccess, IUpgradeInfoProvider, MenuProvider {

	public long power;
	public long maxPower = 2_000;
	public long consumption;

	public int progress;
	public int processTime = 1;

	public FluidTank tank;
	public ItemStack display = ItemStack.EMPTY;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public TileEntityMachineArcWelder(BlockPos pos, BlockState state) {
		super(ModTileEntities.ARC_WELDER.get(), pos, state, 8);
		this.tank = new FluidTank(Fluids.NONE, 24_000);
	}

	@Override
	public String getName() {
		return "container.machineArcWelder";
	}

	@Override
	public void setItem(int i, ItemStack stack) {
		super.setItem(i, stack);

		if(level != null && !stack.isEmpty() && stack.getItem() instanceof ItemMachineUpgrade && i >= 6 && i <= 7) {
			level.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, ModSounds.get("item.upgradePlug"), SoundSource.BLOCKS, 1.0F, 1.0F);
		}
	}

	private Direction getDir() {
		return Direction.from3DDataValue(BlockDummyable.getMeta(getBlockState()) - BlockDummyable.offset);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.power = Library.chargeTEFromItems(slots, 4, this.getPower(), this.getMaxPower());
			this.tank.setType(5, slots);

			if(level.getGameTime() % 20 == 0) {
				for(DirPos pos : getConPos()) {
					this.trySubscribe(level, pos);
					if(tank.getTankType() != Fluids.NONE) this.trySubscribe(tank.getTankType(), level, pos);
				}
			}

			ArcWelderRecipe recipe = ArcWelderRecipes.getRecipe(slots.get(0), slots.get(1), slots.get(2));
			long intendedMaxPower;

			upgradeManager.checkSlots(slots, 6, 7);
			int redLevel = upgradeManager.getLevel(UpgradeType.SPEED);
			int blueLevel = upgradeManager.getLevel(UpgradeType.POWER);
			int blackLevel = upgradeManager.getLevel(UpgradeType.OVERDRIVE);

			if(recipe != null) {
				this.processTime = recipe.duration - (recipe.duration * redLevel / 6) + (recipe.duration * blueLevel / 3);
				this.consumption = recipe.consumption + (recipe.consumption * redLevel) - (recipe.consumption * blueLevel / 6);
				this.consumption *= Math.pow(2, blackLevel);
				intendedMaxPower = consumption * 20;

				if(canProcess(recipe)) {
					this.progress += (1 + blackLevel);
					this.power -= this.consumption;

					if(progress >= processTime) {
						this.progress = 0;
						this.consumeItems(recipe);

						if(slots.get(3).isEmpty()) {
							slots.set(3, recipe.output.copy());
						} else {
							slots.get(3).grow(recipe.output.getCount());
						}

						this.setChanged();
					}

					if(level.getGameTime() % 2 == 0 && level instanceof ServerLevel server) {
						Direction dir = getDir();
						server.sendParticles(ParticleTypes.ELECTRIC_SPARK, worldPosition.getX() + 0.5 - dir.getStepX() * 0.5, worldPosition.getY() + 1.25, worldPosition.getZ() + 0.5 - dir.getStepZ() * 0.5, 5, 0.05, 0.05, 0.05, 0.1);
					}

				} else {
					this.progress = 0;
				}

			} else {
				this.progress = 0;
				this.consumption = 100;
				intendedMaxPower = 2000;
			}

			this.maxPower = Math.max(intendedMaxPower, power);

			this.networkPackNT(25);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeLong(maxPower);
		buf.writeLong(consumption);
		buf.writeInt(progress);
		buf.writeInt(processTime);

		tank.serialize(buf);

		ArcWelderRecipe recipe = ArcWelderRecipes.getRecipe(slots.get(0), slots.get(1), slots.get(2));
		buf.writeBoolean(recipe != null);
		if(recipe != null) BufferUtil.writeItemStack(buf, recipe.output.copyWithCount(1), level.registryAccess());
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		maxPower = buf.readLong();
		consumption = buf.readLong();
		progress = buf.readInt();
		processTime = buf.readInt();

		tank.deserialize(buf);

		this.display = buf.readBoolean() ? BufferUtil.readItemStack(buf, level.registryAccess()) : ItemStack.EMPTY;
	}

	public boolean canProcess(ArcWelderRecipe recipe) {

		if(this.power < this.consumption) return false;

		if(recipe.fluid != null) {
			if(this.tank.getTankType() != recipe.fluid.type) return false;
			if(this.tank.getFill() < recipe.fluid.fill) return false;
		}

		ItemStack out = slots.get(3);
		if(!out.isEmpty()) {
			if(!ItemStack.isSameItemSameComponents(out, recipe.output)) return false;
			if(out.getCount() + recipe.output.getCount() > out.getMaxStackSize()) return false;
		}

		return true;
	}

	public void consumeItems(ArcWelderRecipe recipe) {

		for(AStack aStack : recipe.ingredients) {

			for(int i = 0; i < 3; i++) {
				ItemStack stack = slots.get(i);
				if(aStack.matchesRecipe(stack, true) && stack.getCount() >= aStack.stacksize) {
					this.removeItem(i, aStack.stacksize);
					break;
				}
			}
		}

		if(recipe.fluid != null) {
			this.tank.setFill(tank.getFill() - recipe.fluid.fill);
		}
	}

	protected DirPos[] getConPos() {

		Direction dir = getDir();
		Direction rot = dir.getClockWise();
		BlockPos p = worldPosition;

		return new DirPos[] {
				new DirPos(p.relative(dir), dir),
				new DirPos(p.relative(dir).relative(rot), dir),
				new DirPos(p.relative(dir).relative(rot.getOpposite()), dir),
				new DirPos(p.relative(dir, -2), dir.getOpposite()),
				new DirPos(p.relative(dir, -2).relative(rot), dir.getOpposite()),
				new DirPos(p.relative(dir, -2).relative(rot.getOpposite()), dir.getOpposite()),
				new DirPos(p.relative(rot, 2), rot),
				new DirPos(p.relative(dir, -1).relative(rot, 2), rot),
				new DirPos(p.relative(rot, -2), rot.getOpposite()),
				new DirPos(p.relative(dir, -1).relative(rot, -2), rot.getOpposite())
		};
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);

		this.power = nbt.getLong("power");
		this.maxPower = nbt.getLong("maxPower");
		this.progress = nbt.getInt("progress");
		this.processTime = nbt.getInt("processTime");
		tank.readFromNBT(nbt, "t");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);

		nbt.putLong("power", power);
		nbt.putLong("maxPower", maxPower);
		nbt.putInt("progress", progress);
		nbt.putInt("processTime", processTime);
		tank.writeToNBT(nbt, "t");
	}

	@Override
	public long getPower() {
		return Math.max(Math.min(power, maxPower), 0);
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] {tank};
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {tank};
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return slot < 3;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, Direction side) {
		return slot == 3;
	}

	private static final int[] slot_access = new int[] { 1, 3 };

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return slot_access;
	}

	/// IConditionalInvAccess: every input slot has its own ports ///

	@Override
	public boolean isItemValidForSlot(BlockPos pos, int slot, ItemStack stack) {
		return slot < 3;
	}

	@Override
	public boolean canInsertItem(BlockPos pos, int slot, ItemStack stack, Direction side) {
		return slot < 3;
	}

	@Override
	public boolean canExtractItem(BlockPos pos, int slot, ItemStack stack, Direction side) {
		return slot == 3;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(BlockPos pos, Direction side) {
		Direction dir = getDir();
		Direction rot = dir.getClockWise();
		BlockPos core = worldPosition;

		//Red
		if(pos.equals(core.relative(rot)) || pos.equals(core.relative(rot.getOpposite()).relative(dir.getOpposite())))
			return new int[] { 0, 3 };

		//Yellow
		if(pos.equals(core.relative(dir.getOpposite())))
			return new int[] { 1, 3 };

		//Green
		if(pos.equals(core.relative(rot.getOpposite())) || pos.equals(core.relative(rot).relative(dir.getOpposite())))
			return new int[] { 2, 3 };

		return new int[] { };
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineArcWelder(id, inv, this);
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
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_arc_welder.get()));
		if(type == UpgradeType.SPEED) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_DELAY, "-" + (level * 100 / 6) + "%"));
			info.add(ChatFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + (level * 100) + "%"));
		}
		if(type == UpgradeType.POWER) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_CONSUMPTION, "-" + (level * 100 / 6) + "%"));
			info.add(ChatFormatting.RED + I18nUtil.resolveKey(KEY_DELAY, "+" + (level * 100 / 3) + "%"));
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
