package com.hbm.inventory.container;

import com.hbm.inventory.ModMenus;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.inventory.recipes.anvil.AnvilSmithingRecipe;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * NTM anvil: two smithing slots with a result slot, construction recipes take their ingredients directly
 * from the player's inventory (AnvilCraftPacket). The menu has no block entity, the tier comes with the menu.
 */
public class ContainerAnvil extends AbstractContainerMenu {

	public SimpleContainer input = new SimpleContainer(8) {
		@Override
		public void setChanged() {
			super.setChanged();
			ContainerAnvil.this.updateSmithing();
		}
	};
	public Container output = new ResultContainer();
	public int tier; //because we can't trust these rascals with their packets

	public ContainerAnvil(int id, Inventory inventory, int tier) {
		super(ModMenus.ANVIL.get(), id);
		this.tier = tier;

		this.addSlot(new Slot(input, 0, 17, 27));
		this.addSlot(new Slot(input, 1, 53, 27));
		this.addSlot(new Slot(output, 0, 89, 27) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return false;
			}

			@Override
			public void onTake(Player player, ItemStack stack) {
				super.onTake(player, stack);

				ItemStack left = ContainerAnvil.this.input.getItem(0);
				ItemStack right = ContainerAnvil.this.input.getItem(1);

				if(left.isEmpty() || right.isEmpty()) {
					return;
				}

				for(AnvilSmithingRecipe rec : AnvilRecipes.getSmithing()) {
					int i = rec.matchesInt(left, right);
					if(i != -1) {
						ContainerAnvil.this.input.removeItem(0, rec.amountConsumed(0, i == 1));
						ContainerAnvil.this.input.removeItem(1, rec.amountConsumed(1, i == 1));
						ContainerAnvil.this.updateSmithing();
						return;
					}
				}
			}
		});

		for(int i = 0; i < 3; i++) {
			for(int j = 0; j < 9; j++) {
				this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + 56));
			}
		}

		for(int i = 0; i < 9; i++) {
			this.addSlot(new Slot(inventory, i, 8 + i * 18, 142 + 56));
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack rStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			rStack = stack.copy();

			if(index == 2) {
				if(!this.moveItemStackTo(stack, 3, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
				slot.onQuickCraft(stack, rStack);
			} else if(index <= 1) {
				if(!this.moveItemStackTo(stack, 3, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else {
				if(!this.moveItemStackTo(stack, 0, 2, false))
					return ItemStack.EMPTY;
			}

			if(stack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			slot.onTake(player, stack);
		}

		return rStack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		if(!player.level().isClientSide) {
			this.clearContainer(player, this.input);
		}
	}

	private void updateSmithing() {

		ItemStack left = this.input.getItem(0);
		ItemStack right = this.input.getItem(1);

		if(left.isEmpty() || right.isEmpty()) {
			this.output.setItem(0, ItemStack.EMPTY);
			return;
		}

		for(AnvilSmithingRecipe rec : AnvilRecipes.getSmithing()) {

			if(rec.matches(left, right) && rec.tier <= this.tier) {
				this.output.setItem(0, rec.getOutput(left, right));
				return;
			}
		}

		this.output.setItem(0, ItemStack.EMPTY);
	}
}
