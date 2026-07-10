package io.github.lounode.extrabotany.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import io.github.lounode.extrabotany.common.item.relic.OldExbotanyRelicSwordItem;
import io.github.lounode.extrabotany.common.item.relic.SpearOfSubspaceItem;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class LeftClickPacketOldRelicSword extends LeftClickPack {
	public static final LeftClickPacketOldRelicSword INSTANCE = new LeftClickPacketOldRelicSword();
	public static final ResourceLocation ID = prefix("old_relic_sword_lc");
	public static final CustomPacketPayload.Type<LeftClickPacketOldRelicSword> TYPE = new CustomPacketPayload.Type<>(ID);
	public static final StreamCodec<FriendlyByteBuf, LeftClickPacketOldRelicSword> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	public static LeftClickPacketOldRelicSword decode(FriendlyByteBuf buf) {
		return INSTANCE;
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	@Override
	public CustomPacketPayload.Type<LeftClickPacketOldRelicSword> type() {
		return TYPE;
	}

	@Override
	public void handle(MinecraftServer server, ServerPlayer player) {
		executeRateLimited(server, player, () -> {
			float scale = player.getAttackStrengthScale(0F);
			return OldExbotanyRelicSwordItem.tryUseFromPacket(player, scale)
					|| SpearOfSubspaceItem.tryUseFromPacket(player, scale);
		});
	}
}
