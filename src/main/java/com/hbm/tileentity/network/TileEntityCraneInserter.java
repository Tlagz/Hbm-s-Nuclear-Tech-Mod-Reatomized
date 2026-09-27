package com.hbm.tileentity.network;

import com.hbm.blocks.network.CraneInserter;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerCraneInserter;
import com.hbm.tileentity.ModTileEntities;

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
import net.neoforged.neoforge.items.IItemHandler;

/** Inserter buffer: keeps pushing its 21 slots into the output inventory, one stack (or single item) per tick */
public class TileEntityCraneInserter extends TileEntityCraneBase implements IControlReceiver, MenuProvider {

	public boolean destroyer = true;
	public static final int[] access = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };

	public TileEntityCraneInserter(BlockPos pos, BlockState state) {
		super(ModTileEntities.CRANE_INSERTER.get(), pos, state, 21);
	}

	@Override
	public String getName() {
		return "container.craneInserter";
	}

	@Override
	public void updateEntity() {
		if(isServer()) {

			if(!level.hasNeighborSignal(worldPosition)) {
				IItemHandler target = CraneInserter.getTarget(level, worldPosition, getOutputSide());

				if(target != null) {
					boolean didSomething = false;

					for(int i = 0; i < slots.size(); i++) {
						ItemStack stack = slots.get(i);

						if(!stack.isEmpty()) {
							ItemStack ret = CraneInserter.addToInventory(target, stack.copy());

							if(ret.getCount() != stack.getCount()) {
								slots.set(i, ret);
								this.setChanged();
								didSomething = true;
								break;
							}
						}
					}

					//if the previous operation fails, repeat but use single items instead of the whole stack instead
					//this should fix cases where the inserter can't insert into something that has a stack size limitation
					if(!didSomething) for(int i = 0; i < slots.size(); i++) {
						ItemStack stack = slots.get(i);

						if(!stack.isEmpty()) {
							ItemStack ret = CraneInserter.addToInventory(target, stack.copyWithCount(1));

							if(ret.isEmpty()) {
								this.removeItem(i, 1);
								this.setChanged();
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
		buf.writeBoolean(destroyer);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		destroyer = buf.readBoolean();
	}

	@Override public int[] getAccessibleSlotsFromSide(Direction side) { return access; }
	@Override public boolean isItemValidForSlot(int i, ItemStack itemStack) { return true; }
	@Override public boolean canExtractItem(int i, ItemStack itemStack, Direction side) { return true; }

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.destroyer = nbt.getBoolean("destroyer");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putBoolean("destroyer", this.destroyer);
	}

	@Override
	public boolean hasPermission(Player player) {
		return player.distanceToSqr(worldPosition.getCenter()) < 20 * 20;
	}

	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("destroyer")) this.destroyer = !this.destroyer;
		this.setChanged();
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerCraneInserter(id, inv, this);
	}
}
