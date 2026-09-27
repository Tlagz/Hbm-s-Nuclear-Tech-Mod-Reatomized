package com.hbm.tileentity.machine;

import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerMachineRockMill;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.loader.GenericRecipe;
import com.hbm.items.ModItems;
import com.hbm.lib.Library;
import com.hbm.module.machine.ModuleMachineRockMill;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.DirPos;

import api.hbm.energymk2.IBatteryItem;
import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Rock mill: crushes stone with a big wheel, recipes picked in the recipe selector.
 * Slots: 0 battery, 1 blueprint, 2-4 inputs, 5-7 outputs.
 */
public class TileEntityMachineRockMill extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiverMK2, IControlReceiver, MenuProvider {

	public FluidTank[] inputTanks;
	public FluidTank[] outputTanks;

	public long power;
	public long maxPower = 2_500;
	public boolean didProcess = false;

	public float rotation;
	public float prevRotation;
	public float rotationSpeed = 0F;
	public static final float ACCELERATION = 0.1F;
	public static final float MAX_SPEED = 15F;

	public boolean frame = false;

	public ModuleMachineRockMill rockMillModule;

	public TileEntityMachineRockMill(BlockPos pos, BlockState state) {
		super(ModTileEntities.ROCK_MILL.get(), pos, state, 8);

		this.inputTanks = new FluidTank[1];
		this.outputTanks = new FluidTank[1];
		this.inputTanks[0] = new FluidTank(Fluids.NONE, 4_000);
		this.outputTanks[0] = new FluidTank(Fluids.NONE, 4_000);

		this.rockMillModule = new ModuleMachineRockMill(0, this, slots)
				.itemInput(2).itemOutput(5)
				.fluidInput(inputTanks[0]).fluidOutput(outputTanks[0]);
	}

	@Override
	public String getName() {
		return "container.machineRockMill";
	}

	/** The block the current recipe crushes, for the sounds and the dust */
	private Block getRecipeBlock(GenericRecipe recipe, Block fallback) {
		if(recipe != null && recipe.getIcon().getItem() instanceof BlockItem blockItem) return blockItem.getBlock();
		return fallback;
	}

