package com.hbm.tileentity.machine;

import com.hbm.blocks.machine.FoundryOutlet;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.util.CrucibleUtil;

import api.hbm.block.ICrucibleAcceptor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Foundry outlet: takes material from the channel behind it and pours it into whatever acceptor is up to 4 blocks
 * below. A material filter (set with scraps) and redstone (inverted by using it) decide what passes.
 */
public class TileEntityFoundryOutlet extends TileEntityFoundryBase {

	public NTMMaterial filter = null;
	/* inverts filter behavior, will let everything but the filter material pass */
	public boolean invertFilter = false;
	/** inverts redstone behavior, i.e. when TRUE, the outlet will be blocked by default and only open with redstone */
	public boolean invertRedstone = false;

	public TileEntityFoundryOutlet(BlockPos pos, BlockState state) {
		super(ModTileEntities.FOUNDRY_OUTLET.get(), pos, state);
	}

	/** if TRUE, prevents all fluids from flowing through the outlet and renders a small barrier */
	public boolean isClosed() {
		return invertRedstone ^ this.level.hasNeighborSignal(worldPosition);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if(isServer()) this.updateState();
	}

	/** The filter and lock shown on the model are block state properties */
	public void updateState() {
		BlockState state = getBlockState();
		if(!(state.getBlock() instanceof FoundryOutlet)) return;
		BlockState wanted = state.setValue(FoundryOutlet.FILTER, filter != null).setValue(FoundryOutlet.LOCK, isClosed());
		if(wanted != state) level.setBlock(worldPosition, wanted, 3);
	}

	private Direction getFacing() {
		return getBlockState().getValue(FoundryOutlet.FACING);
	}

	@Override public boolean canAcceptPartialPour(Level world, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) { return false; }
	@Override public MaterialStack pour(Level world, BlockPos pos, double dX, double dY, double dZ, Direction side, MaterialStack stack) { return stack; }

	@Override
	public boolean canAcceptPartialFlow(Level world, BlockPos pos, Direction side, MaterialStack stack) {
		if(filter != null && (filter != stack.material ^ invertFilter)) return false;
		if(isClosed()) return false;
		if(side != getFacing().getOpposite()) return false;

		BlockHitResult[] hit = new BlockHitResult[1];
		ICrucibleAcceptor acc = CrucibleUtil.getPouringTarget(world, new Vec3(pos.getX() + 0.5, pos.getY() - 0.125, pos.getZ() + 0.5), new Vec3(pos.getX() + 0.5, pos.getY() + 0.125 - 4, pos.getZ() + 0.5), hit);
		if(acc == null) return false;

		Vec3 loc = hit[0].getLocation();
		return acc.canAcceptPartialPour(world, hit[0].getBlockPos(), loc.x, loc.y, loc.z, Direction.UP, stack);
	}

	@Override
	public MaterialStack flow(Level world, BlockPos pos, Direction side, MaterialStack stack) {
		BlockHitResult[] hit = new BlockHitResult[1];
		ICrucibleAcceptor acc = CrucibleUtil.getPouringTarget(world, new Vec3(pos.getX() + 0.5, pos.getY() - 0.125, pos.getZ() + 0.5), new Vec3(pos.getX() + 0.5, pos.getY() + 0.125 - 4, pos.getZ() + 0.5), hit);
		if(acc == null) return stack;

		Vec3 loc = hit[0].getLocation();
		// TODO the original's pouring stream particle
		return acc.pour(world, hit[0].getBlockPos(), loc.x, loc.y, loc.z, Direction.UP, stack);
	}

	@Override
	public int getCapacity() {
		return 0;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.invertRedstone = nbt.getBoolean("invert");
		this.invertFilter = nbt.getBoolean("invertFilter");
		this.filter = Mats.matById.get((int) nbt.getShort("filter"));
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putBoolean("invert", this.invertRedstone);
		nbt.putBoolean("invertFilter", this.invertFilter);
		nbt.putShort("filter", this.filter == null ? -1 : (short) this.filter.id);
	}
}
