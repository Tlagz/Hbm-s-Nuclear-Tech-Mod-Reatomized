package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineArcFurnaceLarge;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.recipes.ArcFurnaceRecipes;
import com.hbm.inventory.recipes.ArcFurnaceRecipes.ArcFurnaceRecipe;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemArcElectrode;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.main.ModSounds;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.CrucibleUtil;
import com.hbm.util.DirPos;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Electric arc furnace: melts the 20 items of its grid at once with three electrodes, into solid products (solid mode)
 * or molten materials that are poured out of the spout into a mold or channel below (liquid mode). The lid opens
 * between batches to load the next items from the 5 queue slots.
 * Slots: 0-2 electrodes, 3 battery, 4 speed upgrade, 5-24 grid, 25-29 queue.
 *
 * TODO the original's smoke tower and flame particles and the pouring particle, emptying with a shovel (ItemScraps),
 * the crane item transfer methods (distributeInput, collectRequested)
 */
public class TileEntityMachineArcFurnaceLarge extends TileEntityMachineBase implements IEnergyReceiverMK2, IControlReceiver, IUpgradeInfoProvider, MenuProvider {

	public long power;
	public static final long maxPower = 2_500_000;
	public boolean liquidMode = false;
	public float progress;
	public boolean isProgressing;
	public boolean hasMaterial;
	public int delay;
	public int upgrade;

	public float lid;
	public float prevLid;
	public int approachNum;
	public float syncLid;

	private AudioWrapper audioLid;
	private AudioWrapper audioProgress;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public byte[] electrodes = new byte[3];
	public static final byte ELECTRODE_NONE = 0;
	public static final byte ELECTRODE_FRESH = 1;
	public static final byte ELECTRODE_USED = 2;
	public static final byte ELECTRODE_DEPLETED = 3;

	public int getMaxInputSize() {
		return upgrade == 0 ? 1 : upgrade == 1 ? 4 : upgrade == 2 ? 8 : 16;
	}

	public static final int maxLiquid = MaterialShapes.BLOCK.q(128);
	public List<MaterialStack> liquids = new ArrayList<>();

	public TileEntityMachineArcFurnaceLarge(BlockPos pos, BlockState state) {
		super(ModTileEntities.ARC_FURNACE.get(), pos, state, 30);
	}

	@Override
	public String getName() {
		return "container.machineArcFurnaceLarge";
	}

