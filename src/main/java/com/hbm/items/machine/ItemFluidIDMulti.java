package com.hbm.items.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.IItemControlReceiver;
import com.hbm.items.ISubItems;
import com.hbm.items.ModDataComponents;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

/**
 * Multi fluid identifier: sets the fluid of pipes and machines. Right click swaps primary and secondary type,
 * sneak right click opens the fluid selection screen (left click picks primary, right click secondary).
 */
public class ItemFluidIDMulti extends Item implements IItemFluidIdentifier, IItemControlReceiver, ISubItems {

	public ItemFluidIDMulti(Properties properties) {
		super(properties);
	}

	@Override
	public List<ItemStack> getSubItems() {
		List<ItemStack> list = new ArrayList<>();
		FluidType[] order = Fluids.getInNiceOrder();
		for(int i = 1; i < order.length; ++i) {
			if(!order[i].hasNoID()) {
				ItemStack id = new ItemStack(this);
				setType(id, order[i], true);
				list.add(id);
			}
		}
		return list;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if(!world.isClientSide && !player.isShiftKeyDown()) {
			FluidType primary = getType(stack, true);
			FluidType secondary = getType(stack, false);
			setType(stack, secondary, true);
			setType(stack, primary, false);
			world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.25F, 1.25F);
			// the original used PlayerInformPacket, the action bar does the same job
			player.displayClientMessage(ItemFluidContainerBase.fluidName(secondary), true);
		}

		if(world.isClientSide && player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
			com.hbm.main.ClientHooks.openFluidIdentifierScreen(player);
		}

		return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
	}

	@Override
	public void receiveControl(Player player, ItemStack stack, CompoundTag data) {
		if(data.contains("primary")) {
			setType(stack, Fluids.fromID(data.getInt("primary")), true);
		}
		if(data.contains("secondary")) {
			setType(stack, Fluids.fromID(data.getInt("secondary")), false);
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		list.add(Component.translatable("item.fluid_identifier_multi.info"));
		list.add(Component.literal("   ").append(ItemFluidContainerBase.fluidName(getType(stack, true))));
		list.add(Component.translatable("item.fluid_identifier_multi.info2"));
		list.add(Component.literal("   ").append(ItemFluidContainerBase.fluidName(getType(stack, false))));
	}

	/** Stays in the crafting grid */
	@Override
	public ItemStack getCraftingRemainingItem(ItemStack stack) {
		return stack.copyWithCount(1);
	}

	@Override
	public boolean hasCraftingRemainingItem(ItemStack stack) {
		return true;
	}

	@Override
	public FluidType getType(Level world, BlockPos pos, ItemStack stack) {
		return getType(stack, true);
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
		return true;
	}

	public static int getColor(ItemStack stack, int tintIndex) {
		if(tintIndex == 0) return 0xFFFFFF;
		int j = getType(stack, true).getColor();
		return j < 0 ? 0xFFFFFF : j;
	}

	/** The primary type doubles as FLUID_TYPE (the original mirrored it into the damage value with updateMeta) */
	public static void setType(ItemStack stack, FluidType type, boolean primary) {
		stack.set(primary ? ModDataComponents.FLUID_TYPE.get() : ModDataComponents.FLUID_TYPE_SECONDARY.get(), type.getID());
	}

	public static FluidType getType(ItemStack stack, boolean primary) {
		Integer id = stack.get(primary ? ModDataComponents.FLUID_TYPE.get() : ModDataComponents.FLUID_TYPE_SECONDARY.get());
		return id == null ? Fluids.NONE : Fluids.fromID(id);
	}
}
