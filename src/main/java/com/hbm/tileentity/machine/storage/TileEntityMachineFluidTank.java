package com.hbm.tileentity.machine.storage;

import java.util.HashSet;
import java.util.List;

import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerMachineFluidTank;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Corrosive;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Gaseous;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Gaseous_ART;
import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.inventory.fluid.trait.FluidTrait.FluidReleaseType;
import com.hbm.items.ModDataComponents;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.uninos.UniNodespace;
import api.hbm.fluidmk2.FluidNode;
import api.hbm.energymk2.IEnergyReceiverMK2.ConnectionPriority;
import com.hbm.util.DirPos;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Fluid tank, 256,000mB with the barrel's four modes (receive, buffer, provide, off). Explosions rupture it: it then
 * leaks, gases puff out, flammable contents burn. Slots: 0/1 identifier, 2/3 fill from containers, 4/5 fill containers.
 *
 * TODO repair with the blowtorch (IRepairable), side ladder, the NFPA diamonds on the render (DiamondPronter), OpenComputers
 */
public class TileEntityMachineFluidTank extends TileEntityMachineBase implements IFluidStandardTransceiverMK2, IControlReceiver, MenuProvider {

	protected FluidNode node;
	protected FluidType lastType;

	public FluidTank tank;
	public short mode = 0;
	public static final short modes = 4;
	public boolean hasExploded = false;
	public boolean onFire = false;
	public byte lastRedstone = 0;
	public Explosion lastExplosion = null;

	public int age = 0;

	public TileEntityMachineFluidTank(BlockPos pos, BlockState state) {
		this(ModTileEntities.FLUID_TANK.get(), pos, state, 256_000);
	}

	protected TileEntityMachineFluidTank(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos, BlockState state, int capacity) {
		super(type, pos, state, 6);
		tank = new FluidTank(Fluids.NONE, capacity);
	}

	@Override public long getReceiverSpeed(FluidType type, int pressure) { return Math.max(500, (tank.getMaxFill() - tank.getFill()) / 100); }
	@Override public long getProviderSpeed(FluidType type, int pressure) { return Math.max(500, tank.getFill() / 100); }

	@Override
	public String getName() {
		return "container.fluidtank";
	}

	public byte getComparatorPower() {
		if(tank.getFill() == 0) return 0;
		double frac = (double) tank.getFill() / (double) tank.getMaxFill() * 15D;
		return (byte) (Mth.clamp((int) frac + 1, 0, 15));
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			if(!hasExploded) {

				age++;

				if(age >= 20) {
					age = 0;
					this.markChanged();
				}

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

					for(DirPos pos : getConPos()) {
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

				tank.loadTank(2, 3, slots);
				tank.setType(0, 1, slots);

			} else if(this.node != null) {
				UniNodespace.destroyNode(level, worldPosition, tank.getTankType().getNetworkProvider());
				this.node = null;
			}

			byte comp = this.getComparatorPower(); //comparator shit
			if(comp != this.lastRedstone) {
				this.setChanged();
				for(DirPos pos : getConPos()) level.updateNeighbourForOutputSignal(pos.relative(pos.getDir().getOpposite()), getBlockState().getBlock());
			}
			this.lastRedstone = comp;

			if(tank.getFill() > 0) {

				if(tank.getTankType().isAntimatter()) {
					level.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 1.5, worldPosition.getZ() + 0.5, 5F, Level.ExplosionInteraction.NONE);
					this.explode();
					this.tank.setFill(0);
				}

				if(tank.getTankType().hasTrait(FT_Corrosive.class) && tank.getTankType().getTrait(FT_Corrosive.class).isHighlyCorrosive()) {
					this.explode();
				}

				if(this.hasExploded) {

					int leaking = 0;
					if(tank.getTankType().isAntimatter()) {
						leaking = tank.getFill();
					} else if(tank.getTankType().hasTrait(FT_Gaseous.class) || tank.getTankType().hasTrait(FT_Gaseous_ART.class)) {
						leaking = Math.min(tank.getFill(), tank.getMaxFill() / 100);
					} else {
						leaking = Math.min(tank.getFill(), tank.getMaxFill() / 10000);
					}

					updateLeak(leaking);
				}
			}

			tank.unloadTank(4, 5, slots);

			this.networkPackNT(150);
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
		buf.writeBoolean(hasExploded);
		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		mode = buf.readShort();
		hasExploded = buf.readBoolean();
		tank.deserialize(buf);
	}

	/** called when the tank breaks due to hazardous materials or external force, can be used to quickly void part of the tank or spawn a mushroom cloud */
	public void explode() {
		this.hasExploded = true;
		this.onFire = tank.getTankType().hasTrait(FT_Flammable.class);
		this.markChanged();
	}

