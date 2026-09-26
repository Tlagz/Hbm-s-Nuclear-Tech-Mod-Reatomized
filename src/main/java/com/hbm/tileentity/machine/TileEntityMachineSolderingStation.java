package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineSolderingStation;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.SolderingRecipes;
import com.hbm.inventory.recipes.SolderingRecipes.SolderingRecipe;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Soldering station: toppings, boards and solder (and sometimes a fluid) into circuits and upgrades. The buffer
 * scales with the recipe's consumption. Slots: 0-2 toppings, 3-4 boards, 5 solder, 6 output, 7 battery, 8 fluid ID,
 * 9-10 upgrades. Collision prevention keeps fluidless recipes from running while fluid is in the tank.
 *
 * TODO copy tool, the "tau" spark particle (vanilla electric sparks for now)
 */
public class TileEntityMachineSolderingStation extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardReceiverMK2, IControlReceiver, IUpgradeInfoProvider, MenuProvider {

	public long power;
	public long maxPower = 2_000;
	public long consumption;
	public boolean collisionPrevention = false;
	public int progress;
	public int processTime = 1;

	public FluidTank tank;
	public ItemStack display = ItemStack.EMPTY;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	private SolderingRecipe recipe;

	public TileEntityMachineSolderingStation(BlockPos pos, BlockState state) {
		super(ModTileEntities.SOLDERING_STATION.get(), pos, state, 11);
		this.tank = new FluidTank(Fluids.NONE, 8_000);
	}

