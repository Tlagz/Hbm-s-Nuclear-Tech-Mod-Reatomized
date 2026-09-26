package com.hbm.tileentity.machine.storage;

import com.hbm.blocks.generic.BlockStorageCrate;
import com.hbm.blocks.generic.BlockStorageCrate.CrateType;
import com.hbm.inventory.container.ContainerCrate;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The original's TileEntityCrateBase (iron, steel, desh, tungsten crates and the safe in one), the size comes from
 * the block. The contents and the name travel with the item through the container and custom name components.
 */
public class TileEntityCrate extends TileEntityMachineBase implements MenuProvider {

	public TileEntityCrate(BlockPos pos, BlockState state) {
		super(ModTileEntities.CRATE.get(), pos, state, getType(state).slots);
	}

	public static CrateType getType(BlockState state) {
		return state.getBlock() instanceof BlockStorageCrate crate ? crate.type : CrateType.IRON;
	}

	public CrateType getCrateType() {
		return getType(getBlockState());
	}

	@Override
	public String getName() {
		return getCrateType().name;
	}

	@Override
	public void updateEntity() { }

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return true;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		int[] slots = new int[this.slots.size()];
		for(int i = 0; i < slots.length; i++) slots[i] = i;
		return slots;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, Direction side) {
		return true;
	}

	@Override
	public void startOpen(Player player) {
		if(!player.isSpectator() && level != null) level.playSound(null, worldPosition, ModSounds.get("block.crateOpen"), SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	@Override
	public void stopOpen(Player player) {
		if(!player.isSpectator() && level != null) level.playSound(null, worldPosition, ModSounds.get("block.crateClose"), SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	@Override
	protected void collectImplicitComponents(DataComponentMap.Builder components) {
		super.collectImplicitComponents(components);
		if(this.slots.stream().anyMatch(stack -> !stack.isEmpty())) components.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.slots));
		if(this.hasCustomInventoryName()) components.set(DataComponents.CUSTOM_NAME, Component.literal(this.getInventoryName()));
	}

	@Override
	protected void applyImplicitComponents(DataComponentInput input) {
		super.applyImplicitComponents(input);
		input.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(this.slots);
		Component name = input.get(DataComponents.CUSTOM_NAME);
		if(name != null) this.setCustomName(name.getString());
	}

	@Override
	public void removeComponentsFromTag(CompoundTag tag) {
		super.removeComponentsFromTag(tag);
		tag.remove("items");
		tag.remove("name");
	}

	/** The name is synced for the look overlay */
	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		if(this.hasCustomInventoryName()) tag.putString("name", this.getInventoryName());
		return tag;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public Component getDisplayName() {
		return this.hasCustomInventoryName() ? Component.literal(this.getInventoryName()) : Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerCrate(id, inv, this);
	}
}
