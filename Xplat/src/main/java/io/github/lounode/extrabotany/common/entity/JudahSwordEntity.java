package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.handler.DamageHandler;

import java.util.List;

public class JudahSwordEntity extends Entity {
	private static final String TAG_START_X = "StartX";
	private static final String TAG_START_Y = "StartY";
	private static final String TAG_START_Z = "StartZ";
	private static final String TAG_END_X = "EndX";
	private static final String TAG_END_Y = "EndY";
	private static final String TAG_END_Z = "EndZ";
	private static final String TAG_DAMAGE = "Damage";

	private Entity owner;
	private Vec3 start = Vec3.ZERO;
	private Vec3 end = Vec3.ZERO;
	private float damage = 6F;

	public JudahSwordEntity(EntityType<? extends JudahSwordEntity> type, Level level) {
		super(type, level);
	}

	public JudahSwordEntity(Level level, Entity owner, Vec3 start, Vec3 end, float damage) {
		super(ExtraBotanyEntityType.JUDAH_SWORD, level);
		this.owner = owner;
		this.start = start;
		this.end = end;
		this.damage = damage;
	}

	@Override
	public void tick() {
		Vec3 direction = this.end.subtract(this.start);
		if (direction.lengthSqr() > 0) {
			Vec3 motion = direction.normalize().scale(0.75D);
			this.setPos(this.getX() + motion.x, this.getY() + motion.y, this.getZ() + motion.z);
		}
		this.level().addParticle(ParticleTypes.ENCHANT, this.getX(), this.getY(), this.getZ(), 0.02D, 0.014D, 0.014D);

		if (!this.level().isClientSide()) {
			hitNearby();
			if (this.position().distanceToSqr(this.end) <= 2D || this.tickCount > 80) {
				hitLine();
				this.discard();
			}
		}
		super.tick();
	}

	private void hitNearby() {
		List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.75D));
		for (LivingEntity target : DamageHandler.INSTANCE.getFilteredEntities(targets, this.owner)) {
			if (target instanceof Player) {
				continue;
			}
			target.hurt(makeDamageSource(), this.damage * 0.4F);
			target.setSecondsOnFire(5);
		}
	}

	private void hitLine() {
		AABB area = new AABB(this.start, this.end).inflate(1.8D);
		List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area);
		for (LivingEntity target : DamageHandler.INSTANCE.getFilteredEntities(targets, this.owner)) {
			if (target instanceof Player) {
				continue;
			}
			target.hurt(makeDamageSource(), this.damage * 0.9F);
			dealTrueDamage(target, this.damage * 0.3F);
			target.setSecondsOnFire(5);
		}
	}

	private void dealTrueDamage(LivingEntity target, float amount) {
		if (!target.isAlive() || amount <= 0F) {
			return;
		}
		float health = target.getHealth();
		if (health <= amount) {
			target.hurt(this.damageSources().magic(), Float.MAX_VALUE);
		} else {
			target.setHealth(Math.max(1F, health - amount));
			target.hurt(this.damageSources().magic(), 0.01F);
		}
	}

	private DamageSource makeDamageSource() {
		if (this.owner instanceof Player player) {
			return this.damageSources().playerAttack(player);
		}
		if (this.owner instanceof LivingEntity living) {
			return this.damageSources().mobAttack(living);
		}
		return this.damageSources().magic();
	}

	@Override
	protected void defineSynchedData() {}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		this.start = new Vec3(tag.getDouble(TAG_START_X), tag.getDouble(TAG_START_Y), tag.getDouble(TAG_START_Z));
		this.end = new Vec3(tag.getDouble(TAG_END_X), tag.getDouble(TAG_END_Y), tag.getDouble(TAG_END_Z));
		this.damage = tag.getFloat(TAG_DAMAGE);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putDouble(TAG_START_X, this.start.x);
		tag.putDouble(TAG_START_Y, this.start.y);
		tag.putDouble(TAG_START_Z, this.start.z);
		tag.putDouble(TAG_END_X, this.end.x);
		tag.putDouble(TAG_END_Y, this.end.y);
		tag.putDouble(TAG_END_Z, this.end.z);
		tag.putFloat(TAG_DAMAGE, this.damage);
	}
}
