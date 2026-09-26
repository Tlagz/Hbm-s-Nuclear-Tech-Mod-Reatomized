package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerElectrolyserFluid;
import com.hbm.inventory.container.ContainerElectrolyserMetal;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.recipes.ElectrolyserFluidRecipes;
import com.hbm.inventory.recipes.ElectrolyserFluidRecipes.ElectrolysisRecipe;
import com.hbm.inventory.recipes.ElectrolyserMetalRecipes;
import com.hbm.inventory.recipes.ElectrolyserMetalRecipes.ElectrolysisMetalRecipe;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.CrucibleUtil;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Electrolysis machine with two independent modes sharing power and upgrades: fluid electrolysis (water into hydrogen
 * and oxygen etc.) and metal electrolysis (crystals and nitric acid into two molten metals poured out of the ends).
 * Slots: 0 battery, 1-2 upgrades, 3-4 fluid ID, 5-10 fluid container IO, 11-13 fluid byproducts, 14 crystal, 15-20
 * metal byproducts. The GUI shown depends on the last selected mode.
 *
 * TODO the pouring particle ("foundry" effect), copy tool
 */
public class TileEntityElectrolyser extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IControlReceiver, IUpgradeInfoProvider, MenuProvider {

	public long power;
	public static final long maxPower = 20000000;
	public static final int usageOreBase = 10_000;
	public static final int usageFluidBase = 10_000;
	public int usageOre;
	public int usageFluid;

	public int progressFluid;
	public int processFluidTime = 100;
	public int progressOre;
	public int processOreTime = 600;

	public MaterialStack leftStack;
	public MaterialStack rightStack;
	public int maxMaterial = MaterialShapes.BLOCK.q(16);

	public int lastSelectedGUI = 0;

	public FluidTank[] tanks;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public TileEntityElectrolyser(BlockPos pos, BlockState state) {
		super(ModTileEntities.ELECTROLYSER.get(), pos, state, 21);
		tanks = new FluidTank[4];
		tanks[0] = new FluidTank(Fluids.WATER, 16000);
		tanks[1] = new FluidTank(Fluids.HYDROGEN, 16000);
		tanks[2] = new FluidTank(Fluids.OXYGEN, 16000);
		tanks[3] = new FluidTank(Fluids.NITRIC_ACID, 16000);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] { 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		if(i == 14) return ElectrolyserMetalRecipes.getRecipe(itemStack) != null;
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i != 14;
	}

