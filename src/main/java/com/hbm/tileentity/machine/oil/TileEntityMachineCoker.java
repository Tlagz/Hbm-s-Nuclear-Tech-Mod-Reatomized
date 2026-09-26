package com.hbm.tileentity.machine.oil;

import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.container.ContainerMachineCoker;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.CokerRecipes;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.DirPos;
import com.hbm.util.Tuple.Triplet;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Coker unit: heat from below slowly cooks a fluid into coke and a lighter byproduct. Slots: 0 fluid identifier,
 * 1 output. 20,000 TU per operation, every tick a hundredth of the stored heat is used.
 *
 * TODO copy tool, OpenComputers
 */
public class TileEntityMachineCoker extends TileEntityMachineBase implements IFluidStandardTransceiverMK2, MenuProvider {

	public boolean wasOn;
	public int progress;
	public static int processTime = 20_000;

	public int heat;
	public static int maxHeat = 100_000;
	public static double diffusion = 0.25D;

	public FluidTank[] tanks;

	public TileEntityMachineCoker(BlockPos pos, BlockState state) {
		super(ModTileEntities.COKER.get(), pos, state, 2);
		tanks = new FluidTank[2];
		tanks[0] = new FluidTank(Fluids.HEAVYOIL, 16_000);
		tanks[1] = new FluidTank(Fluids.OIL_COKER, 8_000);
	}

	@Override
	public String getName() {
		return "container.machineCoker";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.tryPullHeat();
			this.tanks[0].setType(0, slots);

			if(level.getGameTime() % 20 == 0) {
				for(DirPos pos : getConPos()) {
					this.trySubscribe(tanks[0].getTankType(), level, pos, pos.getDir());
				}
			}

			this.wasOn = false;

			if(canProcess()) {
				int burn = heat / 100;

				if(burn > 0) {
					this.wasOn = true;
					this.progress += burn;
					this.heat -= burn;

					if(progress >= processTime) {
						this.setChanged();
						progress -= processTime;

						Triplet<Integer, ItemStack, FluidStack> recipe = CokerRecipes.getOutput(tanks[0].getTankType());
						int fillReq = recipe.getX();
						ItemStack output = recipe.getY();
						FluidStack byproduct = recipe.getZ();

						if(output != null) {
							if(slots.get(1).isEmpty()) {
								slots.set(1, output.copy());
							} else {
								slots.get(1).grow(output.getCount());
							}
						}

						if(byproduct != null) {
							tanks[1].setFill(tanks[1].getFill() + byproduct.fill);
						}

						tanks[0].setFill(tanks[0].getFill() - fillReq);
					}
				}

				if(wasOn && level.getGameTime() % 5 == 0) PollutionHandler.incrementPollution(level, worldPosition, PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * 5);
			}

			for(DirPos pos : getConPos()) {
				if(this.tanks[1].getFill() > 0) this.tryProvide(tanks[1], level, pos);
			}

			this.networkPackNT(25);

		} else {

			if(this.wasOn && level.getGameTime() % 2 == 0) {
				CompoundTag fx = new CompoundTag();
				fx.putString("type", "tower");
				fx.putFloat("lift", 10F);
				fx.putFloat("base", 0.75F);
				fx.putFloat("max", 3F);
				fx.putInt("life", 200 + level.random.nextInt(50));
				fx.putInt("color", 0x404040);
				fx.putDouble("posX", worldPosition.getX() + 0.5);
				fx.putDouble("posY", worldPosition.getY() + 22);
				fx.putDouble("posZ", worldPosition.getZ() + 0.5);
				com.hbm.particle.ParticleEffectsNT.effectNT(fx);
			}
		}
	}

	/** Two ports next to each corner extra */
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

	public boolean canProcess() {
		Triplet<Integer, ItemStack, FluidStack> recipe = CokerRecipes.getOutput(tanks[0].getTankType());

		if(recipe == null) return false;

		int fillReq = recipe.getX();
		ItemStack output = recipe.getY();
		FluidStack byproduct = recipe.getZ();

		if(byproduct != null) tanks[1].setTankType(byproduct.type);

		if(tanks[0].getFill() < fillReq) return false;
		if(byproduct != null && byproduct.fill + tanks[1].getFill() > tanks[1].getMaxFill()) return false;

		ItemStack slot = slots.get(1);
		if(output != null && !slot.isEmpty()) {
			if(!ItemStack.isSameItemSameComponents(output, slot)) return false;
			if(output.getCount() + slot.getCount() > output.getMaxStackSize()) return false;
		}

		return true;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(this.wasOn);
		buf.writeInt(this.heat);
		buf.writeInt(this.progress);
		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.wasOn = buf.readBoolean();
		this.heat = buf.readInt();
		this.progress = buf.readInt();
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
	}

	protected void tryPullHeat() {

		if(this.heat >= maxHeat) return;

		if(level.getBlockEntity(worldPosition.below()) instanceof IHeatSource source) {
			int diff = source.getHeatStored() - this.heat;

			if(diff == 0) {
				return;
			}

			if(diff > 0) {
				diff = (int) Math.ceil(diff * diffusion);
				source.useUpHeat(diff);
				this.heat += diff;
				if(this.heat > maxHeat)
					this.heat = maxHeat;
				return;
			}
		}

		this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, Direction side) {
		return true;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] { 1 };
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.tanks[0].readFromNBT(nbt, "t0");
		this.tanks[1].readFromNBT(nbt, "t1");
		this.progress = nbt.getInt("prog");
		this.heat = nbt.getInt("heat");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		this.tanks[0].writeToNBT(nbt, "t0");
		this.tanks[1].writeToNBT(nbt, "t1");
		nbt.putInt("prog", progress);
		nbt.putInt("heat", heat);
	}

	@Override
	public FluidTank[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTank[] getSendingTanks() {
		return new FluidTank[] { tanks[1] };
	}

	@Override
	public FluidTank[] getReceivingTanks() {
		return new FluidTank[] { tanks[0] };
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 2, worldPosition.getY(), worldPosition.getZ() - 2, worldPosition.getX() + 3, worldPosition.getY() + 23, worldPosition.getZ() + 3);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineCoker(id, inv, this);
	}
}
