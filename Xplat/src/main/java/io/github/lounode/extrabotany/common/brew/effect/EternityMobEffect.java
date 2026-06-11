package io.github.lounode.extrabotany.common.brew.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class EternityMobEffect extends MobEffect {
	public EternityMobEffect(MobEffectCategory category, int color) {
		super(category, color);
	}

	public static void onLivingAttack(LivingIncomingDamageEvent event) {
		if (event.getEntity() instanceof Player player && player.hasEffect(ExtraBotanyMobEffects.ETERNITY)) {
			event.setAmount(0);
			event.setCanceled(true);
		}
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
