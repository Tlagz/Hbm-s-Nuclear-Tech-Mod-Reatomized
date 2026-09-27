package com.hbm.tileentity.machine;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMiningLaser;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.recipes.CentrifugeRecipes;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.inventory.recipes.CrystallizerRecipes.CrystallizerRecipe;
import com.hbm.inventory.recipes.ShredderRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.Library;
import com.hbm.main.ModSounds;
import com.hbm.tileentity.IUpgradeInfoProvider;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.DirPos;
import com.hbm.util.InventoryUtil;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFluidStandardSenderMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Mining laser: hangs from a ceiling and strips the world below it layer by layer, collecting the drops (with
 * fortune, or processed by a smelter/shredder/centrifuge/crystallizer upgrade), damming off liquids with sandbags
 * and pushing its output into chests and crates next to it. Oil ore turns into crude oil in its tank.
 * Slots: 0 battery, 1-8 upgrades, 9-29 output.
 *
 * The nullifier also voids the 1.21 stone types (deepslate, tuff, andesite...), the original only knew stone.
 */
public class TileEntityMachineMiningLaser extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardSenderMK2, IUpgradeInfoProvider, IControlReceiver, MenuProvider {

	public long power;
	public int age = 0;
	public static final long maxPower = 100000000;
	public static final int consumption = 10000;
	public FluidTank tank;

	public boolean isOn;
	private boolean redstonePowered;
	public int targetX;
	public int targetY = Integer.MIN_VALUE;
	public int targetZ;
	public int lastTargetX;
	public int lastTargetY;
	public int lastTargetZ;
	public boolean beam;
	double breakProgress;
	private double clientBreakProgress;

