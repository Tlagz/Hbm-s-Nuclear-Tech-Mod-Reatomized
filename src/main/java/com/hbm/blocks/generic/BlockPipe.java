package com.hbm.blocks.generic;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.RotatedPillarBlock;

/**
 * Decorative pipes along the axis of the clicked face (the original's metadata 0/4/8), drawn with the pipe OBJ models:
 * plain, with rims, four thin pipes or framed with a mesh.
 */
public class BlockPipe extends RotatedPillarBlock {

	public enum Style {
		PLAIN("pipe"), RIM("pipe_rim"), QUAD("pipe_quad"), FRAMED("pipe_rim");

		/** OBJ model of the pipe itself, framed pipes add pipe_frame.obj */
		public final String model;

		Style(String model) {
			this.model = model;
		}
	}

	public final Style style;
	/** Textures of the pipe's ends and its side */
	public final String top, side;

	public BlockPipe(Properties properties, Style style, String top, String side) {
		super(properties.noOcclusion());
		this.style = style;
		this.top = top;
		this.side = side;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {
		list.add(Component.literal("Purely decorative"));
	}
}
