package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import io.github.lounode.extrabotany.common.entity.AuraFireEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import io.github.lounode.extrabotany.common.util.SoundEventUtil;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPacketJingwei;
import io.github.lounode.extrabotany.xplat.ExClientXplatAbstractions;

public class FeatherOfJingweiItem extends BaubleItem {
	public static final int MANA_PER_USE = 300;

	public FeatherOfJingweiItem(Properties props) {
		super(props);
	}

	public static void leftClick(PlayerInteractEvent.LeftClickEmpty event) {
		Player player = event.getEntity();
		sendLeftClickPacketIfReady(player);
	}

	public static void leftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		sendLeftClickPacketIfReady(event.getEntity());
	}

	private static void sendLeftClickPacketIfReady(Player player) {
		// sendToServer 是客户端专用(会触发 ExClientXplatAbstractions 加载客户端类);
		// LeftClickBlock 事件在服务端也会 fire,必须在这里挡住,否则专用服务器崩。
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

	public static InteractionResult attackEntity(AttackEntityEvent event) {
		Player player = event.getEntity();
		if (!player.level().isClientSide) {
			trySpawnAuraFire(player);
		}
		return InteractionResult.PASS;
	}

	public static boolean trySpawnAuraFire(Player player) {
		return trySpawnAuraFire(player, player.getAttackStrengthScale(0F));
	}

	public static boolean trySpawnAuraFire(Player player, float attackStrength) {
		if (attackStrength != 1 ||
				player.isSpectator() ||
				!player.getMainHandItem().isEmpty()) {
			return false;
		}

		ItemStack jingwei = EquipmentHandler.findOrEmpty(ExtraBotanyItems.featherOfJingwei, player);
		if (jingwei.isEmpty()) {
			return false;
		}
		if (!ManaItemHandler.instance().requestManaExactForTool(jingwei, player, MANA_PER_USE, true)) {
			return false;
		}

		AuraFireEntity fire = getFire(player);
		player.level().addFreshEntity(fire);
		player.playNotifySound(ExtraBotanySounds.FEATHER_OF_JINGWEI_SHOOT, SoundSource.PLAYERS, 1f, SoundEventUtil.randomPitch(player.level()));
		return true;
	}

	public static AuraFireEntity getFire(Player player) {
		AuraFireEntity fire = new AuraFireEntity(player);

		float motionModifier = 9F;

		fire.setGravity(0);
		fire.setDeltaMovement(fire.getDeltaMovement().scale(motionModifier));

		return fire;
	}
}
