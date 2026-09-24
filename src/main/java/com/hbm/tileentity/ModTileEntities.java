package com.hbm.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineCapacitor.TileEntityCapacitor;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineElectricFurnace;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.tileentity.machine.storage.TileEntityBarrel;
import com.hbm.tileentity.machine.TileEntityMachineWoodBurner;
import com.hbm.tileentity.machine.TileEntityMachineDiesel;
import com.hbm.tileentity.machine.oil.TileEntityMachineOilWell;
import com.hbm.tileentity.network.TileEntityCableBaseNT;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTileEntities {

	public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RefStrings.MODID);

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityCableBaseNT>> CABLE = TILES.register("cable",
			() -> BlockEntityType.Builder.of(TileEntityCableBaseNT::new, ModBlocks.red_cable.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityCapacitor>> CAPACITOR = TILES.register("capacitor",
			() -> BlockEntityType.Builder.of(TileEntityCapacitor::new, ModBlocks.capacitor_copper.get(), ModBlocks.capacitor_gold.get(),
					ModBlocks.capacitor_niobium.get(), ModBlocks.capacitor_tantalium.get(), ModBlocks.capacitor_schrabidate.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityMachineOilWell>> OIL_WELL = TILES.register("machine_well",
			() -> BlockEntityType.Builder.of(TileEntityMachineOilWell::new, ModBlocks.machine_well.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityMachineDiesel>> DIESEL = TILES.register("machine_diesel",
			() -> BlockEntityType.Builder.of(TileEntityMachineDiesel::new, ModBlocks.machine_diesel.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityBarrel>> BARREL = TILES.register("barrel",
			() -> BlockEntityType.Builder.of(TileEntityBarrel::new, ModBlocks.barrel_plastic.get(), ModBlocks.barrel_steel.get(), ModBlocks.barrel_tcalloy.get(), ModBlocks.barrel_antimatter.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityPipeBaseNT>> PIPE = TILES.register("pipe",
			() -> BlockEntityType.Builder.of(TileEntityPipeBaseNT::new, ModBlocks.fluid_duct_neo.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityProxyCombo>> PROXY_COMBO = TILES.register("proxy_combo",
			() -> BlockEntityType.Builder.of(TileEntityProxyCombo::new, ModBlocks.DUMMYABLES.stream().map(b -> (net.minecraft.world.level.block.Block) b.get()).toArray(net.minecraft.world.level.block.Block[]::new)).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityMachineWoodBurner>> WOOD_BURNER = TILES.register("machine_wood_burner",
			() -> BlockEntityType.Builder.of(TileEntityMachineWoodBurner::new, ModBlocks.machine_wood_burner.get()).build(null));

	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityMachineElectricFurnace>> ELECTRIC_FURNACE = TILES.register("machine_electric_furnace",
			() -> BlockEntityType.Builder.of(TileEntityMachineElectricFurnace::new, ModBlocks.machine_electric_furnace_off.get()).build(null));
}
