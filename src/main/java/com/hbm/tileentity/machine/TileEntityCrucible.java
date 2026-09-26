package com.hbm.tileentity.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerCrucible;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.recipes.CrucibleRecipe;
import com.hbm.inventory.recipes.CrucibleRecipes;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.CrucibleUtil;

import api.hbm.tile.IHeatSource;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Crucible: takes heat from the heater below (firebox, oven), melts the items in its 9 slots (or dropped into it) and
 * alloys them with the selected recipe. Recipe materials pour out of the front, everything else out of the back.
 * Slot 0 is unused (the original's template slot), 1-9 are the inputs, one item each.
 *
 * TODO the smoke tower and pouring particles, config (IConfigurableMachine), copy tool (IMetalCopiable)
 */
public class TileEntityCrucible extends TileEntityMachineBase implements IControlReceiver, MenuProvider {

	public int heat;
	public int progress;
	public String recipe = "null";
	public List<MaterialStack> recipeStack = new ArrayList<>();
	public List<MaterialStack> wasteStack = new ArrayList<>();

	/* CONFIGURABLE CONSTANTS */
	public static int recipeZCapacity = MaterialShapes.BLOCK.q(16);
	public static int wasteZCapacity = MaterialShapes.BLOCK.q(16);
	public static int processTime = 20_000;
	public static double diffusion = 0.25D;
	public static int maxHeat = 100_000;

	public TileEntityCrucible(BlockPos pos, BlockState state) {
		super(ModTileEntities.CRUCIBLE.get(), pos, state, 10);
	}

	@Override
	public String getName() {
		return "container.machineCrucible";
	}

	/** prevents clogging */
	@Override
	public int getMaxStackSize() {
		return 1;
	}

	private Direction getDir() {
		return Direction.from3DDataValue(BlockDummyable.getMeta(getBlockState()) - BlockDummyable.offset);
	}

	@Override
	public void updateEntity() {

		if(isServer()) {
			tryPullHeat();

			/* collect items */
			if(level.getGameTime() % 5 == 0) {
				List<ItemEntity> list = level.getEntitiesOfClass(ItemEntity.class, new AABB(worldPosition.getX() - 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() - 0.5, worldPosition.getX() + 1.5, worldPosition.getY() + 1, worldPosition.getZ() + 1.5));

				for(ItemEntity item : list) {
					if(!item.isAlive()) continue;
					ItemStack stack = item.getItem();

					if(this.isItemSmeltable(stack)) {

						for(int i = 1; i < 10; i++) {
							if(slots.get(i).isEmpty()) {

								slots.set(i, stack.copyWithCount(1));
								if(stack.getCount() == 1) {
									item.discard();
									break;
								} else {
									stack.shrink(1);
									item.setItem(stack);
								}
								this.setChanged();
							}
						}
					}
				}
			}

			int totalCap = recipeZCapacity + wasteZCapacity;
			int totalMass = 0;

			for(MaterialStack stack : recipeStack) totalMass += stack.amount;
			for(MaterialStack stack : wasteStack) totalMass += stack.amount;

			double fill = ((double) totalMass / (double) totalCap) * 0.875D;

			// whatever steps into the molten metal burns
			if(totalMass > 0) {
				List<LivingEntity> living = level.getEntitiesOfClass(LivingEntity.class, new AABB(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5 + fill, worldPosition.getZ() + 0.5).inflate(1, 0, 1));
				for(LivingEntity entity : living) {
					entity.hurt(level.damageSources().lava(), 5F);
					entity.igniteForSeconds(5);
				}
			}

			/* smelt items from buffer */
			if(!trySmelt()) {
				this.progress = 0;
			}

			tryRecipe();

			/* pour waste stack */
			if(!this.wasteStack.isEmpty()) {
				Direction dir = getDir().getOpposite();
				CrucibleUtil.pourFullStack(level, worldPosition.getX() + 0.5D + dir.getStepX() * 1.875D, worldPosition.getY() + 0.25D, worldPosition.getZ() + 0.5D + dir.getStepZ() * 1.875D, 6, true, this.wasteStack, MaterialShapes.NUGGET.q(3), null);
				PollutionHandler.incrementPollution(level, worldPosition, PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND / 20F);
			}

			/* pour recipe stack */
			if(!this.recipeStack.isEmpty()) {
				Direction dir = getDir();
				List<MaterialStack> toCast = new ArrayList<>();

				CrucibleRecipe recipe = this.getLoadedRecipe();
				//if no recipe is loaded, everything from the recipe stack will be drainable
				if(recipe == null) {
					toCast.addAll(this.recipeStack);
				} else {
					for(MaterialStack stack : this.recipeStack) {
						for(MaterialStack output : recipe.output) {
							if(stack.material == output.material) {
								toCast.add(stack);
								break;
							}
						}
					}
				}

				if(!toCast.isEmpty()) {
					CrucibleUtil.pourFullStack(level, worldPosition.getX() + 0.5D + dir.getStepX() * 1.875D, worldPosition.getY() + 0.25D, worldPosition.getZ() + 0.5D + dir.getStepZ() * 1.875D, 6, true, toCast, MaterialShapes.NUGGET.q(3), null);
				}
				PollutionHandler.incrementPollution(level, worldPosition, PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND / 20F);
			}

			/* clean up stacks */
			this.recipeStack.removeIf(o -> o.amount <= 0);
			this.wasteStack.removeIf(x -> x.amount <= 0);

			/* sync */
			this.networkPackNT(25);
		} else {

			// smoke over molten contents (a vanilla stand-in for the original's smoke tower)
			if((!this.recipeStack.isEmpty() || !this.wasteStack.isEmpty()) && level.getGameTime() % 10 == 0) {
				level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5, 0, 0.05, 0);
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(progress);
		buf.writeInt(heat);
		new net.minecraft.network.FriendlyByteBuf(buf).writeUtf(recipe);
		writeStacks(buf, recipeStack);
		writeStacks(buf, wasteStack);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		progress = buf.readInt();
		heat = buf.readInt();
		recipe = new net.minecraft.network.FriendlyByteBuf(buf).readUtf();
		readStacks(buf, recipeStack);
		readStacks(buf, wasteStack);
	}

	private static void writeStacks(ByteBuf buf, List<MaterialStack> stacks) {
		buf.writeShort(stacks.size());
		for(MaterialStack sta : stacks) {
			buf.writeInt(sta.material == null ? -1 : sta.material.id);
			buf.writeInt(sta.amount);
		}
	}

	private static void readStacks(ByteBuf buf, List<MaterialStack> stacks) {
		stacks.clear();
		int mats = buf.readShort();
		for(int i = 0; i < mats; i++) {
			int id = buf.readInt();
			int amount = buf.readInt();
			NTMMaterial mat = Mats.matById.get(id);
			if(mat != null) stacks.add(new MaterialStack(mat, amount));
		}
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.recipe = nbt.getString("recipe");
		recipeStack.clear();
		wasteStack.clear();
		int[] rec = nbt.getIntArray("rec");
		for(int i = 0; i < rec.length / 2; i++) {
			NTMMaterial mat = Mats.matById.get(rec[i * 2]);
			if(mat != null) recipeStack.add(new MaterialStack(mat, rec[i * 2 + 1]));
		}
		int[] was = nbt.getIntArray("was");
		for(int i = 0; i < was.length / 2; i++) {
			NTMMaterial mat = Mats.matById.get(was[i * 2]);
			if(mat != null) wasteStack.add(new MaterialStack(mat, was[i * 2 + 1]));
		}
		this.progress = nbt.getInt("progress");
		this.heat = nbt.getInt("heat");
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putString("recipe", this.recipe);
		int[] rec = new int[recipeStack.size() * 2];
		int[] was = new int[wasteStack.size() * 2];
		for(int i = 0; i < recipeStack.size(); i++) { MaterialStack sta = recipeStack.get(i); rec[i * 2] = sta.material.id; rec[i * 2 + 1] = sta.amount; }
		for(int i = 0; i < wasteStack.size(); i++) { MaterialStack sta = wasteStack.get(i); was[i * 2] = sta.material.id; was[i * 2 + 1] = sta.amount; }
		nbt.putIntArray("rec", rec);
		nbt.putIntArray("was", was);
		nbt.putInt("progress", progress);
		nbt.putInt("heat", heat);
	}

	protected void tryPullHeat() {

		if(this.heat >= maxHeat) return;

		BlockEntity con = level.getBlockEntity(worldPosition.below());

		if(con instanceof IHeatSource source) {
			int diff = source.getHeatStored() - this.heat;

			if(diff == 0) {
				return;
			}

			diff = Math.min(diff, maxHeat - this.heat);

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

	protected boolean trySmelt() {

		if(this.heat < maxHeat / 2) return false;

		int slot = this.getFirstSmeltableSlot();
		if(slot == -1) return false;

		int delta = this.heat - (maxHeat / 2);
		delta *= 0.05;

		this.progress += delta;
		this.heat -= delta;

		if(this.progress >= processTime) {
			this.progress = 0;

			List<MaterialStack> materials = Mats.getSmeltingMaterialsFromItem(slots.get(slot));
			CrucibleRecipe recipe = getLoadedRecipe();

			for(MaterialStack material : materials) {
				boolean recipeMaterial = recipe != null && (getQuantaFromType(recipe.input, material.material) > 0 || getQuantaFromType(recipe.output, material.material) > 0);

				if(recipeMaterial) {
					this.addToStack(this.recipeStack, material);
				} else {
					this.addToStack(this.wasteStack, material);
				}
			}

			this.removeItem(slot, 1);
		}

		return true;
	}

	protected void tryRecipe() {
		CrucibleRecipe recipe = this.getLoadedRecipe();

		if(recipe == null) return;
		if(level.getGameTime() % recipe.frequency > 0) return;

		for(MaterialStack stack : recipe.input) {
			if(getQuantaFromType(this.recipeStack, stack.material) < stack.amount) return;
		}

		for(MaterialStack stack : this.recipeStack) {
			stack.amount -= getQuantaFromType(recipe.input, stack.material);
		}

		outer:
		for(MaterialStack out : recipe.output) {

			for(MaterialStack stack : this.recipeStack) {
				if(stack.material == out.material) {
					stack.amount += out.amount;
					continue outer;
				}
			}

			this.recipeStack.add(out.copy());
		}
	}

	protected int getFirstSmeltableSlot() {

		for(int i = 1; i < 10; i++) {
			ItemStack stack = slots.get(i);
			if(!stack.isEmpty() && isItemSmeltable(stack)) {
				return i;
			}
		}

		return -1;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		return i > 0 && isItemSmeltable(stack);
	}

	public boolean isItemSmeltable(ItemStack stack) {

		List<MaterialStack> materials = Mats.getSmeltingMaterialsFromItem(stack);

		//if there's no materials in there at all, don't smelt
		if(materials.isEmpty()) return false;
		CrucibleRecipe recipe = getLoadedRecipe();

		//needs to be true, will always be true if there's no recipe loaded
		boolean matchesRecipe = recipe == null;

		//the amount of material in the entire recipe input
		int recipeContent = recipe != null ? recipe.getInputAmount() : 0;
		//the total amount of the current waste stack, used for simulation
		int recipeAmount = getQuantaFromType(this.recipeStack, null);
		int wasteAmount = getQuantaFromType(this.wasteStack, null);

		for(MaterialStack mat : materials) {
			//if no recipe is loaded, everything will land in the waste stack
			int recipeInputRequired = recipe != null ? getQuantaFromType(recipe.input, mat.material) : 0;

			//this allows pouring the output material back into the crucible
			if(recipe != null && getQuantaFromType(recipe.output, mat.material) > 0) {
				recipeAmount += mat.amount;
				matchesRecipe = true;
				continue;
			}

			if(recipeInputRequired == 0) {
				//if this type isn't required by the recipe, add it to the waste stack
				wasteAmount += mat.amount;
			} else {

				//the maximum is the recipe's ratio scaled up to the recipe stack's capacity
				int matMaximum = recipeInputRequired * recipeZCapacity / recipeContent;
				int amountStored = getQuantaFromType(recipeStack, mat.material);

				matchesRecipe = true;

				recipeAmount += mat.amount;

				//if the amount of that input would exceed the amount dictated by the recipe, return false
				if(amountStored + mat.amount > matMaximum)
					return false;
			}
		}

		//if the amount doesn't exceed the capacity and the recipe matches (or isn't null), return true
		return recipeAmount <= recipeZCapacity && wasteAmount <= wasteZCapacity && matchesRecipe;
	}

	public void addToStack(List<MaterialStack> stack, MaterialStack matStack) {

		for(MaterialStack mat : stack) {
			if(mat.material == matStack.material) {
				mat.amount += matStack.amount;
				return;
			}
		}

		stack.add(matStack.copy());
	}

	public CrucibleRecipe getLoadedRecipe() {
		return CrucibleRecipes.INSTANCE.recipeNameMap.get(recipe);
	}

	public static int getQuantaFromType(MaterialStack[] stacks, NTMMaterial mat) {
		for(MaterialStack stack : stacks) {
			if(mat == null || stack.material == mat) {
				return stack.amount;
			}
		}
		return 0;
	}

	public static int getQuantaFromType(List<MaterialStack> stacks, NTMMaterial mat) {
		int sum = 0;
		for(MaterialStack stack : stacks) {
			if(stack.material == mat) {
				return stack.amount;
			}
			if(mat == null) {
				sum += stack.amount;
			}
		}
		return sum;
	}

	private static final int[] slot_access = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

	@Override
	public int[] getAccessibleSlotsFromSide(Direction side) {
		return slot_access;
	}

	/// pouring into the crucible from above: recipe materials up to the recipe's ratio, without a recipe into the waste ///

	public boolean canAcceptPartialPour(MaterialStack stack) {
		CrucibleRecipe recipe = getLoadedRecipe();

		if(recipe == null) {
			return getQuantaFromType(this.wasteStack, null) < wasteZCapacity;
		}

		int recipeContent = recipe.getInputAmount();
		int recipeInputRequired = getQuantaFromType(recipe.input, stack.material);
		int matMaximum = recipeInputRequired * recipeZCapacity / recipeContent;
		int amountStored = getQuantaFromType(recipeStack, stack.material);

		return amountStored < matMaximum && getQuantaFromType(this.recipeStack, null) < recipeZCapacity;
	}

	public MaterialStack pour(MaterialStack stack) {
		CrucibleRecipe recipe = getLoadedRecipe();

		if(recipe == null) {
			int amount = getQuantaFromType(this.wasteStack, null);

			if(amount + stack.amount <= wasteZCapacity) {
				this.addToStack(this.wasteStack, stack.copy());
				return null;
			} else {
				int toAdd = wasteZCapacity - amount;
				this.addToStack(this.wasteStack, new MaterialStack(stack.material, toAdd));
				return new MaterialStack(stack.material, stack.amount - toAdd);
			}
		}

		// the original compared the recipe's own amount here, the stored amount is what limits the ratio
		int recipeContent = recipe.getInputAmount();
		int recipeInputRequired = getQuantaFromType(recipe.input, stack.material);
		int matMaximum = recipeInputRequired * recipeZCapacity / recipeContent;
		int amountStored = getQuantaFromType(recipeStack, stack.material);

		if(amountStored + stack.amount <= matMaximum && getQuantaFromType(this.recipeStack, null) + stack.amount <= recipeZCapacity) {
			this.addToStack(this.recipeStack, stack.copy());
			return null;
		}

		int toAdd = Math.max(0, Math.min(matMaximum - amountStored, recipeZCapacity - getQuantaFromType(this.recipeStack, null)));
		if(toAdd > 0) this.addToStack(this.recipeStack, new MaterialStack(stack.material, toAdd));
		return new MaterialStack(stack.material, stack.amount - toAdd);
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 2, worldPosition.getZ() + 2);
	}

	@Override
	public boolean hasPermission(Player player) {
		return this.stillValid(player);
	}

	@Override
	public void receiveControl(CompoundTag data) {
		if(data.contains("index") && data.contains("selection")) {
			int index = data.getInt("index");
			String selection = data.getString("selection");
			if(index == 0) {
				this.recipe = selection;
				this.setChanged();
			}
		}
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerCrucible(id, inv, this);
	}
}
