package io.github.lounode.extrabotany.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import io.github.lounode.extrabotany.common.entity.MotorEntity;
import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public record MotorInputPacket(boolean forward, boolean back, boolean left, boolean right, boolean jump, boolean cyclonePressed) implements ExtrabotanyPacket {
	public static final ResourceLocation ID = prefix("motor_input");

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBoolean(forward());
		buf.writeBoolean(back());
		buf.writeBoolean(left());
		buf.writeBoolean(right());
		buf.writeBoolean(jump());
		buf.writeBoolean(cyclonePressed());
	}

	public static MotorInputPacket decode(FriendlyByteBuf buf) {
		return new MotorInputPacket(buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
				buf.readBoolean(), buf.readBoolean());
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	public static void handle(MotorInputPacket packet, MinecraftServer server, ServerPlayer player) {
		server.execute(() -> {
			if (player.getVehicle() instanceof MotorEntity motor) {
				motor.updateInput(packet.forward(), packet.back(), packet.left(), packet.right(), packet.jump(), packet.cyclonePressed());
			}
		});
	}
}
