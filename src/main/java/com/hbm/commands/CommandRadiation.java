package com.hbm.commands;

import com.hbm.handler.radiation.ChunkRadiationManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/** /ntmrad clear | set <amount>, same as the original */
public class CommandRadiation {

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("ntmrad")
				.requires(source -> source.hasPermission(2))
				.then(Commands.literal("clear").executes(ctx -> {
					ChunkRadiationManager.proxy.clearSystem(ctx.getSource().getLevel());
					ctx.getSource().sendSuccess(() -> Component.literal("Cleared radiation data!"), true);
					return 1;
				}))
				.then(Commands.literal("set").then(Commands.argument("amount", FloatArgumentType.floatArg(0F, 100_000F)).executes(ctx -> {
					float amount = FloatArgumentType.getFloat(ctx, "amount");
					BlockPos pos = BlockPos.containing(ctx.getSource().getPosition());
					ChunkRadiationManager.proxy.setRadiation(ctx.getSource().getLevel(), pos, amount);
					ctx.getSource().sendSuccess(() -> Component.literal("Radiation set."), true);
					return 1;
				}))));
	}
}
