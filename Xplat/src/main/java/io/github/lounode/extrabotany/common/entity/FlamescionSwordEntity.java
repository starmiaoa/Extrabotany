package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class FlamescionSwordEntity extends ThrowableProjectile {
	private static final float DAMAGE = 4F;
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
	protected void defineSynchedData() {
	}

	@Override
	public void tick() {
		super.tick();
		this.setNoGravity(true);
		this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 0, 0.03D, 0);
		if (!this.level().isClientSide() && this.tickCount % 4 == 0) {
			FlamescionEntityHelper.damageAround(this, this.getOwner(), RANGE, DAMAGE);
		}
		if (!this.level().isClientSide() && this.tickCount >= 30) {
			this.discard();
		}
	}

	@Override
	protected void onHit(HitResult result) {
	}
}
