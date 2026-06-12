package io.github.lounode.extrabotany.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.client.fx.WispParticleData;

import io.github.lounode.extrabotany.common.util.HerrscherCombatHelper;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

public class PhantomSwordEntity extends OldSwordProjectileEntity {
	private static final String TAG_DELAY = "Delay";
	private static final String TAG_LIFE_TICKS = "LifeTicks";
	private static final String TAG_VARIETY = "Variety";
	private static final String TAG_FAKE = "Fake";
	private static final int LIFETIME = 26;
	private static final EntityDataAccessor<Integer> VARIETY = SynchedEntityData.defineId(PhantomSwordEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> FAKE = SynchedEntityData.defineId(PhantomSwordEntity.class, EntityDataSerializers.BOOLEAN);
	// Synced so the renderer can hide swords that have not been launched yet, like the old version did.
	private static final EntityDataAccessor<Integer> DELAY = SynchedEntityData.defineId(PhantomSwordEntity.class, EntityDataSerializers.INT);
	private static final float[][] RGB = {
			{ 0.82F, 0.2F, 0.58F }, { 0F, 0.71F, 0.10F }, { 0.74F, 0.07F, 0.32F },
			{ 0.01F, 0.45F, 0.8F }, { 0.05F, 0.39F, 0.9F }, { 0.38F, 0.34F, 0.42F },
			{ 0.41F, 0.31F, 0.14F }, { 0.92F, 0.92F, 0.21F }, { 0.61F, 0.92F, 0.98F },
			{ 0.18F, 0.45F, 0.43F }
	};

	private int lifeTicks;

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
		if (delay <= 0) {
			this.shootAt(targetPos, 1.05D);
		}
	}

	@Override
	public void tick() {
		if (!this.level().isClientSide() && this.getDelay() > 0) {
			this.setDelay(this.getDelay() - 1);
			if (this.getDelay() == 0) {
				this.shootAt(this.getTargetPos(), 1.05D);
			}
			return;
		}
		if (this.isFake()) {
			this.setDeltaMovement(Vec3.ZERO);
		}
		super.tick();
		if (!this.level().isClientSide() && this.getDelay() <= 0) {
			this.lifeTicks++;
			if (!this.isFake() && this.lifeTicks % 6 == 0) {
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

	@Override
	protected Item getDefaultItem() {
		return ExtraBotanyItems.firstFractal;
	}

	@Override
	protected int getHitStartTick() {
		return this.getDelay() > 0 ? Integer.MAX_VALUE : 0;
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
		this.level().addParticle(WispParticleData.wisp(0.25F, rgb[0], rgb[1], rgb[2]),
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
		return !(target instanceof Animal) && target.invulnerableTime <= 5;
	}

	@Override
	protected boolean damageTarget(LivingEntity target) {
		float dmg = calculateDamage();
		if (!HerrscherCombatHelper.isFinitePositive(dmg)) {
			return false;
		}
		Entity owner = getOwner();
		Player player = owner instanceof Player p ? p : null;
		HerrscherCombatHelper.dealTrueMagicDamage(target, owner, dmg * 0.05F);
		target.hurt(damageSources().magic(), dmg * 0.3F);
		DamageSource playerDamage = player != null ? damageSources().playerAttack(player)
				: owner instanceof LivingEntity living ? damageSources().mobAttack(living) : damageSources().generic();
		return target.hurt(playerDamage, dmg * 0.65F);
	}

	private float calculateDamage() {
		if (getOwner() instanceof Player player && player.getMainHandItem().is(ExtraBotanyItems.firstFractal)) {
			double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
			return (float) (player.getAttributeValue(Attributes.ATTACK_DAMAGE) * Math.min(1D + speed, 2D));
		}
		return 10F;
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
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.setDelay(tag.getInt(TAG_DELAY));
		this.lifeTicks = tag.getInt(TAG_LIFE_TICKS);
		this.setVariety(tag.getInt(TAG_VARIETY));
		this.setFake(tag.getBoolean(TAG_FAKE));
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(VARIETY, 0);
		this.entityData.define(FAKE, false);
		this.entityData.define(DELAY, 0);
	}
}
