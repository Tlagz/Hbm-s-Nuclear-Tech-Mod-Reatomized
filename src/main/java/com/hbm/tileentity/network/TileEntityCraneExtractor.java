package com.hbm.tileentity.network;

import com.hbm.interfaces.IControlReceiverFilter;
import com.hbm.inventory.container.ContainerCraneExtractor;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.main.ModSounds;
import com.hbm.module.ModulePatternMatcher;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.util.InventoryUtil;

import api.hbm.conveyor.IConveyorBelt;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Extractor: takes items out of the inventory at its output side and puts them onto the belt at its input side
 * (the original's switcheroo), filtered by 9 pattern slots (black or whitelist). Without a belt it fills its own
 * buffer, which gets emptied onto a belt later.
 * Slots: 0-8 filter, 9-17 buffer, 18 stack upgrade, 19 ejector upgrade.
 */
public class TileEntityCraneExtractor extends TileEntityCraneBase implements IControlReceiverFilter, MenuProvider {

	public boolean isWhitelist = false;
	public boolean maxEject = false;
	public ModulePatternMatcher matcher;

	public TileEntityCraneExtractor(BlockPos pos, BlockState state) {
		super(ModTileEntities.CRANE_EXTRACTOR.get(), pos, state, 20);
		this.matcher = new ModulePatternMatcher(9);
	}

	@Override
	public String getName() {
		return "container.craneExtractor";
	}

	@Override
	public void setItem(int i, ItemStack stack) {
		super.setItem(i, stack);

		if(!stack.isEmpty() && (i == 18 || i == 19) && stack.getItem() instanceof ItemMachineUpgrade && level != null && !level.isClientSide) {
			level.playSound(null, worldPosition, ModSounds.get("item.upgradePlug"), SoundSource.BLOCKS, 1.0F, 1.0F);
		}
	}

	@Override
	public void updateEntity() {
		if(isServer()) {

			int delay = getEjectorDelay(slots.get(19));

			if(level.getGameTime() % delay == 0 && !level.hasNeighborSignal(worldPosition)) {
				int amount = getStackAmount(slots.get(18));

				Direction inputSide = getOutputSide(); // note the switcheroo!
				Direction outputSide = getInputSide();
				IItemHandler inv = getNeighborInventory(inputSide);
				Block b = level.getBlockState(worldPosition.relative(outputSide)).getBlock();

				boolean hasSent = false;
				IConveyorBelt belt = b instanceof IConveyorBelt conveyor ? conveyor : null;

				/* try to send items from a connected inv, if present */
				if(inv != null) {

					for(int index = 0; index < inv.getSlots(); index++) {
						ItemStack stack = inv.getStackInSlot(index);

						if(!stack.isEmpty() && !inv.extractItem(index, 1, true).isEmpty()) {

							int maxTarget = Math.min(amount, stack.getMaxStackSize());
							if(this.maxEject && stack.getCount() < maxTarget) continue;
							boolean match = this.matchesFilter(stack);

							if((isWhitelist && match) || (!isWhitelist && !match)) {
								int toSend = Math.min(amount, stack.getCount());

								if(belt != null) {
									ItemStack taken = inv.extractItem(index, toSend, false);
									if(!taken.isEmpty()) sendItem(taken, belt, outputSide);
								} else {
									ItemStack remaining = InventoryUtil.tryAddItemToInventory(this.slots, 9, 17, stack.copyWithCount(toSend));
									inv.extractItem(index, toSend - remaining.getCount(), false);
									this.setChanged();
								}
								hasSent = true;
								break;
							}
						}
					}
				}

				/* if no item has been sent, send buffered items while ignoring the filter */
				if(!hasSent && belt != null) {

					for(int i = 9; i < 18; i++) {
						ItemStack stack = slots.get(i);

						if(!stack.isEmpty()) {
							int toSend = Math.min(amount, stack.getCount());

							int maxTarget = Math.min(amount, stack.getMaxStackSize());
							if(this.maxEject && stack.getCount() < maxTarget) continue;

							ItemStack sent = stack.copyWithCount(toSend);
							removeItem(i, toSend);
							sendItem(sent, belt, outputSide);

							break;
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
		buf.writeBoolean(isWhitelist);
		buf.writeBoolean(maxEject);
		this.matcher.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		isWhitelist = buf.readBoolean();
		maxEject = buf.readBoolean();
		this.matcher.deserialize(buf);
	}

	public boolean matchesFilter(ItemStack stack) {
		return matchesFilter(matcher, 9, stack);
	}

	@Override
	public void nextMode(int i) {
		this.matcher.nextMode(level, slots.get(i), i);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] { 9, 10, 11, 12, 13, 14, 15, 16, 17 };
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		boolean match = this.matchesFilter(itemStack);
		return i > 8 && i < 18 && ((isWhitelist && match) || (!isWhitelist && !match));
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i > 8 && i < 18;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.isWhitelist = nbt.getBoolean("isWhitelist");
		this.maxEject = nbt.getBoolean("maxEject");
		this.matcher.readFromNBT(nbt);
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putBoolean("isWhitelist", this.isWhitelist);
		nbt.putBoolean("maxEject", this.maxEject);
		this.matcher.writeToNBT(nbt);
	}

	@Override
	public boolean hasPermission(Player player) {
		return player.distanceToSqr(worldPosition.getCenter()) < 20 * 20;
	}

	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("whitelist")) {
			this.isWhitelist = !this.isWhitelist;
		}
		if(data.contains("maxEject")) {
			this.maxEject = !this.maxEject;
		}
		if(data.contains("slot")) {
			setFilterContents(data);
		}
		this.setChanged();
	}

	@Override
	public int[] getFilterSlots() {
		return new int[] {0, 9};
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerCraneExtractor(id, inv, this);
	}
}
