package com.hbm.tileentity.machine.oil;

import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.container.ContainerMachineRefinery;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.RefineryRecipes;
import com.hbm.inventory.recipes.RefineryRecipes.RefineryRecipe;
import com.hbm.items.ModDataComponents;
import com.hbm.lib.Library;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
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
 * Oil refinery: 100mB hot oil + 5 HE per tick into heavy oil, naphtha, light oil and petroleum gas, a solid
 * byproduct (sulfur) every 10 operations.
 * Slots: 0 battery, 1/2 input containers, 3-10 output containers (in/out per tank), 11 solid output, 12 identifier.
 *
 * TODO exploding and burning (IOverpressurable, IRepairable, onBlockExploded), tilting, copy/paste
 */
public class TileEntityMachineRefinery extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, MenuProvider {

	public long power = 0;
	public int sulfur = 0;
	public static final int maxSulfur = 10;
	public static final long maxPower = 1000;
	public FluidTank[] tanks;

	public boolean hasExploded = false;
	public boolean onFire = false;

	private AudioWrapper audio;
	private int audioTime;
	public boolean isOn;

	private static final int[] slot_access = new int[] {11};

	public TileEntityMachineRefinery(BlockPos pos, BlockState state) {
		super(ModTileEntities.REFINERY.get(), pos, state, 13);
		tanks = new FluidTank[5];
		tanks[0] = new FluidTank(Fluids.HOTOIL, 64_000);
		tanks[1] = new FluidTank(Fluids.HEAVYOIL, 24_000);
		tanks[2] = new FluidTank(Fluids.NAPHTHA, 24_000);
		tanks[3] = new FluidTank(Fluids.LIGHTOIL, 24_000);
		tanks[4] = new FluidTank(Fluids.PETROLEUM, 24_000);
	}

	@Override
	public String getName() {
		return "container.machineRefinery";
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return false;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);

		power = nbt.getLong("power");
		tanks[0].readFromNBT(nbt, "input");
		tanks[1].readFromNBT(nbt, "heavy");
		tanks[2].readFromNBT(nbt, "naphtha");
		tanks[3].readFromNBT(nbt, "light");
		tanks[4].readFromNBT(nbt, "petroleum");
		sulfur = nbt.getInt("sulfur");
		hasExploded = nbt.getBoolean("exploded");
		onFire = nbt.getBoolean("onFire");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);

		nbt.putLong("power", power);
		tanks[0].writeToNBT(nbt, "input");
		tanks[1].writeToNBT(nbt, "heavy");
		tanks[2].writeToNBT(nbt, "naphtha");
		tanks[3].writeToNBT(nbt, "light");
		tanks[4].writeToNBT(nbt, "petroleum");
		nbt.putInt("sulfur", sulfur);
		nbt.putBoolean("exploded", hasExploded);
		nbt.putBoolean("onFire", onFire);
	}

	/// IPersistentNBT: the tanks stay with the dropped refinery ///

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		boolean empty = true;
		for(FluidTank tank : tanks) if(tank.getFill() > 0) empty = false;
		if(empty) return;
		CompoundTag nbt = new CompoundTag();
		for(int i = 0; i < 5; i++) tanks[i].writeToNBT(nbt, "" + i);
		components.set(ModDataComponents.PERSISTENT.get(), CustomData.of(nbt));
	}

	@Override
	protected void applyImplicitComponents(DataComponentInput input) {
		super.applyImplicitComponents(input);
		CustomData data = input.get(ModDataComponents.PERSISTENT.get());
		if(data == null) return;
		CompoundTag nbt = data.copyTag();
		for(int i = 0; i < 5; i++) tanks[i].readFromNBT(nbt, "" + i);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return slot_access;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i == 11;
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.isOn = false;

			if(!this.hasExploded) {

				this.updateConnections();

				power = Library.chargeTEFromItems(slots, 0, power, maxPower);
				tanks[0].setType(12, slots);
				tanks[0].loadTank(1, 2, slots);

				refine();

				tanks[1].unloadTank(3, 4, slots);
				tanks[2].unloadTank(5, 6, slots);
				tanks[3].unloadTank(7, 8, slots);
				tanks[4].unloadTank(9, 10, slots);

				for(DirPos pos : getConPos()) {
					for(int i = 1; i < 5; i++) {
						if(tanks[i].getFill() > 0) {
							this.tryProvide(tanks[i], level, pos, pos.getDir());
						}
					}
				}
			}

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
		buf.writeLong(this.power);
		for(int i = 0; i < 5; i++) tanks[i].serialize(buf);
		buf.writeBoolean(this.hasExploded);
		buf.writeBoolean(this.onFire);
		buf.writeBoolean(this.isOn);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		for(int i = 0; i < 5; i++) tanks[i].deserialize(buf);
		this.hasExploded = buf.readBoolean();
		this.onFire = buf.readBoolean();
		this.isOn = buf.readBoolean();
	}

	private void refine() {
		RefineryRecipe refinery = RefineryRecipes.getRefinery(tanks[0].getTankType());
		if(refinery == null) {
			for(int i = 1; i < 5; i++) tanks[i].setTankType(Fluids.NONE);
			return;
		}

		FluidStack[] stacks = refinery.outputs;

		for(int i = 0; i < stacks.length; i++) tanks[i + 1].setTankType(stacks[i].type);

		if(power < 5 || tanks[0].getFill() < 100) return;

		for(int i = 0; i < stacks.length; i++) {
			if(tanks[i + 1].getFill() + stacks[i].fill > tanks[i + 1].getMaxFill()) {
				return;
			}
		}

		this.isOn = true;
		tanks[0].setFill(tanks[0].getFill() - 100);

		for(int i = 0; i < stacks.length; i++) tanks[i + 1].setFill(tanks[i + 1].getFill() + stacks[i].fill);

		this.sulfur++;

		if(this.sulfur >= maxSulfur) {
			this.sulfur -= maxSulfur;

			ItemStack out = refinery.solid;

			if(out != null && !out.isEmpty()) {
				ItemStack slot = slots.get(11);

				if(slot.isEmpty()) {
					slots.set(11, out.copy());
				} else if(ItemStack.isSameItemSameComponents(out, slot) && slot.getCount() + out.getCount() <= slot.getMaxStackSize()) {
					slot.grow(out.getCount());
				}
			}

			this.setChanged();
		}

		if(level.getGameTime() % 20 == 0) PollutionHandler.incrementPollution(level, worldPosition, PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * 5);
		this.power -= 5;
	}

	private void updateConnections() {
		for(DirPos pos : getConPos()) {
			this.trySubscribe(level, pos);
			this.trySubscribe(tanks[0].getTankType(), level, pos, pos.getDir());
		}
	}

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

	public long getPowerScaled(long i) {
		return (power * i) / maxPower;
	}

	@Override public void setPower(long i) { power = i; }
	@Override public long getPower() { return power; }
	@Override public long getMaxPower() { return maxPower; }

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 2, worldPosition.getY(), worldPosition.getZ() - 2, worldPosition.getX() + 3, worldPosition.getY() + 10, worldPosition.getZ() + 3);
	}

	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] { tanks[1], tanks[2], tanks[3], tanks[4] }; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { tanks[0] }; }
	@Override public FluidTank[] getAllTanks() { return tanks; }

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineRefinery(id, inv, this);
	}
}
