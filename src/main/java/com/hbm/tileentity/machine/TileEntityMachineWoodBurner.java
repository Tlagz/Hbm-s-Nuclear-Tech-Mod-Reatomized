package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerMachineWoodBurner;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.items.ItemEnums.EnumAshType;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.module.ModuleBurnTime;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.DirPos;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
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
 * Burns furnace fuels (logs and wood burn longer) or flammable fluids for 100 HE/t.
 * Slots: 0 fuel, 1 ash, 2 fluid identifier, 3/4 fluid container in/out, 5 battery.
 *
 * TODO config, EnergyControl info
 */
public class TileEntityMachineWoodBurner extends TileEntityMachineBase implements IFluidStandardReceiverMK2, IControlReceiver, IEnergyProviderMK2, MenuProvider {

	public long power;
	public static final long maxPower = 100_000;
	public int burnTime;
	public int maxBurnTime;
	public boolean liquidBurn = false;
	public boolean isOn = false;
	public int powerGen = 0;

	public FluidTank tank;

	public static ModuleBurnTime burnModule = new ModuleBurnTime().setLogTimeMod(4).setWoodTimeMod(2);

	public int ashLevelWood;
	public int ashLevelCoal;
	public int ashLevelMisc;

	public TileEntityMachineWoodBurner(BlockPos pos, BlockState state) {
		super(ModTileEntities.WOOD_BURNER.get(), pos, state, 6);
		this.tank = new FluidTank(Fluids.WOODOIL, 16_000);
	}

	@Override
	public String getName() {
		return "container.machineWoodBurner";
	}

