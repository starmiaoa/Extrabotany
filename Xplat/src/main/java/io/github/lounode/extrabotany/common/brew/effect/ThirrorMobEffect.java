package io.github.lounode.extrabotany.common.brew.effect;

import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import io.github.lounode.eventwrapper.event.entity.living.LivingAttackEventWrapper;
import io.github.lounode.eventwrapper.eventbus.api.EventBusSubscriberWrapper;
import io.github.lounode.eventwrapper.eventbus.api.SubscribeEventWrapper;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;

public class ThirrorMobEffect extends MobEffect {
	public ThirrorMobEffect(MobEffectCategory category, int color) {
		super(category, color);
	}

	@EventBusSubscriberWrapper
	public static class EventHandler {

		@SubscribeEventWrapper
		public static void onLivingAttack(LivingAttackEventWrapper event) {
			LivingEntity defender = event.getEntity();
			if (defender.level().isClientSide() || event.getSource().is(DamageTypes.THORNS)) {
				return;
			}
			MobEffectInstance effect = defender.getEffect(ExtraBotanyMobEffects.THIRROR);
			if (effect == null) {
				return;
			}
			if (!(event.getSource().getEntity() instanceof LivingEntity attacker) || attacker == defender) {
				return;
			}

			int level = effect.getAmplifier();
			float returnDamage = event.getAmount() / Math.max(1, 6 - level);
			if (returnDamage <= 0F) {
				return;
			}

			attacker.hurt(defender.damageSources().thorns(defender), returnDamage);
		}
	}
}
