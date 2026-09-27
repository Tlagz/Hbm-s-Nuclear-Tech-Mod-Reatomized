package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineExposureChamber;
import com.hbm.inventory.recipes.ExposureChamberRecipes;
import com.hbm.inventory.recipes.ExposureChamberRecipes.ExposureChamberRecipe;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.DirPos;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.phys.AABB;

/**
 * Exposure chamber: loads a particle capsule (8 uses of its particles, the empty capsule goes into the container slot)
 * and bombards ingots with them. Slots: 0 particle, 1 particle internal, 2 particle container, 3 ingredient, 4 output,
 * 5 battery, 6-7 upgrades.
 */
public class TileEntityMachineExposureChamber extends TileEntityMachineBase implements IEnergyReceiverMK2, IUpgradeInfoProvider, MenuProvider {

	public long power;
	public static final long maxPower = 1_000_000;

	public int progress;
	public static final int processTimeBase = 200;
	public int processTime = processTimeBase;
	public static final int consumptionBase = 10_000;
	public int consumption = consumptionBase;

	public int savedParticles;
	public static final int maxParticles = 8;
	public boolean isOn = false;

	public float rotation;
	public float prevRotation;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public TileEntityMachineExposureChamber(BlockPos pos, BlockState state) {
		super(ModTileEntities.EXPOSURE_CHAMBER.get(), pos, state, 8);
	}

