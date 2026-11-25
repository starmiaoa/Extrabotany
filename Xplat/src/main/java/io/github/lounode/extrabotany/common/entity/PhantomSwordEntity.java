package io.github.lounode.extrabotany.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.fx.WispParticleData;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.util.HerrscherCombatHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PhantomSwordEntity extends OldSwordProjectileEntity {
	private static final String TAG_DELAY = "Delay";
	private static final String TAG_LIFE_TICKS = "LifeTicks";
	private static final String TAG_VARIETY = "Variety";
	private static final String TAG_FAKE = "Fake";
	private static final String TAG_SNAPSHOT_DAMAGE = "SnapshotDamage";
	private static final int LIFETIME = 32;
	private static final double FLIGHT_SPEED = 1.65D;

	private static final EntityDataAccessor<Integer> VARIETY = SynchedEntityData.defineId(PhantomSwordEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> FAKE = SynchedEntityData.defineId(PhantomSwordEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DELAY = SynchedEntityData.defineId(PhantomSwordEntity.class, EntityDataSerializers.INT);
	private static final float[][] RGB = {
			{ 0.82F, 0.2F, 0.58F }, { 0F, 0.71F, 0.10F }, { 0.74F, 0.07F, 0.32F },
			{ 0.01F, 0.45F, 0.8F }, { 0.05F, 0.39F, 0.9F }, { 0.38F, 0.34F, 0.42F },
			{ 0.41F, 0.31F, 0.14F }, { 0.92F, 0.92F, 0.21F }, { 0.61F, 0.92F, 0.98F },
			{ 0.18F, 0.45F, 0.43F }
	};

	private int lifeTicks;
	private float snapshotDamage = 22.0F;
	@Nullable
	private UUID lockedTargetUUID;
	private final Set<UUID> hitTargets = new HashSet<>();

	public PhantomSwordEntity(EntityType<? extends PhantomSwordEntity> entityType, Level level) {
		super(entityType, level);
	}

	public PhantomSwordEntity(Level level, LivingEntity owner, Vec3 startPos, Vec3 targetPos, int delay) {
		this(level, owner, startPos, targetPos, delay, level.random.nextInt(10));
	}

	public PhantomSwordEntity(Level level, LivingEntity owner, Vec3 startPos, Vec3 targetPos, int delay, int variety) {
		super(ExtraBotanyEntityType.PHANTOM_SWORD, level, owner);
		this.setPos(startPos.x, startPos.y, startPos.z);
		this.setDelay(delay);
		this.setVariety(variety);
		this.setTargetPos(targetPos);
		this.snapshotDamage = computeOwnerDamage(owner);
		if (delay <= 0) {
			this.shootAt(targetPos, FLIGHT_SPEED);
		}
	}

	public void setLockedTarget(@Nullable LivingEntity target) {
		this.lockedTargetUUID = target != null ? target.getUUID() : null;
	}

	@Override
	public void tick() {
		if (!this.level().isClientSide() && this.getDelay() > 0) {
			this.setDelay(this.getDelay() - 1);
			if (this.getDelay() == 0) {
				Vec3 aim = resolveLiveTargetPos();
				this.shootAt(aim, FLIGHT_SPEED);
			}
			return;
		}
		if (this.isFake()) {
			this.setDeltaMovement(Vec3.ZERO);
		}
		super.tick();
		if (!this.level().isClientSide() && this.getDelay() <= 0) {
			this.lifeTicks++;
			if (!this.isFake() && this.lifeTicks % 4 == 0) {
				PhantomSwordEntity illusion = new PhantomSwordEntity(ExtraBotanyEntityType.PHANTOM_SWORD, this.level());
				illusion.setOwner(this.getOwner());
				illusion.setFake(true);
				illusion.setVariety(this.getVariety());
				illusion.setTargetPos(this.getTargetPos());
				illusion.setPos(this.getX(), this.getY(), this.getZ());
				illusion.setYRot(this.getYRot());
				illusion.setXRot(this.getXRot());
				this.level().addFreshEntity(illusion);
			}
		}
	}

	private Vec3 resolveLiveTargetPos() {
		if (this.lockedTargetUUID != null && this.level() instanceof ServerLevel sl) {
			Entity e = sl.getEntity(this.lockedTargetUUID);
			if (e instanceof LivingEntity living && living.isAlive()) {
				return living.position().add(0.0D, living.getBbHeight() * 0.5D, 0.0D);
			}
		}
		return this.getTargetPos();
	}

	@Override
	protected void onHit(HitResult result) {
		// Spectral phantom blades phase through blocks so terrain/walls never swallow the barrage.
	}

	@Override
	protected void hitEntities() {
		if (isFake()) {
			return;
		}
		AABB axis = new AABB(this.getX(), this.getY(), this.getZ(), this.xOld, this.yOld, this.zOld).inflate(getHitRadius());
		List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, axis);
		List<LivingEntity> filtered = DamageHandler.INSTANCE.getFilteredEntities(targets, this.getOwner());

		for (LivingEntity target : filtered) {
			if (!canHitTarget(target) || !this.hitTargets.add(target.getUUID())) {
				continue;
			}
			if (damageTarget(target)) {
				onDamagedTarget(target);
			}
		}
	}

	@Override
	protected Item getDefaultItem() {
		return ExtraBotanyItems.firstFractal;
	}

	@Override
	protected int getHitStartTick() {
		return this.getDelay() > 0 ? Integer.MAX_VALUE : 0;
	}

	@Override
	protected double getHitRadius() {
		return 2.25D;
	}

	@Override
	protected int getLifetime() {
		return LIFETIME;
	}

	@Override
	protected float getDamage() {
		return calculateDamage();
	}

	@Override
	protected void spawnTrailParticles() {
		float[] rgb = RGB[Math.floorMod(getVariety(), RGB.length)];
		this.level().addParticle(WispParticleData.wisp(0.28F, rgb[0], rgb[1], rgb[2]),
				this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
	}

	@Override
	protected boolean shouldRespectInvulnerability() {
		return false;
	}

	@Override
	protected boolean discardOnHit() {
		return false;
	}

	@Override
	protected boolean canHitTarget(LivingEntity target) {
		if (isFake()) {
			return false;
		}
		return !(target instanceof Animal) && target.isAlive();
	}

	@Override
	protected boolean damageTarget(LivingEntity target) {
		float dmg = calculateDamage();
		if (!HerrscherCombatHelper.isFinitePositive(dmg)) {
			return false;
		}
		Entity owner = getOwner();
		Player player = owner instanceof Player p ? p : null;

		target.invulnerableTime = 0;
		HerrscherCombatHelper.dealTrueMagicDamage(target, owner, dmg * 0.22F);

		target.invulnerableTime = 0;
		boolean magicHit = target.hurt(damageSources().magic(), dmg * 0.48F);

		target.invulnerableTime = 0;
		DamageSource playerDamage = player != null ? damageSources().playerAttack(player)
				: owner instanceof LivingEntity living ? damageSources().mobAttack(living) : damageSources().generic();
		boolean physHit = target.hurt(playerDamage, dmg * 0.95F);

		target.invulnerableTime = 0;
		return magicHit || physHit;
	}

	private static float computeOwnerDamage(@Nullable Entity owner) {
		if (owner instanceof Player player) {
			double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
			double atk = Math.max(21.0D, player.getAttributeValue(Attributes.ATTACK_DAMAGE));
			return (float) (atk * Math.min(1.25D + speed * 1.5D, 2.6D));
		}
		return 22.0F;
	}

	private float calculateDamage() {
		if (getOwner() instanceof Player player && player.getMainHandItem().is(ExtraBotanyItems.firstFractal)) {
			return computeOwnerDamage(player);
		}
		return Math.max(22.0F, this.snapshotDamage);
	}

	public int getVariety() {
		return this.entityData.get(VARIETY);
	}

	public void setVariety(int variety) {
		this.entityData.set(VARIETY, Math.floorMod(variety, RGB.length));
	}

	public boolean isFake() {
		return this.entityData.get(FAKE);
	}

	public void setFake(boolean fake) {
		this.entityData.set(FAKE, fake);
	}

	public int getDelay() {
		return this.entityData.get(DELAY);
	}

	public void setDelay(int delay) {
		this.entityData.set(DELAY, delay);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(TAG_DELAY, this.getDelay());
		tag.putInt(TAG_LIFE_TICKS, this.lifeTicks);
		tag.putInt(TAG_VARIETY, this.getVariety());
		tag.putBoolean(TAG_FAKE, this.isFake());
		tag.putFloat(TAG_SNAPSHOT_DAMAGE, this.snapshotDamage);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.setDelay(tag.getInt(TAG_DELAY));
		this.lifeTicks = tag.getInt(TAG_LIFE_TICKS);
		this.setVariety(tag.getInt(TAG_VARIETY));
		this.setFake(tag.getBoolean(TAG_FAKE));
		if (tag.contains(TAG_SNAPSHOT_DAMAGE)) {
			this.snapshotDamage = tag.getFloat(TAG_SNAPSHOT_DAMAGE);
		}
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(VARIETY, 0);
		this.entityData.define(FAKE, false);
		this.entityData.define(DELAY, 0);
	}
}
