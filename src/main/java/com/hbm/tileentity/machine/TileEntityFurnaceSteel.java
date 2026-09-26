package com.hbm.tileentity.machine;

import java.util.Optional;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.container.ContainerFurnaceSteel;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;

import api.hbm.tile.IHeatSource;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.Tags;

/**
 * Steel furnace: smelts three items at once with the heat of the heater below, as long as it's at least a third hot.
 * Ores, logs and tar build up a bonus, every full 100% gives an extra output.
 * Slots: 0-2 inputs, 3-5 outputs.
 */
public class TileEntityFurnaceSteel extends TileEntityMachineBase implements MenuProvider {

	public int[] progress = new int[3];
	public int[] bonus = new int[3];
	public static final int processTime = 40_000; // assuming vanilla furnace rules with 200 ticks of coal fire burning at 200HU/t

	public int heat;
	public static final int maxHeat = 100_000;
	public static final double diffusion = 0.05D;

	private ItemStack[] lastItems = { ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY };

	public boolean wasOn = false;

	public TileEntityFurnaceSteel(BlockPos pos, BlockState state) {
		super(ModTileEntities.FURNACE_STEEL.get(), pos, state, 6);
	}

	@Override
	public String getName() {
		return "container.furnaceSteel";
	}

	private Optional<RecipeHolder<SmeltingRecipe>> getSmelting(ItemStack stack) {
		if(stack.isEmpty()) return Optional.empty();
		return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level);
	}

	private ItemStack getSmeltingResult(ItemStack stack) {
		return getSmelting(stack).map(r -> r.value().assemble(new SingleRecipeInput(stack), level.registryAccess())).orElse(ItemStack.EMPTY);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			tryPullHeat();

			this.wasOn = false;

			int burn = (heat - maxHeat / 3) / 10;

			for(int i = 0; i < 3; i++) {

				if(slots.get(i).isEmpty() || lastItems[i].isEmpty() || !ItemStack.isSameItem(slots.get(i), lastItems[i])) {
					progress[i] = 0;
					bonus[i] = 0;
				}

				if(canSmelt(i)) {
					progress[i] += burn;
					this.heat -= burn;
					this.wasOn = true;
					if(level.getGameTime() % 20 == 0) PollutionHandler.incrementPollution(level, worldPosition, PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND * 2);
				}

				lastItems[i] = slots.get(i).copy();

				if(progress[i] >= processTime) {
					ItemStack result = getSmeltingResult(slots.get(i));

					if(slots.get(i + 3).isEmpty()) {
						slots.set(i + 3, result.copy());
					} else {
						slots.get(i + 3).grow(result.getCount());
					}

					this.addBonus(slots.get(i), i);

					while(bonus[i] >= 100) {
						slots.get(i + 3).setCount(Math.min(slots.get(i + 3).getMaxStackSize(), slots.get(i + 3).getCount() + result.getCount()));
						bonus[i] -= 100;
					}

					this.removeItem(i, 1);

					progress[i] = 0;
				}
			}

			this.networkPackNT(50);
		} else {

			if(this.wasOn) {
				Direction dir = Direction.from3DDataValue(BlockDummyable.getMeta(getBlockState()) - 10);
				Direction rot = dir.getClockWise();
				double x = worldPosition.getX() + 0.5, y = worldPosition.getY(), z = worldPosition.getZ() + 0.5;
				level.addParticle(ParticleTypes.SMOKE, x - dir.getStepX() * 1.125 - rot.getStepX() * 0.75, y + 2.625, z - dir.getStepZ() * 1.125 - rot.getStepZ() * 0.75, 0.0, 0.05, 0.0);

				if(level.random.nextInt(20) == 0)
					level.addParticle(ParticleTypes.CLOUD, x + dir.getStepX() * 0.75, y + 2, z + dir.getStepZ() * 0.75, 0.0, 0.05, 0.0);

				if(level.random.nextInt(15) == 0)
					level.addParticle(ParticleTypes.LAVA, x + dir.getStepX() * 1.5 + rot.getStepX() * (level.random.nextDouble() - 0.5), y + 0.75, z + dir.getStepZ() * 1.5 + rot.getStepZ() * (level.random.nextDouble() - 0.5), dir.getStepX() * 0.5D, 0.05, dir.getStepZ() * 0.5D);
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		for(int i = 0; i < 3; i++) buf.writeInt(this.progress[i]);
		for(int i = 0; i < 3; i++) buf.writeInt(this.bonus[i]);
		buf.writeInt(this.heat);
		buf.writeBoolean(this.wasOn);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		for(int i = 0; i < 3; i++) this.progress[i] = buf.readInt();
		for(int i = 0; i < 3; i++) this.bonus[i] = buf.readInt();
		this.heat = buf.readInt();
		this.wasOn = buf.readBoolean();
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		int[] p = nbt.getIntArray("progress");
		int[] b = nbt.getIntArray("bonus");
		for(int i = 0; i < 3; i++) {
			this.progress[i] = i < p.length ? p[i] : 0;
			this.bonus[i] = i < b.length ? b[i] : 0;
		}
		this.heat = nbt.getInt("heat");
		ListTag list = nbt.getList("lastItems", 10);
		for(int i = 0; i < list.size(); i++) {
			CompoundTag nbt1 = list.getCompound(i);
			byte b0 = nbt1.getByte("lastItem");
			if(b0 >= 0 && b0 < lastItems.length) {
				lastItems[b0] = ItemStack.parseOptional(registries, nbt1.getCompound("stack"));
			}
		}
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putIntArray("progress", progress);
		nbt.putIntArray("bonus", bonus);
		nbt.putInt("heat", heat);
		ListTag list = new ListTag();
		for(int i = 0; i < lastItems.length; i++) {
			if(!lastItems[i].isEmpty()) {
				CompoundTag nbt1 = new CompoundTag();
				nbt1.putByte("lastItem", (byte) i);
				nbt1.put("stack", lastItems[i].save(registries));
				list.add(nbt1);
			}
		}
		nbt.put("lastItems", list);
	}

	/** The original checked the ore dictionary names: ores 25%, logs and tar 50% */
	protected void addBonus(ItemStack stack, int index) {
		if(stack.is(Tags.Items.ORES)) { this.bonus[index] += 25; return; }
		if(stack.is(ItemTags.LOGS)) { this.bonus[index] += 50; return; }
		if(stack.is(OreDictManager.tag("anyTar"))) { this.bonus[index] += 50; return; }
	}

	protected void tryPullHeat() {

		if(this.heat >= maxHeat) return;

		BlockEntity con = level.getBlockEntity(worldPosition.below());

		if(con instanceof IHeatSource source) {
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

	public boolean canSmelt(int index) {
		if(this.heat < maxHeat / 3) return false;
		if(slots.get(index).isEmpty()) return false;

		ItemStack result = getSmeltingResult(slots.get(index));
		if(result.isEmpty()) return false;

		ItemStack out = slots.get(index + 3);
		if(out.isEmpty()) return true;
		if(!ItemStack.isSameItemSameComponents(result, out)) return false;
		if(result.getCount() + out.getCount() > out.getMaxStackSize()) return false;

		return true;
	}

	private static final int[] slot_access = new int[] { 0, 1, 2, 3, 4, 5 };

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return slot_access;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		if(i < 3) return getSmelting(itemStack).isPresent();
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i > 2;
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 3, worldPosition.getZ() + 2);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerFurnaceSteel(id, inv, this);
	}
}
