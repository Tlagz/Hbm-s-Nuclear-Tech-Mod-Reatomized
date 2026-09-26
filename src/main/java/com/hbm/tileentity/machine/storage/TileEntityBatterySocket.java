package com.hbm.tileentity.machine.storage;

import com.hbm.blocks.BlockDummyable;
import com.hbm.inventory.container.ContainerBatterySocket;
import com.hbm.items.machine.ItemBatterySC;
import com.hbm.items.machine.ItemBatterySC.EnumBatterySC;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.util.DirPos;

import api.hbm.energymk2.IBatteryItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
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
 * Battery socket: connects one battery item (battery packs, capacitors, anything with IBatteryItem) to the power grid
 * directly, the whole 2x2 block acts as a cable. Loaded schrabidium cells make the charge fluctuate.
 *
 * TODO the schrabidium cell's discharge beams, RoR, OpenComputers
 */
public class TileEntityBatterySocket extends TileEntityBatteryBase implements MenuProvider {

	public boolean frame = false;

	public long[] log = new long[20];
	public long delta = 0;

	public long syncPower = 0;
	public long syncMaxPower = 0;
	public ItemStack syncStack = ItemStack.EMPTY;

	public int damageTimer;
	public int damageTarget;
	public double scPowerMult = 1D;

	public TileEntityBatterySocket(BlockPos pos, BlockState state) {
		super(ModTileEntities.BATTERY_SOCKET.get(), pos, state, 1);
	}

	@Override public String getName() { return "container.batterySocket"; }

	@Override
	public void updateEntity() {
		long prevPower = this.getPower();

		super.updateEntity();

		if(isServer()) {

			if(hasSCLoaded()) {
				if(this.damageTarget == 0) pickNewSCTarget();
				this.damageTimer++;
				if(this.damageTimer >= this.damageTarget) pickNewSCTarget();
				fluctuate();
			}

			long avg = (this.getPower() + prevPower) / 2;
			this.delta = avg - this.log[0];

			for(int i = 1; i < this.log.length; i++) {
				this.log[i - 1] = this.log[i];
			}

			this.log[19] = avg;

		} else {
			// the supports are shown when something sits on top
			if(level.getGameTime() % 20 == 0) {
				frame = !level.getBlockState(worldPosition.above(2)).isAir();
			}
		}
	}

	protected boolean hasSCLoaded() {
		return slots.get(0).getItem() instanceof ItemBatterySC sc && sc.pack != EnumBatterySC.EMPTY;
	}

	protected void pickNewSCTarget() {
		this.damageTimer = 0;
		this.damageTarget = 1200 + level.random.nextInt(2400); // 1-3 minutes
		this.setChanged();
	}

	protected void fluctuate() {
		double steppy = 1D / 100D;
		this.scPowerMult += (steppy * (level.random.nextDouble() * 2 - 1));
		this.scPowerMult = Mth.clamp(scPowerMult, 0.1D, 1D);
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(delta);
		buf.writeLong(this.getPower());
		buf.writeLong(this.getMaxPower());
		// every battery variant is its own item, the item alone is enough to render it
		buf.writeInt(this.slots.get(0).isEmpty() ? -1 : BuiltInRegistries.ITEM.getId(this.slots.get(0).getItem()));
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		delta = buf.readLong();
		syncPower = buf.readLong();
		syncMaxPower = buf.readLong();
		int itemId = buf.readInt();
		this.syncStack = itemId == -1 ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.byId(itemId));
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.damageTimer = nbt.getInt("damageTimer");
		this.damageTarget = nbt.getInt("damageTarget");
		this.scPowerMult = nbt.contains("scPowerMult") ? nbt.getDouble("scPowerMult") : 1D;
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putInt("damageTimer", damageTimer);
		nbt.putInt("damageTarget", damageTarget);
		nbt.putDouble("scPowerMult", scPowerMult);
	}

	/** Automation only takes out batteries that are done: empty when outputting, full when charging */
	@Override
	public boolean canExtractItem(int slot, ItemStack stack, Direction side) {
		if(stack.getItem() instanceof IBatteryItem battery) {
			int mode = this.getRelevantMode(false);
			if(mode == mode_output && battery.getCharge(stack) == 0) return true;
			if(mode == mode_input && battery.getCharge(stack) == battery.getMaxCharge(stack)) return true;
		}
		return false;
	}

	@Override public int[] getAccessibleSlotsFromSide(Direction side) { return new int[] {0}; }

	@Override
	public long getPower() {
		long power = powerFromStack(slots.get(0));
		if(this.hasSCLoaded()) power *= this.scPowerMult;
		return power;
	}

	@Override public long getMaxPower() { return maxPowerFromStack(slots.get(0)); }

	@Override
	public void setPower(long power) {
		if(!(slots.get(0).getItem() instanceof IBatteryItem battery)) return;
		battery.setCharge(slots.get(0), power);
	}

	public static long powerFromStack(ItemStack stack) {
		return stack.getItem() instanceof IBatteryItem battery ? battery.getCharge(stack) : 0;
	}

	public static long maxPowerFromStack(ItemStack stack) {
		return stack.getItem() instanceof IBatteryItem battery ? battery.getMaxCharge(stack) : 0;
	}

	@Override
	public long getProviderSpeed() {
		if(!(slots.get(0).getItem() instanceof IBatteryItem battery)) return 0;
		int mode = this.getRelevantMode(true);
		return mode == mode_output || mode == mode_buffer ? battery.getDischargeRate(slots.get(0)) : 0;
	}

	@Override
	public long getReceiverSpeed() {
		if(!(slots.get(0).getItem() instanceof IBatteryItem battery)) return 0;
		int mode = this.getRelevantMode(true);
		return mode == mode_input || mode == mode_buffer ? battery.getChargeRate(slots.get(0)) : 0;
	}

	@Override
	public BlockPos[] getPortPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		BlockPos p = worldPosition;
		return new BlockPos[] {
				p,
				p.relative(dir, -1),
				p.relative(rot),
				p.relative(dir, -1).relative(rot)
		};
	}

	@Override
	public DirPos[] getConPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		BlockPos p = worldPosition;
		return new DirPos[] {
				new DirPos(p.relative(dir), dir),
				new DirPos(p.relative(dir).relative(rot), dir),
				new DirPos(p.relative(dir, -2), dir.getOpposite()),
				new DirPos(p.relative(dir, -2).relative(rot), dir.getOpposite()),
				new DirPos(p.relative(rot, 2), rot),
				new DirPos(p.relative(rot, 2).relative(dir, -1), rot),
				new DirPos(p.relative(rot, -1), rot.getOpposite()),
				new DirPos(p.relative(rot, -1).relative(dir, -1), rot.getOpposite())
		};
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 2, worldPosition.getZ() + 2);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerBatterySocket(id, inv, this);
	}
}
