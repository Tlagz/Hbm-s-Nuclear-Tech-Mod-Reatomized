package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineTurbofan;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FT_Combustible.FuelGrade;
import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.ModSounds;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachinePolluting;
import com.hbm.util.DirPos;
import com.hbm.util.ParticleUtil;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Turbofan: burns aviation fuel for power, sucks in whatever stands in front of it (the intake grinds it up and
 * collects its blood) and blows everything behind it away, with an afterburner (upgrades or the flame pony) setting
 * the exhaust on fire.
 * Slots: 0 fuel in, 1 fuel container out, 2 upgrade, 3 battery, 4 fluid identifier.
 *
 * TODO giblets particle, IFluidCopiable
 */
public class TileEntityMachineTurbofan extends TileEntityMachinePolluting implements IEnergyProviderMK2, IFluidStandardTransceiverMK2, IUpgradeInfoProvider, MenuProvider {

	public long power;
	public static final long maxPower = 1_000_000;
	public FluidTank tank;
	public FluidTank blood;

	public int afterburner;
	public boolean wasOn;
	public boolean showBlood = false;
	protected int output;
	protected int consumption;

	public float spin;
	public float lastSpin;
	public int momentum = 0;

	private AudioWrapper audio;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public TileEntityMachineTurbofan(BlockPos pos, BlockState state) {
		super(ModTileEntities.TURBOFAN.get(), pos, state, 5, 150);
		tank = new FluidTank(Fluids.KEROSENE, 24000);
		blood = new FluidTank(Fluids.BLOOD, 24000);
	}

	@Override
	public String getName() {
		return "container.machineTurbofan";
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.power = nbt.getLong("powerTime");
		tank.readFromNBT(nbt, "fuel");
		blood.readFromNBT(nbt, "blood");
		this.showBlood = nbt.getBoolean("showBlood");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("powerTime", power);
		tank.writeToNBT(nbt, "fuel");
		blood.writeToNBT(nbt, "blood");
		nbt.putBoolean("showBlood", showBlood);
	}

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	/** The intake faces dir, the exhaust -dir; rot is to the side */
	private Direction getDir() {
		return BlockDummyable.getRotation(getBlockState()).getClockWise();
	}

	protected DirPos[] getConPos() {

		Direction dir = getDir();
		Direction rot = dir.getCounterClockWise();
		BlockPos p = worldPosition;

		return new DirPos[] {
				new DirPos(p.relative(rot, 2), rot),
				new DirPos(p.relative(rot, 2).relative(dir, -1), rot),
				new DirPos(p.relative(rot, -2), rot.getOpposite()),
				new DirPos(p.relative(rot, -2).relative(dir, -1), rot.getOpposite())
		};
	}

	/** An area in front of (positive) or behind (negative) the turbofan, 3 wide and 3 high */
	private AABB area(Direction dir, Direction rot, double from, double to) {
		double minX = worldPosition.getX() + 0.5 + dir.getStepX() * from - rot.getStepX() * 1.5;
		double maxX = worldPosition.getX() + 0.5 + dir.getStepX() * to + rot.getStepX() * 1.5;
		double minZ = worldPosition.getZ() + 0.5 + dir.getStepZ() * from - rot.getStepZ() * 1.5;
		double maxZ = worldPosition.getZ() + 0.5 + dir.getStepZ() * to + rot.getStepZ() * 1.5;
		return new AABB(Math.min(minX, maxX), worldPosition.getY(), Math.min(minZ, maxZ), Math.max(minX, maxX), worldPosition.getY() + 3, Math.max(minZ, maxZ));
	}

