package com.hbm.tileentity.machine;

import com.hbm.inventory.container.ContainerFirebox;
import com.hbm.module.ModuleBurnTime;
import com.hbm.tileentity.ModTileEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Firebox: 100 TU/t base, coal-type fuels burn 25% longer and twice as hot, solid fuel 3x, rocket fuel 5x.
 *
 * TODO machine config file (IConfigurableMachine)
 */
public class TileEntityHeaterFirebox extends TileEntityFireboxBase implements MenuProvider {

	public static int baseHeat = 100;
	public static double timeMult = 1D;
	public static int maxHeatEnergy = 100_000;
	public static ModuleBurnTime burnModule = new ModuleBurnTime()
			.setLigniteTimeMod(1.25)
			.setCoalTimeMod(1.25)
			.setCokeTimeMod(1.25)
			.setSolidTimeMod(1.5)
			.setRocketTimeMod(1.5)
			.setBalefireTimeMod(0.5)

			.setLigniteHeatMod(2)
			.setCoalHeatMod(2)
			.setCokeHeatMod(2)
			.setSolidHeatMod(3)
			.setRocketHeatMod(5)
			.setBalefireHeatMod(15);

	public TileEntityHeaterFirebox(BlockPos pos, BlockState state) {
		super(ModTileEntities.FIREBOX.get(), pos, state);
	}

	@Override
	public String getName() {
		return "container.heaterFirebox";
	}

	@Override
	public ModuleBurnTime getModule() {
		return burnModule;
	}

	@Override
	public int getBaseHeat() {
		return baseHeat;
	}

	@Override
	public double getTimeMult() {
		return timeMult;
	}

	@Override
	public int getMaxHeat() {
		return maxHeatEnergy;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerFirebox(id, inv, this);
	}
}
