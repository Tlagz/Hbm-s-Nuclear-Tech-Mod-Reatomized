package com.hbm.datagen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.network.BlockCable;
import com.hbm.lib.RefStrings;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.loaders.ObjModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {

	public ModBlockStateProvider(PackOutput output, ExistingFileHelper efh) {
		super(output, RefStrings.MODID, efh);
	}

	@Override
	protected void registerStatesAndModels() {
		cube(ModBlocks.ore_uranium);
		cube(ModBlocks.ore_titanium);
		cube(ModBlocks.block_uranium);
		cube(ModBlocks.block_titanium);
		cube(ModBlocks.block_steel);

		cable(ModBlocks.red_cable, "blocks/cable_neo", "blocks/cable_neo");
	}

	/** Simple full cube using the original's texture location, textures/blocks/[name].png */
	private void cube(DeferredBlock<? extends Block> block) {
		String name = block.getId().getPath();
		simpleBlockWithItem(block.get(), models().cubeAll(name, texture("blocks/" + name)));
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
