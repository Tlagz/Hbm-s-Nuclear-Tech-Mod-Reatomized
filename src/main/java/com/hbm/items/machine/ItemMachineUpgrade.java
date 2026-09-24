package com.hbm.items.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.items.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Machine upgrade. In a machine GUI the tooltip shows what the upgrade does in that machine (the machine
 * implements IUpgradeInfoProvider), otherwise the special upgrades show their generic description.
 */
public class ItemMachineUpgrade extends Item {

	public UpgradeType type;
	public int tier = 0;

	public ItemMachineUpgrade(Properties properties) {
		this(properties, UpgradeType.SPECIAL);
	}

	public ItemMachineUpgrade(Properties properties, UpgradeType type) {
		super(properties);
		this.type = type;
	}

	public ItemMachineUpgrade(Properties properties, UpgradeType type, int tier) {
		this(properties, type);
		this.tier = tier;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> list, TooltipFlag flag) {

		if(FMLEnvironment.dist == Dist.CLIENT) {
			List<String> info = new ArrayList<>();
			if(com.hbm.main.ClientHooks.provideUpgradeInfo(this.type, this.tier, info, flag.isAdvanced())) {
				for(String line : info) list.add(Component.literal(line));
				return;
			}
		}

		if(this == ModItems.upgrade_radius.get()) {
			list.add(Component.literal("Forcefield Range Upgrade").withStyle(ChatFormatting.RED));
			list.add(Component.literal("Radius +16 / Consumption +500"));
			list.add(Component.literal("Stacks to 16"));
		}

		if(this == ModItems.upgrade_health.get()) {
			list.add(Component.literal("Forcefield Health Upgrade").withStyle(ChatFormatting.RED));
			list.add(Component.literal("Max. Health +50 / Consumption +250"));
			list.add(Component.literal("Stacks to 16"));
		}

		if(this == ModItems.upgrade_smelter.get()) {
			list.add(Component.literal("Mining Laser Upgrade").withStyle(ChatFormatting.RED));
			list.add(Component.literal("Smelts blocks. Easy enough."));
		}

		if(this == ModItems.upgrade_shredder.get()) {
			list.add(Component.literal("Mining Laser Upgrade").withStyle(ChatFormatting.RED));
			list.add(Component.literal("Crunches ores"));
		}

		if(this == ModItems.upgrade_centrifuge.get()) {
			list.add(Component.literal("Mining Laser Upgrade").withStyle(ChatFormatting.RED));
			list.add(Component.literal("Hopefully self-explanatory"));
		}

		if(this == ModItems.upgrade_crystallizer.get()) {
			list.add(Component.literal("Mining Laser Upgrade").withStyle(ChatFormatting.RED));
			list.add(Component.literal("Your new best friend"));
		}

		if(this == ModItems.upgrade_screm.get()) {
			list.add(Component.literal("Mining Laser Upgrade").withStyle(ChatFormatting.RED));
			list.add(Component.literal("It's like in Super Mario where all blocks are"));
			list.add(Component.literal("actually Toads, but here it's Half-Life scientists"));
			list.add(Component.literal("and they scream. A lot."));
		}

		if(this == ModItems.upgrade_nullifier.get()) {
			list.add(Component.literal("Mining Laser Upgrade").withStyle(ChatFormatting.RED));
			list.add(Component.literal("50% chance to override worthless items with /dev/zero"));
			list.add(Component.literal("50% chance to move worthless items to /dev/null"));
		}

		if(this == ModItems.upgrade_gc_speed.get()) {
			list.add(Component.literal("Gas Centrifuge Upgrade").withStyle(ChatFormatting.RED));
			list.add(Component.literal("Allows for total isotopic separation of HEUF6"));
			list.add(Component.literal("also your centrifuge goes sicko mode").withStyle(ChatFormatting.YELLOW));
		}
	}

	public static enum UpgradeType {
		SPEED,
		EFFECT,
		POWER,
		FORTUNE,
		AFTERBURN,
		OVERDRIVE,
		SPECIAL,
		LM_DESROYER,
		LM_SCREM,
		LM_SMELTER(true),
		LM_SHREDDER(true),
		LM_CENTRIFUGE(true),
		LM_CRYSTALLIZER(true),
		GS_SPEED;

		public boolean mutex = false;

		private UpgradeType() { }

		private UpgradeType(boolean mutex) {
			this.mutex = mutex;
		}
	}
}
