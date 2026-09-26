package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.items.ModItems;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityStirling;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Stirling engine on top of a heater, power comes out of the four sides. The type decides the gear and the heat it
 * takes before overspeeding: normal (iron gear), steel (steel gear), creative (never breaks). A lost gear is put back
 * by using the matching large gear on it.
 */
public class MachineStirling extends BlockDummyable implements ILookOverlay {

	/** 0 normal, 1 steel, 2 creative */
	public final int type;

	public MachineStirling(Properties properties, int type) {
		super(properties);
		this.type = type;
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityStirling(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).power();
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

	public ItemStack gearStack() {
		return new ItemStack(type == 1 ? ModItems.gear_large_steel.get() : ModItems.gear_large.get());
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if(player.isShiftKeyDown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityStirling stirling)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		if(!stirling.hasCog && ItemStack.isSameItem(held, gearStack())) {
			if(!world.isClientSide) {
				if(!player.isCreative()) held.shrink(1);
				stirling.hasCog = true;
				stirling.setChanged();
				world.playSound(null, pos, ModSounds.get("item.upgradePlug"), SoundSource.BLOCKS, 1.5F, 0.75F);
			}
			return ItemInteractionResult.sidedSuccess(world.isClientSide);
		}

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		if(core == null || !(world.getBlockEntity(core) instanceof TileEntityStirling stirling)) return;

		List<String> text = new ArrayList<>();
		text.add(stirling.heat + "TU/t");
		text.add((stirling.hasCog ? stirling.powerBuffer : 0) + "HE/t");

		if(type != 2) {
			int maxHeat = stirling.maxHeat();
			double percent = (double) stirling.heat / (double) maxHeat;
			int color = ((int) (0xFF - 0xFF * Math.min(percent, 1D))) << 16 | ((int) (0xFF * Math.min(percent, 1D)) << 8);

			if(percent > 1D)
				color = 0xff0000;

			text.add("&[" + color + "&]" + ((stirling.heat * 1000 / maxHeat) / 10D) + "%");

			if(stirling.heat > maxHeat) {
				text.add("&[" + (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00) + "&]! ! ! OVERSPEED ! ! !");
			}

			if(!stirling.hasCog) {
				text.add("&[" + 0xff0000 + "&]Gear missing!");
			}
		}

		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xffff00, 0x404000, text);
	}
}
