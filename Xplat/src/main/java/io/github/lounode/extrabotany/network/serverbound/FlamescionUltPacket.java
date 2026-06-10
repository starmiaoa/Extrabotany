package io.github.lounode.extrabotany.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import io.github.lounode.extrabotany.common.item.equipment.tool.FlamescionWeaponItem;
import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public record FlamescionUltPacket() implements ExtrabotanyPacket {
	public static final ResourceLocation ID = prefix("flamescion_ult");
	public static final CustomPacketPayload.Type<FlamescionUltPacket> TYPE = new CustomPacketPayload.Type<>(ID);
	public static final StreamCodec<FriendlyByteBuf, FlamescionUltPacket> STREAM_CODEC = StreamCodec.ofMember(FlamescionUltPacket::encode, FlamescionUltPacket::decode);

	@Override
	public void encode(FriendlyByteBuf buf) {}

	public static FlamescionUltPacket decode(FriendlyByteBuf buf) {
		return new FlamescionUltPacket();
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	@Override
	public CustomPacketPayload.Type<FlamescionUltPacket> type() {
		return TYPE;
	}

	public static void handle(FlamescionUltPacket packet, MinecraftServer server, ServerPlayer player) {
		server.execute(() -> FlamescionWeaponItem.castUltimate(player));
	}
}
