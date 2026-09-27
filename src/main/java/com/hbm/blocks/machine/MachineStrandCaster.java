package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.IToolable;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.machine.ItemMold;
import com.hbm.items.machine.ItemMold.Mold;
import com.hbm.items.machine.ItemScraps;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineStrandCaster;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.block.ICrucibleAcceptor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.ItemAbilities;

/**
 * Strand caster, a 2x7 casting line with a 2x2x3 funnel at the front. Metal poured into the funnel's top goes to
 * the core. Right click with a mold installs it, a shovel empties the metal as scraps, the screwdriver takes the
 * mold out.
 */
public class MachineStrandCaster extends BlockDummyable implements ICrucibleAcceptor, ILookOverlay, IToolable {

	private static final int[] FUNNEL = new int[] {2, 0, 1, 0, 1, 0};

	public MachineStrandCaster(Properties properties) {
		super(properties);
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 6, 0, 1, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineStrandCaster(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).inventory().fluid();
		return null;
	}

	@Override
	protected boolean checkRequirement(Level world, BlockPos core, BlockPos placed, Direction dir) {
		return super.checkRequirement(world, core, placed, dir) && MultiblockHandlerXR.checkSpace(world, core, FUNNEL, placed, dir);
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);
		Direction rot = dir.getClockWise();

		MultiblockHandlerXR.fillSpace(world, core, FUNNEL, this, dir);

		// Fluid ports
		this.makeExtra(world, core.relative(rot).relative(dir, -1));
		this.makeExtra(world, core.relative(dir, -1));
		this.makeExtra(world, core.relative(dir, -5));
		this.makeExtra(world, core.relative(rot).relative(dir, -5));
		// Molten metal ports
		this.makeExtra(world, core.relative(rot).relative(dir, -1).above(2));
		this.makeExtra(world, core.relative(dir, -1).above(2));
		this.makeExtra(world, core.relative(rot).above(2));
		this.makeExtra(world, core.above(2));
	}

	private TileEntityMachineStrandCaster caster(Level world, BlockPos pos) {
		BlockPos core = this.findCore(world, pos);
		if(core == null) return null;
		return world.getBlockEntity(core) instanceof TileEntityMachineStrandCaster caster ? caster : null;
	}

	@Override
	public boolean canAcceptPartialPour(Level world, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
		TileEntityMachineStrandCaster caster = caster(world, pos);
		return caster != null && caster.canAcceptPartialPour(pos, side, stack);
	}

	@Override
	public MaterialStack pour(Level world, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
		TileEntityMachineStrandCaster caster = caster(world, pos);
		if(caster == null || !caster.canAcceptPartialPour(pos, side, stack)) return stack;
		return caster.standardAdd(world, stack);
	}

	@Override
	public boolean canAcceptPartialFlow(Level world, BlockPos pos, Direction side, MaterialStack stack) {
		return false;
	}

	@Override
	public MaterialStack flow(Level world, BlockPos pos, Direction side, MaterialStack stack) {
		return stack;
	}

	private static void give(Player player, ItemStack stack) {
		if(!player.getInventory().add(stack)) player.drop(stack, false);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

		TileEntityMachineStrandCaster cast = caster(world, pos);
		if(cast == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		// insert mold
		if(ItemMold.getMold(held) != null && cast.getItem(0).isEmpty()) {
			if(!world.isClientSide) {
				cast.setItem(0, held.copyWithCount(1));
				if(!player.isCreative()) held.shrink(1);
				world.playSound(null, pos, ModSounds.get("item.upgradePlug"), SoundSource.BLOCKS, 1.0F, 1.0F);
				cast.setChanged();
			}
			return ItemInteractionResult.sidedSuccess(world.isClientSide);
		}

		// empty with a shovel
		if(held.canPerformAction(ItemAbilities.SHOVEL_DIG)) {
			if(!world.isClientSide && cast.amount > 0 && cast.type != null) {
				give(player, ItemScraps.create(new MaterialStack(cast.type, cast.amount)));
				cast.amount = 0;
				cast.type = null;
				cast.setChanged();
			}
			return ItemInteractionResult.sidedSuccess(world.isClientSide);
		}

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	/** The molten contents drop as scraps */
	@Override
	protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
		if(!state.is(newState.getBlock()) && world.getBlockEntity(pos) instanceof TileEntityMachineStrandCaster cast && cast.amount > 0 && cast.type != null) {
			Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, ItemScraps.create(new MaterialStack(cast.type, cast.amount)));
			cast.amount = 0;
		}
		super.onRemove(state, world, pos, newState, moved);
	}

	/** The screwdriver takes the mold out */
	@Override
	public boolean onScrew(Level world, Player player, BlockPos pos, Direction side, float fX, float fY, float fZ, ToolType tool) {
		if(tool != ToolType.SCREWDRIVER) return false;
		TileEntityMachineStrandCaster cast = caster(world, pos);
		if(cast == null || cast.getItem(0).isEmpty()) return false;

		if(!world.isClientSide) {
			give(player, cast.getItem(0).copy());
			cast.setItem(0, ItemStack.EMPTY);
			cast.setChanged();
		}
		return true;
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		TileEntityMachineStrandCaster cast = caster(world, pos);
		if(cast == null) return;

		List<String> text = new ArrayList<>();
		Mold mold = cast.getInstalledMold();
		if(mold == null) {
			text.add("&[" + ChatFormatting.RED.getColor() + "&]" + I18nUtil.resolveKey("foundry.noCast"));
		} else {
			text.add("&[" + ChatFormatting.BLUE.getColor() + "&]" + mold.getTitle());
		}

		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(getDescriptionId()), 0xFF4000, 0x401000, text);
	}
}
