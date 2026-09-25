package com.hbm.blocks.machine;

import com.hbm.blocks.BlockEnums.EnumCMMaterials;

import net.minecraft.world.level.block.TransparentBlock;

/** Custom machine tank walls, see-through, faces between two of them are culled like glass */
public class BlockCMGlass extends TransparentBlock {

	private final String descriptionId;

	public BlockCMGlass(Properties properties, String descriptionId, EnumCMMaterials type) {
		super(properties.noOcclusion());
		this.descriptionId = descriptionId;
	}

	@Override
	public String getDescriptionId() {
		return descriptionId;
	}
}
