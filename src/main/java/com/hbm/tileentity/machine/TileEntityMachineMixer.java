package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMixer;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.MixerRecipes;
import com.hbm.inventory.recipes.MixerRecipes.MixerRecipe;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
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
 * Mixer: the fluid identifier picks the output fluid, the input tanks follow the selected recipe (the GUI cycles
 * through alternatives). Slots: 0 battery, 1 solid input, 2 fluid ID, 3-4 upgrades.
 *
 * TODO copy tool
 */
public class TileEntityMachineMixer extends TileEntityMachineBase implements IControlReceiver, IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, MenuProvider {

	public long power;
	public static final long maxPower = 10_000;
	public int progress;
	public int processTime;
	public int recipeIndex;

	public float rotation;
	public float prevRotation;
	public boolean wasOn = false;

	private int consumption = 50;

	public FluidTank[] tanks;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public TileEntityMachineMixer(BlockPos pos, BlockState state) {
		super(ModTileEntities.MIXER.get(), pos, state, 5);
		this.tanks = new FluidTank[3];
		this.tanks[0] = new FluidTank(Fluids.NONE, 16_000);
		this.tanks[1] = new FluidTank(Fluids.NONE, 16_000);
		this.tanks[2] = new FluidTank(Fluids.NONE, 24_000);
	}

	@Override
	public String getName() {
		return "container.machineMixer";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.power = Library.chargeTEFromItems(slots, 0, power, maxPower);
			tanks[2].setType(2, slots);

			upgradeManager.checkSlots(slots, 3, 4);
			int speedLevel = upgradeManager.getLevel(UpgradeType.SPEED);
			int powerLevel = upgradeManager.getLevel(UpgradeType.POWER);
			int overLevel = upgradeManager.getLevel(UpgradeType.OVERDRIVE);

			this.consumption = 50;
			this.consumption += speedLevel * 150;
			this.consumption -= (int) (this.consumption * powerLevel * 0.25);
			this.consumption *= (overLevel * 3 + 1);

			this.autoPort(getConPos());

			this.wasOn = this.canProcess();

			if(this.wasOn) {
				this.progress++;
				this.power -= this.getConsumption();

				this.processTime -= this.processTime * speedLevel / 4;
				this.processTime /= (overLevel + 1);

				if(processTime <= 0) this.processTime = 1;

				if(this.progress >= this.processTime) {
					this.process();
					this.progress = 0;
				}

			} else {
				this.progress = 0;
			}

			for(DirPos pos : getConPos()) {
				if(tanks[2].getFill() > 0) this.tryProvide(tanks[2], level, pos);
			}

			this.networkPackNT(50);

		} else {

			this.prevRotation = this.rotation;

			if(this.wasOn) {
				this.rotation += 20F;
			}

			if(this.rotation >= 360) {
				this.rotation -= 360;
				this.prevRotation -= 360;
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(processTime);
		buf.writeInt(progress);
		buf.writeInt(recipeIndex);
		buf.writeBoolean(wasOn);
		for(int i = 0; i < tanks.length; i++) tanks[i].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		processTime = buf.readInt();
		progress = buf.readInt();
		recipeIndex = buf.readInt();
		wasOn = buf.readBoolean();
		for(int i = 0; i < tanks.length; i++) tanks[i].deserialize(buf);
	}

	public boolean canProcess() {

		MixerRecipe[] recipes = MixerRecipes.getOutput(tanks[2].getTankType());

		if(recipes == null || recipes.length <= 0) {
			this.recipeIndex = 0;
			return false;
		}

		this.recipeIndex = this.recipeIndex % recipes.length;
		MixerRecipe recipe = recipes[this.recipeIndex];

		if(recipe == null) {
			this.recipeIndex = 0;
			return false;
		}

		tanks[0].setTankType(recipe.input1 != null ? recipe.input1.type : Fluids.NONE);
		tanks[1].setTankType(recipe.input2 != null ? recipe.input2.type : Fluids.NONE);

		if(recipe.input1 != null && tanks[0].getFill() < recipe.input1.fill) return false;
		if(recipe.input2 != null && tanks[1].getFill() < recipe.input2.fill) return false;

		/* simplest check would usually go first, but fluid checks also do the setup and we want that to happen even without power */
		if(this.power < getConsumption()) return false;

		if(recipe.output + tanks[2].getFill() > tanks[2].getMaxFill()) return false;

		if(recipe.solidInput != null) {
			ItemStack solid = slots.get(1);
			if(solid.isEmpty()) return false;
			if(!recipe.solidInput.matchesRecipe(solid, true) || recipe.solidInput.stacksize > solid.getCount()) return false;
		}

		this.processTime = recipe.processTime;
		return true;
	}

	protected void process() {

		MixerRecipe[] recipes = MixerRecipes.getOutput(tanks[2].getTankType());
		MixerRecipe recipe = recipes[this.recipeIndex % recipes.length];

		if(recipe.input1 != null) tanks[0].setFill(tanks[0].getFill() - recipe.input1.fill);
		if(recipe.input2 != null) tanks[1].setFill(tanks[1].getFill() - recipe.input2.fill);
		if(recipe.solidInput != null) this.removeItem(1, recipe.solidInput.stacksize);

		tanks[2].setFill(tanks[2].getFill() + recipe.output);
	}

	public int getConsumption() {
		return consumption;
	}

	protected DirPos[] getConPos() {
		BlockPos p = worldPosition;
		return new DirPos[] {
				new DirPos(p.below(), Direction.DOWN),
				new DirPos(p.east(), Direction.EAST),
				new DirPos(p.west(), Direction.WEST),
				new DirPos(p.south(), Direction.SOUTH),
				new DirPos(p.north(), Direction.NORTH),
		};
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] { 1 };
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		MixerRecipe[] recipes = MixerRecipes.getOutput(tanks[2].getTankType());
		if(recipes == null || recipes.length <= 0) return false;

		MixerRecipe recipe = recipes[this.recipeIndex % recipes.length];
		if(recipe == null || recipe.solidInput == null) return false;

		return recipe.solidInput.matchesRecipe(itemStack, true);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.power = nbt.getLong("power");
		this.progress = nbt.getInt("progress");
		this.processTime = nbt.getInt("processTime");
		this.recipeIndex = nbt.getInt("recipe");
		for(int i = 0; i < 3; i++) this.tanks[i].readFromNBT(nbt, i + "");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("power", power);
		nbt.putInt("progress", progress);
		nbt.putInt("processTime", processTime);
		nbt.putInt("recipe", recipeIndex);
		for(int i = 0; i < 3; i++) this.tanks[i].writeToNBT(nbt, i + "");
	}

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override public FluidTank[] getAllTanks() { return tanks; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] {tanks[2]}; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {tanks[0], tanks[1]}; }

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 3, worldPosition.getZ() + 1);
	}

	@Override
	public boolean hasPermission(Player player) {
		return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 16 * 16;
	}

	/** The GUI button cycles through the output's recipes */
	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("toggle")) this.recipeIndex++;
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_mixer.get()));
		if(type == UpgradeType.SPEED) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_DELAY, "-" + (level * 25) + "%"));
			info.add(ChatFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + (level * 300) + "%"));
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
		upgrades.put(UpgradeType.OVERDRIVE, 6);
		return upgrades;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMixer(id, inv, this);
	}
}
