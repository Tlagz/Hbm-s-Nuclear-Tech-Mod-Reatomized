package com.hbm.tileentity.machine;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.container.ContainerRadiolysis;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.RadiolysisRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemRTGPellet;
import com.hbm.lib.Library;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.DirPos;
import com.hbm.util.RTGUtil;
import com.hbm.util.Tuple.Pair;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * RTG and radiolysis chamber: an RTG making twice the power of the plain one, the heat cracks fluids (faster the
 * hotter it is, water into peroxide and hydrogen, oils like the cracking tower) and at 200 heat sterilizes
 * contaminated items (food is destroyed). Slots: 0-9 pellets, 10/11 fluid identifier in/out, 12/13 sterilization
 * in/out, 14 battery.
 *
 * TODO the MKU contagion itself isn't ported, sterilization only clears the ntmContagion custom data flag
 */
public class TileEntityMachineRadiolysis extends TileEntityMachineBase implements IEnergyProviderMK2, IFluidStandardTransceiverMK2, MenuProvider {

	public long power;
	public static final int maxPower = 1000000;
	public int heat;

	public FluidTank[] tanks;

	private static final int[] slot_io = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 12, 13 };
	private static final int[] slot_rtg = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

	public TileEntityMachineRadiolysis(BlockPos pos, BlockState state) {
		super(ModTileEntities.RADIOLYSIS.get(), pos, state, 15);
		tanks = new FluidTank[3];
		tanks[0] = new FluidTank(Fluids.NONE, 2_000);
		tanks[1] = new FluidTank(Fluids.NONE, 2_000);
		tanks[2] = new FluidTank(Fluids.NONE, 2_000);
	}

	@Override
	public String getName() {
		return "container.radiolysis";
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return i == 12 || (i < 10 && itemStack.getItem() instanceof ItemRTGPellet);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return slot_io;
	}

	/** Depleted pellets and sterilized items can be pulled out */
	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return (i < 10 && ModItems.pellet_rtg_depleted.values().stream().anyMatch(item -> itemStack.is(item.get()))) || i == 13;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.power = nbt.getLong("power");
		this.heat = nbt.getInt("heat");
		tanks[0].readFromNBT(nbt, "input");
		tanks[1].readFromNBT(nbt, "output1");
		tanks[2].readFromNBT(nbt, "output2");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("power", power);
		nbt.putInt("heat", heat);
		tanks[0].writeToNBT(nbt, "input");
		tanks[1].writeToNBT(nbt, "output1");
		tanks[2].writeToNBT(nbt, "output2");
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			power = Library.chargeItemsFromTE(slots, 14, power, maxPower);
			heat = RTGUtil.updateRTGs(slots, slot_rtg);
			power += heat * 10;

			if(power > maxPower) power = maxPower;

			tanks[0].setType(10, 11, slots);
			setupTanks();

			if(heat > 100) {
				int crackTime = (int) Math.max(-0.1 * (heat - 100) + 30, 5);

				if(level.getGameTime() % crackTime == 0) crack();
				if(heat >= 200 && level.getGameTime() % 100 == 0) sterilize();
			}

			for(DirPos pos : getConPos()) {
				this.tryProvide(level, pos, pos.getDir());
				this.trySubscribe(tanks[0].getTankType(), level, pos);
				if(tanks[1].getFill() > 0) this.tryProvide(tanks[1], level, pos);
				if(tanks[2].getFill() > 0) this.tryProvide(tanks[2], level, pos);
			}

			this.networkPackNT(50);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(this.power);
		buf.writeInt(this.heat);
		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
		tanks[2].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.heat = buf.readInt();
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
		tanks[2].deserialize(buf);
	}

	protected DirPos[] getConPos() {
		BlockPos p = worldPosition;
		return new DirPos[] {
				new DirPos(p.offset(2, 0, 0), Direction.EAST),
				new DirPos(p.offset(-2, 0, 0), Direction.WEST),
				new DirPos(p.offset(0, 0, 2), Direction.SOUTH),
				new DirPos(p.offset(0, 0, -2), Direction.NORTH)
		};
	}

	private void crack() {
		Pair<FluidStack, FluidStack> quart = RadiolysisRecipes.getRadiolysis(tanks[0].getTankType());

		if(quart != null) {
			int left = quart.getKey().fill;
			int right = quart.getValue().fill;

			if(tanks[0].getFill() >= 100 && hasSpace(left, right)) {
				tanks[0].setFill(tanks[0].getFill() - 100);
				tanks[1].setFill(tanks[1].getFill() + left);
				tanks[2].setFill(tanks[2].getFill() + right);
			}
		}
	}

	private boolean hasSpace(int left, int right) {
		return tanks[1].getFill() + left <= tanks[1].getMaxFill() && tanks[2].getFill() + right <= tanks[2].getMaxFill();
	}

	private void setupTanks() {
		Pair<FluidStack, FluidStack> quart = RadiolysisRecipes.getRadiolysis(tanks[0].getTankType());

		if(quart != null) {
			tanks[1].setTankType(quart.getKey().type);
			tanks[2].setTankType(quart.getValue().type);
		} else {
			tanks[0].setTankType(Fluids.NONE);
			tanks[1].setTankType(Fluids.NONE);
			tanks[2].setTankType(Fluids.NONE);
		}
	}

	/** Food gets destroyed, contaminated items lose the contagion */
	private void sterilize() {
		ItemStack in = slots.get(12);
		if(in.isEmpty()) return;

		if(in.has(DataComponents.FOOD) && !net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(in.getItem()).getPath().equals("pancake")) { // the pancake survives
			this.removeItem(12, 1);
		}

		if(!checkIfValid()) return;

		ItemStack output = slots.get(12).copyWithCount(1);
		CustomData.update(DataComponents.CUSTOM_DATA, output, tag -> tag.remove("ntmContagion"));

		if(slots.get(13).isEmpty()) {
			this.removeItem(12, 1);
			slots.set(13, output);
		} else if(ItemStack.isSameItemSameComponents(slots.get(13), output) && slots.get(13).getCount() < slots.get(13).getMaxStackSize()) {
			this.removeItem(12, 1);
			slots.get(13).grow(1);
		}
	}

	private boolean checkIfValid() {
		ItemStack in = slots.get(12);
		if(in.isEmpty()) return false;
		CustomData data = in.get(DataComponents.CUSTOM_DATA);
		return data != null && data.copyTag().getBoolean("ntmContagion");
	}

	@Override public void setPower(long power) { this.power = power; }
	@Override public long getPower() { return power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override public FluidTank[] getAllTanks() { return tanks; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] {tanks[1], tanks[2]}; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {tanks[0]}; }

	@Override
	public boolean canConnect(FluidType type, Direction dir) {
		return dir != Direction.DOWN;
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 3, worldPosition.getZ() + 2);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerRadiolysis(id, inv, this);
	}
}
