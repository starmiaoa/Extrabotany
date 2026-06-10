package io.github.lounode.extrabotany.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.handler.DamageHandler;

import java.util.List;
import java.util.UUID;

public abstract class FlamescionAreaEntity extends Entity {
	private static final String TAG_OWNER = "Owner";

	@Nullable
	private UUID ownerUUID;
	@Nullable
	private Entity cachedOwner;

	protected FlamescionAreaEntity(EntityType<?> type, Level level) {
		super(type, level);
	}

	protected FlamescionAreaEntity(EntityType<?> type, Level level, LivingEntity owner) {
		this(type, level);
		setOwner(owner);
	}

	public void setOwner(@Nullable Entity owner) {
		if (owner != null) {
			this.ownerUUID = owner.getUUID();
			this.cachedOwner = owner;
		}
	}

	@Nullable
	public Entity getOwnerEntity() {
		if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
			return this.cachedOwner;
		}
		if (this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
			this.cachedOwner = serverLevel.getEntity(this.ownerUUID);
			return this.cachedOwner;
		}
		return null;
	}

	protected List<LivingEntity> getTargets(double range) {
		AABB area = this.getBoundingBox().inflate(range);
		return DamageHandler.INSTANCE.getFilteredEntities(this.level().getEntitiesOfClass(LivingEntity.class, area), getOwnerEntity());
	}

	protected void slowEnemies(double range) {
		for (LivingEntity target : getTargets(range)) {
			if (target instanceof Mob mob && mob instanceof Enemy) {
				target.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 2, 3));
			}
		}
	}

	protected void damageAround(double range, float damage) {
		Entity owner = getOwnerEntity();
		for (LivingEntity target : getTargets(range)) {
			if (target == owner) {
				continue;
			}
			target.invulnerableTime = 0;
			target.hurt(flameSource(owner), damage);
		}
	}

	protected DamageSource flameSource(@Nullable Entity owner) {
		if (owner != null) {
			return this.damageSources().indirectMagic(this, owner);
		}
		return this.damageSources().magic();
	}

	@Override
	public void tick() {
		this.setDeltaMovement(Vec3.ZERO);
		super.tick();
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		if (tag.hasUUID(TAG_OWNER)) {
			this.ownerUUID = tag.getUUID(TAG_OWNER);
			this.cachedOwner = null;
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		if (this.ownerUUID != null) {
			tag.putUUID(TAG_OWNER, this.ownerUUID);
		}
	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean isPushedByFluid() {
		return false;
	}
}
