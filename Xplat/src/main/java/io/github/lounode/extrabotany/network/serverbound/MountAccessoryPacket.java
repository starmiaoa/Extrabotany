package io.github.lounode.extrabotany.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.handler.EquipmentHandler;

import io.github.lounode.extrabotany.common.item.equipment.bauble.MountAccessoryItem;
import io.github.lounode.extrabotany.network.ExtrabotanyPacket;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public record MountAccessoryPacket() implements ExtrabotanyPacket {
	public static final ResourceLocation ID = prefix("mount_accessory");
	public static final CustomPacketPayload.Type<MountAccessoryPacket> TYPE = new CustomPacketPayload.Type<>(ID);
	public static final StreamCodec<FriendlyByteBuf, MountAccessoryPacket> STREAM_CODEC = StreamCodec.ofMember(MountAccessoryPacket::encode, MountAccessoryPacket::decode);

	@Override
	public void encode(FriendlyByteBuf buf) {}

	public static MountAccessoryPacket decode(FriendlyByteBuf buf) {
		return new MountAccessoryPacket();
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	@Override
	public CustomPacketPayload.Type<MountAccessoryPacket> type() {
		return TYPE;
	}

	public static void handle(MountAccessoryPacket packet, MinecraftServer server, ServerPlayer player) {
		server.execute(() -> {
			if (player.getVehicle() != null) {
				return;
			}
			ItemStack stack = EquipmentHandler.findOrEmpty(s -> s.getItem() instanceof MountAccessoryItem, player);
			if (!(stack.getItem() instanceof MountAccessoryItem accessory)) {
				return;
			}
			Entity mount = accessory.createMount(player.level());
			mount.setPos(player.getX(), player.getY() + 0.5D, player.getZ());
			mount.setYRot(player.getYRot());
			if (player.level().addFreshEntity(mount)) {
				player.startRiding(mount);
			}
		});
	}
}
