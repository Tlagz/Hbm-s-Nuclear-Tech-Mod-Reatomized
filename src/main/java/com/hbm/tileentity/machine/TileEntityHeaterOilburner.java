package com.hbm.tileentity.machine;

import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerOilburner;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachinePolluting;
import com.hbm.util.DirPos;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import api.hbm.tile.IHeatSource;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Fluid burner: burns 1-10 mB/t of a flammable fluid into heat for the machine on top.
 * Slots: 0/1 fluid container in/out, 2 fluid identifier.
 *
 * TODO copy tool, RoR values/functions
 */
public class TileEntityHeaterOilburner extends TileEntityMachinePolluting implements IFluidStandardTransceiverMK2, IHeatSource, IControlReceiver, MenuProvider {

	public boolean isOn = false;
	public FluidTank tank;
	public int setting = 1;

	public int heatEnergy;
	public static final int maxHeatEnergy = 100_000;

	public TileEntityHeaterOilburner(BlockPos pos, BlockState state) {
		super(ModTileEntities.HEATER_OILBURNER.get(), pos, state, 3, 100);
		tank = new FluidTank(Fluids.HEATINGOIL, 16000);
	}

	@Override
	public String getName() {
		return "container.heaterOilburner";
	}

	public DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(worldPosition.east(2), Direction.EAST),
				new DirPos(worldPosition.west(2), Direction.WEST),
				new DirPos(worldPosition.south(2), Direction.SOUTH),
				new DirPos(worldPosition.north(2), Direction.NORTH)
		};
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			tank.loadTank(0, 1, slots);
			tank.setType(2, slots);

			for(DirPos pos : this.getConPos()) {
				this.trySubscribe(tank.getTankType(), level, pos, pos.getDir());
				this.sendSmoke(pos, pos.getDir());
			}

			boolean shouldCool = true;

			if(this.isOn && this.heatEnergy < maxHeatEnergy) {

				if(tank.getTankType().hasTrait(FT_Flammable.class)) {
					FT_Flammable type = tank.getTankType().getTrait(FT_Flammable.class);

					int burnRate = setting;
					int toBurn = Math.min(burnRate, tank.getFill());

					tank.setFill(tank.getFill() - toBurn);

					int heat = (int) (type.getHeatEnergy() / 1000);

					this.heatEnergy += heat * toBurn;

					if(level.getGameTime() % 5 == 0 && toBurn > 0) {
						super.pollute(tank.getTankType(), FluidReleaseType.BURN, toBurn * 5);
					}

					shouldCool = false;
				}
			}

			if(this.heatEnergy >= maxHeatEnergy)
				shouldCool = false;

			if(shouldCool)
				this.heatEnergy = Math.max(this.heatEnergy - Math.max(this.heatEnergy / 1000, 1), 0);

			this.networkPackNT(25);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		tank.serialize(buf);

		buf.writeBoolean(isOn);
		buf.writeInt(heatEnergy);
		buf.writeByte((byte) this.setting);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		tank.deserialize(buf);

		isOn = buf.readBoolean();
		heatEnergy = buf.readInt();
		setting = buf.readByte();
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		tank.readFromNBT(nbt, "tank");
		isOn = nbt.getBoolean("isOn");
		heatEnergy = nbt.getInt("heatEnergy");
		setting = nbt.getByte("setting");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		tank.writeToNBT(nbt, "tank");
		nbt.putBoolean("isOn", isOn);
		nbt.putInt("heatEnergy", heatEnergy);
		nbt.putByte("setting", (byte) this.setting);
	}

	public void toggleSetting() {
		setting++;

		if(setting > 10)
			setting = 1;
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] { tank };
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return this.getSmokeTanks();
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] { tank };
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
	public boolean hasPermission(Player player) {
		return player.distanceToSqr(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) <= 256;
	}

	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("toggle")) {
			this.isOn = !this.isOn;
		}
		this.markChanged();
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 2, worldPosition.getZ() + 2);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerOilburner(id, inv, this);
	}
}