	@Override
	public String getName() {
		return "container.exposureChamber";
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.isOn = false;
			this.power = Library.chargeTEFromItems(slots, 5, power, maxPower);

			this.autoPort(getConPos());

			upgradeManager.checkSlots(slots, 6, 7);
			int speedLevel = upgradeManager.getLevel(UpgradeType.SPEED);
			int powerLevel = upgradeManager.getLevel(UpgradeType.POWER);
			int overdriveLevel = upgradeManager.getLevel(UpgradeType.OVERDRIVE);

			this.consumption = consumptionBase;

			this.processTime = processTimeBase - processTimeBase / 4 * speedLevel;
			this.consumption *= (speedLevel / 2 + 1);
			this.processTime *= (powerLevel / 2 + 1);
			this.consumption /= (powerLevel + 1);
			this.processTime /= (overdriveLevel + 1);
			this.consumption *= (overdriveLevel * 2 + 1);

			// load a new capsule once the particles of the last one are used up
			if(slots.get(1).isEmpty() && !slots.get(0).isEmpty() && !slots.get(3).isEmpty() && this.savedParticles <= 0) {
				ExposureChamberRecipe recipe = this.getRecipe(slots.get(0), slots.get(3));

				if(recipe != null) {

					ItemStack container = slots.get(0).getCraftingRemainingItem();
					boolean canStore = false;

					if(container.isEmpty()) {
						canStore = true;
					} else if(slots.get(2).isEmpty()) {
						slots.set(2, container.copy());
						canStore = true;
					} else if(ItemStack.isSameItemSameComponents(slots.get(2), container) && slots.get(2).getCount() < slots.get(2).getMaxStackSize()) {
						slots.get(2).grow(1);
						canStore = true;
					}

					if(canStore) {
						slots.set(1, slots.get(0).copyWithCount(1));
						this.removeItem(0, 1);
						this.savedParticles = maxParticles;
					}
				}
			}

			if(!slots.get(1).isEmpty() && this.savedParticles > 0 && this.power >= this.consumption) {
				ExposureChamberRecipe recipe = this.getRecipe(slots.get(1), slots.get(3));
				ItemStack out = slots.get(4);

				if(recipe != null && (out.isEmpty() || (ItemStack.isSameItemSameComponents(out, recipe.output) && out.getCount() + recipe.output.getCount() <= out.getMaxStackSize()))) {
					this.progress++;
					this.power -= this.consumption;
					this.isOn = true;

					if(this.progress >= this.processTime) {
						this.progress = 0;
						this.savedParticles--;
						this.removeItem(3, 1);

						if(out.isEmpty()) {
							slots.set(4, recipe.output.copy());
						} else {
							out.grow(recipe.output.getCount());
						}
					}

				} else {
					this.progress = 0;
				}
			} else {
				this.progress = 0;
			}

			if(this.savedParticles <= 0) {
				slots.set(1, ItemStack.EMPTY);
			}

			this.networkPackNT(50);

		} else {

			this.prevRotation = this.rotation;

			if(this.isOn) {
				this.rotation += 10F;

				if(this.rotation >= 720F) {
					this.rotation -= 720F;
					this.prevRotation -= 720F;
				}
			}
		}
	}

	/** The ports are on the small end of the chamber */
	public DirPos[] getConPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getCounterClockWise(); // ForgeDirection.getRotation(UP).getOpposite()
		BlockPos p = worldPosition;

		return new DirPos[] {
				new DirPos(p.relative(rot, 7).relative(dir, 2), dir),
				new DirPos(p.relative(rot, 7).relative(dir, -2), dir.getOpposite()),
				new DirPos(p.relative(rot, 8).relative(dir, 2), dir),
				new DirPos(p.relative(rot, 8).relative(dir, -2), dir.getOpposite()),
				new DirPos(p.relative(rot, 9), rot)
		};
	}

	public ExposureChamberRecipe getRecipe(ItemStack particle, ItemStack ingredient) {
		return ExposureChamberRecipes.getRecipe(particle, ingredient);
	}

	/** New capsules only go in while nothing is cached, which prevents clogging */
	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {

		// accept items when the slots are already partially filled, i.e. applicable
		if(i == 0 && !slots.get(0).isEmpty()) return true;
		if(i == 3 && !slots.get(3).isEmpty()) return true;

		// if there's no particle stored, use the un-consumed capsule for reference
		ItemStack particle = !slots.get(1).isEmpty() ? slots.get(1) : slots.get(0);

		// if no particle is loaded and an ingot is present
		if(i == 0 && particle.isEmpty() && !slots.get(3).isEmpty()) {
			return getRecipe(stack, slots.get(3)) != null;
		}

		// if a particle is loaded but no ingot present
		if(i == 3 && !particle.isEmpty() && slots.get(3).isEmpty()) {
			return getRecipe(particle, stack) != null;
		}

		// if there's nothing at all, find a reference recipe and see if the item matches anything
		if(particle.isEmpty() && slots.get(3).isEmpty()) {
			for(ExposureChamberRecipe recipe : ExposureChamberRecipes.recipes) {
				if(i == 0 && recipe.particle.matchesRecipe(stack, true)) return true;
				if(i == 3 && recipe.ingredient.matchesRecipe(stack, true)) return true;
			}
		}

		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i == 2 || i == 4;
	}

	private static final int[] slot_access = new int[] {0, 2, 3, 4};

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return slot_access;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(this.isOn);
		buf.writeInt(this.progress);
		buf.writeInt(this.processTime);
		buf.writeInt(this.consumption);
		buf.writeLong(this.power);
		buf.writeByte((byte) this.savedParticles);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.isOn = buf.readBoolean();
		this.progress = buf.readInt();
		this.processTime = buf.readInt();
		this.consumption = buf.readInt();
		this.power = buf.readLong();
		this.savedParticles = buf.readByte();
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.progress = nbt.getInt("progress");
		this.power = nbt.getLong("power");
		this.savedParticles = nbt.getInt("savedParticles");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putInt("progress", progress);
		nbt.putLong("power", power);
		nbt.putInt("savedParticles", savedParticles);
	}

	@Override public long getPower() { return power; }
	@Override public void setPower(long power) { this.power = power; }
	@Override public long getMaxPower() { return maxPower; }

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 8, worldPosition.getY(), worldPosition.getZ() - 8, worldPosition.getX() + 9, worldPosition.getY() + 5, worldPosition.getZ() + 9);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineExposureChamber(id, inv, this);
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_exposure_chamber.get()));
		if(type == UpgradeType.SPEED) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_DELAY, "-" + (level * 25) + "%"));
			info.add(ChatFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + (level * 50) + "%"));
		}
		if(type == UpgradeType.POWER) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_CONSUMPTION, "-" + (100 - 100 / (level + 1)) + "%"));
			info.add(ChatFormatting.RED + I18nUtil.resolveKey(KEY_DELAY, "+" + (level * 50) + "%"));
		}
		if(type == UpgradeType.OVERDRIVE) {
			info.add((BobMathUtil.getBlink() ? ChatFormatting.RED : ChatFormatting.DARK_GRAY) + "YES");
		}
	}

	@Override
	public HashMap<UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.SPEED, 3);
		upgrades.put(UpgradeType.POWER, 3);
		upgrades.put(UpgradeType.OVERDRIVE, 3);
		return upgrades;
	}
}
