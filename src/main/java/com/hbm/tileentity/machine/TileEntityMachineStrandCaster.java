package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.container.ContainerMachineStrandCaster;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.machine.ItemMold;
import com.hbm.items.machine.ItemMold.Mold;
import com.hbm.items.machine.ItemScraps;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.DirPos;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Strand caster: molten metal poured into the top is cast continuously with the installed mold, up to nine casts at
 * once, cooled by water (which comes out as spent steam). Slots: 0 mold, 1-6 output.
 * The original extended the foundry casting base, here it's a machine with the casting logic of its own.
 */
public class TileEntityMachineStrandCaster extends TileEntityMachineBase implements IFluidStandardTransceiverMK2, MenuProvider {

	public FluidTank water;
	public FluidTank steam;

	public NTMMaterial type;
	public int amount;
	private NTMMaterial lastType;
	private int lastAmount;

	private long lastProgressTick = 0;

	public TileEntityMachineStrandCaster(BlockPos pos, BlockState state) {
		super(ModTileEntities.STRAND_CASTER.get(), pos, state, 7);
		water = new FluidTank(Fluids.WATER, 64_000);
		steam = new FluidTank(Fluids.SPENTSTEAM, 64_000);
	}

	@Override
	public String getName() {
		return "container.machineStrandCaster";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			if(this.lastType != this.type || this.lastAmount != this.amount) {
				this.setChanged();
				this.lastType = this.type;
				this.lastAmount = this.amount;
			}

			// In case of overfill problems, spit out the excess as scrap
			if(amount > getCapacity() && type != null) {
				ItemStack scrap = ItemScraps.create(new MaterialStack(type, Math.max(amount - getCapacity(), 0)));
				level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 2, worldPosition.getZ() + 0.5, scrap));
				this.amount = this.getCapacity();
			}

			if(this.amount == 0) this.type = null;

			this.updateConnections();

			int moldsToCast = maxProcessable();

			// Makes it flush the buffers after 10 seconds of inactivity, or when they're full
			if(moldsToCast > 0 && (moldsToCast >= 9 || level.getGameTime() >= lastProgressTick + 200)) {

				Mold mold = this.getInstalledMold();
				this.amount -= moldsToCast * mold.getCost();

				ItemStack out = mold.getOutput(type);
				int remaining = out.getCount() * moldsToCast;
				final int maxStackSize = out.getMaxStackSize();

				for(int i = 1; i < 7; i++) {
					if(remaining <= 0) break;

					if(slots.get(i).isEmpty()) {
						int toDeposit = Math.min(remaining, maxStackSize);
						slots.set(i, out.copyWithCount(toDeposit));
						remaining -= toDeposit;
					} else if(ItemStack.isSameItemSameComponents(slots.get(i), out)) {
						int toDeposit = Math.min(remaining, maxStackSize - slots.get(i).getCount());
						slots.get(i).grow(toDeposit);
						remaining -= toDeposit;
					}
				}

				this.setChanged();
				water.setFill(water.getFill() - getWaterRequired() * moldsToCast);
				steam.setFill(steam.getFill() + getWaterRequired() * moldsToCast);
				lastProgressTick = level.getGameTime();
			}

			this.networkPackNT(150);
		}
	}

	/** How many casts fit into the output, the water and the steam tank */
	private int maxProcessable() {
		Mold mold = this.getInstalledMold();
		if(type == null || mold == null || mold.getOutput(type) == null) return 0;

		ItemStack out = mold.getOutput(type);
		int freeSlots = 0;
		final int stackLimit = out.getMaxStackSize();

		for(int i = 1; i < 7; i++) {
			if(slots.get(i).isEmpty()) {
				freeSlots += stackLimit;
			} else if(ItemStack.isSameItemSameComponents(slots.get(i), out)) {
				freeSlots += stackLimit - slots.get(i).getCount();
			}
		}

		int moldsToCast = amount / mold.getCost();
		moldsToCast = Math.min(moldsToCast, freeSlots / out.getCount());
		moldsToCast = Math.min(moldsToCast, water.getFill() / getWaterRequired());
		moldsToCast = Math.min(moldsToCast, (steam.getMaxFill() - steam.getFill()) / getWaterRequired());
		return moldsToCast;
	}

	public DirPos[] getFluidConPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		BlockPos p = worldPosition;
		return new DirPos[] {
				new DirPos(p.relative(rot, 2).relative(dir, -1), rot),
				new DirPos(p.relative(rot, -1).relative(dir, -1), rot.getOpposite()),
				new DirPos(p.relative(rot, 2).relative(dir, -5), rot),
				new DirPos(p.relative(rot, -1).relative(dir, -5), rot.getOpposite())
		};
	}

	/** The four blocks on top the metal can be poured into */
	public BlockPos[] getMetalPourPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		BlockPos p = worldPosition.above(2);
		return new BlockPos[] {
				p.relative(rot).relative(dir, -1),
				p.relative(dir, -1),
				p.relative(rot),
				p
		};
	}

	public Mold getInstalledMold() {
		return ItemMold.getMold(slots.get(0));
	}

	/** A mold's worth of metal times ten, or a generous buffer without a mold */
	public int getCapacity() {
		Mold mold = this.getInstalledMold();
		return mold == null ? 50000 : mold.getCost() * 10;
	}

	private int getWaterRequired() {
		return getInstalledMold() != null ? 5 * getInstalledMold().getCost() : 50;
	}

	private void updateConnections() {
		for(DirPos pos : getFluidConPos()) {
			this.trySubscribe(water.getTankType(), level, pos, pos.getDir());
			if(steam.getFill() > 0) this.tryProvide(steam, level, pos);
		}
	}

	/// POURING (the block forwards the pours hitting the top ports) ///

	public boolean canAcceptPartialPour(BlockPos pos, Direction side, MaterialStack stack) {
		if(side != Direction.UP) return false;
		for(BlockPos port : getMetalPourPos()) {
			if(port.equals(pos)) return this.standardCheck(stack);
		}
		return false;
	}

	public boolean standardCheck(MaterialStack stack) {
		if(this.type != null && this.type != stack.material) return false;
		Mold mold = this.getInstalledMold();
		if(mold == null || mold.getOutput(stack.material) == null) return false;
		return this.amount < mold.getCost() * 9;
	}

	/** Adds as much as fits (up to nine casts), returns the rest or null */
	public MaterialStack standardAdd(Level world, MaterialStack stack) {
		this.type = stack.material;
		int limit = this.getInstalledMold() != null ? this.getInstalledMold().getCost() * 9 : this.getCapacity();

		if(stack.amount + this.amount <= limit) {
			this.amount += stack.amount;
			return null;
		}

		int required = limit - this.amount;
		this.amount = limit;
		stack.amount -= required;
		lastProgressTick = world.getGameTime();
		return stack;
	}

	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] { steam }; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] { water }; }
	@Override public FluidTank[] getAllTanks() { return new FluidTank[] { water, steam }; }

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		water.serialize(buf);
		steam.serialize(buf);
		buf.writeInt(type == null ? -1 : type.id);
		buf.writeInt(amount);
		// the mold is needed on the client for the renderer and the look overlay, every mold is its own item
		buf.writeInt(slots.get(0).isEmpty() ? -1 : net.minecraft.core.registries.BuiltInRegistries.ITEM.getId(slots.get(0).getItem()));
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		water.deserialize(buf);
		steam.deserialize(buf);
		int id = buf.readInt();
		this.type = id == -1 ? null : Mats.matById.get(id);
		this.amount = buf.readInt();
		int mold = buf.readInt();
		slots.set(0, mold == -1 ? ItemStack.EMPTY : new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.byId(mold)));
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		water.writeToNBT(nbt, "w");
		steam.writeToNBT(nbt, "s");
		nbt.putLong("t", lastProgressTick);
		if(type != null) nbt.putInt("type", type.id);
		nbt.putInt("amount", amount);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		water.readFromNBT(nbt, "w");
		steam.readFromNBT(nbt, "s");
		lastProgressTick = nbt.getLong("t");
		this.type = nbt.contains("type") ? Mats.matById.get(nbt.getInt("type")) : null;
		this.amount = nbt.getInt("amount");
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return i == 0 && ItemMold.getMold(stack) != null;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] { 1, 2, 3, 4, 5, 6 };
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, Direction side) {
		return slot != 0;
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 7, worldPosition.getY(), worldPosition.getZ() - 7, worldPosition.getX() + 7, worldPosition.getY() + 3, worldPosition.getZ() + 7);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineStrandCaster(id, inv, this);
	}
}
