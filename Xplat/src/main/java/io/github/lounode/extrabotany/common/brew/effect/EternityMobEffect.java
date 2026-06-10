package io.github.lounode.extrabotany.common.brew.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;

public class EternityMobEffect extends MobEffect {
	public EternityMobEffect(MobEffectCategory category, int color) {
		super(category, color);
	}

	public static boolean shouldCancelDamage(LivingEntity entity) {
		return entity instanceof Player player && player.hasEffect(ExtraBotanyMobEffects.ETERNITY);
	}

	public static void onPlayerTick(Player player) {
		var effect = player.getEffect(ExtraBotanyMobEffects.ETERNITY);
		if (effect == null || effect.getDuration() >= 115) {
			return;
		}

		Vec3 movement = player.getDeltaMovement();
		player.setDeltaMovement(movement.x * 0.25D, 0.0D, movement.z * 0.25D);
	}
}
