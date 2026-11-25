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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import io.github.lounode.extrabotany.common.entity.gaia.VoidHerrscher;
import io.github.lounode.extrabotany.common.util.HerrscherCombatHelper;

import java.util.UUID;

public class HerrscherVoidFieldEntity extends Entity {
	public static final int MODE_TIME_LOCK = 0;
	public static final int MODE_COLLAPSE = 1;

	private static final DustParticleOptions GOLD_DUST = new DustParticleOptions(new Vector3f(1.0F, 0.84F, 0.30F), 1.1F);
	private static final DustParticleOptions VIOLET_DUST = new DustParticleOptions(new Vector3f(0.72F, 0.28F, 1.0F), 1.2F);
	private static final DustParticleOptions CRIMSON_DUST = new DustParticleOptions(new Vector3f(1.0F, 0.22F, 0.56F), 1.3F);

	private static final EntityDataAccessor<Integer> DATA_MODE = SynchedEntityData.defineId(HerrscherVoidFieldEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(HerrscherVoidFieldEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> DATA_MAX_LIFE = SynchedEntityData.defineId(HerrscherVoidFieldEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_DETONATED = SynchedEntityData.defineId(HerrscherVoidFieldEntity.class, EntityDataSerializers.BOOLEAN);

	private static final String TAG_LIFE = "Life";
	private static final String TAG_MAX_LIFE = "MaxLife";
	private static final String TAG_MODE = "Mode";
	private static final String TAG_RADIUS = "Radius";
	private static final String TAG_DAMAGE = "Damage";
	private static final String TAG_DETONATED = "Detonated";
	private static final String TAG_SPEAR_SPAWNED = "SpearSpawned";
	private static final String TAG_OWNER = "Owner";

	private int life = 65;
	private int maxLife = 65;
	private int mode = MODE_TIME_LOCK;
	private float radius = 2.8F;
	private float collapseDamage = 10.0F;
	private boolean detonated;
	private boolean spearSpawned;
	@Nullable
	private UUID ownerUUID;
	@Nullable
	private Entity cachedOwner;

	public HerrscherVoidFieldEntity(EntityType<? extends HerrscherVoidFieldEntity> type, Level level) {
		super(type, level);
	}

	public HerrscherVoidFieldEntity(Level level) {
		this(ExtraBotanyEntityType.VOID_FIELD, level);
	}

	public void configure(int mode, float radius, int duration, float collapseDamage, @Nullable Entity owner) {
		this.mode = mode;
		this.radius = Math.max(1.5F, radius);
		this.life = Math.max(15, duration);
		this.maxLife = this.life;
		this.collapseDamage = collapseDamage;
		this.detonated = false;
		this.spearSpawned = false;
		setOwner(owner);
		this.entityData.set(DATA_MODE, this.mode);
		this.entityData.set(DATA_RADIUS, this.radius);
		this.entityData.set(DATA_MAX_LIFE, this.maxLife);
		this.entityData.set(DATA_DETONATED, false);
	}

	public int getFieldMode() {
		return this.entityData.get(DATA_MODE);
	}

	public float getFieldRadius() {
		return this.entityData.get(DATA_RADIUS);
	}

	public int getMaxLife() {
		return Math.max(1, this.entityData.get(DATA_MAX_LIFE));
	}

	public boolean isDetonated() {
		return this.entityData.get(DATA_DETONATED);
	}

	public float getCollapseProgress(float partialTick) {
		int fuse = Math.max(10, getMaxLife() - 10);
		return Mth.clamp((this.tickCount + partialTick) / (float) fuse, 0.0F, 1.0F);
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			spawnClientParticles();
			return;
		}
		if (this.ownerUUID != null && this.tickCount % 10 == 0 && !(getOwner() instanceof LivingEntity owner && owner.isAlive())) {
			discard();
			return;
		}

		if (this.mode == MODE_TIME_LOCK) {
			tickTimeLock();
		} else {
			tickCollapse();
		}

		if (--life <= 0) {
			discard();
		}
	}

	private void tickTimeLock() {
		double r = this.radius;
		AABB box = new AABB(getX() - r, getY() - 0.5D, getZ() - r, getX() + r, getY() + 3.0D, getZ() + r);
		for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, box)) {
			if (living instanceof VoidHerrscher || living == getOwner()) {
				continue;
			}
			if (living instanceof Player player && (player.isCreative() || player.isSpectator())) {
				continue;
			}
			double dx = living.getX() - getX();
			double dz = living.getZ() - getZ();
			if (dx * dx + dz * dz > r * r) {
				continue;
			}
			Vec3 vel = living.getDeltaMovement();
			living.setDeltaMovement(vel.x * 0.48D, Math.min(vel.y, 0.12D), vel.z * 0.48D);
			living.hurtMarked = true;
			if (this.tickCount % 10 == 0) {
				living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 1, true, true));
			}
		}

		if (!this.spearSpawned && this.tickCount >= 22 && getOwner() instanceof LivingEntity owner && owner.isAlive()) {
			this.spearSpawned = true;
			SubspaceEntity portal = new SubspaceEntity(level(), owner);
			portal.configure(SubspaceEntity.TYPE_SPEAR, 24, 2, 10, 0.62F, random.nextFloat() * 360F);
			portal.setPos(getX(), getY() + 4.8D, getZ());
			portal.setAimDirection(new Vec3(0.0D, -1.0D, 0.0D));
			portal.setSpearDamage(9.0F);
			portal.setSpearSpeed(1.85D);
			level().addFreshEntity(portal);
		}
	}

	private void tickCollapse() {
		int fuse = Math.max(10, this.maxLife - 10);
		double r = this.radius;
		if (!this.detonated) {
			if (this.tickCount == 1) {
				level().playSound(null, getX(), getY(), getZ(), SoundEvents.BEACON_AMBIENT, SoundSource.HOSTILE, 0.9F, 1.6F);
			} else if (this.tickCount == fuse - 10) {
				level().playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.1F, 0.65F);
			}
			AABB box = new AABB(getX() - r, getY() - 0.5D, getZ() - r, getX() + r, getY() + 3.2D, getZ() + r);
			for (Player player : level().getEntitiesOfClass(Player.class, box, p -> p.isAlive() && !p.isCreative() && !p.isSpectator())) {
				Vec3 toCenter = new Vec3(getX() - player.getX(), 0.0D, getZ() - player.getZ());
				double dist = toCenter.length();
				if (dist > 0.35D && dist <= r) {
					Vec3 pull = toCenter.normalize().scale(0.045D);
					player.setDeltaMovement(player.getDeltaMovement().add(pull.x, 0.0D, pull.z));
					player.hurtMarked = true;
				}
			}

			if (this.tickCount >= fuse) {
				detonateCollapse();
			}
		}
	}

	private void detonateCollapse() {
		this.detonated = true;
		this.entityData.set(DATA_DETONATED, true);
		double r = this.radius;
		Entity owner = getOwner();
		AABB box = new AABB(getX() - r, getY() - 1.0D, getZ() - r, getX() + r, getY() + 3.5D, getZ() + r);
		for (Player player : level().getEntitiesOfClass(Player.class, box, p -> p.isAlive() && !p.isCreative() && !p.isSpectator())) {
			double dx = player.getX() - getX();
			double dz = player.getZ() - getZ();
			if (dx * dx + dz * dz <= r * r) {
				player.hurt(damageSources().indirectMagic(this, owner != null ? owner : this), this.collapseDamage);
				HerrscherCombatHelper.dealTrueMagicDamage(player, owner != null ? owner : this, 3.5F);
				player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
				Vec3 knock = new Vec3(dx, 0.0D, dz);
				if (knock.lengthSqr() > 1.0E-4D) {
					knock = knock.normalize().scale(0.65D);
					player.setDeltaMovement(knock.x, 0.35D, knock.z);
					player.hurtMarked = true;
				}
			}
		}

		level().playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.3F, 1.35F);
		level().playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.HOSTILE, 1.2F, 0.6F);
		if (level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.8D, getZ(), 2, 0.4D, 0.3D, 0.4D, 0.0D);
			serverLevel.sendParticles(CRIMSON_DUST, getX(), getY() + 0.6D, getZ(), 28, r * 0.45D, 0.5D, r * 0.45D, 0.05D);
			serverLevel.sendParticles(GOLD_DUST, getX(), getY() + 0.6D, getZ(), 22, r * 0.45D, 0.5D, r * 0.45D, 0.05D);
			serverLevel.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.8D, getZ(), 18, r * 0.35D, 0.6D, r * 0.35D, 0.18D);
		}
	}

	private void spawnClientParticles() {
		float r = getFieldRadius();
		int fieldMode = getFieldMode();
		if (fieldMode == MODE_TIME_LOCK) {
			for (int i = 0; i < 3; i++) {
				double angle = this.tickCount * 0.22D + i * (Mth.TWO_PI / 3.0D);
				double px = getX() + Math.cos(angle) * r;
				double pz = getZ() + Math.sin(angle) * r;
				level().addParticle(i == 0 ? GOLD_DUST : VIOLET_DUST, px, getY() + 0.15D + (this.tickCount % 16) * 0.06D, pz, 0.0D, 0.02D, 0.0D);
			}
			if (this.tickCount % 2 == 0) {
				level().addParticle(ParticleTypes.REVERSE_PORTAL,
						getX() + (random.nextDouble() - 0.5D) * r * 1.4D,
						getY() + 0.2D + random.nextDouble() * 1.6D,
						getZ() + (random.nextDouble() - 0.5D) * r * 1.4D,
						0.0D, 0.03D, 0.0D);
			}
		} else {
			if (isDetonated()) {
				return;
			}
			float prog = getCollapseProgress(0.0F);
			double ringR = Mth.lerp(prog, r, 0.35D);
			for (int i = 0; i < 4; i++) {
				double angle = this.tickCount * 0.45D + i * (Math.PI * 0.5D);
				double px = getX() + Math.cos(angle) * ringR;
				double pz = getZ() + Math.sin(angle) * ringR;
				level().addParticle(i % 2 == 0 ? CRIMSON_DUST : GOLD_DUST, px, getY() + 0.25D, pz,
						-Math.cos(angle) * 0.06D, 0.04D, -Math.sin(angle) * 0.06D);
			}
			double edgeAngle = this.tickCount * 0.65D;
			level().addParticle(VIOLET_DUST, getX() + Math.cos(edgeAngle) * r, getY() + 0.15D, getZ() + Math.sin(edgeAngle) * r, 0.0D, 0.01D, 0.0D);
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
		this.entityData.define(DATA_MODE, MODE_TIME_LOCK);
		this.entityData.define(DATA_RADIUS, 2.8F);
		this.entityData.define(DATA_MAX_LIFE, 65);
		this.entityData.define(DATA_DETONATED, false);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		this.life = tag.getInt(TAG_LIFE);
		this.maxLife = tag.contains(TAG_MAX_LIFE) ? Math.max(1, tag.getInt(TAG_MAX_LIFE)) : Math.max(1, this.life);
		this.mode = tag.getInt(TAG_MODE);
		this.radius = tag.contains(TAG_RADIUS) ? tag.getFloat(TAG_RADIUS) : 2.8F;
		this.collapseDamage = tag.contains(TAG_DAMAGE) ? tag.getFloat(TAG_DAMAGE) : 10.0F;
		this.detonated = tag.getBoolean(TAG_DETONATED);
		this.spearSpawned = tag.getBoolean(TAG_SPEAR_SPAWNED);
		if (tag.hasUUID(TAG_OWNER)) {
			this.ownerUUID = tag.getUUID(TAG_OWNER);
			this.cachedOwner = null;
		}
		this.entityData.set(DATA_MODE, this.mode);
		this.entityData.set(DATA_RADIUS, this.radius);
		this.entityData.set(DATA_MAX_LIFE, this.maxLife);
		this.entityData.set(DATA_DETONATED, this.detonated);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putInt(TAG_LIFE, life);
		tag.putInt(TAG_MAX_LIFE, maxLife);
		tag.putInt(TAG_MODE, mode);
		tag.putFloat(TAG_RADIUS, radius);
		tag.putFloat(TAG_DAMAGE, collapseDamage);
		tag.putBoolean(TAG_DETONATED, detonated);
		tag.putBoolean(TAG_SPEAR_SPAWNED, spearSpawned);
		if (this.ownerUUID != null) {
			tag.putUUID(TAG_OWNER, this.ownerUUID);
		}
	}
}
