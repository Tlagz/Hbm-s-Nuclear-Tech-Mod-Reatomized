package com.hbm.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.network.TileEntityCableBaseNT;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTileEntities {

	public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RefStrings.MODID);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityCableBaseNT>> CABLE = TILES.register("cable",
			() -> BlockEntityType.Builder.of(TileEntityCableBaseNT::new, ModBlocks.red_cable.get()).build(null));
}
