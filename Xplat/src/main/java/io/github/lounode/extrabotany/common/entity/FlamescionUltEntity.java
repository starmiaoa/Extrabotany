package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

public class FlamescionUltEntity extends FlamescionAreaEntity {
	private static final DustParticleOptions SOLAR_GOLD = new DustParticleOptions(new Vector3f(1.0F, 0.78F, 0.22F), 1.6F);
	private static final DustParticleOptions CRIMSON_FLARE = new DustParticleOptions(new Vector3f(0.95F, 0.22F, 0.14F), 1.7F);
	private static final double RANGE = 9.0D;

	public FlamescionUltEntity(EntityType<? extends FlamescionUltEntity> type, Level level) {
		super(type, level);
	}

	public FlamescionUltEntity(Level level, LivingEntity owner) {
		super(ExtraBotanyEntityType.FLAMESCION_ULT, level, owner);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			spawnUltParticles();
			return;
		}

		// Phase 1 (ticks 1-25): Sound, TimeLock domain, gently draw enemies inward
		if (this.tickCount == 1) {
			this.level().playSound(null, this.blockPosition(), ExtraBotanySounds.FLAMESCION_ULT, SoundSource.PLAYERS, 1.35F, 1.0F);
		}
		if (this.tickCount <= 45) {
			slowEnemies(RANGE);
		}
		if (this.tickCount <= 25) {
			Entity owner = getOwnerEntity();
			Vec3 center = this.position().add(0.0D, 0.5D, 0.0D);
			for (LivingEntity target : getTargets(RANGE)) {
				if (target == owner) {
					continue;
				}
				Vec3 toCenter = center.subtract(target.position());
				double dist = toCenter.length();
				if (dist > 1.2D) {
					double pull = Math.min(0.24D, dist * 0.12D);
					Vec3 step = toCenter.scale(pull / dist);
					target.setDeltaMovement(target.getDeltaMovement().scale(0.55D).add(step.x, Math.min(step.y, 0.15D), step.z));
					target.hurtMarked = true;
				}
			}
		}

		// Phase 2 (ticks 26-45): Giant blade plunges and cleaves the domain
		if (this.tickCount == 28) {
			damageAround(RANGE, 16.0F);
			this.level().playSound(null, this.blockPosition(), ExtraBotanySounds.FLAMESCION_SLASH, SoundSource.PLAYERS, 1.3F, 0.7F);
			this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.4F, 0.85F);
		}
		if (this.tickCount == 40) {
			damageAround(RANGE, 18.0F);
			this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5F, 0.75F);
		}

		// Phase 3 (ticks 46-75): Blazing pillars & final supernova detonation
		if (this.tickCount == 58) {
			damageAround(RANGE + 0.5D, 24.0F);
			this.level().playSound(null, this.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.8F, 0.65F);
		}
		if (this.tickCount >= 75) {
			this.discard();
		}
	}

	private void spawnUltParticles() {
		int t = this.tickCount;
		if (t <= 25) {
			// Rising golden-crimson ember vortex
			double progress = t / 25.0D;
			double ringR = 2.0D + progress * 6.5D;
			for (int i = 0; i < 8; i++) {
				double angle = t * 0.25D + i * (Math.PI * 0.25D);
				double px = this.getX() + Math.cos(angle) * ringR;
				double pz = this.getZ() + Math.sin(angle) * ringR;
				this.level().addParticle(i % 2 == 0 ? ParticleTypes.FLAME : ParticleTypes.SOUL_FIRE_FLAME,
						px, this.getY() + 0.25D + this.random.nextDouble() * 1.5D, pz,
						-Math.cos(angle) * 0.08D, 0.16D + this.random.nextDouble() * 0.1D, -Math.sin(angle) * 0.08D);
				if (i % 2 == 0) {
					this.level().addParticle(i % 4 == 0 ? SOLAR_GOLD : CRIMSON_FLARE,
							px, this.getY() + 0.4D + this.random.nextDouble() * 3.0D, pz,
							0.0D, 0.12D, 0.0D);
				}
			}
		} else if (t <= 45) {
			// Giant blade plunge impact rings
			double ringProgress = ((t - 26) % 12) / 12.0D;
			double radius = 1.5D + ringProgress * 8.0D;
			for (int i = 0; i < 14; i++) {
				double angle = (Math.PI * 2.0D * i) / 14.0D + t * 0.15D;
				double px = this.getX() + Math.cos(angle) * radius;
				double pz = this.getZ() + Math.sin(angle) * radius;
				this.level().addParticle(ParticleTypes.FLAME, px, this.getY() + 0.4D, pz,
						Math.cos(angle) * 0.22D, 0.12D, Math.sin(angle) * 0.22D);
				if (i % 3 == 0) {
					this.level().addParticle(ParticleTypes.LAVA, px, this.getY() + 0.5D, pz, 0.0D, 0.15D, 0.0D);
				}
			}
			if (t == 28 || t == 40) {
				for (int i = 0; i < 6; i++) {
					this.level().addParticle(ParticleTypes.EXPLOSION,
							this.getX() + (this.random.nextDouble() - 0.5D) * 5.0D,
							this.getY() + 0.5D + this.random.nextDouble() * 3.0D,
							this.getZ() + (this.random.nextDouble() - 0.5D) * 5.0D,
							0.0D, 0.0D, 0.0D);
				}
			}
		} else if (t <= 72) {
			// Blazing pillars & supernova detonation storm
			for (int i = 0; i < 8; i++) {
				double angle = (Math.PI * 2.0D * i) / 8.0D + t * 0.04D;
				double pr = 5.5D;
				double px = this.getX() + Math.cos(angle) * pr;
				double pz = this.getZ() + Math.sin(angle) * pr;
				this.level().addParticle(ParticleTypes.FLAME,
						px, this.getY() + this.random.nextDouble() * 5.5D, pz,
						0.0D, 0.24D, 0.0D);
				if (i % 2 == 0) {
					this.level().addParticle(SOLAR_GOLD,
							px, this.getY() + 0.5D + this.random.nextDouble() * 6.0D, pz,
							0.0D, 0.15D, 0.0D);
				}
			}
			if (t >= 56 && t <= 66) {
				for (int i = 0; i < 4; i++) {
					this.level().addParticle(ParticleTypes.EXPLOSION,
							this.getX() + (this.random.nextDouble() - 0.5D) * 8.5D,
							this.getY() + 0.5D + this.random.nextDouble() * 4.5D,
							this.getZ() + (this.random.nextDouble() - 0.5D) * 8.5D,
							0.0D, 0.0D, 0.0D);
				}
				for (int i = 0; i < 10; i++) {
					double a = this.random.nextDouble() * Math.PI * 2.0D;
					this.level().addParticle(ParticleTypes.LAVA,
							this.getX() + Math.cos(a) * 2.0D, this.getY() + 0.8D, this.getZ() + Math.sin(a) * 2.0D,
							Math.cos(a) * 0.35D, 0.25D + this.random.nextDouble() * 0.2D, Math.sin(a) * 0.35D);
				}
			}
		}
	}
}
