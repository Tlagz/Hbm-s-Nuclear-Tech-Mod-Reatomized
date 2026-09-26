package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerHeaterHeatex;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.inventory.fluid.trait.FT_Coolable.CoolingType;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.DirPos;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import api.hbm.tile.IHeatSource;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Heat exchanging heater: cools a hot fluid (hot coolant, steam...) and gives its heat to the machine on top.
 * The amount per cycle and the cycle delay are set in the GUI. Slot 0: fluid identifier.
 *
 * TODO copy tool, RoR values
 */
public class TileEntityHeaterHeatex extends TileEntityMachineBase implements IHeatSource, IFluidStandardTransceiverMK2, IControlReceiver, MenuProvider {

	public FluidTank[] tanks;
	public int amountToCool = 24_000;
	public int tickDelay = 1;
	public int heatEnergy;

	public TileEntityHeaterHeatex(BlockPos pos, BlockState state) {
		super(ModTileEntities.HEATER_HEATEX.get(), pos, state, 1);
		this.tanks = new FluidTank[2];
		this.tanks[0] = new FluidTank(Fluids.COOLANT_HOT, 24_000);
		this.tanks[1] = new FluidTank(Fluids.COOLANT, 24_000);
	}

	@Override
	public String getName() {
		return "container.heaterHeatex";
	}

	/** Sync snapshot: the hot tank before this tick's conversion, the cold tank after */
	private ByteBuf buf;

	@Override
	public void updateEntity() {

		if(isServer()) {

			if(this.buf != null) this.buf.release();
			this.buf = Unpooled.buffer();

			this.tanks[0].setType(0, slots);
			this.setupTanks();
			this.updateConnections();

			this.heatEnergy *= 0.999;

			tanks[0].serialize(buf);
			this.tryConvert();
			tanks[1].serialize(buf);

			networkPackNT(25);

			for(DirPos pos : getConPos()) {
				if(this.tanks[1].getFill() > 0) this.tryProvide(tanks[1], level, pos, pos.getDir());
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		if(this.buf != null) {
			buf.writeBytes(this.buf, this.buf.readerIndex(), this.buf.readableBytes());
		} else {
			tanks[0].serialize(buf);
			tanks[1].serialize(buf);
		}
		buf.writeInt(this.heatEnergy);
		buf.writeInt(this.amountToCool);
		buf.writeInt(this.tickDelay);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
		this.heatEnergy = buf.readInt();
		this.amountToCool = buf.readInt();
		this.tickDelay = buf.readInt();
	}

	protected void setupTanks() {

		if(tanks[0].getTankType().hasTrait(FT_Coolable.class)) {
			FT_Coolable trait = tanks[0].getTankType().getTrait(FT_Coolable.class);
			if(trait.getEfficiency(CoolingType.HEATEXCHANGER) > 0) {
				tanks[1].setTankType(trait.coolsTo);
				return;
			}
		}

		tanks[0].setTankType(Fluids.NONE);
		tanks[1].setTankType(Fluids.NONE);
	}

	protected void updateConnections() {
		for(DirPos pos : getConPos()) {
			this.trySubscribe(tanks[0].getTankType(), level, pos, pos.getDir());
		}
	}

	protected void tryConvert() {

		if(!tanks[0].getTankType().hasTrait(FT_Coolable.class)) return;
		if(tickDelay < 1) tickDelay = 1;
		if(level.getGameTime() % tickDelay != 0) return;

		FT_Coolable trait = tanks[0].getTankType().getTrait(FT_Coolable.class);

		int inputOps = tanks[0].getFill() / trait.amountReq;
		int outputOps = (tanks[1].getMaxFill() - tanks[1].getFill()) / trait.amountProduced;
		int opCap = this.amountToCool;

		int ops = Math.min(inputOps, Math.min(outputOps, opCap));
		tanks[0].setFill(tanks[0].getFill() - trait.amountReq * ops);
		tanks[1].setFill(tanks[1].getFill() + trait.amountProduced * ops);
		this.heatEnergy += trait.heatEnergy * ops * trait.getEfficiency(CoolingType.HEATEXCHANGER);
		this.markChanged();
	}

	/** Two ports on the front and two on the back, next to the corner blocks */
	private DirPos[] getConPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();

		return new DirPos[] {
				new DirPos(worldPosition.relative(dir, 2).relative(rot), dir),
				new DirPos(worldPosition.relative(dir, 2).relative(rot, -1), dir),
				new DirPos(worldPosition.relative(dir, -2).relative(rot), dir.getOpposite()),
				new DirPos(worldPosition.relative(dir, -2).relative(rot, -1), dir.getOpposite())
		};
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.tanks[0].readFromNBT(nbt, "0");
		this.tanks[1].readFromNBT(nbt, "1");
		this.heatEnergy = nbt.getInt("heatEnergy");
		this.amountToCool = nbt.getInt("toCool");
		this.tickDelay = nbt.getInt("delay");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		this.tanks[0].writeToNBT(nbt, "0");
		this.tanks[1].writeToNBT(nbt, "1");
		nbt.putInt("heatEnergy", heatEnergy);
		nbt.putInt("toCool", amountToCool);
		nbt.putInt("delay", tickDelay);
	}

	@Override
	public int getHeatStored() {
		return heatEnergy;
	}

	@Override
	public void useUpHeat(int heat) {
		this.heatEnergy = Math.max(0, this.heatEnergy - heat);
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] {tanks[1]};
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {tanks[0]};
	}

	@Override
	public boolean canConnect(FluidType type, Direction dir) {
		Direction facing = BlockDummyable.getRotation(getBlockState());
		return dir == facing || dir == facing.getOpposite();
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 1, worldPosition.getZ() + 2);
	}

	@Override
	public boolean hasPermission(Player player) {
		return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) < 16 * 16;
	}

	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("toCool")) this.amountToCool = Mth.clamp(data.getInt("toCool"), 1, tanks[0].getMaxFill());
		if(data.contains("delay")) this.tickDelay = Math.max(data.getInt("delay"), 1);
		this.markChanged();
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerHeaterHeatex(id, inv, this);
	}
}
