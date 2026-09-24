package com.hbm.test;

import java.io.File;

import com.hbm.blocks.ModBlocks;
import com.hbm.creativetabs.NtmTab;
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
	private static int serverTicks = 0;

	/** Opens the furnace GUI for the screenshot, has to happen on the server */
	@SubscribeEvent
	public static void onServerTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
		if(!ENABLED || furnacePos == null) return;
		serverTicks++;
		// second world screenshot: close up of the wood burner, holding its item
		if(serverTicks == 160 && burnerPos != null) {
			for(ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
				player.teleportTo(player.serverLevel(), burnerPos.getX() + 3.5, burnerPos.getY(), burnerPos.getZ() - 5.5, -20F, 30F);
				player.getInventory().selected = 0;
				player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket(0));
			}
		}
		BlockPos open = serverTicks == 205 ? furnacePos : serverTicks == 245 ? burnerPos : serverTicks == 275 ? barrelPos : null;
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
		for(int x = -14; x <= 14; x++) for(int z = -7; z <= 12; z++) for(int y = 0; y <= 10; y++) {
			level.setBlockAndUpdate(origin.offset(x, y, z), y == 0 ? Blocks.SMOOTH_STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
		}

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
		}

		player.setGameMode(GameType.CREATIVE);
		level.setDayTime(6000);
		player.teleportTo(level, -4.5, origin.getY() + 2, -1.5, 20F, 15F);
		player.getInventory().clearContent();
		player.getInventory().add(new ItemStack(ModBlocks.machine_wood_burner.get()));
		player.getInventory().add(new ItemStack(ModItems.geiger_counter.get()));
		player.getInventory().add(new ItemStack(ModBlocks.red_cable.get()));
		player.getInventory().add(new ItemStack(ModItems.ingot_uranium.get(), 16));
		player.getInventory().add(new ItemStack(ModBlocks.ore_uranium.get()));
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

			if(ticks == 150 || ticks == 185) {
				if(mc.screen != null) mc.setScreen(null);
				Screenshot.grab(mc.gameDirectory, "devscene_world_" + ticks + ".png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			}

			if(ticks == 230) {
				Screenshot.grab(mc.gameDirectory, "devscene_gui_furnace.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			}
			if(ticks == 295) {
				Screenshot.grab(mc.gameDirectory, "devscene_gui_barrel.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			}
			if(ticks == 270) {
				Screenshot.grab(mc.gameDirectory, "devscene_gui_wood_burner.png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			}

			// creative tabs, one screenshot each
			NtmTab[] tabs = { NtmTab.MACHINE, NtmTab.CONTROL };
			for(int t = 0; t < tabs.length; t++) {
				if(ticks == 310 + t * 20) openTab(mc, tabs[t]);
				if(ticks == 325 + t * 20) Screenshot.grab(mc.gameDirectory, "devscene_tab_" + tabs[t].name().toLowerCase() + ".png", mc.getMainRenderTarget(), msg -> MainRegistry.logger.info("DevScene: " + msg.getString()));
			}

			if(ticks == 310 + tabs.length * 20) {
				MainRegistry.logger.info("DevScene: done, screenshots in " + new File(mc.gameDirectory, "screenshots").getAbsolutePath());
				mc.stop();
			}
		}
	}
}
