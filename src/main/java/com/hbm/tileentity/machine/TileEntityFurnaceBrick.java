package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.Optional;

import com.hbm.blocks.machine.MachineBrickFurnace;
import com.hbm.inventory.container.ContainerFurnaceBrick;
import com.hbm.items.ItemEnums.EnumAshType;
import com.hbm.items.ModItems;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Bricked furnace: a vanilla style furnace that smelts clay, bricks, stone, sand and logs faster and collects ash
 * from the burned fuel. Slots: 0 input, 1 fuel, 2 output, 3 ash.
 */
public class TileEntityFurnaceBrick extends TileEntityMachineBase implements MenuProvider {

	private static final int[] slotsTop = new int[] { 0 };
	private static final int[] slotsBottom = new int[] { 2, 1, 3 };
	private static final int[] slotsSides = new int[] { 1 };

	public static HashMap<Item, Integer> burnSpeed = new HashMap<>();

	public int burnTime;
	public int maxBurnTime;
	public int progress;

	public int ashLevelWood;
	public int ashLevelCoal;
	public int ashLevelMisc;

	public TileEntityFurnaceBrick(BlockPos pos, BlockState state) {
		super(ModTileEntities.FURNACE_BRICK.get(), pos, state, 4);
	}

	public static void registerSpeeds() {
		burnSpeed.clear();
		burnSpeed.put(Items.CLAY_BALL, 4);
		burnSpeed.put(ModItems.ball_fireclay.get(), 4);
		burnSpeed.put(Blocks.NETHERRACK.asItem(), 4);
		burnSpeed.put(Blocks.COBBLESTONE.asItem(), 2);
		burnSpeed.put(Blocks.SAND.asItem(), 2);
		for(Item log : new Item[] {Items.OAK_LOG, Items.SPRUCE_LOG, Items.BIRCH_LOG, Items.JUNGLE_LOG, Items.ACACIA_LOG, Items.DARK_OAK_LOG, Items.MANGROVE_LOG, Items.CHERRY_LOG}) burnSpeed.put(log, 2);
	}

	@Override
	public String getName() {
		return "container.furnaceBrick";
	}

	private Optional<RecipeHolder<SmeltingRecipe>> getSmelting(ItemStack stack) {
		if(stack.isEmpty() || level == null) return Optional.empty();
		return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level);
	}

	private ItemStack getSmeltingResult(ItemStack stack) {
		return getSmelting(stack).map(r -> r.value().assemble(new SingleRecipeInput(stack), level.registryAccess())).orElse(ItemStack.EMPTY);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			boolean wasBurning = this.burnTime > 0;
			boolean markDirty = false;

			if(this.burnTime > 0) {
				this.burnTime--;
			}

			if(this.burnTime != 0 || !this.slots.get(1).isEmpty() && !this.slots.get(0).isEmpty()) {

				if(this.burnTime == 0 && this.canSmelt()) {
					ItemStack fuel = this.slots.get(1);
					this.maxBurnTime = this.burnTime = fuel.isEmpty() ? 0 : fuel.getBurnTime(RecipeType.SMELTING);

					if(this.burnTime > 0) {
						markDirty = true;

						EnumAshType type = TileEntityFireboxBase.getAshFromFuel(fuel);
						if(type == EnumAshType.WOOD) ashLevelWood += burnTime;
						if(type == EnumAshType.COAL) ashLevelCoal += burnTime;
						if(type == EnumAshType.MISC) ashLevelMisc += burnTime;
						int threshold = 2000;
						if(processAsh(ashLevelWood, EnumAshType.WOOD, threshold)) ashLevelWood -= threshold;
						if(processAsh(ashLevelCoal, EnumAshType.COAL, threshold)) ashLevelCoal -= threshold;
						if(processAsh(ashLevelMisc, EnumAshType.MISC, threshold)) ashLevelMisc -= threshold;

						ItemStack remainder = fuel.getCraftingRemainingItem();
						fuel.shrink(1);
						if(fuel.isEmpty()) this.slots.set(1, remainder);
					}
				}

				if(this.burnTime > 0 && this.canSmelt()) {
					this.progress += this.getBurnSpeed();

					if(this.progress >= 200) {
						this.progress = 0;
						this.smeltItem();
						markDirty = true;
					}
				} else {
					this.progress = 0;
				}
			}

			if(wasBurning != this.burnTime > 0) {
				markDirty = true;
				MachineBrickFurnace.updateBlockState(this.burnTime > 0, level, worldPosition);
			}

			if(markDirty) this.setChanged();

			this.networkPackNT(15);
		}
	}

	public int getBurnSpeed() {
		Integer speed = burnSpeed.get(slots.get(0).getItem());
		return speed != null ? speed : 1;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return slot >= 2 ? false : (slot == 1 ? stack.getBurnTime(RecipeType.SMELTING) > 0 : true);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return side == Direction.DOWN ? slotsBottom : (side == Direction.UP ? slotsTop : slotsSides);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, Direction side) {
		return slot >= 2;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(burnTime);
		buf.writeInt(maxBurnTime);
		buf.writeInt(progress);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.burnTime = buf.readInt();
		this.maxBurnTime = buf.readInt();
		this.progress = buf.readInt();
	}

	/** One pile of ash for every 2000 ticks of fuel of a kind */
	protected boolean processAsh(int level, EnumAshType type, int threshold) {

		if(level >= threshold) {
			ItemStack ash = slots.get(3);
			Item ashItem = ModItems.powder_ash.get(type).get();

			if(ash.isEmpty()) {
				slots.set(3, new ItemStack(ashItem));
				return true;
			} else if(ash.getCount() < ash.getMaxStackSize() && ash.is(ashItem)) {
				ash.grow(1);
				return true;
			}
		}

		return false;
	}

	private boolean canSmelt() {
		if(this.slots.get(0).isEmpty()) return false;

		ItemStack result = getSmeltingResult(this.slots.get(0));
		if(result.isEmpty()) return false;

		ItemStack out = this.slots.get(2);
		if(out.isEmpty()) return true;
		if(!ItemStack.isSameItemSameComponents(out, result)) return false;

		int count = out.getCount() + result.getCount();
		return count <= getMaxStackSize() && count <= out.getMaxStackSize();
	}

	public void smeltItem() {
		if(this.canSmelt()) {
			ItemStack result = getSmeltingResult(this.slots.get(0));

			if(this.slots.get(2).isEmpty()) {
				this.slots.set(2, result.copy());
			} else {
				this.slots.get(2).grow(result.getCount());
			}

			this.slots.get(0).shrink(1);
		}
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.burnTime = nbt.getInt("burnTime");
		this.maxBurnTime = nbt.getInt("maxBurn");
		this.progress = nbt.getInt("progress");
		this.ashLevelWood = nbt.getInt("ashWood");
		this.ashLevelCoal = nbt.getInt("ashCoal");
		this.ashLevelMisc = nbt.getInt("ashMisc");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putInt("burnTime", this.burnTime);
		nbt.putInt("maxBurn", this.maxBurnTime);
		nbt.putInt("progress", this.progress);
		nbt.putInt("ashWood", this.ashLevelWood);
		nbt.putInt("ashCoal", this.ashLevelCoal);
		nbt.putInt("ashMisc", this.ashLevelMisc);
	}

	@Override
	public Component getDisplayName() {
		return this.hasCustomInventoryName() ? Component.literal(this.getInventoryName()) : Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerFurnaceBrick(id, inv, this);
	}
}
