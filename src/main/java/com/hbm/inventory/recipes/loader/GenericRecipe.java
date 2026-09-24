package com.hbm.inventory.recipes.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hbm.inventory.FluidStack;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.recipes.loader.GenericRecipes.ChanceOutput;
import com.hbm.inventory.recipes.loader.GenericRecipes.ChanceOutputMulti;
import com.hbm.inventory.recipes.loader.GenericRecipes.IOutput;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidIcon;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * A recipe of the generic recipe system (assembly machine, chemical plant etc.): item and fluid in- and outputs,
 * duration, power, blueprint pools and auto switch groups.
 *
 * TODO NEI extras (JEI)
 */
public class GenericRecipe {

	protected final String name;
	public String nameWrapper;
	public AStack[] inputItem;
	public FluidStack[] inputFluid;
	public IOutput[] outputItem;
	public FluidStack[] outputFluid;
	public int duration;
	public long power;
	protected ItemStack icon;
	public boolean writeIcon = false;
	public boolean customLocalization = false;
	protected String[] blueprintPools = null;
	public String autoSwitchGroup = null;

	public GenericRecipe(String name) {
		this.name = name;
	}

	public boolean isPooled() { return blueprintPools != null; }
	public String[] getPools() { return this.blueprintPools; }

	public boolean isPartOfPool(String lookingFor) {
		if(!isPooled()) return false;
		for(String pool : blueprintPools) if(pool.equals(lookingFor)) return true;
		return false;
	}

	public GenericRecipe setDuration(int duration) { this.duration = duration; return this; }
	public GenericRecipe setPower(long power) { this.power = power; return this; }
	public GenericRecipe setup(int duration, long power) { return this.setDuration(duration).setPower(power); }
	public GenericRecipe setupNamed(int duration, long power) { return this.setDuration(duration).setPower(power).setNamed(); }
	public GenericRecipe setNameWrapper(String wrapper) { this.nameWrapper = wrapper; return this; }
	public GenericRecipe setIcon(ItemStack icon) { this.icon = icon; this.writeIcon = true; return this; }
	public GenericRecipe setIcon(ItemLike item) { return this.setIcon(new ItemStack(item)); }
	public GenericRecipe setNamed() { this.customLocalization = true; return this; }

	public GenericRecipe setPools(String... pools) {
		this.blueprintPools = pools;
		for(String pool : pools) {
			if(pool.startsWith(GenericRecipes.POOL_PREFIX_528)) throw new IllegalArgumentException("Tried initializing a recipe's default blueprint pool with a 528 blueprint - this is not allowed.");
			GenericRecipes.addToPool(pool, this);
		}
		return this;
	}
	/** Only for recipe configs - same as regular except the anti 528 check doesn't exist */
	public GenericRecipe setPoolsAllow528(String... pools) { this.blueprintPools = pools; for(String pool : pools) GenericRecipes.addToPool(pool, this); return this; }
	/** 528 mode isn't ported, these pools never apply */
	public GenericRecipe setPools528(String... pools) { return this; }
	public GenericRecipe setGroup(String autoSwitch, GenericRecipes<?> set) { this.autoSwitchGroup = autoSwitch; set.addToGroup(autoSwitch, this); return this; }

	public GenericRecipe inputItems(AStack... input) { this.inputItem = input; for(AStack stack : this.inputItem) checkStackLimit(stack); return this; }
	/** Expensive mode isn't ported, the expensive inputs never apply */
	public GenericRecipe inputItemsEx(AStack... input) { return this; }
	public GenericRecipe inputFluids(FluidStack... input) { this.inputFluid = input; return this; }
	public GenericRecipe inputFluidsEx(FluidStack... input) { return this; }
	public GenericRecipe outputItems(IOutput... output) { this.outputItem = output; return this; }
	public GenericRecipe outputFluids(FluidStack... output) { this.outputFluid = output; return this; }

	private void checkStackLimit(AStack stack) {
		int max = 64;
		if(stack instanceof ComparableStack comp) max = comp.toStack().getMaxStackSize();
		if(stack.stacksize > max) throw new IllegalArgumentException("AStack " + stack + " in " + this.name + " exceeds stack limit of " + max + "!");
	}

	public GenericRecipe outputItems(ItemStack... output) {
		this.outputItem = new IOutput[output.length];
		for(int i = 0; i < outputItem.length; i++) this.outputItem[i] = new ChanceOutput(output[i]);
		return this;
	}

