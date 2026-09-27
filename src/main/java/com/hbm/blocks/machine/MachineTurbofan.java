package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.fluid.trait.FT_Combustible.FuelGrade;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineTurbofan;

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
import net.minecraft.world.phys.BlockHitResult;

/** Turbofan: 3 wide, 3 high and 7 long, fuel/power ports on both sides */
public class MachineTurbofan extends BlockDummyable {

	public MachineTurbofan(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityMachineTurbofan(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).fluid().power();
		return null;
	}

	@Override public int[] getDimensions() { return new int[] {2, 0, 1, 1, 3, 3}; }
	@Override public int getOffset() { return 1; }

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);

		Direction rot = dir.getClockWise();

		this.makeExtra(world, pos);
		this.makeExtra(world, pos.relative(rot, -1));
		this.makeExtra(world, pos.relative(dir, -2));
		this.makeExtra(world, pos.relative(dir, -2).relative(rot, -1));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		list.add(Component.literal("Fuel efficiency:").withStyle(ChatFormatting.YELLOW));
		list.add(Component.literal("-" + FuelGrade.AERO.getLocalizedName() + ": ").withStyle(ChatFormatting.YELLOW).append(Component.literal("100%").withStyle(ChatFormatting.RED)));
	}
}
