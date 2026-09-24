package com.hbm.inventory;

import com.hbm.inventory.container.ContainerBarrel;
import com.hbm.inventory.container.ContainerElectricFurnace;
import com.hbm.inventory.container.ContainerMachineWoodBurner;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineElectricFurnace;
import com.hbm.tileentity.machine.TileEntityMachineWoodBurner;
import com.hbm.tileentity.machine.storage.TileEntityBarrel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Menu types. Every machine menu is opened with the tile's position in the extra data, the client
 * looks the tile up at that position (the original's GUIHandler did the same with x/y/z).
 */
public class ModMenus {

	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, RefStrings.MODID);

	public static final DeferredHolder<MenuType<?>, MenuType<ContainerElectricFurnace>> ELECTRIC_FURNACE =
			tile("electric_furnace", TileEntityMachineElectricFurnace.class, ContainerElectricFurnace::new);

	public static final DeferredHolder<MenuType<?>, MenuType<ContainerMachineWoodBurner>> WOOD_BURNER =
			tile("machine_wood_burner", TileEntityMachineWoodBurner.class, ContainerMachineWoodBurner::new);

	public static final DeferredHolder<MenuType<?>, MenuType<ContainerBarrel>> BARREL =
			tile("barrel", TileEntityBarrel.class, ContainerBarrel::new);

	@FunctionalInterface
	public interface TileMenuFactory<T extends BlockEntity, M extends AbstractContainerMenu> {
		M create(int id, Inventory inv, T tile);
	}

	private static <T extends BlockEntity, M extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<M>> tile(String name, Class<T> tileClass, TileMenuFactory<T, M> factory) {
		return MENUS.register(name, () -> IMenuTypeExtension.create((int id, Inventory inv, RegistryFriendlyByteBuf buf) -> {
			BlockPos pos = buf.readBlockPos();
			BlockEntity tile = inv.player.level().getBlockEntity(pos);
			if(!tileClass.isInstance(tile)) throw new IllegalStateException("No " + tileClass.getSimpleName() + " at " + pos);
			return factory.create(id, inv, tileClass.cast(tile));
		}));
	}
}
