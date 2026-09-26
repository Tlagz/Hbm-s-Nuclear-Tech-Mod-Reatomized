package com.hbm.tileentity.machine.oil;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.container.ContainerMachineOilWell;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.util.BobMathUtil;
import com.hbm.util.DirPos;
import com.hbm.util.i18n.I18nUtil;

import io.netty.buffer.ByteBuf;
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
 * Pumpjack: a faster, cheaper oil drill than the derrick, with the nodding head animation.
 * TODO radon/asbestos gas when drilling through uranium or asbestos ore (gas blocks not ported), config
 */
public class TileEntityMachinePumpjack extends TileEntityOilDrillBase implements MenuProvider {

	protected static int maxPower = 250_000;
	protected static int consumption = 200;
	protected static int delay = 25;
	protected static int oilPerDepsoit = 750;
	protected static int gasPerDepositMin = 50;
	protected static int gasPerDepositMax = 250;
	protected static double drainChance = 0.025D;

	public float rot = 0;
	public float prevRot = 0;
	public float speed = 0;

	public TileEntityMachinePumpjack(BlockPos pos, BlockState state) {
		super(ModTileEntities.PUMPJACK.get(), pos, state);
	}

	@Override
	public String getName() {
		return "container.pumpjack";
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
	public void updateEntity() {
		super.updateEntity();

		if(!isServer()) {
			this.prevRot = rot;

			if(this.indicator == 0) {
				this.rot += speed;
			}

			if(this.rot >= 360) {
				this.prevRot -= 360;
				this.rot -= 360;
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeFloat(this.indicator == 0 ? (5F + (2F * this.speedLevel)) + (this.overLevel - 1F) * 10 : 0F);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.speed = buf.readFloat();
	}

	@Override
	public void onSuck(BlockPos pos) {

		this.tanks[0].setFill(this.tanks[0].getFill() + oilPerDepsoit);
		if(this.tanks[0].getFill() > this.tanks[0].getMaxFill()) this.tanks[0].setFill(tanks[0].getMaxFill());
		this.tanks[1].setFill(this.tanks[1].getFill() + (gasPerDepositMin + level.random.nextInt((gasPerDepositMax - gasPerDepositMin + 1))));
		if(this.tanks[1].getFill() > this.tanks[1].getMaxFill()) this.tanks[1].setFill(tanks[1].getMaxFill());

		if(level.random.nextDouble() < drainChance) {
			level.setBlock(pos, ModBlocks.ore_oil_empty.get().defaultBlockState(), Block.UPDATE_ALL);
		}
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 7, worldPosition.getY(), worldPosition.getZ() - 7, worldPosition.getX() + 8, worldPosition.getY() + 6, worldPosition.getZ() + 8);
	}

	/** The original's port positions, the mixed up offsets of the second and fourth port included */
	@Override
	public DirPos[] getConPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getCounterClockWise(); // getRotation(DOWN)
		int x = worldPosition.getX(), y = worldPosition.getY(), z = worldPosition.getZ();

		return new DirPos[] {
			new DirPos(new BlockPos(x + rot.getStepX() * 2 + dir.getStepX() * 2, y, z + rot.getStepZ() * 2 + dir.getStepZ() * 2), dir),
			new DirPos(new BlockPos(x + rot.getStepX() * 2 + dir.getStepX() * 2, y, z + rot.getStepZ() * 4 - dir.getStepZ() * 2), dir.getOpposite()),
			new DirPos(new BlockPos(x + rot.getStepX() * 4 - dir.getStepX() * 2, y, z + rot.getStepZ() * 4 + dir.getStepZ() * 2), dir),
			new DirPos(new BlockPos(x + rot.getStepX() * 4 - dir.getStepX() * 2, y, z + rot.getStepZ() * 2 - dir.getStepZ() * 2), dir.getOpposite())
		};
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_pumpjack.get()));
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
