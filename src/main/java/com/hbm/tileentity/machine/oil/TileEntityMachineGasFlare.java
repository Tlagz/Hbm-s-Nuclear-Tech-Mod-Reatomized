package com.hbm.tileentity.machine.oil;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineGasFlare;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Gaseous;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Gaseous_ART;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.DirPos;
import com.hbm.util.ParticleUtil;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Flare stack: vents gases (valve open) or burns flammable fluids for a bit of power (valve open, ignition on).
 * Slots: 0 battery, 1/2 fluid container in/out, 3 fluid identifier, 4/5 upgrades (speed, effectiveness).
 *
 * TODO tilting (checkTilt), EnergyControl info, copy tool
 */
public class TileEntityMachineGasFlare extends TileEntityMachineBase implements IEnergyProviderMK2, IFluidStandardReceiverMK2, IControlReceiver, IUpgradeInfoProvider, MenuProvider {

	public long power;
	public static final long maxPower = 100000;
	public FluidTank tank;

	public boolean isOn = false;
	public boolean doesBurn = false;
	protected int fluidUsed = 0;
	protected int output = 0;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public TileEntityMachineGasFlare(BlockPos pos, BlockState state) {
		super(ModTileEntities.GAS_FLARE.get(), pos, state, 6);
		tank = new FluidTank(Fluids.GAS, 64000);
	}

