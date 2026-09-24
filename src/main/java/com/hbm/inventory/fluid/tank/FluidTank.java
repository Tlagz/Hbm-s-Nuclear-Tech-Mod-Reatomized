package com.hbm.inventory.fluid.tank;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;

public class FluidTank implements Cloneable {
	
	public static final FluidTank[] EMPTY_ARRAY = new FluidTank[0];

	public static final List<FluidLoadingHandler> loadingHandlers = new ArrayList<FluidLoadingHandler>();

	static {
		loadingHandlers.add(new FluidLoaderStandard());
		loadingHandlers.add(new FluidLoaderFillableItem());
		loadingHandlers.add(new FluidLoaderInfinite());
	}

	protected FluidType type;
	protected int fluid;
	protected int maxFluid;
	protected int pressure = 0;
	
	public FluidTank(FluidType type, int maxFluid) {
		this.type = type;
		this.maxFluid = maxFluid;
	}
	
	public FluidTank withPressure(int pressure) {
		if(this.pressure != pressure) this.setFill(0);
		this.pressure = pressure;
		return this;
	}
	
	public void setFill(int i) { fluid = i; }
	
	public void setTankType(FluidType type) {
		if(type == null) type = Fluids.NONE;
		if(this.type == type) return;
		
		this.type = type;
		this.setFill(0);
	}
	
	public void resetTank() {
		this.type = Fluids.NONE;
		this.fluid = 0;
		this.pressure = 0;
	}
	
	/** Changes type and pressure based on a fluid stack, useful for changing tank types based on recipes */
	public FluidTank conform(FluidStack stack) {
		this.setTankType(stack.type);
		this.withPressure(stack.pressure);
		return this;
	}
	
	public FluidType getTankType() { return type; }
	public int getFill() { return fluid; }
	public int getMaxFill() { return maxFluid; }
	public int getPressure() { return pressure; }
	
	public int changeTankSize(int size) {
		maxFluid = size;
		
		if(fluid > maxFluid) {
			int dif = fluid - maxFluid;
			fluid = maxFluid;
			return dif;
		}
		return 0;
	}
	
	//Fills tank from canisters
	public boolean loadTank(int in, int out, List<ItemStack> slots) {
		if(slots.get(in).isEmpty()) return false;

		boolean isInfiniteBarrel = slots.get(in).is(ModItems.fluid_barrel_infinite.get());
		if(!isInfiniteBarrel && pressure != 0) return false;

		int prev = this.getFill();

		for(FluidLoadingHandler handler : loadingHandlers) {
			if(handler.emptyItem(slots, in, out, this)) {
				break;
			}
		}

		return this.getFill() > prev;
	}

	//Fills canisters from tank
	public boolean unloadTank(int in, int out, List<ItemStack> slots) {
		if(slots.get(in).isEmpty()) return false;

		int prev = this.getFill();

		for(FluidLoadingHandler handler : loadingHandlers) {
			if(handler.fillItem(slots, in, out, this)) {
				break;
			}
		}

		return this.getFill() < prev;
	}

	public boolean setType(int in, List<ItemStack> slots) {
		return setType(in, in, slots);
	}

	/**
	 * Changes the tank type to the one of the fluid identifier in slot "in" and returns true if successful.
	 * With in != out the identifier is moved to the "out" slot.
	 */
	public boolean setType(int in, int out, List<ItemStack> slots) {

		if(!slots.get(in).isEmpty() && slots.get(in).getItem() instanceof IItemFluidIdentifier id) {

			if(in == out) {
				FluidType newType = id.getType(null, null, slots.get(in));

				if(type != newType) {
					type = newType;
					fluid = 0;
					return true;
				}

			} else if(slots.get(out).isEmpty()) {
				FluidType newType = id.getType(null, null, slots.get(in));
				if(type != newType) {
					type = newType;
					slots.set(out, slots.get(in).copy());
					slots.set(in, ItemStack.EMPTY);
					fluid = 0;
					return true;
				}
			}
		}

		return false;
	}

	//Called by TE to save fillstate
	public void writeToNBT(CompoundTag nbt, String s) {
		nbt.putInt(s, fluid);
		nbt.putInt(s + "_max", maxFluid);
		nbt.putInt(s + "_type", type.getID());
		nbt.putShort(s + "_p", (short) pressure);
	}
	
	//Called by TE to load fillstate
	public void readFromNBT(CompoundTag nbt, String s) {
		fluid = nbt.getInt(s);
		int max = nbt.getInt(s + "_max");
		if(max > 0)
			maxFluid = max;
		
		fluid = Mth.clamp(fluid, 0, max);
		
		type = Fluids.fromNameCompat(nbt.getString(s + "_type")); //compat
		if(type == Fluids.NONE)
			type = Fluids.fromID(nbt.getInt(s + "_type"));
		
		this.pressure = nbt.getShort(s + "_p");
	}
	
	public void serialize(ByteBuf buf) {
		buf.writeInt(fluid);
		buf.writeInt(maxFluid);
		buf.writeInt(type.getID());
		buf.writeShort((short) pressure);
	}

	public void deserialize(ByteBuf buf) {
		fluid = buf.readInt();
		maxFluid = buf.readInt();
		type = Fluids.fromID(buf.readInt());
		pressure = buf.readShort();
	}
}
