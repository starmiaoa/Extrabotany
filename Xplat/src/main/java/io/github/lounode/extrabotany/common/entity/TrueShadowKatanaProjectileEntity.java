package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

public class TrueShadowKatanaProjectileEntity extends OldSwordProjectileEntity {
	private static final int LIFETIME = 40;
	private static final float DAMAGE = 5F;

	public TrueShadowKatanaProjectileEntity(EntityType<? extends TrueShadowKatanaProjectileEntity> entityType, Level level) {
		super(entityType, level);
	}

	public TrueShadowKatanaProjectileEntity(Level level, LivingEntity owner, Vec3 startPos, Vec3 targetPos) {
		super(ExtraBotanyEntityType.TRUE_SHADOW_KATANA_PROJECTILE, level, owner);
		this.setPos(startPos.x, startPos.y, startPos.z);
		this.shootAt(targetPos, 0.75D);
	}

	@Override
	protected Item getDefaultItem() {
		return ExtraBotanyItems.trueShadowKatana;
	}

	@Override
	protected int getHitStartTick() {
		return 4;
	}

	@Override
	protected int getLifetime() {
		return LIFETIME;
	}

	@Override
	protected float getDamage() {
		return DAMAGE;
	}

	@Override
	protected boolean shouldRespectInvulnerability() {
		return false;
	}

	@Override
	protected void spawnTrailParticles() {
		if (this.tickCount % 2 == 0) {
			this.level().addParticle(ParticleTypes.SMOKE,
					this.getX(), this.getY(), this.getZ(),
					0.0D, 0.0D, 0.0D);
		}
	}
}
