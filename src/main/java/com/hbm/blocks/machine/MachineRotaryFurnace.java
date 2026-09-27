package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineRotaryFurnace;
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
 * Rotary furnace, 5x3x5: the back row takes the inputs and steam, the front the fuel and the recipe fluid, the metal
 * pours out of one side. The look overlay names the ports.
 */
public class MachineRotaryFurnace extends BlockDummyable implements ILookOverlay {

	public MachineRotaryFurnace(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineRotaryFurnace(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).inventory().fluid();
		return null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override
	public int[] getDimensions() {
		return new int[] {4, 0, 1, 1, 2, 2};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);
		Direction rot = dir.getCounterClockWise();

		//back
		for(int i = -2; i <= 2; i++) this.makeExtra(world, core.relative(dir, -1).relative(rot, i));
		//side fluid
		this.makeExtra(world, core.relative(dir).relative(rot, 2));
		//exhaust
		this.makeExtra(world, core.relative(rot).above(4));
		//solid fuel
		this.makeExtra(world, core.relative(dir).relative(rot));
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {

		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityMachineRotaryFurnace furnace)) return;

		Direction dir = getRotation(furnace.getBlockState());
		List<String> text = new ArrayList<>();

		//steam
		if(hitCheck(dir, core, -1, -1, pos) || hitCheck(dir, core, -1, -2, pos)) {
			text.add(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET + furnace.tanks[1].getTankType().getLocalizedName());
			text.add(ChatFormatting.RED + "<- " + ChatFormatting.RESET + furnace.tanks[2].getTankType().getLocalizedName());
		}

		//fluids
		if(hitCheck(dir, core, 1, 2, pos) || hitCheck(dir, core, -1, 2, pos)) {
			text.add(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET + furnace.tanks[0].getTankType().getLocalizedName());
		}

		if(hitCheck(dir, core, 1, 1, pos)) {
			text.add(ChatFormatting.YELLOW + "-> " + ChatFormatting.RESET + "Fuel");
		}

		if(!text.isEmpty()) {
			ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
		}
	}

	protected boolean hitCheck(Direction dir, BlockPos core, int exDir, int exRot, BlockPos hit) {
		return core.relative(dir, exDir).relative(dir.getCounterClockWise(), exRot).equals(hit);
	}
}