	@Override
	public String getName() {
		return "container.gasFlare";
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.power = nbt.getLong("powerTime");
		tank.readFromNBT(nbt, "gas");
		isOn = nbt.getBoolean("isOn");
		doesBurn = nbt.getBoolean("doesBurn");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("powerTime", power);
		tank.writeToNBT(nbt, "gas");
		nbt.putBoolean("isOn", isOn);
		nbt.putBoolean("doesBurn", doesBurn);
	}

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	@Override
	public boolean hasPermission(Player player) {
		return player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) <= 256;
	}

	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("valve")) this.isOn = !this.isOn;
		if(data.contains("dial")) this.doesBurn = !this.doesBurn;
		this.markChanged();
	}

	private boolean isGaseous() {
		return tank.getTankType().hasTrait(FT_Gaseous.class) || tank.getTankType().hasTrait(FT_Gaseous_ART.class);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.fluidUsed = 0;
			this.output = 0;

			for(DirPos pos : getConPos()) {
				this.tryProvide(level, pos, pos.getDir());
				this.trySubscribe(tank.getTankType(), level, pos, pos.getDir());
			}

			tank.setType(3, slots);
			tank.loadTank(1, 2, slots);

			int maxVent = 50;
			int maxBurn = 10;

			if(isOn && tank.getFill() > 0 && !this.tilted) {

				upgradeManager.checkSlots(slots, 4, 5);
				int burn = upgradeManager.getLevel(UpgradeType.SPEED);
				int yield = upgradeManager.getLevel(UpgradeType.EFFECT);

				maxVent += maxVent * burn;
				maxBurn += maxBurn * burn;

				double x = worldPosition.getX(), y = worldPosition.getY(), z = worldPosition.getZ();

				if(!doesBurn || !(tank.getTankType().hasTrait(FT_Flammable.class))) {

					if(isGaseous()) {

						int eject = Math.min(maxVent, tank.getFill());
						this.fluidUsed = eject;
						tank.setFill(tank.getFill() - eject);
						tank.getTankType().onFluidRelease(this, tank, eject);

						if(level.getGameTime() % 7 == 0)
							level.playSound(null, x, y + 11, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, getVolume(1.5F), 0.5F);

						if(level.getGameTime() % 5 == 0 && eject > 0) {
							FluidTrait.onRelease(level, worldPosition, tank.getTankType(), tank, FluidReleaseType.SPILL, eject * 5);
						}
					}
				} else {

					if(tank.getTankType().hasTrait(FT_Flammable.class)) {
						int eject = Math.min(maxBurn, tank.getFill());
						this.fluidUsed = eject;
						tank.setFill(tank.getFill() - eject);

						int penalty = 5;
						if(!isGaseous())
							penalty = 10;

						long powerProd = tank.getTankType().getTrait(FT_Flammable.class).getHeatEnergy() * eject / 1_000; // divided by 1000 per mB
						powerProd /= penalty;
						powerProd += powerProd * yield / 3;

						this.output = (int) powerProd;
						power += powerProd;

						if(power > maxPower)
							power = maxPower;

						ParticleUtil.spawnGasFlame(level, x + 0.5F, y + 11.75F, z + 0.5F, level.random.nextGaussian() * 0.15, 0.2, level.random.nextGaussian() * 0.15);

						List<Entity> list = level.getEntitiesOfClass(Entity.class, new AABB(x - 1, y + 12, z - 2, x + 2, y + 17, z + 2));
						for(Entity e : list) {
							e.igniteForSeconds(5);
							e.hurt(level.damageSources().onFire(), 5F);
						}

						if(level.getGameTime() % 3 == 0)
							level.playSound(null, x, y + 11, z, ModSounds.get("weapon.flamethrowerShoot"), SoundSource.BLOCKS, getVolume(1.5F), 0.75F);

						if(level.getGameTime() % 5 == 0 && eject > 0) {
							FluidTrait.onRelease(level, worldPosition, tank.getTankType(), tank, FluidReleaseType.BURN, eject * 5);
						}
					}
				}
			}

			power = Library.chargeItemsFromTE(slots, 0, power, maxPower);

			this.networkPackNT(50);

		} else {

			if(isOn && tank.getFill() > 0) {

				if((!doesBurn || !(tank.getTankType().hasTrait(FT_Flammable.class))) && isGaseous()) {

					CompoundTag data = new CompoundTag();
					data.putString("type", "tower");
					data.putFloat("lift", 1F);
					data.putFloat("base", 0.25F);
					data.putFloat("max", 3F);
					data.putInt("life", 150 + level.random.nextInt(20));
					data.putInt("color", tank.getTankType().getColor());

					data.putDouble("posX", worldPosition.getX() + 0.5);
					data.putDouble("posZ", worldPosition.getZ() + 0.5);
					data.putDouble("posY", worldPosition.getY() + 11);

					com.hbm.particle.ParticleEffectsNT.effectNT(data);
				}

				if(doesBurn && tank.getTankType().hasTrait(FT_Flammable.class)
						&& com.hbm.main.ClientHooks.distanceToPlayer(worldPosition.getX(), worldPosition.getY() + 10, worldPosition.getZ()) <= 32) {

					CompoundTag data = new CompoundTag();
					data.putString("type", "vanillaExt");
					data.putString("mode", "smoke");

					if(level.getGameTime() % 2 == 0) {
						data.putDouble("posX", worldPosition.getX() + 1.5);
						data.putDouble("posZ", worldPosition.getZ() + 1.5);
						data.putDouble("posY", worldPosition.getY() + 10.75);
					} else {
						data.putDouble("posX", worldPosition.getX() + 1.125);
						data.putDouble("posZ", worldPosition.getZ() - 0.5);
						data.putDouble("posY", worldPosition.getY() + 11.75);
					}

					com.hbm.particle.ParticleEffectsNT.effectNT(data);
				}
			}
		}
	}

	public DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(worldPosition.east(2), Direction.EAST),
				new DirPos(worldPosition.west(2), Direction.WEST),
				new DirPos(worldPosition.south(2), Direction.SOUTH),
				new DirPos(worldPosition.north(2), Direction.NORTH)
		};
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(this.power);
		buf.writeBoolean(this.isOn);
		buf.writeBoolean(this.doesBurn);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.isOn = buf.readBoolean();
		this.doesBurn = buf.readBoolean();
		tank.deserialize(buf);
	}

	@Override
	public long getPower() {
		return this.power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public void setPower(long i) {
		this.power = i;
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
	public ConnectionPriority getFluidPriority() {
		return ConnectionPriority.LOW;
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.EFFECT;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_flare.get()));
		if(type == UpgradeType.SPEED) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + (level * 100) + "%"));
		}
		if(type == UpgradeType.EFFECT) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_EFFICIENCY, "+" + (100 * level / 3) + "%"));
		}
	}

	@Override
	public HashMap<UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.SPEED, 3);
		upgrades.put(UpgradeType.EFFECT, 3);
		return upgrades;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineGasFlare(id, inv, this);
	}
}
