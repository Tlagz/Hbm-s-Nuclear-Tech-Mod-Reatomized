package com.hbm.module;

import java.util.List;

import com.hbm.util.BufferUtil;
import com.hbm.util.ItemStackUtil;

import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Filter slot modes: exact (item and damage), wildcard (item) or an ore dictionary key.
 * The original's bedrock grade mode waits for the bedrock ores; metadata variants are separate items here, so
 * exact and wildcard only differ for damaged tools.
 */
public class ModulePatternMatcher {

	public static final String MODE_EXACT = "exact";
	public static final String MODE_WILDCARD = "wildcard";
	public static final String MODE_BEDROCK = "bedrock";
	public String[] modes;

	public ModulePatternMatcher() {
		this.modes = new String[1];
	}

	public ModulePatternMatcher(int count) {
		this.modes = new String[count];
	}

	public void initPatternSmart(Level world, ItemStack stack, int i) {

		if(world.isClientSide) return;

		if(stack.isEmpty()) {
			modes[i] = null;
			return;
		}

		List<String> names = ItemStackUtil.getOreDictNames(stack);

		if(iterateAndCheck(names, i, "ingot")) return;
		if(iterateAndCheck(names, i, "block")) return;
		if(iterateAndCheck(names, i, "dust")) return;
		if(iterateAndCheck(names, i, "nugget")) return;
		if(iterateAndCheck(names, i, "plate")) return;

		modes[i] = defaultMode(stack);
	}

	private boolean iterateAndCheck(List<String> names, int i, String prefix) {

		for(String s : names) {
			if(s.startsWith(prefix)) {
				modes[i] = s;
				return true;
			}
		}

		return false;
	}

	/** The original used exact for items with subtypes, wildcard for the rest (like damageable tools) */
	private static String defaultMode(ItemStack stack) {
		return stack.isDamageableItem() ? MODE_WILDCARD : MODE_EXACT;
	}

	public void initPatternStandard(Level world, ItemStack stack, int i) {

		if(world.isClientSide) return;

		if(stack.isEmpty()) {
			modes[i] = null;
			return;
		}

		modes[i] = defaultMode(stack);
	}

	public void nextMode(Level world, ItemStack pattern, int i) {

		if(world.isClientSide) return;

		if(pattern.isEmpty()) {
			modes[i] = null;
			return;
		}

		if(modes[i] == null) {
			modes[i] = MODE_EXACT;
		} else if(MODE_EXACT.equals(modes[i])) {
			modes[i] = MODE_WILDCARD;
		} else if(MODE_BEDROCK.equals(modes[i])) {
			modes[i] = MODE_WILDCARD;
		} else if(MODE_WILDCARD.equals(modes[i])) {

			List<String> names = ItemStackUtil.getOreDictNames(pattern);

			if(names.isEmpty()) {
				modes[i] = MODE_EXACT;
			} else {
				modes[i] = names.get(0);
			}
		} else {

			List<String> names = ItemStackUtil.getOreDictNames(pattern);

			if(names.size() < 2 || modes[i].equals(names.get(names.size() - 1))) {
				modes[i] = MODE_EXACT;
			} else {

				for(int j = 0; j < names.size() - 1; j++) {

					if(modes[i].equals(names.get(j))) {
						modes[i] = names.get(j + 1);
						return;
					}
				}
			}
		}
	}

	public boolean isValidForFilter(ItemStack filter, int index, ItemStack input) {

		String mode = modes[index];

		if(mode == null) {
			modes[index] = mode = MODE_EXACT;
		}

		switch(mode) {
		case MODE_EXACT: return ItemStack.isSameItem(input, filter) && (!input.isDamageableItem() || input.getDamageValue() == filter.getDamageValue());
		case MODE_WILDCARD: return input.getItem() == filter.getItem();
		case MODE_BEDROCK: return false; // TODO bedrock ore grades
		default:
			List<String> keys = ItemStackUtil.getOreDictNames(input);
			return keys.contains(mode);
		}
	}

	public void readFromNBT(CompoundTag nbt) {

		for(int i = 0; i < modes.length; i++) {
			if(nbt.contains("mode" + i)) {
				modes[i] = nbt.getString("mode" + i);
			} else {
				modes[i] = null;
			}
		}
	}

	public void writeToNBT(CompoundTag nbt) {

		for(int i = 0; i < modes.length; i++) {
			if(modes[i] != null) {
				nbt.putString("mode" + i, modes[i]);
			}
		}
	}

	public void serialize(ByteBuf buf) {
		for(int i = 0; i < modes.length; i++) {
			BufferUtil.writeString(buf, modes[i]);
		}
	}

	public void deserialize(ByteBuf buf) {
		for(int i = 0; i < modes.length; i++) {
			modes[i] = BufferUtil.readString(buf);
		}
	}

	public static String getLabel(String mode) {
		switch(mode) {
		case MODE_EXACT: return ChatFormatting.YELLOW + "Item and meta match";
		case MODE_WILDCARD: return ChatFormatting.YELLOW + "Item matches";
		case MODE_BEDROCK: return ChatFormatting.YELLOW + "Item and bedrock grade match";
		default: return ChatFormatting.YELLOW + "Ore dict key matches: " + mode;
		}
	}
}
