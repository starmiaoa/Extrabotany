package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

import org.joml.Vector3f;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.util.HerrscherCombatHelper;

import java.util.List;

public class SubspaceSpearEntity extends ThrowableItemProjectile {
	private static final DustParticleOptions GOLD_DUST = new DustParticleOptions(new Vector3f(1.0F, 0.84F, 0.30F), 1.0F);
	private static final DustParticleOptions VIOLET_DUST = new DustParticleOptions(new Vector3f(0.68F, 0.25F, 0.98F), 1.05F);

	private static final String TAG_DAMAGE = "Damage";
	private static final String TAG_LIFE = "Life";
	private static final double HIT_XZ = 1D;
	private static final double HIT_Y = 0.45D;

	private float damage = 12F;
	private int life = 100;

	public SubspaceSpearEntity(EntityType<? extends SubspaceSpearEntity> entityType, Level level) {
		super(entityType, level);
		this.setNoGravity(true);
	}

	public SubspaceSpearEntity(Level level, LivingEntity owner, float damage, int life) {
		super(ExtraBotanyEntityType.SUBSPACE_SPEAR, owner, level);
		this.damage = damage;
		this.life = life;
		this.setNoGravity(true);
	}

	@Override
	public void tick() {
		super.tick();
		this.setNoGravity(true);

		if (this.level().isClientSide()) {
			spawnFlightTrail();
			return;
		}

		hitEntities();
		if (this.tickCount > this.life || !(this.getOwner() instanceof LivingEntity owner) || !owner.isAlive()) {
			this.discard();
		}
	}

	private void spawnFlightTrail() {
		int steps = 4;
		for (int i = 0; i < steps; i++) {
			double t = i / (double) steps;
			double px = Mth.lerp(t, this.xOld, this.getX());
			double py = Mth.lerp(t, this.yOld, this.getY()) + 0.25D;
			double pz = Mth.lerp(t, this.zOld, this.getZ());

			double jx = (this.random.nextDouble() - 0.5D) * 0.10D;
			double jy = (this.random.nextDouble() - 0.5D) * 0.10D;
			double jz = (this.random.nextDouble() - 0.5D) * 0.10D;

			DustParticleOptions dust = (i % 2 == 0) ? GOLD_DUST : VIOLET_DUST;
			this.level().addParticle(dust, px + jx, py + jy, pz + jz, 0.0D, 0.0D, 0.0D);

			if (i == 0 || i == 2) {
				this.level().addParticle(ParticleTypes.END_ROD, px, py, pz,
						(this.random.nextDouble() - 0.5D) * 0.025D,
						(this.random.nextDouble() - 0.5D) * 0.025D,
						(this.random.nextDouble() - 0.5D) * 0.025D);
			}
		}
	}

	private void hitEntities() {
		Entity owner = this.getOwner();
		AABB axis = new AABB(this.getX(), this.getY(), this.getZ(), this.xOld, this.yOld, this.zOld).inflate(HIT_XZ, HIT_Y, HIT_XZ);
		List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, axis);
		for (LivingEntity target : DamageHandler.INSTANCE.getFilteredEntities(targets, owner)) {
			if (target.hurtTime > 0) {
				continue;
			}
			HerrscherCombatHelper.dealTrueMagicDamage(target, owner, this.damage * 0.4F);
			target.hurt(makeDamageSource(), this.damage * 1.5F);
			emitImpactBurst(target);
		}
	}

	private void emitImpactBurst(LivingEntity target) {
		if (!(this.level() instanceof ServerLevel serverLevel)) {
			return;
		}
		double hx = target.getX();
		double hy = target.getY() + target.getBbHeight() * 0.5D;
		double hz = target.getZ();
		serverLevel.sendParticles(ParticleTypes.END_ROD, hx, hy, hz, 12, 0.35D, 0.35D, 0.35D, 0.14D);
		serverLevel.sendParticles(ParticleTypes.WITCH, hx, hy, hz, 10, 0.35D, 0.35D, 0.35D, 0.08D);
		serverLevel.sendParticles(GOLD_DUST, hx, hy, hz, 14, 0.45D, 0.45D, 0.45D, 0.05D);
		serverLevel.sendParticles(VIOLET_DUST, hx, hy, hz, 10, 0.45D, 0.45D, 0.45D, 0.05D);
		SoundSource source = this.getOwner() instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
		this.level().playSound(null, hx, hy, hz, SoundEvents.AMETHYST_CLUSTER_BREAK, source,
				0.45F, 1.45F + this.random.nextFloat() * 0.30F);
	}

	private DamageSource makeDamageSource() {
		Entity owner = this.getOwner();
		if (owner instanceof Player player) {
			return this.damageSources().playerAttack(player);
		}
		if (owner instanceof LivingEntity living) {
			return this.damageSources().mobAttack(living);
		}
		return this.damageSources().generic();
	}

	@Override
	protected void onHit(HitResult result) {
	}

	@Override
	protected Item getDefaultItem() {
		return ExtraBotanyItems.spearOfSubspace;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putFloat(TAG_DAMAGE, this.damage);
		tag.putInt(TAG_LIFE, this.life);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.damage = tag.getFloat(TAG_DAMAGE);
		this.life = tag.getInt(TAG_LIFE);
	}
}
