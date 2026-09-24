package com.hbm.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.Fluids.CD_Canister;
import com.hbm.inventory.fluid.Fluids.CD_Gastank;
import com.hbm.items.ModDataComponents;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidContainerBase;
import com.hbm.lib.RefStrings;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

/**
 * Every item that holds a fixed amount of fluid, with its empty counterpart. Machines use it through
 * FluidTank.loadTank/unloadTank. Stacks are compared like the original compared item and damage: same item,
 * same fluid component (and potion contents, so only water bottles count as water).
 *
 * TODO ore dictionary entries (containerXXXfluid) as item tags, compat containers
 */
public class FluidContainerRegistry {

	public static List<FluidContainer> allContainers = new ArrayList<FluidContainer>();
	private static HashMap<FluidType, List<FluidContainer>> containerMap = new HashMap<FluidType, List<FluidContainer>>();

	public static void clearRegistry() {
		allContainers.clear();
		containerMap.clear();
	}

	/** Items that aren't ported yet are looked up by name and skipped until they exist */
	private static ItemStack stack(String name) {
		Item item = BuiltInRegistries.ITEM.getOptional(RefStrings.loc(name)).orElse(null);
		return item == null ? null : new ItemStack(item);
	}

	/** Registers a container if its full item exists, and its empty item (unless the container is consumed) */
	private static void register(String full, String empty, FluidType type, int amount) {
		ItemStack fullStack = stack(full);
		ItemStack emptyStack = empty == null ? null : empty.startsWith("minecraft:") ? new ItemStack(BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(empty))) : stack(empty);
		if(fullStack == null || (empty != null && emptyStack == null)) return;
		registerContainer(new FluidContainer(fullStack, emptyStack, type, amount));
	}

	public static void register() {

		clearRegistry();

		registerContainer(new FluidContainer(new ItemStack(Items.WATER_BUCKET), new ItemStack(Items.BUCKET), Fluids.WATER, 1000));
		registerContainer(new FluidContainer(PotionContents.createItemStack(Items.POTION, Potions.WATER), new ItemStack(Items.GLASS_BOTTLE), Fluids.WATER, 250));
		registerContainer(new FluidContainer(new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.BUCKET), Fluids.LAVA, 1000));
		register("bucket_mud", "minecraft:bucket", Fluids.WATZ, 1000);
		register("bucket_schrabidic_acid", "minecraft:bucket", Fluids.SCHRABIDIC, 1000);
		register("bucket_sulfuric_acid", "minecraft:bucket", Fluids.SULFURIC_ACID, 1000);

		register("red_barrel", "tank_steel", Fluids.DIESEL, 10000);
		register("pink_barrel", "tank_steel", Fluids.KEROSENE, 10000);
		register("lox_barrel", "tank_steel", Fluids.OXYGEN, 10000);

		register("ore_oil", null, Fluids.OIL, 250);
		register("ore_gneiss_gas", null, Fluids.PETROLEUM, 250); // 50 with enable528, the 528 mode is not ported

		register("cell_deuterium", "cell_empty", Fluids.DEUTERIUM, 1000);
		register("cell_tritium", "cell_empty", Fluids.TRITIUM, 1000);
		register("cell_uf6", "cell_empty", Fluids.UF6, 1000);
		register("cell_puf6", "cell_empty", Fluids.PUF6, 1000);
		register("cell_antimatter", "cell_empty", Fluids.AMAT, 1000);
		register("cell_anti_schrabidium", "cell_empty", Fluids.ASCHRAB, 1000);
		register("cell_sas3", "cell_empty", Fluids.SAS3, 1000);
		register("bottle_mercury", "minecraft:glass_bottle", Fluids.MERCURY, 1000);
		register("ingot_mercury", null, Fluids.MERCURY, 125);

		register("rod_zirnox_tritium", "rod_zirnox_empty", Fluids.TRITIUM, 2000);

		register("particle_hydrogen", "particle_empty", Fluids.HYDROGEN, 1000);
		register("particle_amat", "particle_empty", Fluids.AMAT, 1000);
		register("particle_aschrab", "particle_empty", Fluids.ASCHRAB, 1000);

		register("iv_blood", "iv_empty", Fluids.BLOOD, 100);
		register("iv_xp", "iv_xp_empty", Fluids.XPJUICE, 100);
		registerContainer(new FluidContainer(new ItemStack(Items.EXPERIENCE_BOTTLE), new ItemStack(Items.GLASS_BOTTLE), Fluids.XPJUICE, 100));

		register("can_mug", "can_empty", Fluids.MUG, 100);

		FluidType[] fluids = Fluids.getAll();
		for(int i = 1; i < fluids.length; i++) {

			FluidType type = fluids[i];

			if(type.getContainer(CD_Canister.class) != null) registerContainer(new FluidContainer(ItemFluidContainerBase.withFluid(ModItems.canister_full, type), new ItemStack(ModItems.canister_empty.get()), type, 1000));
			if(type.getContainer(CD_Gastank.class) != null) registerContainer(new FluidContainer(ItemFluidContainerBase.withFluid(ModItems.gas_full, type), new ItemStack(ModItems.gas_empty.get()), type, 1000));

			if(type.hasNoContainer()) continue;

			// TODO disperser canisters and glyphid glands (ItemDisperser, weapons phase)

			registerContainer(new FluidContainer(ItemFluidContainerBase.withFluid(ModItems.fluid_tank_lead_full, type), new ItemStack(ModItems.fluid_tank_lead_empty.get()), type, 1000));

			if(type.needsLeadContainer()) continue;

			registerContainer(new FluidContainer(ItemFluidContainerBase.withFluid(ModItems.fluid_tank_full, type), new ItemStack(ModItems.fluid_tank_empty.get()), type, 1000));
			registerContainer(new FluidContainer(ItemFluidContainerBase.withFluid(ModItems.fluid_barrel_full, type), new ItemStack(ModItems.fluid_barrel_empty.get()), type, 16000));
		}
	}

	public static void registerContainer(FluidContainer con) {
		allContainers.add(con);
		containerMap.computeIfAbsent(con.type, k -> new ArrayList<FluidContainer>()).add(con);
	}

	/** The original's isItemEqual (item + damage), ignoring the count and other data like custom names */
	public static boolean isSame(ItemStack a, ItemStack b) {
		if(a == null || b == null || a.isEmpty() || b.isEmpty()) return false;
		return a.getItem() == b.getItem()
				&& Objects.equals(a.get(ModDataComponents.FLUID_TYPE.get()), b.get(ModDataComponents.FLUID_TYPE.get()))
				&& Objects.equals(a.get(DataComponents.POTION_CONTENTS), b.get(DataComponents.POTION_CONTENTS));
	}

	public static List<FluidContainer> getContainers(FluidType type) {
		return containerMap.get(type);
	}

	public static FluidContainer getContainer(FluidType type, ItemStack stack) {
		if(stack == null || stack.isEmpty() || !containerMap.containsKey(type)) return null;

		for(FluidContainer container : getContainers(type)) {
			if(container.emptyContainer != null && isSame(container.emptyContainer, stack))
				return container;
		}

		return null;
	}

	public static int getFluidContent(ItemStack stack, FluidType type) {
		if(stack == null || stack.isEmpty() || !containerMap.containsKey(type)) return 0;

		for(FluidContainer container : containerMap.get(type)) {
			if(isSame(container.fullContainer, stack))
				return container.content;
		}

		return 0;
	}

	public static FluidType getFluidType(ItemStack stack) {
		if(stack == null || stack.isEmpty()) return Fluids.NONE;

		for(FluidContainer container : allContainers) {
			if(isSame(container.fullContainer, stack))
				return container.type;
		}

		return Fluids.NONE;
	}

	/** @return the full container for this empty one, null if there is none */
	public static ItemStack getFullContainer(ItemStack stack, FluidType type) {
		if(stack == null || stack.isEmpty() || !containerMap.containsKey(type)) return null;

		for(FluidContainer container : containerMap.get(type)) {
			if(container.emptyContainer != null && isSame(container.emptyContainer, stack))
				return container.fullContainer.copy();
		}

		return null;
	}

	/** @return the empty container for this full one, null if there is none (or it's consumed) */
	public static ItemStack getEmptyContainer(ItemStack stack) {
		if(stack == null || stack.isEmpty()) return null;

		for(FluidContainer container : allContainers) {
			if(isSame(container.fullContainer, stack))
				return container.emptyContainer == null ? null : container.emptyContainer.copy();
		}

		return null;
	}
}
