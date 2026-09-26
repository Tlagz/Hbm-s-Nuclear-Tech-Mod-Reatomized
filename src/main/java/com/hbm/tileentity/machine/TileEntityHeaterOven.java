package com.hbm.tileentity.machine;

import com.hbm.inventory.container.ContainerFirebox;
import com.hbm.module.ModuleBurnTime;
import com.hbm.tileentity.ModTileEntities;

import api.hbm.tile.IHeatSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Heating oven: a bigger firebox that burns fuel five times hotter and also takes half the heat of the heater below
 * it, so heaters can be stacked.
 *
 * TODO config (IConfigurableMachine)
 */
public class TileEntityHeaterOven extends TileEntityFireboxBase implements MenuProvider {

	public static int baseHeat = 500;
	public static double timeMult = 0.125D;
	public static int maxHeatEnergy = 500_000;
	public static double heatEff = 0.5D;
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

	public TileEntityHeaterOven(BlockPos pos, BlockState state) {
		super(ModTileEntities.HEATER_OVEN.get(), pos, state);
	}

	@Override
	public String getName() {
		return "container.heaterOven";
	}

	@Override
	public void updateEntity() {
		if(isServer()) {
			this.tryPullHeat();
		}
		super.updateEntity();
	}

	protected void tryPullHeat() {
		BlockEntity con = level.getBlockEntity(worldPosition.below());

		if(con instanceof IHeatSource source) {
			int toPull = Math.max(Math.min(source.getHeatStored(), this.getMaxHeat() - this.heatEnergy), 0);
			this.heatEnergy += toPull * heatEff;
			source.useUpHeat(toPull);
		}
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
