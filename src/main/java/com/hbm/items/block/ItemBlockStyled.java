package com.hbm.items.block;

import java.util.ArrayList;
import java.util.List;

import com.hbm.items.ISubItems;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Block item for blocks whose metadata variants became a block state property (the original's IBlockMulti):
 * one creative stack per value, the value is placed through the BLOCK_STATE component.
 */
public class ItemBlockStyled extends BlockItem implements ISubItems {

	private final IntegerProperty property;

	public ItemBlockStyled(Block block, IntegerProperty property, Properties properties) {
		super(block, properties);
		this.property = property;
	}

	@Override
	public List<ItemStack> getSubItems() {
		List<ItemStack> list = new ArrayList<>();
		for(int value : property.getPossibleValues()) {
			ItemStack stack = new ItemStack(this);
			if(value != 0) stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(property, value));
			list.add(stack);
		}
		return list;
	}

	/** The value this stack places, 0 without the component */
	public static int getValue(ItemStack stack, IntegerProperty property) {
		BlockItemStateProperties props = stack.get(DataComponents.BLOCK_STATE);
		if(props == null) return 0;
		Integer value = props.get(property);
		return value == null ? 0 : value;
	}
}