	private Direction getDir() {
		return BlockDummyable.getRotation(getBlockState());
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			powerGen = 0;

			this.power = Library.chargeItemsFromTE(slots, 5, power, maxPower);
			this.tank.setType(2, slots);
			this.tank.loadTank(3, 4, slots);

			for(DirPos pos : getConPos()) {
				if(power > 0) this.tryProvide(level, pos, pos.getDir());
				if(level.getGameTime() % 20 == 0) this.trySubscribe(tank.getTankType(), level, pos, pos.getDir());
			}

			if(!liquidBurn) {

				if(this.burnTime <= 0) {

					ItemStack fuel = slots.get(0);
					if(!fuel.isEmpty()) {
						int burn = burnModule.getBurnTime(fuel);
						if(burn > 0) {
							EnumAshType type = getAshFromFuel(fuel);
							if(type == EnumAshType.WOOD) ashLevelWood += burn;
							if(type == EnumAshType.COAL) ashLevelCoal += burn;
							if(type == EnumAshType.MISC) ashLevelMisc += burn;
							int threshold = 2000;
							while(processAsh(ashLevelWood, EnumAshType.WOOD, threshold)) ashLevelWood -= threshold;
							while(processAsh(ashLevelCoal, EnumAshType.COAL, threshold)) ashLevelCoal -= threshold;
							while(processAsh(ashLevelMisc, EnumAshType.MISC, threshold)) ashLevelMisc -= threshold;

							this.maxBurnTime = this.burnTime = burn;
							ItemStack container = fuel.getCraftingRemainingItem();
							this.removeItem(0, 1);
							if(slots.get(0).isEmpty() && !container.isEmpty()) slots.set(0, container.copy());
							this.markChanged();
						}
					}

				} else if(this.power < maxPower && isOn) {
					this.burnTime--;
					this.powerGen += 100;
					if(level.getGameTime() % 20 == 0) PollutionHandler.incrementPollution(level, worldPosition, PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND);
				}

			} else {

				if(this.power < maxPower && tank.getFill() > 0 && isOn) {
					FT_Flammable trait = tank.getTankType().getTrait(FT_Flammable.class);

					if(trait != null) {

						int toBurn = Math.min(tank.getFill(), 2);

						if(toBurn > 0) {
							this.powerGen += trait.getHeatEnergy() * toBurn / 2_000L;
							this.tank.setFill(this.tank.getFill() - toBurn);
							if(level.getGameTime() % 20 == 0) PollutionHandler.incrementPollution(level, worldPosition, PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * toBurn / 2F);
						}
					}
				}
			}

			this.power += this.powerGen;
			if(this.power > maxPower) this.power = maxPower;

			this.networkPackNT(25);
		} else {

			if(powerGen > 0) {
				Direction dir = getDir();
				Direction rot = dir.getClockWise(); // ForgeDirection.getRotation(UP)
				level.addParticle(ParticleTypes.SMOKE, worldPosition.getX() + 0.5 - dir.getStepX() + rot.getStepX(), worldPosition.getY() + 4, worldPosition.getZ() + 0.5 - dir.getStepZ() + rot.getStepZ(), 0, 0.05, 0);
			}
		}
	}

	/** The original's TileEntityFireboxBase.getAshFromFuel, categories from tags */
	public static EnumAshType getAshFromFuel(ItemStack stack) {
		return switch(ModuleBurnTime.getCategory(stack)) {
			case "coke", "coal", "lignite" -> EnumAshType.COAL;
			case "log", "wood", "sapling" -> EnumAshType.WOOD;
			default -> EnumAshType.MISC;
		};
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(burnTime);
		buf.writeInt(powerGen);
		buf.writeInt(maxBurnTime);
		buf.writeBoolean(isOn);
		buf.writeBoolean(liquidBurn);

		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		burnTime = buf.readInt();
		powerGen = buf.readInt();
		maxBurnTime = buf.readInt();
		isOn = buf.readBoolean();
		liquidBurn = buf.readBoolean();

		tank.deserialize(buf);
	}

	/** Behind the machine, where the power and fluid connections are */
	private DirPos[] getConPos() {
		Direction dir = getDir();
		Direction rot = dir.getClockWise(); // ForgeDirection.getRotation(UP)
		BlockPos back = worldPosition.relative(dir, -2);
		return new DirPos[] {
				new DirPos(back, dir.getOpposite()),
				new DirPos(back.relative(rot), dir.getOpposite())
		};
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.power = nbt.getLong("power");
		this.burnTime = nbt.getInt("burnTime");
		this.maxBurnTime = nbt.getInt("maxBurnTime");
		this.isOn = nbt.getBoolean("isOn");
		this.liquidBurn = nbt.getBoolean("liquidBurn");
		tank.readFromNBT(nbt, "t");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("power", power);
		nbt.putInt("burnTime", burnTime);
		nbt.putInt("maxBurnTime", maxBurnTime);
		nbt.putBoolean("isOn", isOn);
		nbt.putBoolean("liquidBurn", liquidBurn);
		tank.writeToNBT(nbt, "t");
	}

	protected boolean processAsh(int level, EnumAshType type, int threshold) {

		if(level >= threshold) {
			ItemStack ash = slots.get(1);
			if(ash.isEmpty()) {
				slots.set(1, ModItems.powder_ash.stack(type));
				return true;
			} else if(ash.getCount() < ash.getMaxStackSize() && ash.is(ModItems.powder_ash.get(type).get())) {
				ash.grow(1);
				return true;
			}
		}

		return false;
	}

	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("toggle")) {
			this.isOn = !this.isOn;
			this.markChanged();
		}
		if(data.contains("switch")) {
			this.liquidBurn = !this.liquidBurn;
			this.markChanged();
		}
	}

	@Override
	public boolean hasPermission(Player player) {
		return this.stillValid(player);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] { 0, 1 };
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return i == 0 && burnModule.getBurnTime(itemStack) > 0;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, Direction side) {
		return slot == 1;
	}

	@Override public void setPower(long power) { this.power = power; }
	@Override public long getPower() { return power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override
	public boolean canConnect(Direction dir) {
		return dir == getDir().getOpposite();
	}

	@Override
	public boolean canConnect(com.hbm.inventory.fluid.FluidType type, Direction dir) {
		return dir == getDir().getOpposite();
	}

	@Override public FluidTank[] getAllTanks() { return new FluidTank[] {tank}; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {tank}; }

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 6, worldPosition.getZ() + 2);
	}

	/// GUI ///

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineWoodBurner(id, inv, this);
	}
}
