package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import org.joml.Vector3f;

public class FlamescionSlashEntity extends FlamescionAreaEntity {
	private static final EntityDataAccessor<Float> ROTATION = SynchedEntityData.defineId(FlamescionSlashEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(FlamescionSlashEntity.class, EntityDataSerializers.FLOAT);
	private static final DustParticleOptions GOLDEN_SPARK = new DustParticleOptions(new Vector3f(1.0F, 0.72F, 0.2F), 1.15F);

	public FlamescionSlashEntity(EntityType<? extends FlamescionSlashEntity> type, Level level) {
		super(type, level);
	}

	public FlamescionSlashEntity(Level level, LivingEntity owner) {
		super(ExtraBotanyEntityType.FLAMESCION_SLASH, level, owner);
		setRotation(this.random.nextFloat() * 120.0F - 60.0F);
		setPitch(this.random.nextFloat() * 360.0F);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(ROTATION, 0.0F);
		builder.define(PITCH, 0.0F);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			spawnArcParticles();
		} else {
			slowEnemies(3.5D);
			if (this.tickCount == 2 || this.tickCount == 6) {
				damageAround(3.5D, 4.0F);
			}
			if (this.tickCount >= 10) {
				this.discard();
			}
		}
	}

	private void spawnArcParticles() {
		double radius = 1.1D + this.tickCount * 0.16D;
		double yawRad = Math.toRadians(getPitch());
		double tiltRad = Math.toRadians(getRotation());
		for (int i = 0; i < 6; i++) {
			double arcAngle = (this.random.nextDouble() - 0.5D) * Math.PI * 1.15D;
			double lx = Math.cos(arcAngle) * radius;
			double ly = Math.sin(arcAngle) * radius * Math.sin(tiltRad);
			double lz = Math.sin(arcAngle) * radius * Math.cos(tiltRad);
			double wx = lx * Math.cos(yawRad) - lz * Math.sin(yawRad);
			double wz = lx * Math.sin(yawRad) + lz * Math.cos(yawRad);
			this.level().addParticle(ParticleTypes.FLAME,
					this.getX() + wx, this.getY() + 0.25D + ly, this.getZ() + wz,
					wx * 0.04D, 0.03D + this.random.nextDouble() * 0.03D, wz * 0.04D);
			if (i % 2 == 0) {
				this.level().addParticle(GOLDEN_SPARK,
						this.getX() + wx * 0.85D, this.getY() + 0.25D + ly * 0.85D, this.getZ() + wz * 0.85D,
						0.0D, 0.02D, 0.0D);
			}
		}
		if (this.tickCount <= 4) {
			this.level().addParticle(ParticleTypes.LAVA, this.getX(), this.getY() + 0.25D, this.getZ(), 0.0D, 0.05D, 0.0D);
		}
	}

	public float getRotation() {
		return this.entityData.get(ROTATION);
	}

	public void setRotation(float rotation) {
		this.entityData.set(ROTATION, rotation);
	}

	public float getPitch() {
		return this.entityData.get(PITCH);
	}

	public void setPitch(float pitch) {
		this.entityData.set(PITCH, pitch);
	}
}
