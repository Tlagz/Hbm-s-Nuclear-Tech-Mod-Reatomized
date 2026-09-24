package com.hbm.blocks;

import com.hbm.main.ModSounds;

import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.common.util.DeferredSoundType;

/**
 * Custom block sounds. The original's ModSoundType could randomize the pitch per play (pitchFunction),
 * vanilla sound types can't, the base pitch is used instead.
 */
public class ModSoundTypes {

	/** customDig(metal, "hbm:block.pipePlaced", 0.85, 0.85): pipe sound for breaking and placing, metal otherwise */
	public static final DeferredSoundType PIPE = new DeferredSoundType(0.85F, 0.85F,
			ModSounds.holder("block.pipePlaced"), () -> SoundEvents.METAL_STEP, ModSounds.holder("block.pipePlaced"),
			() -> SoundEvents.METAL_HIT, () -> SoundEvents.METAL_FALL);
}
