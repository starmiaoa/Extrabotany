package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

public class FlamescionUltEntity extends FlamescionAreaEntity {
	private static final double RANGE = 8D;
	private static final float DAMAGE = 12F;

	public FlamescionUltEntity(EntityType<? extends FlamescionUltEntity> type, Level level) {
		super(type, level);
	}

	public FlamescionUltEntity(Level level, LivingEntity owner) {
		super(ExtraBotanyEntityType.FLAMESCION_ULT, level, owner);
	}

	@Override
	protected void defineSynchedData() {
	}

	@Override
	public void tick() {
		super.tick();
		if (this.tickCount == 1 && !this.level().isClientSide()) {
			this.level().playSound(null, this.blockPosition(), ExtraBotanySounds.FLAMESCION_ULT, SoundSource.PLAYERS, 1F, 1F);
		}
		if (!this.level().isClientSide() && (this.tickCount == 10 || this.tickCount == 35 || this.tickCount == 60)) {
			damageAround(RANGE, DAMAGE);
		}
		if (this.level().isClientSide() && this.tickCount >= 40) {
			this.level().addParticle(ParticleTypes.EXPLOSION,
					this.getX() - 2D + this.random.nextDouble() * 4D,
					this.getY() - 2D + this.random.nextDouble() * 4D,
					this.getZ() - 2D + this.random.nextDouble() * 4D,
					0, 0, 0);
		}
		if (!this.level().isClientSide() && this.tickCount >= 85) {
			this.discard();
		}
	}
}
