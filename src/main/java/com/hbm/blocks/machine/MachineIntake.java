package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineIntake;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Air intake, 2x2 and one block tall, every block is a power/air port */
public class MachineIntake extends BlockDummyable implements ILookOverlay {

	public MachineIntake(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineIntake(pos, state);
		return new TileEntityProxyCombo(pos, state).power().fluid();
	}

	@Override public int[] getDimensions() { return new int[] {0, 0, 1, 0, 1, 0}; }
	@Override public int getOffset() { return 0; }

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityMachineIntake intake)) return;

		List<String> text = new ArrayList<>();
		text.add((intake.power < intake.getMaxPower() / 20 ? ChatFormatting.RED : ChatFormatting.GREEN) + "Power: " + BobMathUtil.getShortNumber(intake.power) + "HE");
		text.add(ChatFormatting.RED + "<- " + ChatFormatting.RESET + intake.compair.getTankType().getLocalizedName() + ": " + intake.compair.getFill() + "/" + intake.compair.getMaxFill() + "mB");
		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
	}
}
