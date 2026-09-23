package com.hbm.handler.radiation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.level.ChunkDataEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

public abstract class ChunkRadiationHandler {

	/**
	 * Updates the radiation system, i.e. all worlds.
	 * Doesn't need parameters because it governs the ENTIRE system.
	 */
	public abstract void updateSystem();
	public abstract float getRadiation(Level world, BlockPos pos);
	public abstract void setRadiation(Level world, BlockPos pos, float rad);
	public abstract void incrementRad(Level world, BlockPos pos, float rad);
	public abstract void decrementRad(Level world, BlockPos pos, float rad);
	public abstract void clearSystem(Level world);

	/*
	 * Proxy'd event handlers
	 */
	public void receiveWorldLoad(LevelEvent.Load event) { }
	public void receiveWorldUnload(LevelEvent.Unload event) { }
	public void receiveWorldTick() { }

	public void receiveChunkLoad(ChunkDataEvent.Load event) { }
	public void receiveChunkSave(ChunkDataEvent.Save event) { }
	public void receiveChunkUnload(ChunkEvent.Unload event) { }

	public void handleWorldDestruction() { }
}
