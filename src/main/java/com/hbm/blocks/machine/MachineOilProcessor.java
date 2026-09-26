package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.ModDataComponents;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.oil.TileEntityMachineCatalyticReformer;
import com.hbm.tileentity.machine.oil.TileEntityMachineHydrotreater;
import com.hbm.tileentity.machine.oil.TileEntityMachineVacuumDistill;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The original's MachineVacuumDistill, MachineCatalyticReformer and MachineHydrotreater: towers on a 3x3 (reformer
 * 5x3) base with power and fluid ports at the corners, the tanks are kept when broken.
 */
public abstract class MachineOilProcessor extends BlockDummyable {

	public MachineOilProcessor(Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	protected abstract int tankCount();

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);
		this.makeExtra(world, core.offset(1, 0, 1));
		this.makeExtra(world, core.offset(1, 0, -1));
		this.makeExtra(world, core.offset(-1, 0, 1));
		this.makeExtra(world, core.offset(-1, 0, -1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		CustomData persistent = stack.get(ModDataComponents.PERSISTENT.get());
		if(persistent == null) return;
		for(int i = 0; i < tankCount(); i++) {
			FluidTank tank = new FluidTank(Fluids.NONE, 0);
			tank.readFromNBT(persistent.copyTag(), "" + i);
			list.add(Component.literal(tank.getFill() + "/" + tank.getMaxFill() + "mB ").append(Component.translatable(tank.getTankType().getConditionalName())).withStyle(ChatFormatting.YELLOW));
		}
	}

	/** Vacuum distiller, 3x3x9 */
	public static class VacuumDistill extends MachineOilProcessor {

		public VacuumDistill(Properties properties) { super(properties); }

		@Override
		public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
			if(meta >= 12) return new TileEntityMachineVacuumDistill(pos, state);
			if(meta >= extra) return new TileEntityProxyCombo(pos, state).fluid().power();
			return null;
		}

		@Override public int[] getDimensions() { return new int[] {8, 0, 1, 1, 1, 1}; }
		@Override public int getOffset() { return 1; }
		@Override protected int tankCount() { return 5; }
	}

	/** Hydrotreater, 3x3x7 */
	public static class Hydrotreater extends MachineOilProcessor {

		public Hydrotreater(Properties properties) { super(properties); }

		@Override
		public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
			if(meta >= 12) return new TileEntityMachineHydrotreater(pos, state);
			if(meta >= extra) return new TileEntityProxyCombo(pos, state).fluid().power();
			return null;
		}

		@Override public int[] getDimensions() { return new int[] {6, 0, 1, 1, 1, 1}; }
		@Override public int getOffset() { return 1; }
		@Override protected int tankCount() { return 4; }
	}

	/** Catalytic reformer, a 5x3 base with two towers */
	public static class CatalyticReformer extends MachineOilProcessor {

		private static final int[] TOWER_A = new int[] {3, -3, 1, 0, -1, 2};
		private static final int[] TOWER_B = new int[] {6, -3, 1, 1, 2, 0};

		public CatalyticReformer(Properties properties) { super(properties); }

		@Override
		public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
			if(meta >= 12) return new TileEntityMachineCatalyticReformer(pos, state);
			if(meta >= extra) return new TileEntityProxyCombo(pos, state).fluid().power();
			return null;
		}

		@Override public int[] getDimensions() { return new int[] {2, 0, 1, 1, 2, 2}; }
		@Override public int getOffset() { return 1; }
		@Override protected int tankCount() { return 4; }

		@Override
		protected boolean checkRequirement(Level world, BlockPos core, BlockPos placed, Direction dir) {
			return super.checkRequirement(world, core, placed, dir) &&
					MultiblockHandlerXR.checkSpace(world, core, TOWER_A, placed, dir) &&
					MultiblockHandlerXR.checkSpace(world, core, TOWER_B, placed, dir);
		}

		@Override
		protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
			super.fillSpace(world, pos, dir, o);
			BlockPos core = pos.relative(dir, o);
			MultiblockHandlerXR.fillSpace(world, core, TOWER_A, this, dir);
			MultiblockHandlerXR.fillSpace(world, core, TOWER_B, this, dir);
			Direction rot = dir.getClockWise();
			this.makeExtra(world, core.relative(rot, 2));
			this.makeExtra(world, core.relative(rot, -2));
		}
	}
}
