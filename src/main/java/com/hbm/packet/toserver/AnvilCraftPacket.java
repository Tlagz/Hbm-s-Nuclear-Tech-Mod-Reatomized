package com.hbm.packet.toserver;

import com.hbm.inventory.container.ContainerAnvil;
import com.hbm.inventory.recipes.anvil.AnvilRecipes;
import com.hbm.inventory.recipes.anvil.AnvilRecipes.AnvilConstructionRecipe;
import com.hbm.lib.RefStrings;
import com.hbm.util.InventoryUtil;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Anvil construction: recipe index in AnvilRecipes' list, mode 1 crafts as many as possible (shift click) */
public record AnvilCraftPacket(int recipeIndex, int mode) implements CustomPacketPayload {

	public static final Type<AnvilCraftPacket> TYPE = new Type<>(RefStrings.loc("anvil_craft"));

	public static final StreamCodec<RegistryFriendlyByteBuf, AnvilCraftPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, AnvilCraftPacket::recipeIndex,
			ByteBufCodecs.VAR_INT, AnvilCraftPacket::mode,
			AnvilCraftPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void send(AnvilConstructionRecipe recipe, int mode) {
		PacketDistributor.sendToServer(new AnvilCraftPacket(AnvilRecipes.getConstruction().indexOf(recipe), mode));
	}

	public static void handle(AnvilCraftPacket m, IPayloadContext context) {

		if(m.recipeIndex() < 0 || m.recipeIndex() >= AnvilRecipes.getConstruction().size()) //recipe is out of range -> bad
			return;

		Player p = context.player();

		if(!(p.containerMenu instanceof ContainerAnvil anvil)) //player isn't even using an anvil -> bad
			return;

		AnvilConstructionRecipe recipe = AnvilRecipes.getConstruction().get(m.recipeIndex());

		if(!recipe.isTierValid(anvil.tier)) //player is using the wrong type of anvil -> bad
			return;

		int count = m.mode() == 1 ? (recipe.output.size() > 1 ? 64 : (recipe.output.get(0).stack.getMaxStackSize() / recipe.output.get(0).stack.getCount())) : 1;

		for(int i = 0; i < count; i++) {

			if(InventoryUtil.doesPlayerHaveAStacks(p, recipe.input, true)) {
				InventoryUtil.giveChanceStacksToPlayer(p, recipe.output);
			} else {
				break;
			}
		}

		p.inventoryMenu.broadcastChanges();
		p.containerMenu.broadcastChanges();
	}
}
