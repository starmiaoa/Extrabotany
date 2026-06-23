package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.handler.DamageHandler;

import java.util.List;

public abstract class OldSwordProjectileEntity extends ThrowableItemProjectile {
	private static final String TAG_TARGET_X = "TargetX";
	private static final String TAG_TARGET_Y = "TargetY";
	private static final String TAG_TARGET_Z = "TargetZ";
	private static final String TAG_ATTACK_BONUS = "AttackBonus";

	// 遗物剑无外部加成时的攻击力(玩家基础 1 + 钻石剑 8 = 9);超出此值的部分视为外部增伤
	// (神化词缀 / 攻击力饰品 / 力量药水等),按 1:1 叠加到投射物伤害,让“射出去的东西”也吃到增伤。
	private static final double RELIC_BASE_ATTACK_DAMAGE = 9.0D;

	private Vec3 targetPos = Vec3.ZERO;
	private float attackBonus = 0F;

	protected OldSwordProjectileEntity(EntityType<? extends OldSwordProjectileEntity> entityType, Level level) {
		super(entityType, level);
		this.setNoGravity(true);
	}

	protected OldSwordProjectileEntity(EntityType<? extends OldSwordProjectileEntity> entityType, Level level, LivingEntity owner) {
		super(entityType, owner, level);
		this.setNoGravity(true);
		// 只让玩家投掷时吃外部增伤;怪物/赫尔谢尔投掷保持原固定伤害,避免误增强 Boss。
		this.attackBonus = owner instanceof Player
				? (float) Math.max(0D, owner.getAttributeValue(Attributes.ATTACK_DAMAGE) - RELIC_BASE_ATTACK_DAMAGE)
				: 0F;
	}

	@Override
	public void tick() {
		super.tick();
		this.setNoGravity(true);

		if (this.level().isClientSide()) {
			spawnTrailParticles();
			return;
		}

		if (this.getOwner() instanceof LivingEntity owner && !owner.isAlive()) {
			this.discard();
			return;
		}

		if (this.tickCount >= getLifetime()) {
			this.discard();
			return;
		}

		if (this.tickCount >= getHitStartTick()) {
			hitEntities();
		}
	}

	protected void shootAt(Vec3 target, double speed) {
		this.setTargetPos(target);
		Vec3 delta = target.subtract(this.position()).normalize().scale(speed);
		this.setDeltaMovement(delta);

		double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
		this.setXRot((float) (Mth.atan2(delta.y, horizontal) * Mth.RAD_TO_DEG));
		this.setYRot((float) (Mth.atan2(delta.x, delta.z) * Mth.RAD_TO_DEG));
	}

	protected void hitEntities() {
		AABB axis = new AABB(this.getX(), this.getY(), this.getZ(), this.xOld, this.yOld, this.zOld).inflate(getHitRadius());
		List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, axis);
		List<LivingEntity> filtered = DamageHandler.INSTANCE.getFilteredEntities(targets, this.getOwner());

		for (LivingEntity target : filtered) {
			if (!canHitTarget(target)) {
				continue;
			}
			if (target.hurtTime > 0 && shouldRespectInvulnerability()) {
				continue;
			}

			if (damageTarget(target)) {
				onDamagedTarget(target);
				if (discardOnHit()) {
					this.discard();
				}
				break;
			}
		}
	}

	protected boolean damageTarget(LivingEntity target) {
		return target.hurt(makeDamageSource(), getEffectiveDamage());
	}

	/** 投射物实际伤害 = 固定基础伤害 + 发射瞬间记录的外部增伤(神化等模组给玩家的攻击力加成)。 */
	protected float getEffectiveDamage() {
		return getDamage() + this.attackBonus;
	}

	public float getAttackBonus() {
		return this.attackBonus;
	}

	protected DamageSource makeDamageSource() {
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

	protected void setTargetPos(Vec3 targetPos) {
		this.targetPos = targetPos;
	}

	protected Vec3 getTargetPos() {
		return this.targetPos;
	}

	protected BlockPos getTargetBlockPos() {
		return BlockPos.containing(this.targetPos);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putDouble(TAG_TARGET_X, targetPos.x);
		tag.putDouble(TAG_TARGET_Y, targetPos.y);
		tag.putDouble(TAG_TARGET_Z, targetPos.z);
		tag.putFloat(TAG_ATTACK_BONUS, attackBonus);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.targetPos = new Vec3(tag.getDouble(TAG_TARGET_X), tag.getDouble(TAG_TARGET_Y), tag.getDouble(TAG_TARGET_Z));
		this.attackBonus = tag.getFloat(TAG_ATTACK_BONUS);
	}

	protected int getHitStartTick() {
		return 0;
	}

	protected double getHitRadius() {
		return 2.0D;
	}

	protected boolean shouldRespectInvulnerability() {
		return true;
	}

	protected boolean discardOnHit() {
		return true;
	}

	protected boolean canHitTarget(LivingEntity target) {
		return true;
	}

	protected void onDamagedTarget(LivingEntity target) {}

	protected void spawnTrailParticles() {}

	protected abstract int getLifetime();

	protected abstract float getDamage();
}
