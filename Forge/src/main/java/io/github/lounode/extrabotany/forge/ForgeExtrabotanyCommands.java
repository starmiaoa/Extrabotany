package io.github.lounode.extrabotany.forge;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

/**
 * /exbot itemcheck [true|false]
 * 开/关 盖亚守护者III(本我) 与 空之律者 召唤时对玩家身上(背包+饰品)物品白名单的检测(召唤门禁 + 战斗缴械)。
 * 与配置项 server.gaia.guardianItemCheck 同步:命令写入配置并保存,配置文件直接修改也会生效。
 */
public final class ForgeExtrabotanyCommands {
	private ForgeExtrabotanyCommands() {}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("exbot")
				.requires(src -> src.hasPermission(2))
				.then(Commands.literal("itemcheck")
						.executes(ctx -> {
							boolean cur = ExtraBotanyConfig.common().guardianItemCheck();
							ctx.getSource().sendSuccess(() -> Component.translatable(
									"commands.extrabotany.itemcheck.query",
									Component.translatable(cur ? "commands.extrabotany.state.on" : "commands.extrabotany.state.off")
											.withStyle(cur ? ChatFormatting.GREEN : ChatFormatting.RED)), false);
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
