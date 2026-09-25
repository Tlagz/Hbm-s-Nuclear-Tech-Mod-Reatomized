package com.hbm.items.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.hbm.inventory.OreDictManager;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.ItemEnums.EnumCasingType;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Foundry molds, one item per mold (the original's item damage was the mold's id): mold_ingot, mold_plates...
 * Small molds go into the foundry mold, large ones into the basin. What a mold casts from a material is looked up
 * through the material's ore dictionary tags, special molds (blades, stamps, casings) have fixed outputs.
 */
public class ItemMold extends Item {

	/** molds in "pretty" order, variable between versions */
	public static final List<Mold> molds = new ArrayList<>();
	/** molds by their static ID, the original's stack item damage */
	public static final HashMap<Integer, Mold> moldById = new HashMap<>();
	/** the registered item of each mold id */
	public static final Map<Integer, DeferredItem<ItemMold>> ITEMS = new LinkedHashMap<>();

	public static final HashMap<NTMMaterial, Supplier<ItemStack>> blockOverrides = new HashMap<>();

	public final Mold mold;

	public ItemMold(Properties properties, Mold mold) {
		super(properties);
		this.mold = mold;
	}

	@Override
	public String getDescriptionId() {
		return "item.hbm.mold";
	}

	static {
		blockOverrides.put(Mats.MAT_STONE,		() -> new ItemStack(Blocks.STONE));
		blockOverrides.put(Mats.MAT_OBSIDIAN,	() -> new ItemStack(Blocks.OBSIDIAN));

		int S = 0;
		int L = 1;
		registerMold(new MoldShape(		0, S, "nugget", MaterialShapes.NUGGET));
		registerMold(new MoldShape(		1, S, "billet", MaterialShapes.BILLET));
		registerMold(new MoldShape(		2, S, "ingot", MaterialShapes.INGOT));
		registerMold(new MoldShape(		3, S, "plate", MaterialShapes.PLATE));
		registerMold(new MoldShape(		4, S, "wire", MaterialShapes.WIRE, 8));

		registerMold(new MoldShape(		19, S, "plate_cast", MaterialShapes.CASTPLATE));
		registerMold(new MoldShape(		20, S, "wire_dense", MaterialShapes.DENSEWIRE));

		registerMold(new MoldMulti(		5, S, "blade", MaterialShapes.INGOT.q(3),
				Mats.MAT_TITANIUM, stack(() -> ModItems.blade_titanium.get()),
				Mats.MAT_TUNGSTEN, stack(() -> ModItems.blade_tungsten.get())));

		registerMold(new MoldMulti(		6, S, "blades", MaterialShapes.INGOT.q(4),
				Mats.MAT_STEEL,			stack(() -> ModItems.blades_steel.get()),
				Mats.MAT_TITANIUM,		stack(() -> ModItems.blades_titanium.get())));

		registerMold(new MoldMulti(		7, S, "stamp", MaterialShapes.INGOT.q(4),
				Mats.MAT_STONE,			stack(() -> ModItems.stamp_stone_flat.get()),
				Mats.MAT_IRON,			stack(() -> ModItems.stamp_iron_flat.get()),
				Mats.MAT_STEEL,			stack(() -> ModItems.stamp_steel_flat.get()),
				Mats.MAT_TITANIUM,		stack(() -> ModItems.stamp_titanium_flat.get()),
				Mats.MAT_OBSIDIAN,		stack(() -> ModItems.stamp_obsidian_flat.get())));

		registerMold(new MoldShape(		8, S, "shell", MaterialShapes.SHELL));
		registerMold(new MoldShape(		9, S, "pipe", MaterialShapes.PIPE));

		registerMold(new MoldShape(		10, L, "ingots", MaterialShapes.INGOT, 9));
		registerMold(new MoldShape(		11, L, "plates", MaterialShapes.PLATE, 9));
		registerMold(new MoldShape(		13, L, "plates_cast", MaterialShapes.CASTPLATE, 3));
		registerMold(new MoldShape(		21, L, "wires_dense", MaterialShapes.DENSEWIRE, 9));
		registerMold(new MoldBlock(		12, L, "block", MaterialShapes.BLOCK));

		registerMold(new MoldMulti(		16, S, "c9", MaterialShapes.PLATE.q(1, 4),
				Mats.MAT_GUNMETAL,		sup(() -> ModItems.casing.stack(EnumCasingType.SMALL)),
				Mats.MAT_WEAPONSTEEL,	sup(() -> ModItems.casing.stack(EnumCasingType.SMALL_STEEL))));
		registerMold(new MoldMulti(		17, S, "c50", MaterialShapes.PLATE.q(1, 2),
				Mats.MAT_GUNMETAL,		sup(() -> ModItems.casing.stack(EnumCasingType.LARGE)),
				Mats.MAT_WEAPONSTEEL,	sup(() -> ModItems.casing.stack(EnumCasingType.LARGE_STEEL))));

		registerMold(new MoldShape(		22, S, "barrel_light", MaterialShapes.LIGHTBARREL));
		registerMold(new MoldShape(		23, S, "barrel_heavy", MaterialShapes.HEAVYBARREL));
		registerMold(new MoldShape(		24, S, "receiver_light", MaterialShapes.LIGHTRECEIVER));
		registerMold(new MoldShape(		25, S, "receiver_heavy", MaterialShapes.HEAVYRECEIVER));
		registerMold(new MoldShape(		26, S, "mechanism", MaterialShapes.MECHANISM));
		registerMold(new MoldShape(		27, S, "stock", MaterialShapes.STOCK));
		registerMold(new MoldShape(		28, S, "grip", MaterialShapes.GRIP));
	}

