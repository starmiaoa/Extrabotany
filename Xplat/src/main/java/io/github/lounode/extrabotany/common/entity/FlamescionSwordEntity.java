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

public class FlamescionSwordEntity extends ThrowableProjectile {
	private static final EntityDataAccessor<Float> FLIGHT_YAW = SynchedEntityData.defineId(FlamescionSwordEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> FLIGHT_PITCH = SynchedEntityData.defineId(FlamescionSwordEntity.class, EntityDataSerializers.FLOAT);
	private static final DustParticleOptions GOLDEN_PLASMA = new DustParticleOptions(new Vector3f(1.0F, 0.78F, 0.22F), 1.25F);
	private static final float DAMAGE = 6.0F;
	private static final double RANGE = 3.5D;

	public FlamescionSwordEntity(EntityType<? extends FlamescionSwordEntity> type, Level level) {
		super(type, level);
		this.setNoGravity(true);
	}

	public FlamescionSwordEntity(Level level, LivingEntity owner) {
		super(ExtraBotanyEntityType.FLAMESCION_SWORD, owner, level);
		this.setNoGravity(true);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(FLIGHT_YAW, 0.0F);
		builder.define(FLIGHT_PITCH, 0.0F);
	}

	public void setFlightDirection(Vec3 dir) {
		if (dir.lengthSqr() > 1.0E-6D) {
			double horizontal = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
			setFlightYaw((float) (Mth.atan2(dir.x, dir.z) * Mth.RAD_TO_DEG));
			setFlightPitch((float) (Mth.atan2(dir.y, horizontal) * Mth.RAD_TO_DEG));
		}
	}

	public float getFlightYaw() {
		return this.entityData.get(FLIGHT_YAW);
	}

	public void setFlightYaw(float yaw) {
		this.entityData.set(FLIGHT_YAW, yaw);
	}

	public float getFlightPitch() {
		return this.entityData.get(FLIGHT_PITCH);
	}

	public void setFlightPitch(float pitch) {
		this.entityData.set(FLIGHT_PITCH, pitch);
	}

	@Override
	public void tick() {
		super.tick();
		this.setNoGravity(true);
		if (this.level().isClientSide()) {
			spawnWakeParticles();
		} else {
			if (this.tickCount == 1 && getFlightYaw() == 0.0F && getFlightPitch() == 0.0F) {
				setFlightDirection(this.getDeltaMovement());
			}
			if (this.tickCount % 3 == 0) {
				FlamescionEntityHelper.damageAround(this, this.getOwner(), RANGE, DAMAGE);
			}
			if (this.tickCount >= 26) {
				this.discard();
			}
		}
	}

	private void spawnWakeParticles() {
		Vec3 vel = this.getDeltaMovement();
		for (int i = 0; i < 4; i++) {
			double ox = (this.random.nextDouble() - 0.5D) * 0.8D;
			double oy = (this.random.nextDouble() - 0.5D) * 0.8D;
			double oz = (this.random.nextDouble() - 0.5D) * 0.8D;
			this.level().addParticle(ParticleTypes.FLAME,
					this.getX() + ox, this.getY() + oy, this.getZ() + oz,
					-vel.x * 0.12D, 0.02D - vel.y * 0.12D, -vel.z * 0.12D);
		}
		this.level().addParticle(GOLDEN_PLASMA,
				this.getX() + (this.random.nextDouble() - 0.5D) * 0.5D,
				this.getY() + (this.random.nextDouble() - 0.5D) * 0.5D,
				this.getZ() + (this.random.nextDouble() - 0.5D) * 0.5D,
				0.0D, 0.02D, 0.0D);
		if (this.tickCount % 2 == 0) {
			this.level().addParticle(ParticleTypes.LAVA, this.getX(), this.getY(), this.getZ(), 0.0D, 0.02D, 0.0D);
		}
	}

	@Override
	protected void onHit(HitResult result) {
	}
}
