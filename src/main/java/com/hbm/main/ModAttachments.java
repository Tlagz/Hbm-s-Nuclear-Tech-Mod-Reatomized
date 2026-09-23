package com.hbm.main;

import java.util.function.Supplier;

import com.hbm.extprop.HbmLivingProps;
import com.hbm.lib.RefStrings;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Data attachments, the replacement for 1.7.10's IExtendedEntityProperties */
public class ModAttachments {

	public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, RefStrings.MODID);

	/** Saved under the attachment id, the compound keys inside are the same as the original's "HbmLivingProps" tag */
	public static final Supplier<AttachmentType<HbmLivingProps>> LIVING_PROPS = ATTACHMENTS.register("living_props",
			() -> AttachmentType.serializable(HbmLivingProps::new).build());
}
