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
		// NeoForge payload handlers run synchronously on the game thread, so no dispatch is needed.
		// Delaying via server.execute() pushes the attack-strength check to the next tick, by which
		// point the client's own START_DIGGING (sent right after the left-click packet) has already
		// reset the cooldown -- making getAttackStrengthScale(0F) != 1 and the shot fire at random.
		long gameTime = player.level().getGameTime();
		Long lastUse = LAST_SUCCESSFUL_USE.get(player);
		if (lastUse != null && gameTime >= lastUse && gameTime - lastUse < MIN_INTERVAL_TICKS) {
			return;
		}
		if (action.getAsBoolean()) {
			LAST_SUCCESSFUL_USE.put(player, gameTime);
			player.resetAttackStrengthTicker();
		}
	}

	public abstract void handle(MinecraftServer server, ServerPlayer player);
}
