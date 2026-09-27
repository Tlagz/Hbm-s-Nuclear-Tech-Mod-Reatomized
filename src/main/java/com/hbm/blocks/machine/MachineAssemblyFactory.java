package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineAssemblyFactory;
import com.hbm.util.DirPos;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
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

/** Assembly factory, 5x5 and 3 tall. The bottom ring and the two rails on top are ports. */
public class MachineAssemblyFactory extends BlockDummyable implements ITooltipProvider, ILookOverlay {

	public MachineAssemblyFactory(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineAssemblyFactory(pos, state);
		if(meta >= 6) return new TileEntityProxyCombo(pos, state).inventory().power().fluid();
		return null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override public int[] getDimensions() { return new int[] {2, 0, 2, 2, 2, 2}; }
	@Override public int getOffset() { return 2; }

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);

		BlockPos core = pos.relative(dir, o);
		for(int i = -2; i <= 2; i++) for(int j = -2; j <= 2; j++) {
			if(Math.abs(i) == 2 || Math.abs(j) == 2) this.makeExtra(world, core.offset(i, 0, j));
		}

		Direction rot = dir.getClockWise();
		for(int i = -2; i <= 2; i++) {
			this.makeExtra(world, core.above(2).relative(dir, i).relative(rot, 2));
			this.makeExtra(world, core.above(2).relative(dir, i).relative(rot, -2));
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		this.addStandardInfo(list);
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityMachineAssemblyFactory assemfac)) return;

		for(DirPos port : assemfac.getCoolPos()) if(port.equals(pos.relative(port.getDir()))) {
			List<String> text = new ArrayList<>();
			text.add(ChatFormatting.GREEN + "-> " + ChatFormatting.RESET + assemfac.water.getTankType().getLocalizedName());
			text.add(ChatFormatting.RED + "<- " + ChatFormatting.RESET + assemfac.lps.getTankType().getLocalizedName());
			ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
			return;
		}

		DirPos[] io = assemfac.getIOPos();
		for(int i = 0; i < io.length; i++) if(io[i].equals(pos.relative(io[i].getDir()))) {
			List<String> text = new ArrayList<>();
			text.add(ChatFormatting.YELLOW + "-> " + ChatFormatting.RESET + "Recipe field [" + (i + 1) + "]");
			ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
			return;
		}
	}
}