	@Override
	public String getName() {
		return "container.machineSolderingStation";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.power = Library.chargeTEFromItems(slots, 7, this.getPower(), this.getMaxPower());
			this.tank.setType(8, slots);

			this.autoPort(getConPos());

			recipe = SolderingRecipes.getRecipe(new ItemStack[] {slots.get(0), slots.get(1), slots.get(2), slots.get(3), slots.get(4), slots.get(5)});
			long intendedMaxPower;

			upgradeManager.checkSlots(slots, 9, 10);
			int redLevel = upgradeManager.getLevel(UpgradeType.SPEED);
			int blueLevel = upgradeManager.getLevel(UpgradeType.POWER);
			int blackLevel = upgradeManager.getLevel(UpgradeType.OVERDRIVE);

			if(recipe != null) {

				this.processTime = recipe.duration - (recipe.duration * redLevel / 6) + (recipe.duration * blueLevel / 3);
				this.consumption = recipe.consumption + (recipe.consumption * redLevel) - (recipe.consumption * blueLevel / 6);
				this.consumption *= (long) Math.pow(2, blackLevel);
				intendedMaxPower = consumption * 20;

				if(canProcess(recipe)) {
					this.progress += (1 + blackLevel);
					this.power -= this.consumption;

					if(progress >= processTime) {
						this.progress = 0;
						this.consumeItems(recipe);

						if(slots.get(6).isEmpty()) {
							slots.set(6, recipe.output.copy());
						} else {
							slots.get(6).grow(recipe.output.getCount());
						}

						this.setChanged();
					}

					if(level.getGameTime() % 20 == 0 && level instanceof ServerLevel server) {
						Direction dir = BlockDummyable.getRotation(getBlockState());
						Direction rot = dir.getClockWise();
						server.sendParticles(ParticleTypes.ELECTRIC_SPARK,
								worldPosition.getX() + 0.5 - dir.getStepX() * 0.5 + rot.getStepX() * 0.5, worldPosition.getY() + 1.125, worldPosition.getZ() + 0.5 - dir.getStepZ() * 0.5 + rot.getStepZ() * 0.5,
								3, 0.05, 0.05, 0.05, 0.1);
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

	public boolean canProcess(SolderingRecipe recipe) {

		if(this.power < this.consumption) return false;

		if(recipe.fluid != null) {
			if(this.tank.getTankType() != recipe.fluid.type) return false;
			if(this.tank.getFill() < recipe.fluid.fill) return false;
		}

		if(collisionPrevention && recipe.fluid == null && this.tank.getFill() > 0) return false;

		ItemStack out = slots.get(6);
		if(!out.isEmpty()) {
			if(!ItemStack.isSameItemSameComponents(out, recipe.output)) return false;
			if(out.getCount() + recipe.output.getCount() > out.getMaxStackSize()) return false;
		}

		return true;
	}

	private void consume(AStack[] ingredients, int from, int to) {
		for(AStack aStack : ingredients) {
			for(int i = from; i < to; i++) {
				ItemStack stack = slots.get(i);
				if(aStack.matchesRecipe(stack, true) && stack.getCount() >= aStack.stacksize) {
					this.removeItem(i, aStack.stacksize);
					break;
				}
			}
		}
	}

	public void consumeItems(SolderingRecipe recipe) {
		consume(recipe.toppings, 0, 3);
		consume(recipe.pcb, 3, 5);
		consume(recipe.solder, 5, 6);

		if(recipe.fluid != null) {
			this.tank.setFill(tank.getFill() - recipe.fluid.fill);
		}
	}

	/** Each kind of input only goes into its slots, and never twice into the same group */
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot < 3) return validFor(slot, stack, 0, 3, SolderingRecipes.toppings);
		if(slot < 5) return validFor(slot, stack, 3, 5, SolderingRecipes.pcb);
		if(slot < 6) return validFor(slot, stack, 5, 6, SolderingRecipes.solder);
		return false;
	}

	private boolean validFor(int slot, ItemStack stack, int from, int to, Iterable<AStack> ingredients) {
		for(int i = from; i < to; i++) if(i != slot && !slots.get(i).isEmpty() && ItemStack.isSameItem(slots.get(i), stack)) return false;
		for(AStack t : ingredients) if(t.matchesRecipe(stack, true)) return true;
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i == 6;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] { 0, 1, 2, 3, 4, 5, 6 };
	}

	protected DirPos[] getConPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		BlockPos p = worldPosition;
		return new DirPos[] {
				new DirPos(p.relative(dir), dir),
				new DirPos(p.relative(dir).relative(rot), dir),
				new DirPos(p.relative(dir, -2), dir.getOpposite()),
				new DirPos(p.relative(dir, -2).relative(rot), dir.getOpposite()),
				new DirPos(p.relative(rot, -1), rot.getOpposite()),
				new DirPos(p.relative(dir, -1).relative(rot, -1), rot.getOpposite()),
				new DirPos(p.relative(rot, 2), rot),
				new DirPos(p.relative(dir, -1).relative(rot, 2), rot),
		};
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(this.power);
		buf.writeLong(this.maxPower);
		buf.writeLong(this.consumption);
		buf.writeInt(this.progress);
		buf.writeInt(this.processTime);
		buf.writeBoolean(this.collisionPrevention);
		// every variant is its own item, the item is enough for the display
		buf.writeInt(recipe != null ? BuiltInRegistries.ITEM.getId(recipe.output.getItem()) : -1);
		this.tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.maxPower = buf.readLong();
		this.consumption = buf.readLong();
		this.progress = buf.readInt();
		this.processTime = buf.readInt();
		this.collisionPrevention = buf.readBoolean();
		int id = buf.readInt();
		this.display = id == -1 ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.byId(id));
		this.tank.deserialize(buf);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.power = nbt.getLong("power");
		this.maxPower = nbt.getLong("maxPower");
		this.progress = nbt.getInt("progress");
		this.processTime = nbt.getInt("processTime");
		this.collisionPrevention = nbt.getBoolean("collisionPrevention");
		tank.readFromNBT(nbt, "t");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("power", power);
		nbt.putLong("maxPower", maxPower);
		nbt.putInt("progress", progress);
		nbt.putInt("processTime", processTime);
		nbt.putBoolean("collisionPrevention", collisionPrevention);
		tank.writeToNBT(nbt, "t");
	}

	@Override public long getPower() { return Math.max(Math.min(power, maxPower), 0); }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override public FluidTank[] getAllTanks() { return new FluidTank[] {tank}; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {tank}; }

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 3, worldPosition.getZ() + 2);
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_soldering_station.get()));
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

	@Override
	public boolean hasPermission(Player player) {
		return this.stillValid(player);
	}

	/** The only button toggles the collision prevention */
	@Override
	public void receiveControl(CompoundTag data) {
		this.collisionPrevention = !this.collisionPrevention;
		this.setChanged();
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineSolderingStation(id, inv, this);
	}
}
