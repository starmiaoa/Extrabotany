package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class HerrscherVoidFieldEntity extends Entity {
	private static final String TAG_LIFE = "Life";
	private int life = 60;

	public HerrscherVoidFieldEntity(EntityType<? extends HerrscherVoidFieldEntity> type, Level level) {
		super(type, level);
	}

	public HerrscherVoidFieldEntity(Level level) {
		this(ExtraBotanyEntityType.VOID_FIELD, level);
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			for (int i = 0; i < 6; i++) {
				level().addParticle(ParticleTypes.PORTAL, getX() + (random.nextDouble() - 0.5D) * 3D, getY() + random.nextDouble() * 2D, getZ() + (random.nextDouble() - 0.5D) * 3D, 0, 0.02D, 0);
			}
			return;
		}
		for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, new AABB(blockPosition()).inflate(1.5D, 1.5D, 1.5D))) {
			living.setDeltaMovement(living.getDeltaMovement().scale(0.12D));
			living.hurtMarked = true;
		}
		if (--life <= 0) {
			discard();
		}
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		this.life = tag.getInt(TAG_LIFE);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putInt(TAG_LIFE, life);
	}
}
