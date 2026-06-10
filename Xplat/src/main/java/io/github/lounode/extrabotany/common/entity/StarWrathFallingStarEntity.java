package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

public class StarWrathFallingStarEntity extends ThrowableItemProjectile {
	private static final int LIFETIME = 80;
	private static final float DAMAGE = 12F;

	public StarWrathFallingStarEntity(EntityType<? extends StarWrathFallingStarEntity> entityType, Level level) {
		super(entityType, level);
	}

	public StarWrathFallingStarEntity(Level level, LivingEntity owner) {
		super(ExtraBotanyEntityType.STAR_WRATH_FALLING_STAR, owner, level);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			this.level().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
			return;
		}
		if (this.tickCount > LIFETIME) {
			this.discard();
		}
	}

	@Override
	protected Item getDefaultItem() {
		return ExtraBotanyItems.starWrath;
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		if (!this.level().isClientSide() && result.getEntity() instanceof LivingEntity living && living != this.getOwner()) {
			living.hurt(makeDamageSource(), DAMAGE);
			this.discard();
		}
	}

	@Override
	protected void onHit(HitResult result) {
		super.onHit(result);
		if (!this.level().isClientSide() && result.getType() == HitResult.Type.BLOCK) {
			this.discard();
		}
	}

	private DamageSource makeDamageSource() {
		Entity owner = this.getOwner();
		if (owner instanceof Player player) {
			return this.damageSources().playerAttack(player);
		}
		if (owner instanceof LivingEntity living) {
			return this.damageSources().mobAttack(living);
		}
		return this.damageSources().generic();
	}
}