	@Override
	public void updateEntity() {

		if(maxPower <= 0) this.maxPower = 2_500;

		if(isServer()) {

			GenericRecipe recipe = rockMillModule.getRecipe();
			if(recipe != null) {
				this.maxPower = recipe.power * 100;
			}
			this.maxPower = BobMathUtil.max(this.power, this.maxPower, 2_500);

			this.power = Library.chargeTEFromItems(slots, 0, power, maxPower);

			for(DirPos pos : getConPos()) {
				this.trySubscribe(level, pos);
				for(FluidTank tank : inputTanks) if(tank.getTankType() != Fluids.NONE) this.trySubscribe(tank.getTankType(), level, pos);
				for(FluidTank tank : outputTanks) if(tank.getFill() > 0) this.tryProvide(tank, level, pos);
			}

			this.rockMillModule.update(1D, 1D, true, slots.get(1));
			this.didProcess = this.rockMillModule.didProcess;
			if(this.rockMillModule.markDirty) this.setChanged();

			if(this.didProcess && (level.getGameTime() + worldPosition.hashCode()) % 3 == 0) {
				Block block = getRecipeBlock(recipe, Blocks.STONE);
				level.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 1.5, worldPosition.getZ() + 0.5, block.defaultBlockState().getSoundType().getStepSound(), SoundSource.BLOCKS, this.getVolume(1.0F), 0.75F);
			}

			this.networkPackNT(100);

		} else {

			this.prevRotation = this.rotation;
			this.rotationSpeed += ACCELERATION * (this.didProcess ? 1 : -1);
			this.rotationSpeed = Mth.clamp(this.rotationSpeed, 0F, MAX_SPEED);
			this.rotation += this.rotationSpeed;

			if(this.rotation >= 360F) {
				this.prevRotation -= 360F;
				this.rotation -= 360F;
			}

			if(level.getGameTime() % 20 == 0) {
				frame = !level.getBlockState(worldPosition.above(3)).isAir();
			}

			// dust of the crushed block flying off the wheel
			if(this.didProcess) {
				Block block = getRecipeBlock(rockMillModule.getRecipe(), Blocks.GRAVEL);
				double angle = level.random.nextDouble() * Math.PI * 2;
				double vx = Math.cos(angle);
				double vz = Math.sin(angle);
				double speed = 0.125D;
				level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, block.defaultBlockState()),
						worldPosition.getX() + 0.5 + vx * 2.25, worldPosition.getY() + 1.5, worldPosition.getZ() + 0.5 + vz * 2.25,
						vx * speed, -0.1D, vz * speed);
			}
		}
	}

	public DirPos[] getConPos() {
		BlockPos p = worldPosition;
		return new DirPos[] {
				new DirPos(p.offset(3, 0, 1), Direction.EAST),
				new DirPos(p.offset(3, 0, -1), Direction.EAST),
				new DirPos(p.offset(-3, 0, 1), Direction.WEST),
				new DirPos(p.offset(-3, 0, -1), Direction.WEST),
				new DirPos(p.offset(1, 0, 3), Direction.SOUTH),
				new DirPos(p.offset(-1, 0, 3), Direction.SOUTH),
				new DirPos(p.offset(1, 0, -3), Direction.NORTH),
				new DirPos(p.offset(-1, 0, -3), Direction.NORTH),
		};
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		for(FluidTank tank : inputTanks) tank.serialize(buf);
		for(FluidTank tank : outputTanks) tank.serialize(buf);
		buf.writeLong(power);
		buf.writeLong(maxPower);
		buf.writeBoolean(didProcess);
		this.rockMillModule.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		for(FluidTank tank : inputTanks) tank.deserialize(buf);
		for(FluidTank tank : outputTanks) tank.deserialize(buf);
		this.power = buf.readLong();
		this.maxPower = buf.readLong();
		this.didProcess = buf.readBoolean();
		this.rockMillModule.deserialize(buf);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.inputTanks[0].readFromNBT(nbt, "i" + 0);
		this.outputTanks[0].readFromNBT(nbt, "o" + 0);
		this.power = nbt.getLong("power");
		this.maxPower = nbt.getLong("maxPower");
		this.rockMillModule.readFromNBT(nbt);
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		this.inputTanks[0].writeToNBT(nbt, "i" + 0);
		this.outputTanks[0].writeToNBT(nbt, "o" + 0);
		nbt.putLong("power", power);
		nbt.putLong("maxPower", maxPower);
		this.rockMillModule.writeToNBT(nbt);
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == 0) return stack.getItem() instanceof IBatteryItem; // battery
		if(slot == 1 && stack.is(ModItems.blueprints.get())) return true;
		if(this.rockMillModule.isItemValid(slot, stack)) return true; // recipe input crap
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return (i >= 5 && i <= 7) || this.rockMillModule.isSlotClogged(i);
	}

	private static final int[] slot_access = new int[] {2, 3, 4, 5, 6, 7};

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return slot_access;
	}

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override public FluidTank[] getReceivingTanks() { return inputTanks; }
	@Override public FluidTank[] getSendingTanks() { return outputTanks; }
	@Override public FluidTank[] getAllTanks() { return new FluidTank[] {inputTanks[0], outputTanks[0]}; }

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineRockMill(id, inv, this);
	}

	@Override public boolean hasPermission(Player player) { return this.stillValid(player); }

	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("index") && data.contains("selection")) {
			int index = data.getInt("index");
			String selection = data.getString("selection");
			if(index == 0) {
				this.rockMillModule.setRecipe(selection, false);
				this.markChanged();
			}
		}
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 2, worldPosition.getY(), worldPosition.getZ() - 2, worldPosition.getX() + 3, worldPosition.getY() + 3, worldPosition.getZ() + 3);
	}
}
