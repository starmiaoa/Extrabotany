package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
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

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PhotonShotgunProjectileEntity extends ThrowableItemProjectile {
	private static final int LIFETIME = 10;
	private static final float DAMAGE = 5F;
	private static final float TRUE_DAMAGE = 0.5F;
	private static final double HIT_RADIUS = 0.7D;

	private final Set<UUID> damaged = new HashSet<>();

	public PhotonShotgunProjectileEntity(EntityType<? extends PhotonShotgunProjectileEntity> entityType, Level level) {
		super(entityType, level);
		this.setNoGravity(true);
	}

	public PhotonShotgunProjectileEntity(Level level, LivingEntity owner) {
		super(ExtraBotanyEntityType.PHOTON_SHOTGUN_PROJECTILE, owner, level);
		this.setNoGravity(true);
	}

	@Override
	public void tick() {
		super.tick();
		this.setNoGravity(true);

		if (this.level().isClientSide()) {
			this.level().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
			return;
		}

		hitEntities();
		if (this.tickCount > LIFETIME) {
			this.discard();
		}
	}

	private void hitEntities() {
		Entity owner = this.getOwner();
		AABB axis = new AABB(this.getX(), this.getY(), this.getZ(), this.xOld, this.yOld, this.zOld).inflate(HIT_RADIUS);
		List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, axis);
		for (LivingEntity target : DamageHandler.INSTANCE.getFilteredEntities(targets, owner)) {
			if (target instanceof Player || this.damaged.contains(target.getUUID())) {
				continue;
			}
			if (target.hurt(makeDamageSource(), DAMAGE)) {
				dealTrueDamage(target);
				this.damaged.add(target.getUUID());
			}
		}
	}

	private void dealTrueDamage(LivingEntity target) {
		if (!target.isAlive() || TRUE_DAMAGE <= 0F) {
			return;
		}

		float health = target.getHealth();
		if (health <= TRUE_DAMAGE) {
			target.hurt(this.damageSources().indirectMagic(this, this.getOwner()), Float.MAX_VALUE);
		} else {
			target.setHealth(Math.max(1F, health - TRUE_DAMAGE));
			target.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 0.01F);
		}
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
		if (!this.level().isClientSide() && result.getType() == HitResult.Type.BLOCK) {
			this.discard();
		}
	}

	@Override
	protected Item getDefaultItem() {
		return ExtraBotanyItems.photonShotgun;
	}
}
