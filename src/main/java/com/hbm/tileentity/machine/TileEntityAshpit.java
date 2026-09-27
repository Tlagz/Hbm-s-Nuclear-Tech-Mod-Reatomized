package com.hbm.tileentity.machine;

import com.hbm.inventory.container.ContainerAshpit;
import com.hbm.items.ItemEnums.EnumAshType;
import com.hbm.items.ModItems;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Ashpit: goes under fireboxes (wood, coal and misc ash from the burnt fuel) and chimneys (fly ash and soot), turns
 * the collected ash into ash items. Slots: 0-4 ash.
 *
 * TODO config (IConfigurableMachine), chimneys
 */
public class TileEntityAshpit extends TileEntityMachineBase implements MenuProvider {

	private int playersUsing = 0;
	public float doorAngle = 0;
	public float prevDoorAngle = 0;
	public boolean isFull;

	public int ashLevelWood;
	public int ashLevelCoal;
	public int ashLevelMisc;
	public int ashLevelFly;
	public int ashLevelSoot;

	public static int thresholdWood = 2000;
	public static int thresholdCoal = 2000;
	public static int thresholdMisc = 2000;
	public static int thresholdFly = 2000;
	public static int thresholdSoot = 8000;

	public TileEntityAshpit(BlockPos pos, BlockState state) {
		super(ModTileEntities.ASHPIT.get(), pos, state, 5);
	}

	@Override
	public void startOpen(Player player) {
		if(level != null && !level.isClientSide) this.playersUsing++;
	}

	@Override
	public void stopOpen(Player player) {
		if(level != null && !level.isClientSide) this.playersUsing--;
	}

	@Override
	public String getName() {
		return "container.ashpit";
	}

	/** Adds ash of that type for the fuel burnt above, from fireboxes and friends */
	public void addAsh(EnumAshType type, int amount) {
		if(type == EnumAshType.WOOD) ashLevelWood += amount;
		if(type == EnumAshType.COAL) ashLevelCoal += amount;
		if(type == EnumAshType.MISC) ashLevelMisc += amount;
		if(type == EnumAshType.FLY) ashLevelFly += amount;
		if(type == EnumAshType.SOOT) ashLevelSoot += amount;
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			if(processAsh(ashLevelWood, EnumAshType.WOOD, thresholdWood)) ashLevelWood -= thresholdWood;
			if(processAsh(ashLevelCoal, EnumAshType.COAL, thresholdCoal)) ashLevelCoal -= thresholdCoal;
			if(processAsh(ashLevelMisc, EnumAshType.MISC, thresholdMisc)) ashLevelMisc -= thresholdMisc;
			if(processAsh(ashLevelFly, EnumAshType.FLY, thresholdFly)) ashLevelFly -= thresholdFly;
			if(processAsh(ashLevelSoot, EnumAshType.SOOT, thresholdSoot)) ashLevelSoot -= thresholdSoot;

			isFull = false;

			for(int i = 0; i < 5; i++) {
				if(!slots.get(i).isEmpty()) isFull = true;
			}

			this.networkPackNT(50);

		} else {

			this.prevDoorAngle = this.doorAngle;
			float swingSpeed = (doorAngle / 10F) + 3;

			if(this.playersUsing > 0) {
				this.doorAngle += swingSpeed;
			} else {
				this.doorAngle -= swingSpeed;
			}

			this.doorAngle = Mth.clamp(this.doorAngle, 0F, 135F);
		}
	}

	/** The original also took the threshold off the wood ash when starting a new stack, that's left out here */
	protected boolean processAsh(int level, EnumAshType type, int threshold) {

		if(level >= threshold) {
			for(int i = 0; i < 5; i++) {
				ItemStack slot = slots.get(i);
				if(slot.isEmpty()) {
					slots.set(i, ModItems.powder_ash.stack(type));
					return true;
				} else if(slot.getCount() < slot.getMaxStackSize() && slot.is(ModItems.powder_ash.get(type).get())) {
					slot.grow(1);
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(this.playersUsing);
		buf.writeBoolean(this.isFull);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.playersUsing = buf.readInt();
		this.isFull = buf.readBoolean();
	}

	private static final int[] slot_access = new int[] { 0, 1, 2, 3, 4 };

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return slot_access;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return true;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.ashLevelWood = nbt.getInt("ashLevelWood");
		this.ashLevelCoal = nbt.getInt("ashLevelCoal");
		this.ashLevelMisc = nbt.getInt("ashLevelMisc");
		this.ashLevelFly = nbt.getInt("ashLevelFly");
		this.ashLevelSoot = nbt.getInt("ashLevelSoot");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putInt("ashLevelWood", ashLevelWood);
		nbt.putInt("ashLevelCoal", ashLevelCoal);
		nbt.putInt("ashLevelMisc", ashLevelMisc);
		nbt.putInt("ashLevelFly", ashLevelFly);
		nbt.putInt("ashLevelSoot", ashLevelSoot);
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 1, worldPosition.getZ() + 2);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerAshpit(id, inv, this);
	}
}
