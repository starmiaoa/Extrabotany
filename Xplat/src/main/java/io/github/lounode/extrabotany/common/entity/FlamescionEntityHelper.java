package io.github.lounode.extrabotany.common.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.handler.DamageHandler;

import java.util.List;

public final class FlamescionEntityHelper {
	private FlamescionEntityHelper() {}

	public static void damageAround(Entity source, @Nullable Entity owner, double range, float damage) {
		AABB area = source.getBoundingBox().inflate(range);
		List<LivingEntity> targets = source.level().getEntitiesOfClass(LivingEntity.class, area);
		for (LivingEntity target : DamageHandler.INSTANCE.getFilteredEntities(targets, owner)) {
			if (target == owner) {
				continue;
			}
			target.invulnerableTime = 0;
			target.hurt(flameSource(source, owner), damage);
		}
	}

	public static void slowEnemies(Entity source, @Nullable Entity owner, double range) {
		AABB area = source.getBoundingBox().inflate(range);
		List<LivingEntity> targets = source.level().getEntitiesOfClass(LivingEntity.class, area);
		for (LivingEntity target : DamageHandler.INSTANCE.getFilteredEntities(targets, owner)) {
			if (target instanceof Mob mob && mob instanceof Enemy) {
				target.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 2, 3));
			}
		}
	}

	public static DamageSource flameSource(Entity source, @Nullable Entity owner) {
		if (owner != null) {
			return source.damageSources().indirectMagic(source, owner);
		}
		return source.damageSources().magic();
	}
}