	@Override
	public String getName() {
		return "container.machineElectrolyser";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.power = Library.chargeTEFromItems(slots, 0, power, maxPower);
			this.tanks[0].setType(3, 4, slots);
			this.tanks[0].loadTank(5, 6, slots);
			this.tanks[1].unloadTank(7, 8, slots);
			this.tanks[2].unloadTank(9, 10, slots);

			if(level.getGameTime() % 20 == 0) {
				for(DirPos pos : this.getConPos()) {
					this.trySubscribe(level, pos, pos.getDir());
					this.trySubscribe(tanks[0].getTankType(), level, pos, pos.getDir());
					this.trySubscribe(tanks[3].getTankType(), level, pos, pos.getDir());
					if(tanks[1].getFill() > 0) this.tryProvide(tanks[1], level, pos);
					if(tanks[2].getFill() > 0) this.tryProvide(tanks[2], level, pos);
				}
			}

			upgradeManager.checkSlots(slots, 1, 2);
			int speedLevel = upgradeManager.getLevel(UpgradeType.SPEED);
			int powerLevel = upgradeManager.getLevel(UpgradeType.POWER);

			usageOre = usageOreBase - usageOreBase * powerLevel / 4 + usageOreBase * speedLevel;
			usageFluid = usageFluidBase - usageFluidBase * powerLevel / 4 + usageFluidBase * speedLevel;

			for(int i = 0; i < getCycleCount(); i++) {

				if(this.canProcessFluid()) {
					this.progressFluid++;
					this.power -= this.usageFluid;

					if(this.progressFluid >= this.getDurationFluid()) {
						this.processFluids();
						this.progressFluid = 0;
						this.setChanged();
					}
				}

				if(this.canProcessMetal()) {
					this.progressOre++;
					this.power -= this.usageOre;

					if(this.progressOre >= this.getDurationMetal()) {
						this.processMetal();
						this.progressOre = 0;
						this.setChanged();
					}
				}
			}

			// left metal out of the back, right metal out of the front
			Direction dir = BlockDummyable.getRotation(getBlockState());
			int quanta = MaterialShapes.NUGGET.q(3) * Math.max(getCycleCount() * speedLevel, 1);
			if(this.leftStack != null) {
				this.leftStack = pour(this.leftStack, dir.getOpposite(), quanta);
			}
			if(this.rightStack != null) {
				this.rightStack = pour(this.rightStack, dir, quanta);
			}

			this.networkPackNT(50);
		}
	}

	private MaterialStack pour(MaterialStack stack, Direction dir, int quanta) {
		List<MaterialStack> toCast = new ArrayList<>();
		toCast.add(stack);
		CrucibleUtil.pourFullStack(level, worldPosition.getX() + 0.5D + dir.getStepX() * 5.875D, worldPosition.getY() + 2D, worldPosition.getZ() + 0.5D + dir.getStepZ() * 5.875D,
				6, true, toCast, quanta, new CrucibleUtil.Impact());
		return stack.amount <= 0 ? null : stack;
	}

	/** Three ports on each end */
	public DirPos[] getConPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		BlockPos back = worldPosition.relative(dir, -6);
		BlockPos front = worldPosition.relative(dir, 6);

		return new DirPos[] {
				new DirPos(back, dir.getOpposite()),
				new DirPos(back.relative(rot), dir.getOpposite()),
				new DirPos(back.relative(rot, -1), dir.getOpposite()),
				new DirPos(front, dir),
				new DirPos(front.relative(rot), dir),
				new DirPos(front.relative(rot, -1), dir)
		};
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(this.power);
		buf.writeInt(this.progressFluid);
		buf.writeInt(this.progressOre);
		buf.writeInt(this.usageOre);
		buf.writeInt(this.usageFluid);
		buf.writeInt(this.getDurationFluid());
		buf.writeInt(this.getDurationMetal());
		for(int i = 0; i < 4; i++) tanks[i].serialize(buf);
		buf.writeBoolean(this.leftStack != null);
		buf.writeBoolean(this.rightStack != null);
		if(this.leftStack != null) {
			buf.writeInt(leftStack.material.id);
			buf.writeInt(leftStack.amount);
		}
		if(this.rightStack != null) {
			buf.writeInt(rightStack.material.id);
			buf.writeInt(rightStack.amount);
		}
		buf.writeInt(lastSelectedGUI);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.progressFluid = buf.readInt();
		this.progressOre = buf.readInt();
		this.usageOre = buf.readInt();
		this.usageFluid = buf.readInt();
		this.processFluidTime = buf.readInt();
		this.processOreTime = buf.readInt();
		for(int i = 0; i < 4; i++) tanks[i].deserialize(buf);
		boolean left = buf.readBoolean();
		boolean right = buf.readBoolean();
		this.leftStack = left ? new MaterialStack(Mats.matById.get(buf.readInt()), buf.readInt()) : null;
		this.rightStack = right ? new MaterialStack(Mats.matById.get(buf.readInt()), buf.readInt()) : null;
		this.lastSelectedGUI = buf.readInt();
	}

	public boolean canProcessFluid() {

		if(this.power < usageFluid) return false;

		ElectrolysisRecipe recipe = ElectrolyserFluidRecipes.recipes.get(tanks[0].getTankType());

		if(recipe == null) return false;

		tanks[1].setTankType(recipe.output1.type);
		tanks[2].setTankType(recipe.output2.type);

		if(recipe.amount > tanks[0].getFill()) return false;
		if(recipe.output1.fill + tanks[1].getFill() > tanks[1].getMaxFill()) return false;
		if(recipe.output2.fill + tanks[2].getFill() > tanks[2].getMaxFill()) return false;

		return fitsByproducts(recipe.byproduct, 11);
	}

	/** Whether the byproducts fit into the slots starting at start */
	private boolean fitsByproducts(ItemStack[] byproducts, int start) {
		if(byproducts == null) return true;
		for(int i = 0; i < byproducts.length; i++) {
			ItemStack slot = slots.get(start + i);
			ItemStack byproduct = byproducts[i];
			if(slot.isEmpty()) continue;
			if(!ItemStack.isSameItemSameComponents(slot, byproduct)) return false;
			if(slot.getCount() + byproduct.getCount() > slot.getMaxStackSize()) return false;
		}
		return true;
	}

	private void addByproducts(ItemStack[] byproducts, int start) {
		if(byproducts == null) return;
		for(int i = 0; i < byproducts.length; i++) {
			ItemStack slot = slots.get(start + i);
			if(slot.isEmpty()) {
				slots.set(start + i, byproducts[i].copy());
			} else {
				slot.grow(byproducts[i].getCount());
			}
		}
	}

	public void processFluids() {
		ElectrolysisRecipe recipe = ElectrolyserFluidRecipes.recipes.get(tanks[0].getTankType());
		tanks[0].setFill(tanks[0].getFill() - recipe.amount);
		tanks[1].setTankType(recipe.output1.type);
		tanks[2].setTankType(recipe.output2.type);
		tanks[1].setFill(tanks[1].getFill() + recipe.output1.fill);
		tanks[2].setFill(tanks[2].getFill() + recipe.output2.fill);
		addByproducts(recipe.byproduct, 11);
	}

	public boolean canProcessMetal() {

		if(slots.get(14).isEmpty()) return false;
		if(this.power < usageOre) return false;
		if(this.tanks[3].getFill() < 100) return false;

		ElectrolysisMetalRecipe recipe = ElectrolyserMetalRecipes.getRecipe(slots.get(14));
		if(recipe == null) return false;

		if(leftStack != null && recipe.output1 != null) {
			if(recipe.output1.material != leftStack.material) return false;
			if(recipe.output1.amount + leftStack.amount > this.maxMaterial) return false;
		}

		if(rightStack != null && recipe.output2 != null) {
			if(recipe.output2.material != rightStack.material) return false;
			if(recipe.output2.amount + rightStack.amount > this.maxMaterial) return false;
		}

		return fitsByproducts(recipe.byproduct, 15);
	}

	public void processMetal() {

		ElectrolysisMetalRecipe recipe = ElectrolyserMetalRecipes.getRecipe(slots.get(14));

		if(recipe.output1 != null) {
			if(leftStack == null) {
				leftStack = new MaterialStack(recipe.output1.material, recipe.output1.amount);
			} else {
				leftStack.amount += recipe.output1.amount;
			}
		}

		if(recipe.output2 != null) {
			if(rightStack == null) {
				rightStack = new MaterialStack(recipe.output2.material, recipe.output2.amount);
			} else {
				rightStack.amount += recipe.output2.amount;
			}
		}

		addByproducts(recipe.byproduct, 15);

		this.tanks[3].setFill(this.tanks[3].getFill() - 100);
		this.removeItem(14, 1);
	}

	public int getDurationMetal() {
		ElectrolysisMetalRecipe result = ElectrolyserMetalRecipes.getRecipe(slots.get(14));
		int base = result != null ? result.duration : 600;
		int speed = upgradeManager.getLevel(UpgradeType.SPEED) - Math.min(upgradeManager.getLevel(UpgradeType.POWER), 1);
		return (int) Math.ceil((base * Math.max(1F - 0.25F * speed, 0.2)));
	}

	public int getDurationFluid() {
		ElectrolysisRecipe result = ElectrolyserFluidRecipes.getRecipe(tanks[0].getTankType());
		int base = result != null ? result.duration : 100;
		int speed = upgradeManager.getLevel(UpgradeType.SPEED) - Math.min(upgradeManager.getLevel(UpgradeType.POWER), 1);
		return (int) Math.ceil((base * Math.max(1F - 0.25F * speed, 0.2)));
	}

	public int getCycleCount() {
		int speed = upgradeManager.getLevel(UpgradeType.OVERDRIVE);
		return Math.min(1 + speed * 2, 7);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.power = nbt.getLong("power");
		this.progressFluid = nbt.getInt("progressFluid");
		this.progressOre = nbt.getInt("progressOre");
		this.processFluidTime = nbt.getInt("processFluidTime");
		this.processOreTime = nbt.getInt("processOreTime");
		if(nbt.contains("leftType")) this.leftStack = new MaterialStack(Mats.matById.get(nbt.getInt("leftType")), nbt.getInt("leftAmount"));
		else this.leftStack = null;
		if(nbt.contains("rightType")) this.rightStack = new MaterialStack(Mats.matById.get(nbt.getInt("rightType")), nbt.getInt("rightAmount"));
		else this.rightStack = null;
		for(int i = 0; i < 4; i++) tanks[i].readFromNBT(nbt, "t" + i);
		this.lastSelectedGUI = nbt.getInt("lastSelectedGUI");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("power", this.power);
		nbt.putInt("progressFluid", this.progressFluid);
		nbt.putInt("progressOre", this.progressOre);
		nbt.putInt("processFluidTime", getDurationFluid());
		nbt.putInt("processOreTime", getDurationMetal());
		if(this.leftStack != null) {
			nbt.putInt("leftType", leftStack.material.id);
			nbt.putInt("leftAmount", leftStack.amount);
		}
		if(this.rightStack != null) {
			nbt.putInt("rightType", rightStack.material.id);
			nbt.putInt("rightAmount", rightStack.amount);
		}
		for(int i = 0; i < 4; i++) tanks[i].writeToNBT(nbt, "t" + i);
		nbt.putInt("lastSelectedGUI", this.lastSelectedGUI);
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 5, worldPosition.getY(), worldPosition.getZ() - 5, worldPosition.getX() + 6, worldPosition.getY() + 4, worldPosition.getZ() + 6);
	}

	@Override public long getPower() { return this.power; }
	@Override public long getMaxPower() { return maxPower; }
	@Override public void setPower(long power) { this.power = power; }

	@Override public FluidTank[] getAllTanks() { return tanks; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] {tanks[1], tanks[2]}; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {tanks[0], tanks[3]}; }

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		if(lastSelectedGUI == 0) return new ContainerElectrolyserFluid(id, inv, this);
		return new ContainerElectrolyserMetal(id, inv, this);
	}

	@Override
	public void receiveControl(CompoundTag data) { }

	@Override
	public void receiveControl(Player player, CompoundTag data) {
		if(data.contains("sgm")) lastSelectedGUI = 1;
		if(data.contains("sgf")) lastSelectedGUI = 0;
		this.setChanged();
		if(player instanceof ServerPlayer serverPlayer) serverPlayer.openMenu(this, buf -> buf.writeBlockPos(worldPosition));
	}

	@Override
	public boolean hasPermission(Player player) {
		return this.stillValid(player);
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_electrolyser.get()));
		if(type == UpgradeType.SPEED) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_DELAY, "-" + (level * 25) + "%"));
			info.add(ChatFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + (level * 100) + "%"));
		}
		if(type == UpgradeType.POWER) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_CONSUMPTION, "-" + (level * 25) + "%"));
			info.add(ChatFormatting.RED + I18nUtil.resolveKey(KEY_DELAY, "+" + (25) + "%"));
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
