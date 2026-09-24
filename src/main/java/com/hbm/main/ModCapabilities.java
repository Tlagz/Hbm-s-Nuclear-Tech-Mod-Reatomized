package com.hbm.main;

import com.hbm.lib.RefStrings;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.tileentity.TileEntityProxyCombo;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;

/** Item handler capability for all machines with an inventory, so hoppers and item pipes of other mods work */
@EventBusSubscriber(modid = RefStrings.MODID)
public class ModCapabilities {

	@SubscribeEvent
	public static void register(RegisterCapabilitiesEvent event) {
		for(var holder : ModTileEntities.TILES.getEntries()) {
			BlockEntityType<?> type = holder.get();
			event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (tile, side) -> {
				// multiblock dummies with the inventory flag hand out the core's inventory
				BlockEntity target = tile;
				if(tile instanceof TileEntityProxyCombo proxy) {
					if(!proxy.inventory) return null;
					target = proxy.getTile();
				}
				if(!(target instanceof TileEntityMachineBase machine)) return null;
				return side == null ? new InvWrapper(machine) : new SidedInvWrapper(machine, side);
			});
		}
	}
}
