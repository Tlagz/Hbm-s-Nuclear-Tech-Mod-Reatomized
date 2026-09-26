package com.hbm.tileentity.machine.oil;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.container.ContainerMachineOilWell;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.util.BobMathUtil;
import com.hbm.util.DirPos;
import com.hbm.util.i18n.I18nUtil;
import com.hbm.world.feature.OilSpot;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Hydraulic fracking tower: needs fracking solution, drills from the surface, also taps bedrock oil, and contaminates
 * the land around it.
 * TODO config
 */
public class TileEntityMachineFrackingTower extends TileEntityOilDrillBase implements MenuProvider {

	protected static int maxPower = 5_000_000;
	protected static int consumption = 5000;
	protected static int solutionRequired = 10;
	protected static int delay = 20;
	protected static int oilPerDepsoit = 1000;
	protected static int gasPerDepositMin = 100;
	protected static int gasPerDepositMax = 500;
	protected static double drainChance = 0.02D;
	protected static int oilPerBedrockDepsoit = 100;
	protected static int gasPerBedrockDepositMin = 10;
	protected static int gasPerBedrockDepositMax = 50;
	protected static int destructionRange = 75;

	public TileEntityMachineFrackingTower(BlockPos pos, BlockState state) {
		super(ModTileEntities.FRACKING_TOWER.get(), pos, state);
		tanks = new FluidTank[3];
		tanks[0] = new FluidTank(Fluids.OIL, 64_000);
		tanks[1] = new FluidTank(Fluids.GAS, 64_000);
		tanks[2] = new FluidTank(Fluids.FRACKSOL, 64_000);
	}

	@Override
	public String getName() {
		return "container.frackingTower";
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

	/** The original drilled all the way down (y 0), the world bottom here */
	@Override
	public int getDrillDepth() {
		return level.getMinBuildHeight();
	}

	@Override
	public boolean canPump() {
		boolean b = this.tanks[2].getFill() >= solutionRequired;

		if(!b) {
			this.indicator = 3;
		}

		return b;
	}

	@Override
	public boolean canSuckBlock(BlockState b) {
		return super.canSuckBlock(b) || b.is(ModBlocks.ore_bedrock_oil.get());
	}

	@Override
	public void doSuck(BlockPos pos) {
		super.doSuck(pos);

		if(level.getBlockState(pos).is(ModBlocks.ore_bedrock_oil.get())) {
			onSuck(pos);
		}
	}

	@Override
	public void onSuck(BlockPos pos) {

		BlockState b = level.getBlockState(pos);

		int oil = 0;
		int gas = 0;

		if(b.is(ModBlocks.ore_oil.get())) {
			oil = oilPerDepsoit;
			gas = gasPerDepositMin + level.random.nextInt(gasPerDepositMax - gasPerDepositMin + 1);

			if(level.random.nextDouble() < drainChance) {
				level.setBlock(pos, ModBlocks.ore_oil_empty.get().defaultBlockState(), Block.UPDATE_ALL);
			}
		}

		if(b.is(ModBlocks.ore_bedrock_oil.get())) {
			oil = oilPerBedrockDepsoit;
			gas = gasPerBedrockDepositMin + level.random.nextInt(gasPerBedrockDepositMax - gasPerBedrockDepositMin + 1);
		}

		this.tanks[0].setFill(this.tanks[0].getFill() + oil);
		if(this.tanks[0].getFill() > this.tanks[0].getMaxFill()) this.tanks[0].setFill(tanks[0].getMaxFill());
		this.tanks[1].setFill(this.tanks[1].getFill() + gas);
		if(this.tanks[1].getFill() > this.tanks[1].getMaxFill()) this.tanks[1].setFill(tanks[1].getMaxFill());

		this.tanks[2].setFill(tanks[2].getFill() - solutionRequired);

		OilSpot.generateOilSpot(level, worldPosition.getX(), worldPosition.getZ(), destructionRange, 10, false);
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] { tanks[0], tanks[1] };
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] { tanks[2] };
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
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
	protected void updateConnections() {
		for(DirPos pos : getConPos()) {
			this.trySubscribe(level, pos, pos.getDir());
			this.trySubscribe(tanks[2].getTankType(), level, pos, pos.getDir());
		}
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 3, worldPosition.getY(), worldPosition.getZ() - 3, worldPosition.getX() + 4, worldPosition.getY() + 25, worldPosition.getZ() + 4);
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_fracking_tower.get()));
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
			info.add((BobMathUtil.getBlink() ? ChatFormatting.RED : ChatFormatting.DARK_GRAY) + "YES");
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
