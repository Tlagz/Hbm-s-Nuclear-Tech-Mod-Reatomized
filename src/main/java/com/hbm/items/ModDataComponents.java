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

	/** Ticks left of an RTG pellet (the original's "PELLET_DEPLETION" NBT long), absent = full lifespan */
	public static final Supplier<DataComponentType<Long>> PELLET_DEPLETION = COMPONENTS.registerComponentType("pellet_depletion",
			builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

	/** Fluid ID of fluid containers and identifiers, the original stored it as the item damage */
	public static final Supplier<DataComponentType<Integer>> FLUID_TYPE = COMPONENTS.registerComponentType("fluid_type",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

	/** Fluid amount shown by fluid icons (the original's "fill" NBT) */
	public static final Supplier<DataComponentType<Integer>> FLUID_FILL = COMPONENTS.registerComponentType("fluid_fill",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

	/** Fluid pressure shown by fluid icons (the original's "pressure" NBT) */
	public static final Supplier<DataComponentType<Integer>> FLUID_PRESSURE = COMPONENTS.registerComponentType("fluid_pressure",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

	/** Blueprint pool of a blueprint item (the original's "pool" NBT string) */
	public static final Supplier<DataComponentType<String>> BLUEPRINT_POOL = COMPONENTS.registerComponentType("blueprint_pool",
			builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

	/** Uses of an arc furnace electrode (the original's "durability" NBT, counting up) */
	public static final Supplier<DataComponentType<Integer>> ELECTRODE_WEAR = COMPONENTS.registerComponentType("electrode_wear",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

	/** Material id, amount in quanta and liquid flag of foundry scraps (the original's item damage and NBT) */
	public static final Supplier<DataComponentType<Integer>> SCRAP_MATERIAL = COMPONENTS.registerComponentType("scrap_material",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
	public static final Supplier<DataComponentType<Integer>> SCRAP_AMOUNT = COMPONENTS.registerComponentType("scrap_amount",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));
	public static final Supplier<DataComponentType<Boolean>> SCRAP_LIQUID = COMPONENTS.registerComponentType("scrap_liquid",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

	/** A Stirling engine item without its gear (the original's item damage 1) */
	public static final Supplier<DataComponentType<Boolean>> NO_COG = COMPONENTS.registerComponentType("no_cog",
			builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

	/** Secondary fluid of the multi fluid identifier (the original's "fluid2" NBT) */
	public static final Supplier<DataComponentType<Integer>> FLUID_TYPE_SECONDARY = COMPONENTS.registerComponentType("fluid_type_secondary",
			builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

	/** A position a tool is linked to, e.g. the mirror tool's boiler (the original's posX/posY/posZ NBT) */
	public static final Supplier<DataComponentType<net.minecraft.core.BlockPos>> LINKED_POS = COMPONENTS.registerComponentType("linked_pos",
			builder -> builder.persistent(net.minecraft.core.BlockPos.CODEC).networkSynchronized(net.minecraft.core.BlockPos.STREAM_CODEC));
}
