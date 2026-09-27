package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineDiFurnace;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.container.ContainerDiFurnace;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.BlastFurnaceRecipes;
import com.hbm.items.ModItems;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachinePolluting;

import api.hbm.fluidmk2.IFluidStandardSenderMK2;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Alloy furnace ("blast furnace" in the original): two inputs, burns coal/coke/lava and friends, triple speed with
 * the extension on top. Slots: 0 upper input, 1 lower input, 2 fuel, 3 output.
 * Each input slot only accepts automation from one side, right clicking the empty slot cycles through the sides.
 */
public class TileEntityDiFurnace extends TileEntityMachinePolluting implements IFluidStandardSenderMK2, MenuProvider {

	public int progress;
	public int fuel;
	public static final int maxFuel = 12800;
	public static final int processingSpeed = 400;
	private static final int[] slots_io = new int[] { 0, 1, 2, 3 };

	public byte sideFuel = 1;
	public byte sideUpper = 1;
	public byte sideLower = 1;

	public TileEntityDiFurnace(BlockPos pos, BlockState state) {
		super(ModTileEntities.DI_FURNACE.get(), pos, state, 4, 50);
	}

	@Override
	public String getName() {
		return "container.diFurnace";
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return i != 3;
	}

	public boolean hasItemPower(ItemStack stack) {
		return getItemPower(stack) > 0;
	}

	public static int getItemPower(ItemStack stack) {
		if(stack.isEmpty()) return 0;

		Item item = stack.getItem();
		if(item == Items.COAL) return 200;
		if(item == Blocks.COAL_BLOCK.asItem()) return 2000;
		if(ModBlocks.block_coke.any().test(stack)) return 4000;
		if(item == Items.LAVA_BUCKET) return 12800;
		if(item == Items.BLAZE_ROD) return 1000;
		if(item == Items.BLAZE_POWDER) return 300;
		if(item == ModItems.lignite.get()) return 150;
		if(item == ModItems.powder_lignite.get()) return 150;
		if(item == ModItems.powder_coal.get()) return 200;
		if(ModItems.briquette.values().stream().anyMatch(b -> stack.is(b.get()))) return 200;
		if(ModItems.coke.values().stream().anyMatch(coke -> stack.is(coke.get()))) return 400;
		if(item == ModItems.solid_fuel.get()) return 400;
		return 0;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.fuel = nbt.getInt("powerTime");
		this.progress = nbt.getShort("cookTime");
		byte[] modes = nbt.getByteArray("modes");
		if(modes.length == 3) {
			this.sideFuel = modes[0];
			this.sideUpper = modes[1];
			this.sideLower = modes[2];
		}
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putInt("powerTime", fuel);
		nbt.putShort("cookTime", (short) progress);
		nbt.putByteArray("modes", new byte[] {sideFuel, sideUpper, sideLower});
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return slots_io;
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemStack, Direction side) {
		int j = side.get3DDataValue();
		if(i == 0 && this.sideUpper != j) return false;
		if(i == 1 && this.sideLower != j) return false;
		if(i == 2 && this.sideFuel != j) return false;
		if(i == 3) return false;
		return this.isItemValidForSlot(i, itemStack);
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i == 3;
	}

	public int getDiFurnaceProgressScaled(int i) {
		return (progress * i) / processingSpeed;
	}

	public int getPowerRemainingScaled(int i) {
		return (fuel * i) / maxFuel;
	}

	public boolean canProcess() {
		if(slots.get(0).isEmpty() || slots.get(1).isEmpty()) return false;
		if(!this.hasPower()) return false;

		ItemStack output = BlastFurnaceRecipes.getOutput(slots.get(0), slots.get(1));
		if(output.isEmpty()) return false;

		ItemStack out = slots.get(3);
		if(out.isEmpty()) return true;
		if(!ItemStack.isSameItemSameComponents(out, output)) return false;
		return out.getCount() + output.getCount() <= out.getMaxStackSize();
	}

	private void processItem() {
		ItemStack itemStack = BlastFurnaceRecipes.getOutput(slots.get(0), slots.get(1));

		if(slots.get(3).isEmpty()) {
			slots.set(3, itemStack.copy());
		} else if(ItemStack.isSameItemSameComponents(slots.get(3), itemStack)) {
			slots.get(3).grow(itemStack.getCount());
		}

		for(int i = 0; i < 2; i++) {
			this.removeItem(i, 1);
		}
	}

	public boolean hasPower() {
		return fuel > 0;
	}

	public boolean isProcessing() {
		return this.progress > 0;
	}

	public boolean hasExtension() {
		return level.getBlockState(worldPosition.above()).is(ModBlocks.machine_difurnace_extension.get());
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			boolean extension = hasExtension();

			for(Direction dir : Direction.values()) {
				this.sendSmoke(worldPosition.relative(dir), dir);
			}
			if(extension) this.sendSmoke(worldPosition.above(2), Direction.UP);

			boolean markDirty = false;

			ItemStack fuelStack = this.slots.get(2);
			if(this.hasItemPower(fuelStack) && this.fuel <= (TileEntityDiFurnace.maxFuel - TileEntityDiFurnace.getItemPower(fuelStack))) {
				this.fuel += getItemPower(fuelStack);
				markDirty = true;
				ItemStack remainder = fuelStack.getCraftingRemainingItem();
				fuelStack.shrink(1);
				if(fuelStack.isEmpty()) this.slots.set(2, remainder);
			}

			if(canProcess()) {

				fuel -= 1; //switch it up on me, fuel efficiency, on fumes i'm running - running - running - running
				progress += extension ? 3 : 1;

				if(this.progress >= TileEntityDiFurnace.processingSpeed) {
					this.progress -= TileEntityDiFurnace.processingSpeed; // look mom, ive finally added something to a popular project
					this.processItem();
					markDirty = true;
				}

				if(fuel < 0) {
					fuel = 0;
				}

				if(level.getGameTime() % 20 == 0) this.pollute(PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * (extension ? 3 : 1));

			} else {
				progress = 0;
			}

			MachineDiFurnace.updateBlockState(this.progress > 0, level, worldPosition);

			networkPackNT(15);

			if(markDirty) {
				this.setChanged();
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeShort(this.progress);
		buf.writeShort(this.fuel);
		buf.writeBytes(new byte[] {this.sideFuel, this.sideUpper, this.sideLower});
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.progress = buf.readShort();
		this.fuel = buf.readShort();
		byte[] modes = new byte[3];
		buf.readBytes(modes);
		this.sideFuel = modes[0];
		this.sideUpper = modes[1];
		this.sideLower = modes[2];
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[0];
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return this.getSmokeTanks();
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerDiFurnace(id, inv, this);
	}
}
