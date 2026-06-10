package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.handler.DamageHandler;

import java.util.Comparator;
import java.util.UUID;

public class SubspaceEntity extends Entity {
	public static final int TYPE_DOMAIN = 0;
	public static final int TYPE_SPEAR = 1;

	private static final String TAG_OWNER = "Owner";
	private static final String TAG_LIVE_TICKS = "LiveTicks";
	private static final String TAG_DELAY = "Delay";
	private static final String TAG_INTERVAL = "Interval";
	private static final String TAG_COUNT = "Count";
	private static final String TAG_TYPE = "Type";
	private static final String TAG_SIZE = "Size";
	private static final String TAG_ROTATION = "Rotation";

	@Nullable
	private UUID ownerUUID;
	@Nullable
	private Entity cachedOwner;
	private int liveTicks;
	private int delay;
	private int interval = 10;
	private int count;
	private int type;
	private float size;
	private float rotation;

	public SubspaceEntity(EntityType<? extends SubspaceEntity> type, Level level) {
		super(type, level);
	}

	public SubspaceEntity(Level level, LivingEntity owner) {
		super(ExtraBotanyEntityType.SUBSPACE, level);
		setOwner(owner);
	}

	public void configure(int type, int liveTicks, int delay, int interval, float size, float rotation) {
		this.type = type;
		this.liveTicks = liveTicks;
		this.delay = delay;
		this.interval = Math.max(1, interval);
		this.size = size;
		this.rotation = rotation;
	}

	@Override
	public void tick() {
		this.setDeltaMovement(Vec3.ZERO);
		super.tick();

		spawnParticles();

		if (this.level().isClientSide()) {
			return;
		}
		if (!(getOwner() instanceof LivingEntity owner) || !owner.isAlive()) {
			this.discard();
			return;
		}
		if (this.tickCount < this.delay) {
			return;
		}
		if (this.tickCount > this.liveTicks + this.delay) {
			this.discard();
			return;
		}

		if (this.type == TYPE_SPEAR) {
			if (this.tickCount > this.delay + 8 && this.count < 1) {
				spawnSpear(owner, owner.getLookAngle(), 12F, 2.45D, 100);
				this.count++;
			}
		} else if (this.type == TYPE_DOMAIN) {
			if (this.tickCount % this.interval == 0 && this.count < 5 && this.tickCount > this.delay + 5
					&& this.tickCount < this.liveTicks - this.delay - 10) {
				Vec3 direction = findDirection(owner);
				spawnSpear(owner, direction, 12F, 1.35D, 80);
				this.count++;
			}
		}
	}

	private Vec3 findDirection(LivingEntity owner) {
		AABB area = this.getBoundingBox().inflate(12D);
		return this.level().getEntitiesOfClass(LivingEntity.class, area).stream()
				.filter(target -> DamageHandler.INSTANCE.checkPassable(target, owner))
				.min(Comparator.comparingDouble(target -> target.distanceToSqr(this)))
				.map(target -> target.position().add(0, target.getBbHeight() * 0.5D, 0).subtract(this.position()).normalize())
				.orElseGet(owner::getLookAngle);
	}

	private void spawnSpear(LivingEntity owner, Vec3 direction, float damage, double speed, int life) {
		if (direction.lengthSqr() == 0) {
			direction = owner.getLookAngle();
		}
		SubspaceSpearEntity spear = new SubspaceSpearEntity(this.level(), owner, damage, life);
		spear.setPos(this.getX(), this.getY() - 0.75D, this.getZ());
		spear.shoot(direction.x, direction.y, direction.z, (float) speed, 1.0F);
		spear.setYRot(owner.getYRot());
		spear.setXRot(-owner.getXRot());
		this.level().addFreshEntity(spear);
	}

	private void spawnParticles() {
		float range = Math.max(0.25F, this.size);
		for (int i = 0; i < 4; i++) {
			this.level().addParticle(ParticleTypes.PORTAL,
					this.getX() + (this.random.nextDouble() - 0.5D) * range,
					this.getY() + this.random.nextDouble() * range,
					this.getZ() + (this.random.nextDouble() - 0.5D) * range,
					0, 0.02D, 0);
		}
	}

	public void setOwner(@Nullable Entity owner) {
		if (owner != null) {
			this.ownerUUID = owner.getUUID();
			this.cachedOwner = owner;
		}
	}

	@Nullable
	public Entity getOwner() {
		if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
			return this.cachedOwner;
		}
		if (this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
			this.cachedOwner = serverLevel.getEntity(this.ownerUUID);
			return this.cachedOwner;
		}
		return null;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		if (tag.hasUUID(TAG_OWNER)) {
			this.ownerUUID = tag.getUUID(TAG_OWNER);
			this.cachedOwner = null;
		}
		this.liveTicks = tag.getInt(TAG_LIVE_TICKS);
		this.delay = tag.getInt(TAG_DELAY);
		this.interval = tag.getInt(TAG_INTERVAL);
		this.count = tag.getInt(TAG_COUNT);
		this.type = tag.getInt(TAG_TYPE);
		this.size = tag.getFloat(TAG_SIZE);
		this.rotation = tag.getFloat(TAG_ROTATION);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		if (this.ownerUUID != null) {
			tag.putUUID(TAG_OWNER, this.ownerUUID);
		}
		tag.putInt(TAG_LIVE_TICKS, this.liveTicks);
		tag.putInt(TAG_DELAY, this.delay);
		tag.putInt(TAG_INTERVAL, this.interval);
		tag.putInt(TAG_COUNT, this.count);
		tag.putInt(TAG_TYPE, this.type);
		tag.putFloat(TAG_SIZE, this.size);
		tag.putFloat(TAG_ROTATION, this.rotation);
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
