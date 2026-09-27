package com.hbm.datagen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineCapacitor;
import com.hbm.blocks.machine.MachineElectricFurnace;
import com.hbm.blocks.network.BlockCable;
import com.hbm.blocks.network.FluidDuctStandard;
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
		pipe(ModBlocks.fluid_duct_neo);
		for(var barrel : ModBlocks.BARRELS) barrel(barrel);
		ModBlocks.ANVILS.forEach(this::anvil);
		ModBlocks.SCAFFOLDS.forEach(this::scaffold);
		decoBlocks();
		for(var pipe : ModBlocks.PIPES) decoPipe(pipe);
		grate(ModBlocks.steel_grate, "blocks/grate_top");
		grate(ModBlocks.steel_grate_wide, "blocks/grate_wide_top");
		metalFence(ModBlocks.fence_metal);
		metalFence(ModBlocks.fence_metal_post);
		foundryVessel(ModBlocks.foundry_mold, "mold", 8);
		foundryVessel(ModBlocks.foundry_basin, "basin", 16);
		foundryChannel();
		foundryOutlet(ModBlocks.foundry_outlet, "foundry_outlet");
		foundryOutlet(ModBlocks.foundry_slagtap, "foundry_slagtap");
		foundryTank();
		dynamicSlag();

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
		ModelFile brickOff = models().orientableWithBottom("machine_furnace_brick_off", texture("blocks/machine_furnace_brick_side"),
				texture("blocks/machine_furnace_brick_front_off"), texture("blocks/machine_furnace_brick_bottom"), texture("blocks/machine_furnace_brick_top"));
		ModelFile brickOn = models().orientableWithBottom("machine_furnace_brick_on", texture("blocks/machine_furnace_brick_side"),
				texture("blocks/machine_furnace_brick_front_on"), texture("blocks/machine_furnace_brick_bottom"), texture("blocks/machine_furnace_brick_top"));
		horizontalBlock(ModBlocks.machine_furnace_brick_off.get(), state -> state.getValue(com.hbm.blocks.machine.MachineBrickFurnace.LIT) ? brickOn : brickOff);
		simpleBlockItem(ModBlocks.machine_furnace_brick_off.get(), brickOff);

		ModelFile turbine = models().cubeBottomTop("machine_turbine", texture("blocks/machine_turbine_base"), texture("blocks/machine_turbine_top"), texture("blocks/machine_turbine_top"));
		simpleBlock(ModBlocks.machine_turbine.get(), turbine);
		simpleBlockItem(ModBlocks.machine_turbine.get(), turbine);

		ModelFile deuterium = models().cubeBottomTop("machine_deuterium_extractor", texture("blocks/deuterium_extractor_side"), texture("blocks/deuterium_extractor_top_water"), texture("blocks/deuterium_extractor_top_water"));
		simpleBlock(ModBlocks.machine_deuterium_extractor.get(), deuterium);
		simpleBlockItem(ModBlocks.machine_deuterium_extractor.get(), deuterium);

		ModelFile rtgFurnaceOff = models().orientableWithBottom("machine_rtg_furnace_off", texture("blocks/machine_rtg_furnace_side_alt"),
				texture("blocks/machine_rtg_furnace_off_alt"), texture("blocks/machine_rtg_furnace_base_alt"), texture("blocks/machine_rtg_furnace_base_alt"));
		ModelFile rtgFurnaceOn = models().orientableWithBottom("machine_rtg_furnace_on", texture("blocks/machine_rtg_furnace_side_alt"),
				texture("blocks/machine_rtg_furnace_on_alt"), texture("blocks/machine_rtg_furnace_base_alt"), texture("blocks/machine_rtg_furnace_base_alt"));
		horizontalBlock(ModBlocks.machine_rtg_furnace_off.get(), state -> state.getValue(com.hbm.blocks.machine.MachineRtgFurnace.LIT) ? rtgFurnaceOn : rtgFurnaceOff);
		simpleBlockItem(ModBlocks.machine_rtg_furnace_off.get(), rtgFurnaceOff);

		// the funnel is the original's funnel.obj, split into one material per part
		BlockModelBuilder funnel = models().getBuilder("machine_funnel").parent(models().getExistingFile(mcLoc("block/block")))
				.texture("top", texture("blocks/machine_funnel_top")).texture("bottom", texture("blocks/machine_funnel_bottom")).texture("side", texture("blocks/machine_funnel_side"))
				.texture("particle", texture("blocks/machine_funnel_side")).renderType("cutout")
				.customLoader(ObjModelBuilder::begin).modelLocation(modLoc("models/blocks/funnel_block.obj")).flipV(true).automaticCulling(false).end();
		funnel.rootTransforms().translation(0.5F, 0F, 0.5F);
		simpleBlock(ModBlocks.machine_funnel.get(), funnel);
		simpleBlockItem(ModBlocks.machine_funnel.get(), funnel);

		// alloy furnace: LIT instead of the original's on/off blocks, EXTENDED (extension on top) uses the tall textures
		ModelFile[][] diFurnace = new ModelFile[2][2];
		for(int lit = 0; lit < 2; lit++) for(int ext = 0; ext < 2; ext++) {
			String on = lit == 1 ? "on" : "off";
			diFurnace[lit][ext] = models().orientableWithBottom("machine_difurnace_" + on + (ext == 1 ? "_extended" : ""),
					texture(ext == 1 ? "blocks/difurnace_side_tall" : "blocks/difurnace_side_alt"),
					texture("blocks/difurnace_front_" + on + (ext == 1 ? "_tall" : "_alt")),
					texture("blocks/brick_fire"),
					texture(ext == 1 ? "blocks/brick_fire" : "blocks/difurnace_top_" + on + "_alt"));
		}
		horizontalBlock(ModBlocks.machine_difurnace_off.get(), state -> diFurnace[state.getValue(com.hbm.blocks.machine.MachineDiFurnace.LIT) ? 1 : 0][state.getValue(com.hbm.blocks.machine.MachineDiFurnace.EXTENDED) ? 1 : 0]);
		simpleBlockItem(ModBlocks.machine_difurnace_off.get(), diFurnace[0][0]);

		// the extension is the original's difurnace_extension.obj, split into one material per part, centered on the block
		BlockModelBuilder extension = models().getBuilder("machine_difurnace_extension").parent(models().getExistingFile(mcLoc("block/block")))
				.texture("top", texture("blocks/difurnace_top_off_alt")).texture("bottom", texture("blocks/brick_fire")).texture("side", texture("blocks/difurnace_extension"))
				.texture("particle", texture("blocks/difurnace_extension")).renderType("cutout")
				.customLoader(ObjModelBuilder::begin).modelLocation(modLoc("models/blocks/difurnace_extension_block.obj")).flipV(true).automaticCulling(false).end();
		extension.rootTransforms().translation(0.5F, 0F, 0.5F);
		simpleBlock(ModBlocks.machine_difurnace_extension.get(), extension);
		simpleBlockItem(ModBlocks.machine_difurnace_extension.get(), extension);

		// the shredder has its front texture on north and south, the side texture on east and west
		ModelFile shredder = models().cube("machine_shredder", texture("blocks/machine_shredder_bottom_alt"), texture("blocks/machine_shredder_top_alt"),
				texture("blocks/machine_shredder_front_alt"), texture("blocks/machine_shredder_front_alt"), texture("blocks/machine_shredder_side_alt"), texture("blocks/machine_shredder_side_alt"))
				.texture("particle", texture("blocks/machine_shredder_side_alt"));
		simpleBlock(ModBlocks.machine_shredder.get(), shredder);
		simpleBlockItem(ModBlocks.machine_shredder.get(), shredder);

		// the safe's door faces the player
		ModelFile safe = models().orientable("safe", texture("blocks/safe_side"), texture("blocks/safe_front"), texture("blocks/safe_side"));
		horizontalBlock(ModBlocks.safe.get(), safe);
		simpleBlockItem(ModBlocks.safe.get(), safe);
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

	/**
	 * NTM anvils: the original's anvil.obj, the top part has its own texture (the murky anvil's differs).
	 * The model is long along z, north/south facing anvils are turned by 90 degrees like the original's ISBRH.
	 */
	private void anvil(DeferredBlock<? extends Block> block, String[] textures) {
		String name = block.getId().getPath();
		ResourceLocation side = texture(textures[0]);
		ResourceLocation top = texture(textures[1]);
		BlockModelBuilder model = models().getBuilder(name).parent(models().getExistingFile(mcLoc("block/block"))).texture("particle", side);
		CompositeModelBuilder<BlockModelBuilder> composite = model.customLoader(CompositeModelBuilder::begin);
		for(int part = 0; part < 2; part++) {
			BlockModelBuilder child = models().nested().texture("texture0", part == 0 ? top : side);
			ObjModelBuilder<BlockModelBuilder> obj = child.customLoader(ObjModelBuilder::begin).modelLocation(modLoc("models/blocks/anvil.obj")).flipV(true).automaticCulling(false);
			for(String p : List.of("Top", "Bottom", "Front", "Back", "Left", "Right")) obj.visibility(p, part == 0 ? p.equals("Top") : !p.equals("Top"));
			obj.end();
			child.rootTransforms().translation(0.5F, 0, 0.5F);
			composite.child(part == 0 ? "top" : "sides", child);
		}
		composite.end();

		getVariantBuilder(block.get()).forAllStates(state -> {
			Direction dir = state.getValue(com.hbm.blocks.machine.NTMAnvil.FACING);
			return ConfiguredModel.builder().modelFile(model).rotationY(dir.getAxis() == Direction.Axis.Z ? 90 : 0).build();
		});
		simpleBlockItem(block.get(), model);
	}

	/**
	 * Scaffolds: the original's RenderScaffoldBlock turned scaffold.obj by a yaw and a pitch per orientation and moved
	 * it into the block, exactly that transform is the model's root transform (ObjUtil: v' = Ry(rot) * Rz(-pitch) * v).
	 * The OBJ loader moves the root transform's origin by +0.5 (blockCenterToCorner), the -0.5 origin cancels that so
	 * the rotation happens around the model's own origin like in the original.
	 */
	private void scaffold(DeferredBlock<? extends Block> block, String textureName) {
		String name = block.getId().getPath();
		ResourceLocation texture = texture("blocks/" + textureName);
		float[][] orientations = {
				// rot, pitch, offset
				{ (float) -Math.PI * 0.5F, 0, 0.5F, 0, 0.5F },
				{ (float) -Math.PI * 0.5F, (float) Math.PI * 0.5F, 0.5F, 0.5F, 0 },
				{ (float) -Math.PI, 0, 0.5F, 0, 0.5F },
				{ (float) -Math.PI, (float) Math.PI * 0.5F, 1, 0.5F, 0.5F }
		};
		BlockModelBuilder[] models = new BlockModelBuilder[4];
		for(int i = 0; i < 4; i++) {
			float[] o = orientations[i];
			models[i] = models().getBuilder(name + "_" + i).parent(models().getExistingFile(mcLoc("block/block")))
					.texture("texture0", texture).texture("particle", texture).renderType("cutout")
					.customLoader(ObjModelBuilder::begin).modelLocation(modLoc("models/blocks/scaffold.obj")).flipV(true).automaticCulling(false).end();
			models[i].rootTransforms().translation(o[2], o[3], o[4]).rotation(new org.joml.Quaternionf().rotateY(o[0]).rotateZ(-o[1])).origin(new org.joml.Vector3f(-0.5F, -0.5F, -0.5F));
		}
		getVariantBuilder(block.get()).forAllStates(state -> ConfiguredModel.builder().modelFile(models[state.getValue(com.hbm.blocks.generic.BlockScaffold.ORIENTATION)]).build());
		simpleBlockItem(block.get(), models[0]);
	}

	/**
	 * Steel walls and corners: the boxes of their shape with the wall texture on every face (the original rendered its
	 * bounds with renderStandardBlock), one model per facing. The roof is the original's ModelSteelRoof, the beam beam.obj.
	 */
	private void decoBlocks() {
		for(var block : List.of(ModBlocks.steel_wall, ModBlocks.steel_corner)) {
			String name = block.getId().getPath();
			ResourceLocation tex = texture("blocks/steel_wall");
			Map<Direction, ModelFile> models = new HashMap<>();
			for(Direction dir : Direction.Plane.HORIZONTAL) {
				BlockModelBuilder model = models().getBuilder(name + "_" + dir.getName()).parent(models().getExistingFile(mcLoc("block/block"))).texture("wall", tex).texture("particle", tex);
				var shape = block.get().type == com.hbm.blocks.generic.DecoBlock.Type.WALL ? com.hbm.blocks.generic.DecoBlock.wall(dir) : com.hbm.blocks.generic.DecoBlock.corner(dir);
				for(var box : shape.toAabbs()) {
					model.element().from((float) box.minX * 16, (float) box.minY * 16, (float) box.minZ * 16).to((float) box.maxX * 16, (float) box.maxY * 16, (float) box.maxZ * 16)
							.allFaces((face, builder) -> builder.texture("#wall")).end();
				}
				models.put(dir, model);
			}
			getVariantBuilder(block.get()).forAllStates(state -> ConfiguredModel.builder().modelFile(models.get(state.getValue(com.hbm.blocks.generic.DecoBlock.FACING))).build());
			simpleBlockItem(block.get(), models.get(Direction.SOUTH));
		}

		// ModelSteelRoof: a plate with two ridges, 64x32 box UVs, drawn upside down around the block like every 1.7.10 ModelBase
		BlockModelBuilder roof = models().getBuilder("steel_roof").parent(models().getExistingFile(mcLoc("block/block")))
				.texture("roof", texture("models/steelroof")).texture("particle", texture("blocks/steel_roof"));
		modelBox(roof, "#roof", 64, 32, -8, 23, -8, 16, 1, 16, 0, 0);
		modelBox(roof, "#roof", 64, 32, -3, 22, -8, 1, 1, 16, 30, 15);
		modelBox(roof, "#roof", 64, 32, -8, 21, 2, 16, 2, 2, 0, 17);
		simpleBlock(ModBlocks.steel_roof.get(), roof);
		simpleBlockItem(ModBlocks.steel_roof.get(), roof);

		ResourceLocation beamTex = texture("blocks/steel_beam");
		BlockModelBuilder beam = models().getBuilder("steel_beam").parent(models().getExistingFile(mcLoc("block/block")))
				.texture("texture0", beamTex).texture("particle", beamTex)
				.customLoader(ObjModelBuilder::begin).modelLocation(modLoc("models/blocks/beam.obj")).flipV(true).automaticCulling(false).end();
		beam.rootTransforms().translation(0.5F, 0, 0.5F);
		simpleBlock(ModBlocks.steel_beam.get(), beam);
		simpleBlockItem(ModBlocks.steel_beam.get(), beam);
	}

	/**
	 * One box of a 1.7.10 ModelBase (ModelRenderer.addBox at a rotation point, (x, y, z) = point + offset) as a model
	 * element. Tile entity renderers drew those models turned upside down around the block's bottom center,
	 * so model (x, y, z) is block pixel (8 - x, 24 - y, 8 + z). The faces get the ModelBox texture layout at (u, v).
	 */
	private static void modelBox(BlockModelBuilder model, String texture, int texW, int texH, float x, float y, float z, int dx, int dy, int dz, int u, int v) {
		float su = 16F / texW, sv = 16F / texH;
		var element = model.element().from(8 - x - dx, 24 - y - dy, 8 + z).to(8 - x, 24 - y, 8 + z + dz);
		// ModelBox: -x face (world east), +x (west), top, bottom, -z (north), +z (south)
		float[][] rects = {
				{ u, v + dz, u + dz, v + dz + dy },
				{ u + dz + dx, v + dz, u + dz + dx + dz, v + dz + dy },
				{ u + dz, v, u + dz + dx, v + dz },
				{ u + dz + dx, v, u + dz + dx + dx, v + dz },
				{ u + dz, v + dz, u + dz + dx, v + dz + dy },
				{ u + dz + dx + dz, v + dz, u + dz + dx + dz + dx, v + dz + dy } };
		Direction[] faces = { Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH };
		for(int i = 0; i < 6; i++) {
			float[] r = rects[i];
			element.face(faces[i]).texture(texture).uvs(r[0] * su, r[1] * sv, r[2] * su, r[3] * sv).end();
		}
		element.end();
	}

	/**
	 * Decorative pipes: pipe.obj / pipe_rim.obj / pipe_quad.obj with the end texture on "Top" and the side texture on
	 * "Side", framed pipes add pipe_frame.obj's frame and mesh. Pointing up, turned onto the axis like a log.
	 */
	private void decoPipe(DeferredBlock<com.hbm.blocks.generic.BlockPipe> block) {
		var pipe = block.get();
		String name = block.getId().getPath();
		BlockModelBuilder model = models().getBuilder(name).parent(models().getExistingFile(mcLoc("block/block"))).texture("particle", texture(pipe.side));
		CompositeModelBuilder<BlockModelBuilder> composite = model.customLoader(CompositeModelBuilder::begin);
		String[][] parts = pipe.style == com.hbm.blocks.generic.BlockPipe.Style.FRAMED
				? new String[][] { {pipe.style.model, "Top", pipe.top}, {pipe.style.model, "Side", pipe.side}, {"pipe_frame", "Frame", "blocks/pipe_frame"}, {"pipe_frame", "Mesh", "blocks/pipe_mesh"} }
				: new String[][] { {pipe.style.model, "Top", pipe.top}, {pipe.style.model, "Side", pipe.side} };
		for(String[] part : parts) {
			BlockModelBuilder child = models().nested().texture("texture0", texture(part[2])).renderType("cutout");
			ObjModelBuilder<BlockModelBuilder> obj = child.customLoader(ObjModelBuilder::begin).modelLocation(modLoc("models/blocks/" + part[0] + ".obj")).flipV(true).automaticCulling(false);
			for(String other : part[0].equals("pipe_frame") ? List.of("Frame", "Mesh") : List.of("Top", "Side")) obj.visibility(other, other.equals(part[1]));
			obj.end();
			child.rootTransforms().translation(0.5F, 0.5F, 0.5F);
			composite.child(part[1].toLowerCase(), child);
		}
		composite.end();

		getVariantBuilder(pipe).forAllStates(state -> switch(state.getValue(RotatedPillarBlock.AXIS)) {
			case X -> ConfiguredModel.builder().modelFile(model).rotationX(90).rotationY(90).build();
			case Z -> ConfiguredModel.builder().modelFile(model).rotationX(90).build();
			default -> ConfiguredModel.builder().modelFile(model).build();
		});
		simpleBlockItem(pipe, model);
	}

	/** Grates: a 2 pixel plate at the level's height with the grate texture on top and bottom, the side strip around */
	private void grate(DeferredBlock<com.hbm.blocks.generic.BlockGrate> block, String top) {
		String name = block.getId().getPath();
		ResourceLocation topTex = texture(top), sideTex = texture("blocks/grate_side");
		ModelFile[] models = new ModelFile[10];
		for(int level = 0; level < 10; level++) {
			float y = (float) com.hbm.blocks.generic.BlockGrate.getY(level) * 16;
			// the side strip of the plate's height, the levels outside the block use the nearest one
			float v = Math.clamp(16 - y - 2, 0, 14);
			BlockModelBuilder model = models().getBuilder(name + "_" + level).parent(models().getExistingFile(mcLoc("block/block")))
					.texture("top", topTex).texture("side", sideTex).texture("particle", topTex).renderType("cutout");
			model.element().from(0, y, 0).to(16, y + 2, 16)
					.face(Direction.UP).texture("#top").uvs(0, 0, 16, 16).end()
					.face(Direction.DOWN).texture("#top").uvs(0, 0, 16, 16).end()
					.face(Direction.NORTH).texture("#side").uvs(0, v, 16, v + 2).end()
					.face(Direction.SOUTH).texture("#side").uvs(0, v, 16, v + 2).end()
					.face(Direction.EAST).texture("#side").uvs(0, v, 16, v + 2).end()
					.face(Direction.WEST).texture("#side").uvs(0, v, 16, v + 2).end()
					.end();
			models[level] = model;
		}
		getVariantBuilder(block.get()).forAllStates(state -> ConfiguredModel.builder().modelFile(models[state.getValue(com.hbm.blocks.generic.BlockGrate.LEVEL)]).build());
		simpleBlockItem(block.get(), models[0]);
	}

	/**
	 * Chain link fences (the original's RenderFence): flat fence panels through the middle towards every connection
	 * and the post, one model per connection combination.
	 */
	private void metalFence(DeferredBlock<com.hbm.blocks.generic.BlockMetalFence> block) {
		var fence = block.get();
		String name = block.getId().getPath();
		ResourceLocation panel = texture("blocks/fence_metal"), post = texture("blocks/fence_metal_post");
		Map<Integer, ModelFile> models = new HashMap<>();
		for(int mask = 0; mask < 16; mask++) {
			boolean n = (mask & 1) != 0, e = (mask & 2) != 0, s = (mask & 4) != 0, w = (mask & 8) != 0;
			BlockModelBuilder model = models().getBuilder(name + "_" + mask).parent(models().getExistingFile(mcLoc("block/block")))
					.texture("panel", panel).texture("post", post).texture("particle", panel).renderType("cutout");
			boolean hasX = e || w, hasZ = n || s;
			if(!hasX && !hasZ) hasX = true;
			float min = 7, max = 9;
			if(hasX) model.element().from(w ? 0 : min, 0, 8).to(e ? 16 : max, 16, 8)
					.face(Direction.NORTH).texture("#panel").end().face(Direction.SOUTH).texture("#panel").end().end();
			if(hasZ) model.element().from(8, 0, n ? 0 : min).to(8, 16, s ? 16 : max)
					.face(Direction.EAST).texture("#panel").end().face(Direction.WEST).texture("#panel").end().end();
			if(com.hbm.blocks.generic.BlockMetalFence.showPost(fence.alwaysPost, n, e, s, w)) {
				model.element().from(6, 0, 6).to(10, 16, 10).allFaces((face, builder) -> builder.texture("#post")).end();
			}
			models.put(mask, model);
		}
		getVariantBuilder(fence).forAllStatesExcept(state -> {
			int mask = (state.getValue(com.hbm.blocks.generic.BlockMetalFence.NORTH) ? 1 : 0) | (state.getValue(com.hbm.blocks.generic.BlockMetalFence.EAST) ? 2 : 0)
					| (state.getValue(com.hbm.blocks.generic.BlockMetalFence.SOUTH) ? 4 : 0) | (state.getValue(com.hbm.blocks.generic.BlockMetalFence.WEST) ? 8 : 0);
			return ConfiguredModel.builder().modelFile(models.get(mask)).build();
		}, com.hbm.blocks.generic.BlockMetalFence.WATERLOGGED);
		simpleBlockItem(fence, models.get(2 | 8));
	}

	/**
	 * Molds and basins: an open box with 2 pixel walls and floor (the original's inner faces at 0.125/0.875),
	 * [name]_top on the rim, _side outside, _inner inside and _bottom on the floor.
	 */
	private void foundryVessel(DeferredBlock<? extends Block> block, String name, float height) {
		String path = "blocks/foundry_" + name;
		BlockModelBuilder model = models().getBuilder(block.getId().getPath()).parent(models().getExistingFile(mcLoc("block/block")))
				.texture("top", texture(path + "_top")).texture("side", texture(path + "_side")).texture("inner", texture(path + "_inner"))
				.texture("bottom", texture(path + "_bottom")).texture("particle", texture(path + "_side")).renderType("cutout");
		model.element().from(0, 0, 0).to(16, 2, 16).face(Direction.UP).texture("#bottom").end().face(Direction.DOWN).texture("#bottom").cullface(Direction.DOWN).end()
				.face(Direction.NORTH).texture("#side").end().face(Direction.SOUTH).texture("#side").end().face(Direction.WEST).texture("#side").end().face(Direction.EAST).texture("#side").end().end();
		// walls: west, east, north, south with the outer face towards their side
		float[][] walls = { {0, 0, 2, 16}, {14, 0, 16, 16}, {2, 0, 14, 2}, {2, 14, 14, 16} };
		Direction[] outer = { Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH };
		for(int i = 0; i < 4; i++) {
			float[] w = walls[i];
			var element = model.element().from(w[0], 2, w[1]).to(w[2], height, w[3]);
			element.face(Direction.UP).texture("#top").end();
			for(Direction dir : Direction.Plane.HORIZONTAL) element.face(dir).texture(dir == outer[i] ? "#side" : "#inner").end();
			element.end();
		}
		simpleBlock(block.get(), model);
		simpleBlockItem(block.get(), model);
	}

	/** Foundry channel: the center with its corner posts, an arm towards every connection and an end wall towards the others */
	private void foundryChannel() {
		ResourceLocation top = texture("blocks/foundry_channel_top"), side = texture("blocks/foundry_channel_side"), inner = texture("blocks/foundry_channel_inner"), bottom = texture("blocks/foundry_channel_bottom");
		java.util.function.Function<String, BlockModelBuilder> base = name -> models().getBuilder(name).parent(models().getExistingFile(mcLoc("block/block")))
				.texture("top", top).texture("side", side).texture("inner", inner).texture("bottom", bottom).texture("particle", side).renderType("cutout");

		BlockModelBuilder center = base.apply("foundry_channel_center");
		center.element().from(5, 0, 5).to(11, 2, 11).allFaces((dir, face) -> face.texture(dir == Direction.UP || dir == Direction.DOWN ? "#bottom" : "#side")).end();
		for(float[] post : new float[][] { {5, 5}, {10, 5}, {5, 10}, {10, 10} }) {
			center.element().from(post[0], 2, post[1]).to(post[0] + 1, 8, post[1] + 1).allFaces((dir, face) -> face.texture(dir == Direction.UP ? "#top" : "#side")).end();
		}

		// east versions, turned for the other directions
		BlockModelBuilder arm = base.apply("foundry_channel_arm");
		arm.element().from(11, 0, 5).to(16, 2, 11).allFaces((dir, face) -> face.texture(dir == Direction.UP || dir == Direction.DOWN ? "#bottom" : "#side")).end();
		arm.element().from(11, 2, 5).to(16, 8, 6).allFaces((dir, face) -> face.texture(dir == Direction.UP ? "#top" : dir == Direction.SOUTH ? "#inner" : "#side")).end();
		arm.element().from(11, 2, 10).to(16, 8, 11).allFaces((dir, face) -> face.texture(dir == Direction.UP ? "#top" : dir == Direction.NORTH ? "#inner" : "#side")).end();

		BlockModelBuilder end = base.apply("foundry_channel_end");
		end.element().from(10, 2, 6).to(11, 8, 10).allFaces((dir, face) -> face.texture(dir == Direction.UP ? "#top" : dir == Direction.WEST ? "#inner" : "#side")).end();

		var builder = getMultipartBuilder(ModBlocks.foundry_channel.get());
		builder.part().modelFile(center).addModel().end();
		Direction[] dirs = { Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH };
		for(int i = 0; i < 4; i++) {
			var prop = com.hbm.blocks.machine.FoundryChannel.property(dirs[i]);
			builder.part().modelFile(arm).rotationY(i * 90).addModel().condition(prop, true).end();
			builder.part().modelFile(end).rotationY(i * 90).addModel().condition(prop, false).end();
		}

		// inventory: straight along x
		BlockModelBuilder item = base.apply("foundry_channel_inventory");
		item.element().from(0, 0, 5).to(16, 2, 11).allFaces((dir, face) -> face.texture(dir == Direction.UP || dir == Direction.DOWN ? "#bottom" : "#side")).end();
		item.element().from(0, 2, 5).to(16, 8, 6).allFaces((dir, face) -> face.texture(dir == Direction.UP ? "#top" : dir == Direction.SOUTH ? "#inner" : "#side")).end();
		item.element().from(0, 2, 10).to(16, 8, 11).allFaces((dir, face) -> face.texture(dir == Direction.UP ? "#top" : dir == Direction.NORTH ? "#inner" : "#side")).end();
		simpleBlockItem(ModBlocks.foundry_channel.get(), item);
	}

	/**
	 * Foundry outlet, modeled facing north (sitting in the south part of its block, towards the channel): floor, side
	 * walls and the open front texture on both ends, the filter and the lock as planes near the channel.
	 */
	private void foundryOutlet(DeferredBlock<? extends com.hbm.blocks.machine.FoundryOutlet> block, String name) {
		String p = "blocks/foundry_outlet_";
		String t = "blocks/" + name + "_";
		BlockModelBuilder model = models().getBuilder(name).parent(models().getExistingFile(mcLoc("block/block")))
				.texture("top", texture(t + "top")).texture("side", texture(t + "side")).texture("inner", texture(t + "inner")).texture("bottom", texture(t + "bottom"))
				.texture("front", texture(t + "front")).texture("particle", texture(t + "side")).renderType("cutout");
		model.element().from(5, 0, 10).to(11, 2, 16).allFaces((dir, face) -> face.texture(dir == Direction.UP || dir == Direction.DOWN ? "#bottom" : "#side")).end();
		model.element().from(5, 2, 10).to(6, 8, 16).allFaces((dir, face) -> face.texture(dir == Direction.UP ? "#top" : dir == Direction.EAST ? "#inner" : "#side")).end();
		model.element().from(10, 2, 10).to(11, 8, 16).allFaces((dir, face) -> face.texture(dir == Direction.UP ? "#top" : dir == Direction.WEST ? "#inner" : "#side")).end();
		model.element().from(5, 0, 10).to(11, 8, 16).face(Direction.NORTH).texture("#front").end().face(Direction.SOUTH).texture("#front").end().end();

		BlockModelBuilder filter = models().getBuilder("foundry_outlet_filter").parent(models().getExistingFile(mcLoc("block/block")))
				.texture("filter", texture(p + "filter")).texture("particle", texture(p + "filter")).renderType("cutout");
		filter.element().from(6, 1, 15.5F).to(10, 8, 15.5F).face(Direction.NORTH).texture("#filter").end().face(Direction.SOUTH).texture("#filter").end().end();
		BlockModelBuilder lock = models().getBuilder("foundry_outlet_lock").parent(models().getExistingFile(mcLoc("block/block")))
				.texture("lock", texture(p + "lock")).texture("particle", texture(p + "lock")).renderType("cutout");
		lock.element().from(6, 1, 15).to(10, 8, 15).face(Direction.NORTH).texture("#lock").end().face(Direction.SOUTH).texture("#lock").end().end();

		var builder = getMultipartBuilder(block.get());
		for(Direction dir : Direction.Plane.HORIZONTAL) {
			int y = ((int) dir.toYRot() + 180) % 360;
			builder.part().modelFile(model).rotationY(y).addModel().condition(com.hbm.blocks.machine.FoundryOutlet.FACING, dir).end();
			builder.part().modelFile(filter).rotationY(y).addModel().condition(com.hbm.blocks.machine.FoundryOutlet.FACING, dir).condition(com.hbm.blocks.machine.FoundryOutlet.FILTER, true).end();
			builder.part().modelFile(lock).rotationY(y).addModel().condition(com.hbm.blocks.machine.FoundryOutlet.FACING, dir).condition(com.hbm.blocks.machine.FoundryOutlet.LOCK, true).end();
		}
		simpleBlockItem(block.get(), model);
	}

	/**
	 * Foundry tank (the original's RenderFoundryTank ISBRH as a multipart): a floor unless there's a tank below and a
	 * wall towards every side without a tank. Walls are modeled on the east side and turned; their outer face shows
	 * the outlet hole and the upper texture when stacked, the inner face turns into the floor texture under a tank
	 * above, and wall ends facing a connected neighbor get their own faces.
	 */
	private void foundryTank() {
		String p = "blocks/foundry_tank_";
		java.util.function.Function<String, BlockModelBuilder> base = name -> models().getBuilder(name).parent(models().getExistingFile(mcLoc("block/block")))
				.texture("top", texture(p + "top")).texture("side", texture(p + "side")).texture("side_outlet", texture(p + "side_outlet"))
				.texture("upper", texture(p + "upper")).texture("upper_outlet", texture(p + "upper_outlet")).texture("bottom", texture(p + "bottom"))
				.texture("inner", texture(p + "inner")).texture("particle", texture(p + "side")).renderType("cutout");

		var builder = getMultipartBuilder(ModBlocks.foundry_tank.get());
		var FT = com.hbm.blocks.machine.FoundryTank.class;

		BlockModelBuilder floor = base.apply("foundry_tank_floor");
		floor.element().from(0, 0, 0).to(16, 2, 16).face(Direction.UP).texture("#bottom").end().face(Direction.DOWN).texture("#bottom").cullface(Direction.DOWN).end().end();
		builder.part().modelFile(floor).addModel().condition(com.hbm.blocks.machine.FoundryTank.DOWN, false).end();

		Direction[] dirs = { Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH };
		for(int down = 0; down < 2; down++) for(int out = 0; out < 2; out++) for(int up = 0; up < 2; up++) {
			String outer = "#" + (down == 1 ? "upper" : "side") + (out == 1 ? "_outlet" : "");
			String inner = up == 1 ? "#bottom" : "#inner";
			BlockModelBuilder wall = base.apply("foundry_tank_wall_" + down + out + up);
			wall.element().from(14, 0, 0).to(16, 16, 16).face(Direction.EAST).texture(outer).cullface(Direction.EAST).end()
					.face(Direction.WEST).texture(inner).end().face(Direction.UP).texture("#top").end().end();
			for(int i = 0; i < 4; i++) {
				builder.part().modelFile(wall).rotationY(i * 90).addModel()
						.condition(com.hbm.blocks.machine.FoundryTank.connection(dirs[i]), false)
						.condition(com.hbm.blocks.machine.FoundryTank.DOWN, down == 1)
						.condition(com.hbm.blocks.machine.FoundryTank.outlet(dirs[i]), out == 1)
						.condition(com.hbm.blocks.machine.FoundryTank.UP, up == 1).end();
			}
		}

		for(int up = 0; up < 2; up++) {
			String inner = up == 1 ? "#bottom" : "#inner";
			BlockModelBuilder endCw = base.apply("foundry_tank_end_cw_" + up);
			endCw.element().from(14, 0, 0).to(16, 16, 16).face(Direction.SOUTH).texture(inner).end().end();
			BlockModelBuilder endCcw = base.apply("foundry_tank_end_ccw_" + up);
			endCcw.element().from(14, 0, 0).to(16, 16, 16).face(Direction.NORTH).texture(inner).end().end();
			for(int i = 0; i < 4; i++) {
				builder.part().modelFile(endCw).rotationY(i * 90).addModel()
						.condition(com.hbm.blocks.machine.FoundryTank.connection(dirs[i]), false)
						.condition(com.hbm.blocks.machine.FoundryTank.connection(dirs[i].getClockWise()), true)
						.condition(com.hbm.blocks.machine.FoundryTank.UP, up == 1).end();
				builder.part().modelFile(endCcw).rotationY(i * 90).addModel()
						.condition(com.hbm.blocks.machine.FoundryTank.connection(dirs[i]), false)
						.condition(com.hbm.blocks.machine.FoundryTank.connection(dirs[i].getCounterClockWise()), true)
						.condition(com.hbm.blocks.machine.FoundryTank.UP, up == 1).end();
			}
		}

		// inventory: a lone tank
		BlockModelBuilder item = base.apply("foundry_tank_inventory");
		item.element().from(0, 0, 0).to(16, 2, 16).face(Direction.UP).texture("#bottom").end().face(Direction.DOWN).texture("#bottom").end().end();
		item.element().from(14, 0, 0).to(16, 16, 16).face(Direction.EAST).texture("#side").end().face(Direction.WEST).texture("#inner").end().face(Direction.UP).texture("#top").end().end();
		item.element().from(0, 0, 0).to(2, 16, 16).face(Direction.WEST).texture("#side").end().face(Direction.EAST).texture("#inner").end().face(Direction.UP).texture("#top").end().end();
		item.element().from(0, 0, 14).to(16, 16, 16).face(Direction.SOUTH).texture("#side").end().face(Direction.NORTH).texture("#inner").end().face(Direction.UP).texture("#top").end().end();
		item.element().from(0, 0, 0).to(16, 16, 2).face(Direction.NORTH).texture("#side").end().face(Direction.SOUTH).texture("#inner").end().face(Direction.UP).texture("#top").end().end();
		simpleBlockItem(ModBlocks.foundry_tank.get(), item);
	}

	/** Dynamic slag is drawn by RenderSlag, the model only provides the break particles */
	private void dynamicSlag() {
		BlockModelBuilder model = models().getBuilder("slag").texture("particle", texture("blocks/slag"));
		simpleBlock(ModBlocks.slag.get(), model);
		simpleBlockItem(ModBlocks.slag.get(), models().cubeAll("slag_item", texture("blocks/slag")));
	}

	/** Barrels: the original's barrel.obj "Barrel" part with the barrel's texture, the model is centered on x/z */
	private void barrel(DeferredBlock<? extends Block> block) {
		String name = block.getId().getPath();
		ResourceLocation texture = texture("blocks/" + name);
		BlockModelBuilder model = models().getBuilder(name).parent(models().getExistingFile(mcLoc("block/block")))
				.texture("texture0", texture).texture("particle", texture).renderType("cutout")
				.customLoader(ObjModelBuilder::begin).modelLocation(modLoc("models/blocks/barrel.obj")).flipV(true).automaticCulling(false)
				.visibility("Barrel", true).visibility("Connector", false).end();
		model.rootTransforms().translation(0.5F, 0, 0.5F);
		simpleBlock(block.get(), model);
		simpleBlockItem(block.get(), model);
	}

	private static final List<String> PIPE_PARTS = List.of("pX", "nX", "pY", "nY", "pZ", "nZ", "ppn", "ppp", "npn", "npp", "pnn", "pnp", "nnn", "nnp");

	/**
	 * Pipes: the original RenderTestPipe drew every part twice, the pipe texture and the overlay tinted with the
	 * fluid color. Here each model is a composite of both, the overlay child uses hbm_tinted.mtl (tint index 0,
	 * the block color handler returns the fluid color). One model per style and visible part set.
	 */
	private void pipe(DeferredBlock<? extends Block> block) {
		String name = block.getId().getPath();
		Map<String, ModelFile> cache = new HashMap<>();

		getVariantBuilder(block.get()).forAllStates(state -> {
			int style = state.getValue(FluidDuctStandard.STYLE);
			int mask = FluidDuctStandard.connectionMask(state);
			List<String> parts = pipeParts(mask);
			String key = style + "_" + String.join("_", parts).toLowerCase();
			ModelFile model = cache.computeIfAbsent(key, k -> pipeModel(name + "_" + k, FluidDuctStandard.STYLE_TEXTURES[style], parts, false));
			return ConfiguredModel.builder().modelFile(model).build();
		});

		// inventory: the four horizontal arms, one model per style picked by the "style" item property
		var item = itemModels().getBuilder(name).parent(pipeModel(name + "_inventory_0", FluidDuctStandard.STYLE_TEXTURES[0], List.of("pX", "nX", "pZ", "nZ"), true));
		for(int style = 1; style < FluidDuctStandard.STYLE_TEXTURES.length; style++) {
			item.override().predicate(modLoc("style"), style)
					.model(pipeModel(name + "_inventory_" + style, FluidDuctStandard.STYLE_TEXTURES[style], List.of("pX", "nX", "pZ", "nZ"), true)).end();
		}
	}

	/** The original's part selection, including its pZ/nZ swap in the junction case */
	private static List<String> pipeParts(int mask) {
		boolean pX = (mask & (1 << Direction.EAST.ordinal())) != 0;
		boolean nX = (mask & (1 << Direction.WEST.ordinal())) != 0;
		boolean pY = (mask & (1 << Direction.UP.ordinal())) != 0;
		boolean nY = (mask & (1 << Direction.DOWN.ordinal())) != 0;
		boolean pZ = (mask & (1 << Direction.SOUTH.ordinal())) != 0;
		boolean nZ = (mask & (1 << Direction.NORTH.ordinal())) != 0;

		if(mask == 0) return List.of("pX", "nX", "pY", "nY", "pZ", "nZ");
		if(!pY && !nY && !pZ && !nZ) return List.of("pX", "nX");
		if(!pX && !nX && !pZ && !nZ) return List.of("pY", "nY");
		if(!pX && !nX && !pY && !nY) return List.of("pZ", "nZ");

		List<String> parts = new java.util.ArrayList<>();
		if(pX) parts.add("pX");
		if(nX) parts.add("nX");
		if(pY) parts.add("pY");
		if(nY) parts.add("nY");
		if(pZ) parts.add("nZ");
		if(nZ) parts.add("pZ");
		if(!pX && !pY && !pZ) parts.add("ppn");
		if(!pX && !pY && !nZ) parts.add("ppp");
		if(!nX && !pY && !pZ) parts.add("npn");
		if(!nX && !pY && !nZ) parts.add("npp");
		if(!pX && !nY && !pZ) parts.add("pnn");
		if(!pX && !nY && !nZ) parts.add("pnp");
		if(!nX && !nY && !pZ) parts.add("nnn");
		if(!nX && !nY && !nZ) parts.add("nnp");
		return parts;
	}

	private BlockModelBuilder pipeModel(String name, String tex, List<String> visible, boolean inventory) {
		ResourceLocation base = texture("blocks/" + tex);
		ResourceLocation overlay = texture("blocks/" + tex + "_overlay");
		BlockModelBuilder model = models().getBuilder(name).parent(models().getExistingFile(mcLoc("block/block")))
				.texture("particle", base).renderType("cutout");
		CompositeModelBuilder<BlockModelBuilder> composite = model.customLoader(CompositeModelBuilder::begin);
		for(int layer = 0; layer < 2; layer++) {
			// composite children bake with their own render type, the root one is not inherited
			BlockModelBuilder child = models().nested().texture("texture0", layer == 0 ? base : overlay).renderType("cutout");
			ObjModelBuilder<BlockModelBuilder> obj = child.customLoader(ObjModelBuilder::begin).modelLocation(modLoc("models/blocks/pipe_neo.obj")).flipV(true).automaticCulling(false);
			if(layer == 1) obj.overrideMaterialLibrary(modLoc("models/hbm_tinted.mtl"));
			for(String part : PIPE_PARTS) obj.visibility(part, visible.contains(part));
			obj.end();
			child.rootTransforms().translation(0.5F, 0.5F, 0.5F);
			composite.child(layer == 0 ? "pipe" : "overlay", child);
		}
		composite.end();
		return model;
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
