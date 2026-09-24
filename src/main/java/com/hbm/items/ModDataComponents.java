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

	/** Fluid ID of fluid containers and identifiers, the original stored it as the item damage */
	public static final Supplier<DataComponentType<Integer>> FLUID_TYPE = COMPONENTS.registerComponentType("fluid_type",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

	/** Secondary fluid of the multi fluid identifier (the original's "fluid2" NBT) */
	public static final Supplier<DataComponentType<Integer>> FLUID_TYPE_SECONDARY = COMPONENTS.registerComponentType("fluid_type_secondary",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
}