	/** called every tick post explosion, used for leaking fluid and spawning particles */
	public void updateLeak(int amount) {
		if(!hasExploded) return;
		if(amount <= 0) return;

		this.tank.setFill(Math.max(0, this.tank.getFill() - amount));

		FluidType type = tank.getTankType();
		double x = worldPosition.getX(), y = worldPosition.getY(), z = worldPosition.getZ();

		if(type.hasTrait(FT_Flammable.class) && onFire) {
			List<Entity> affected = level.getEntitiesOfClass(Entity.class, new AABB(x - 1.5, y, z - 1.5, x + 2.5, y + 5, z + 2.5));
			for(Entity e : affected) e.igniteForSeconds(5);
			RandomSource rand = level.random;
			if(level instanceof ServerLevel server) {
				server.sendParticles(ParticleTypes.FLAME, x + rand.nextDouble(), y + 0.5 + rand.nextDouble(), z + rand.nextDouble(), 1, rand.nextGaussian() * 0.2, 0.1, rand.nextGaussian() * 0.2, 0.05);
			}

			if(level.getGameTime() % 5 == 0) {
				FluidTrait.onRelease(level, worldPosition, tank.getTankType(), tank, FluidReleaseType.BURN, amount * 5);
			}
		} else if(type.hasTrait(FT_Gaseous.class) || type.hasTrait(FT_Gaseous_ART.class)) {

			if(level.getGameTime() % 5 == 0 && level instanceof ServerLevel server) {
				CompoundTag data = new CompoundTag();
				data.putString("type", "tower");
				data.putFloat("lift", 1F);
				data.putFloat("base", 1F);
				data.putFloat("max", 5F);
				data.putInt("life", 100 + level.random.nextInt(20));
				data.putInt("color", tank.getTankType().getColor());
				AuxParticlePacketNT.sendToAllAround(server, data, x + 0.5, y + 1, z + 0.5, 150);
			}

			if(level.getGameTime() % 5 == 0) {
				FluidTrait.onRelease(level, worldPosition, tank.getTankType(), tank, FluidReleaseType.SPILL, amount * 5);
			}
		}
	}

	protected DirPos[] getConPos() {
		BlockPos p = worldPosition;
		return new DirPos[] {
				new DirPos(p.offset(2, 0, -1), Direction.EAST),
				new DirPos(p.offset(2, 0, 1), Direction.EAST),
				new DirPos(p.offset(-2, 0, -1), Direction.WEST),
				new DirPos(p.offset(-2, 0, 1), Direction.WEST),
				new DirPos(p.offset(-1, 0, 2), Direction.SOUTH),
				new DirPos(p.offset(1, 0, 2), Direction.SOUTH),
				new DirPos(p.offset(-1, 0, -2), Direction.NORTH),
				new DirPos(p.offset(1, 0, -2), Direction.NORTH)
		};
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 2, worldPosition.getY(), worldPosition.getZ() - 2, worldPosition.getX() + 3, worldPosition.getY() + 3, worldPosition.getZ() + 3);
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		mode = nbt.getShort("mode");
		tank.readFromNBT(nbt, "tank");
		hasExploded = nbt.getBoolean("exploded");
		onFire = nbt.getBoolean("onFire");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putShort("mode", mode);
		tank.writeToNBT(nbt, "tank");
		nbt.putBoolean("exploded", hasExploded);
		nbt.putBoolean("onFire", onFire);
	}

	/// IPersistentNBT: the contents and the damage stay with the dropped tank ///

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		if(tank.getFill() == 0 && !this.hasExploded) return;
		CompoundTag data = new CompoundTag();
		this.tank.writeToNBT(data, "tank");
		data.putShort("mode", mode);
		data.putBoolean("hasExploded", hasExploded);
		data.putBoolean("onFire", onFire);
		components.set(ModDataComponents.PERSISTENT.get(), CustomData.of(data));
	}

	@Override
	protected void applyImplicitComponents(DataComponentInput input) {
		super.applyImplicitComponents(input);
		CustomData data = input.get(ModDataComponents.PERSISTENT.get());
		if(data == null) return;
		CompoundTag nbt = data.copyTag();
		this.tank.readFromNBT(nbt, "tank");
		this.mode = nbt.getShort("mode");
		this.hasExploded = nbt.getBoolean("hasExploded");
		this.onFire = nbt.getBoolean("onFire");
	}

	@Override
	public long transferFluid(FluidType type, int pressure, long fluid) {
		long toTransfer = Math.min(getDemand(type, pressure), fluid);
		tank.setFill(tank.getFill() + (int) toTransfer);
		return fluid - toTransfer;
	}

	@Override
	public long getDemand(FluidType type, int pressure) {
		if(this.mode == 2 || this.mode == 3) return 0;
		if(tank.getPressure() != pressure) return 0;
		return type == tank.getTankType() ? tank.getMaxFill() - tank.getFill() : 0;
	}

	@Override public boolean canConnect(FluidType fluid, Direction dir) { return true; }

	@Override
	public FluidTank[] getAllTanks() {
		return new FluidTank[] { tank };
	}

	@Override
	public FluidTank[] getSendingTanks() {
		if(this.hasExploded) return new FluidTank[0];
		return (mode == 1 || mode == 2) ? new FluidTank[] {tank} : new FluidTank[0];
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		if(this.hasExploded) return new FluidTank[0];
		return (mode == 0 || mode == 1) ? new FluidTank[] {tank} : new FluidTank[0];
	}

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
		return new ContainerMachineFluidTank(id, inv, this);
	}
}
