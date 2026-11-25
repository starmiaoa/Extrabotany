package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

public class StrengthenSlashEntity extends ThrowableProjectile {
	private static final EntityDataAccessor<Float> SLASH_YAW = SynchedEntityData.defineId(StrengthenSlashEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> SLASH_PITCH = SynchedEntityData.defineId(StrengthenSlashEntity.class, EntityDataSerializers.FLOAT);
	private static final DustParticleOptions CRESCENT_GLOW = new DustParticleOptions(new Vector3f(1.0F, 0.52F, 0.12F), 1.35F);
	private static final float DAMAGE = 8.0F;
	private static final double RANGE = 3.0D;

	public StrengthenSlashEntity(EntityType<? extends StrengthenSlashEntity> type, Level level) {
		super(type, level);
		this.setNoGravity(true);
	}

	public StrengthenSlashEntity(Level level, LivingEntity owner) {
		super(ExtraBotanyEntityType.STRENGTHEN_SLASH, owner, level);
		this.setNoGravity(true);
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(SLASH_YAW, 0.0F);
		this.entityData.define(SLASH_PITCH, 0.0F);
	}

	public void setFlightDirection(Vec3 dir) {
		if (dir.lengthSqr() > 1.0E-6D) {
			double horizontal = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
			setSlashYaw((float) (Mth.atan2(dir.x, dir.z) * Mth.RAD_TO_DEG));
			setSlashPitch((float) (Mth.atan2(dir.y, horizontal) * Mth.RAD_TO_DEG));
		}
	}

	public float getSlashYaw() {
		return this.entityData.get(SLASH_YAW);
	}

	public void setSlashYaw(float yaw) {
		this.entityData.set(SLASH_YAW, yaw);
	}

	public float getSlashPitch() {
		return this.entityData.get(SLASH_PITCH);
	}

	public void setSlashPitch(float pitch) {
		this.entityData.set(SLASH_PITCH, pitch);
	}

	@Override
	public void tick() {
		super.tick();
		this.setNoGravity(true);
		if (this.level().isClientSide()) {
			spawnTrailParticles();
		} else {
			if (this.tickCount == 1 && getSlashYaw() == 0.0F && getSlashPitch() == 0.0F) {
				setFlightDirection(this.getDeltaMovement());
			}
			if (this.tickCount % 3 == 0) {
				FlamescionEntityHelper.damageAround(this, this.getOwner(), RANGE, DAMAGE);
			}
			if (this.tickCount >= 18) {
				this.discard();
			}
		}
	}

	private void spawnTrailParticles() {
		Vec3 vel = this.getDeltaMovement();
		Vec3 forward = vel.lengthSqr() > 1.0E-6D ? vel.normalize() : Vec3.directionFromRotation(-getSlashPitch(), -getSlashYaw());
		Vec3 side = new Vec3(-forward.z, 0.0D, forward.x);
		if (side.lengthSqr() > 1.0E-4D) {
			side = side.normalize();
		} else {
			side = new Vec3(1.0D, 0.0D, 0.0D);
		}
		for (int i = -2; i <= 2; i++) {
			double offset = i * 0.65D;
			double px = this.getX() + side.x * offset - forward.x * Math.abs(i) * 0.25D;
			double py = this.getY() + (this.random.nextDouble() - 0.5D) * 0.35D;
			double pz = this.getZ() + side.z * offset - forward.z * Math.abs(i) * 0.25D;
			this.level().addParticle(ParticleTypes.FLAME, px, py, pz, -forward.x * 0.08D, 0.02D, -forward.z * 0.08D);
			if (i == -1 || i == 1) {
				this.level().addParticle(CRESCENT_GLOW, px, py, pz, 0.0D, 0.01D, 0.0D);
			}
		}
		if (this.tickCount % 2 == 0) {
			this.level().addParticle(ParticleTypes.LAVA, this.getX(), this.getY(), this.getZ(), 0.0D, 0.02D, 0.0D);
		}
	}

	@Override
	protected void onHit(HitResult result) {
	}
}
