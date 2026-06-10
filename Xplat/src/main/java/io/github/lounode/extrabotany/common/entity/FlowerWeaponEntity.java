package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.tool.KingGardenItem;

import java.util.List;
import java.util.UUID;

public class FlowerWeaponEntity extends ThrowableItemProjectile {
	private static final EntityDataAccessor<Boolean> CHARGING = SynchedEntityData.defineId(FlowerWeaponEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> VARIETY = SynchedEntityData.defineId(FlowerWeaponEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> CHARGE_TICKS = SynchedEntityData.defineId(FlowerWeaponEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> LIVE_TICKS = SynchedEntityData.defineId(FlowerWeaponEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DELAY = SynchedEntityData.defineId(FlowerWeaponEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> ROTATION = SynchedEntityData.defineId(FlowerWeaponEntity.class, EntityDataSerializers.FLOAT);

	private static final String TAG_OWNER = "Owner";
	private static final String TAG_CHARGING = "Charging";
	private static final String TAG_VARIETY = "Variety";
	private static final String TAG_CHARGE_TICKS = "ChargeTicks";
	private static final String TAG_LIVE_TICKS = "LiveTicks";
	private static final String TAG_DELAY = "Delay";
	private static final String TAG_ROTATION = "Rotation";

	@Nullable
	private UUID ownerUUID;
	@Nullable
	private Entity cachedOwner;

	public FlowerWeaponEntity(EntityType<? extends FlowerWeaponEntity> entityType, Level level) {
		super(entityType, level);
		this.setNoGravity(true);
	}

	public FlowerWeaponEntity(Level level, LivingEntity owner) {
		super(ExtraBotanyEntityType.FLOWER_WEAPON, owner, level);
		this.setOwnerEntity(owner);
		this.setNoGravity(true);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(CHARGING, false);
		builder.define(VARIETY, 0);
		builder.define(CHARGE_TICKS, 0);
		builder.define(LIVE_TICKS, 0);
		builder.define(DELAY, 0);
		builder.define(ROTATION, 0F);
	}

	@Override
	public void tick() {
		this.setNoGravity(true);
		Entity owner = getOwnerEntity();
		if (!this.level().isClientSide() && (!(owner instanceof Player player) || !player.isAlive())) {
			this.discard();
			return;
		}

		if (!this.level().isClientSide() && owner instanceof Player player) {
			boolean newCharging = findKingGarden(player) != null && KingGardenItem.isCharging(findKingGarden(player));
			if (isCharging() != newCharging) {
				setCharging(newCharging);
			}
		}

		Vec3 motion = this.getDeltaMovement();
		int liveTicks = getLiveTicks();
		int delay = getDelay();
		boolean charging = isCharging() && liveTicks == 0;
		if (charging) {
			this.setDeltaMovement(Vec3.ZERO);
			setChargeTicks(getChargeTicks() + 1);
			if (!this.level().isClientSide() && this.random.nextInt(20) == 0) {
				this.level().playSound(null, this.blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 0.1F, 1F + this.random.nextFloat() * 3F);
			}
		} else {
			if (liveTicks < delay) {
				this.setDeltaMovement(Vec3.ZERO);
			} else if (liveTicks == delay && owner instanceof Player player) {
				Vec3 target = getLookTarget(player);
				motion = target.subtract(this.position()).normalize().scale(2D);
				this.setDeltaMovement(motion);
				this.level().playSound(null, this.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 1.4F, 0.1F + this.random.nextFloat() * 3F);
			}
			setLiveTicks(liveTicks + 1);
			if (!this.level().isClientSide()) {
				hitEntities(owner);
			}
		}

		super.tick();
		if (!charging) {
			this.setDeltaMovement(motion);
		}

		if (this.level().isClientSide() && liveTicks > delay) {
			this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY(), this.getZ(), 0D, 0.05D, 0D);
		}
		if (!this.level().isClientSide() && liveTicks > 200 + delay) {
			this.discard();
		}
	}

	private void hitEntities(@Nullable Entity owner) {
		AABB axis = new AABB(this.getX(), this.getY(), this.getZ(), this.xOld, this.yOld, this.zOld).inflate(2D);
		List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, axis);
		for (LivingEntity target : DamageHandler.INSTANCE.getFilteredEntities(entities, owner)) {
			if (target.hurtTime > 0) {
				continue;
			}
			applyEffect(target, owner);
			if (getVariety() == 5) {
				this.level().explode(this, this.getX(), this.getY(), this.getZ(), 2F, Level.ExplosionInteraction.NONE);
			}
			this.discard();
			return;
		}
	}

	private void applyEffect(LivingEntity target, @Nullable Entity owner) {
		Player player = owner instanceof Player p ? p : null;
		switch (getVariety()) {
			case 0 -> target.hurt(magicSource(owner), 9F);
			case 1 -> {
				target.hurt(attackSource(owner), 7F);
				target.igniteForSeconds(5);
			}
			case 2 -> {
				target.hurt(attackSource(owner), 7F);
				target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 1));
			}
			case 3 -> {
				target.hurt(magicSource(owner), 8F);
				target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 1));
			}
			case 4 -> target.hurt(attackSource(owner), 12F);
			case 5 -> target.hurt(attackSource(owner), 5F);
			case 6 -> {
				target.hurt(attackSource(owner), 7F);
				target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
			}
			case 7 -> {
				target.hurt(attackSource(owner), 7F);
				target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
			}
			case 8 -> target.hurt(this.damageSources().lava(), 10F);
			case 9 -> {
				target.hurt(magicSource(owner), 6F);
				target.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 200, 1));
			}
			case 10 -> {
				target.hurt(attackSource(owner), 6F);
				target.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
			}
			case 11 -> {
				target.hurt(attackSource(owner), 8F);
				target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
			}
			case 12 -> target.hurt(attackSource(owner), 9F);
			case 13 -> {
				target.hurt(magicSource(owner), 5F);
				if (player != null) {
					player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
				}
			}
			case 14 -> {
				target.hurt(magicSource(owner), 5F);
				if (player != null) {
					player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1));
				}
			}
			case 15 -> {
				target.hurt(attackSource(owner), 8F);
				if (player != null) {
					player.addEffect(new MobEffectInstance(MobEffects.JUMP, 200, 1));
				}
			}
			case 16 -> {
				target.hurt(attackSource(owner), 8F);
				if (player != null) {
					player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
				}
			}
			default -> target.hurt(attackSource(owner), 6F);
		}
	}

	private DamageSource attackSource(@Nullable Entity owner) {
		if (owner instanceof Player player) {
			return this.damageSources().playerAttack(player);
		}
		if (owner instanceof LivingEntity living) {
			return this.damageSources().mobAttack(living);
		}
		return this.damageSources().generic();
	}

	private DamageSource magicSource(@Nullable Entity owner) {
		if (owner != null) {
			return this.damageSources().indirectMagic(this, owner);
		}
		return this.damageSources().magic();
	}

	private Vec3 getLookTarget(Player player) {
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle();
		Vec3 end = eye.add(look.scale(64D));
		BlockHitResult hit = this.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		return hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
	}

	@Nullable
	private static ItemStack findKingGarden(Player player) {
		ItemStack main = player.getMainHandItem();
		if (main.is(ExtraBotanyItems.kingGarden)) {
			return main;
		}
		ItemStack off = player.getOffhandItem();
		return off.is(ExtraBotanyItems.kingGarden) ? off : null;
	}

	public void setOwnerEntity(@Nullable Entity owner) {
		if (owner != null) {
			this.ownerUUID = owner.getUUID();
			this.cachedOwner = owner;
		}
	}

	@Nullable
	public Entity getOwnerEntity() {
		Entity owner = this.getOwner();
		if (owner != null) {
			return owner;
		}
		if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
			return this.cachedOwner;
		}
		if (this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
			this.cachedOwner = serverLevel.getEntity(this.ownerUUID);
			return this.cachedOwner;
		}
		return null;
	}

	public boolean isCharging() {
		return this.entityData.get(CHARGING);
	}

	public void setCharging(boolean charging) {
		this.entityData.set(CHARGING, charging);
	}

	public int getVariety() {
		return this.entityData.get(VARIETY);
	}

	public void setVariety(int variety) {
		this.entityData.set(VARIETY, variety);
	}

	public int getChargeTicks() {
		return this.entityData.get(CHARGE_TICKS);
	}

	public void setChargeTicks(int ticks) {
		this.entityData.set(CHARGE_TICKS, ticks);
	}

	public int getLiveTicks() {
		return this.entityData.get(LIVE_TICKS);
	}

	public void setLiveTicks(int ticks) {
		this.entityData.set(LIVE_TICKS, ticks);
	}

	public int getDelay() {
		return this.entityData.get(DELAY);
	}

	public void setDelay(int delay) {
		this.entityData.set(DELAY, delay);
	}

	public void setRotation(float rotation) {
		this.entityData.set(ROTATION, rotation);
	}

	@Override
	protected void onHit(HitResult result) {
	}

	@Override
	protected Item getDefaultItem() {
		return ExtraBotanyItems.kingGarden;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (this.ownerUUID != null) {
			tag.putUUID(TAG_OWNER, this.ownerUUID);
		}
		tag.putBoolean(TAG_CHARGING, isCharging());
		tag.putInt(TAG_VARIETY, getVariety());
		tag.putInt(TAG_CHARGE_TICKS, getChargeTicks());
		tag.putInt(TAG_LIVE_TICKS, getLiveTicks());
		tag.putInt(TAG_DELAY, getDelay());
		tag.putFloat(TAG_ROTATION, this.entityData.get(ROTATION));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.hasUUID(TAG_OWNER)) {
			this.ownerUUID = tag.getUUID(TAG_OWNER);
			this.cachedOwner = null;
		}
		setCharging(tag.getBoolean(TAG_CHARGING));
		setVariety(tag.getInt(TAG_VARIETY));
		setChargeTicks(tag.getInt(TAG_CHARGE_TICKS));
		setLiveTicks(tag.getInt(TAG_LIVE_TICKS));
		setDelay(tag.getInt(TAG_DELAY));
		setRotation(tag.getFloat(TAG_ROTATION));
	}
}
