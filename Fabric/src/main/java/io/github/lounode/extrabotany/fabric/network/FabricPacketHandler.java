package io.github.lounode.extrabotany.fabric.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import vazkii.botania.network.TriConsumer;

import io.github.lounode.extrabotany.client.gui.HUD;
import io.github.lounode.extrabotany.network.clientbound.ColorfulBossEventPacket;
import io.github.lounode.extrabotany.network.clientbound.GaiaBossEventPacket;
import io.github.lounode.extrabotany.network.clientbound.ManaReaderPacket;
import io.github.lounode.extrabotany.network.clientbound.SpawnGaiaPacket;
import io.github.lounode.extrabotany.network.serverbound.FlyingBoatInputPacket;
import io.github.lounode.extrabotany.network.serverbound.FlamescionShiftPacket;
import io.github.lounode.extrabotany.network.serverbound.FlamescionUltPacket;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPacketExcalibur;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPacketFlamescion;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPacketJingwei;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPacketOldRelicSword;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPacketVoidArchives;
import io.github.lounode.extrabotany.network.serverbound.MountAccessoryPacket;
import io.github.lounode.extrabotany.network.serverbound.MotorInputPacket;
import io.github.lounode.extrabotany.network.serverbound.UfoInputPacket;

import java.util.function.Consumer;
import java.util.function.Function;

public class FabricPacketHandler {
	private FabricPacketHandler() {}

	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(LeftClickPacketExcalibur.ID, makeServerBoundHandler(LeftClickPacketExcalibur::decode, LeftClickPacketExcalibur::handle));
		ServerPlayNetworking.registerGlobalReceiver(LeftClickPacketFlamescion.ID, makeServerBoundHandler(LeftClickPacketFlamescion::decode, LeftClickPacketFlamescion::handle));
		ServerPlayNetworking.registerGlobalReceiver(LeftClickPacketJingwei.ID, makeServerBoundHandler(LeftClickPacketJingwei::decode, LeftClickPacketJingwei::handle));
		ServerPlayNetworking.registerGlobalReceiver(LeftClickPacketOldRelicSword.ID, makeServerBoundHandler(LeftClickPacketOldRelicSword::decode, LeftClickPacketOldRelicSword::handle));
		ServerPlayNetworking.registerGlobalReceiver(LeftClickPacketVoidArchives.ID, makeServerBoundHandler(LeftClickPacketVoidArchives::decode, LeftClickPacketVoidArchives::handle));
		ServerPlayNetworking.registerGlobalReceiver(UfoInputPacket.ID, makeServerBoundHandler(UfoInputPacket::decode, UfoInputPacket::handle));
		ServerPlayNetworking.registerGlobalReceiver(MotorInputPacket.ID, makeServerBoundHandler(MotorInputPacket::decode, MotorInputPacket::handle));
		ServerPlayNetworking.registerGlobalReceiver(FlyingBoatInputPacket.ID, makeServerBoundHandler(FlyingBoatInputPacket::decode, FlyingBoatInputPacket::handle));
		ServerPlayNetworking.registerGlobalReceiver(MountAccessoryPacket.ID, makeServerBoundHandler(MountAccessoryPacket::decode, MountAccessoryPacket::handle));
		ServerPlayNetworking.registerGlobalReceiver(FlamescionShiftPacket.ID, makeServerBoundHandler(FlamescionShiftPacket::decode, FlamescionShiftPacket::handle));
		ServerPlayNetworking.registerGlobalReceiver(FlamescionUltPacket.ID, makeServerBoundHandler(FlamescionUltPacket::decode, FlamescionUltPacket::handle));
		registerOperation();
	}

	public static void initClient() {
		ClientPlayNetworking.registerGlobalReceiver(ManaReaderPacket.ID, makeClientBoundHandler(ManaReaderPacket::decode, ManaReaderPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(SpawnGaiaPacket.ID, makeClientBoundHandler(SpawnGaiaPacket::decode, SpawnGaiaPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(ColorfulBossEventPacket.ID, makeClientBoundHandler(ColorfulBossEventPacket::decode, (packet) -> HUD.getInstance().getBossOverlay().update(packet)));
	}

	private static void registerOperation() {
		ColorfulBossEventPacket.Operation.register("add", () -> ColorfulBossEventPacket.AddOperation.CODEC);
		ColorfulBossEventPacket.Operation.register("remove", () -> ColorfulBossEventPacket.RemoveOperation.CODEC);
		ColorfulBossEventPacket.Operation.register("update_progress", () -> ColorfulBossEventPacket.UpdateProgressOperation.CODEC);
		ColorfulBossEventPacket.Operation.register("update_name", () -> ColorfulBossEventPacket.UpdateNameOperation.CODEC);
		ColorfulBossEventPacket.Operation.register("update_style", () -> ColorfulBossEventPacket.UpdateStyleOperation.CODEC);
		ColorfulBossEventPacket.Operation.register("update_properties", () -> ColorfulBossEventPacket.UpdatePropertiesOperation.CODEC);
		ColorfulBossEventPacket.Operation.register("update_player_count", () -> GaiaBossEventPacket.UpdatePlayerCountOperation.CODEC);
		ColorfulBossEventPacket.Operation.register("update_grain_time", () -> GaiaBossEventPacket.UpdateGrainTimeOperation.CODEC);
	}

	private static <T> ServerPlayNetworking.PlayChannelHandler makeServerBoundHandler(Function<FriendlyByteBuf, T> decoder, TriConsumer<T, MinecraftServer, ServerPlayer> handle) {
		return (server, player, _handler, buf, _responseSender) -> handle.accept(decoder.apply(buf), server, player);
	}

	private static <T> ClientPlayNetworking.PlayChannelHandler makeClientBoundHandler(Function<FriendlyByteBuf, T> decoder, Consumer<T> handler) {
		return (_client, _handler, buf, _responseSender) -> handler.accept(decoder.apply(buf));
	}
}
