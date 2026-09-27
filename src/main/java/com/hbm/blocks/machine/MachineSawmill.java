package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.items.ModItems;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntitySawmill;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Stirling sawmill, 3x3 and 2 tall, used by hand: right click with a log/plank/stick/sapling to insert it, right click
 * again to take the products out, right click with a sawblade to replace a lost blade.
 */
public class MachineSawmill extends BlockDummyable implements ILookOverlay, ITooltipProvider {

	public MachineSawmill(Properties properties) {
		super(properties);
		this.bounding.add(new AABB(-1.5D, 0D, -1.5D, 1.5D, 1D, 1.5D));
		this.bounding.add(new AABB(-1.25D, 1D, -0.5D, -0.625D, 1.875D, 0.5D));
		this.bounding.add(new AABB(-0.625D, 1D, -1D, 1.375D, 2D, 1D));
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntitySawmill(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).inventory();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {1, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);
		this.makeExtra(world, core.east());
		this.makeExtra(world, core.west());
		this.makeExtra(world, core.south());
		this.makeExtra(world, core.north());
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if(player.isShiftKeyDown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntitySawmill sawmill)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		if(world.isClientSide) return ItemInteractionResult.SUCCESS;

		// a new blade
		if(!sawmill.hasBlade && held.is(ModItems.sawblade.get())) {
			if(!player.isCreative()) held.shrink(1);
			sawmill.hasBlade = true;
			sawmill.setChanged();
			world.playSound(null, pos, ModSounds.get("item.upgradePlug"), SoundSource.BLOCKS, 1.5F, 0.75F);
			return ItemInteractionResult.SUCCESS;
		}

		// take the products out
		if(!sawmill.getItem(1).isEmpty() || !sawmill.getItem(2).isEmpty()) {
			for(int i = 1; i < 3; i++) {
				ItemStack out = sawmill.getItem(i);
				if(!out.isEmpty()) {
					if(!player.getInventory().add(out.copy())) player.drop(out.copy(), false);
					sawmill.setItem(i, ItemStack.EMPTY);
				}
			}
			sawmill.setChanged();
			return ItemInteractionResult.SUCCESS;
		}

		// put one item in
		if(sawmill.getItem(0).isEmpty() && !held.isEmpty() && !sawmill.getOutput(held).isEmpty()) {
			sawmill.setItem(0, held.copyWithCount(1));
			held.shrink(1);
			sawmill.setChanged();
			return ItemInteractionResult.SUCCESS;
		}

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		this.addStandardInfo(list);
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntitySawmill sawmill)) return;

		List<String> text = new ArrayList<>();
		text.add(sawmill.heat + "TU/t");

		double percent = (double) sawmill.heat / 300D;
		int color = ((int) (0xFF - 0xFF * Math.min(percent, 1D))) << 16 | ((int) (0xFF * Math.min(percent, 1D)) << 8);
		if(percent > 1D) color = 0xff0000;
		text.add("&[" + color + "&]" + ((sawmill.heat * 1000 / 300) / 10D) + "%");

		int limiter = sawmill.progress * 26 / TileEntitySawmill.processingTime;
		StringBuilder bar = new StringBuilder(ChatFormatting.GREEN + "[ ");
		for(int i = 0; i < 25; i++) {
			if(i == limiter) bar.append(ChatFormatting.RESET);
			bar.append("▏");
		}
		bar.append(ChatFormatting.GREEN + " ]");
		text.add(bar.toString());

		for(int i = 0; i < 3; i++) {
			ItemStack stack = sawmill.getItem(i);
			if(!stack.isEmpty()) {
				text.add((i == 0 ? (ChatFormatting.GREEN + "-> ") : (ChatFormatting.RED + "<- ")) + ChatFormatting.RESET + stack.getHoverName().getString() + (stack.getCount() > 1 ? " x" + stack.getCount() : ""));
			}
		}

		if(sawmill.heat > 300) {
			text.add("&[" + (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00) + "&]! ! ! OVERSPEED ! ! !");
		}

		if(!sawmill.hasBlade) {
			text.add("&[" + 0xff0000 + "&]Blade missing!");
		}

		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
	}
}