	private static void push(Entity e, Direction dir) {
		e.setDeltaMovement(e.getDeltaMovement().add(-dir.getStepX() * 0.2, 0, -dir.getStepZ() * 0.2));
		e.hurtMarked = true;
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.output = 0;
			this.consumption = 0;

			tank.setType(4, slots);
			tank.loadTank(0, 1, slots);
			blood.setTankType(Fluids.BLOOD);

			this.wasOn = false;

			upgradeManager.checkSlots(slots, 2, 2);
			this.afterburner = upgradeManager.getLevel(UpgradeType.AFTERBURN);

			if(slots.get(2).is(ModItems.flame_pony.get()))
				this.afterburner = 100;

			long burnValue = 0;
			int amount = 1 + this.afterburner;
			int amountToBurn = Math.min(amount, this.tank.getFill());

			boolean redstone = false;

			for(DirPos pos : getConPos()) {
				if(!level.isLoaded(pos)) continue;
				if(level.hasNeighborSignal(pos)) {
					redstone = true;
					break;
				}
			}

			if(!redstone) {

				if(tank.getTankType().hasTrait(FT_Combustible.class) && tank.getTankType().getTrait(FT_Combustible.class).getGrade() == FuelGrade.AERO) {
					burnValue = tank.getTankType().getTrait(FT_Combustible.class).getCombustionEnergy() / 1_000;
				}

				if(amountToBurn > 0) {
					this.wasOn = true;
					this.tank.setFill(this.tank.getFill() - amountToBurn);
					this.output = (int) (burnValue * amountToBurn * (1 + Math.min(this.afterburner / 3D, 4)));
					this.power += this.output;
					this.consumption = amountToBurn;

					if(level.getGameTime() % 20 == 0) super.pollute(tank.getTankType(), FluidTrait.FluidReleaseType.BURN, amountToBurn * 5);
				}
			}

			power = Library.chargeItemsFromTE(slots, 3, power, power);

			this.autoPort(getConPos());

			if(burnValue > 0 && amountToBurn > 0) {

				Direction dir = getDir();
				Direction rot = dir.getClockWise();

				if(this.afterburner > 0) {

					for(int i = 0; i < 2; i++) {
						double speed = 2 + level.random.nextDouble() * 3;
						double deviation = level.random.nextGaussian() * 0.2;
						ParticleUtil.spawnGasFlame(level, worldPosition.getX() + 0.5F - dir.getStepX() * (3 - i), worldPosition.getY() + 1.5F, worldPosition.getZ() + 0.5F - dir.getStepZ() * (3 - i),
								-dir.getStepX() * speed + deviation, 0, -dir.getStepZ() * speed + deviation);
					}

					if(this.afterburner > 90 && level.random.nextInt(30) == 0) {
						level.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 1.5, worldPosition.getZ() + 0.5, ModSounds.get("block.damage"), SoundSource.BLOCKS, 3.0F, 0.95F + level.random.nextFloat() * 0.2F);
					}

					if(this.afterburner > 90) {
						ParticleUtil.spawnGasFlame(level,
								worldPosition.getX() + 0.5F + dir.getStepX() * (level.random.nextDouble() * 4 - 2) + rot.getStepX() * (level.random.nextDouble() * 2 - 1),
								worldPosition.getY() + 1F + level.random.nextDouble() * 2,
								worldPosition.getZ() + 0.5F - dir.getStepZ() * (level.random.nextDouble() * 4 - 2) + rot.getStepZ() * (level.random.nextDouble() * 2 - 1),
								0, 0.1 * level.random.nextDouble(), 0);
					}
				}

				// exhaust: blows away and, with the afterburner, sets on fire
				for(Entity e : level.getEntitiesOfClass(Entity.class, area(dir, rot, -3.5, -19.5))) {

					if(this.afterburner > 0) {
						e.igniteForSeconds(5);
						e.hurt(level.damageSources().onFire(), 5F);
					}
					push(e, dir);
				}

				// intake: sucks in
				for(Entity e : level.getEntitiesOfClass(Entity.class, area(dir, rot, 3.5, 8.5))) {
					push(e, dir);
				}

				// the blades
				for(Entity e : level.getEntitiesOfClass(Entity.class, area(dir, rot, 3.5, 3.75))) {
					e.hurt(ModDamageSource.source(level, ModDamageSource.TURBOFAN), 1000);
					e.makeStuckInBlock(Blocks.COBWEB.defaultBlockState(), new Vec3(0.25D, 0.05F, 0.25D));

					if(!e.isAlive() && e instanceof LivingEntity) {
						level.playSound(null, e.getX(), e.getY(), e.getZ(), SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS, 2.0F, 0.95F + level.random.nextFloat() * 0.2F);

						blood.setFill(blood.getFill() + 50);
						if(blood.getFill() > blood.getMaxFill()) {
							blood.setFill(blood.getMaxFill());
						}
						this.showBlood = true;
					}
				}
			}

			if(this.power > maxPower) {
				this.power = maxPower;
			}

			this.networkPackNT(150);

		} else {

			this.lastSpin = this.spin;

			if(wasOn) {
				if(this.momentum < 100F)
					this.momentum++;
			} else {
				if(this.momentum > 0)
					this.momentum--;
			}

			this.spin += momentum / 2;

			if(this.spin >= 360) {
				this.spin -= 360F;
				this.lastSpin -= 360F;
			}

			if(momentum > 0) {

				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}

				audio.keepAlive();
				audio.updateVolume(getVolume(momentum / 50F));
				audio.updatePitch(momentum / 200F + 0.5F + this.afterburner * 0.16F);

			} else {

				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeByte((byte) afterburner);
		buf.writeBoolean(wasOn);
		buf.writeBoolean(showBlood);
		tank.serialize(buf);
		blood.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.afterburner = buf.readByte();
		this.wasOn = buf.readBoolean();
		this.showBlood = buf.readBoolean();
		tank.deserialize(buf);
		blood.deserialize(buf);
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return AudioWrapper.getLoopedSound("hbm:block.turbofanoperate", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 1.0F, 50F, 1.0F, 20);
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		if(audio != null) { audio.stopSound(); audio = null; }
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if(audio != null) { audio.stopSound(); audio = null; }
	}

	@Override public long getPower() { return power; }
	@Override public long getMaxPower() { return maxPower; }
	@Override public void setPower(long i) { this.power = i; }

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition).inflate(5);
	}

	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { tank }; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] { blood }; }
	@Override public FluidTank[] getAllTanks() { return new FluidTank[] { tank, blood, smoke, smoke_leaded, smoke_poison }; }

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineTurbofan(id, inv, this);
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.AFTERBURN;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_turbofan.get()));
		if(type == UpgradeType.AFTERBURN) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_EFFICIENCY, "+" + (int) (level * 100 * (1 + Math.min(level / 3D, 4D))) + "%"));
			info.add(ChatFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + (level * 100) + "%"));
		}
	}

	@Override
	public HashMap<UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.AFTERBURN, 3);
		return upgrades;
	}
}
