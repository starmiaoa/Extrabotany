package io.github.lounode.extrabotany.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import io.github.lounode.extrabotany.common.item.equipment.tool.FlamescionWeaponItem;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class LeftClickPacketFlamescion extends LeftClickPack {
	public static final LeftClickPacketFlamescion INSTANCE = new LeftClickPacketFlamescion();
	public static final ResourceLocation ID = prefix("flamescion_lc");

	public static LeftClickPacketFlamescion decode(FriendlyByteBuf buf) {
		return INSTANCE;
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	@Override
	public void handle(MinecraftServer server, ServerPlayer player) {
		float scale = player.getAttackStrengthScale(0F);
		server.execute(() -> FlamescionWeaponItem.tryUseFromPacket(player, scale));
	}
}
