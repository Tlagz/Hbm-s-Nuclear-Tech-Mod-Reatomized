package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.Library;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.ArmorUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Tesla coil: 5000 HE per tick to zap every living thing within 10 blocks that it can see, damage split between
 * the targets. Creepers get charged instead, ocelots are spared, a meteorite battery below powers it for free.
 *
 * TODO taint/tesla/cyber crabs (healed or ignored in the original) once they exist
 */
public class TileEntityTesla extends TileEntityLoadedBase implements IEnergyReceiverMK2 {

	public long power;
	public static final long maxPower = 100000;

	public static int range = 10;
	public static double offset = 1.75;

	public List<double[]> targets = new ArrayList<>();

	public TileEntityTesla(BlockPos pos, BlockState state) {
		super(ModTileEntities.TESLA.get(), pos, state);
	}

	@Override
	public void updateEntity() {

		if(!level.isClientSide) {

			for(Direction dir : Direction.values()) this.trySubscribe(level, worldPosition.relative(dir), dir);

			this.targets.clear();

			if(level.getBlockState(worldPosition.below()).is(ModBlocks.meteor_battery.get()))
				power = maxPower;

			if(power >= 5000) {
				power -= 5000;

				double dx = worldPosition.getX() + 0.5;
				double dy = worldPosition.getY() + offset;
				double dz = worldPosition.getZ() + 0.5;

				this.targets = zap(level, dx, dy, dz, range, null);
			}

			this.networkPackNT(100);
		}
	}

	/** Zaps everything in range and sight, returns the beam end points */
	public static List<double[]> zap(Level world, double x, double y, double z, double radius, Entity source) {

		List<double[]> ret = new ArrayList<>();

		List<LivingEntity> targets = world.getEntitiesOfClass(LivingEntity.class, new AABB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius));

		for(LivingEntity e : targets) {

			if(e instanceof Ocelot || e == source)
				continue;

			Vec3 vec = new Vec3(e.getX() - x, e.getY() + e.getBbHeight() / 2 - y, e.getZ() - z);

			if(vec.length() > range)
				continue;

			if(Library.isObstructed(world, x, y, z, e.getX(), e.getY() + e.getBbHeight() / 2, e.getZ()))
				continue;

			if(e instanceof Creeper creeper) {
				// the original set the "powered" data watcher entry, same as a lightning strike
				CompoundTag nbt = new CompoundTag();
				creeper.addAdditionalSaveData(nbt);
				if(!nbt.getBoolean("powered")) {
					nbt.putBoolean("powered", true);
					creeper.readAdditionalSaveData(nbt);
				}
				ret.add(new double[] {e.getX(), e.getY() + e.getBbHeight() / 2, e.getZ()});
				continue;
			}

			if(!(e instanceof Player player && ArmorUtil.checkForFaraday(player)))
				if(e.hurt(ModDamageSource.source(world, ModDamageSource.ELECTRICITY), Mth.clamp(e.getMaxHealth() * 0.5F, 3, 20) / (float) targets.size()))
					world.playSound(null, e.getX(), e.getY(), e.getZ(), ModSounds.get("weapon.tesla"), SoundSource.BLOCKS, 1.0F, 1.0F);

			double offset = 0;

			if(source != null && e instanceof Player && world.isClientSide)
				offset = e.getBbHeight();

			ret.add(new double[] {e.getX(), e.getY() + e.getBbHeight() / 2 - offset, e.getZ()});
		}

		return ret;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeShort((short) targets.size());
		for(double[] d : this.targets) {
			buf.writeDouble(d[0]);
			buf.writeDouble(d[1]);
			buf.writeDouble(d[2]);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		int s = buf.readShort();

		this.targets.clear();

		for(int i = 0; i < s; i++)
			this.targets.add(new double[] {buf.readDouble(), buf.readDouble(), buf.readDouble()});
	}

	@Override public void setPower(long i) { power = i; }
	@Override public long getPower() { return power; }
	@Override public long getMaxPower() { return maxPower; }

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition).inflate(range + 1);
	}
}
