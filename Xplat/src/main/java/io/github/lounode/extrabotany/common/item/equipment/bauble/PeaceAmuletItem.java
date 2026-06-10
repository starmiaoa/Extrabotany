package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.handler.EquipmentHandler;

import io.github.lounode.eventwrapper.event.entity.living.LivingHurtEventWrapper;
import io.github.lounode.eventwrapper.eventbus.api.EventBusSubscriberWrapper;
import io.github.lounode.eventwrapper.eventbus.api.SubscribeEventWrapper;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;

public class PeaceAmuletItem extends SimpleBaubleItem {
	public PeaceAmuletItem(Properties props) {
		super(props);
	}

	@EventBusSubscriberWrapper
	public static class EventHandler {
		@SubscribeEventWrapper
		public static void onLivingHurt(LivingHurtEventWrapper event) {
			Entity attacker = event.getSource().getEntity();
			if (!(attacker instanceof Player player)) {
				return;
			}
			if (!isPeaceAmuletDamage(event, player)) {
				return;
			}

			LivingEntity target = event.getEntity();
			boolean sourceEquipped = isEquipped(player);
			boolean targetEquipped = target instanceof Player targetPlayer && isEquipped(targetPlayer);
			if ((sourceEquipped && isProtectedTarget(target)) || targetEquipped) {
				event.setAmount(0);
				event.setCanceled(true);
			}
		}

		private static boolean isPeaceAmuletDamage(LivingHurtEventWrapper event, Player player) {
			return event.getSource().is(ExtraBotanyTags.DamageTypes.PEACE_AMULET_AVAILABLE)
					|| isExtraBotanyItem(player.getMainHandItem())
					|| isExtraBotanyItem(player.getOffhandItem());
		}

		private static boolean isProtectedTarget(LivingEntity target) {
			return target instanceof Player || (target instanceof Mob && !(target instanceof Enemy));
		}

		private static boolean isEquipped(Player player) {
			return !EquipmentHandler.findOrEmpty(ExtraBotanyItems.peaceAmulet, player).isEmpty();
		}

		private static boolean isExtraBotanyItem(ItemStack stack) {
			Item item = stack.getItem();
			return ExtraBotanyItems.ALL.containsValue(item);
		}
	}
}
