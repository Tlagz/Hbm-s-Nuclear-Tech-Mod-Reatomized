package com.hbm.tileentity.machine.oil;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.ModMenus;
import com.hbm.inventory.container.ContainerOilProcessor;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.OilProcessingRecipes;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.util.DirPos;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Vacuum distiller: pressurized oil (2 PU) into four fractions, 10,000 HE per 100mB.
 * Slots: 0 battery, 1/2 input containers, 3-10 output containers, 11 fluid identifier.
 */
public class TileEntityMachineVacuumDistill extends TileEntityOilProcessorBase {

	private AudioWrapper audio;
	private int audioTime;
	public boolean isOn;

	public TileEntityMachineVacuumDistill(BlockPos pos, BlockState state) {
		super(ModTileEntities.VACUUM_DISTILL.get(), pos, state, 12);
		this.tanks = new FluidTank[5];
		this.tanks[0] = new FluidTank(Fluids.OIL, 64_000).withPressure(2);
		this.tanks[1] = new FluidTank(Fluids.HEAVYOIL_VACUUM, 24_000);
		this.tanks[2] = new FluidTank(Fluids.REFORMATE, 24_000);
		this.tanks[3] = new FluidTank(Fluids.LIGHTOIL_VACUUM, 24_000);
		this.tanks[4] = new FluidTank(Fluids.SOURGAS, 24_000);
	}

	@Override
	public String getName() {
		return "container.vacuumDistill";
	}

	@Override
	protected String[] getTankKeys() {
		return new String[] {"input", "heavy", "reformate", "light", "gas"};
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.isOn = false;

			this.chargeAndConnect(true);
			tanks[0].setType(11, slots);
			tanks[0].loadTank(1, 2, slots);

			refine();

			tanks[1].unloadTank(3, 4, slots);
			tanks[2].unloadTank(5, 6, slots);
			tanks[3].unloadTank(7, 8, slots);
			tanks[4].unloadTank(9, 10, slots);

			this.sendOutputs();

			this.networkPackNT(150);
		} else {

			if(this.isOn) audioTime = 20;

			if(audioTime > 0) {

				audioTime--;

				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}

				audio.updateVolume(getVolume(1F));
				audio.keepAlive();

			} else {

				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return AudioWrapper.getLoopedSound("hbm:block.boiler", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 0.25F, 15F, 1.0F, 20);
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(this.isOn);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.isOn = buf.readBoolean();
	}

	private void refine() {
		FluidStack[] stacks = OilProcessingRecipes.vacuum.get(tanks[0].getTankType());

		if(stacks == null) {
			for(int i = 1; i < 5; i++) tanks[i].setTankType(Fluids.NONE);
			return;
		}

		for(int i = 0; i < stacks.length; i++) tanks[i + 1].setTankType(stacks[i].type);

		if(power < 10_000) return;
		if(tanks[0].getFill() < 100) return;
		for(int i = 0; i < stacks.length; i++) if(tanks[i + 1].getFill() + stacks[i].fill > tanks[i + 1].getMaxFill()) return;

		this.isOn = true;
		power -= 10_000;
		tanks[0].setFill(tanks[0].getFill() - 100);

		for(int i = 0; i < stacks.length; i++) tanks[i + 1].setFill(tanks[i + 1].getFill() + stacks[i].fill);
	}

	@Override
	public DirPos[] getConPos() {
		BlockPos p = worldPosition;
		return new DirPos[] {
				new DirPos(p.offset(2, 0, 1), Direction.EAST),
				new DirPos(p.offset(2, 0, -1), Direction.EAST),
				new DirPos(p.offset(-2, 0, 1), Direction.WEST),
				new DirPos(p.offset(-2, 0, -1), Direction.WEST),
				new DirPos(p.offset(1, 0, 2), Direction.SOUTH),
				new DirPos(p.offset(-1, 0, 2), Direction.SOUTH),
				new DirPos(p.offset(1, 0, -2), Direction.NORTH),
				new DirPos(p.offset(-1, 0, -2), Direction.NORTH)
		};
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 9, worldPosition.getZ() + 2);
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] {tanks[1], tanks[2], tanks[3], tanks[4]};
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] {tanks[0]};
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerOilProcessor(ModMenus.VACUUM_DISTILL.get(), id, inv, this);
	}
}
