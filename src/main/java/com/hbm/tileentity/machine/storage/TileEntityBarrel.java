package com.hbm.tileentity.machine.storage;

import java.util.HashSet;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.BlockFluidBarrel;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.container.ContainerBarrel;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType;
import com.hbm.items.ModDataComponents;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.DirPos;

import api.hbm.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import api.hbm.fluidmk2.FluidNode;
import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Fluid barrel. Modes: 0 input, 1 buffer (acts like a pipe, providing and receiving), 2 output, 3 disabled.
 * Slots: 0/1 fluid identifier in/out, 2/3 containers to empty in/out, 4/5 containers to fill in/out.
 *
 * TODO comparator updates towards adjacent blocks (updateRedstoneConnection), Tom's firestorm water explosion,
 *  copy/paste settings, OpenComputers/ROR
 */
public class TileEntityBarrel extends TileEntityMachineBase implements IFluidStandardTransceiverMK2, IControlReceiver, MenuProvider {

	protected FluidNode node;
	protected FluidType lastType;

	public FluidTank tank;
	public short mode = 0;
	public static final short modes = 4;
	public int age = 0;
	public byte lastRedstone = 0;

	public TileEntityBarrel(BlockPos pos, BlockState state) {
		super(ModTileEntities.BARREL.get(), pos, state, 6);
		int capacity = state.getBlock() instanceof BlockFluidBarrel barrel ? barrel.capacity : 0;
		tank = new FluidTank(Fluids.NONE, capacity);
	}

	@Override
	public String getName() {
		return "container.barrel";
	}

	public byte getComparatorPower() {
		if(tank.getFill() == 0) return 0;
		double frac = (double) tank.getFill() / (double) tank.getMaxFill() * 15D;
		return (byte) (Mth.clamp((int) frac + 1, 0, 15));
	}