	public GenericRecipe setIconToFirstIngredient() {
		if(this.inputItem != null) {
			List<ItemStack> stacks = this.inputItem[0].extractForNEI();
			if(!stacks.isEmpty()) this.icon = stacks.get(0);
		}
		return this;
	}

	public ItemStack getIcon() {

		if(icon == null) {
			if(outputItem != null) {
				if(outputItem[0] instanceof ChanceOutput single) icon = single.stack.copy();
				if(outputItem[0] instanceof ChanceOutputMulti multi) icon = multi.pool.get(0).stack.copy();
				return icon;
			}
			if(outputFluid != null) {
				icon = ItemFluidIcon.make(outputFluid[0]);
			}
		}

		if(icon == null) icon = new ItemStack(ModItems.nothing.get());
		return icon;
	}

	public String getInternalName() {
		return this.name;
	}

	public String getLocalizedName() {
		String name = null;
		if(customLocalization) name = I18nUtil.resolveKey(this.name);
		if(name == null) name = this.getIcon().getHoverName().getString();
		if(this.nameWrapper != null) name = I18nUtil.resolveKey(this.nameWrapper, name);
		return name;
	}

	/** The recipe tooltip. extended adds the internal name (the original showed it while holding shift) */
	public List<String> print(boolean extended) {
		List<String> list = new ArrayList<>();

		header(list, extended);
		autoSwitch(list);
		duration(list);
		power(list);
		input(list);
		output(list);

		return list;
	}

	protected void header(List<String> list, boolean extended) {
		list.add(ChatFormatting.YELLOW + this.getLocalizedName());
		if(extended) list.add(ChatFormatting.DARK_GRAY + "Internal: " + this.getInternalName());
	}

	protected void autoSwitch(List<String> list) {
		if(this.autoSwitchGroup != null) {
			String[] lines = I18nUtil.resolveKeyArray("autoswitch", I18nUtil.resolveKey(this.autoSwitchGroup));
			for(String line : lines) list.add(ChatFormatting.GOLD + line);
		}
	}

	protected void duration(List<String> list) {
		if(duration > 0) {
			double seconds = this.duration / 20D;
			list.add(ChatFormatting.RED + I18nUtil.resolveKey("gui.recipe.duration") + ": " + seconds + "s");
		}
	}

	protected void power(List<String> list) {
		if(power > 0) {
			list.add(ChatFormatting.RED + I18nUtil.resolveKey("gui.recipe.consumption") + ": " + BobMathUtil.getShortNumber(power) + "HE/t");
		}
	}

	protected void input(List<String> list) {
		list.add(ChatFormatting.BOLD + I18nUtil.resolveKey("gui.recipe.input") + ":");
		if(inputItem != null) for(AStack stack : inputItem) {
			ItemStack display = stack.extractForCyclingDisplay(20);
			list.add("  " + ChatFormatting.GRAY + display.getCount() + "x " + display.getHoverName().getString());
		}
		if(inputFluid != null) for(FluidStack fluid : inputFluid) list.add("  " + ChatFormatting.BLUE + fluid.fill + "mB " + fluid.type.getLocalizedName() + (fluid.pressure == 0 ? "" : " " + I18nUtil.resolveKey("gui.recipe.atPressure") + " " + ChatFormatting.RED + fluid.pressure + " PU"));
	}

	protected void output(List<String> list) {
		list.add(ChatFormatting.BOLD + I18nUtil.resolveKey("gui.recipe.output") + ":");
		if(outputItem != null) for(IOutput output : outputItem)
			for(String line : output.getLabel()) list.add("  " + line);
		if(outputFluid != null) for(FluidStack fluid : outputFluid) {
			String pressurePart = fluid.pressure == 0 ? "" :
				" " + I18nUtil.resolveKey("gui.recipe.atPressure") + " " + ChatFormatting.RED + fluid.pressure + " PU";
			list.add("  " + ChatFormatting.BLUE + fluid.fill + "mB " + fluid.type.getLocalizedName() + pressurePart);
		}
	}

	/** Default impl only matches localized name substring, can be extended to include ingredients as well */
	public boolean matchesSearch(String substring) {
		return getLocalizedName().toLowerCase(Locale.US).contains(substring.toLowerCase(Locale.US));
	}
}
