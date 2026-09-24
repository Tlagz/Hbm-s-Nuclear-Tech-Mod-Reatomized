package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModDataComponents;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IEnergyProviderMK2;
import api.hbm.energymk2.IEnergyReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Capacitors store power. They receive through their front (the face they were placed against) and
 * provide through a chain of capacitor buses attached to their back.
 * The stored power is kept on the item when broken (the original's IPersistentNBT, now a data component).
 *
 * TODO look overlay (HUD with charge and I/O while looking at it), redstone-over-radio and OpenComputers compat
 */
public class MachineCapacitor extends Block implements EntityBlock {

	public static final DirectionProperty FACING = DirectionalBlock.FACING;

	protected final long power;
	public final String name;

	public MachineCapacitor(Properties properties, long power, String name) {
		super(properties);
		this.power = power;
		this.name = name;
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	/** Same as the original, which used the clicked side as metadata */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getClickedFace());
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityCapacitor(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return world.isClientSide || type != ModTileEntities.CAPACITOR.get() ? null : TileEntityLoadedBase.ticker();
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
		list.add(Component.literal("Stores up to " + BobMathUtil.getShortNumber(this.power) + "HE").withStyle(ChatFormatting.GOLD));
		list.add(Component.literal("Charge speed: " + BobMathUtil.getShortNumber(this.power / 200) + "HE").withStyle(ChatFormatting.GOLD));
		list.add(Component.literal("Discharge speed: " + BobMathUtil.getShortNumber(this.power / 600) + "HE").withStyle(ChatFormatting.GOLD));

		Long stored = stack.get(ModDataComponents.CHARGE.get());
		if(stored != null) list.add(Component.literal(BobMathUtil.getShortNumber(stored) + "/" + BobMathUtil.getShortNumber(this.power) + "HE").withStyle(ChatFormatting.YELLOW));

		if(Screen.hasShiftDown()) {
			for(String s : I18nUtil.resolveKeyArray("tile.capacitor.desc")) list.add(Component.literal(s).withStyle(ChatFormatting.YELLOW));
		} else {
			list.add(Component.literal("Hold <").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
					.append(Component.literal("LSHIFT").withStyle(ChatFormatting.YELLOW, ChatFormatting.ITALIC))
					.append(Component.literal("> to display more info").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
		}
	}

	public long getCapacity() {
		return power;
	}

	public static class TileEntityCapacitor extends TileEntityLoadedBase implements IEnergyProviderMK2, IEnergyReceiverMK2 {

		public long power;
		protected long maxPower;
		public long powerReceived;
		public long powerSent;
		public long lastPowerReceived;
		public long lastPowerSent;

		public TileEntityCapacitor(BlockPos pos, BlockState state) {
			super(ModTileEntities.CAPACITOR.get(), pos, state);
			this.maxPower = state.getBlock() instanceof MachineCapacitor capacitor ? capacitor.power : 0;
		}

		private Direction facing() {
			return getBlockState().getValue(FACING);
		}

		@Override
		public void updateEntity() {

			if(isServer()) {

				Direction opp = facing();
				Direction dir = opp.getOpposite();

				// follow the bus chain at the back, it has to be straight
				BlockPos pos = worldPosition.relative(dir);
				boolean didStep = false;
				Direction last = null;

				while(level.getBlockState(pos).is(ModBlocks.capacitor_bus.get())) {
					Direction current = level.getBlockState(pos).getValue(MachineCapacitorBus.FACING);
					if(!didStep) last = current;
					didStep = true;

					if(last != current) {
						pos = null;
						break;
					}

					pos = pos.relative(current);
				}

				if(pos != null && last != null) {
					this.tryUnsubscribe(level, pos);
					this.tryProvide(level, pos, last);
				}

				this.trySubscribe(level, worldPosition.relative(opp), opp);

				networkPackNT(15);

				this.lastPowerSent = powerSent;
				this.lastPowerReceived = powerReceived;
				this.powerSent = 0;
				this.powerReceived = 0;
			}
		}

		@Override
		public void serialize(ByteBuf buf) {
			buf.writeLong(power);
			buf.writeLong(maxPower);
			buf.writeLong(powerReceived);
			buf.writeLong(powerSent);
		}

		@Override
		public void deserialize(ByteBuf buf) {
			power = buf.readLong();
			maxPower = buf.readLong();
			powerReceived = buf.readLong();
			powerSent = buf.readLong();
		}

		@Override
		public long transferPower(long power) {
			if(power + this.getPower() <= this.getMaxPower()) {
				this.setPower(power + this.getPower());
				this.powerReceived += power;
				return 0;
			}
			long capacity = this.getMaxPower() - this.getPower();
			long overshoot = power - capacity;
			this.powerReceived += (this.getMaxPower() - this.getPower());
			this.setPower(this.getMaxPower());
			return overshoot;
		}

		@Override
		public void usePower(long power) {
			this.powerSent += Math.min(this.getPower(), power);
			this.setPower(this.getPower() - power);
		}

		@Override public long getPower() { return power; }
		@Override public long getMaxPower() { return maxPower; }
		@Override public long getProviderSpeed() { return this.getMaxPower() / 300; }
		@Override public long getReceiverSpeed() { return this.getMaxPower() / 100; }
		@Override public ConnectionPriority getPriority() { return ConnectionPriority.LOW; }

		@Override
		public void setPower(long power) {
			this.power = power;
			this.setChanged();
		}

		@Override
		public boolean canConnect(Direction dir) {
			return dir == facing();
		}

		@Override
		protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
			super.loadAdditional(nbt, registries);
			this.power = nbt.getLong("power");
			if(nbt.contains("maxPower")) this.maxPower = nbt.getLong("maxPower");
		}

		@Override
		protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
			super.saveAdditional(nbt, registries);
			nbt.putLong("power", power);
			nbt.putLong("maxPower", maxPower);
		}

		/** Keeps the charge on the dropped item (copied by the loot table) and restores it when placed */
		@Override
		protected void collectImplicitComponents(DataComponentMap.Builder components) {
			super.collectImplicitComponents(components);
			components.set(ModDataComponents.CHARGE.get(), power);
		}

		@Override
		protected void applyImplicitComponents(DataComponentInput input) {
			super.applyImplicitComponents(input);
			this.power = Math.min(input.getOrDefault(ModDataComponents.CHARGE.get(), 0L), maxPower);
		}
	}
}