	@Override
	public long getDemand(FluidType type, int pressure) {
		if(this.tilted) return 0;
		if(this.mode == 2 || this.mode == 3) return 0;
		if(tank.getPressure() != pressure) return 0;
		return type == tank.getTankType() ? tank.getMaxFill() - tank.getFill() : 0;
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			byte comp = this.getComparatorPower(); //do comparator shenanigans
			if(comp != this.lastRedstone) {
				this.setChanged();
				level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
			}
			this.lastRedstone = comp;

			tank.setType(0, 1, slots);
			tank.loadTank(2, 3, slots);
			tank.unloadTank(4, 5, slots);

			// In buffer mode, acts like a pipe block, providing fluid to its own node
			// otherwise, it is a regular providing/receiving machine, blocking further propagation
			if(mode == 1) {
				if(this.node == null || this.node.expired || tank.getTankType() != lastType) {

					this.node = (FluidNode) UniNodespace.getNode(level, worldPosition, tank.getTankType().getNetworkProvider());

					if(this.node == null || this.node.expired || tank.getTankType() != lastType) {
						this.node = this.createNode(tank.getTankType());
						UniNodespace.createNode(level, this.node);
						lastType = tank.getTankType();
					}
				}

				if(node != null && node.hasValidNet()) {
					node.net.addProvider(this);
					node.net.addReceiver(this);
				}
			} else {
				if(this.node != null) {
					UniNodespace.destroyNode(level, worldPosition, tank.getTankType().getNetworkProvider());
					this.node = null;
				}

				if(!this.tilted) for(DirPos pos : getConPos()) {
					FluidNode dirNode = (FluidNode) UniNodespace.getNode(level, pos, tank.getTankType().getNetworkProvider());

					if(mode == 2) {
						tryProvide(tank, level, pos, pos.getDir());
					} else {
						if(dirNode != null && dirNode.hasValidNet()) dirNode.net.removeProvider(this);
					}

					if(mode == 0) {
						if(dirNode != null && dirNode.hasValidNet()) dirNode.net.addReceiver(this);
					} else {
						if(dirNode != null && dirNode.hasValidNet()) dirNode.net.removeReceiver(this);
					}
				}
			}

			if(tank.getFill() > 0) {
				checkFluidInteraction();
			}

			this.networkPackNT(50);
		}
	}

	protected FluidNode createNode(FluidType type) {
		DirPos[] conPos = getConPos();

		HashSet<BlockPos> posSet = new HashSet<>();
		posSet.add(worldPosition);
		for(DirPos pos : conPos) {
			posSet.add(pos.relative(pos.getDir().getOpposite()));
		}

		return new FluidNode(type.getNetworkProvider(), posSet.toArray(new BlockPos[posSet.size()])).setConnections(conPos);
	}

	@Override
	public void setRemoved() {
		super.setRemoved();

		if(isServer() && this.isLoaded && this.node != null) {
			UniNodespace.destroyNode(level, worldPosition, tank.getTankType().getNetworkProvider());
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeShort(mode);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		mode = buf.readShort();
		tank.deserialize(buf);
	}

	protected DirPos[] getConPos() {
		DirPos[] pos = new DirPos[6];
		for(Direction dir : Direction.values()) pos[dir.ordinal()] = new DirPos(worldPosition.relative(dir), dir);
		return pos;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		ItemStack full = FluidContainerRegistry.getFullContainer(itemStack, tank.getTankType());
		//if fillable and the fill being possible for this tank size
		if(i == 4 && full != null && FluidContainerRegistry.getFluidContent(full, tank.getTankType()) <= tank.getMaxFill())
			return true;
		int content = FluidContainerRegistry.getFluidContent(itemStack, tank.getTankType());
		//if content is above 0 but still within capacity
		if(i == 2 && content > 0 && content <= tank.getMaxFill())
			return true;

		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i == 3 || i == 5;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] {2, 3, 4, 5};
	}

	public void checkFluidInteraction() {

		Block b = this.getBlockState().getBlock();

		//for when you fill antimatter into a matter tank
		if(b != ModBlocks.barrel_antimatter.get() && tank.getTankType().isAntimatter()) {
			level.destroyBlock(worldPosition, false);
			level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 5, true, Level.ExplosionInteraction.BLOCK);
			return;
		}

		//for when you fill hot or corrosive liquids into a plastic tank
		if(b == ModBlocks.barrel_plastic.get() && (tank.getTankType().isCorrosive() || tank.getTankType().isHot())) {
			level.destroyBlock(worldPosition, false);
			level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
			return;
		}

		if(b == ModBlocks.barrel_corroded.get()) {
			if(level.random.nextInt(3) == 0) {
				tank.setFill(tank.getFill() - 1);
				FluidTrait.onRelease(level, worldPosition, tank.getTankType(), tank, FluidReleaseType.SPILL, 1);
			}
			if(level.random.nextInt(3 * 60 * 20) == 0) level.destroyBlock(worldPosition, false);
		}
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		mode = nbt.getShort("mode");
		tank.readFromNBT(nbt, "tank");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putShort("mode", mode);
		tank.writeToNBT(nbt, "tank");
	}

	/// IPersistentNBT: the tank and mode stay with the dropped barrel ///

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		if(tank.getFill() == 0) return;
		CompoundTag data = new CompoundTag();
		this.tank.writeToNBT(data, "tank");
		data.putShort("mode", mode);
		components.set(ModDataComponents.PERSISTENT.get(), CustomData.of(data));
	}

	@Override
	protected void applyImplicitComponents(DataComponentInput input) {
		super.applyImplicitComponents(input);
		CustomData data = input.get(ModDataComponents.PERSISTENT.get());
		if(data == null) return;
		CompoundTag nbt = data.copyTag();
		this.tank.readFromNBT(nbt, "tank");
		// the original read the mode from the key "nbt" (always 0), fixed here
		this.mode = nbt.getShort("mode");
	}

	@Override
	public boolean canConnect(FluidType fluid, Direction dir) {
		return fluid == tank.getTankType();
	}

	@Override public FluidTank[] getSendingTanks() { return (mode == 1 || mode == 2) ? new FluidTank[] {tank} : new FluidTank[0]; }
	@Override public FluidTank[] getReceivingTanks() { return (mode == 0 || mode == 1) ? new FluidTank[] {tank} : new FluidTank[0]; }
	@Override public FluidTank[] getAllTanks() { return new FluidTank[] { tank }; }

	@Override
	public ConnectionPriority getFluidPriority() {
		return mode == 1 ? ConnectionPriority.LOW : ConnectionPriority.NORMAL;
	}

	/// GUI: the mode button (the original's AuxButtonPacket) ///

	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("mode")) {
			mode = (short) ((mode + 1) % modes);
			this.markChanged();
		}
	}

	@Override
	public boolean hasPermission(Player player) {
		return this.stillValid(player);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerBarrel(id, inv, this);
	}
}
