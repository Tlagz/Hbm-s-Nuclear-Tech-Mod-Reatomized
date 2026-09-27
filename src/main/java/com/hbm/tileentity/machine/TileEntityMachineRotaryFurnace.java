package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.container.ContainerMachineRotaryFurnace;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.recipes.RotaryFurnaceRecipes;
import com.hbm.inventory.recipes.RotaryFurnaceRecipes.RotaryFurnaceRecipe;
import com.hbm.module.ModuleBurnTime;
import com.hbm.tileentity.IConditionalInvAccess;
import com.hbm.tileentity.ModTileEntities;
import com.hbm.tileentity.TileEntityMachinePolluting;
import com.hbm.util.CrucibleUtil;
import com.hbm.util.DirPos;

import api.hbm.fluidmk2.IFluidStandardTransceiverMK2;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Rotary furnace: burns fuel and steam to melt up to three ingredients (and a fluid) into molten metal, poured out
 * of the side. Better fuels (solid, rocket, balefire) speed it up at the cost of more steam. Slots: 0-2 inputs,
 * 3 fluid identifier, 4 fuel. The back row takes one input each, the front fuel port the fuel.
 *
 * TODO the pouring particle, config (IConfigurableMachine), copy tool
 */
public class TileEntityMachineRotaryFurnace extends TileEntityMachinePolluting implements IFluidStandardTransceiverMK2, IConditionalInvAccess, MenuProvider {

	public FluidTank[] tanks;
	public boolean isProgressing;
	public float progress;
	public int burnTime;
	public double burnHeat = 1D;
	public int maxBurnTime;
	public int steamUsed = 0;
	public boolean isVenting;
	public MaterialStack output;
	public static final int maxOutput = MaterialShapes.BLOCK.q(16);

	public int anim;
	public int lastAnim;

	/** Given this has no heat, the heat mod instead affects the progress per fuel **/
	public static ModuleBurnTime burnModule = new ModuleBurnTime()
		.setCokeTimeMod(1.25)
		.setRocketTimeMod(1.5)
		.setSolidTimeMod(1.5)
		.setBalefireTimeMod(1.5)
		.setSolidHeatMod(1.5)
		.setRocketHeatMod(3)
		.setBalefireHeatMod(10);

	public TileEntityMachineRotaryFurnace(BlockPos pos, BlockState state) {
		super(ModTileEntities.ROTARY_FURNACE.get(), pos, state, 5, 50);
		tanks = new FluidTank[3];
		tanks[0] = new FluidTank(Fluids.NONE, 16_000);
		tanks[1] = new FluidTank(Fluids.STEAM, 12_000);
		tanks[2] = new FluidTank(Fluids.SPENTSTEAM, 120);
	}

	@Override
	public String getName() {
		return "container.machineRotaryFurnace";
	}

