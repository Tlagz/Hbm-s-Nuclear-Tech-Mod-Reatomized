package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.potion.HbmPotion;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Decontamination shower: everything standing on it loses half a RAD per tick, its radiation poisoning effect and
 * all contamination. Needs no power. Rising mycelium specks (the original's "townaura" vanillaExt) on the client.
 */
public class TileEntityDecon extends TileEntityLoadedBase {

	public TileEntityDecon(BlockPos pos, BlockState state) {
		super(ModTileEntities.DECON.get(), pos, state);
	}

	@Override
	public void updateEntity() {

		if(!level.isClientSide) {

			List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, new AABB(worldPosition.getX() - 0.5, worldPosition.getY(), worldPosition.getZ() - 0.5, worldPosition.getX() + 1.5, worldPosition.getY() + 2, worldPosition.getZ() + 1.5));

			for(LivingEntity e : entities) {
				HbmLivingProps.incrementRadiation(e, -0.5F);
				e.removeEffect(HbmPotion.radiation);
				HbmLivingProps.getCont(e).clear();
			}
		} else {

			RandomSource rand = level.random;
			level.addParticle(ParticleTypes.MYCELIUM, worldPosition.getX() + 0.125 + rand.nextDouble() * 0.75, worldPosition.getY() + 1.1, worldPosition.getZ() + 0.125 + rand.nextDouble() * 0.75, 0.0, 0.04, 0.0);
		}
	}
}
