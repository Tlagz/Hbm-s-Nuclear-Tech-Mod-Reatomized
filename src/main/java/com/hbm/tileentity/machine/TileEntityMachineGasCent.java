package com.hbm.tileentity.machine;

import java.nio.charset.StandardCharsets;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.container.ContainerMachineGasCent;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.GasCentrifugeRecipes;
import com.hbm.inventory.recipes.GasCentrifugeRecipes.PseudoFluidType;
import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.Library;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.DirPos;
import com.hbm.util.InventoryUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import io.netty.buffer.ByteBuf;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Gas centrifuge: the fluid is turned into a pseudo fluid (enrichment stage), every stage leaves items behind and
 * passes the rest to the centrifuge behind it (cascades). The last LEU centrifuge of a cascade turns its output into
 * uranium fuel. Slots: 0-3 output, 4 battery, 5 fluid identifier, 6 GC speed upgrade.
 */
public class TileEntityMachineGasCent extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardReceiverMK2, MenuProvider {

	public long power;
	public int progress;
	public boolean isProgressing;
	public static final int maxPower = 100000;
	public static final int processingSpeed = 150;

	public FluidTank tank;
	public PseudoFluidTank inputTank;
	public PseudoFluidTank outputTank;

	private int audioDuration = 0;
	private AudioWrapper audio;

	private static final int[] slots_io = new int[] { 0, 1, 2, 3 };

	public TileEntityMachineGasCent(BlockPos pos, BlockState state) {
		super(ModTileEntities.GAS_CENT.get(), pos, state, 7);
		tank = new FluidTank(Fluids.UF6, 2000);
		inputTank = new PseudoFluidTank(PseudoFluidType.NUF6, 8000);
		outputTank = new PseudoFluidTank(PseudoFluidType.LEUF6, 8000);
	}

	@Override
	public String getName() {
		return "container.gasCentrifuge";
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i < 4;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return slots_io;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		power = nbt.getLong("power");
		progress = nbt.getShort("progress");
		tank.readFromNBT(nbt, "tank");
		inputTank.readFromNBT(nbt, "inputTank");
		outputTank.readFromNBT(nbt, "outputTank");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("power", power);
		nbt.putShort("progress", (short) progress);
		tank.writeToNBT(nbt, "tank");
		inputTank.writeToNBT(nbt, "inputTank");
		outputTank.writeToNBT(nbt, "outputTank");
	}

	public int getCentrifugeProgressScaled(int i) {
		return (progress * i) / getProcessingSpeed();
	}

	public long getPowerRemainingScaled(int i) {
		return (power * i) / maxPower;
	}

	private boolean hasSpeedUpgrade() {
		return slots.get(6).is(ModItems.upgrade_gc_speed.get());
	}

	private boolean canEnrich() {
		if(power > 0 && this.inputTank.getFill() >= inputTank.getTankType().getFluidConsumed() && this.outputTank.getFill() + this.inputTank.getTankType().getFluidProduced() <= outputTank.getMaxFill()) {

			ItemStack[] list = inputTank.getTankType().getOutput();

			if(this.inputTank.getTankType().getIfHighSpeed() && !hasSpeedUpgrade()) return false;
			if(list == null || list.length < 1) return false;

			return InventoryUtil.doesArrayHaveSpace(slots, 0, 3, list);
		}

		return false;
	}

	private void enrich() {
		ItemStack[] output = inputTank.getTankType().getOutput();

		this.progress = 0;
		inputTank.setFill(inputTank.getFill() - inputTank.getTankType().getFluidConsumed());
		outputTank.setFill(outputTank.getFill() + inputTank.getTankType().getFluidProduced());

		for(ItemStack stack : output) InventoryUtil.tryAddItemToInventory(slots, 0, 3, stack);
	}

	private void attemptConversion() {
		if(inputTank.getFill() < inputTank.getMaxFill() && tank.getFill() > 0) {
			int fill = Math.min(inputTank.getMaxFill() - inputTank.getFill(), tank.getFill());
			tank.setFill(tank.getFill() - fill);
			inputTank.setFill(inputTank.getFill() + fill);
		}
	}

