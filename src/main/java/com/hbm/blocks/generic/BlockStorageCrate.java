package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ILookOverlay;
import com.hbm.tileentity.machine.storage.TileEntityCrate;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Storage crates and the safe. The contents stay in the item when broken (the original's CRATE_KEEP_CONTENTS,
 * on by default) through the container component copied by the loot table; the safe faces the player.
 *
 * TODO locks and keys, opening crates from the inventory (ItemBlockStorageCrate), spiders, the tungsten crate's
 * laser smelting, the 6kB NBT limit warning
 */
public class BlockStorageCrate extends Block implements EntityBlock, ILookOverlay {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	/** Slot layout and GUI of every crate */
	public enum CrateType {
		//		slots	rows	cols	slotX	invX	invY	width	height	texture
		IRON(	36,		4,		9,		8,		8,		104,	176,	186,	"gui_crate_iron",		"container.crateIron"),
		STEEL(	54,		6,		9,		8,		8,		140,	176,	222,	"gui_crate_steel",		"container.crateSteel"),
		DESH(	104,	8,		13,		8,		44,		174,	248,	256,	"gui_crate_desh",		"container.crateDesh"),
		TUNGSTEN(27,	3,		9,		8,		8,		86,		176,	168,	"gui_crate_tungsten",	"container.crateTungsten"),
		SAFE(	15,		3,		5,		44,		8,		86,		176,	168,	"gui_safe",				"container.safe");

		public final int slots, rows, cols, slotX, invX, invY, width, height;
		public final String texture, name;

		CrateType(int slots, int rows, int cols, int slotX, int invX, int invY, int width, int height, String texture, String name) {
			this.slots = slots;
			this.rows = rows;
			this.cols = cols;
			this.slotX = slotX;
			this.invX = invX;
			this.invY = invY;
			this.width = width;
			this.height = height;
			this.texture = texture;
			this.name = name;
		}
	}

	public final CrateType type;

	public BlockStorageCrate(Properties properties, CrateType type) {
		super(properties);
		this.type = type;
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityCrate(pos, state);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if(player.isShiftKeyDown()) return InteractionResult.PASS;

		if(!world.isClientSide && player instanceof ServerPlayer serverPlayer && world.getBlockEntity(pos) instanceof TileEntityCrate crate) {
			serverPlayer.openMenu(crate, buf -> buf.writeBlockPos(pos));
		}
		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	@Override
	protected boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos));
	}

	/** Lists up to 10 stacks of the stored contents */
	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
		if(contents == null) return;

		List<Component> lines = new ArrayList<>();
		int amount = 0;

		for(ItemStack content : contents.nonEmptyItems()) {
			amount++;
			if(lines.size() < 10) {
				lines.add(Component.literal(" - ").append(content.getHoverName()).append(content.getCount() > 1 ? " x" + content.getCount() : "").withStyle(ChatFormatting.AQUA));
			}
		}

		if(!lines.isEmpty()) {
			list.add(Component.literal("Contains:").withStyle(ChatFormatting.AQUA));
			list.addAll(lines);
			amount -= lines.size();
			if(amount > 0) list.add(Component.literal("...and " + amount + " more.").withStyle(ChatFormatting.AQUA));
		}
	}

	/** Named crates show their name when looked at */
	@Override
	public void printHook(GuiGraphics graphics, Level world, BlockPos pos) {
		if(!(world.getBlockEntity(pos) instanceof TileEntityCrate crate) || !crate.hasCustomInventoryName()) return;
		ILookOverlay.printGeneric(graphics, crate.getInventoryName(), 0xffff00, 0x404000, new ArrayList<>(0));
	}
}
