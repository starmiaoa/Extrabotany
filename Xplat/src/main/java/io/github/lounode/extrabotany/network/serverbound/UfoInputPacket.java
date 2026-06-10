package io.github.lounode.extrabotany.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import io.github.lounode.extrabotany.common.entity.UfoEntity;
import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public record UfoInputPacket(boolean forward, boolean back, boolean left, boolean right, boolean up, boolean down, boolean catchPressed) implements ExtrabotanyPacket {
	public static final ResourceLocation ID = prefix("ufo_input");

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBoolean(forward());
		buf.writeBoolean(back());
		buf.writeBoolean(left());
		buf.writeBoolean(right());
		buf.writeBoolean(up());
		buf.writeBoolean(down());
		buf.writeBoolean(catchPressed());
	}

	public static UfoInputPacket decode(FriendlyByteBuf buf) {
		return new UfoInputPacket(buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
				buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	public static void handle(UfoInputPacket packet, MinecraftServer server, ServerPlayer player) {
		server.execute(() -> {
			if (player.getVehicle() instanceof UfoEntity ufo && ufo.getControllingPassenger() == player) {
				ufo.updateInput(packet.forward(), packet.back(), packet.left(), packet.right(), packet.up(), packet.down(), packet.catchPressed());
			}
		});
	}
}
