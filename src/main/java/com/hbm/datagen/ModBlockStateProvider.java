package com.hbm.datagen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineCapacitor;
import com.hbm.blocks.machine.MachineElectricFurnace;
import com.hbm.blocks.network.BlockCable;
import com.hbm.lib.RefStrings;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.loaders.CompositeModelBuilder;
import net.neoforged.neoforge.client.model.generators.loaders.ObjModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {

	public ModBlockStateProvider(PackOutput output, ExistingFileHelper efh) {
		super(output, RefStrings.MODID, efh);
	}

	@Override
	protected void registerStatesAndModels() {
		ModBlocks.MODELS.forEach(this::generated);

		cable(ModBlocks.red_cable, "blocks/cable_neo", "blocks/cable_neo");

		for(var capacitor : List.of(ModBlocks.capacitor_copper, ModBlocks.capacitor_gold, ModBlocks.capacitor_niobium, ModBlocks.capacitor_tantalium, ModBlocks.capacitor_schrabidate)) {
			capacitor(capacitor);
		}

		// bus: output texture on the facing side
		ModelFile bus = models().cube("capacitor_bus", texture("blocks/capacitor_bus_side"), texture("blocks/capacitor_bus_side"), texture("blocks/capacitor_bus_out"),
				texture("blocks/capacitor_bus_side"), texture("blocks/capacitor_bus_side"), texture("blocks/capacitor_bus_side")).texture("particle", texture("blocks/capacitor_bus_side"));
		directionalBlock(ModBlocks.capacitor_bus.get(), bus);
		simpleBlockItem(ModBlocks.capacitor_bus.get(), bus);

		// blocks rendered by their tile entity: only a particle texture, the item uses the NTM item renderer
		ModBlocks.TILE_RENDERED.forEach((holder, particle) -> {
			ModelFile model = models().getBuilder(holder.getId().getPath()).texture("particle", texture(particle));
			getVariantBuilder(holder.get()).forAllStates(state -> ConfiguredModel.builder().modelFile(model).build());
			// display like vanilla's block/block (the 1.7.10 renderers expected those hand transforms), except the GUI
			// which ItemRenderBase sets up itself to match the original inventory rendering
			itemModels().getBuilder(holder.getId().getPath()).parent(new ModelFile.UncheckedModelFile("builtin/entity")).transforms()
					.transform(ItemDisplayContext.GROUND).translation(0, 3, 0).scale(0.25F).end()
					.transform(ItemDisplayContext.FIXED).scale(0.5F).end()
					.transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(75, 45, 0).translation(0, 2.5F, 0).scale(0.375F).end()
					.transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(75, 45, 0).translation(0, 2.5F, 0).scale(0.375F).end()
					.transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 45, 0).scale(0.4F).end()
					.transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 225, 0).scale(0.4F).end();
		});

		// original: separate _off/_on blocks, now LIT state
		ModelFile furnaceOff = models().orientableWithBottom("machine_electric_furnace_off", texture("blocks/machine_electric_furnace_side"),
				texture("blocks/machine_electric_furnace_front_off"), texture("blocks/machine_electric_furnace_bottom"), texture("blocks/machine_electric_furnace_top"));
		ModelFile furnaceOn = models().orientableWithBottom("machine_electric_furnace_on", texture("blocks/machine_electric_furnace_side"),
				texture("blocks/machine_electric_furnace_front_on"), texture("blocks/machine_electric_furnace_bottom"), texture("blocks/machine_electric_furnace_top"));
		horizontalBlock(ModBlocks.machine_electric_furnace_off.get(), state -> state.getValue(MachineElectricFurnace.LIT) ? furnaceOn : furnaceOff);
		simpleBlockItem(ModBlocks.machine_electric_furnace_off.get(), furnaceOff);
	}

	/** Models of generated blocks, see ModBlocks.BlockModel */
	private void generated(DeferredBlock<?> holder, ModBlocks.BlockModel model) {
		String name = holder.getId().getPath();
		Block block = holder.get();

		switch(model.type()) {
		case "cube" -> simpleBlockWithItem(block, models().cubeAll(name, texture(model.texture())));
		case "column" -> simpleBlockWithItem(block, models().cubeColumn(name, texture(model.texture()), texture(model.end())));
		case "axis" -> {
			axisBlock((RotatedPillarBlock) block, texture(model.texture()), texture(model.end()));
			simpleBlockItem(block, models().getExistingFile(modLoc("block/" + name)));
		}
		case "stairs" -> {
			stairsBlock((StairBlock) block, texture(model.texture()));
			simpleBlockItem(block, models().getExistingFile(modLoc("block/" + name)));
		}
		case "glass", "glass_translucent" -> simpleBlockWithItem(block, models().cubeAll(name, texture(model.texture()))
				.renderType(model.type().equals("glass") ? "cutout" : "translucent"));
		default -> throw new IllegalArgumentException("Unknown model type " + model.type());
		}
	}

	/**
	 * Cables use the original OBJ (Core, posX..negZ arms and CX/CY/CZ straight pieces), one model per
	 * visible part combination, mirroring the original RenderCable logic.
	 * Note the original's posZ/negZ OBJ parts are swapped compared to their names.
	 */
	private void cable(DeferredBlock<? extends Block> block, String obj, String tex) {
		String name = block.getId().getPath();
		Map<String, ModelFile> cache = new HashMap<>();

		getVariantBuilder(block.get()).forAllStates(state -> {
			boolean pX = state.getValue(BlockCable.CONNECTIONS.get(Direction.EAST));
			boolean nX = state.getValue(BlockCable.CONNECTIONS.get(Direction.WEST));
			boolean pY = state.getValue(BlockCable.CONNECTIONS.get(Direction.UP));
			boolean nY = state.getValue(BlockCable.CONNECTIONS.get(Direction.DOWN));
			boolean pZ = state.getValue(BlockCable.CONNECTIONS.get(Direction.SOUTH));
			boolean nZ = state.getValue(BlockCable.CONNECTIONS.get(Direction.NORTH));

			List<String> parts;
			if(pX && nX && !pY && !nY && !pZ && !nZ) parts = List.of("CX");
			else if(!pX && !nX && pY && nY && !pZ && !nZ) parts = List.of("CY");
			else if(!pX && !nX && !pY && !nY && pZ && nZ) parts = List.of("CZ");
			else {
				parts = new java.util.ArrayList<>(List.of("Core"));
				if(pX) parts.add("posX");
				if(nX) parts.add("negX");
				if(pY) parts.add("posY");
				if(nY) parts.add("negY");
				if(nZ) parts.add("posZ");
				if(pZ) parts.add("negZ");
			}

			String key = String.join("_", parts).toLowerCase();
			ModelFile model = cache.computeIfAbsent(key, k -> objModel(name + "_" + k, obj, tex, parts));
			return ConfiguredModel.builder().modelFile(model).build();
		});

		// inventory: core with the four horizontal arms, like the original's inventory render
		simpleBlockItem(block.get(), objModel(name + "_inventory", obj, tex, List.of("Core", "posX", "negX", "posZ", "negZ")));
	}

	/**
	 * Capacitors: the original's capacitor.obj with one texture per part (Top, Side, Bottom, InnerTop, InnerSide),
	 * combined with the composite loader. The model points up, the facing is applied as a rotation.
	 */
	private void capacitor(DeferredBlock<? extends Block> block) {
		String name = block.getId().getPath();
		String mat = ((MachineCapacitor) block.get()).name;
		BlockModelBuilder model = models().getBuilder(name).parent(models().getExistingFile(mcLoc("block/block")))
				.texture("particle", texture("blocks/capacitor_" + mat + "_side"));
		CompositeModelBuilder<BlockModelBuilder> composite = model.customLoader(CompositeModelBuilder::begin);
		String[][] parts = { {"Top", "top"}, {"Side", "side"}, {"Bottom", "bottom"}, {"InnerTop", "inner_top"}, {"InnerSide", "inner_side"} };
		for(String[] part : parts) {
			BlockModelBuilder child = models().nested().texture("texture0", texture("blocks/capacitor_" + mat + "_" + part[1]));
			ObjModelBuilder<BlockModelBuilder> obj = child.customLoader(ObjModelBuilder::begin).modelLocation(modLoc("models/blocks/capacitor.obj")).flipV(true).automaticCulling(false);
			for(String[] other : parts) obj.visibility(other[0], other[0].equals(part[0]));
			obj.end();
			child.rootTransforms().translation(0.5F, 0.5F, 0.5F);
			composite.child(part[1], child);
		}
		composite.end();

		getVariantBuilder(block.get()).forAllStates(state -> {
			Direction dir = state.getValue(MachineCapacitor.FACING);
			int x = dir == Direction.DOWN ? 180 : dir.getAxis().isHorizontal() ? 90 : 0;
			int y = dir.getAxis().isHorizontal() ? ((int) dir.toYRot() + 180) % 360 : 0;
			return ConfiguredModel.builder().modelFile(model).rotationX(x).rotationY(y).build();
		});
		simpleBlockItem(block.get(), model);
	}

	private static final List<String> CABLE_PARTS = List.of("Core", "posX", "negX", "posY", "negY", "posZ", "negZ", "CX", "CY", "CZ");

	private BlockModelBuilder objModel(String name, String obj, String tex, List<String> visible) {
		ResourceLocation texture = texture(tex);
		ObjModelBuilder<BlockModelBuilder> loader = models().getBuilder(name)
				.parent(models().getExistingFile(mcLoc("block/block")))
				.texture("texture0", texture)
				.texture("particle", texture)
				.customLoader(ObjModelBuilder::begin)
				.modelLocation(modLoc("models/" + obj + ".obj"))
				.flipV(true)
				.automaticCulling(false);
		for(String part : CABLE_PARTS) loader.visibility(part, visible.contains(part));
		BlockModelBuilder model = loader.end();
		// the original OBJs are centered on the origin, block models span 0..1
		model.rootTransforms().translation(0.5F, 0.5F, 0.5F);
		return model;
	}

	private ResourceLocation texture(String path) {
		ResourceLocation tex = modLoc(path);
		ModSpriteSourceProvider.USED_TEXTURES.add(tex);
		return tex;
	}
}
