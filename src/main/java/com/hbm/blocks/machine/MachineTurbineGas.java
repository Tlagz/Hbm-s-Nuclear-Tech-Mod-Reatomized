package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineTurbineGas;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Gas turbine, 3x10x3: fuel and lube ports on the front end, water on the back end, power out of one side and hot
 * steam out of the other. The look overlay names the ports.
 */
public class MachineTurbineGas extends BlockDummyable implements ILookOverlay {

	public MachineTurbineGas(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineTurbineGas(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).power().fluid();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 0, 1, 1, 4, 5};
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);
		Direction rot = dir.getClockWise();

		this.makeExtra(world, core.relative(dir, -1).relative(rot));
		this.makeExtra(world, core.relative(dir, 1).relative(rot));
		this.makeExtra(world, core.relative(dir, -1).relative(rot, -4));
		this.makeExtra(world, core.relative(dir, 1).relative(rot, -4));
		this.makeExtra(world, core.relative(rot, 4).above());
		this.makeExtra(world, core.relative(rot, -5).above());
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {

		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityMachineTurbineGas turbine)) return;

		Direction dir = getRotation(turbine.getBlockState());
		List<String> text = new ArrayList<>();

		if(hitCheck(dir, core, -1, -1, 0, pos) || hitCheck(dir, core, 1, -1, 0, pos)) {
			text.add(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET + turbine.tanks[0].getTankType().getLocalizedName());
			text.add(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET + turbine.tanks[1].getTankType().getLocalizedName());
		}

		if(hitCheck(dir, core, -1, 4, 0, pos) || hitCheck(dir, core, 1, 4, 0, pos)) {
			text.add(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET + turbine.tanks[2].getTankType().getLocalizedName());
		}

		if(hitCheck(dir, core, 0, 5, 1, pos)) {
			text.add(ChatFormatting.RED + "<- " + ChatFormatting.RESET + turbine.tanks[3].getTankType().getLocalizedName());
		}

		if(hitCheck(dir, core, 0, -4, 1, pos)) {
			text.add(ChatFormatting.RED + "<- " + ChatFormatting.RESET + "Power");
		}

		if(!text.isEmpty()) {
			ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
		}
	}

	/** Whether the looked at block is the one at exDir along the facing and exRot counter-clockwise from the core */
	protected boolean hitCheck(Direction dir, BlockPos core, int exDir, int exRot, int exY, BlockPos hit) {
		Direction turn = dir.getCounterClockWise();
		return core.relative(dir, exDir).relative(turn, exRot).above(exY).equals(hit);
	}
}
