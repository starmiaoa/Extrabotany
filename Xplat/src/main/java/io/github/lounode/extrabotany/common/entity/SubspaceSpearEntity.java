package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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

import java.util.List;

public class SubspaceSpearEntity extends ThrowableItemProjectile {
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
			this.level().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
			return;
		}

		hitEntities();
		if (this.tickCount > this.life || !(this.getOwner() instanceof LivingEntity owner) || !owner.isAlive()) {
			this.discard();
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
			dealTrueDamage(target, this.damage * 0.4F);
			target.hurt(makeDamageSource(), this.damage * 1.5F);
		}
	}

	private void dealTrueDamage(LivingEntity target, float amount) {
		if (!target.isAlive() || amount <= 0F) {
			return;
		}
		float health = target.getHealth();
		if (health <= amount) {
			target.hurt(this.damageSources().indirectMagic(this, this.getOwner()), Float.MAX_VALUE);
		} else {
			target.setHealth(Math.max(1F, health - amount));
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
