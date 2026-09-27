package com.hbm.tileentity.machine;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.container.ContainerFunnel;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Combinator funnel: compresses items with their 3x3 (and/or 2x2) crafting recipe, e.g. ingots into blocks.
 * Slots: 0-8 input (from the top and sides), 9-17 output (from below).
 *
 * TODO clear the caches when the recipes are reloaded
 */
public class TileEntityMachineFunnel extends TileEntityMachineBase implements IControlReceiver, MenuProvider {

	public int mode = 0;
	public static final int MODE_ALL = 0;
	public static final int MODE_3x3 = 1;
	public static final int MODE_2x2 = 2;

	/** Crafting lookups are slow, the results (or no result, as EMPTY) are remembered per item */
	public static final HashMap<ComparableStack, ItemStack> from4Cache = new HashMap<>();
	public static final HashMap<ComparableStack, ItemStack> from9Cache = new HashMap<>();

	public TileEntityMachineFunnel(BlockPos pos, BlockState state) {
		super(ModTileEntities.FUNNEL.get(), pos, state, 18);
	}

	@Override
	public String getName() {
		return "container.machineFunnel";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			for(int i = 0; i < 9; i++) {

				ItemStack in = slots.get(i);
				if(in.isEmpty()) continue;

				int stacksize = 9;
				ItemStack compressed = (mode == MODE_2x2 || in.getCount() < 9) ? ItemStack.EMPTY : this.getFrom9(in);

				if(compressed.isEmpty()) {
					compressed = (mode == MODE_3x3 || in.getCount() < 4) ? ItemStack.EMPTY : this.getFrom4(in);
					stacksize = 4;
				}

				if(!compressed.isEmpty() && in.getCount() >= stacksize) {
					ItemStack out = slots.get(i + 9);

					if(out.isEmpty()) {
						slots.set(i + 9, compressed.copy());
						this.removeItem(i, stacksize);
					} else if(ItemStack.isSameItemSameComponents(out, compressed) && out.getCount() + compressed.getCount() <= compressed.getMaxStackSize()) {
						out.grow(compressed.getCount());
						this.removeItem(i, stacksize);
					}
				}
			}

			this.networkPackNT(15);
		}
	}

	public ItemStack getFrom4(ItemStack ingredient) {
		ComparableStack singular = new ComparableStack(ingredient).makeSingular();
		ItemStack cached = from4Cache.get(singular);
		if(cached != null) return cached;

		ItemStack one = ingredient.copyWithCount(1);
		ItemStack match = getMatch(CraftingInput.of(2, 2, List.of(one, one, one, one)));
		from4Cache.put(singular, match.copy());
		return match;
	}

	public ItemStack getFrom9(ItemStack ingredient) {
		ComparableStack singular = new ComparableStack(ingredient).makeSingular();
		ItemStack cached = from9Cache.get(singular);
		if(cached != null) return cached;

		ItemStack match = getMatch(CraftingInput.of(3, 3, Collections.nCopies(9, ingredient.copyWithCount(1))));
		from9Cache.put(singular, match.copy());
		return match;
	}

	public ItemStack getMatch(CraftingInput grid) {
		if(level == null) return ItemStack.EMPTY;
		return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level).map(r -> r.value().assemble(grid, level.registryAccess())).orElse(ItemStack.EMPTY);
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(this.mode);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.mode = buf.readInt();
	}

	private static final int[] topAccess = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
	private static final int[] bottomAccess = new int[] { 9, 10, 11, 12, 13, 14, 15, 16, 17 };

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return side == Direction.DOWN ? bottomAccess : topAccess;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack stack, Direction side) {
		if(side == Direction.DOWN) return i > 8;
		return side != Direction.UP && i < 9;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot > 8) return false;
		if(!slots.get(slot).isEmpty()) return true; // same type merging skips the validity check anyway
		return !this.getFrom9(stack).isEmpty() || !this.getFrom4(stack).isEmpty();
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.mode = nbt.getInt("mode");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putInt("mode", mode);
	}

	@Override public boolean hasPermission(Player player) { return this.stillValid(player); }

	/** The mode button cycles all, 3x3 only, 2x2 only */
	@Override
	public void receiveControl(CompoundTag data) {
		this.mode++;
		if(mode > 2) mode = 0;
		this.setChanged();
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerFunnel(id, inv, this);
	}
}