	@Override
	public void updateEntity() {

		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getCounterClockWise();

		if(isServer()) {

			tanks[0].setType(3, slots);

			for(DirPos pos : getSteamPos()) {
				this.trySubscribe(tanks[1].getTankType(), level, pos, pos.getDir());
				if(tanks[2].getFill() > 0) this.tryProvide(tanks[2], level, pos);
			}

			if(tanks[0].getTankType() != Fluids.NONE) for(DirPos pos : getFluidPos()) {
				this.trySubscribe(tanks[0].getTankType(), level, pos, pos.getDir());
			}

			if(smoke.getFill() > 0) this.tryProvide(smoke, level, worldPosition.relative(rot).above(5), Direction.UP);

			if(this.output != null) {
				MaterialStack leftover = CrucibleUtil.pourSingleStack(level, worldPosition.getX() + 0.5D + rot.getStepX() * 2.875D, worldPosition.getY() + 1.25D, worldPosition.getZ() + 0.5D + rot.getStepZ() * 2.875D,
						6, true, this.output, MaterialShapes.INGOT.q(1), new CrucibleUtil.Impact());
				this.output = leftover;
				if(this.output != null && this.output.amount <= 0) this.output = null;
			}

			RotaryFurnaceRecipe recipe = RotaryFurnaceRecipes.getRecipe(slots.get(0), slots.get(1), slots.get(2));
			this.isProgressing = false;

			if(recipe != null) {

				ItemStack fuel = slots.get(4);
				if(this.burnTime <= 0 && !fuel.isEmpty() && fuel.getBurnTime(RecipeType.SMELTING) > 0) {
					this.burnHeat = burnModule.getMod(fuel, burnModule.getModHeat());
					this.maxBurnTime = this.burnTime = burnModule.getBurnTime(fuel) / 2;
					this.removeItem(4, 1);
					this.setChanged();
				}

				float processSpeed = Math.max((float) burnHeat, 1);
				float steamUseMult = (float) (10 * Math.log10(processSpeed) + 1);

				if(this.canProcess(recipe, steamUseMult)) {
					this.progress += processSpeed / recipe.duration;
					tanks[1].setFill((int) (tanks[1].getFill() - recipe.steam * steamUseMult));
					steamUsed += (int) (recipe.steam * steamUseMult);
					this.isProgressing = true;

					if(this.progress >= 1F) {
						this.progress -= 1F;
						this.consumeItems(recipe);

						if(this.output == null) {
							this.output = recipe.output.copy();
						} else {
							this.output.amount += recipe.output.amount;
						}
						this.setChanged();
					}

					if(this.burnTime > 0) {
						this.pollute(PollutionType.SOOT, PollutionHandler.SOOT_PER_SECOND / 10F);
						this.burnTime--;
					}

				} else {
					this.progress = 0;
				}

				// every 100mB of steam used comes back as 1mB of spent steam
				if(this.steamUsed >= 100) {
					int steamReturn = this.steamUsed / 100;
					int canReturn = tanks[2].getMaxFill() - tanks[2].getFill();
					int doesReturn = Math.min(steamReturn, canReturn);
					this.steamUsed -= doesReturn * 100;
					tanks[2].setFill(tanks[2].getFill() + doesReturn);
				}

			} else {
				this.progress = 0;
			}

			this.networkPackNT(50);
			this.isVenting = false;

		} else {

			if(this.burnTime > 0 && com.hbm.main.ClientHooks.distanceToPlayer(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()) < 25) {
				level.addParticle(ParticleTypes.FLAME, worldPosition.getX() + 0.5 + dir.getStepX() * 0.5 + rot.getStepX() + level.random.nextGaussian() * 0.25, worldPosition.getY() + 0.375, worldPosition.getZ() + 0.5 + dir.getStepZ() * 0.5 + rot.getStepZ() + level.random.nextGaussian() * 0.25, 0, 0, 0);
			}

			if(isVenting && level.getGameTime() % 2 == 0) {
				CompoundTag fx = new CompoundTag();
				fx.putString("type", "tower");
				fx.putFloat("lift", 10F);
				fx.putFloat("base", 0.25F);
				fx.putFloat("max", 2.5F);
				fx.putInt("life", 100 + level.random.nextInt(20));
				fx.putInt("color", 0x202020);
				fx.putDouble("posX", worldPosition.getX() + 0.5 + rot.getStepX());
				fx.putDouble("posY", worldPosition.getY() + 5);
				fx.putDouble("posZ", worldPosition.getZ() + 0.5 + rot.getStepZ());
				com.hbm.particle.ParticleEffectsNT.effectNT(fx);
			}

			this.lastAnim = this.anim;
			if(this.isProgressing) {
				this.anim += (int) Math.max(burnModule.getMod(slots.get(4), burnModule.getModHeat()), 1);
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		tanks[0].serialize(buf);
		tanks[1].serialize(buf);
		tanks[2].serialize(buf);
		buf.writeBoolean(isVenting);
		buf.writeBoolean(isProgressing);
		buf.writeFloat(progress);
		buf.writeInt(burnTime);
		buf.writeInt(maxBurnTime);
		if(this.output != null) {
			buf.writeBoolean(true);
			buf.writeInt(this.output.material.id);
			buf.writeInt(this.output.amount);
		} else {
			buf.writeBoolean(false);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		tanks[0].deserialize(buf);
		tanks[1].deserialize(buf);
		tanks[2].deserialize(buf);
		isVenting = buf.readBoolean();
		isProgressing = buf.readBoolean();
		progress = buf.readFloat();
		burnTime = buf.readInt();
		maxBurnTime = buf.readInt();
		this.output = buf.readBoolean() ? new MaterialStack(Mats.matById.get(buf.readInt()), buf.readInt()) : null;
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		this.tanks[0].readFromNBT(nbt, "t0");
		this.tanks[1].readFromNBT(nbt, "t1");
		this.tanks[2].readFromNBT(nbt, "t2");
		this.progress = nbt.getFloat("prog");
		this.burnTime = nbt.getInt("burn");
		this.burnHeat = nbt.getDouble("heat");
		this.maxBurnTime = nbt.getInt("maxBurn");
		this.output = nbt.contains("outType") ? new MaterialStack(Mats.matById.get(nbt.getInt("outType")), nbt.getInt("outAmount")) : null;
	}

	@Override
	protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		this.tanks[0].writeToNBT(nbt, "t0");
		this.tanks[1].writeToNBT(nbt, "t1");
		this.tanks[2].writeToNBT(nbt, "t2");
		nbt.putFloat("prog", progress);
		nbt.putInt("burn", burnTime);
		nbt.putDouble("heat", burnHeat);
		nbt.putInt("maxBurn", maxBurnTime);
		if(this.output != null) {
			nbt.putInt("outType", this.output.material.id);
			nbt.putInt("outAmount", this.output.amount);
		}
	}

	/** Steam in and spent steam out at the back */
	public DirPos[] getSteamPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getCounterClockWise();
		return new DirPos[] {
				new DirPos(worldPosition.relative(dir, -2).relative(rot, -2), dir.getOpposite()),
				new DirPos(worldPosition.relative(dir, -2).relative(rot, -1), dir.getOpposite())
		};
	}

	/** The recipe fluid on the spout's side */
	public DirPos[] getFluidPos() {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getCounterClockWise();
		return new DirPos[] {
				new DirPos(worldPosition.relative(dir).relative(rot, 3), rot),
				new DirPos(worldPosition.relative(dir, -1).relative(rot, 3), rot)
		};
	}

	public boolean canProcess(RotaryFurnaceRecipe recipe, float steamUseMult) {

		if(this.burnTime <= 0) return false;

		if(recipe.fluid != null) {
			if(this.tanks[0].getTankType() != recipe.fluid.type) return false;
			if(this.tanks[0].getFill() < recipe.fluid.fill) return false;
		}

		if(tanks[1].getFill() < recipe.steam * steamUseMult) return false;
		if(tanks[2].getMaxFill() - tanks[2].getFill() < recipe.steam * steamUseMult / 100) return false;
		if(this.steamUsed > 100) return false;

		if(this.output != null) {
			if(this.output.material != recipe.output.material) return false;
			if(this.output.amount + recipe.output.amount > maxOutput) return false;
		}

		return true;
	}

	public void consumeItems(RotaryFurnaceRecipe recipe) {

		for(AStack aStack : recipe.ingredients) {
			for(int i = 0; i < 3; i++) {
				ItemStack stack = slots.get(i);
				if(aStack.matchesRecipe(stack, true) && stack.getCount() >= aStack.stacksize) {
					this.removeItem(i, aStack.stacksize);
					break;
				}
			}
		}

		if(recipe.fluid != null) {
			this.tanks[0].setFill(tanks[0].getFill() - recipe.fluid.fill);
		}
	}

	@Override
	public void pollute(PollutionType type, float amount) {
		FluidTank tank = type == PollutionType.SOOT ? smoke : type == PollutionType.HEAVYMETAL ? smoke_leaded : smoke_poison;

		int fluidAmount = (int) Math.ceil(amount * 100);
		tank.setFill(tank.getFill() + fluidAmount);

		if(tank.getFill() > tank.getMaxFill()) {
			int overflow = tank.getFill() - tank.getMaxFill();
			tank.setFill(tank.getMaxFill());
			PollutionHandler.incrementPollution(level, worldPosition, type, overflow / 100F);
			this.isVenting = true;
		}
	}

	@Override public int[] getAccessibleSlotsFromSide(Direction side) { return new int[0]; }
	@Override public boolean isItemValidForSlot(int slot, ItemStack stack) { return slot < 3 || slot == 4; }
	@Override public boolean canExtractItem(int slot, ItemStack stack, Direction side) { return false; }

	@Override public boolean isItemValidForSlot(BlockPos pos, int slot, ItemStack stack) { return slot < 3 || slot == 4; }
	@Override public boolean canExtractItem(BlockPos pos, int slot, ItemStack stack, Direction side) { return false; }

	/** The three back ports take one input each (red, yellow, green), the front fuel port the fuel */
	@Override
	public int[] getAccessibleSlotsFromSide(BlockPos pos, Direction side) {
		Direction dir = BlockDummyable.getRotation(getBlockState());
		Direction rot = dir.getClockWise();
		BlockPos core = worldPosition;

		if(side == dir.getOpposite() && pos.equals(core.relative(dir, -1).relative(rot, -2))) return new int[] {0};
		if(side == dir.getOpposite() && pos.equals(core.relative(dir, -1).relative(rot, -1))) return new int[] {1};
		if(side == dir.getOpposite() && pos.equals(core.relative(dir, -1))) return new int[] {2};
		if(side == dir && pos.equals(core.relative(dir).relative(rot, -1))) return new int[] {4};

		return new int[] { };
	}

	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition.getX() - 2, worldPosition.getY(), worldPosition.getZ() - 2, worldPosition.getX() + 3, worldPosition.getY() + 5, worldPosition.getZ() + 3);
	}

	@Override public FluidTank[] getAllTanks() { return new FluidTank[] {tanks[0], tanks[1], tanks[2], smoke}; }
	@Override public FluidTank[] getSendingTanks() { return new FluidTank[] {tanks[2], smoke}; }
	@Override public FluidTank[] getReceivingTanks() { return new FluidTank[] {tanks[0], tanks[1]}; }

	@Override
	public Component getDisplayName() {
		return Component.translatable(getName());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		return new ContainerMachineRotaryFurnace(id, inv, this);
	}
}
