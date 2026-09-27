package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerSILEX;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.SILEXRecipes;
import com.hbm.inventory.recipes.SILEXRecipes.SILEXRecipe;
import com.hbm.inventory.recipes.SILEXRecipes.WeightedOutput;
import com.hbm.items.machine.ItemFELCrystal.EnumWavelengths;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;

import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * SILEX: laser isotope separation. Items dissolve in peroxide into up to 16000mB of "fluid" (some fluids like UF6 go
 * in directly), the FEL shining through the chamber sets the wavelength, which has to be at least the recipe's.
 * A stronger laser is faster. Slots: 0 input, 1 fluid identifier, 2-3 fluid containers in/out, 4 output, 5-10 queue.
 */
public class TileEntitySILEX extends TileEntityMachineBase implements IFluidStandardReceiverMK2, IControlReceiver, MenuProvider {

	public EnumWavelengths mode = EnumWavelengths.NULL;
	public FluidTank tank;
	/** What's dissolved right now (an item or a fluid icon), empty if nothing */
	public ItemStack current = ItemStack.EMPTY;
	public int currentFill;
	public static final int maxFill = 16000;
	public int progress;
	public static final int processTime = 100;

	public static final int PRIME = 137;
	public int recipeIndex = 0;
	private int loadDelay;

	/** Fluids that turn into their SILEX "item" directly (through the item translations, UF6 is uranium) */
	public static boolean isConvertedFluid(FluidType type) {
		return type == Fluids.UF6 || type == Fluids.PUF6 || type == Fluids.DEATH;
	}

	public TileEntitySILEX(BlockPos pos, BlockState state) {
		super(ModTileEntities.SILEX.get(), pos, state, 11);
		tank = new FluidTank(Fluids.PEROXIDE, 16000);
	}

	@Override
	public String getName() {
		return "container.machineSILEX";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			tank.setType(1, 1, slots);
			tank.loadTank(2, 3, slots);

			Direction dir = BlockDummyable.getRotation(getBlockState()).getClockWise();
			this.trySubscribe(tank.getTankType(), level, worldPosition.relative(dir, 2).above(), dir);
			this.trySubscribe(tank.getTankType(), level, worldPosition.relative(dir, -2).above(), dir.getOpposite());

			loadFluid();

			if(!process()) {
				this.progress = 0;
			}

			dequeue();

			if(currentFill <= 0) {
				current = ItemStack.EMPTY;
			}

			this.networkPackNT(50);

			// the FEL sets it again every tick while shining through
			this.mode = EnumWavelengths.NULL;
		}
	}

	private static boolean same(ItemStack a, ItemStack b) {
		return ItemStack.isSameItemSameComponents(a, b);
	}

	public void loadFluid() {

		ItemStack icon = ItemFluidIcon.make(tank.getTankType(), 1);

		if(isConvertedFluid(tank.getTankType()) || SILEXRecipes.getOutput(icon) != null) {
			if(currentFill == 0) current = icon.copy();

			if(!current.isEmpty() && same(current, icon)) {
				int toFill = Math.min(50, Math.min(maxFill - currentFill, tank.getFill()));
				currentFill += toFill;
				tank.setFill(tank.getFill() - toFill);
			}
		}

		loadDelay++;
		if(loadDelay > 20) loadDelay = 0;

		ItemStack in = slots.get(0);
		if(loadDelay == 0 && !in.isEmpty() && tank.getTankType() == Fluids.PEROXIDE && (current.isEmpty() || same(current, in.copyWithCount(1)))) {

			SILEXRecipe recipe = SILEXRecipes.getOutput(in);
			if(recipe == null) return;

			int load = recipe.fluidProduced;

			if(load <= maxFill - this.currentFill && load <= tank.getFill()) {
				this.currentFill += load;
				this.current = in.copyWithCount(1);
				tank.setFill(tank.getFill() - load);
				this.removeItem(0, 1);
			}
		}
	}

	private boolean process() {

		if(current.isEmpty() || currentFill <= 0) return false;

		SILEXRecipe recipe = SILEXRecipes.getOutput(current);

		if(recipe == null) return false;
		if(recipe.laserStrength.ordinal() > this.mode.ordinal()) return false;
		if(currentFill < recipe.fluidConsumed) return false;
		if(!slots.get(4).isEmpty()) return false;

		int progressSpeed = (int) Math.pow(2, this.mode.ordinal() - recipe.laserStrength.ordinal() + 1) / 2;

		progress += progressSpeed;

		if(progress >= processTime) {

			currentFill -= recipe.fluidConsumed;

			// not random: the outputs are picked in a fixed but scrambled order by stepping through the weights
			int totalWeight = 0;
			for(WeightedOutput weighted : recipe.outputs) totalWeight += weighted.weight();
			this.recipeIndex %= Math.max(totalWeight, 1);

			int weight = 0;
			for(WeightedOutput weighted : recipe.outputs) {
				weight += weighted.weight();
				if(this.recipeIndex < weight) {
					slots.set(4, weighted.stack().copy());
					break;
				}
			}

			progress = 0;
			this.setChanged();
			this.recipeIndex += PRIME;
		}

		return true;
	}

	private void dequeue() {

		ItemStack out = slots.get(4);
		if(out.isEmpty()) return;

		for(int i = 5; i < 11; i++) {
			ItemStack queue = slots.get(i);
			if(!queue.isEmpty() && queue.getCount() < queue.getMaxStackSize() && same(out, queue)) {
				queue.grow(1);
				this.removeItem(4, 1);
				return;
			}
		}

		for(int i = 5; i < 11; i++) {
			if(slots.get(i).isEmpty()) {
				slots.set(i, out.copy());
				slots.set(4, ItemStack.EMPTY);
				return;
			}
		}
	}

	public int getProgressScaled(int i) {
		return (progress * i) / processTime;
	}

	public int getFluidScaled(int i) {
		return (tank.getFill() * i) / tank.getMaxFill();
	}

	public int getFillScaled(int i) {
		return (currentFill * i) / maxFill;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(currentFill);
		buf.writeInt(progress);
		buf.writeByte(mode.ordinal());
		tank.serialize(buf);
		BufferUtil.writeItemStack(buf, current, level.registryAccess());
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		currentFill = buf.readInt();
		progress = buf.readInt();
		mode = EnumWavelengths.values()[buf.readByte()];
		tank.deserialize(buf);
		current = BufferUtil.readItemStack(buf, level.registryAccess());
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.tank.readFromNBT(nbt, "tank");
		this.currentFill = nbt.getInt("fill");
		this.recipeIndex = nbt.getInt("recipeIndex");
		this.current = currentFill > 0 && nbt.contains("current") ? ItemStack.parseOptional(registries, nbt.getCompound("current")) : ItemStack.EMPTY;
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		this.tank.writeToNBT(nbt, "tank");
		nbt.putInt("fill", this.currentFill);
		nbt.putInt("recipeIndex", this.recipeIndex);
		if(!current.isEmpty()) nbt.put("current", current.save(registries));
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] { 0, 5, 6, 7, 8, 9, 10 };
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return i == 0 && SILEXRecipes.getOutput(itemStack) != null;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, Direction side) {
		return slot >= 5;
	}

	/** The "void contents" button */
	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("void")) {
			this.currentFill = 0;
			this.current = ItemStack.EMPTY;
		}
	}

	@Override public boolean hasPermission(Player player) { return this.stillValid(player); }

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 3, worldPosition.getZ() + 2);
	}

	@Override public FluidTank[] getAllTanks() { return new FluidTank[] {tank}; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {tank}; }

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerSILEX(id, inv, this);
	}
}