	@Override
	public void setItem(int i, ItemStack stack) {
		super.setItem(i, stack);

		if(level != null && !stack.isEmpty() && stack.getItem() instanceof ItemMachineUpgrade && i == 4) {
			level.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, ModSounds.get("item.upgradePlug"), SoundSource.BLOCKS, 1.0F, 1.0F);
		}
	}

	public Direction getDir() {
		return Direction.from3DDataValue(BlockDummyable.getMeta(getBlockState()) - BlockDummyable.offset);
	}

	private ArcFurnaceRecipe getRecipe(ItemStack stack) {
		return ArcFurnaceRecipes.getOutput(stack, this.liquidMode, level);
	}

	@Override
	public void updateEntity() {

		upgradeManager.checkSlots(slots, 4, 4);
		this.upgrade = upgradeManager.getLevel(UpgradeType.SPEED);

		if(isServer()) {

			this.power = Library.chargeTEFromItems(slots, 3, power, maxPower);
			this.isProgressing = false;

			for(DirPos pos : getConPos()) this.trySubscribe(level, pos);

			if(lid > 0) loadIngredients();

			if(power > 0) {

				boolean ingredients = this.hasIngredients();
				boolean electrodes = this.hasElectrodes();

				int consumption = (int) (1_000 * Math.pow(5, upgrade));

				if(ingredients && electrodes && delay <= 0 && this.liquids.isEmpty()) {
					if(lid > 0) {
						lid -= 1F / (60F / (upgrade * 0.5F + 1));
						if(lid < 0) lid = 0;
						this.progress = 0;
					} else {

						if(power >= consumption) {
							int duration = 400 / (upgrade * 2 + 1);
							this.progress += 1F / duration;
							this.isProgressing = true;
							this.power -= consumption;
							if(this.progress >= 1F) {
								this.process();
								this.progress = 0;
								this.setChanged();
								this.delay = (int) (120 / (upgrade * 0.5 + 1));
								PollutionHandler.incrementPollution(level, worldPosition, PollutionType.SOOT, 10F);
							}
						}
					}
				} else {
					if(this.delay > 0) delay--;
					this.progress = 0;
					if(lid < 1) {
						lid += 1F / (60F / (upgrade * 0.5F + 1));
						if(lid > 1) lid = 1;
					}
				}

				hasMaterial = ingredients;
			}

			this.decideElectrodeState();

			if(!hasMaterial) hasMaterial = this.hasIngredients();

			// pour into whatever is below the spout, nothing is spilled if there is no mold or channel
			if(!this.liquids.isEmpty() && this.lid > 0F) {
				Direction dir = getDir();
				CrucibleUtil.pourFullStack(level, worldPosition.getX() + 0.5D + dir.getStepX() * 2.875D, worldPosition.getY() + 1.25D, worldPosition.getZ() + 0.5D + dir.getStepZ() * 2.875D,
						6, true, this.liquids, MaterialShapes.INGOT.q(1), new CrucibleUtil.Impact());
			}

			this.liquids.removeIf(o -> o.amount <= 0);

			this.networkPackNT(150);
		} else {

			this.prevLid = this.lid;

			if(this.approachNum > 0) {
				this.lid = this.lid + ((this.syncLid - this.lid) / (float) this.approachNum);
				--this.approachNum;
			} else {
				this.lid = this.syncLid;
			}

			if(this.lid != this.prevLid) {
				if(this.audioLid == null || !this.audioLid.isPlaying()) {
					this.audioLid = AudioWrapper.getLoopedSound("hbm:door.wgh_start", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), this.getVolume(0.75F), 15F, 1.0F, 5);
					this.audioLid.startSound();
				}
				this.audioLid.keepAlive();
			} else {
				if(this.audioLid != null) {
					this.audioLid.stopSound();
					this.audioLid = null;
				}
			}

			if((lid == 1 || lid == 0) && lid != prevLid && !(this.prevLid == 0 && this.lid == 1)) {
				level.playLocalSound(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, ModSounds.get("door.wgh_stop"), SoundSource.BLOCKS, this.getVolume(1), 1F, false);
			}

			if(this.isProgressing) {
				if(this.audioProgress == null || !this.audioProgress.isPlaying()) {
					this.audioProgress = AudioWrapper.getLoopedSound("hbm:block.electricHum", worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), this.getVolume(1.5F), 15F, 0.75F, 5);
					this.audioProgress.startSound();
				}
				this.audioProgress.updatePitch(0.75F);
				this.audioProgress.keepAlive();
			} else {
				if(this.audioProgress != null) {
					this.audioProgress.stopSound();
					this.audioProgress = null;
				}
			}

			// smoke rising out of the opening furnace, flames while it closes over hot material (vanilla stand-ins)
			if(this.lid != this.prevLid && this.lid > this.prevLid && !(this.prevLid == 0 && this.lid == 1)) {
				for(int i = 0; i < 3; i++) {
					level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, worldPosition.getX() + 0.5 + level.random.nextGaussian() * 0.5, worldPosition.getY() + 4, worldPosition.getZ() + 0.5 + level.random.nextGaussian() * 0.5, 0, 0.05, 0);
				}
			}

			if(this.lid != this.prevLid && this.lid < this.prevLid && this.lid > 0.5F && this.hasMaterial && level.random.nextInt(5) == 0) {
				for(int i = 0; i < 2; i++) {
					level.addParticle(ParticleTypes.FLAME, worldPosition.getX() + 0.5 + level.random.nextGaussian() * 0.5, worldPosition.getY() + 2.75, worldPosition.getZ() + 0.5 + level.random.nextGaussian() * 0.5, 0, 0.05, 0);
				}
			}
		}
	}

	/** Moves items from the input queue to the main grid */
	public void loadIngredients() {

		boolean markDirty = false;

		for(int q /* queue */ = 25; q < 30; q++) {
			if(slots.get(q).isEmpty()) continue;
			ArcFurnaceRecipe recipe = getRecipe(slots.get(q));
			if(recipe == null) continue;
			int max = this.getMaxInputSize();
			int recipeMax = this.liquidMode ? max : slots.get(q).getMaxStackSize() / recipe.solidOutput.getCount();
			max = Math.min(max, recipeMax);

			// add to existing stacks
			for(int i /* ingredient */ = 5; i < 25; i++) {
				ItemStack ingredient = slots.get(i);
				if(ingredient.isEmpty()) continue;
				if(!ItemStack.isSameItemSameComponents(slots.get(q), ingredient)) continue;
				int toMove = Math.min(Math.min(ingredient.getMaxStackSize() - ingredient.getCount(), slots.get(q).getCount()), max - ingredient.getCount());
				if(toMove > 0) {
					this.removeItem(q, toMove);
					ingredient.grow(toMove);
					markDirty = true;
				}
				if(slots.get(q).isEmpty()) break;
			}

			// add to empty slot
			if(!slots.get(q).isEmpty()) for(int i /* ingredient */ = 5; i < 25; i++) {
				if(!slots.get(i).isEmpty()) continue;
				int toMove = Math.min(max, slots.get(q).getCount());
				slots.set(i, slots.get(q).copyWithCount(toMove));
				this.removeItem(q, toMove);
				markDirty = true;
				if(slots.get(q).isEmpty()) break;
			}
		}

		if(markDirty) this.setChanged();
	}

	public void decideElectrodeState() {
		for(int i = 0; i < 3; i++) {
			ItemStack stack = slots.get(i);
			if(!stack.isEmpty()) {
				if(stack.getItem() instanceof ItemArcElectrode) {
					if(this.isProgressing || ItemArcElectrode.getDurability(stack) > 0) this.electrodes[i] = ELECTRODE_USED;
					else this.electrodes[i] = ELECTRODE_FRESH;
					continue;
				}
				if(isBurntElectrode(stack)) { this.electrodes[i] = ELECTRODE_DEPLETED; continue; }
			}
			this.electrodes[i] = ELECTRODE_NONE;
		}
	}

	private static boolean isBurntElectrode(ItemStack stack) {
		for(var burnt : ModItems.arc_electrode_burnt.values()) if(stack.is(burnt.get())) return true;
		return false;
	}

	public void process() {

		for(int i = 5; i < 25; i++) {
			if(slots.get(i).isEmpty()) continue;
			ArcFurnaceRecipe recipe = getRecipe(slots.get(i));
			if(recipe == null) continue;

			if(!liquidMode && recipe.solidOutput != null) {
				int amount = slots.get(i).getCount();
				slots.set(i, recipe.solidOutput.copyWithCount(recipe.solidOutput.getCount() * amount));
			}

			if(liquidMode && recipe.fluidOutput != null) {

				while(!slots.get(i).isEmpty()) {
					int liquid = getStackAmount(liquids);
					int toAdd = getStackAmount(recipe.fluidOutput);

					if(liquid + toAdd <= maxLiquid) {
						this.removeItem(i, 1);
						for(MaterialStack stack : recipe.fluidOutput) {
							this.addToStack(stack);
						}
					} else {
						break;
					}
				}
			}
		}

		// burnt out electrodes turn into the burnt variant of their type
		for(int i = 0; i < 3; i++) {
			ItemStack stack = slots.get(i);
			if(stack.getItem() instanceof ItemArcElectrode electrode && ItemArcElectrode.damage(stack)) {
				slots.set(i, new ItemStack(ModItems.arc_electrode_burnt.get(electrode.type).get()));
			}
		}
	}

	public boolean hasIngredients() {

		for(int i = 5; i < 25; i++) {
			if(slots.get(i).isEmpty()) continue;
			ArcFurnaceRecipe recipe = getRecipe(slots.get(i));
			if(recipe == null) continue;
			if(liquidMode && recipe.fluidOutput != null) return true;
			if(!liquidMode && recipe.solidOutput != null) return true;
		}

		return false;
	}

	public boolean hasElectrodes() {
		for(int i = 0; i < 3; i++) {
			if(!(slots.get(i).getItem() instanceof ItemArcElectrode)) return false;
		}
		return true;
	}

	private static final int[] slot_access = new int[] {
			0, 1, 2,
			5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
			25, 26, 27, 28, 29};

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return slot_access;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, Direction side) {
		if(slot < 3) return stack.getItem() instanceof ItemArcElectrode;
		if(slot >= 25) return getRecipe(stack) != null;
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot < 3) return stack.getItem() instanceof ItemArcElectrode;
		if(slot > 4) {
			ArcFurnaceRecipe recipe = getRecipe(stack);
			if(recipe == null) return false;
			return liquidMode ? recipe.fluidOutput != null : recipe.solidOutput != null;
		}
		return false;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, Direction side) {
		if(slot < 3) return lid >= 1 && !(stack.getItem() instanceof ItemArcElectrode);
		if(slot > 4 && slot < 25) return lid > 0 && getRecipe(stack) == null;
		if(slot >= 25) return getRecipe(stack) == null;
		return false;
	}

	public void addToStack(MaterialStack matStack) {

		for(MaterialStack mat : liquids) {
			if(mat.material == matStack.material) {
				mat.amount += matStack.amount;
				return;
			}
		}

		liquids.add(matStack.copy());
	}

	public static int getStackAmount(List<MaterialStack> stack) {
		int amount = 0;
		for(MaterialStack mat : stack) amount += mat.amount;
		return amount;
	}

	public static int getStackAmount(MaterialStack[] stack) {
		int amount = 0;
		for(MaterialStack mat : stack) amount += mat.amount;
		return amount;
	}

	protected DirPos[] getConPos() {
		Direction dir = getDir();
		Direction rot = dir.getClockWise();
		BlockPos p = worldPosition;

		return new DirPos[] {
				new DirPos(p.relative(dir, 3).relative(rot), dir),
				new DirPos(p.relative(dir, 3).relative(rot.getOpposite()), dir),
				new DirPos(p.relative(rot, 3).relative(dir), rot),
				new DirPos(p.relative(rot, 3).relative(dir.getOpposite()), rot),
				new DirPos(p.relative(rot, -3).relative(dir), rot.getOpposite()),
				new DirPos(p.relative(rot, -3).relative(dir.getOpposite()), rot.getOpposite())
		};
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeFloat(progress);
		buf.writeFloat(lid);
		buf.writeBoolean(isProgressing);
		buf.writeBoolean(liquidMode);
		buf.writeBoolean(hasMaterial);

		for(int i = 0; i < 3; i++) buf.writeByte(electrodes[i]);

		buf.writeShort(liquids.size());

		for(MaterialStack mat : liquids) {
			buf.writeInt(mat.material.id);
			buf.writeInt(mat.amount);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.progress = buf.readFloat();
		this.syncLid = buf.readFloat();
		this.isProgressing = buf.readBoolean();
		this.liquidMode = buf.readBoolean();
		this.hasMaterial = buf.readBoolean();

		for(int i = 0; i < 3; i++) electrodes[i] = buf.readByte();

		int mats = buf.readShort();

		this.liquids.clear();
		for(int i = 0; i < mats; i++) {
			liquids.add(new MaterialStack(Mats.matById.get(buf.readInt()), buf.readInt()));
		}

		if(syncLid != 0 && syncLid != 1) this.approachNum = 2;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);

		this.power = nbt.getLong("power");
		this.liquidMode = nbt.getBoolean("liquidMode");
		this.progress = nbt.getFloat("progress");
		this.lid = nbt.getFloat("lid");
		this.delay = nbt.getInt("delay");

		int count = nbt.getShort("count");
		liquids.clear();

		for(int i = 0; i < count; i++) {
			var material = Mats.matById.get(nbt.getInt("m" + i));
			if(material != null) liquids.add(new MaterialStack(material, nbt.getInt("a" + i)));
		}
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putLong("power", power);
		nbt.putBoolean("liquidMode", liquidMode);
		nbt.putFloat("progress", progress);
		nbt.putFloat("lid", lid);
		nbt.putInt("delay", delay);

		int count = liquids.size();
		nbt.putShort("count", (short) count);
		for(int i = 0; i < count; i++) {
			MaterialStack mat = liquids.get(i);
			nbt.putInt("m" + i, mat.material.id);
			nbt.putInt("a" + i, mat.amount);
		}
	}

	@Override
	public void onChunkUnloaded() {
		super.onChunkUnloaded();
		stopAudio();
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		stopAudio();
	}

	private void stopAudio() {
		if(audioLid != null) { audioLid.stopSound(); audioLid = null; }
		if(audioProgress != null) { audioProgress.stopSound(); audioProgress = null; }
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 3, worldPosition.getY(), worldPosition.getZ() - 3, worldPosition.getX() + 4, worldPosition.getY() + 6, worldPosition.getZ() + 4);
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineArcFurnaceLarge(id, inv, this);
	}

	@Override
	public boolean hasPermission(Player player) {
		return this.stillValid(player);
	}

	@Override
	public void receiveControl(CompoundTag data) {
		if(data.getBoolean("liquid")) {
			this.liquidMode = !this.liquidMode;
			this.setChanged();
		}
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_arc_furnace.get()));
		if(type == UpgradeType.SPEED) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_DELAY, "-" + (100 - 100 / (level * 2 + 1)) + "%"));
			info.add(ChatFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + ((int) Math.pow(5, level) * 100 - 100) + "%"));
		}
	}

	@Override
	public HashMap<UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.SPEED, 3);
		return upgrades;
	}
}
