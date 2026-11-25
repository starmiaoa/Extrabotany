package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import io.github.lounode.extrabotany.common.handler.DamageHandler;

import java.util.Comparator;
import java.util.UUID;

public class SubspaceEntity extends Entity {
	public static final int TYPE_DOMAIN = 0;
	public static final int TYPE_SPEAR = 1;

	private static final EntityDataAccessor<Float> DATA_SIZE = SynchedEntityData.defineId(SubspaceEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_ROTATION = SynchedEntityData.defineId(SubspaceEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> DATA_LIVE_TICKS = SynchedEntityData.defineId(SubspaceEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_DELAY = SynchedEntityData.defineId(SubspaceEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_TYPE = SynchedEntityData.defineId(SubspaceEntity.class, EntityDataSerializers.INT);

	private static final DustParticleOptions GOLD_DUST = new DustParticleOptions(new Vector3f(1.0F, 0.835F, 0.31F), 1.0F);
	private static final DustParticleOptions VIOLET_DUST = new DustParticleOptions(new Vector3f(0.659F, 0.333F, 0.969F), 1.05F);

	private static final String TAG_OWNER = "Owner";
	private static final String TAG_LIVE_TICKS = "LiveTicks";
	private static final String TAG_DELAY = "Delay";
	private static final String TAG_INTERVAL = "Interval";
	private static final String TAG_COUNT = "Count";
	private static final String TAG_TYPE = "Type";
	private static final String TAG_SIZE = "Size";
	private static final String TAG_ROTATION = "Rotation";
	private static final String TAG_MAX_SHOTS = "MaxShots";
	private static final String TAG_SPEAR_DAMAGE = "SpearDamage";
	private static final String TAG_SPEAR_SPEED = "SpearSpeed";
	private static final String TAG_HAS_AIM = "HasAim";
	private static final String TAG_AIM_X = "AimX";
	private static final String TAG_AIM_Y = "AimY";
	private static final String TAG_AIM_Z = "AimZ";

	@Nullable
	private UUID ownerUUID;
	@Nullable
	private Entity cachedOwner;
	private int liveTicks = 24;
	private int delay;
	private int interval = 10;
	private int count;
	private int maxShots = 5;
	private int type;
	private float size = 1.0F;
	private float rotation;
	private float spearDamage = 12F;
	private double spearSpeed = -1D;
	private boolean hasCustomAim;
	private double aimX;
	private double aimY;
	private double aimZ;

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
		this.entityData.set(DATA_TYPE, type);
		this.entityData.set(DATA_LIVE_TICKS, liveTicks);
		this.entityData.set(DATA_DELAY, delay);
		this.entityData.set(DATA_SIZE, size);
		this.entityData.set(DATA_ROTATION, rotation);
	}

	public void setAimDirection(Vec3 direction) {
		if (direction.lengthSqr() > 1.0E-6D) {
			Vec3 norm = direction.normalize();
			this.hasCustomAim = true;
			this.aimX = norm.x;
			this.aimY = norm.y;
			this.aimZ = norm.z;
		}
	}

	public void setSpearDamage(float damage) {
		this.spearDamage = Math.max(1.0F, damage);
	}

	public void setSpearSpeed(double speed) {
		this.spearSpeed = Math.max(0.3D, speed);
	}

	public void setMaxShots(int maxShots) {
		this.maxShots = Math.max(1, maxShots);
	}

	public float getSize() {
		return this.entityData.get(DATA_SIZE);
	}

	public float getRotation() {
		return this.entityData.get(DATA_ROTATION);
	}

	public int getLiveTicks() {
		return this.entityData.get(DATA_LIVE_TICKS);
	}

	public int getDelay() {
		return this.entityData.get(DATA_DELAY);
	}

	public int getSubspaceType() {
		return this.entityData.get(DATA_TYPE);
	}

	public float getOpenScale(float partialTick) {
		int delayTicks = getDelay();
		int duration = Math.max(12, getLiveTicks());
		float activeAge = (this.tickCount + partialTick) - delayTicks;
		if (activeAge <= 0.0F || activeAge >= duration) {
			return 0.0F;
		}
		if (activeAge < 6.0F) {
			float t = activeAge / 6.0F;
			return t * t * (3.0F - 2.0F * t);
		}
		if (activeAge > duration - 6.0F) {
			float t = Mth.clamp((duration - activeAge) / 6.0F, 0.0F, 1.0F);
			return t * t * (3.0F - 2.0F * t);
		}
		return 1.0F + 0.06F * Mth.sin(activeAge * 0.35F);
	}

	@Override
	public void tick() {
		this.setDeltaMovement(Vec3.ZERO);
		super.tick();

		if (this.level().isClientSide()) {
			spawnParticles();
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
				Vec3 dir = this.hasCustomAim ? new Vec3(this.aimX, this.aimY, this.aimZ) : owner.getLookAngle();
				double spd = this.spearSpeed > 0D ? this.spearSpeed : 2.45D;
				spawnSpear(owner, dir, this.spearDamage, spd, 100);
				this.count++;
			}
		} else if (this.type == TYPE_DOMAIN) {
			if (this.tickCount % this.interval == 0 && this.count < this.maxShots && this.tickCount > this.delay + 5
					&& this.tickCount < this.delay + this.liveTicks - 8) {
				Vec3 direction = findDirection(owner);
				double spd = this.spearSpeed > 0D ? this.spearSpeed : 1.45D;
				spawnSpear(owner, direction, this.spearDamage, spd, 85);
				this.count++;
			}
		}
	}

	private Vec3 findDirection(LivingEntity owner) {
		AABB area = this.getBoundingBox().inflate(24D);
		return this.level().getEntitiesOfClass(LivingEntity.class, area).stream()
				.filter(target -> DamageHandler.INSTANCE.checkPassable(target, owner))
				.min(Comparator.comparingDouble(target -> target.distanceToSqr(this)))
				.map(target -> target.position().add(0, target.getBbHeight() * 0.5D, 0).subtract(this.position()).normalize())
				.orElseGet(() -> this.hasCustomAim ? new Vec3(this.aimX, this.aimY, this.aimZ) : owner.getLookAngle());
	}

	private void spawnSpear(LivingEntity owner, Vec3 direction, float damage, double speed, int life) {
		if (direction.lengthSqr() == 0) {
			direction = owner.getLookAngle();
		}
		Vec3 dir = direction.normalize();
		SubspaceSpearEntity spear = new SubspaceSpearEntity(this.level(), owner, damage, life);
		spear.setPos(this.getX(), this.getY() - 0.75D, this.getZ());
		spear.shoot(dir.x, dir.y, dir.z, (float) speed, 1.0F);
		Vec3 motion = spear.getDeltaMovement();
		Vec3 aim = motion.lengthSqr() > 1.0E-6D ? motion : dir;
		double horiz = Math.sqrt(aim.x * aim.x + aim.z * aim.z);
		float yaw = (float) (Mth.atan2(aim.x, aim.z) * Mth.RAD_TO_DEG);
		float pitch = (float) (Mth.atan2(aim.y, horiz) * Mth.RAD_TO_DEG);
		spear.setYRot(yaw);
		spear.setXRot(pitch);
		spear.yRotO = yaw;
		spear.xRotO = pitch;
		this.level().addFreshEntity(spear);

		double cx = this.getX();
		double cy = this.getY() - 0.5D;
		double cz = this.getZ();
		if (this.level() instanceof ServerLevel serverLevel) {
			double spread = Math.max(0.25D, this.size * 0.45D);
			serverLevel.sendParticles(GOLD_DUST, cx, cy, cz, 10, spread, spread, spread, 0.04D);
			serverLevel.sendParticles(VIOLET_DUST, cx, cy, cz, 10, spread, spread, spread, 0.04D);
			serverLevel.sendParticles(ParticleTypes.END_ROD, cx, cy, cz, 6, spread * 0.6D, spread * 0.6D, spread * 0.6D, 0.12D);
			serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, cx, cy, cz, 8, spread * 0.7D, spread * 0.7D, spread * 0.7D, 0.08D);
		}
		SoundSource source = owner instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
		this.level().playSound(null, cx, cy, cz, SoundEvents.AMETHYST_BLOCK_CHIME, source,
				0.55F, 1.45F + this.random.nextFloat() * 0.35F);
		this.level().playSound(null, cx, cy, cz, SoundEvents.ENDERMAN_TELEPORT, source,
				0.22F, 1.75F + this.random.nextFloat() * 0.25F);
	}

	private void spawnParticles() {
		double cx = this.getX();
		double cy = this.getY() - 0.5D;
		double cz = this.getZ();
		float openScale = getOpenScale(0.0F);
		if (openScale <= 0.02F) {
			int delayTicks = getDelay();
			if (this.tickCount >= delayTicks - 3 && this.tickCount < delayTicks) {
				this.level().addParticle(GOLD_DUST,
						cx + (this.random.nextDouble() - 0.5D) * 0.25D,
						cy + (this.random.nextDouble() - 0.5D) * 0.25D,
						cz + (this.random.nextDouble() - 0.5D) * 0.25D,
						0.0D, 0.0D, 0.0D);
			}
			return;
		}

		double radius = Math.max(0.35F, getSize()) * openScale * 0.95D;
		double yawRad = this.getYRot() * Mth.DEG_TO_RAD;
		double rightX = Math.cos(yawRad);
		double rightZ = Math.sin(yawRad);
		double baseAngle = this.tickCount * 0.34D;

		for (int i = 0; i < 2; i++) {
			double goldAngle = baseAngle + i * Math.PI;
			double gx = cx + rightX * Math.cos(goldAngle) * radius;
			double gy = cy + Math.sin(goldAngle) * radius;
			double gz = cz + rightZ * Math.cos(goldAngle) * radius;
			this.level().addParticle(GOLD_DUST, gx, gy, gz, 0.0D, 0.01D, 0.0D);

			double violetAngle = -baseAngle * 1.15D + (i + 0.5D) * Math.PI;
			double vr = radius * 0.86D;
			double vx = cx + rightX * Math.cos(violetAngle) * vr;
			double vy = cy + Math.sin(violetAngle) * vr;
			double vz = cz + rightZ * Math.cos(violetAngle) * vr;
			this.level().addParticle(VIOLET_DUST, vx, vy, vz, 0.0D, 0.01D, 0.0D);
		}

		if (this.tickCount % 3 == 0) {
			double sparkAngle = this.random.nextDouble() * Mth.TWO_PI;
			double sr = radius * (0.4D + this.random.nextDouble() * 0.6D);
			this.level().addParticle(ParticleTypes.END_ROD,
					cx + rightX * Math.cos(sparkAngle) * sr,
					cy + Math.sin(sparkAngle) * sr,
					cz + rightZ * Math.cos(sparkAngle) * sr,
					(this.random.nextDouble() - 0.5D) * 0.03D,
					(this.random.nextDouble() - 0.5D) * 0.03D,
					(this.random.nextDouble() - 0.5D) * 0.03D);
		}

		if (this.tickCount % 2 == 0) {
			double portalAngle = this.random.nextDouble() * Mth.TWO_PI;
			double pr = radius * (0.65D + this.random.nextDouble() * 0.35D);
			this.level().addParticle(ParticleTypes.REVERSE_PORTAL,
					cx + rightX * Math.cos(portalAngle) * pr,
					cy + Math.sin(portalAngle) * pr,
					cz + rightZ * Math.cos(portalAngle) * pr,
					(this.random.nextDouble() - 0.5D) * 0.04D,
					(this.random.nextDouble() - 0.5D) * 0.04D,
					(this.random.nextDouble() - 0.5D) * 0.04D);
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
	protected void defineSynchedData() {
		this.entityData.define(DATA_SIZE, 1.0F);
		this.entityData.define(DATA_ROTATION, 0.0F);
		this.entityData.define(DATA_LIVE_TICKS, 24);
		this.entityData.define(DATA_DELAY, 0);
		this.entityData.define(DATA_TYPE, TYPE_DOMAIN);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		if (tag.hasUUID(TAG_OWNER)) {
			this.ownerUUID = tag.getUUID(TAG_OWNER);
			this.cachedOwner = null;
		}
		this.liveTicks = tag.getInt(TAG_LIVE_TICKS);
		this.delay = tag.getInt(TAG_DELAY);
		this.interval = Math.max(1, tag.getInt(TAG_INTERVAL));
		this.count = tag.getInt(TAG_COUNT);
		this.maxShots = tag.contains(TAG_MAX_SHOTS) ? Math.max(1, tag.getInt(TAG_MAX_SHOTS)) : 5;
		this.type = tag.getInt(TAG_TYPE);
		this.size = tag.getFloat(TAG_SIZE);
		this.rotation = tag.getFloat(TAG_ROTATION);
		this.spearDamage = tag.contains(TAG_SPEAR_DAMAGE) ? tag.getFloat(TAG_SPEAR_DAMAGE) : 12F;
		this.spearSpeed = tag.contains(TAG_SPEAR_SPEED) ? tag.getDouble(TAG_SPEAR_SPEED) : -1D;
		this.hasCustomAim = tag.getBoolean(TAG_HAS_AIM);
		this.aimX = tag.getDouble(TAG_AIM_X);
		this.aimY = tag.getDouble(TAG_AIM_Y);
		this.aimZ = tag.getDouble(TAG_AIM_Z);
		this.entityData.set(DATA_LIVE_TICKS, this.liveTicks);
		this.entityData.set(DATA_DELAY, this.delay);
		this.entityData.set(DATA_TYPE, this.type);
		this.entityData.set(DATA_SIZE, this.size);
		this.entityData.set(DATA_ROTATION, this.rotation);
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
		tag.putInt(TAG_MAX_SHOTS, this.maxShots);
		tag.putInt(TAG_TYPE, this.type);
		tag.putFloat(TAG_SIZE, this.size);
		tag.putFloat(TAG_ROTATION, this.rotation);
		tag.putFloat(TAG_SPEAR_DAMAGE, this.spearDamage);
		tag.putDouble(TAG_SPEAR_SPEED, this.spearSpeed);
		tag.putBoolean(TAG_HAS_AIM, this.hasCustomAim);
		tag.putDouble(TAG_AIM_X, this.aimX);
		tag.putDouble(TAG_AIM_Y, this.aimY);
		tag.putDouble(TAG_AIM_Z, this.aimZ);
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
