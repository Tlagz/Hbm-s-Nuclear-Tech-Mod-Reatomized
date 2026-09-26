package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerFurnaceIron;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.module.ModuleBurnTime;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.i18n.I18nUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Iron furnace: a bigger furnace with two fuel slots that doesn't burn fuel while idle and gets bonus burn time from
 * coal, coke and solid fuels. Slots: 0 input, 1-2 fuel, 3 output, 4 speed upgrade.
 */
public class TileEntityFurnaceIron extends TileEntityMachineBase implements IUpgradeInfoProvider, MenuProvider {

	public int maxBurnTime;
	public int burnTime;
	public boolean wasOn = false;

	public int progress;
	public int processingTime;
	public static final int baseTime = 160;

	public ModuleBurnTime burnModule;
	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public TileEntityFurnaceIron(BlockPos pos, BlockState state) {
		super(ModTileEntities.FURNACE_IRON.get(), pos, state, 5);

		burnModule = new ModuleBurnTime()
				.setLigniteTimeMod(1.25)
				.setCoalTimeMod(1.25)
				.setCokeTimeMod(1.5)
				.setSolidTimeMod(2)
				.setRocketTimeMod(2)
				.setBalefireTimeMod(2);
	}

	@Override
	public String getName() {
		return "container.furnaceIron";
	}

	private Optional<RecipeHolder<SmeltingRecipe>> getSmelting(ItemStack stack) {
		if(stack.isEmpty() || level == null) return Optional.empty();
		return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level);
	}

	public ItemStack getSmeltingResult(ItemStack stack) {
		return getSmelting(stack).map(r -> r.value().assemble(new SingleRecipeInput(stack), level.registryAccess())).orElse(ItemStack.EMPTY);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			upgradeManager.checkSlots(slots, 4, 4);
			this.processingTime = baseTime - ((baseTime / 2) * upgradeManager.getLevel(UpgradeType.SPEED) / 3);

			wasOn = false;

			// burn time only runs down while smelting, so a lit fuel item isn't wasted when idle
			if(burnTime <= 0) {

				for(int i = 1; i < 3; i++) {
					ItemStack fuel = slots.get(i);
					if(fuel.isEmpty()) continue;

					int time = burnModule.getBurnTime(fuel);
					if(time > 0) {
						this.maxBurnTime = this.burnTime = time;
						ItemStack remainder = fuel.getCraftingRemainingItem();
						fuel.shrink(1);
						if(fuel.isEmpty()) slots.set(i, remainder);
						break;
					}
				}
			}

			if(canSmelt()) {
				wasOn = true;
				this.progress++;
				this.burnTime--;

				if(this.progress % 15 == 0 && !this.muffled) {
					level.playSound(null, worldPosition, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0F, 0.5F + level.random.nextFloat() * 0.5F);
				}

				if(this.progress >= this.processingTime) {
					ItemStack result = getSmeltingResult(slots.get(0));

					if(slots.get(3).isEmpty()) {
						slots.set(3, result.copy());
					} else {
						slots.get(3).grow(result.getCount());
					}

					this.removeItem(0, 1);

					this.progress = 0;
					this.setChanged();
				}

				if(level.getGameTime() % 20 == 0) PollutionHandler.incrementPollution(level, worldPosition, PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND);

			} else {
				this.progress = 0;
			}

			this.networkPackNT(50);

		} else {

			if(this.progress > 0) {
				Direction dir = BlockDummyable.getRotation(getBlockState());
				Direction rot = dir.getClockWise();

				double offset = this.progress % 2 == 0 ? 1 : 0.5;
				level.addParticle(ParticleTypes.SMOKE, worldPosition.getX() + 0.5 - dir.getStepX() * offset - rot.getStepX() * 0.1875, worldPosition.getY() + 2, worldPosition.getZ() + 0.5 - dir.getStepZ() * offset - rot.getStepZ() * 0.1875, 0.0, 0.01, 0.0);

				if(this.progress % 5 == 0) {
					double rand = level.random.nextDouble();
					level.addParticle(ParticleTypes.FLAME, worldPosition.getX() + 0.5 + dir.getStepX() * 0.25 + rot.getStepX() * rand, worldPosition.getY() + 0.25 + level.random.nextDouble() * 0.25, worldPosition.getZ() + 0.5 + dir.getStepZ() * 0.25 + rot.getStepZ() * rand, 0.0, 0.0, 0.0);
				}
			}
		}
	}

	/** Whether the input has a smelting result that fits into the output */
	private boolean hasSmeltable() {
		if(slots.get(0).isEmpty()) return false;

		ItemStack result = getSmeltingResult(slots.get(0));
		if(result.isEmpty()) return false;

		ItemStack out = slots.get(3);
		if(out.isEmpty()) return true;
		if(!ItemStack.isSameItemSameComponents(result, out)) return false;
		return result.getCount() + out.getCount() <= out.getMaxStackSize();
	}

	public boolean canSmelt() {
		if(this.burnTime <= 0) return false;
		return hasSmeltable();
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(this.maxBurnTime);
		buf.writeInt(this.burnTime);
		buf.writeInt(this.progress);
		buf.writeInt(this.processingTime);
		buf.writeBoolean(this.wasOn);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.maxBurnTime = buf.readInt();
		this.burnTime = buf.readInt();
		this.progress = buf.readInt();
		this.processingTime = buf.readInt();
		this.wasOn = buf.readBoolean();
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return new int[] { 0, 1, 2, 3 };
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		if(i == 0) return !getSmeltingResult(itemStack).isEmpty();
		if(i < 3) return burnModule.getBurnTime(itemStack) > 0;
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i == 3;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.maxBurnTime = nbt.getInt("maxBurnTime");
		this.burnTime = nbt.getInt("burnTime");
		this.progress = nbt.getInt("progress");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putInt("maxBurnTime", maxBurnTime);
		nbt.putInt("burnTime", burnTime);
		nbt.putInt("progress", progress);
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 3, worldPosition.getZ() + 2);
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.furnace_iron.get()));
		if(type == UpgradeType.SPEED) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_DELAY, "-" + (level * 50 / 3) + "%"));
		}
	}

	@Override
	public HashMap<UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.SPEED, 3);
		return upgrades;
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerFurnaceIron(id, inv, this);
	}
}
