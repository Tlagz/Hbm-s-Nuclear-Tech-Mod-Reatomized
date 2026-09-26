package com.hbm.blocks.network;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.storage.TileEntityBatterySocket;
import com.hbm.util.BobMathUtil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Battery socket, 2x2x2: a battery item connected to the grid, every block acts as a cable */
public class MachineBatterySocket extends BlockDummyable implements ITooltipProvider, ILookOverlay {

	public MachineBatterySocket(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityBatterySocket(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).inventory().power().conductor();
		return null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override public int[] getDimensions() { return new int[] {1, 0, 1, 0, 1, 0}; }
	@Override public int getOffset() { return 0; }

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		Direction rot = dir.getClockWise();
		this.makeExtra(world, pos.relative(dir, -1));
		this.makeExtra(world, pos.relative(rot));
		this.makeExtra(world, pos.relative(dir, -1).relative(rot));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		this.addStandardInfo(list);
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
		if(getMeta(state) < extra) return 0;
		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityBatterySocket battery)) return 0;
		return battery.getComparatorPower();
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {

		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityBatterySocket socket)) return;
		if(socket.syncStack.isEmpty()) return;

		List<String> text = new ArrayList<>();
		text.add(BobMathUtil.getShortNumber(socket.syncPower) + " / " + BobMathUtil.getShortNumber(socket.syncMaxPower) + "HE");

		double percent = (double) socket.syncPower / Math.max(socket.syncMaxPower, 1);
		int charge = (int) Math.floor(percent * 10_000D);
		int color = ((int) (0xFF - 0xFF * percent)) << 16 | ((int) (0xFF * percent) << 8);

		text.add("&[" + color + "&]" + (charge / 100D) + "%");

		ILookOverlay.printGeneric(graphics, socket.syncStack.getHoverName().getString(), 0xffff00, 0x404000, text);
	}
}
