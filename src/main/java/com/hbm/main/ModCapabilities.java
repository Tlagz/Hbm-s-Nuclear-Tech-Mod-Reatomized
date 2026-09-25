package com.hbm.main;

import com.hbm.lib.RefStrings;
import com.hbm.tileentity.ConditionalInvView;
import com.hbm.tileentity.IConditionalInvAccess;
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
				// other inventories with sided access, e.g. foundry molds handing out the cast item
				if(!(target instanceof TileEntityMachineBase machine)) {
					if(target instanceof net.minecraft.world.WorldlyContainer worldly) return side == null ? new InvWrapper(worldly) : new SidedInvWrapper(worldly, side);
					return null;
				}
				if(side == null) return new InvWrapper(machine);
				// machines with port specific slots see which block is asked (the core counts as a port too)
				if(machine instanceof IConditionalInvAccess access) return new SidedInvWrapper(new ConditionalInvView(machine, access, tile.getBlockPos()), side);
				return new SidedInvWrapper(machine, side);
			});
		}
	}
}
