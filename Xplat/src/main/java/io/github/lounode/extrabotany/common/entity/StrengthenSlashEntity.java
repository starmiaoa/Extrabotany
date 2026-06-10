package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

public class StrengthenSlashEntity extends ThrowableProjectile {
	private static final float DAMAGE = 5F;
	private static final double RANGE = 2.5D;

	public StrengthenSlashEntity(EntityType<? extends StrengthenSlashEntity> type, Level level) {
		super(type, level);
		this.setNoGravity(true);
	}

	public StrengthenSlashEntity(Level level, LivingEntity owner) {
		super(ExtraBotanyEntityType.STRENGTHEN_SLASH, owner, level);
		this.setNoGravity(true);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	public void tick() {
		super.tick();
		this.setNoGravity(true);
		this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 0, 0.02D, 0);
		if (!this.level().isClientSide() && this.tickCount % 2 == 0) {
			FlamescionEntityHelper.damageAround(this, this.getOwner(), RANGE, DAMAGE);
		}
		if (!this.level().isClientSide() && this.tickCount >= 15) {
			this.discard();
		}
	}

	@Override
	protected void onHit(HitResult result) {
	}
}