	public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);

	public TileEntityMachineMiningLaser(BlockPos pos, BlockState state) {
		super(ModTileEntities.MINING_LASER.get(), pos, state, 30);
		tank = new FluidTank(Fluids.OIL, 64_000);
	}

	@Override
	public String getName() {
		return "container.miningLaser";
	}

	private DirPos[] getConPos() {
		return new DirPos[] {
			new DirPos(worldPosition.east(2), Direction.EAST),
			new DirPos(worldPosition.west(2), Direction.WEST),
			new DirPos(worldPosition.south(2), Direction.SOUTH),
			new DirPos(worldPosition.north(2), Direction.NORTH),
		};
	}

	private boolean isMultiblockRedstonePowered() {
		for(DirPos conPos : getConPos()) {
			if(level.hasNeighborSignal(conPos.relative(conPos.getDir().getOpposite()))) return true;
		}
		return false;
	}

	@Override
	public void updateEntity() {

		if(isServer()) {

			this.trySubscribe(level, worldPosition.above(2), Direction.UP);

			for(DirPos pos : getConPos()) {
				if(tank.getFill() > 0) this.tryProvide(tank, level, pos, pos.getDir());
			}

			power = Library.chargeTEFromItems(slots, 0, power, maxPower);

			//reset progress if the position changes
			if(lastTargetX != targetX || lastTargetY != targetY || lastTargetZ != targetZ)
				breakProgress = 0;

			//set last positions for interpolation and the like
			lastTargetX = targetX;
			lastTargetY = targetY;
			lastTargetZ = targetZ;

			boolean prevRedstone = this.redstonePowered;
			this.redstonePowered = this.isMultiblockRedstonePowered();
			if(prevRedstone != this.redstonePowered) this.setChanged();

			boolean shouldRun = this.isOn && !this.redstonePowered;

			if(shouldRun) {

				upgradeManager.checkSlots(slots, 1, 8);
				int cycles = 1 + upgradeManager.getLevel(UpgradeType.OVERDRIVE);
				int speed = 1 + upgradeManager.getLevel(UpgradeType.SPEED);
				int range = 1 + upgradeManager.getLevel(UpgradeType.EFFECT) * 2;
				int fortune = upgradeManager.getLevel(UpgradeType.FORTUNE);
				int consumption = TileEntityMachineMiningLaser.consumption
						- (TileEntityMachineMiningLaser.consumption * upgradeManager.getLevel(UpgradeType.POWER) / 16)
						+ (TileEntityMachineMiningLaser.consumption * upgradeManager.getLevel(UpgradeType.SPEED) / 16);

				for(int i = 0; i < cycles; i++) {

					if(power < consumption) {
						beam = false;
						break;
					}

					power -= consumption;

					// back to the top once the bottom of the world is reached (and on the very first run)
					if(targetY <= level.getMinBuildHeight())
						targetY = worldPosition.getY() - 2;

					scan(range);

					BlockPos target = new BlockPos(targetX, targetY, targetZ);
					BlockState state = level.getBlockState(target);

					if(state.getBlock() instanceof LiquidBlock) {
						level.removeBlock(target, false);
						buildDam();
						continue;
					}

					if(beam && canBreak(state, target)) {

						breakProgress += getBreakSpeed(speed);
						clientBreakProgress = Math.min(breakProgress, 1);

						if(breakProgress < 1) {
							level.destroyBlockProgress(worldPosition.hashCode(), target, (int) Math.floor(breakProgress * 10));
						} else {
							level.destroyBlockProgress(worldPosition.hashCode(), target, -1);
							breakBlock(fortune);
							buildDam();
						}
					}
				}
			} else {
				targetY = worldPosition.getY() - 2;
				beam = false;
			}

			for(DirPos pos : getConPos()) {
				this.tryFillContainer(pos);
			}

			this.networkPackNT(250);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(this.power);
		buf.writeInt(this.lastTargetX);
		buf.writeInt(this.lastTargetY);
		buf.writeInt(this.lastTargetZ);
		buf.writeInt(this.targetX);
		buf.writeInt(this.targetY);
		buf.writeInt(this.targetZ);
		buf.writeBoolean(this.beam);
		buf.writeBoolean(this.isOn);
		buf.writeDouble(this.clientBreakProgress);
		tank.serialize(buf);
		buf.writeBoolean(this.redstonePowered);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.power = buf.readLong();
		this.lastTargetX = buf.readInt();
		this.lastTargetY = buf.readInt();
		this.lastTargetZ = buf.readInt();
		this.targetX = buf.readInt();
		this.targetY = buf.readInt();
		this.targetZ = buf.readInt();
		this.beam = buf.readBoolean();
		this.isOn = buf.readBoolean();
		this.breakProgress = buf.readDouble();
		tank.deserialize(buf);
		this.redstonePowered = buf.readBoolean();
	}

	/** Liquids next to the freshly dug hole get walled off with sandbags */
	private void buildDam() {
		BlockPos target = new BlockPos(targetX, targetY, targetZ);
		for(Direction dir : Direction.values()) {
			if(level.getBlockState(target.relative(dir)).getBlock() instanceof LiquidBlock) level.setBlockAndUpdate(target.relative(dir), ModBlocks.barricade.get().defaultBlockState());
		}
	}

	/** Pushes the output into chests, crates, safes and hoppers at the ports */
	private void tryFillContainer(BlockPos pos) {

		BlockState b = level.getBlockState(pos);
		if(!b.is(Blocks.CHEST) && !b.is(Blocks.TRAPPED_CHEST) && !b.is(ModBlocks.crate_iron.get()) && !b.is(ModBlocks.crate_desh.get()) &&
				!b.is(ModBlocks.crate_steel.get()) && !b.is(ModBlocks.safe.get()) && !b.is(Blocks.HOPPER))
			return;

		BlockEntity te = level.getBlockEntity(pos);
		if(!(te instanceof Container inventory))
			return;

		for(int i = 9; i <= 29; i++) {

			if(!slots.get(i).isEmpty()) {
				int prev = slots.get(i).getCount();
				slots.set(i, addToContainer(inventory, slots.get(i)));

				if(slots.get(i).isEmpty() || slots.get(i).getCount() < prev)
					return;
			}
		}
	}

	/** The original's InventoryUtil.tryAddItemToInventory on an IInventory, returns what didn't fit */
	private static ItemStack addToContainer(Container inv, ItemStack stack) {
		stack = stack.copy();

		for(int i = 0; i < inv.getContainerSize() && !stack.isEmpty(); i++) {
			ItemStack slot = inv.getItem(i);
			if(!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, stack) && inv.canPlaceItem(i, stack)) {
				int transfer = Math.min(stack.getCount(), Math.min(slot.getMaxStackSize(), inv.getMaxStackSize()) - slot.getCount());
				if(transfer > 0) {
					slot.grow(transfer);
					stack.shrink(transfer);
					inv.setChanged();
				}
			}
		}

		for(int i = 0; i < inv.getContainerSize() && !stack.isEmpty(); i++) {
			if(inv.getItem(i).isEmpty() && inv.canPlaceItem(i, stack)) {
				int transfer = Math.min(stack.getCount(), Math.min(stack.getMaxStackSize(), inv.getMaxStackSize()));
				inv.setItem(i, stack.copyWithCount(transfer));
				stack.shrink(transfer);
			}
		}

		return stack.isEmpty() ? ItemStack.EMPTY : stack;
	}

	private void spawn(BlockPos target, ItemStack stack) {
		level.addFreshEntity(new ItemEntity(level, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, stack));
	}

	private void breakBlock(int fortune) {

		BlockPos target = new BlockPos(targetX, targetY, targetZ);
		BlockState state = level.getBlockState(target);
		Block b = state.getBlock();
		boolean normal = true;

		ItemStack stack = new ItemStack(b.asItem());

		if(!stack.isEmpty()) {
			if(hasCrystallizer()) {

				CrystallizerRecipe result = CrystallizerRecipes.getOutput(stack, Fluids.PEROXIDE);
				if(result == null) result = CrystallizerRecipes.getOutput(stack, Fluids.SULFURIC_ACID);

				if(result != null) {
					spawn(target, result.output.copy());
					normal = false;
				}

			} else if(hasCentrifuge()) {

				ItemStack[] result = CentrifugeRecipes.getOutput(stack);
				if(result != null) {
					for(ItemStack sta : result) {
						if(sta != null && !sta.isEmpty()) {
							spawn(target, sta.copy());
							normal = false;
						}
					}
				}

			} else if(hasShredder()) {

				ItemStack result = ShredderRecipes.getShredderResult(stack);
				if(result != null && !result.isEmpty() && !result.is(ModItems.scrap.get())) {
					spawn(target, result.copy());
					normal = false;
				}

			} else if(hasSmelter()) {

				SingleRecipeInput input = new SingleRecipeInput(stack);
				ItemStack result = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, level).map(r -> r.value().assemble(input, level.registryAccess())).orElse(ItemStack.EMPTY);
				if(!result.isEmpty()) {
					spawn(target, result.copy());
					normal = false;
				}
			}
		}

		if(normal && level instanceof ServerLevel server) {
			for(ItemStack drop : Block.getDrops(state, server, target, level.getBlockEntity(target), null, getTool(fortune))) spawn(target, drop);
		}
		level.destroyBlock(target, false);

		suckDrops();

		if(doesScream()) {
			level.playSound(null, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, ModSounds.get("block.screm"), SoundSource.BLOCKS, 2000.0F, 1.0F);
		}

		breakProgress = 0;
	}

	/** Loot tables want a tool: a netherite pickaxe carrying the fortune level (the original passed fortune directly) */
	private ItemStack getTool(int fortune) {
		ItemStack tool = new ItemStack(Items.NETHERITE_PICKAXE);
		if(fortune > 0) tool.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE), fortune);
		return tool;
	}

	private static Set<Item> bad;

	private static Set<Item> getBad() {
		if(bad == null) bad = Set.of(
				Blocks.DIRT.asItem(), Blocks.STONE.asItem(), Blocks.COBBLESTONE.asItem(), Blocks.SAND.asItem(), Blocks.SANDSTONE.asItem(), Blocks.GRAVEL.asItem(),
				ModBlocks.basalt.get().asItem(), ModBlocks.stone_gneiss.get().asItem(), Items.FLINT, Items.SNOWBALL, Items.WHEAT_SEEDS,
				Blocks.DEEPSLATE.asItem(), Blocks.COBBLED_DEEPSLATE.asItem(), Blocks.TUFF.asItem(), Blocks.ANDESITE.asItem(), Blocks.DIORITE.asItem(), Blocks.GRANITE.asItem());
		return bad;
	}

	//hahahahahahahaha he said "suck"
	private void suckDrops() {

		int rangeHor = 3;
		int rangeVer = 1;
		boolean nullifier = hasNullifier();

		List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(
				targetX + 0.5 - rangeHor, targetY + 0.5 - rangeVer, targetZ + 0.5 - rangeHor,
				targetX + 0.5 + rangeHor, targetY + 0.5 + rangeVer, targetZ + 0.5 + rangeHor));

		for(ItemEntity item : items) {

			if(!item.isAlive()) continue;

			if(nullifier && getBad().contains(item.getItem().getItem())) {
				item.discard();
				continue;
			}

			if(item.getItem().is(ModBlocks.ore_oil.get().asItem())) {

				tank.setTankType(Fluids.OIL); //just to be sure

				tank.setFill(tank.getFill() + 500);
				if(tank.getFill() > tank.getMaxFill())
					tank.setFill(tank.getMaxFill());

				item.discard();
				continue;
			}

			ItemStack stack = InventoryUtil.tryAddItemToInventory(slots, 9, 29, item.getItem().copy());

			if(stack.isEmpty()) {
				item.discard();
			} else {
				item.setItem(stack.copy()); //copy is not necessary but i'm paranoid due to the kerfuffle of the old drill
			}
		}

		for(LivingEntity mob : level.getEntitiesOfClass(LivingEntity.class, new AABB(targetX - 0.5, targetY - 0.5, targetZ - 0.5, targetX + 1.5, targetY + 1.5, targetZ + 1.5))) {
			mob.igniteForSeconds(5);
		}
	}

	public double getBreakSpeed(int speed) {

		BlockPos target = new BlockPos(targetX, targetY, targetZ);
		float hardness = level.getBlockState(target).getDestroySpeed(level, target) * 15 / speed;

		if(hardness == 0)
			return 1;

		return 1 / hardness;
	}

	public void scan(int range) {

		for(int x = -range; x <= range; x++) {
			for(int z = -range; z <= range; z++) {

				BlockPos pos = new BlockPos(x + worldPosition.getX(), targetY, z + worldPosition.getZ());
				BlockState state = level.getBlockState(pos);

				if(state.getBlock() instanceof LiquidBlock) {
					continue;
				}

				if(canBreak(state, pos)) {
					targetX = pos.getX();
					targetZ = pos.getZ();
					beam = true;
					return;
				}
			}
		}

		beam = false;
		targetY--;
	}

	private boolean canBreak(BlockState state, BlockPos pos) {
		return !state.isAir() && state.getDestroySpeed(level, pos) >= 0 && !(state.getBlock() instanceof LiquidBlock) && !state.is(Blocks.BEDROCK);
	}

	private boolean hasUpgrade(Item item) {
		for(int i = 1; i < 9; i++) {
			if(slots.get(i).is(item)) return true;
		}
		return false;
	}

	public int getRange() {
		int range = 1;
		for(int i = 1; i < 9; i++) {
			ItemStack stack = slots.get(i);
			if(stack.is(ModItems.upgrade_effect_1.get())) range += 2;
			else if(stack.is(ModItems.upgrade_effect_2.get())) range += 4;
			else if(stack.is(ModItems.upgrade_effect_3.get())) range += 6;
		}
		return Math.min(range, 25);
	}

	public boolean hasNullifier() { return hasUpgrade(ModItems.upgrade_nullifier.get()); }
	public boolean hasSmelter() { return hasUpgrade(ModItems.upgrade_smelter.get()); }
	public boolean hasShredder() { return hasUpgrade(ModItems.upgrade_shredder.get()); }
	public boolean hasCentrifuge() { return hasUpgrade(ModItems.upgrade_centrifuge.get()); }
	public boolean hasCrystallizer() { return hasUpgrade(ModItems.upgrade_crystallizer.get()); }
	public boolean doesScream() { return hasUpgrade(ModItems.upgrade_screm.get()); }

	public int getConsumption() {
		return consumption;
	}

	public int getWidth() {
		return 1 + getRange() * 2;
	}

	public int getPowerScaled(int i) {
		return (int) ((power * i) / maxPower);
	}

	public int getProgressScaled(int i) {
		return (int) (breakProgress * i);
	}

	public AABB getRenderBoundingBox() {
		return AABB.INFINITE;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemStack, Direction side) {
		return i >= 9 && i <= 29;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		int[] slots = new int[21];
		for(int i = 0; i < 21; i++) slots[i] = i + 9;
		return slots;
	}

	@Override
	public void setItem(int i, ItemStack stack) {
		super.setItem(i, stack);

		if(!stack.isEmpty() && i >= 1 && i <= 8 && stack.getItem() instanceof ItemMachineUpgrade && level != null && !level.isClientSide)
			level.playSound(null, worldPosition.getX() + 0.5, worldPosition.getY() + 1.5, worldPosition.getZ() + 0.5, ModSounds.get("item.upgradePlug"), SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	/** The on/off button */
	@Override
	public void receiveControl(CompoundTag data) {
		this.isOn = !this.isOn;
		this.setChanged();
	}

	@Override public boolean hasPermission(Player player) { return this.stillValid(player); }

	@Override public void setPower(long i) { power = i; }
	@Override public long getPower() { return power; }
	@Override public long getMaxPower() { return maxPower; }

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		tank.readFromNBT(nbt, "oil");
		power = nbt.getLong("power");
		isOn = nbt.getBoolean("isOn");
		redstonePowered = false;
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		tank.writeToNBT(nbt, "oil");
		nbt.putLong("power", power);
		nbt.putBoolean("isOn", isOn);
	}

	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] { tank }; }
	@Override public FluidTank[] getAllTanks() { return new FluidTank[] { tank }; }

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMiningLaser(id, inv, this);
	}

	@Override
	public boolean canProvideInfo(UpgradeType type, int level, boolean extendedInfo) {
		return type == UpgradeType.SPEED || type == UpgradeType.POWER || type == UpgradeType.OVERDRIVE || type == UpgradeType.EFFECT || type == UpgradeType.FORTUNE;
	}

	@Override
	public void provideInfo(UpgradeType type, int level, List<String> info, boolean extendedInfo) {
		info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_mining_laser.get()));
		if(type == UpgradeType.SPEED) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_DELAY, "-" + (100 - 100 / (level + 1)) + "%"));
			info.add(ChatFormatting.RED + I18nUtil.resolveKey(KEY_CONSUMPTION, "+" + (100 * level / 16) + "%"));
		}
		if(type == UpgradeType.POWER) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_CONSUMPTION, "-" + (100 * level / 16) + "%"));
		}
		if(type == UpgradeType.EFFECT) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_RANGE, "+" + (2 * level) + "m"));
		}
		if(type == UpgradeType.FORTUNE) {
			info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(KEY_FORTUNE, "+" + level));
		}
		if(type == UpgradeType.OVERDRIVE) {
			info.add((BobMathUtil.getBlink() ? ChatFormatting.RED : ChatFormatting.DARK_GRAY) + "YES");
		}
	}

	@Override
	public HashMap<UpgradeType, Integer> getValidUpgrades() {
		HashMap<UpgradeType, Integer> upgrades = new HashMap<>();
		upgrades.put(UpgradeType.SPEED, 12);
		upgrades.put(UpgradeType.POWER, 12);
		upgrades.put(UpgradeType.EFFECT, 12);
		upgrades.put(UpgradeType.FORTUNE, 3);
		upgrades.put(UpgradeType.OVERDRIVE, 9);
		return upgrades;
	}
}