	/** Moves the output stage into the centrifuge behind this one, false if there is none */
	private boolean attemptTransfer(BlockEntity te) {

		if(te instanceof TileEntityMachineGasCent cent) {

			if(cent.tank.getTankType() == tank.getTankType()) {

				if(cent.inputTank.getTankType() != outputTank.getTankType() && outputTank.getTankType() != PseudoFluidType.NONE) {
					cent.inputTank.setTankType(outputTank.getTankType());
					cent.outputTank.setTankType(outputTank.getTankType().getOutputType());
				}

				if(cent.inputTank.getFill() < cent.inputTank.getMaxFill() && outputTank.getFill() > 0) {
					int fill = Math.min(cent.inputTank.getMaxFill() - cent.inputTank.getFill(), outputTank.getFill());
					outputTank.setFill(outputTank.getFill() - fill);
					cent.inputTank.setFill(cent.inputTank.getFill() + fill);
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			updateConnections();

			power = Library.chargeTEFromItems(slots, 4, power, maxPower);
			setTankType(5);

			if(GasCentrifugeRecipes.fluidConversions.containsValue(inputTank.getTankType())) {
				attemptConversion();
			}

			if(canEnrich()) {

				isProgressing = true;
				this.progress++;

				this.power -= hasSpeedUpgrade() ? 300 : 200;

				if(this.power < 0) {
					power = 0;
					this.progress = 0;
				}

				if(progress >= getProcessingSpeed())
					enrich();

			} else {
				isProgressing = false;
				this.progress = 0;
			}

			if(level.getGameTime() % 10 == 0) {
				Direction dir = BlockDummyable.getRotation(getBlockState());
				BlockEntity te = level.getBlockEntity(worldPosition.relative(dir, -1));

				if(!attemptTransfer(te) && this.inputTank.getTankType() == PseudoFluidType.LEUF6) {
					ItemStack[] converted = new ItemStack[] { new ItemStack(ModItems.nugget_uranium_fuel.get(), 6), new ItemStack(ModItems.fluorite.get()) };

					if(this.outputTank.getFill() >= 600 && InventoryUtil.doesArrayHaveSpace(slots, 0, 3, converted)) {
						this.outputTank.setFill(this.outputTank.getFill() - 600);
						for(ItemStack stack : converted) InventoryUtil.tryAddItemToInventory(slots, 0, 3, stack);
					}
				}
			}

			this.networkPackNT(50);

		} else {

			if(isProgressing) {
				audioDuration += 2;
			} else {
				audioDuration -= 3;
			}

			audioDuration = Mth.clamp(audioDuration, 0, 60);

			if(audioDuration > 10 && com.hbm.main.ClientHooks.distanceToPlayer(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) < 25) {

				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}

				audio.updateVolume(getVolume(1F));
				audio.updatePitch((audioDuration - 10) / 100F + 0.5F);
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
		return AudioWrapper.getLoopedSound("hbm:block.centrifugeOperate", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), 1.0F, 10F, 1.0F, 20);
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

	private static void writeString(ByteBuf buf, String s) {
		byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
		buf.writeInt(bytes.length);
		buf.writeBytes(bytes);
	}

	private static String readString(ByteBuf buf) {
		byte[] bytes = new byte[buf.readInt()];
		buf.readBytes(bytes);
		return new String(bytes, StandardCharsets.UTF_8);
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(progress);
		buf.writeBoolean(isProgressing);
		buf.writeInt(inputTank.getFill());
		buf.writeInt(outputTank.getFill());
		writeString(buf, inputTank.getTankType().name);
		writeString(buf, outputTank.getTankType().name);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		progress = buf.readInt();
		isProgressing = buf.readBoolean();
		int inFill = buf.readInt();
		int outFill = buf.readInt();
		inputTank.setTankType(PseudoFluidType.types.get(readString(buf)));
		outputTank.setTankType(PseudoFluidType.types.get(readString(buf)));
		inputTank.setFill(inFill);
		outputTank.setFill(outFill);
		tank.deserialize(buf);
	}

	private void updateConnections() {
		for(DirPos pos : getConPos()) {
			this.trySubscribe(level, pos, pos.getDir());

			if(GasCentrifugeRecipes.fluidConversions.containsValue(inputTank.getTankType())) {
				this.trySubscribe(tank.getTankType(), level, pos, pos.getDir());
			}
		}
	}

	private DirPos[] getConPos() {
		BlockPos p = worldPosition;
		return new DirPos[] {
			new DirPos(p.below(), Direction.DOWN),
			new DirPos(p.east(), Direction.EAST),
			new DirPos(p.west(), Direction.WEST),
			new DirPos(p.south(), Direction.SOUTH),
			new DirPos(p.north(), Direction.NORTH)
		};
	}

	@Override public void setPower(long i) { power = i; }
	@Override public long getPower() { return power; }
	@Override public long getMaxPower() { return maxPower; }

	public int getProcessingSpeed() {
		return hasSpeedUpgrade() ? processingSpeed - 70 : processingSpeed;
	}

	/** The fluid identifier picks the input fluid, which also resets the stages */
	public void setTankType(int in) {

		if(slots.get(in).getItem() instanceof IItemFluidIdentifier id) {
			FluidType newType = id.getType(level, worldPosition, slots.get(in));

			if(tank.getTankType() != newType) {
				PseudoFluidType pseudo = GasCentrifugeRecipes.fluidConversions.get(newType);

				if(pseudo != null) {
					inputTank.setTankType(pseudo);
					outputTank.setTankType(pseudo.getOutputType());
					tank.setTankType(newType);
				}
			}
		}
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] { tank };
	}

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] { tank };
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), worldPosition.getX() + 1, worldPosition.getY() + 5, worldPosition.getZ() + 1);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineGasCent(id, inv, this);
	}

	public static class PseudoFluidTank {

		PseudoFluidType type;
		int fluid;
		int maxFluid;

		public PseudoFluidTank(PseudoFluidType type, int maxFluid) {
			this.type = type;
			this.maxFluid = maxFluid;
		}

		public void setFill(int i) {
			fluid = i;
		}

		public void setTankType(PseudoFluidType type) {
			if(type == null) type = PseudoFluidType.NONE;
			if(this.type.equals(type)) return;
			this.type = type;
			this.setFill(0);
		}

		public PseudoFluidType getTankType() {
			return type;
		}

		public int getFill() {
			return fluid;
		}

		public int getMaxFill() {
			return maxFluid;
		}

		public void writeToNBT(CompoundTag nbt, String s) {
			nbt.putInt(s, fluid);
			nbt.putInt(s + "_max", maxFluid);
			nbt.putString(s + "_type", type.name);
		}

		public void readFromNBT(CompoundTag nbt, String s) {
			fluid = nbt.getInt(s);
			int max = nbt.getInt(s + "_max");
			if(max > 0) maxFluid = max;
			type = PseudoFluidType.types.get(nbt.getString(s + "_type"));
			if(type == null) type = PseudoFluidType.NONE;
		}
	}
}
