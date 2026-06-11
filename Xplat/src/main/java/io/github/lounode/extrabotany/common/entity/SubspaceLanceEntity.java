package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
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

import io.github.lounode.extrabotany.common.util.HerrscherCombatHelper;

import java.util.Comparator;
import java.util.UUID;

public class SubspaceLanceEntity extends Entity {
	private static final String TAG_LIFE = "Life";
	private static final String TAG_DAMAGE = "Damage";
	private static final String TAG_OWNER = "Owner";
	private int life = 1200;
	private float damage = 4F;
	@Nullable
	private UUID ownerUUID;
	@Nullable
	private Entity cachedOwner;

	public SubspaceLanceEntity(EntityType<? extends SubspaceLanceEntity> type, Level level) {
		super(type, level);
	}

	public SubspaceLanceEntity(Level level, float damage, int life) {
		this(ExtraBotanyEntityType.SUBSPACE_LANCE, level);
		this.damage = damage;
		this.life = life;
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			level().addParticle(ParticleTypes.END_ROD, getX(), getY() + 1.5D, getZ(), 0, 0, 0);
			return;
		}
		if (ownerUUID != null && tickCount % 20 == 0 && !(getOwner() instanceof LivingEntity owner && owner.isAlive())) {
			discard();
			return;
		}
		if (!onGround()) {
			setDeltaMovement(getDeltaMovement().add(0, -0.15D, 0));
			move(net.minecraft.world.entity.MoverType.SELF, getDeltaMovement());
		}
		if (tickCount % 35 == 0) {
			shockNearest();
		}
		if (tickCount % 5 == 0) {
			damageLanceNets();
		}
		if (--life <= 0) {
			discard();
		}
	}

	private void shockNearest() {
		level().getEntitiesOfClass(Player.class, new AABB(blockPosition()).inflate(8D), Player::isAlive).stream()
				.min(Comparator.comparingDouble(this::distanceToSqr))
				.ifPresent(player -> {
					if (player.getHealth() > 10F) {
						HerrscherCombatHelper.dealTrueMagicDamage(player, this, damage + player.getMaxHealth() * 0.1F);
					} else {
						player.hurt(damageSources().indirectMagic(this, this), damage);
					}
					player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
				});
	}

	private void damageLanceNets() {
		if (!(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		for (SubspaceLanceEntity other : level().getEntitiesOfClass(SubspaceLanceEntity.class, new AABB(blockPosition()).inflate(15D), Entity::isAlive)) {
			if (other == this || other.getId() < this.getId()) {
				continue;
			}
			Vec3 a = position().add(0, 1.5D, 0);
			Vec3 b = other.position().add(0, 1.5D, 0);
			for (Player player : level().getEntitiesOfClass(Player.class, new AABB(a, b).inflate(0.8D), Player::isAlive)) {
				if (distanceToSegment(player.position().add(0, player.getBbHeight() * 0.5D, 0), a, b) <= 0.8D && player.getHealth() > 8F) {
					player.hurt(damageSources().lightningBolt(), 1.2F);
					HerrscherCombatHelper.dealTrueMagicDamage(player, this, 0.4F);
					player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 100, 1));
				}
			}
			serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, (a.x + b.x) * 0.5D, (a.y + b.y) * 0.5D, (a.z + b.z) * 0.5D, 8, Math.abs(a.x - b.x) * 0.25D, Math.abs(a.y - b.y) * 0.25D, Math.abs(a.z - b.z) * 0.25D, 0.01D);
		}
	}

	private static double distanceToSegment(Vec3 p, Vec3 a, Vec3 b) {
		Vec3 ab = b.subtract(a);
		double len = Math.max(1.0E-6D, ab.lengthSqr());
		double t = Math.max(0D, Math.min(1D, p.subtract(a).dot(ab) / len));
		return p.distanceTo(a.add(ab.scale(t)));
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
		} else if (this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
			this.cachedOwner = serverLevel.getEntity(this.ownerUUID);
			return this.cachedOwner;
		} else {
			return null;
		}
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		life = tag.getInt(TAG_LIFE);
		damage = tag.getFloat(TAG_DAMAGE);
		if (tag.hasUUID(TAG_OWNER)) {
			ownerUUID = tag.getUUID(TAG_OWNER);
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putInt(TAG_LIFE, life);
		tag.putFloat(TAG_DAMAGE, damage);
		if (ownerUUID != null) {
			tag.putUUID(TAG_OWNER, ownerUUID);
		}
	}
}
