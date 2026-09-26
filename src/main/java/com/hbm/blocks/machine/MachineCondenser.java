package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ILookOverlay;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.machine.TileEntityCondenser;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Steam condenser, a plain block */
public class MachineCondenser extends Block implements EntityBlock, ILookOverlay {

	public MachineCondenser(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityCondenser(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return world.isClientSide ? null : TileEntityLoadedBase.ticker();
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		if(!(world.getBlockEntity(pos) instanceof TileEntityCondenser condenser)) return;

		List<String> text = new ArrayList<>();
		for(int i = 0; i < condenser.tanks.length; i++)
			text.add((i < 1 ? (ChatFormatting.GREEN + "-> ") : (ChatFormatting.RED + "<- ")) + ChatFormatting.RESET + condenser.tanks[i].getTankType().getLocalizedName() + ": " + condenser.tanks[i].getFill() + "/" + condenser.tanks[i].getMaxFill() + "mB");

		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
	}
}
