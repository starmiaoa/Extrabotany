package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.eventwrapper.event.entity.player.AttackEntityEventWrapper;
import io.github.lounode.eventwrapper.event.entity.player.PlayerInteractEventWrapper;
import io.github.lounode.eventwrapper.eventbus.api.EventBusSubscriberWrapper;
import io.github.lounode.eventwrapper.eventbus.api.SubscribeEventWrapper;
import io.github.lounode.extrabotany.common.entity.AuraFireEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import io.github.lounode.extrabotany.common.util.SoundEventUtil;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPacketJingwei;
import io.github.lounode.extrabotany.xplat.ExClientXplatAbstractions;

@EventBusSubscriberWrapper
public class FeatherOfJingweiItem extends BaubleItem {
	public static final int MANA_PER_USE = 300;

	public FeatherOfJingweiItem(Properties props) {
		super(props);
	}

	@SubscribeEventWrapper
	public static void leftClick(PlayerInteractEventWrapper.LeftClickEmpty event) {
		Player player = event.getEntity();
		leftClickBlock(player);
	}

	public static void leftClickBlock(Player player) {
		// sendToServer 是客户端专用(会触发 ExClientXplatAbstractions 加载客户端类);
		// 目前只从 LeftClickEmpty(仅客户端)调用,加守卫防止将来绑到服务端也 fire 的事件时崩。
		if (!player.level().isClientSide) {
			return;
		}
		if (!player.getMainHandItem().isEmpty()) {
			return;
		}
		if (!EquipmentHandler.findOrEmpty(ExtraBotanyItems.featherOfJingwei, player).isEmpty()) {
			ExClientXplatAbstractions.INSTANCE.sendToServer(LeftClickPacketJingwei.INSTANCE);
		}
	}

	@SubscribeEventWrapper
	public static InteractionResult attackEntity(AttackEntityEventWrapper event) {
		Player player = event.getEntity();
		if (!player.level().isClientSide) {
			trySpawnAuraFire(player);
		}
		return InteractionResult.PASS;
	}

	public static void trySpawnAuraFire(Player player) {
		trySpawnAuraFire(player, player.getAttackStrengthScale(0F));
	}

	public static void trySpawnAuraFire(Player player, float attackStrength) {
		if (attackStrength != 1 ||
				player.isSpectator() ||
				!player.getMainHandItem().isEmpty()) {
			return;
		}

		ItemStack jingwei = EquipmentHandler.findOrEmpty(ExtraBotanyItems.featherOfJingwei, player);
		if (jingwei.isEmpty()) {
			return;
		}
		if (!ManaItemHandler.instance().requestManaExactForTool(jingwei, player, MANA_PER_USE, true)) {
			return;
		}

		AuraFireEntity fire = getFire(player);
		player.level().addFreshEntity(fire);
		player.playNotifySound(ExtraBotanySounds.FEATHER_OF_JINGWEI_SHOOT, SoundSource.PLAYERS, 1f, SoundEventUtil.randomPitch(player.level()));
	}

	public static AuraFireEntity getFire(Player player) {
		AuraFireEntity fire = new AuraFireEntity(player);

		float motionModifier = 9F;

		fire.setGravity(0);
		fire.setDeltaMovement(fire.getDeltaMovement().scale(motionModifier));

		return fire;
	}
}
