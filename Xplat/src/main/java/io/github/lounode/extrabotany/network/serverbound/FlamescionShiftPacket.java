package io.github.lounode.extrabotany.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import io.github.lounode.extrabotany.common.item.equipment.tool.FlamescionWeaponItem;
import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public record FlamescionShiftPacket() implements ExtrabotanyPacket {
	public static final ResourceLocation ID = prefix("flamescion_shift");

	@Override
	public void encode(FriendlyByteBuf buf) {}

	public static FlamescionShiftPacket decode(FriendlyByteBuf buf) {
		return new FlamescionShiftPacket();
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	public static void handle(FlamescionShiftPacket packet, MinecraftServer server, ServerPlayer player) {
		server.execute(() -> FlamescionWeaponItem.castShiftDash(player));
	}
}
