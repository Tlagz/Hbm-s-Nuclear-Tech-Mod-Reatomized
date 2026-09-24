package com.hbm.items.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.inventory.recipes.loader.GenericRecipes;
import com.hbm.items.ISubItems;
import com.hbm.items.ModDataComponents;
import com.hbm.items.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Blueprints unlock the pooled recipes of generic recipe machines (alternate, discoverable and secret recipes),
 * the pool name is stored in the stack. Right click with paper in the inventory copies it.
 * The texture depends on the pool's prefix, see the "hbm:pool" item property.
 */
public class ItemBlueprints extends Item implements ISubItems {

	public ItemBlueprints(Properties properties) {
		super(properties);
	}

	/** Item property for the model: 0 regular (blue), 1 discover (beige), 2 secret (black), 3 528 (grey) */
	public static float poolType(ItemStack stack) {
		String poolName = stack.get(ModDataComponents.BLUEPRINT_POOL.get());
		if(poolName == null) return 0;
		if(poolName.startsWith(GenericRecipes.POOL_PREFIX_DISCOVER)) return 1;
		if(poolName.startsWith(GenericRecipes.POOL_PREFIX_SECRET)) return 2;
		if(poolName.startsWith(GenericRecipes.POOL_PREFIX_528)) return 3;
		return 0;
	}

	@Override
	public List<ItemStack> getSubItems() {
		List<ItemStack> list = new ArrayList<>();
		for(Entry<String, List<String>> pool : GenericRecipes.blueprintPools.entrySet()) {
			String poolName = pool.getKey();
			if(!poolName.startsWith(GenericRecipes.POOL_PREFIX_SECRET)) list.add(make(poolName));
		}
		return list;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if(world.isClientSide) return InteractionResultHolder.pass(stack);

		String poolName = stack.get(ModDataComponents.BLUEPRINT_POOL.get());
		if(poolName == null) return InteractionResultHolder.pass(stack);

		if(poolName.startsWith(GenericRecipes.POOL_PREFIX_SECRET)) return InteractionResultHolder.pass(stack);

		int paper = player.getInventory().findSlotMatchingItem(new ItemStack(Items.PAPER));
		if(paper < 0) return InteractionResultHolder.pass(stack);

		player.getInventory().removeItem(paper, 1);
		player.swing(hand, true);

		ItemStack copy = stack.copyWithCount(1);

		if(!player.getAbilities().instabuild) {
			if(stack.getCount() < stack.getMaxStackSize()) {
				stack.grow(1);
				return InteractionResultHolder.success(stack);
			}

			if(!player.getInventory().add(copy)) {
				player.drop(copy, false);
			}

			player.inventoryMenu.broadcastChanges();
		} else {
			player.drop(copy, false);
		}

		return InteractionResultHolder.success(stack);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {

		String poolName = stack.get(ModDataComponents.BLUEPRINT_POOL.get());
		if(poolName == null) return;

		List<String> pool = GenericRecipes.blueprintPools.get(poolName);
		if(pool == null || pool.isEmpty()) return;

		if(poolName.startsWith(GenericRecipes.POOL_PREFIX_SECRET)) {
			list.add(Component.literal("Cannot be copied!").withStyle(ChatFormatting.RED));
		} else {
			list.add(Component.literal("Right-click to copy (requires paper)").withStyle(ChatFormatting.YELLOW));
		}

		for(String name : pool) {
			GenericRecipe recipe = GenericRecipes.nameToRecipeGlobal.get(name);
			if(recipe != null) {
				list.add(Component.literal(recipe.getLocalizedName()));
			}
		}
	}

	public static String grabPool(ItemStack stack) {
		if(stack == null || !stack.is(ModItems.blueprints.get())) return null;
		return stack.get(ModDataComponents.BLUEPRINT_POOL.get());
	}

	public static ItemStack make(String pool) {
		ItemStack stack = new ItemStack(ModItems.blueprints.get());
		stack.set(ModDataComponents.BLUEPRINT_POOL.get(), pool);
		return stack;
	}
}
