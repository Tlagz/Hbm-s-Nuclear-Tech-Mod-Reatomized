package com.hbm.tileentity.machine.oil;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.container.ContainerMachineOilWell;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.util.DirPos;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Oil derrick: 100 HE/t, drills every 50 ticks, 500mB oil and 100-500mB gas per deposit block, 5% chance
 * to drain the deposit block.
 *
 * TODO radon/asbestos gas when drilling through uranium/asbestos ore (gas blocks not ported)
 */
public class TileEntityMachineOilWell extends TileEntityOilDrillBase implements MenuProvider {

	protected static int maxPower = 100_000;
	protected static int consumption = 100;
	protected static int delay = 50;
	protected static int oilPerDepsoit = 500;
	protected static int gasPerDepositMin = 100;
	protected static int gasPerDepositMax = 500;
	protected static double drainChance = 0.05D;

	public TileEntityMachineOilWell(BlockPos pos, BlockState state) {
		super(ModTileEntities.OIL_WELL.get(), pos, state);
	}

	@Override
	public String getName() {
		return "container.oilWell";
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public int getPowerReq() {
		return consumption;
	}

	@Override
	public int getDelay() {
		return delay;
	}

	@Override
	public void onSuck(BlockPos pos) {

		level.playSound(null, worldPosition, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 2.0F, 0.5F);

		this.tanks[0].setFill(this.tanks[0].getFill() + oilPerDepsoit);
		if(this.tanks[0].getFill() > this.tanks[0].getMaxFill()) this.tanks[0].setFill(tanks[0].getMaxFill());
		this.tanks[1].setFill(this.tanks[1].getFill() + (gasPerDepositMin + level.random.nextInt((gasPerDepositMax - gasPerDepositMin + 1))));
		if(this.tanks[1].getFill() > this.tanks[1].getMaxFill()) this.tanks[1].setFill(tanks[1].getMaxFill());

		if(level.random.nextDouble() < drainChance) {
			level.setBlock(pos, ModBlocks.ore_oil_empty.get().defaultBlockState(), Block.UPDATE_ALL);
		}
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 10, worldPosition.getZ() + 2);
	}

	@Override
	public DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(worldPosition.east(), Direction.EAST),
				new DirPos(worldPosition.west(), Direction.WEST),
				new DirPos(worldPosition.south(), Direction.SOUTH),
				new DirPos(worldPosition.north(), Direction.NORTH)
		};
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_well.get()));
		if(type == UpgradeType.SPEED) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_DELAY, "-" + (level * 25) + "%"));
			info.add(ChatFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + (level * 25) + "%"));
		}
		if(type == UpgradeType.POWER) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_CONSUMPTION, "-" + (level * 25) + "%"));
			info.add(ChatFormatting.RED + I18nUtil.resolveKey(KEY_DELAY, "+" + (level * 10) + "%"));
		}
		if(type == UpgradeType.AFTERBURN) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_BURN, level * 10, level * 50));
		}
		if(type == UpgradeType.OVERDRIVE) {
			info.add(((System.currentTimeMillis() / 500) % 2 == 0 ? ChatFormatting.RED : ChatFormatting.DARK_GRAY) + "YES");
		}
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineOilWell(id, inv, this);
	}
}
