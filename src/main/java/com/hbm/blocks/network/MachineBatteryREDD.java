package com.hbm.blocks.network;

import java.math.BigInteger;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.items.ModDataComponents;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.storage.TileEntityBatteryREDD;
import com.hbm.util.BobMathUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** REDD, 9x5x10: six ports at the corners and ends, the stored energy is kept on the item */
public class MachineBatteryREDD extends BlockDummyable {

	public MachineBatteryREDD(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity createNewTileEntity(BlockPos pos, BlockState state, int meta) {
		if(meta >= 12) return new TileEntityBatteryREDD(pos, state);
		if(meta >= extra) return new TileEntityProxyCombo(pos, state).power().conductor();
		return null;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		return this.standardOpenBehavior(world, pos, player);
	}

	@Override public int[] getDimensions() { return new int[] {9, 0, 2, 2, 4, 4}; }
	@Override public int getOffset() { return 2; }

	@Override
	protected void fillSpace(Level world, BlockPos pos, Direction dir, int o) {
		super.fillSpace(world, pos, dir, o);
		BlockPos core = pos.relative(dir, o);
		Direction rot = dir.getClockWise();

		this.makeExtra(world, core.relative(dir, 2).relative(rot, 2));
		this.makeExtra(world, core.relative(dir, 2).relative(rot, -2));
		this.makeExtra(world, core.relative(dir, -2).relative(rot, 2));
		this.makeExtra(world, core.relative(dir, -2).relative(rot, -2));
		this.makeExtra(world, core.relative(rot, 4));
		this.makeExtra(world, core.relative(rot, -4));
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		CustomData persistent = stack.get(ModDataComponents.PERSISTENT.get());
		if(persistent == null) return;
		byte[] bytes = persistent.copyTag().getByteArray("power");
		if(bytes.length > 0) list.add(Component.literal(BobMathUtil.format(new BigInteger(bytes)) + " HE").withStyle(ChatFormatting.YELLOW));
	}
}
