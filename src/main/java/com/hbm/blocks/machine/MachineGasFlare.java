package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.oil.TileEntityMachineGasFlare;

import net.minecraft.ChatFormatting;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/** Flare stack: a 3x3 base with a thin chimney, 12 tall */
public class MachineGasFlare extends BlockDummyable {

	public MachineGasFlare(Properties properties) {
		super(properties);
		this.bounding.add(new AABB(-1.5D, 0D, -1.5D, 1.5D, 3.875D, 1.5D));
		this.bounding.add(new AABB(-0.75D, 3.875D, -0.75D, 0.75D, 9, 0.75D));
		this.bounding.add(new AABB(-1.5D, 9D, -1.5D, 1.5D, 9.375D, 1.5D));
		this.bounding.add(new AABB(-0.75D, 9.375D, -0.75D, 0.75D, 12, 0.75D));
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineGasFlare(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).power().fluid();
		return null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override
	public int[] getDimensions() {
		return new int[] {11, 0, 1, 1, 1, 1};
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
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		list.add(Component.literal("Can burn fluids and vent gasses").withStyle(ChatFormatting.GOLD));
		list.add(Component.literal("Burns up to ").withStyle(ChatFormatting.GOLD).append(Component.literal("10mB/t").withStyle(ChatFormatting.RED)));
		list.add(Component.literal("Vents up to ").withStyle(ChatFormatting.GOLD).append(Component.literal("50mB/t").withStyle(ChatFormatting.RED)));
		list.add(Component.empty());
		list.add(Component.literal("Fuel efficiency:").withStyle(ChatFormatting.YELLOW));
		list.add(Component.literal("-Flammable Gasses: ").withStyle(ChatFormatting.YELLOW).append(Component.literal("20%").withStyle(ChatFormatting.RED)));
		list.add(Component.literal("-Flammable Liquids: ").withStyle(ChatFormatting.YELLOW).append(Component.literal("10%").withStyle(ChatFormatting.RED)));
	}
}