	private static Supplier<ItemStack> sup(Supplier<ItemStack> stack) {
		return stack;
	}

	private static Supplier<ItemStack> stack(Supplier<Item> item) {
		return () -> new ItemStack(item.get());
	}

	public static void registerMold(Mold mold) {
		molds.add(mold);
		moldById.put(mold.id, mold);
	}

	/** The mold item of a mold id, for recipes (the original's new ItemStack(ModItems.mold, 1, id)) */
	public static DeferredItem<ItemMold> get(int id) {
		return ITEMS.get(id);
	}

	public static ItemStack stack(int id) {
		return new ItemStack(get(id).get());
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		list.add(Component.literal(mold.getTitle()).withStyle(ChatFormatting.YELLOW));

		if(mold.size == 0) list.add(Component.translatable("block.hbm.foundry_mold").withStyle(ChatFormatting.GOLD));
		if(mold.size == 1) list.add(Component.translatable("block.hbm.foundry_basin").withStyle(ChatFormatting.RED));
	}

	/** The mold of a stack, null if it isn't one */
	public static Mold getMold(ItemStack stack) {
		return stack.getItem() instanceof ItemMold item ? item.mold : null;
	}

	public static int nextOrder = 0;

	public static abstract class Mold {
		public int order;
		public int id;
		/** 0 for the small foundry mold, 1 for the basin */
		public int size;
		public String name;

		public Mold(int id, int size, String name) {
			this.order = nextOrder++;
			this.id = id;
			this.size = size;
			this.name = name;
		}

		public abstract ItemStack getOutput(NTMMaterial mat);
		public abstract int getCost();
		public abstract String getTitle();
	}

	public static class MoldShape extends Mold {

		public MaterialShapes shape;
		public int amount;

		public MoldShape(int id, int size, String name, MaterialShapes shape) {
			this(id, size, name, shape, 1);
		}

		public MoldShape(int id, int size, String name, MaterialShapes shape, int amount) {
			super(id, size, name);
			this.shape = shape;
			this.amount = amount;
		}

		/** The first item of [shape][material], NTM items first and fragments last like the original */
		@Override
		public ItemStack getOutput(NTMMaterial mat) {

			for(String name : mat.names) {
				String od = shape.name() + name;
				var tag = BuiltInRegistries.ITEM.getTag(OreDictManager.tag(od));
				if(tag.isEmpty() || tag.get().size() == 0) continue;

				//prioritize NTM items
				for(Holder<Item> holder : tag.get()) {
					var key = BuiltInRegistries.ITEM.getKey(holder.value());
					if(key.getNamespace().equals(RefStrings.MODID)) {
						if(key.getPath().contains("fragment")) continue; // deprioritize fragments
						return new ItemStack(holder.value(), this.amount);
					}
				}
				//...then try whatever comes first
				return new ItemStack(tag.get().get(0).value(), this.amount);
			}

			return null;
		}

		@Override
		public int getCost() {
			return shape.q(amount);
		}

		@Override
		public String getTitle() {
			return I18nUtil.resolveKey("shape." + shape.name()) + " x" + amount;
		}
	}

	public static class MoldBlock extends MoldShape {

		public MoldBlock(int id, int size, String name, MaterialShapes shape) {
			super(id, size, name, shape);
		}

		@Override
		public ItemStack getOutput(NTMMaterial mat) {

			Supplier<ItemStack> override = blockOverrides.get(mat);

			if(override != null)
				return override.get();

			return super.getOutput(mat);
		}
	}

	/* not so graceful but it does the job and it does it well */
	public static class MoldMulti extends Mold {

		public HashMap<NTMMaterial, Supplier<ItemStack>> map = new HashMap<>();
		public int amount;
		private final Supplier<ItemStack> first;

		@SuppressWarnings("unchecked")
		public MoldMulti(int id, int size, String name, int amount, Object... inputs) {
			super(id, size, name);
			this.amount = amount;

			Supplier<ItemStack> first = null;
			for(int i = 0; i < inputs.length; i += 2) {
				Supplier<ItemStack> out = (Supplier<ItemStack>) inputs[i + 1];
				map.put((NTMMaterial) inputs[i], out);
				if(i == 0) first = out;
			}
			this.first = first;
		}

		@Override
		public ItemStack getOutput(NTMMaterial mat) {
			Supplier<ItemStack> out = this.map.get(mat);
			return out != null ? out.get() : null;
		}

		@Override
		public int getCost() {
			return amount;
		}

		@Override
		public String getTitle() {
			return I18nUtil.resolveKey("shape." + name) + " x" + (first != null ? first.get().getCount() : 1);
		}
	}
}
