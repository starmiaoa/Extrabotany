package io.github.lounode.extrabotany.forge;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

public final class ForgeExtrabotanyCommands {
	private ForgeExtrabotanyCommands() {}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("exbot")
				.requires(src -> src.hasPermission(2))
				.then(Commands.literal("itemcheck")
						.executes(ctx -> {
							boolean current = ExtraBotanyConfig.common().guardianItemCheck();
							ctx.getSource().sendSuccess(() -> Component.translatable(
									"commands.extrabotany.itemcheck.query",
									Component.translatable(current ? "commands.extrabotany.state.on" : "commands.extrabotany.state.off")
											.withStyle(current ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
							return 1;
						})
						.then(Commands.argument("value", BoolArgumentType.bool())
								.executes(ctx -> {
									boolean value = BoolArgumentType.getBool(ctx, "value");
									ForgeExtrabotanyConfig.setGuardianItemCheck(value);
									ctx.getSource().sendSuccess(() -> Component.translatable(
											value ? "commands.extrabotany.itemcheck.on" : "commands.extrabotany.itemcheck.off")
											.withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED), true);
									return 1;
								}))));
	}
}
