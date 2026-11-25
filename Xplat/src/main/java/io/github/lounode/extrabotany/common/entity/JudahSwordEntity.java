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
import net.minecraft.world.damagesource.DamageSource;
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
import io.github.lounode.extrabotany.common.util.HerrscherCombatHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class JudahSwordEntity extends Entity {
	private static final EntityDataAccessor<Vector3f> START_POS = SynchedEntityData.defineId(JudahSwordEntity.class, EntityDataSerializers.VECTOR3);
	private static final EntityDataAccessor<Vector3f> END_POS = SynchedEntityData.defineId(JudahSwordEntity.class, EntityDataSerializers.VECTOR3);
	private static final EntityDataAccessor<Boolean> DETONATED = SynchedEntityData.defineId(JudahSwordEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DETONATE_TICKS = SynchedEntityData.defineId(JudahSwordEntity.class, EntityDataSerializers.INT);

	private static final String TAG_START_X = "StartX";
	private static final String TAG_START_Y = "StartY";
	private static final String TAG_START_Z = "StartZ";
	private static final String TAG_END_X = "EndX";
	private static final String TAG_END_Y = "EndY";
	private static final String TAG_END_Z = "EndZ";
	private static final String TAG_DAMAGE = "Damage";
	private static final String TAG_OWNER = "Owner";

	public static final int DETONATE_LINGER_TICKS = 14;
	private static final double FLIGHT_SPEED = 1.05D;

	@Nullable
	private UUID ownerUUID;
	@Nullable
	private Entity cachedOwner;
	private float damage = 8.5F;
	private final Set<UUID> pathHitIds = new HashSet<>();

	public JudahSwordEntity(EntityType<? extends JudahSwordEntity> type, Level level) {
		super(type, level);
	}

	public JudahSwordEntity(Level level, @Nullable Entity owner, Vec3 start, Vec3 end, float damage) {
		super(ExtraBotanyEntityType.JUDAH_SWORD, level);
		setOwnerEntity(owner);
		setStartPos(start);
		setEndPos(end);
		this.damage = damage;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(START_POS, new Vector3f());
		builder.define(END_POS, new Vector3f());
		builder.define(DETONATED, false);
		builder.define(DETONATE_TICKS, 0);
	}

	@Override
	public void tick() {
		super.tick();
		Vec3 start = getStartPos();
		Vec3 end = getEndPos();

		if (isDetonated()) {
			int dt = getDetonateTicks() + 1;
			if (!this.level().isClientSide()) {
				setDetonateTicks(dt);
				if (dt >= DETONATE_LINGER_TICKS) {
					this.discard();
				}
			}
			return;
		}

		Vec3 direction = end.subtract(start);
		if (direction.lengthSqr() > 1.0E-5D) {
			Vec3 step = direction.normalize().scale(FLIGHT_SPEED);
			this.setPos(this.getX() + step.x, this.getY() + step.y, this.getZ() + step.z);
		}

		if (this.level().isClientSide()) {
			DustParticleOptions pinkDust = new DustParticleOptions(new Vector3f(1.0F, 0.45F, 0.68F), 1.35F);
			this.level().addParticle(pinkDust, this.getX(), this.getY(), this.getZ(), 0, 0.03D, 0);
			if (this.tickCount % 2 == 0) {
				this.level().addParticle(ParticleTypes.CHERRY_LEAVES, this.getX(), this.getY() + 0.15D, this.getZ(),
						(this.random.nextDouble() - 0.5D) * 0.08D, 0.03D, (this.random.nextDouble() - 0.5D) * 0.08D);
			}
		}

		if (!this.level().isClientSide()) {
			hitNearby();
			if (this.position().distanceToSqr(end) <= 1.8D || this.tickCount > 36) {
				this.setPos(end.x, end.y, end.z);
				detonateLine(start, end);
			}
		}
	}

	private void hitNearby() {
		Entity owner = getOwnerEntity();
		List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(1.25D));
		for (LivingEntity target : DamageHandler.INSTANCE.getFilteredEntities(targets, owner)) {
			if (this.pathHitIds.add(target.getUUID())) {
				target.invulnerableTime = 0;
				target.hurt(makeDamageSource(), this.damage * 0.55F);
				target.igniteForSeconds(5.0F);
			}
		}
	}

	private void detonateLine(Vec3 start, Vec3 end) {
		setDetonated(true);
		setDetonateTicks(0);

		Entity owner = getOwnerEntity();
		AABB area = new AABB(start, end).inflate(2.2D);
		List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, area);
		for (LivingEntity target : DamageHandler.INSTANCE.getFilteredEntities(targets, owner)) {
			if (target.getBoundingBox().inflate(1.65D).clip(start, end).isPresent()) {
				target.invulnerableTime = 0;
				target.hurt(makeDamageSource(), this.damage * 1.25F);
				HerrscherCombatHelper.dealTrueMagicDamage(target, owner, this.damage * 0.35F);
				target.igniteForSeconds(6.0F);
			}
		}

		this.level().playSound(null, end.x, end.y, end.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.7F, 1.35F);
		if (this.level() instanceof ServerLevel sl) {
			DustParticleOptions crimson = new DustParticleOptions(new Vector3f(1.0F, 0.28F, 0.52F), 1.65F);
			int steps = 12;
			for (int i = 0; i <= steps; i++) {
				double t = (double) i / steps;
				double px = start.x + (end.x - start.x) * t;
				double py = start.y + (end.y - start.y) * t;
				double pz = start.z + (end.z - start.z) * t;
				sl.sendParticles(crimson, px, py, pz, 2, 0.25D, 0.2D, 0.25D, 0.04D);
				if (i % 2 == 0) {
					sl.sendParticles(ParticleTypes.FLAME, px, py, pz, 1, 0.15D, 0.15D, 0.15D, 0.02D);
				}
			}
		}
	}

	private DamageSource makeDamageSource() {
		Entity owner = getOwnerEntity();
		if (owner instanceof Player player) {
			return this.damageSources().playerAttack(player);
		}
		if (owner instanceof LivingEntity living) {
			return this.damageSources().mobAttack(living);
		}
		return this.damageSources().magic();
	}

	public void setOwnerEntity(@Nullable Entity owner) {
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
		if (this.ownerUUID != null && this.level() instanceof ServerLevel sl) {
			this.cachedOwner = sl.getEntity(this.ownerUUID);
			return this.cachedOwner;
		}
		return null;
	}

	public Vec3 getStartPos() {
		Vector3f v = this.entityData.get(START_POS);
		return new Vec3(v.x, v.y, v.z);
	}

	public void setStartPos(Vec3 pos) {
		this.entityData.set(START_POS, new Vector3f((float) pos.x, (float) pos.y, (float) pos.z));
	}

	public Vec3 getEndPos() {
		Vector3f v = this.entityData.get(END_POS);
		return new Vec3(v.x, v.y, v.z);
	}

	public void setEndPos(Vec3 pos) {
		this.entityData.set(END_POS, new Vector3f((float) pos.x, (float) pos.y, (float) pos.z));
	}

	public boolean isDetonated() {
		return this.entityData.get(DETONATED);
	}

	public void setDetonated(boolean detonated) {
		this.entityData.set(DETONATED, detonated);
	}

	public int getDetonateTicks() {
		return this.entityData.get(DETONATE_TICKS);
	}

	public void setDetonateTicks(int ticks) {
		this.entityData.set(DETONATE_TICKS, ticks);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		if (tag.hasUUID(TAG_OWNER)) {
			this.ownerUUID = tag.getUUID(TAG_OWNER);
			this.cachedOwner = null;
		}
		setStartPos(new Vec3(tag.getDouble(TAG_START_X), tag.getDouble(TAG_START_Y), tag.getDouble(TAG_START_Z)));
		setEndPos(new Vec3(tag.getDouble(TAG_END_X), tag.getDouble(TAG_END_Y), tag.getDouble(TAG_END_Z)));
		this.damage = tag.getFloat(TAG_DAMAGE);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		if (this.ownerUUID != null) {
			tag.putUUID(TAG_OWNER, this.ownerUUID);
		}
		Vec3 start = getStartPos();
		Vec3 end = getEndPos();
		tag.putDouble(TAG_START_X, start.x);
		tag.putDouble(TAG_START_Y, start.y);
		tag.putDouble(TAG_START_Z, start.z);
		tag.putDouble(TAG_END_X, end.x);
		tag.putDouble(TAG_END_Y, end.y);
		tag.putDouble(TAG_END_Z, end.z);
		tag.putFloat(TAG_DAMAGE, this.damage);
	}
}
