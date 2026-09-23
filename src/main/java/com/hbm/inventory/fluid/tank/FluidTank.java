package com.hbm.inventory.fluid.tank;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public class FluidTank implements Cloneable {
	
	public static final FluidTank[] EMPTY_ARRAY = new FluidTank[0];

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
	
	// TODO loadTank/unloadTank/setType (filling tanks from canisters and fluid identifiers) come back with the
	//  fluid container items, renderTank/renderTankInfo with the GUI system (phase 3)

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
