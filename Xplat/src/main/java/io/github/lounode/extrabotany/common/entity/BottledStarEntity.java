package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

public class BottledStarEntity extends ThrowableItemProjectile {
	private static final int LIFETIME = 130;

	public BottledStarEntity(EntityType<? extends BottledStarEntity> entityType, Level level) {
		super(entityType, level);
		this.setNoGravity(true);
	}

	public BottledStarEntity(Level level, LivingEntity owner) {
		super(ExtraBotanyEntityType.BOTTLED_STAR, owner, level);
		this.setNoGravity(true);
	}

	@Override
	public void tick() {
		super.tick();
		this.setNoGravity(true);
		this.setDeltaMovement(Vec3.ZERO);

		if (this.level().isClientSide) {
			this.level().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.25D, this.getZ(), 0, 0.01D, 0);
			return;
		}

		if (this.tickCount > LIFETIME || !(this.getOwner() instanceof LivingEntity owner) || !owner.isAlive()) {
			this.discard();
			return;
		}

		if (this.tickCount % 6 == 0) {
			spawnFallingStar(owner, randomTargetPos(1.8D));
		}

		if (this.tickCount % 4 == 0) {
			LivingEntity target = findTarget(owner);
			if (target != null) {
				spawnFallingStar(owner, target.position());
			}
		}
	}

	private Vec3 randomTargetPos(double range) {
		return this.position().add(
				(this.random.nextDouble() * 2D - 1D) * range,
				0,
				(this.random.nextDouble() * 2D - 1D) * range
		);
	}

	private LivingEntity findTarget(LivingEntity owner) {
		AABB area = this.getBoundingBox().inflate(12D);
		for (LivingEntity living : this.level().getEntitiesOfClass(LivingEntity.class, area)) {
			if (DamageHandler.INSTANCE.checkPassable(living, owner)) {
				return living;
			}
		}
		return null;
	}

	private void spawnFallingStar(LivingEntity owner, Vec3 impact) {
		Vec3 motionSource = new Vec3(
				(this.random.nextDouble() * 2D - 1D) * 32.4D,
				24D,
				(this.random.nextDouble() * 2D - 1D) * 32.4D
		);
		Vec3 start = impact.add(motionSource);
		Vec3 velocity = motionSource.normalize().reverse().scale(1.5D);

		StarWrathFallingStarEntity star = new StarWrathFallingStarEntity(this.level(), owner);
		star.setPos(start.x, start.y, start.z);
		star.setDeltaMovement(velocity);
		this.level().addFreshEntity(star);
	}

	@Override
	protected Item getDefaultItem() {
		return ExtraBotanyItems.bottledStar;
	}
}
