package com.hbm.tileentity.network;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.network.CraneInserter;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.interfaces.IControlReceiverFilter;
import com.hbm.inventory.container.ContainerCraneGrabber;
import com.hbm.module.ModulePatternMatcher;
import com.hbm.tileentity.ModTileEntities;

import api.hbm.conveyor.IConveyorBelt;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Grabber: pulls matching items off the belt in front of its input side, into the inventory at its output side or
 * onto the belt there. Slots: 0-8 filter, 9 stack upgrade, 10 ejector upgrade.
 */
public class TileEntityCraneGrabber extends TileEntityCraneBase implements IControlReceiverFilter, MenuProvider {

	public boolean isWhitelist = false;
	public ModulePatternMatcher matcher;
	public long lastGrabbedTick = 0;

	public TileEntityCraneGrabber(BlockPos pos, BlockState state) {
		super(ModTileEntities.CRANE_GRABBER.get(), pos, state, 11);
		this.matcher = new ModulePatternMatcher(9);
	}

	@Override
	public void nextMode(int i) {
		this.matcher.nextMode(level, slots.get(i), i);
	}

	@Override
	public String getName() {
		return "container.craneGrabber";
	}

	/** The area it grabs from, closer for multi-lane belts so it only takes one lane */
	private AABB getGrabArea() {
		Direction inputSide = getInputSide();
		double reach = 1D;
		if(isHorizontal()) { //ignore if pointing up or down
			Block b = level.getBlockState(worldPosition.relative(inputSide)).getBlock();
			if(b == ModBlocks.conveyor_double.get()) reach = 0.5D;
			if(b == ModBlocks.conveyor_triple.get()) reach = 0.33D;
		}

		double x = worldPosition.getX() + inputSide.getStepX() * reach;
		double y = worldPosition.getY() + inputSide.getStepY() * reach;
		double z = worldPosition.getZ() + inputSide.getStepZ() * reach;
		return new AABB(x + 0.1875D, y + 0.1875D, z + 0.1875D, x + 0.8125D, y + 0.8125D, z + 0.8125D);
	}

	@Override
	public void updateEntity() {
		if(isServer()) {

			int delay = getEjectorDelay(slots.get(10));

			if(level.getGameTime() >= lastGrabbedTick + delay && !level.hasNeighborSignal(worldPosition)) {
				int amount = getStackAmount(slots.get(9));

				Direction outputSide = getOutputSide();
				BlockPos outPos = worldPosition.relative(outputSide);
				Block beltBlock = level.getBlockState(outPos).getBlock();

				if(beltBlock instanceof IConveyorBelt belt) {

					for(EntityMovingItem item : level.getEntitiesOfClass(EntityMovingItem.class, getGrabArea())) {
						if(!item.isAlive()) continue;
						ItemStack stack = item.getItemStack();
						boolean match = this.matchesFilter(stack);
						if(this.isWhitelist && !match || !this.isWhitelist && match) continue;

						lastGrabbedTick = level.getGameTime();

						Vec3 pos = new Vec3(worldPosition.getX() + 0.5 + outputSide.getStepX() * 0.55, worldPosition.getY() + 0.5 + outputSide.getStepY() * 0.55, worldPosition.getZ() + 0.5 + outputSide.getStepZ() * 0.55);
						Vec3 snap = belt.getClosestSnappingPosition(level, outPos, pos);
						EntityMovingItem newItem = new EntityMovingItem(level);
						newItem.setItemStack(item.getItemStack().copy());
						newItem.setPos(snap.x, snap.y, snap.z);
						item.discard();
						level.addFreshEntity(newItem);
						break;
					}

				} else {

					IItemHandler inv = getNeighborInventory(outputSide);

					if(inv != null) {

						for(EntityMovingItem item : level.getEntitiesOfClass(EntityMovingItem.class, getGrabArea())) {
							if(!item.isAlive()) continue;
							ItemStack stack = item.getItemStack();
							boolean match = this.matchesFilter(stack);
							if(this.isWhitelist && !match || !this.isWhitelist && match) continue;

							lastGrabbedTick = level.getGameTime();

							int toAdd = Math.min(stack.getCount(), amount);
							ItemStack ret = CraneInserter.addToInventory(inv, stack.copyWithCount(toAdd));
							int didAdd = toAdd - ret.getCount();

							ItemStack left = stack.copy();
							left.shrink(didAdd);

							if(left.isEmpty()) {
								item.discard();
							} else {
								item.setItemStack(left);
							}

							amount -= didAdd;
							if(amount <= 0) {
								break;
							}
						}
					}
				}
			}

			this.networkPackNT(15);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(this.isWhitelist);
		this.matcher.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.isWhitelist = buf.readBoolean();
		this.matcher.deserialize(buf);
	}

	public boolean matchesFilter(ItemStack stack) {
		return matchesFilter(matcher, 9, stack);
	}

	@Override public int[] getAccessibleSlotsFromSide(Direction side) { return new int[0]; }
	@Override public boolean isItemValidForSlot(int i, ItemStack itemStack) { return false; }

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.isWhitelist = nbt.getBoolean("isWhitelist");
		this.matcher.readFromNBT(nbt);
		this.lastGrabbedTick = nbt.getLong("lastGrabbedTick");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putBoolean("isWhitelist", this.isWhitelist);
		this.matcher.writeToNBT(nbt);
		nbt.putLong("lastGrabbedTick", lastGrabbedTick);
	}

	@Override
	public boolean hasPermission(Player player) {
		return player.distanceToSqr(worldPosition.getCenter()) < 20 * 20;
	}

	@Override
	public int[] getFilterSlots() {
		return new int[] {0, 9};
	}

	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("whitelist")) {
			this.isWhitelist = !this.isWhitelist;
		}
		if(data.contains("slot")) {
			setFilterContents(data);
		}
		this.setChanged();
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerCraneGrabber(id, inv, this);
	}
}
