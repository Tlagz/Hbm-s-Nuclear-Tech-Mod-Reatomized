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

	/** customStep(stone, "hbm:step.metalBlock", 0.5, 1.0): metal steps, stone otherwise */
	public static final DeferredSoundType GRATE = new DeferredSoundType(0.5F, 1.0F,
			() -> SoundEvents.STONE_BREAK, ModSounds.holder("step.metalBlock"), () -> SoundEvents.STONE_PLACE,
			() -> SoundEvents.STONE_HIT, () -> SoundEvents.STONE_FALL);

	/** placeBreakStep("hbm:block.platemetalPlace", "hbm:block.platemetalPlace", "hbm:step.platemetal", 1.0, 1.0) */
	public static final DeferredSoundType PLATEMETAL = new DeferredSoundType(1.0F, 1.0F,
			ModSounds.holder("block.platemetalPlace"), ModSounds.holder("step.platemetal"), ModSounds.holder("block.platemetalPlace"),
			() -> SoundEvents.STONE_HIT, () -> SoundEvents.STONE_FALL);
}
