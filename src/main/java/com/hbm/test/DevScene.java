package com.hbm.test;

import java.io.File;

import com.hbm.blocks.ModBlocks;
import com.hbm.creativetabs.NtmTab;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Development helper for checking visuals without manual setup, only active with -Dhbm.devScene=true
 * (the runDevScene gradle task). Builds a test scene in front of the player on login, takes screenshots
 * after the chunks have rendered (run/screenshots/devscene_*.png) and closes the game.
 */
@EventBusSubscriber(modid = RefStrings.MODID)
public class DevScene {

	public static final boolean ENABLED = Boolean.getBoolean("hbm.devScene");
	static BlockPos furnacePos;
	static BlockPos burnerPos;
	static BlockPos barrelPos;
	static BlockPos assemblerPos;
	static BlockPos chemplantPos;
	static BlockPos welderPos;
	static BlockPos blastFurnacePos;
	static BlockPos shredderPos;
	private static int serverTicks = 0;

	/** Opens the furnace GUI for the screenshot, has to happen on the server */
	@SubscribeEvent
	public static void onServerTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
		if(!ENABLED || furnacePos == null) return;
		serverTicks++;
		// second world screenshot: close up of the wood burner, holding its item
		if(serverTicks == 160 && burnerPos != null) {
			for(ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
				player.teleportTo(player.serverLevel(), 4.5, -59, -10.8, -20F, 25F);
				player.getInventory().selected = 0;
				player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket(0));
			}
		}
		// third world screenshot: the running assembly machine
		if(serverTicks == 186 && assemblerPos != null) {
			for(ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
				player.teleportTo(player.serverLevel(), 0.5, -59, -11.8, 0F, 18F);
			}
		}
		// the electric furnace is a plain block, its GUI closes beyond 8 blocks: stand next to it for its screenshot, then go back
		if(serverTicks == 212 || serverTicks == 250) {
			for(ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
				if(serverTicks == 212) player.teleportTo(player.serverLevel(), -6.5, -59, 4.2, 0F, 18F);
				else player.teleportTo(player.serverLevel(), 0.5, -59, -11.8, 0F, 18F);
			}
		}
		// steel anvil GUI (no block entity, the tier comes with the menu)
		if(serverTicks == 310) {
			for(ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
				player.closeContainer();
				player.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inv, p) -> new com.hbm.inventory.container.ContainerAnvil(id, inv, 2),
						net.minecraft.network.chat.Component.translatable("container.anvil", 2)), buf -> buf.writeInt(2));
			}
		}
		BlockPos open = serverTicks == 215 ? furnacePos : serverTicks == 255 ? burnerPos : serverTicks == 285 ? barrelPos : serverTicks == 345 ? assemblerPos : serverTicks == 395 ? chemplantPos : serverTicks == 435 ? welderPos : serverTicks == 475 ? blastFurnacePos : serverTicks == 515 ? shredderPos : null;
		if(open != null) {
			for(ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
				player.closeContainer();
				if(player.level().getBlockEntity(open) instanceof net.minecraft.world.MenuProvider provider) {
					player.openMenu(provider, buf -> buf.writeBlockPos(open));
				}
			}
		}
	}

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if(!ENABLED || !(event.getEntity() instanceof ServerPlayer player)) return;

		ServerLevel level = player.serverLevel();
		BlockPos origin = new BlockPos(0, -60, 0);

		// clear and floor
		for(int x = -14; x <= 16; x++) for(int z = -12; z <= 12; z++) for(int y = 0; y <= 10; y++) {
			level.setBlockAndUpdate(origin.offset(x, y, z), y == 0 ? Blocks.SMOOTH_STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
		}
		// the world is kept between runs, clearing the last run's machines drops their contents
		level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(origin).inflate(20)).forEach(net.minecraft.world.entity.Entity::discard);

		// cables: straight line, corner, T, cross, vertical
		for(int x = -5; x <= -1; x++) level.setBlockAndUpdate(origin.offset(x, 1, 6), ModBlocks.red_cable.get().defaultBlockState());
		level.setBlockAndUpdate(origin.offset(-5, 1, 7), ModBlocks.red_cable.get().defaultBlockState());
		level.setBlockAndUpdate(origin.offset(-3, 1, 7), ModBlocks.red_cable.get().defaultBlockState());
		for(int y = 1; y <= 4; y++) level.setBlockAndUpdate(origin.offset(1, y, 6), ModBlocks.red_cable.get().defaultBlockState());
		level.setBlockAndUpdate(origin.offset(3, 1, 6), ModBlocks.red_cable.get().defaultBlockState());

		// wall of all generated full blocks behind the cables, 24 wide
		int i = 0;
		for(var block : ModBlocks.MODELS.keySet()) {
			if(block.get() instanceof net.minecraft.world.level.block.FallingBlock) continue;
			level.setBlockAndUpdate(origin.offset(12 - i % 24, 1 + i / 24, 11), block.get().defaultBlockState());
			i++;
		}

		// machines: furnace with a creative battery and iron, capacitors of every tier
		BlockPos furnacePos = origin.offset(-7, 1, 6);
		level.setBlockAndUpdate(furnacePos, ModBlocks.machine_electric_furnace_off.get().defaultBlockState()
				.setValue(com.hbm.blocks.machine.MachineElectricFurnace.FACING, net.minecraft.core.Direction.NORTH));
		if(level.getBlockEntity(furnacePos) instanceof com.hbm.tileentity.machine.TileEntityMachineElectricFurnace furnace) {
			furnace.setItem(0, new ItemStack(ModItems.battery_creative.get()));
			furnace.setItem(1, new ItemStack(net.minecraft.world.item.Items.RAW_IRON, 64));
		}
		DevScene.furnacePos = furnacePos;
		int c = 0;
		for(var cap : java.util.List.of(ModBlocks.capacitor_copper, ModBlocks.capacitor_gold, ModBlocks.capacitor_niobium, ModBlocks.capacitor_tantalium, ModBlocks.capacitor_schrabidate)) {
			level.setBlockAndUpdate(origin.offset(-9 + c * 2, 1, 3), cap.get().defaultBlockState());
			c++;
		}
		level.setBlockAndUpdate(origin.offset(-9, 2, 3), ModBlocks.capacitor_bus.get().defaultBlockState());

		// wood burner multiblock facing the player, running on logs
		burnerPos = ModBlocks.machine_wood_burner.get().placeMultiblock(level, origin.offset(-3, 1, 1), net.minecraft.core.Direction.NORTH);
		if(burnerPos != null && level.getBlockEntity(burnerPos) instanceof com.hbm.tileentity.machine.TileEntityMachineWoodBurner burner) {
			burner.isOn = true;
			burner.setItem(0, new ItemStack(net.minecraft.world.item.Items.OAK_LOG, 64));
		}

		// pipes in front of the burner: a diesel line with a riser and a T, a silver kerosene pipe, colored untyped pipes
		{
			var duct = ModBlocks.fluid_duct_neo.get();
			var S = com.hbm.blocks.network.FluidDuctStandard.STYLE;
			java.util.List<BlockPos> pipes = new java.util.ArrayList<>();
			for(int x = -7; x <= -4; x++) pipes.add(origin.offset(x, 1, -1));
			pipes.add(origin.offset(-6, 2, -1));
			pipes.add(origin.offset(-6, 3, -1));
			pipes.add(origin.offset(-5, 1, 0));
			for(BlockPos p : pipes) level.setBlockAndUpdate(p, duct.defaultBlockState());
			for(BlockPos p : pipes) com.hbm.blocks.network.FluidDuctStandard.setType(level, p, com.hbm.inventory.fluid.Fluids.DIESEL);

			level.setBlockAndUpdate(origin.offset(-2, 1, -1), duct.defaultBlockState().setValue(S, 1));
			com.hbm.blocks.network.FluidDuctStandard.setType(level, origin.offset(-2, 1, -1), com.hbm.inventory.fluid.Fluids.KEROSENE);
			for(int x = 0; x <= 1; x++) level.setBlockAndUpdate(origin.offset(x, 1, -1), duct.defaultBlockState().setValue(S, 2));
			level.setBlockAndUpdate(origin.offset(2, 1, -1), duct.defaultBlockState().setValue(S, 2));
			com.hbm.blocks.network.FluidDuctStandard.setType(level, origin.offset(2, 1, -1), com.hbm.inventory.fluid.Fluids.WATER);
			for(int x = 0; x <= 2; x++) com.hbm.blocks.network.FluidDuctStandard.refreshConnections(level, origin.offset(x, 1, -1));
		}

		// barrels of every kind next to the pipes, the steel one with diesel feeding a pipe
		{
			int bx = 4;
			for(var barrel : ModBlocks.BARRELS) {
				level.setBlockAndUpdate(origin.offset(bx, 1, -1), barrel.get().defaultBlockState());
				bx++;
			}
			barrelPos = origin.offset(6, 1, -1);
			if(level.getBlockEntity(barrelPos) instanceof com.hbm.tileentity.machine.storage.TileEntityBarrel barrel) {
				barrel.tank.setTankType(com.hbm.inventory.fluid.Fluids.DIESEL);
				barrel.tank.setFill(10000);
				barrel.setItem(2, com.hbm.items.machine.ItemFluidContainerBase.withFluid(com.hbm.items.ModItems.canister_full, com.hbm.inventory.fluid.Fluids.DIESEL).copyWithCount(3));
			}
			// running diesel generator in front of the steel barrel, which outputs into it
			if(level.getBlockEntity(barrelPos) instanceof com.hbm.tileentity.machine.storage.TileEntityBarrel barrel) barrel.mode = 2;
			level.setBlockAndUpdate(barrelPos.north(), ModBlocks.machine_diesel.get().defaultBlockState().setValue(com.hbm.blocks.machine.MachineDiesel.FACING, net.minecraft.core.Direction.WEST));
			if(level.getBlockEntity(barrelPos.north()) instanceof com.hbm.tileentity.machine.TileEntityMachineDiesel diesel) diesel.isOn = true;
		}

		// firebox with a boiler on top, heating oil; the refinery next to it
		{
			var N = net.minecraft.core.Direction.NORTH;
			BlockPos fb = ModBlocks.heater_firebox.get().placeMultiblock(level, origin.offset(8, 1, -5), N);
			BlockPos bo = ModBlocks.machine_boiler.get().placeMultiblock(level, origin.offset(8, 2, -5), N);
			if(fb != null && level.getBlockEntity(fb) instanceof com.hbm.tileentity.machine.TileEntityHeaterFirebox heater) heater.setItem(0, new ItemStack(net.minecraft.world.item.Items.COAL, 64));
			if(bo != null && level.getBlockEntity(bo) instanceof com.hbm.tileentity.machine.TileEntityHeatBoiler boiler) { boiler.tanks[0].setTankType(com.hbm.inventory.fluid.Fluids.OIL); boiler.tanks[0].setFill(8000); }
			ModBlocks.machine_refinery.get().placeMultiblock(level, origin.offset(12, 1, -5), N);
		}

		// burner press pressing iron plates
		{
			BlockPos pr = ModBlocks.machine_press.get().placeMultiblock(level, origin.offset(5, 1, -7), net.minecraft.core.Direction.NORTH);
			if(pr != null && level.getBlockEntity(pr) instanceof com.hbm.tileentity.machine.TileEntityMachinePress press) {
				press.setItem(0, new ItemStack(net.minecraft.world.item.Items.COAL, 64));
				press.setItem(1, new ItemStack(ModItems.stamp_iron_plate.get()));
				press.setItem(2, new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 64));
			}
		}

		// anvils next to the press, facing both ways
		{
			int ax = 8;
			for(var anvil : java.util.List.of(ModBlocks.anvil_iron, ModBlocks.anvil_steel, ModBlocks.anvil_desh, ModBlocks.anvil_murky)) {
				var facing = ax % 2 == 0 ? net.minecraft.core.Direction.NORTH : net.minecraft.core.Direction.EAST;
				level.setBlockAndUpdate(origin.offset(ax, 1, -8), anvil.get().defaultBlockState().setValue(com.hbm.blocks.machine.NTMAnvil.FACING, facing));
				ax++;
			}
		}

		// assembly machine next to the press, making hazmat cloth
		{
			assemblerPos = ModBlocks.machine_assembly_machine.get().placeMultiblock(level, origin.offset(2, 1, -8), net.minecraft.core.Direction.NORTH);
			if(assemblerPos != null && level.getBlockEntity(assemblerPos) instanceof com.hbm.tileentity.machine.TileEntityMachineAssemblyMachine assembler) {
				assembler.setItem(0, new ItemStack(ModItems.battery_creative.get()));
				assembler.setItem(1, com.hbm.items.machine.ItemBlueprints.make(com.hbm.inventory.recipes.loader.GenericRecipes.POOL_PREFIX_ALT + "plates"));
				assembler.assemblerModule.setRecipe("ass.hazcloth", false);
				assembler.setItem(4, new ItemStack(ModItems.powder_lead.get(), 64));
				assembler.setItem(5, new ItemStack(net.minecraft.world.item.Items.STRING, 64));
			}
		}

		// chemical plant next to the assembler, making sulfuric acid
		{
			chemplantPos = ModBlocks.machine_chemical_plant.get().placeMultiblock(level, origin.offset(-1, 1, -8), net.minecraft.core.Direction.NORTH);
			if(chemplantPos != null && level.getBlockEntity(chemplantPos) instanceof com.hbm.tileentity.machine.TileEntityMachineChemicalPlant chemplant) {
				chemplant.setItem(0, new ItemStack(ModItems.battery_creative.get()));
				chemplant.chemplantModule.setRecipe("chem.sulfuricacid", false);
				chemplant.inputTanks[0].setTankType(com.hbm.inventory.fluid.Fluids.PEROXIDE);
				chemplant.inputTanks[0].setFill(24_000);
				chemplant.inputTanks[1].setTankType(com.hbm.inventory.fluid.Fluids.WATER);
				chemplant.inputTanks[1].setFill(24_000);
				chemplant.setItem(4, new ItemStack(ModItems.sulfur.get(), 64));
			}
		}

		// arc welder next to the chemical plant, welding motors
		{
			welderPos = ModBlocks.machine_arc_welder.get().placeMultiblock(level, origin.offset(-4, 1, -7), net.minecraft.core.Direction.NORTH);
			if(welderPos != null && level.getBlockEntity(welderPos) instanceof com.hbm.tileentity.machine.TileEntityMachineArcWelder welder) {
				welder.setItem(4, new ItemStack(ModItems.battery_creative.get()));
				welder.setItem(0, new ItemStack(ModItems.plate_steel.get(), 64));
				welder.setItem(1, ModItems.wire_dense.stack(Mats.MAT_MINGRADE, 64));
			}
		}

		// blast furnace behind the welder, making steel
		{
			blastFurnacePos = ModBlocks.machine_blast_furnace.get().placeMultiblock(level, origin.offset(-6, 1, -4), net.minecraft.core.Direction.NORTH);
			if(blastFurnacePos != null && level.getBlockEntity(blastFurnacePos) instanceof com.hbm.tileentity.machine.TileEntityMachineBlastFurnace furnace) {
				furnace.setItem(0, new ItemStack(net.minecraft.world.item.Items.COAL, 64));
				furnace.setItem(1, new ItemStack(net.minecraft.world.item.Items.IRON_INGOT, 64));
				furnace.setItem(2, new ItemStack(net.minecraft.world.item.Items.SAND, 64));
			}
		}

		// shredder in front of the assembler, doubling iron ore (close to the third camera, plain blocks close their GUI beyond 8 blocks)
		{
			shredderPos = origin.offset(3, 1, -10);
			level.setBlockAndUpdate(shredderPos, ModBlocks.machine_shredder.get().defaultBlockState());
			if(level.getBlockEntity(shredderPos) instanceof com.hbm.tileentity.machine.TileEntityMachineShredder shredder) {
				shredder.setItem(29, new ItemStack(ModItems.battery_creative.get()));
				shredder.setItem(27, new ItemStack(ModItems.blades_steel.get()));
				shredder.setItem(28, new ItemStack(ModItems.blades_titanium.get()));
				shredder.setItem(0, new ItemStack(net.minecraft.world.item.Items.IRON_ORE, 64));
				shredder.setItem(1, new ItemStack(ModBlocks.ore_uranium.get(), 64));
				shredder.setItem(2, new ItemStack(net.minecraft.world.item.Items.COBBLESTONE, 64));
			}
		}

		// steel scaffolds, one per orientation and color
		{
			var scaffolds = java.util.List.of(ModBlocks.steel_scaffold, ModBlocks.steel_scaffold_red, ModBlocks.steel_scaffold_white, ModBlocks.steel_scaffold_yellow);
			for(int s = 0; s < 4; s++) level.setBlockAndUpdate(origin.offset(-10 + s, 1, 1), scaffolds.get(s).get().defaultBlockState().setValue(com.hbm.blocks.generic.BlockScaffold.ORIENTATION, s));
		}

		// oil derrick
		ModBlocks.machine_well.get().placeMultiblock(level, origin.offset(4, 1, 3), net.minecraft.core.Direction.NORTH);

		player.setGameMode(GameType.CREATIVE);
		level.setDayTime(6000);
		player.teleportTo(level, -4.5, origin.getY() + 2, -1.5, 20F, 15F);
		player.getInventory().clearContent();
		player.getInventory().add(new ItemStack(ModBlocks.machine_wood_burner.get()));
		player.getInventory().add(new ItemStack(ModItems.geiger_counter.get()));
		player.getInventory().add(new ItemStack(ModBlocks.red_cable.get()));
		player.getInventory().add(new ItemStack(ModItems.ingot_uranium.get(), 16));
		player.getInventory().add(new ItemStack(ModBlocks.ore_uranium.get()));
		// half the ingredients of the anvil's firebox recipe
		player.getInventory().add(new ItemStack(net.minecraft.world.item.Items.FURNACE));
		player.getInventory().add(new ItemStack(ModItems.plate_steel.get(), 8));
		player.getInventory().add(new ItemStack(ModItems.ingot_copper.get(), 4));
		// material autogen items in the main inventory, visible in the machine GUI screenshots
		for(ItemStack stack : java.util.List.of(
				ModItems.wire_fine.stack(Mats.MAT_COPPER, 8), ModItems.wire_fine.stack(Mats.MAT_STEEL, 8),
				ModItems.wire_dense.stack(Mats.MAT_GOLD), ModItems.wire_dense.stack(Mats.MAT_NEODYMIUM),
				ModItems.bolt.stack(Mats.MAT_STEEL, 16), ModItems.bolt.stack(Mats.MAT_DURA, 16),
				ModItems.plate_cast.stack(Mats.MAT_STEEL), ModItems.plate_cast.stack(Mats.MAT_DESH),
				ModItems.plate_welded.stack(Mats.MAT_TITANIUM), ModItems.shell.stack(Mats.MAT_STEEL),
				ModItems.pipe.stack(Mats.MAT_COPPER), ModItems.pipe.stack(Mats.MAT_RUBBER),
				ModItems.battery_sc.stack(com.hbm.items.machine.ItemBatterySC.EnumBatterySC.PU238), ModItems.part_grip.stack(Mats.MAT_WOOD),
				ModItems.part_barrel_light.stack(Mats.MAT_GUNMETAL), ModItems.part_mechanism.stack(Mats.MAT_WEAPONSTEEL),
				ModItems.bedrock_ore_fragment.stack(Mats.MAT_URANIUM), ModItems.pwr_fuel.stack(com.hbm.items.machine.ItemPWRFuel.EnumPWRFuel.MEU),
				ModItems.battery_pack.stack(com.hbm.items.machine.ItemBatteryPack.EnumBatteryPack.BATTERY_LITHIUM), ModItems.battery_pack.stack(com.hbm.items.machine.ItemBatteryPack.EnumBatteryPack.CAPACITOR_GOLD),
				ModItems.rod_dual.stack(com.hbm.items.machine.ItemBreedingRod.BreedingRodType.U238), ModItems.drive.stack(com.hbm.items.machine.ItemDrive.EnumDriveType.FLASH_EMPTY),
				ModItems.pile_rod.stack(com.hbm.items.machine.ItemPileRodMK2.EnumPileRod.NU),
				ModItems.chemical_dye.stack(com.hbm.items.machine.ItemChemicalDye.EnumChemDye.RED, 4), ModItems.crayon.stack(com.hbm.items.machine.ItemChemicalDye.EnumChemDye.LIME, 2),
				ModItems.ore_byproduct.stack(com.hbm.items.special.ItemByproduct.EnumByproduct.B_COPPER, 9), wornElectrode())) {
			player.getInventory().add(stack);
		}
	}

	/** A desh electrode with a third of its uses left, shows the wear bar */
	private static ItemStack wornElectrode() {
		ItemStack electrode = ModItems.arc_electrode.stack(com.hbm.items.machine.ItemArcElectrode.EnumElectrodeType.DESH);
		for(int i = 0; i < 330; i++) com.hbm.items.machine.ItemArcElectrode.damage(electrode);
		return electrode;
	}

	@EventBusSubscriber(modid = RefStrings.MODID, value = Dist.CLIENT)
	public static class Client {

		private static void openTab(Minecraft mc, NtmTab tab) {
			var screen = new net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen(mc.player, mc.player.connection.enabledFeatures(), false);
			mc.setScreen(screen);
			try {
				var select = screen.getClass().getDeclaredMethod("selectTab", net.minecraft.world.item.CreativeModeTab.class);
				select.setAccessible(true);
				select.invoke(screen, com.hbm.creativetabs.ModCreativeTabs.BY_TAB.get(tab).get());
			} catch(Exception ex) {
				MainRegistry.logger.warn("DevScene: could not select creative tab", ex);
			}
		}

		private static int ticks = 0;

		@SubscribeEvent
		public static void onClientTick(ClientTickEvent.Post event) {
			if(!ENABLED) return;
			Minecraft mc = Minecraft.getInstance();
			if(mc.level == null || mc.player == null) return;

			ticks++;
			// screenshots must not catch the pause menu when the window is in the background
			mc.options.pauseOnLostFocus = false;

			if(ticks == 150 || ticks == 185 || ticks == 205) {
				if(mc.screen != null) mc.setScreen(null);
				Screenshot.grab(mc.gameDirectory, "devscene_world_" + ticks + ".png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			}

			if(ticks == 240) {
				Screenshot.grab(mc.gameDirectory, "devscene_gui_furnace.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			}
			if(ticks == 305) {
				Screenshot.grab(mc.gameDirectory, "devscene_gui_barrel.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			}
			if(ticks == 280) {
				Screenshot.grab(mc.gameDirectory, "devscene_gui_wood_burner.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			}

			// anvil GUI: search for the firebox and select it
			if(ticks == 328 && mc.screen instanceof com.hbm.inventory.gui.GUIAnvil anvil) {
				try {
					var search = com.hbm.inventory.gui.GUIAnvil.class.getDeclaredField("search");
					search.setAccessible(true);
					((net.minecraft.client.gui.components.EditBox) search.get(anvil)).setValue("firebox");
					var selection = com.hbm.inventory.gui.GUIAnvil.class.getDeclaredField("selection");
					selection.setAccessible(true);
					selection.setInt(anvil, 0);
				} catch(Exception ex) {
					MainRegistry.logger.warn("DevScene: could not select the anvil recipe", ex);
				}
			}
			if(ticks == 335) {
				Screenshot.grab(mc.gameDirectory, "devscene_gui_anvil.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			}

			// assembly machine GUI, then its recipe selector (the blueprint unlocks the plates)
			if(ticks == 370) {
				Screenshot.grab(mc.gameDirectory, "devscene_gui_assembler.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			}
			if(ticks == 375 && mc.screen instanceof com.hbm.inventory.gui.GUIMachineAssemblyMachine gui && assemblerPos != null) {
				var module = gui.getMenu().tile.assemblerModule;
				com.hbm.inventory.gui.GUIScreenRecipeSelector.openSelector(module.getRecipeSet(), assemblerPos, module.getRecipeName(), 0,
						com.hbm.items.machine.ItemBlueprints.grabPool(gui.getMenu().tile.getItem(1)), gui);
			}
			if(ticks == 385) {
				Screenshot.grab(mc.gameDirectory, "devscene_gui_recipe_selector.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			}

			if(ticks == 420) {
				Screenshot.grab(mc.gameDirectory, "devscene_gui_chemplant.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			}

			if(ticks == 460) {
				Screenshot.grab(mc.gameDirectory, "devscene_gui_arc_welder.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			}

			if(ticks == 500) {
				Screenshot.grab(mc.gameDirectory, "devscene_gui_blast_furnace.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			}

			if(ticks == 540) {
				Screenshot.grab(mc.gameDirectory, "devscene_gui_shredder.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			}

			// creative tabs, one screenshot each
			NtmTab[] tabs = { NtmTab.MACHINE, NtmTab.CONTROL };
			for(int t = 0; t < tabs.length; t++) {
				if(ticks == 560 + t * 20) openTab(mc, tabs[t]);
				if(ticks == 575 + t * 20) Screenshot.grab(mc.gameDirectory, "devscene_tab_" + tabs[t].name().toLowerCase() + ".png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			}

			if(ticks == 560 + tabs.length * 20) {
				MainRegistry.logger.info("DevScene: done, screenshots in " + new File(mc.gameDirectory, "screenshots").getAbsolutePath());
				mc.stop();
			}
		}
	}
}
