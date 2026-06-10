package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.relic.JudahOathItem;

import java.util.List;
import java.util.UUID;

public class JudahOathEntity extends ThrowableItemProjectile {
	private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(JudahOathEntity.class, EntityDataSerializers.INT);
	private static final String TAG_OWNER = "Owner";
	private static final String TAG_VARIANT = "Variant";
	private static final float DAMAGE = 7F;

	@Nullable
	private UUID ownerUUID;
	@Nullable
	private Entity cachedOwner;
	private boolean landed;
	private float range = 5F;
	private int fakeCount;
	private int attackCount;
	private int standbyTicks;

	public JudahOathEntity(EntityType<? extends JudahOathEntity> entityType, Level level) {
		super(entityType, level);
	}

	public JudahOathEntity(Level level, LivingEntity owner, JudahOathItem.Variant variant) {
		super(ExtraBotanyEntityType.JUDAH_OATH, owner, level);
		setOwnerEntity(owner);
		setVariant(variant);
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(VARIANT, JudahOathItem.Variant.JUDAH.id());
	}

	@Override
	public void tick() {
		super.tick();
		spawnAuraParticles();

		if (!this.landed) {
			this.setDeltaMovement(this.getDeltaMovement().multiply(1D, 0.6D, 1D));
			if (!this.level().getBlockState(this.blockPosition().below()).is(Blocks.AIR) || this.onGround()) {
				this.landed = true;
				this.setDeltaMovement(Vec3.ZERO);
				this.level().addParticle(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
			}
		} else {
			this.setDeltaMovement(Vec3.ZERO);
			this.standbyTicks++;
			if (this.range <= 13F) {
				this.range += 0.5F;
			}
			if (!this.level().isClientSide()) {
				tickStandby();
			}
		}

		if (!this.level().isClientSide() && (this.standbyTicks > 140 || this.tickCount > 300 || !(getOwnerEntity() instanceof Player player) || !player.isAlive())) {
			this.discard();
		}
	}

	private void tickStandby() {
		JudahOathItem.Variant variant = getVariant();
		if (variant == JudahOathItem.Variant.SAKURA) {
			if (this.standbyTicks >= 20 && this.standbyTicks % 30 == 0 && this.attackCount <= 2) {
				spawnSakuraStar();
				this.attackCount++;
			}
			return;
		}

		if (this.tickCount % 4 == 0 && this.fakeCount < 13) {
			spawnSpear(this.position(), true);
			this.fakeCount++;
		}

		Vec3 targetPos = this.position().add(0, 10, 0);
		List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(this.range));
		Entity owner = getOwnerEntity();
		for (LivingEntity target : targets) {
			if (!DamageHandler.INSTANCE.checkPassable(target, owner)) {
				continue;
			}
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 9));
			targetPos = target.position().add(0, 10, 0);
		}

		if (this.standbyTicks > 20 && this.tickCount % 10 == 0 && this.attackCount < 13) {
			spawnSpear(targetPos, false);
			this.attackCount++;
		}
	}

	private void spawnSpear(Vec3 pos, boolean fake) {
		JudahSpearEntity spear = new JudahSpearEntity(this.level(), getOwnerEntity(), getVariant(), DAMAGE, fake);
		spear.setPos(pos.x, pos.y, pos.z);
		this.level().addFreshEntity(spear);
	}

	private void spawnSakuraStar() {
		Vec3 center = this.position();
		double angleStep = Math.PI * 2D / 5D;
		Vec3[] points = new Vec3[5];
		for (int i = 0; i < 5; i++) {
			double angle = angleStep * i + Math.toRadians(this.getYRot() + this.attackCount * 36D);
			points[i] = center.add(Math.cos(angle) * 11D, 1D, Math.sin(angle) * 11D);
		}
		for (int i = 0; i < 5; i++) {
			Vec3 start = points[i];
			Vec3 end = points[(i + 2) % 5];
			JudahSwordEntity sword = new JudahSwordEntity(this.level(), getOwnerEntity(), start, end, 6F);
			sword.setPos(start.x, start.y, start.z);
			this.level().addFreshEntity(sword);
		}
	}

	private void spawnAuraParticles() {
		JudahOathItem.Variant variant = getVariant();
		if (!this.landed) {
			this.level().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
			return;
		}
		for (int i = 0; i < 360; i += 16) {
			float rad = i * Mth.DEG_TO_RAD;
			double x = this.getX() + 0.5D - Math.cos(rad) * this.range;
			double z = this.getZ() + 0.5D - Math.sin(rad) * this.range;
			this.level().addParticle(ParticleTypes.ENCHANT,
					x, this.getY() + 0.2D, z,
					variant.red() * 0.02D, variant.green() * 0.02D, variant.blue() * 0.02D);
		}
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

	public JudahOathItem.Variant getVariant() {
		return JudahOathItem.Variant.byId(this.entityData.get(VARIANT));
	}

	public void setVariant(JudahOathItem.Variant variant) {
		this.entityData.set(VARIANT, variant.id());
	}

	@Override
	protected void onHit(HitResult result) {
	}

	@Override
	protected Item getDefaultItem() {
		return switch (getVariant()) {
			case KIRA -> ExtraBotanyItems.judahOathKira;
			case SAKURA -> ExtraBotanyItems.judahOathSakura;
			default -> ExtraBotanyItems.judahOath;
		};
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (this.ownerUUID != null) {
			tag.putUUID(TAG_OWNER, this.ownerUUID);
		}
		tag.putInt(TAG_VARIANT, getVariant().id());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.hasUUID(TAG_OWNER)) {
			this.ownerUUID = tag.getUUID(TAG_OWNER);
			this.cachedOwner = null;
		}
		setVariant(JudahOathItem.Variant.byId(tag.getInt(TAG_VARIANT)));
	}
}
