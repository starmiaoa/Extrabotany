package io.github.lounode.extrabotany.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BooleanSupplier;

public abstract class LeftClickPack implements ExtrabotanyPacket {
	private static final int MIN_INTERVAL_TICKS = 1;
	private static final Map<ServerPlayer, Long> LAST_SUCCESSFUL_USE = new WeakHashMap<>();

	@Override
	public void encode(FriendlyByteBuf buf) {

	}

	@Override
	public abstract ResourceLocation getFabricId();

	protected static void executeRateLimited(MinecraftServer server, ServerPlayer player, BooleanSupplier action) {
		server.execute(() -> {
			long gameTime = player.level().getGameTime();
			Long lastUse = LAST_SUCCESSFUL_USE.get(player);
			if (lastUse != null && gameTime >= lastUse && gameTime - lastUse < MIN_INTERVAL_TICKS) {
				return;
			}
			if (action.getAsBoolean()) {
				LAST_SUCCESSFUL_USE.put(player, gameTime);
				player.resetAttackStrengthTicker();
			}
		});
	}

	public abstract void handle(MinecraftServer server, ServerPlayer player);
}
