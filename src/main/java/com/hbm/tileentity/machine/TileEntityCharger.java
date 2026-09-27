package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.machine.Charger;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Charger: a wall mounted charging station, charges the batteries a player standing in front of it holds or wears
 * (held item and armor), takes power from the block it's mounted on. Folds out while someone is being charged.
 */
public class TileEntityCharger extends TileEntityLoadedBase implements IEnergyReceiverMK2 {

	private static final EquipmentSlot[] SLOTS = new EquipmentSlot[] {EquipmentSlot.MAINHAND, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

	private List<Player> players = new ArrayList<>();
	private long charge = 0;
	private int lastOp = 0;

	boolean particles = false;

	public int usingTicks;
	public int lastUsingTicks;
	public static final int delay = 20;

	public TileEntityCharger(BlockPos pos, BlockState state) {
		super(ModTileEntities.CHARGER.get(), pos, state);
	}

	/** Towards the wall it's mounted on */
	private Direction getWallSide() {
		return getBlockState().getValue(Charger.FACING).getOpposite();
	}

	@Override
	public void updateEntity() {

		Direction dir = getWallSide();

		if(!level.isClientSide) {

			this.trySubscribe(level, worldPosition.relative(dir), dir);

			players = level.getEntitiesOfClass(Player.class, new AABB(worldPosition.getX() + 0.5, worldPosition.getY(), worldPosition.getZ() + 0.5, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5).inflate(0.5, 0.0, 0.5));

			charge = 0;

			for(Player player : players) {
				for(EquipmentSlot slot : SLOTS) {
					ItemStack stack = player.getItemBySlot(slot);
					if(stack.getItem() instanceof IBatteryItem battery) {
						charge += Math.min(battery.getMaxCharge(stack) - battery.getCharge(stack), battery.getChargeRate(stack));
					}
				}
			}

			particles = lastOp > 0;

			if(particles) {
				lastOp--;
				if(level.getGameTime() % 20 == 0) level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.2F, 0.5F);
			}

			networkPackNT(50);
		}

		lastUsingTicks = usingTicks;

		if((charge > 0 || particles) && usingTicks < delay) {
			usingTicks++;
			if(usingTicks == 2) level.playLocalSound(worldPosition, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5F, 0.5F, false);
		}
		if((charge <= 0 && !particles) && usingTicks > 0) {
			usingTicks--;
			if(usingTicks == 4) level.playLocalSound(worldPosition, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5F, 0.5F, false);
		}

		if(particles && level.isClientSide) {
			RandomSource rand = level.random;
			level.addParticle(ParticleTypes.ENCHANTED_HIT,
					worldPosition.getX() + 0.5 + rand.nextDouble() * 0.0625 + dir.getStepX() * 0.75,
					worldPosition.getY() + 0.1,
					worldPosition.getZ() + 0.5 + rand.nextDouble() * 0.0625 + dir.getStepZ() * 0.75,
					-dir.getStepX() + rand.nextGaussian() * 0.1,
					0,
					-dir.getStepZ() + rand.nextGaussian() * 0.1);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(this.charge);
		buf.writeBoolean(this.particles);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.charge = buf.readLong();
		this.particles = buf.readBoolean();
	}

	@Override public long getPower() { return 0; }
	/** The demand is whatever the batteries in reach can take this tick */
	@Override public long getMaxPower() { return charge; }
	@Override public void setPower(long power) { }

	@Override
	public long transferPower(long power) {

		if(this.usingTicks < delay || power == 0) return power;

		for(Player player : players) {
			for(EquipmentSlot slot : SLOTS) {
				ItemStack stack = player.getItemBySlot(slot);
				if(stack.getItem() instanceof IBatteryItem battery) {
					long toCharge = Math.min(battery.getMaxCharge(stack) - battery.getCharge(stack), battery.getChargeRate(stack));
					toCharge = Math.min(toCharge, Math.max(power / 5, 1));
					battery.chargeBattery(stack, toCharge);
					power -= toCharge;
					lastOp = 4;
				}
			}
		}

		return power;
	}
}
