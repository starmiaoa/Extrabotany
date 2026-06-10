package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

public class TrueTerrabladeProjectileEntity extends OldSwordProjectileEntity {
	private static final int LIFETIME = 60;
	private static final float DAMAGE = 11F;

	public TrueTerrabladeProjectileEntity(EntityType<? extends TrueTerrabladeProjectileEntity> entityType, Level level) {
		super(entityType, level);
	}

	public TrueTerrabladeProjectileEntity(Level level, LivingEntity owner, Vec3 targetPos) {
		super(ExtraBotanyEntityType.TRUE_TERRABLADE_PROJECTILE, level, owner);
		this.setPos(owner.getX(), owner.getY() + 1.1D, owner.getZ());
		this.shootAt(targetPos, 0.8D);
	}

	@Override
	protected Item getDefaultItem() {
		return ExtraBotanyItems.trueTerrablade;
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
	protected boolean discardOnHit() {
		return false;
	}

	@Override
	protected void spawnTrailParticles() {
		if (this.tickCount % 2 == 0) {
			this.level().addParticle(ParticleTypes.ENCHANT,
					this.getX(), this.getY(), this.getZ(),
					0.1D, 0.95D, 0.1D);
		}
	}
}
