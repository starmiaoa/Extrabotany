package io.github.lounode.extrabotany.common.brew.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;

public class WitchCurseMobEffect extends MobEffect {
	public WitchCurseMobEffect(MobEffectCategory category, int color) {
		super(category, color);
	}

	public static void onLivingHeal(LivingHealEvent event) {
		LivingEntity entity = event.getEntity();
		float amount = event.getAmount();
		if (!Float.isFinite(amount) || amount <= 0F) {
			event.setAmount(0F);
			return;
		}
		var effect = entity.getEffect(ExtraBotanyMobEffects.WITCH_CURSE);
		if (effect == null) {
			return;
		}
		int amplifier = Math.max(1, effect.getAmplifier());
		event.setAmount(amount / amplifier);
	}
}
