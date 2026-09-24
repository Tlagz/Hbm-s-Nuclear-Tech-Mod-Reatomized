package com.hbm.items;

import java.util.function.Supplier;

import com.hbm.lib.RefStrings;
import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Item stack data, the replacement for the stack NBT the original used */
public class ModDataComponents {

	public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(RefStrings.MODID);

	/** Stored energy of batteries (the original's "charge" NBT long) */
	public static final Supplier<DataComponentType<Long>> CHARGE = COMPONENTS.registerComponentType("charge",
			builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

	/**
	 * Machine data kept when the block is broken (the original's IPersistentNBT "persistent" tag), e.g. a barrel's
	 * tank. Written by the tile's collectImplicitComponents, copied to the drop by the loot table, applied on placement.
	 */
	public static final Supplier<DataComponentType<net.minecraft.world.item.component.CustomData>> PERSISTENT = COMPONENTS.registerComponentType("persistent",
			builder -> builder.persistent(net.minecraft.world.item.component.CustomData.CODEC).networkSynchronized(net.minecraft.world.item.component.CustomData.STREAM_CODEC));

	/** Fluid ID of fluid containers and identifiers, the original stored it as the item damage */
	public static final Supplier<DataComponentType<Integer>> FLUID_TYPE = COMPONENTS.registerComponentType("fluid_type",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

	/** Blueprint pool of a blueprint item (the original's "pool" NBT string) */
	public static final Supplier<DataComponentType<String>> BLUEPRINT_POOL = COMPONENTS.registerComponentType("blueprint_pool",
			builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

	/** Secondary fluid of the multi fluid identifier (the original's "fluid2" NBT) */
	public static final Supplier<DataComponentType<Integer>> FLUID_TYPE_SECONDARY = COMPONENTS.registerComponentType("fluid_type_secondary",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
}
