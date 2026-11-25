package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;

public class FlamescionVoidEntity extends FlamescionAreaEntity {
	private static final DustParticleOptions VORTEX_GOLD = new DustParticleOptions(new Vector3f(1.0F, 0.65F, 0.15F), 1.3F);
	private static final double RANGE = 6.0D;
	private static final float TICK_DAMAGE = 3.0F;
	private static final float BURST_DAMAGE = 6.0F;

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
		if (this.level().isClientSide()) {
			spawnVortexParticles();
		} else {
			Entity owner = getOwnerEntity();
			Vec3 center = this.position().add(0.0D, 0.4D, 0.0D);
			for (LivingEntity target : getTargets(RANGE)) {
				if (target == owner) {
					continue;
				}
				Vec3 toCenter = center.subtract(target.position());
				double dist = toCenter.length();
				if (dist > 0.35D) {
					Vec3 radial = toCenter.scale(1.0D / dist);
					double pullSpeed = Math.min(0.45D, dist * 0.22D);
					Vec3 tangent = new Vec3(-radial.z, 0.0D, radial.x);
					if (tangent.lengthSqr() > 1.0E-4D) {
						tangent = tangent.normalize().scale(0.14D);
					} else {
						tangent = Vec3.ZERO;
					}
					Vec3 desired = radial.scale(pullSpeed).add(tangent);
					target.setDeltaMovement(target.getDeltaMovement().scale(0.35D).add(desired));
					target.hurtMarked = true;
				}
				target.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 4, 2));
			}
			if (this.tickCount % 10 == 0 && this.tickCount < 48) {
				damageAround(RANGE, TICK_DAMAGE);
			}
			if (this.tickCount == 48) {
				damageAround(RANGE + 0.5D, BURST_DAMAGE);
				this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.9F, 1.25F);
			}
			if (this.tickCount >= 50) {
				this.discard();
			}
		}
	}

	private void spawnVortexParticles() {
		double baseAngle = this.tickCount * 0.32D;
		double radius = 2.6D * (1.0D - (this.tickCount / 65.0D) * 0.35D);
		for (int i = 0; i < 4; i++) {
			double angle = baseAngle + i * (Math.PI * 0.5D);
			double px = this.getX() + Math.cos(angle) * radius;
			double pz = this.getZ() + Math.sin(angle) * radius;
			double vx = -Math.cos(angle) * 0.11D - Math.sin(angle) * 0.08D;
			double vz = -Math.sin(angle) * 0.11D + Math.cos(angle) * 0.08D;
			this.level().addParticle(i % 2 == 0 ? ParticleTypes.FLAME : ParticleTypes.SOUL_FIRE_FLAME,
					px, this.getY() + 0.35D + this.random.nextDouble() * 0.45D, pz, vx, 0.03D, vz);
		}
		if (this.tickCount % 2 == 0) {
			double a = this.random.nextDouble() * Math.PI * 2.0D;
			double r = 0.8D + this.random.nextDouble() * 2.4D;
			this.level().addParticle(VORTEX_GOLD,
					this.getX() + Math.cos(a) * r,
					this.getY() + 0.2D + this.random.nextDouble() * 0.9D,
					this.getZ() + Math.sin(a) * r,
					0.0D, 0.04D, 0.0D);
		}
		if (this.tickCount >= 47) {
			for (int i = 0; i < 12; i++) {
				double a = (Math.PI * 2.0D * i) / 12.0D;
				this.level().addParticle(ParticleTypes.FLAME,
						this.getX(), this.getY() + 0.4D, this.getZ(),
						Math.cos(a) * 0.38D, 0.08D + this.random.nextDouble() * 0.12D, Math.sin(a) * 0.38D);
			}
			this.level().addParticle(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
		}
	}
}
