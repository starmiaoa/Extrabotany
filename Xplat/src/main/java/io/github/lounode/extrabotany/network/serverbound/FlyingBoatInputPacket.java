package io.github.lounode.extrabotany.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import io.github.lounode.extrabotany.common.entity.FlyingBoatEntity;
import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public record FlyingBoatInputPacket(boolean forward, boolean back, boolean left, boolean right, boolean up, boolean down) implements ExtrabotanyPacket {
	public static final ResourceLocation ID = prefix("flying_boat_input");

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBoolean(forward());
		buf.writeBoolean(back());
		buf.writeBoolean(left());
		buf.writeBoolean(right());
		buf.writeBoolean(up());
		buf.writeBoolean(down());
	}

	public static FlyingBoatInputPacket decode(FriendlyByteBuf buf) {
		return new FlyingBoatInputPacket(buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	public static void handle(FlyingBoatInputPacket packet, MinecraftServer server, ServerPlayer player) {
		server.execute(() -> {
			if (player.getVehicle() instanceof FlyingBoatEntity boat && boat.getControllingPassenger() == player) {
				boat.updateInput(packet.forward(), packet.back(), packet.left(), packet.right(), packet.up(), packet.down());
			}
		});
	}
}
