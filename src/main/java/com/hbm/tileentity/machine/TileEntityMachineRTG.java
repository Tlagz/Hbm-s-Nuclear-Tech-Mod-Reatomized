package com.hbm.tileentity.machine;

import com.hbm.inventory.container.ContainerMachineRTG;
import com.hbm.items.machine.ItemRTGPellet;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.RTGUtil;

import api.hbm.energymk2.IEnergyProviderMK2;
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

/** RT generator: 15 pellet slots, 5HE/t per unit of pellet heat (capped), the pellets decay while inside */
public class TileEntityMachineRTG extends TileEntityMachineBase implements IEnergyProviderMK2, MenuProvider {

	public int heat;
	public final int heatMax = RTGUtil.RTG_DECAY ? 600 : 200;
	public long power;
	public final long powerMax = 100000;
	public static final int[] slot_io = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 };

	public TileEntityMachineRTG(BlockPos pos, BlockState state) {
		super(ModTileEntities.RTG.get(), pos, state, 15);
	}

	@Override
	public String getName() {
		return "container.rtg";
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return itemStack.getItem() instanceof ItemRTGPellet;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return slot_io;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return false;
	}

	public long getPowerScaled(long i) {
		return (power * i) / powerMax;
	}

	public int getHeatScaled(int i) {
		return (heat * i) / heatMax;
	}

	public boolean hasPower() {
		return power > 0;
	}

	public boolean hasHeat() {
		return RTGUtil.hasHeat(slots, slot_io);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			for(Direction dir : Direction.values()) this.tryProvide(level, worldPosition.relative(dir), dir);

			heat = RTGUtil.updateRTGs(slots, slot_io);

			if(heat > heatMax) heat = heatMax;

			power += heat * 5;
			if(power > powerMax) power = powerMax;

			this.networkPackNT(50);
		}
	}

	/** The original synced the heat through the container, here it goes with the tile's packet */
	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(heat);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		heat = buf.readInt();
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		power = nbt.getLong("power");
		heat = nbt.getInt("heat");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("power", power);
		nbt.putInt("heat", heat);
	}

	@Override public long getPower() { return power; }
	@Override public long getMaxPower() { return powerMax; }
	@Override public void setPower(long i) { this.power = i; }

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineRTG(id, inv, this);
	}
}
