package io.github.lounode.extrabotany.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import io.github.lounode.extrabotany.common.item.equipment.tool.FlamescionWeaponItem;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class LeftClickPacketFlamescion extends LeftClickPack {
	public static final LeftClickPacketFlamescion INSTANCE = new LeftClickPacketFlamescion();
	public static final ResourceLocation ID = prefix("flamescion_lc");
	public static final CustomPacketPayload.Type<LeftClickPacketFlamescion> TYPE = new CustomPacketPayload.Type<>(ID);
	public static final StreamCodec<FriendlyByteBuf, LeftClickPacketFlamescion> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	public static LeftClickPacketFlamescion decode(FriendlyByteBuf buf) {
		return INSTANCE;
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	@Override
	public CustomPacketPayload.Type<LeftClickPacketFlamescion> type() {
		return TYPE;
	}

	@Override
	public void handle(MinecraftServer server, ServerPlayer player) {
		executeRateLimited(server, player,
				() -> FlamescionWeaponItem.tryUseFromPacket(player, player.getAttackStrengthScale(0F)));
	}
}
