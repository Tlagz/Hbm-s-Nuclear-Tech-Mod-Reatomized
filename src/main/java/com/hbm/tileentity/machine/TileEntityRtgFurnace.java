package com.hbm.tileentity.machine;

import java.util.Optional;

import com.hbm.blocks.machine.MachineRtgFurnace;
import com.hbm.inventory.container.ContainerRtgFurnace;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.RTGUtil;

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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.state.BlockState;

/**
 * RTG furnace: a furnace heated by up to three RTG pellets, progress goes up by their heat every tick (1000 per item).
 * Slots: 0 input, 1-3 pellets, 4 output.
 */
public class TileEntityRtgFurnace extends TileEntityMachineBase implements MenuProvider {

	public int dualCookTime;
	public static final int processingSpeed = 1000;

	private static final int[] slots_top = new int[] {0};
	private static final int[] slots_bottom = new int[] {4};
	private static final int[] slots_side = new int[] {1, 2, 3};

	public TileEntityRtgFurnace(BlockPos pos, BlockState state) {
		super(ModTileEntities.RTG_FURNACE.get(), pos, state, 5);
	}

	@Override
	public String getName() {
		return "container.rtgFurnace";
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return true;
	}

	public boolean hasPower() {
		return RTGUtil.hasHeat(slots, slots_side);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return side == Direction.DOWN ? slots_bottom : (side == Direction.UP ? slots_top : slots_side);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return side != Direction.DOWN || i != 1 || itemStack.is(Items.BUCKET);
	}

	public int getDiFurnaceProgressScaled(int i) {
		return (dualCookTime * i) / processingSpeed;
	}

	private ItemStack getSmeltingResult(ItemStack stack) {
		if(stack.isEmpty() || level == null) return ItemStack.EMPTY;
		Optional<RecipeHolder<SmeltingRecipe>> recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level);
		return recipe.map(r -> r.value().assemble(new SingleRecipeInput(stack), level.registryAccess())).orElse(ItemStack.EMPTY);
	}

	public boolean canProcess() {
		if(slots.get(0).isEmpty()) return false;

		ItemStack itemStack = getSmeltingResult(slots.get(0));
		if(itemStack.isEmpty()) return false;

		ItemStack out = slots.get(4);
		if(out.isEmpty()) return true;
		if(!ItemStack.isSameItemSameComponents(out, itemStack)) return false;
		return out.getCount() + itemStack.getCount() <= out.getMaxStackSize();
	}

	private void processItem() {
		if(canProcess()) {
			ItemStack itemStack = getSmeltingResult(slots.get(0));

			if(slots.get(4).isEmpty()) {
				slots.set(4, itemStack.copy());
			} else {
				slots.get(4).grow(itemStack.getCount());
			}

			this.removeItem(0, 1);
		}
	}

	public boolean isProcessing() {
		return this.dualCookTime > 0;
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			boolean markDirty = false;

			if(hasPower() && canProcess()) {
				dualCookTime += RTGUtil.updateRTGs(slots, slots_side);

				if(this.dualCookTime >= processingSpeed) {
					this.dualCookTime = 0;
					this.processItem();
					markDirty = true;
				}
			} else {
				dualCookTime = 0;
				RTGUtil.updateRTGs(slots, slots_side);
			}

			MachineRtgFurnace.updateBlockState(this.dualCookTime > 0, level, worldPosition);

			this.networkPackNT(15);

			if(markDirty) this.setChanged();
		}
	}

	/** The original synced the progress through the container */
	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(dualCookTime);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		dualCookTime = buf.readInt();
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		dualCookTime = nbt.getShort("cookTime");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putShort("cookTime", (short) dualCookTime);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerRtgFurnace(id, inv, this);
	}
}
