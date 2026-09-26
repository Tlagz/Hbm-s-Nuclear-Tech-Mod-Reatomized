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
	static BlockPos arcFurnacePos;
	static BlockPos cruciblePos;
	static BlockPos steamEnginePos, condenserPos;
	static BlockPos towerSmallPos, towerLargePos, condenserPoweredPos;
	static BlockPos industrialTurbinePos, chungusPos;
	static BlockPos oilburnerPos, heatexPos, boilerOnBurnerPos, industrialBoilerPos, fireboxPos;
	static BlockPos pumpSteamPos, pumpElectricPos;
	static BlockPos combustionPos, centrifugePos, crystallizerPos;
	static BlockPos fluidTankPos, pumpjackPos, frackingPos, flarePos, vacuumPos;
	private static int serverTicks = 0;

	/**
	 * Server side of the timeline (teleports, opening GUIs). The server starts ticking before the client, so the actions
	 * follow the client's tick counter instead of their own, every tick number runs exactly once.
	 */
	@SubscribeEvent
	public static void onServerTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
		if(!ENABLED || furnacePos == null) return;
		while(serverTicks < Client.ticks) {
			serverTicks++;
			act(event.getServer(), serverTicks);
		}
	}

	private static void act(net.minecraft.server.MinecraftServer server, int serverTicks) {
		// second world screenshot: close up of the wood burner, holding its item
		if(serverTicks == 160 && burnerPos != null) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				player.teleportTo(player.serverLevel(), 4.5, -59, -10.8, -20F, 25F);
				player.getInventory().selected = 0;
				player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket(0));
			}
		}
		// third world screenshot: the running assembly machine
		if(serverTicks == 186 && assemblerPos != null) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				player.teleportTo(player.serverLevel(), 0.5, -59, -11.8, 0F, 18F);
			}
		}
		// the electric furnace is a plain block, its GUI closes beyond 8 blocks: stand next to it for its screenshot, then go back
		if(serverTicks == 212 || serverTicks == 250) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				if(serverTicks == 212) player.teleportTo(player.serverLevel(), -6.5, -59, 4.2, 0F, 18F);
				else player.teleportTo(player.serverLevel(), 0.5, -59, -11.8, 0F, 18F);
			}
		}
		// last world screenshot: the deco blocks
		if(serverTicks == 600) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				player.closeContainer();
				player.teleportTo(player.serverLevel(), -10.5, -59, -5.2, 180F, 28F);
			}
		}
		// the arc furnace from the west and its GUI
		// the crucible and its GUI
		if(serverTicks == 750 || serverTicks == 790) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				player.closeContainer();
				if(serverTicks == 750) player.teleportTo(player.serverLevel(), 4.5, -57, 3.5, 22F, 24F);
				else if(cruciblePos != null && player.level().getBlockEntity(cruciblePos) instanceof net.minecraft.world.MenuProvider provider) player.openMenu(provider, buf -> buf.writeBlockPos(cruciblePos));
			}
		}
		// steam engine fed with steam, its spent steam going into a condenser whose water is taken away
		if(steamEnginePos != null && server.overworld().getBlockEntity(steamEnginePos) instanceof com.hbm.tileentity.machine.TileEntitySteamEngine engine
				&& server.overworld().getBlockEntity(condenserPos) instanceof com.hbm.tileentity.machine.TileEntityCondenser condenser) {
			engine.tanks[0].setFill(engine.tanks[0].getMaxFill());
			condenser.tanks[1].setFill(0);
		}
		// the cooling towers and the powered condenser outside the floor, always condensing so they steam
		for(BlockPos tower : new BlockPos[] {towerSmallPos, towerLargePos, condenserPoweredPos}) {
			if(tower != null && server.overworld().getBlockEntity(tower) instanceof com.hbm.tileentity.machine.TileEntityCondenser c) {
				c.tanks[0].setFill(c.tanks[0].getMaxFill() / 2);
				c.tanks[1].setFill(0);
				if(c instanceof com.hbm.tileentity.machine.TileEntityCondenserPowered powered) powered.setPower(powered.getMaxPower());
			}
		}
		// the turbines south of the floor, full of steam
		for(BlockPos turbinePos : new BlockPos[] {industrialTurbinePos, chungusPos}) {
			if(turbinePos != null && server.overworld().getBlockEntity(turbinePos) instanceof com.hbm.tileentity.machine.TileEntityTurbineBase turbine) {
				turbine.tanks[0].setFill(turbine.tanks[0].getMaxFill() / 2);
				turbine.tanks[1].setFill(0);
			}
		}
		// the fluid burner under a boiler, the heat exchanger and the industrial boiler on a firebox
		if(oilburnerPos != null && server.overworld().getBlockEntity(oilburnerPos) instanceof com.hbm.tileentity.machine.TileEntityHeaterOilburner burner) {
			burner.isOn = true;
			burner.setting = 5;
			burner.tank.setFill(burner.tank.getMaxFill());
		}
		if(heatexPos != null && server.overworld().getBlockEntity(heatexPos) instanceof com.hbm.tileentity.machine.TileEntityHeaterHeatex heatex) {
			heatex.tanks[0].setFill(heatex.tanks[0].getMaxFill() / 2);
			heatex.tanks[1].setFill(0);
		}
		if(boilerOnBurnerPos != null && server.overworld().getBlockEntity(boilerOnBurnerPos) instanceof com.hbm.tileentity.machine.TileEntityHeatBoiler boiler) {
			boiler.tanks[0].setFill(boiler.tanks[0].getMaxFill());
			boiler.tanks[1].setFill(0);
		}
		if(industrialBoilerPos != null && server.overworld().getBlockEntity(industrialBoilerPos) instanceof com.hbm.tileentity.machine.TileEntityHeatBoilerIndustrial boiler) {
			boiler.tanks[0].setFill(boiler.tanks[0].getMaxFill());
			boiler.tanks[1].setFill(0);
			if(server.overworld().getBlockEntity(fireboxPos) instanceof com.hbm.tileentity.machine.TileEntityHeaterFirebox firebox) firebox.heatEnergy = 100_000;
		}
		// the water pumps on the natural ground, steam and power fed, water taken away
		if(pumpSteamPos != null && server.overworld().getBlockEntity(pumpSteamPos) instanceof com.hbm.tileentity.machine.TileEntityMachinePumpSteam pump) {
			pump.steam.setFill(pump.steam.getMaxFill());
			pump.lps.setFill(0);
			pump.water.setFill(0);
		}
		if(pumpElectricPos != null && server.overworld().getBlockEntity(pumpElectricPos) instanceof com.hbm.tileentity.machine.TileEntityMachinePumpElectric pump) {
			pump.setPower(pump.getMaxPower());
			pump.water.setFill(0);
		}
		// the combustion engine running on diesel with desh pistons
		if(combustionPos != null && server.overworld().getBlockEntity(combustionPos) instanceof com.hbm.tileentity.machine.TileEntityMachineCombustionEngine engine) {
			engine.tank.setTankType(com.hbm.inventory.fluid.Fluids.DIESEL);
			engine.tank.setFill(engine.tank.getMaxFill());
			if(engine.getItem(2).isEmpty()) engine.setItem(2, ModItems.piston_set.stack(com.hbm.items.machine.ItemPistons.EnumPistonType.DESH));
			engine.isOn = true;
			engine.setting = 20;
			engine.setPower(0);
		}
		// the centrifuge working on iron ore
		if(centrifugePos != null && server.overworld().getBlockEntity(centrifugePos) instanceof com.hbm.tileentity.machine.TileEntityMachineCentrifuge centrifuge) {
			centrifuge.setPower(com.hbm.tileentity.machine.TileEntityMachineCentrifuge.maxPower);
			if(centrifuge.getItem(0).isEmpty()) centrifuge.setItem(0, new ItemStack(net.minecraft.world.item.Items.IRON_ORE, 16));
		}
		// the acidizer working on iron ore with peroxide
		if(crystallizerPos != null && server.overworld().getBlockEntity(crystallizerPos) instanceof com.hbm.tileentity.machine.TileEntityMachineCrystallizer crystallizer) {
			crystallizer.setPower(com.hbm.tileentity.machine.TileEntityMachineCrystallizer.maxPower);
			crystallizer.tank.setFill(crystallizer.tank.getMaxFill());
			if(crystallizer.getItem(0).isEmpty()) crystallizer.setItem(0, new ItemStack(net.minecraft.world.item.Items.IRON_ORE, 16));
		}
		// the pumpjack on a (refilled) oil deposit, both drills powered
		if(pumpjackPos != null && server.overworld().getBlockEntity(pumpjackPos) instanceof com.hbm.tileentity.machine.oil.TileEntityMachinePumpjack jack) {
			jack.power = jack.getMaxPower();
			if(!server.overworld().getBlockState(pumpjackPos.below(2)).is(ModBlocks.ore_oil.get()) && !server.overworld().getBlockState(pumpjackPos.below(2)).is(ModBlocks.oil_pipe.get())) {
				server.overworld().setBlockAndUpdate(pumpjackPos.below(2), ModBlocks.ore_oil.get().defaultBlockState());
			}
		}
		if(frackingPos != null && server.overworld().getBlockEntity(frackingPos) instanceof com.hbm.tileentity.machine.oil.TileEntityMachineFrackingTower frack) {
			frack.power = frack.getMaxPower();
			frack.tanks[2].setFill(frack.tanks[2].getMaxFill());
		}
		// the flare stack burning natural gas
		if(flarePos != null && server.overworld().getBlockEntity(flarePos) instanceof com.hbm.tileentity.machine.oil.TileEntityMachineGasFlare flare) {
			flare.tank.setTankType(com.hbm.inventory.fluid.Fluids.GAS);
			flare.tank.setFill(flare.tank.getMaxFill());
			flare.isOn = true;
			flare.doesBurn = true;
		}
		if(serverTicks == 1685 || serverTicks == 1720) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				player.closeContainer();
				if(serverTicks == 1685) player.teleportTo(player.serverLevel(), -2.5, -59, -10.5, 180F, -18F);
				else if(player.level().getBlockEntity(vacuumPos) instanceof net.minecraft.world.MenuProvider provider) player.openMenu(provider, buf -> buf.writeBlockPos(vacuumPos));
			}
		}
		if(serverTicks == 1610 || serverTicks == 1645) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				if(serverTicks == 1610) player.teleportTo(player.serverLevel(), -13.5, -59, -3.5, 148F, -52F);
				else if(player.level().getBlockEntity(flarePos) instanceof net.minecraft.world.MenuProvider provider) player.openMenu(provider, buf -> buf.writeBlockPos(flarePos));
			}
		}
		if(serverTicks == 1540 || serverTicks == 1575) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				if(serverTicks == 1540) player.teleportTo(player.serverLevel(), -13.5, -59, 12.5, 126F, 10F);
				else player.teleportTo(player.serverLevel(), -13.5, -59, -9.5, 79F, -35F);
			}
		}
		if(serverTicks == 1460 || serverTicks == 1500) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				player.closeContainer();
				if(serverTicks == 1460) player.teleportTo(player.serverLevel(), 14.5, -59, -8.5, -90F, -30F);
				else player.teleportTo(player.serverLevel(), -13.5, -59, 0.5, 90F, -25F);
			}
		}
		if(serverTicks == 1385 || serverTicks == 1420) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				player.closeContainer();
				if(serverTicks == 1385) player.teleportTo(player.serverLevel(), 13.5, -59, 1.5, -100F, -12F);
				else if(player.level().getBlockEntity(fluidTankPos) instanceof net.minecraft.world.MenuProvider provider) player.openMenu(provider, buf -> buf.writeBlockPos(fluidTankPos));
			}
		}
		if(serverTicks == 1310 || serverTicks == 1345) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				player.closeContainer();
				if(serverTicks == 1310) player.teleportTo(player.serverLevel(), -11.5, -60, 29.5, 180F, -25F);
				else if(player.level().getBlockEntity(crystallizerPos) instanceof net.minecraft.world.MenuProvider provider) player.openMenu(provider, buf -> buf.writeBlockPos(crystallizerPos));
			}
		}
		if(serverTicks == 1235 || serverTicks == 1270) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				player.closeContainer();
				if(serverTicks == 1235) player.teleportTo(player.serverLevel(), -11.5, -60, 34.2, 180F, -15F);
				else if(player.level().getBlockEntity(centrifugePos) instanceof net.minecraft.world.MenuProvider provider) player.openMenu(provider, buf -> buf.writeBlockPos(centrifugePos));
			}
		}
		if(serverTicks == 1160 || serverTicks == 1195) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				if(serverTicks == 1160) player.teleportTo(player.serverLevel(), -5.5, -60, 32.0, 160F, 25F);
				else if(player.level().getBlockEntity(combustionPos) instanceof net.minecraft.world.MenuProvider provider) player.openMenu(provider, buf -> buf.writeBlockPos(combustionPos));
			}
		}
		if(serverTicks == 1125) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				player.closeContainer();
				player.teleportTo(player.serverLevel(), 8.0, -60, 36.5, 180F, -8F);
			}
		}
		if(serverTicks == 1020 || serverTicks == 1055 || serverTicks == 1090) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				player.closeContainer();
				if(serverTicks == 1020) player.teleportTo(player.serverLevel(), 8.5, -60, 13.2, 0F, 12F);
				BlockPos gui = serverTicks == 1055 ? oilburnerPos : serverTicks == 1090 ? heatexPos : null;
				if(gui != null && player.level().getBlockEntity(gui) instanceof net.minecraft.world.MenuProvider provider) player.openMenu(provider, buf -> buf.writeBlockPos(gui));
			}
		}
		if(serverTicks == 945 || serverTicks == 985) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				if(serverTicks == 945) player.teleportTo(player.serverLevel(), 9.5, -60, 21.5, 90F, 8F);
				else player.teleportTo(player.serverLevel(), -5.5, -60, 15.2, 0F, 18F);
			}
		}
		if(serverTicks == 870 || serverTicks == 910) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				if(serverTicks == 870) player.teleportTo(player.serverLevel(), 15.5, -59, -2.5, -103F, -20F);
				else player.teleportTo(player.serverLevel(), 20.5, -60, 3.2, 0F, 15F);
			}
		}
		if(serverTicks == 830) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				player.closeContainer();
				player.teleportTo(player.serverLevel(), -6.5, -57, 4.5, 48F, 25F);
			}
		}
		// close-up of the foundry under the spout
		if(serverTicks == 715) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				player.closeContainer();
				player.teleportTo(player.serverLevel(), 5.3, -59, 6.5, -90F, 50F);
			}
		}
		if(serverTicks == 640 || serverTicks == 680) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				player.closeContainer();
				if(serverTicks == 640) player.teleportTo(player.serverLevel(), 0.5, -59, 7.5, -100F, -12F);
				else if(arcFurnacePos != null && player.level().getBlockEntity(arcFurnacePos) instanceof net.minecraft.world.MenuProvider provider) player.openMenu(provider, buf -> buf.writeBlockPos(arcFurnacePos));
			}
		}
		// steel anvil GUI (no block entity, the tier comes with the menu)
		if(serverTicks == 310) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
				player.closeContainer();
				player.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inv, p) -> new com.hbm.inventory.container.ContainerAnvil(id, inv, 2),
						net.minecraft.network.chat.Component.translatable("container.anvil", 2)), buf -> buf.writeInt(2));
			}
		}
		BlockPos open = serverTicks == 215 ? furnacePos : serverTicks == 255 ? burnerPos : serverTicks == 285 ? barrelPos : serverTicks == 345 ? assemblerPos : serverTicks == 395 ? chemplantPos : serverTicks == 435 ? welderPos : serverTicks == 475 ? blastFurnacePos : serverTicks == 515 ? shredderPos : null;
		if(open != null) {
			for(ServerPlayer player : server.getPlayerList().getPlayers()) {
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

		// deco blocks in the corner: walls, roof, beam, grates in front, pipes on every axis behind, fences at the edge
		{
			var S = com.hbm.blocks.generic.DecoBlock.FACING;
			level.setBlockAndUpdate(origin.offset(-13, 1, -9), ModBlocks.steel_wall.get().defaultBlockState().setValue(S, net.minecraft.core.Direction.NORTH));
			level.setBlockAndUpdate(origin.offset(-12, 1, -9), ModBlocks.steel_corner.get().defaultBlockState().setValue(S, net.minecraft.core.Direction.NORTH));
			level.setBlockAndUpdate(origin.offset(-11, 1, -9), ModBlocks.steel_roof.get().defaultBlockState());
			level.setBlockAndUpdate(origin.offset(-10, 1, -9), ModBlocks.steel_beam.get().defaultBlockState());
			level.setBlockAndUpdate(origin.offset(-9, 1, -9), ModBlocks.steel_grate.get().defaultBlockState().setValue(com.hbm.blocks.generic.BlockGrate.LEVEL, 3));
			level.setBlockAndUpdate(origin.offset(-8, 1, -9), ModBlocks.steel_grate_wide.get().defaultBlockState().setValue(com.hbm.blocks.generic.BlockGrate.LEVEL, 6));
			var A = net.minecraft.world.level.block.RotatedPillarBlock.AXIS;
			level.setBlockAndUpdate(origin.offset(-13, 1, -11), ModBlocks.deco_pipe.get().defaultBlockState());
			level.setBlockAndUpdate(origin.offset(-12, 1, -11), ModBlocks.deco_pipe_rim_rusted.get().defaultBlockState().setValue(A, net.minecraft.core.Direction.Axis.X));
			level.setBlockAndUpdate(origin.offset(-11, 1, -11), ModBlocks.deco_pipe_quad_green.get().defaultBlockState().setValue(A, net.minecraft.core.Direction.Axis.Z));
			level.setBlockAndUpdate(origin.offset(-10, 1, -11), ModBlocks.deco_pipe_framed_red.get().defaultBlockState());
			level.setBlockAndUpdate(origin.offset(-9, 1, -11), ModBlocks.deco_pipe_marked.get().defaultBlockState().setValue(A, net.minecraft.core.Direction.Axis.X));
			for(int x = -13; x <= -9; x++) {
				BlockPos fencePos = origin.offset(x, 1, -12);
				level.setBlockAndUpdate(fencePos, (x == -11 ? ModBlocks.fence_metal_post : ModBlocks.fence_metal).get().defaultBlockState());
			}
			for(int x = -13; x <= -9; x++) {
				BlockPos fencePos = origin.offset(x, 1, -12);
				level.setBlockAndUpdate(fencePos, level.getBlockState(fencePos).updateShape(net.minecraft.core.Direction.EAST, level.getBlockState(fencePos.east()), level, fencePos, fencePos.east())
						.updateShape(net.minecraft.core.Direction.WEST, level.getBlockState(fencePos.west()), level, fencePos, fencePos.west()));
			}
		}

		// electric arc furnace on the east side, spout towards the west, melting sand
		{
			arcFurnacePos = ModBlocks.machine_arc_furnace.get().placeMultiblock(level, origin.offset(10, 1, 6), net.minecraft.core.Direction.WEST);
			if(arcFurnacePos != null && level.getBlockEntity(arcFurnacePos) instanceof com.hbm.tileentity.machine.TileEntityMachineArcFurnaceLarge arc) {
				arc.setItem(3, new ItemStack(ModItems.battery_creative.get()));
				for(int e = 0; e < 3; e++) arc.setItem(e, ModItems.arc_electrode.stack(com.hbm.items.machine.ItemArcElectrode.EnumElectrodeType.values()[e]));
				arc.setItem(4, new ItemStack(ModItems.upgrade_speed_3.get()));
				// liquid mode: molten steel poured out of the spout into the channels below
				arc.liquidMode = true;
				arc.setItem(25, new ItemStack(ModItems.ingot_steel.get(), 64));
			}
			// foundry under the spout: channels leading to an ingot mold, a plate mold and a wire mold
			int[][] molds = { {8, 5, 2}, {8, 7, 3}, {7, 6, 4} };
			for(int[] m : molds) {
				BlockPos moldPos = origin.offset(m[0], 1, m[1]);
				level.setBlockAndUpdate(moldPos, ModBlocks.foundry_mold.get().defaultBlockState());
				if(level.getBlockEntity(moldPos) instanceof com.hbm.tileentity.machine.TileEntityFoundryMold mold) mold.slots.set(0, com.hbm.items.machine.ItemMold.stack(m[2]));
			}
			// channels after the molds, their connections come from the neighbors
			for(BlockPos ch : java.util.List.of(origin.offset(9, 1, 6), origin.offset(8, 1, 6))) {
				level.setBlockAndUpdate(ch, net.minecraft.world.level.block.Block.updateFromNeighbourShapes(ModBlocks.foundry_channel.get().defaultBlockState(), level, ch));
			}
		}

		// crucible on a firebox next to the foundry, alloying steel, some copper already molten
		{
			var N = net.minecraft.core.Direction.NORTH;
			BlockPos fb = ModBlocks.heater_firebox.get().placeMultiblock(level, origin.offset(5, 1, 8), N);
			if(fb != null && level.getBlockEntity(fb) instanceof com.hbm.tileentity.machine.TileEntityHeaterFirebox heater) heater.setItem(0, new ItemStack(net.minecraft.world.item.Items.COAL, 64));
			cruciblePos = ModBlocks.machine_crucible.get().placeMultiblock(level, origin.offset(5, 2, 8), N);
			if(cruciblePos != null && level.getBlockEntity(cruciblePos) instanceof com.hbm.tileentity.machine.TileEntityCrucible crucible) {
				crucible.recipe = "crucible.steel";
				crucible.setItem(1, new ItemStack(net.minecraft.world.item.Items.IRON_INGOT));
				for(int slot = 2; slot <= 4; slot++) crucible.setItem(slot, new ItemStack(net.minecraft.world.item.Items.CHARCOAL));
				crucible.setItem(5, new ItemStack(ModItems.powder_flux.get()));
				crucible.wasteStack.add(new com.hbm.inventory.material.Mats.MaterialStack(Mats.MAT_COPPER, com.hbm.inventory.material.MaterialShapes.BLOCK.q(4)));
				crucible.heat = com.hbm.tileentity.machine.TileEntityCrucible.maxHeat;
			}
			// a Stirling engine on another firebox and a heating oven next to the crucible
			BlockPos fb2 = ModBlocks.heater_firebox.get().placeMultiblock(level, origin.offset(2, 1, 8), N);
			if(fb2 != null && level.getBlockEntity(fb2) instanceof com.hbm.tileentity.machine.TileEntityHeaterFirebox heater) heater.setItem(0, new ItemStack(net.minecraft.world.item.Items.COAL, 64));
			ModBlocks.machine_stirling_steel.get().placeMultiblock(level, origin.offset(2, 2, 8), N);
			BlockPos ov = ModBlocks.heater_oven.get().placeMultiblock(level, origin.offset(-1, 1, 8), N);
			if(ov != null && level.getBlockEntity(ov) instanceof com.hbm.tileentity.machine.TileEntityHeaterOven oven) oven.setItem(0, new ItemStack(net.minecraft.world.item.Items.COAL, 64));
			// steel furnace on the oven, smelting ore
			BlockPos fs = ModBlocks.furnace_steel.get().placeMultiblock(level, origin.offset(-1, 2, 8), N);
			if(fs != null && level.getBlockEntity(fs) instanceof com.hbm.tileentity.machine.TileEntityFurnaceSteel furnace) {
				furnace.setItem(0, new ItemStack(net.minecraft.world.item.Items.IRON_ORE, 16));
				furnace.setItem(1, new ItemStack(net.minecraft.world.item.Items.OAK_LOG, 16));
			}
			// a pillar for the camera to look into the crucible
			level.setBlockAndUpdate(origin.offset(4, 1, 3), Blocks.STONE_BRICKS.defaultBlockState());
			level.setBlockAndUpdate(origin.offset(4, 2, 3), Blocks.STONE_BRICKS.defaultBlockState());
		}

		// steam engine along the x axis facing west, the condenser on its port side
		{
			steamEnginePos = ModBlocks.machine_steam_engine.get().placeMultiblock(level, origin.offset(-14, 1, 8), net.minecraft.core.Direction.WEST);
			condenserPos = origin.offset(-13, 2, 6);
			level.setBlockAndUpdate(condenserPos.below(), ModBlocks.block_steel.get().defaultBlockState());
			level.setBlockAndUpdate(condenserPos, ModBlocks.machine_condenser.get().defaultBlockState());
			// a pillar for the camera
			level.setBlockAndUpdate(origin.offset(-7, 1, 4), Blocks.STONE_BRICKS.defaultBlockState());
			level.setBlockAndUpdate(origin.offset(-7, 2, 4), Blocks.STONE_BRICKS.defaultBlockState());
		}

		// cooling towers and the powered condenser east of the floor, on the natural ground
		{
			for(int x = 17; x <= 32; x++) for(int z = -13; z <= 12; z++) for(int y = 0; y <= 20; y++) {
				level.setBlockAndUpdate(origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
			}
			towerLargePos = ModBlocks.machine_tower_large.get().placeMultiblock(level, origin.offset(27, 0, -4), net.minecraft.core.Direction.NORTH);
			towerSmallPos = ModBlocks.machine_tower_small.get().placeMultiblock(level, origin.offset(24, 0, -12), net.minecraft.core.Direction.NORTH);
			condenserPoweredPos = ModBlocks.machine_condenser_powered.get().placeMultiblock(level, origin.offset(20, 0, 7), net.minecraft.core.Direction.NORTH);
		}

		// the leviathan and the industrial turbine south of the floor
		{
			for(int x = -12; x <= 12; x++) for(int z = 14; z <= 32; z++) for(int y = 0; y <= 8; y++) {
				level.setBlockAndUpdate(origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
			}
			chungusPos = ModBlocks.machine_chungus.get().placeMultiblock(level, origin.offset(0, 0, 29), net.minecraft.core.Direction.SOUTH);
			industrialTurbinePos = ModBlocks.machine_industrial_turbine.get().placeMultiblock(level, origin.offset(-3, 0, 20), net.minecraft.core.Direction.EAST);
		}

		// heaters south of the floor, east of the turbines
		{
			oilburnerPos = ModBlocks.heater_oilburner.get().placeMultiblock(level, origin.offset(6, 0, 16), net.minecraft.core.Direction.NORTH);
			boilerOnBurnerPos = ModBlocks.machine_boiler.get().placeMultiblock(level, origin.offset(6, 2, 16), net.minecraft.core.Direction.NORTH);
			heatexPos = ModBlocks.heater_heatex.get().placeMultiblock(level, origin.offset(10, 0, 16), net.minecraft.core.Direction.NORTH);
			fireboxPos = ModBlocks.heater_firebox.get().placeMultiblock(level, origin.offset(8, 0, 23), net.minecraft.core.Direction.NORTH);
			industrialBoilerPos = ModBlocks.machine_industrial_boiler.get().placeMultiblock(level, origin.offset(8, 1, 23), net.minecraft.core.Direction.NORTH);
		}

		// water pumps on the grass south of the heaters
		{
			pumpSteamPos = ModBlocks.pump_steam.get().placeMultiblock(level, origin.offset(5, 0, 28), net.minecraft.core.Direction.NORTH);
			pumpElectricPos = ModBlocks.pump_electric.get().placeMultiblock(level, origin.offset(11, 0, 28), net.minecraft.core.Direction.NORTH);
		}

		// combustion engine on the grass south-west of the heaters
		combustionPos = ModBlocks.machine_combustion_engine.get().placeMultiblock(level, origin.offset(-8, 0, 28), net.minecraft.core.Direction.NORTH);

		// centrifuge west of the combustion engine
		centrifugePos = ModBlocks.machine_centrifuge.get().placeMultiblock(level, origin.offset(-11, 0, 31), net.minecraft.core.Direction.NORTH);

		// ore acidizer north of the centrifuge
		crystallizerPos = ModBlocks.machine_crystallizer.get().placeMultiblock(level, origin.offset(-12, 0, 23), net.minecraft.core.Direction.NORTH);

		// fluid tank, orbus and BAT9000 east of the floor
		{
			fluidTankPos = ModBlocks.machine_fluidtank.get().placeMultiblock(level, origin.offset(19, 0, -3), net.minecraft.core.Direction.NORTH);
			if(fluidTankPos != null && level.getBlockEntity(fluidTankPos) instanceof com.hbm.tileentity.machine.storage.TileEntityMachineFluidTank tank) {
				tank.tank.setTankType(com.hbm.inventory.fluid.Fluids.DIESEL);
				tank.tank.setFill(128_000);
			}
			BlockPos orbusPos = ModBlocks.machine_orbus.get().placeMultiblock(level, origin.offset(19, 0, 3), net.minecraft.core.Direction.NORTH);
			if(orbusPos != null && level.getBlockEntity(orbusPos) instanceof com.hbm.tileentity.machine.storage.TileEntityMachineOrbus orbus) {
				orbus.tank.setTankType(com.hbm.inventory.fluid.Fluids.HYDROGEN);
				orbus.tank.setFill(300_000);
			}
			BlockPos batPos = ModBlocks.machine_bat9000.get().placeMultiblock(level, origin.offset(28, 0, 7), net.minecraft.core.Direction.NORTH);
			if(batPos != null && level.getBlockEntity(batPos) instanceof com.hbm.tileentity.machine.storage.TileEntityMachineBAT9000 bat) {
				bat.tank.setTankType(com.hbm.inventory.fluid.Fluids.KEROSENE);
				bat.tank.setFill(1_200_000);
			}
		}

		// fractioning towers (two segments and a separator) east of the floor, the catalytic cracker west of it
		{
			BlockPos frac = ModBlocks.machine_fraction_tower.get().placeMultiblock(level, origin.offset(19, 0, -9), net.minecraft.core.Direction.NORTH);
			ModBlocks.machine_fraction_tower.get().placeMultiblock(level, origin.offset(19, 3, -9), net.minecraft.core.Direction.NORTH);
			ModBlocks.fraction_spacer.get().placeMultiblock(level, origin.offset(19, 6, -9), net.minecraft.core.Direction.NORTH);
			if(frac != null && level.getBlockEntity(frac) instanceof com.hbm.tileentity.machine.oil.TileEntityMachineFractionTower tower) {
				tower.tanks[0].setTankType(com.hbm.inventory.fluid.Fluids.HEAVYOIL);
				tower.tanks[0].setFill(4_000);
			}

			for(int x = -30; x <= -15; x++) for(int z = -10; z <= 10; z++) for(int y = 0; y <= 16; y++) {
				level.setBlockAndUpdate(origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
			}
			BlockPos cracker = ModBlocks.machine_catalytic_cracker.get().placeMultiblock(level, origin.offset(-22, 0, -3), net.minecraft.core.Direction.NORTH);
			if(cracker != null && level.getBlockEntity(cracker) instanceof com.hbm.tileentity.machine.oil.TileEntityMachineCatalyticCracker cat) {
				cat.tanks[0].setTankType(com.hbm.inventory.fluid.Fluids.OIL);
				cat.tanks[0].setFill(4_000);
				cat.tanks[1].setFill(8_000);
			}
		}

		// pumpjack and fracking tower west of the floor, next to the cracker
		pumpjackPos = ModBlocks.machine_pumpjack.get().placeMultiblock(level, origin.offset(-18, 0, 7), net.minecraft.core.Direction.NORTH);
		if(pumpjackPos != null) {
			level.setBlockAndUpdate(pumpjackPos.below(), Blocks.STONE.defaultBlockState());
			level.setBlockAndUpdate(pumpjackPos.below(2), ModBlocks.ore_oil.get().defaultBlockState());
		}
		frackingPos = ModBlocks.machine_fracking_tower.get().placeMultiblock(level, origin.offset(-26, 0, -7), net.minecraft.core.Direction.NORTH);

		// flare stack between the cracker and the floor
		flarePos = ModBlocks.machine_flare.get().placeMultiblock(level, origin.offset(-17, 0, -9), net.minecraft.core.Direction.NORTH);

		// vacuum distiller, catalytic reformer and hydrotreater north of the floor
		{
			for(int x = -14; x <= 16; x++) for(int z = -22; z <= -13; z++) for(int y = 0; y <= 12; y++) {
				level.setBlockAndUpdate(origin.offset(x, y, z), Blocks.AIR.defaultBlockState());
			}
			vacuumPos = ModBlocks.machine_vacuum_distill.get().placeMultiblock(level, origin.offset(-8, 0, -16), net.minecraft.core.Direction.NORTH);
			BlockPos reformerPos = ModBlocks.machine_catalytic_reformer.get().placeMultiblock(level, origin.offset(-2, 0, -16), net.minecraft.core.Direction.NORTH);
			BlockPos treaterPos = ModBlocks.machine_hydrotreater.get().placeMultiblock(level, origin.offset(4, 0, -16), net.minecraft.core.Direction.NORTH);
			for(BlockPos machine : new BlockPos[] {vacuumPos, reformerPos, treaterPos}) {
				if(machine != null && level.getBlockEntity(machine) instanceof com.hbm.tileentity.machine.oil.TileEntityOilProcessorBase proc) {
					proc.setPower(com.hbm.tileentity.machine.oil.TileEntityOilProcessorBase.maxPower);
					proc.tanks[0].setFill(proc.tanks[0].getMaxFill());
					if(proc.getContainerSize() > 10) proc.setItem(10, new ItemStack(ModItems.catalytic_converter.get()));
				}
			}
			if(treaterPos != null && level.getBlockEntity(treaterPos) instanceof com.hbm.tileentity.machine.oil.TileEntityMachineHydrotreater treater) treater.tanks[1].setFill(32_000);
			if(vacuumPos != null && level.getBlockEntity(vacuumPos) instanceof com.hbm.tileentity.machine.oil.TileEntityMachineVacuumDistill distill) distill.setItem(10, ItemStack.EMPTY);
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

		static volatile int ticks = 0;

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

			// deco blocks, the server moves the camera at 600
			if(ticks == 600) mc.setScreen(null);
			if(ticks == 625) Screenshot.grab(mc.gameDirectory, "devscene_world_deco.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));

			if(ticks == 670) Screenshot.grab(mc.gameDirectory, "devscene_world_arc_furnace.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			if(ticks == 705) Screenshot.grab(mc.gameDirectory, "devscene_gui_arc_furnace.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));

			if(ticks == 740) Screenshot.grab(mc.gameDirectory, "devscene_world_foundry.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));

			if(ticks == 775) Screenshot.grab(mc.gameDirectory, "devscene_world_crucible.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			if(ticks == 815) Screenshot.grab(mc.gameDirectory, "devscene_gui_crucible.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));

			if(ticks == 860) Screenshot.grab(mc.gameDirectory, "devscene_world_steam_engine.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));

			if(ticks == 900) Screenshot.grab(mc.gameDirectory, "devscene_world_cooling_towers.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			if(ticks == 935) Screenshot.grab(mc.gameDirectory, "devscene_world_condenser_powered.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));

			if(ticks == 975) Screenshot.grab(mc.gameDirectory, "devscene_world_leviathan.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			if(ticks == 1010) Screenshot.grab(mc.gameDirectory, "devscene_world_industrial_turbine.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));

			if(ticks == 1045) Screenshot.grab(mc.gameDirectory, "devscene_world_heaters.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			if(ticks == 1080) Screenshot.grab(mc.gameDirectory, "devscene_gui_oilburner.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			if(ticks == 1115) Screenshot.grab(mc.gameDirectory, "devscene_gui_heatex.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));

			if(ticks == 1150) Screenshot.grab(mc.gameDirectory, "devscene_world_pumps.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));

			if(ticks == 1185) Screenshot.grab(mc.gameDirectory, "devscene_world_combustion_engine.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			if(ticks == 1225) Screenshot.grab(mc.gameDirectory, "devscene_gui_combustion_engine.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));

			if(ticks == 1260) Screenshot.grab(mc.gameDirectory, "devscene_world_centrifuge.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			if(ticks == 1300) Screenshot.grab(mc.gameDirectory, "devscene_gui_centrifuge.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));

			if(ticks == 1335) Screenshot.grab(mc.gameDirectory, "devscene_world_crystallizer.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			if(ticks == 1375) Screenshot.grab(mc.gameDirectory, "devscene_gui_crystallizer.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));

			if(ticks == 1410) Screenshot.grab(mc.gameDirectory, "devscene_world_tanks.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			if(ticks == 1450) Screenshot.grab(mc.gameDirectory, "devscene_gui_fluid_tank.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));

			if(ticks == 1490) Screenshot.grab(mc.gameDirectory, "devscene_world_fraction_tower.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			if(ticks == 1530) Screenshot.grab(mc.gameDirectory, "devscene_world_cracker.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));

			if(ticks == 1565) Screenshot.grab(mc.gameDirectory, "devscene_world_pumpjack.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			if(ticks == 1600) Screenshot.grab(mc.gameDirectory, "devscene_world_fracking.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));

			if(ticks == 1640) Screenshot.grab(mc.gameDirectory, "devscene_world_flare.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			if(ticks == 1675) Screenshot.grab(mc.gameDirectory, "devscene_gui_flare.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));

			if(ticks == 1718) Screenshot.grab(mc.gameDirectory, "devscene_world_oil_processing.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			if(ticks == 1750) Screenshot.grab(mc.gameDirectory, "devscene_gui_vacuum_distill.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));

			if(ticks == 1760) {
				MainRegistry.logger.info("DevScene: done, screenshots in " + new File(mc.gameDirectory, "screenshots").getAbsolutePath());
				mc.stop();
			}
		}
	}
}
