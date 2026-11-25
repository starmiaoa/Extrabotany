package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
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

import io.github.lounode.extrabotany.common.util.HerrscherCombatHelper;

import java.util.Comparator;
import java.util.UUID;

public class SubspaceLanceEntity extends Entity {
	private static final DustParticleOptions GOLD_DUST = new DustParticleOptions(new Vector3f(1.0F, 0.84F, 0.30F), 1.05F);
	private static final DustParticleOptions VIOLET_DUST = new DustParticleOptions(new Vector3f(0.68F, 0.25F, 0.98F), 1.15F);
	private static final DustParticleOptions NET_VIOLET_DUST = new DustParticleOptions(new Vector3f(0.76F, 0.32F, 1.0F), 1.2F);
	private static final DustParticleOptions NET_GOLD_DUST = new DustParticleOptions(new Vector3f(1.0F, 0.86F, 0.35F), 1.0F);

	private static final String TAG_LIFE = "Life";
	private static final String TAG_DAMAGE = "Damage";
	private static final String TAG_OWNER = "Owner";
	private static final String TAG_HITS_LEFT = "HitsLeft";
	private static final String TAG_LANDED = "Landed";

	private int life = 700;
	private float damage = 4F;
	private int hitsLeft = 3;
	private boolean landed;
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
	public boolean isPickable() {
		return true;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (level().isClientSide() || isRemoved()) {
			return false;
		}
		if (source.getEntity() instanceof Player) {
			hitsLeft--;
			level().playSound(null, getX(), getY() + 1.0D, getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.HOSTILE, 0.9F, 1.1F + random.nextFloat() * 0.3F);
			if (level() instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(GOLD_DUST, getX(), getY() + 1.4D, getZ(), 10, 0.35D, 0.7D, 0.35D, 0.05D);
				serverLevel.sendParticles(VIOLET_DUST, getX(), getY() + 1.4D, getZ(), 10, 0.35D, 0.7D, 0.35D, 0.05D);
			}
			if (hitsLeft <= 0) {
				level().playSound(null, getX(), getY() + 1.0D, getZ(), SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.0F, 0.85F);
				if (level() instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.5D, getZ(), 18, 0.45D, 0.9D, 0.45D, 0.14D);
				}
				discard();
			}
			return true;
		}
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			spawnClientParticles();
			return;
		}
		if (ownerUUID != null && tickCount % 20 == 0 && !(getOwner() instanceof LivingEntity owner && owner.isAlive())) {
			discard();
			return;
		}
		if (!onGround()) {
			setDeltaMovement(getDeltaMovement().add(0, -0.18D, 0));
			move(net.minecraft.world.entity.MoverType.SELF, getDeltaMovement());
		} else if (!landed) {
			landed = true;
			onLanceImpact();
		}
		if (landed && tickCount % 45 == 0) {
			shockNearest();
		}
		if (landed && tickCount % 5 == 0) {
			damageLanceNets();
		}
		if (--life <= 0) {
			discard();
		}
	}

	private void onLanceImpact() {
		level().playSound(null, getX(), getY(), getZ(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.HOSTILE, 0.75F, 1.35F);
		if (level() instanceof ServerLevel serverLevel) {
			for (int i = 0; i < 16; i++) {
				double a = i * (Mth.TWO_PI / 16.0D);
				serverLevel.sendParticles(GOLD_DUST, getX() + Math.cos(a) * 1.4D, getY() + 0.25D, getZ() + Math.sin(a) * 1.4D, 1, 0.05D, 0.05D, 0.05D, 0.02D);
				serverLevel.sendParticles(VIOLET_DUST, getX() + Math.cos(a) * 1.9D, getY() + 0.25D, getZ() + Math.sin(a) * 1.9D, 1, 0.05D, 0.05D, 0.05D, 0.02D);
			}
		}
		for (Player player : level().getEntitiesOfClass(Player.class, new AABB(blockPosition()).inflate(2.2D), p -> p.isAlive() && !p.isCreative() && !p.isSpectator())) {
			player.hurt(damageSources().indirectMagic(this, getOwner() != null ? getOwner() : this), damage * 1.4F);
		}
	}

	private void spawnClientParticles() {
		if (!this.onGround() || Math.abs(this.getY() - this.yOld) > 0.08D) {
			for (int i = 0; i < 4; i++) {
				double t = i / 4.0D;
				double sx = Mth.lerp(t, this.xOld, this.getX()) + (this.random.nextDouble() - 0.5D) * 0.25D;
				double sy = Mth.lerp(t, this.yOld, this.getY()) + 1.6D;
				double sz = Mth.lerp(t, this.zOld, this.getZ()) + (this.random.nextDouble() - 0.5D) * 0.25D;
				this.level().addParticle((i % 2 == 0) ? GOLD_DUST : VIOLET_DUST, sx, sy, sz, 0.0D, 0.08D, 0.0D);
				if (i == 0 || i == 2) {
					this.level().addParticle(ParticleTypes.END_ROD, sx, sy, sz, 0.0D, 0.12D, 0.0D);
				}
			}
		}

		double baseAngle = this.tickCount * 0.30D;
		double goldHeight = ((this.tickCount * 0.16D) % 3.0D) + 0.2D;
		double goldRadius = 0.65D + 0.15D * Math.sin(this.tickCount * 0.15D);
		this.level().addParticle(GOLD_DUST,
				this.getX() + Math.cos(baseAngle) * goldRadius,
				this.getY() + goldHeight,
				this.getZ() + Math.sin(baseAngle) * goldRadius,
				0.0D, 0.05D, 0.0D);

		double violetAngle = baseAngle + Math.PI;
		double violetHeight = (((this.tickCount + 10) * 0.16D) % 3.0D) + 0.2D;
		double violetRadius = 0.78D + 0.15D * Math.cos(this.tickCount * 0.15D);
		this.level().addParticle(VIOLET_DUST,
				this.getX() + Math.cos(violetAngle) * violetRadius,
				this.getY() + violetHeight,
				this.getZ() + Math.sin(violetAngle) * violetRadius,
				0.0D, 0.05D, 0.0D);

		if (this.tickCount % 2 == 0) {
			double sparkAngle = this.random.nextDouble() * Mth.TWO_PI;
			this.level().addParticle(ParticleTypes.END_ROD,
					this.getX() + Math.cos(sparkAngle) * 0.45D,
					this.getY() + 1.2D + this.random.nextDouble() * 1.4D,
					this.getZ() + Math.sin(sparkAngle) * 0.45D,
					0.0D, 0.04D, 0.0D);

			double ringAngle = this.random.nextDouble() * Mth.TWO_PI;
			double ringR = 0.85D + this.random.nextDouble() * 0.45D;
			this.level().addParticle(ParticleTypes.PORTAL,
					this.getX() + Math.cos(ringAngle) * ringR,
					this.getY() + 0.20D,
					this.getZ() + Math.sin(ringAngle) * ringR,
					-Math.cos(ringAngle) * 0.12D, 0.08D, -Math.sin(ringAngle) * 0.12D);
		}
	}

	private void shockNearest() {
		level().getEntitiesOfClass(Player.class, new AABB(blockPosition()).inflate(7.5D), p -> p.isAlive() && !p.isCreative() && !p.isSpectator()).stream()
				.min(Comparator.comparingDouble(this::distanceToSqr))
				.ifPresent(player -> {
					if (player.getHealth() > 10F) {
						HerrscherCombatHelper.dealTrueMagicDamage(player, this, damage + player.getMaxHealth() * 0.075F);
					} else {
						player.hurt(damageSources().indirectMagic(this, this), damage);
					}
					player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
					if (level() instanceof ServerLevel serverLevel) {
						serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0D, player.getZ(), 6, 0.3D, 0.5D, 0.3D, 0.08D);
					}
				});
	}

	private void damageLanceNets() {
		if (!(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		int phase = (this.tickCount / 5) % 3;
		for (SubspaceLanceEntity other : level().getEntitiesOfClass(SubspaceLanceEntity.class, new AABB(blockPosition()).inflate(16D), Entity::isAlive)) {
			if (other == this || other.getId() < this.getId() || !other.landed) {
				continue;
			}
			Vec3 a = position().add(0, 1.5D, 0);
			Vec3 b = other.position().add(0, 1.5D, 0);
			for (Player player : level().getEntitiesOfClass(Player.class, new AABB(a, b).inflate(0.8D), p -> p.isAlive() && !p.isCreative() && !p.isSpectator())) {
				if (distanceToSegment(player.position().add(0, player.getBbHeight() * 0.5D, 0), a, b) <= 0.8D && player.getHealth() > 6F) {
					player.hurt(damageSources().lightningBolt(), 1.5F);
					HerrscherCombatHelper.dealTrueMagicDamage(player, this, 0.5F);
					player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 80, 1));
				}
			}

			int segments = Math.max(10, Math.min(14, (int) Math.ceil(a.distanceTo(b) * 1.25D)));
			for (int i = 0; i <= segments; i++) {
				double t = (double) i / segments;
				double px = Mth.lerp(t, a.x, b.x);
				double py = Mth.lerp(t, a.y, b.y);
				double pz = Mth.lerp(t, a.z, b.z);
				serverLevel.sendParticles(NET_VIOLET_DUST, px, py, pz, 1, 0.04D, 0.04D, 0.04D, 0.0D);
				if (i % 2 == 0) {
					serverLevel.sendParticles(NET_GOLD_DUST, px, py, pz, 1, 0.03D, 0.03D, 0.03D, 0.0D);
				}
				if ((i + phase) % 3 == 0) {
					serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, px, py, pz, 1, 0.06D, 0.06D, 0.06D, 0.02D);
				}
			}
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
		hitsLeft = tag.contains(TAG_HITS_LEFT) ? Math.max(1, tag.getInt(TAG_HITS_LEFT)) : 3;
		landed = tag.getBoolean(TAG_LANDED);
		if (tag.hasUUID(TAG_OWNER)) {
			ownerUUID = tag.getUUID(TAG_OWNER);
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putInt(TAG_LIFE, life);
		tag.putFloat(TAG_DAMAGE, damage);
		tag.putInt(TAG_HITS_LEFT, hitsLeft);
		tag.putBoolean(TAG_LANDED, landed);
		if (ownerUUID != null) {
			tag.putUUID(TAG_OWNER, ownerUUID);
		}
	}
}
