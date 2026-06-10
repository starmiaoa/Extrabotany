package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;

public class FlamescionVoidEntity extends FlamescionAreaEntity {
	private static final double RANGE = 6D;
	private static final float DAMAGE = 1.5F;

	public FlamescionVoidEntity(EntityType<? extends FlamescionVoidEntity> type, Level level) {
		super(type, level);
	}

	public FlamescionVoidEntity(Level level, LivingEntity owner) {
		super(ExtraBotanyEntityType.FLAMESCION_VOID, level, owner);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	public void tick() {
		super.tick();
		this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 0.3D, this.getZ(), 0, 0.03D, 0);
		if (!this.level().isClientSide()) {
			Entity owner = getOwnerEntity();
			for (LivingEntity target : getTargets(RANGE)) {
				if (target == owner) {
					continue;
				}
				Vec3 pull = this.position().subtract(target.position());
				if (pull.lengthSqr() > 0D) {
					target.setDeltaMovement(pull.normalize().scale(1.5D));
					target.hurtMarked = true;
				}
				target.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 2, 3));
			}
			if (this.tickCount % 15 == 0) {
				damageAround(RANGE, DAMAGE);
			}
			if (this.tickCount >= 40) {
				this.discard();
			}
		}
	}
}
