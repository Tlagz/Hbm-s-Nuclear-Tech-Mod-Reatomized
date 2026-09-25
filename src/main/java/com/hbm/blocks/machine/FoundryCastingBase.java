package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.IToolable;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.machine.ItemMold;
import com.hbm.items.machine.ItemMold.Mold;
import com.hbm.items.machine.ItemScraps;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.machine.TileEntityFoundryBase;
import com.hbm.tileentity.machine.TileEntityFoundryCastingBase;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.block.ICrucibleAcceptor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.ItemAbilities;

/**
 * Molds and basins: take a mold item, get filled by pouring (or flowing, molds only), cast the mold's output.
 * Using it takes the cast item, a shovel empties the material as scraps, the screwdriver takes the empty mold out.
 */
public abstract class FoundryCastingBase extends Block implements EntityBlock, ICrucibleAcceptor, IToolable, ILookOverlay {

	protected FoundryCastingBase(Properties properties) {
		super(properties.noOcclusion());
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return world.isClientSide ? null : TileEntityLoadedBase.ticker();
	}

	/// ICrucibleAcceptor: the tile entity decides ///

	@Override
	public boolean canAcceptPartialPour(Level world, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
		return world.getBlockEntity(pos) instanceof TileEntityFoundryBase tile && tile.canAcceptPartialPour(world, pos, dX, dY, dZ, side, stack);
	}

	@Override
	public MaterialStack pour(Level world, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) {
		return world.getBlockEntity(pos) instanceof TileEntityFoundryBase tile ? tile.pour(world, pos, dX, dY, dZ, side, stack) : stack;
	}

	@Override
	public boolean canAcceptPartialFlow(Level world, BlockPos pos, Direction side, MaterialStack stack) {
		return world.getBlockEntity(pos) instanceof TileEntityFoundryBase tile && tile.canAcceptPartialFlow(world, pos, side, stack);
	}

	@Override
	public MaterialStack flow(Level world, BlockPos pos, Direction side, MaterialStack stack) {
		return world.getBlockEntity(pos) instanceof TileEntityFoundryBase tile ? tile.flow(world, pos, side, stack) : stack;
	}

	/** Gives the item to the player, drops it above the block if the inventory is full */
	protected static void give(Player player, ItemStack stack) {
		if(!player.getInventory().add(stack)) player.drop(stack, false);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if(!(world.getBlockEntity(pos) instanceof TileEntityFoundryCastingBase cast)) return InteractionResult.PASS;
		if(cast.slots.get(1).isEmpty()) return InteractionResult.PASS;
		if(!world.isClientSide) {
			give(player, cast.slots.get(1).copy());
			cast.slots.set(1, ItemStack.EMPTY);
			cast.sync();
		}
		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if(!(world.getBlockEntity(pos) instanceof TileEntityFoundryCastingBase cast)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		//remove casted item
		if(!cast.slots.get(1).isEmpty()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

		//insert mold
		Mold mold = ItemMold.getMold(held);
		if(mold != null && cast.slots.get(0).isEmpty() && mold.size == cast.getMoldSize()) {
			if(!world.isClientSide) {
				cast.slots.set(0, held.copyWithCount(1));
				if(!player.isCreative()) held.shrink(1);
				world.playSound(null, pos, ModSounds.get("item.upgradePlug"), SoundSource.BLOCKS, 1.0F, 1.0F);
				cast.sync();
			}
			return ItemInteractionResult.sidedSuccess(world.isClientSide);
		}

		//empty with a shovel
		if(held.canPerformAction(ItemAbilities.SHOVEL_DIG)) {
			if(!world.isClientSide && cast.amount > 0 && cast.type != null) {
				give(player, ItemScraps.create(new MaterialStack(cast.type, cast.amount)));
				cast.amount = 0;
				cast.type = null;
				cast.sync();
			}
			return ItemInteractionResult.sidedSuccess(world.isClientSide);
		}

		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	/** The contents drop as scraps, the mold and the cast item as items */
	@Override
	protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
		if(!state.is(newState.getBlock()) && world.getBlockEntity(pos) instanceof TileEntityFoundryCastingBase cast) {
			if(cast.amount > 0 && cast.type != null) {
				Containers.dropItemStack(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, ItemScraps.create(new MaterialStack(cast.type, cast.amount)));
				cast.amount = 0;
			}
			Containers.dropContents(world, pos, cast.slots);
		}
		super.onRemove(state, world, pos, newState, moved);
	}

	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
		if(world.getBlockEntity(pos) instanceof TileEntityFoundryCastingBase cast && cast.amount > 0 && cast.amount >= cast.getCapacity()) {
			double y = pos.getY() + getShape(state, world, pos, net.minecraft.world.phys.shapes.CollisionContext.empty()).max(Direction.Axis.Y);
			world.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.25 + rand.nextDouble() * 0.5, y, pos.getZ() + 0.25 + rand.nextDouble() * 0.5, 0.0, 0.0, 0.0);
		}
	}

	/** The screwdriver takes an empty mold out */
	@Override
	public boolean onScrew(Level world, Player player, BlockPos pos, Direction side, float fX, float fY, float fZ, ToolType tool) {
		if(tool != ToolType.SCREWDRIVER) return false;
		if(!(world.getBlockEntity(pos) instanceof TileEntityFoundryCastingBase cast)) return false;
		if(cast.slots.get(0).isEmpty() || cast.amount > 0) return false;
		if(!world.isClientSide) {
			give(player, cast.slots.get(0).copy());
			cast.slots.set(0, ItemStack.EMPTY);
			cast.sync();
		}
		return true;
	}

	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		if(!(world.getBlockEntity(pos) instanceof TileEntityFoundryCastingBase cast)) return;
		List<String> text = new ArrayList<>();

		Mold mold = ItemMold.getMold(cast.slots.get(0));
		if(mold == null) {
			text.add("&[" + ChatFormatting.RED.getColor() + "&]" + I18nUtil.resolveKey("foundry.noCast"));
		} else {
			text.add("&[" + ChatFormatting.BLUE.getColor() + "&]" + mold.getTitle());
		}

		if(cast.type != null && cast.amount > 0) {
			text.add("&[" + ChatFormatting.YELLOW.getColor() + "&]" + cast.type.getLocalizedName() + ": " + cast.amount + " / " + cast.getCapacity());
		}

		ILookOverlay.printGeneric(graphics, I18nUtil.resolveKey(this.getDescriptionId()), 0xFF4000, 0x401000, text);
	}
}
