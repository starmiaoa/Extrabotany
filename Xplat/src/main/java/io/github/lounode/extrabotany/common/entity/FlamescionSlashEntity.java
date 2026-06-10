package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class FlamescionSlashEntity extends FlamescionAreaEntity {
	private static final EntityDataAccessor<Float> ROTATION = SynchedEntityData.defineId(FlamescionSlashEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(FlamescionSlashEntity.class, EntityDataSerializers.FLOAT);

	public FlamescionSlashEntity(EntityType<? extends FlamescionSlashEntity> type, Level level) {
		super(type, level);
	}

	public FlamescionSlashEntity(Level level, LivingEntity owner) {
		super(ExtraBotanyEntityType.FLAMESCION_SLASH, level, owner);
		setRotation((float) (this.random.nextFloat() * 120F - 60F));
		setPitch(this.random.nextFloat() * 360F);
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(ROTATION, 0F);
		this.entityData.define(PITCH, 0F);
	}

	@Override
	public void tick() {
		super.tick();
		this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY() + 0.5D, this.getZ(), 0, 0.04D, 0);
		slowEnemies(3.5D);
		if (!this.level().isClientSide() && (this.tickCount == 2 || this.tickCount == 5)) {
			damageAround(3.5D, 1F);
		}
		if (!this.level().isClientSide() && this.tickCount >= 6) {
			this.discard();
		}
	}

	public void setRotation(float rotation) {
		this.entityData.set(ROTATION, rotation);
	}

	public void setPitch(float pitch) {
		this.entityData.set(PITCH, pitch);
	}
}
