package com.hbm.items.tool;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineSolarBoiler;
import com.hbm.items.ModDataComponents;
import com.hbm.tileentity.machine.TileEntitySolarMirror;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Remembers a solar boiler and turns heliostat mirrors towards it (within 100 blocks and at least 45 degrees up) */
public class ItemMirrorTool extends Item {

	public ItemMirrorTool(Properties properties) {
		super(properties.stacksTo(1));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {

		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = world.getBlockState(pos);
		ItemStack stack = context.getItemInHand();
		Player player = context.getPlayer();

		if(state.getBlock() instanceof MachineSolarBoiler boiler) {

			BlockPos core = boiler.findCore(world, pos);

			if(core != null && !world.isClientSide) {
				stack.set(ModDataComponents.LINKED_POS.get(), core.above());
				if(player != null) player.sendSystemMessage(Component.translatable("item.mirror_tool.linked").withStyle(ChatFormatting.YELLOW));
			}

			return InteractionResult.sidedSuccess(world.isClientSide);
		}

		BlockPos target = stack.get(ModDataComponents.LINKED_POS.get());

		if(state.is(ModBlocks.solar_mirror.get()) && target != null) {

			if(!world.isClientSide && world.getBlockEntity(pos) instanceof TileEntitySolarMirror mirror) {
				int dx = pos.getX() - target.getX();
				int dy = pos.getY() - target.getY();
				int dz = pos.getZ() - target.getZ();

				boolean withinReach = Math.sqrt(dx * dx + dy * dy + dz * dz) <= 100;
				boolean withinAngle = dx * dx + dz * dz <= dy * dy;

				if(!withinReach) {
					if(player != null) player.sendSystemMessage(Component.translatable("item.mirror_tool.reach").withStyle(ChatFormatting.RED));
				} else if(!withinAngle) {
					if(player != null) player.sendSystemMessage(Component.translatable("item.mirror_tool.angle").withStyle(ChatFormatting.RED));
				} else {
					mirror.setTarget(target.getX(), target.getY(), target.getZ());
				}
			}

			return InteractionResult.sidedSuccess(world.isClientSide);
		}

		return InteractionResult.PASS;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		for(String s : I18nUtil.resolveKeyArray("item.mirror_tool.desc"))
			list.add(Component.literal(s).withStyle(ChatFormatting.YELLOW));
	}
}
