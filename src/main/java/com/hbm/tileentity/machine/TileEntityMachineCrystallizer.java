package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerCrystallizer;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.inventory.recipes.CrystallizerRecipes.CrystallizerRecipe;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.DirPos;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IBatteryItem;
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
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Ore acidizer: an item and an acid into crystals and more. Slots: 0 input, 1 battery, 2 output, 3/4 fluid
 * container in/out, 5/6 upgrades (speed, effectiveness = chance to keep the input, overdrive), 7 fluid identifier.
 *
 * TODO the side ladder (HbmPlayerProps.isOnLadder), copy tool
 */
public class TileEntityMachineCrystallizer extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardReceiverMK2, IUpgradeInfoProvider, MenuProvider {

	public long power;
	public static final long maxPower = 1000000;
	public static final int demand = 1000;
	public short progress;
	public short duration = 600;
	public boolean isOn;

	public float angle;
	public float prevAngle;

	private AudioWrapper audio;

	public FluidTank tank;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public TileEntityMachineCrystallizer(BlockPos pos, BlockState state) {
		super(ModTileEntities.CRYSTALLIZER.get(), pos, state, 8);
		tank = new FluidTank(Fluids.PEROXIDE, 8000);
	}

	@Override
	public String getName() {
		return "container.crystallizer";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.isOn = false;

			this.autoPort(getConPos());

			power = Library.chargeTEFromItems(slots, 1, power, maxPower);
			tank.setType(7, slots);
			tank.loadTank(3, 4, slots);

			upgradeManager.checkSlots(slots, 5, 6);

			for(int i = 0; i < getCycleCount(); i++) {

				if(canProcess()) {

					progress++;
					power -= getPowerRequired();
					isOn = true;

					if(progress > getDuration()) {
						progress = 0;
						processItem();

						this.setChanged();
					}

				} else {
					progress = 0;
				}
			}

			this.networkPackNT(25);
		} else {

			prevAngle = angle;

			if(isOn) {
				angle += 5F * this.getCycleCount();

				if(angle >= 360) {
					angle -= 360;
					prevAngle -= 360;
				}

				double dist = com.hbm.main.ClientHooks.distanceToPlayer(worldPosition.getX() + 0.5, worldPosition.getY() + 6, worldPosition.getZ() + 0.5);

				if(level.random.nextInt(20) == 0 && dist < 50) {
					level.addParticle(ParticleTypes.CLOUD, worldPosition.getX() + level.random.nextDouble(), worldPosition.getY() + 6.5D, worldPosition.getZ() + level.random.nextDouble(), 0.0, 0.1, 0.0);
				}

				if(com.hbm.main.ClientHooks.distanceToPlayer(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) < 25) {
					if(audio == null) {
						audio = createAudioLoop();
						audio.startSound();
					} else if(!audio.isPlaying()) {
						audio = rebootAudio(audio);
					}
					audio.keepAlive();
					audio.updateVolume(this.getVolume(1F));
					audio.updatePitch(0.75F);
				} else {
					if(audio != null) {
						audio.stopSound();
						audio = null;
					}
				}

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
		return AudioWrapper.getLoopedSound("hbm:block.chemicalPlant", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 1F, 15F, 0.75F, 15);
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	/** Two ports next to each corner extra */
	protected DirPos[] getConPos() {
		BlockPos p = worldPosition;
		return new DirPos[] {
				new DirPos(p.offset(2, 0, 1), Direction.EAST),
				new DirPos(p.offset(2, 0, -1), Direction.EAST),
				new DirPos(p.offset(-2, 0, 1), Direction.WEST),
				new DirPos(p.offset(-2, 0, -1), Direction.WEST),
				new DirPos(p.offset(1, 0, 2), Direction.SOUTH),
				new DirPos(p.offset(-1, 0, 2), Direction.SOUTH),
				new DirPos(p.offset(1, 0, -2), Direction.NORTH),
				new DirPos(p.offset(-1, 0, -2), Direction.NORTH)
		};
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeShort(progress);
		buf.writeShort(getDuration());
		buf.writeLong(power);
		buf.writeBoolean(isOn);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		progress = buf.readShort();
		duration = buf.readShort();
		power = buf.readLong();
		isOn = buf.readBoolean();
		tank.deserialize(buf);
	}

	private void processItem() {

		CrystallizerRecipe result = CrystallizerRecipes.getOutput(slots.get(0), tank.getTankType());

		if(result == null) //never happens but you can't be sure enough
			return;

		ItemStack stack = result.output.copy();

		if(slots.get(2).isEmpty())
			slots.set(2, stack);
		else if(slots.get(2).getCount() + stack.getCount() <= slots.get(2).getMaxStackSize())
			slots.get(2).grow(stack.getCount());

		tank.setFill(tank.getFill() - getRequiredAcid(result.acidAmount));

		float freeChance = this.getFreeChance(result);

		if(freeChance == 0 || freeChance < level.random.nextFloat())
			this.removeItem(0, result.itemAmount);
	}

	private boolean canProcess() {

		//Is there no input?
		if(slots.get(0).isEmpty())
			return false;

		if(power < getPowerRequired())
			return false;

		CrystallizerRecipe result = CrystallizerRecipes.getOutput(slots.get(0), tank.getTankType());

		//Or output?
		if(result == null)
			return false;

		//Not enough of the input item?
		if(slots.get(0).getCount() < result.itemAmount)
			return false;

		if(tank.getFill() < getRequiredAcid(result.acidAmount)) return false;

		ItemStack stack = result.output.copy();
		ItemStack out = slots.get(2);

		//Does the output not match?
		if(!out.isEmpty() && !ItemStack.isSameItemSameComponents(out, stack))
			return false;

		//Or is the output slot already full?
		if(!out.isEmpty() && out.getCount() + stack.getCount() > out.getMaxStackSize())
			return false;

		return true;
	}

	public int getRequiredAcid(int base) {
		return base;
	}

	public float getFreeChance(CrystallizerRecipe recipe) {
		int efficiency = upgradeManager.getLevel(UpgradeType.EFFECT);
		if(efficiency > 0) {
			return Math.min(efficiency * recipe.productivity, 0.99F);
		}
		return 0;
	}

	public short getDuration() {
		CrystallizerRecipe result = CrystallizerRecipes.getOutput(slots.get(0), tank.getTankType());
		int base = result != null ? result.duration : 600;
		int speed = upgradeManager.getLevel(UpgradeType.SPEED);
		if(speed > 0) {
			return (short) Math.ceil((base * Math.max(1F - 0.25F * speed, 0.25F)));
		}
		return (short) base;
	}

	public int getPowerRequired() {
		int speed = upgradeManager.getLevel(UpgradeType.SPEED);
		int effect = upgradeManager.getLevel(UpgradeType.EFFECT);
		return (int) (demand + speed * demand + effect * demand * 2);
	}

	public float getCycleCount() {
		int speed = upgradeManager.getLevel(UpgradeType.OVERDRIVE);
		return Math.min(1 + speed * 2, 7);
	}

	public long getPowerScaled(int i) {
		return (power * i) / maxPower;
	}

	public int getProgressScaled(int i) {
		return duration <= 0 ? 0 : (progress * i) / duration;
	}

	@Override
	public void setPower(long i) {
		this.power = i;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		power = nbt.getLong("power");
		tank.readFromNBT(nbt, "tank");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("power", power);
		tank.writeToNBT(nbt, "tank");
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		if(i == 0 && CrystallizerRecipes.getOutput(itemStack, tank.getTankType()) != null) return true;
		if(i == 1 && itemStack.getItem() instanceof IBatteryItem) return true;
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i == 2;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] { 0, 2 };
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 10, worldPosition.getZ() + 2);
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {tank};
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] { tank };
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.EFFECT || type == UpgradeType.OVERDRIVE;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_crystallizer.get()));
		if(type == UpgradeType.SPEED) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_DELAY, "-" + (level * 25) + "%"));
			info.add(ChatFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + (level * 100) + "%"));
		}
		if(type == UpgradeType.EFFECT) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_EFFICIENCY, "x" + level));
			info.add(ChatFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + (level * 200) + "%"));
		}
		if(type == UpgradeType.OVERDRIVE) {
			info.add((BobMathUtil.getBlink() ? ChatFormatting.RED : ChatFormatting.DARK_GRAY) + "YES");
		}
	}

	@Override
	public HashMap<UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.SPEED, 3);
		upgrades.put(UpgradeType.EFFECT, 3);
		upgrades.put(UpgradeType.OVERDRIVE, 3);
		return upgrades;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerCrystallizer(id, inv, this);
	}
}
