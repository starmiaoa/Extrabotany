package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

import org.joml.Vector3f;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.relic.JudahOathItem;
import io.github.lounode.extrabotany.common.util.HerrscherCombatHelper;

import java.util.List;

public class JudahSpearEntity extends ThrowableItemProjectile {
	private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(JudahSpearEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> FAKE = SynchedEntityData.defineId(JudahSpearEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> IMPACTED = SynchedEntityData.defineId(JudahSpearEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> IMPACT_TICKS = SynchedEntityData.defineId(JudahSpearEntity.class, EntityDataSerializers.INT);

	private static final String TAG_VARIANT = "Variant";
	private static final String TAG_DAMAGE = "Damage";
	private static final String TAG_FAKE = "Fake";
	private static final String TAG_IMPACTED = "Impacted";

	public static final int IMPACT_LINGER_TICKS = 12;

	private float damage = 8.0F;
	private boolean dealtDamage;

	public JudahSpearEntity(EntityType<? extends JudahSpearEntity> entityType, Level level) {
		super(entityType, level);
		this.setNoGravity(true);
	}

	public JudahSpearEntity(Level level, Entity owner, JudahOathItem.Variant variant, float damage, boolean fake) {
		super(ExtraBotanyEntityType.JUDAH_SPEAR, level);
		this.setOwner(owner);
		this.damage = damage;
		setVariant(variant);
		setFake(fake);
		this.setNoGravity(true);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(VARIANT, JudahOathItem.Variant.JUDAH.id());
		builder.define(FAKE, false);
		builder.define(IMPACTED, false);
		builder.define(IMPACT_TICKS, 0);
	}

	@Override
	public void tick() {
		super.tick();
		this.setNoGravity(true);

		if (isImpacted()) {
			int imp = getImpactTicks() + 1;
			if (!this.level().isClientSide()) {
				setImpactTicks(imp);
				if (imp >= IMPACT_LINGER_TICKS) {
					this.discard();
				}
			}
			return;
		}

		if (isFake()) {
			// Ascending skyward spear launched from the deployed Cross
			this.setPos(this.getX(), this.getY() + 0.95D, this.getZ());
			if (this.level().isClientSide()) {
				spawnTrailParticles();
			}
			if (!this.level().isClientSide() && this.tickCount > 36) {
				this.discard();
			}
			return;
		}

		// Descending lethal divine light / frost-star lance
		this.setPos(this.getX(), this.getY() - 1.35D, this.getZ());
		if (this.level().isClientSide()) {
			spawnTrailParticles();
		}

		if (!this.level().isClientSide()) {
			boolean hitEnemy = hitEntities();
			BlockPos pos = BlockPos.containing(this.getX(), this.getY() - 0.1D, this.getZ());
			BlockState state = this.level().getBlockState(pos);
			boolean hitSolidGround = !state.getCollisionShape(this.level(), pos).isEmpty();

			if (hitEnemy || hitSolidGround) {
				triggerImpact();
			} else if (this.tickCount > 70) {
				this.discard();
			}
		}
	}

	private boolean hitEntities() {
		if (this.dealtDamage) {
			return false;
		}
		Entity owner = this.getOwner();
		JudahOathItem.Variant variant = getVariant();

		double hitRadius = (variant == JudahOathItem.Variant.KIRA) ? 1.85D : 1.55D;
		AABB axis = new AABB(this.getX() - hitRadius, this.getY() - 1.8D, this.getZ() - hitRadius,
				this.getX() + hitRadius, this.getY() + 2.2D, this.getZ() + hitRadius);
		List<LivingEntity> targets = DamageHandler.INSTANCE.getFilteredEntities(
				this.level().getEntitiesOfClass(LivingEntity.class, axis), owner);

		if (targets.isEmpty()) {
			return false;
		}

		if (variant == JudahOathItem.Variant.KIRA) {
			// KIRA: 3.2-block AoE Frost-Star Nova shatter!
			AABB novaBox = this.getBoundingBox().inflate(3.2D, 2.5D, 3.2D);
			List<LivingEntity> novaTargets = DamageHandler.INSTANCE.getFilteredEntities(
					this.level().getEntitiesOfClass(LivingEntity.class, novaBox), owner);
			for (LivingEntity target : novaTargets) {
				target.invulnerableTime = 0;
				target.hurt(makeDamageSource(), this.damage * 1.45F);
				HerrscherCombatHelper.dealTrueMagicDamage(target, owner, this.damage * 0.22F);
				target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 2));
				target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 90, target.getTicksFrozen() + 60));
			}
		} else {
			// JUDAH: Focused Divine Sanction lightning spear + Chains of Binding stun!
			LivingEntity primary = targets.get(0);
			primary.invulnerableTime = 0;
			primary.hurt(makeDamageSource(), this.damage * 1.85F);
			HerrscherCombatHelper.dealTrueMagicDamage(primary, owner, this.damage * 0.28F);
			primary.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 4));
			if (primary.getMaxHealth() < 150.0F) {
				primary.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.IMMOBILIZE, 25, 0));
			}
			// Secondary golden splash shock to nearby enemies within 2.0 blocks
			for (int i = 1; i < targets.size(); i++) {
				LivingEntity splash = targets.get(i);
				splash.invulnerableTime = 0;
				splash.hurt(makeDamageSource(), this.damage * 0.9F);
				HerrscherCombatHelper.dealTrueMagicDamage(splash, owner, this.damage * 0.12F);
			}
		}

		this.dealtDamage = true;
		return true;
	}

	private void triggerImpact() {
		setImpacted(true);
		setImpactTicks(0);

		JudahOathItem.Variant variant = getVariant();
		if (variant == JudahOathItem.Variant.KIRA) {
			this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.75F, 1.45F);
			this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.6F);
		} else {
			this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.55F, 1.35F);
			this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TRIDENT_HIT_GROUND, SoundSource.PLAYERS, 0.85F, 1.15F);
		}

		if (this.level() instanceof ServerLevel sl) {
			DustParticleOptions dust = new DustParticleOptions(new Vector3f(variant.red(), variant.green(), variant.blue()), 1.5F);
			sl.sendParticles(dust, this.getX(), this.getY() + 0.3D, this.getZ(), 14, 0.7D, 0.4D, 0.7D, 0.1D);
			sl.sendParticles(variant == JudahOathItem.Variant.KIRA ? ParticleTypes.SNOWFLAKE : ParticleTypes.ELECTRIC_SPARK,
					this.getX(), this.getY() + 0.4D, this.getZ(), 16, 0.9D, 0.5D, 0.9D, 0.14D);
		}
	}

	private DamageSource makeDamageSource() {
		Entity owner = this.getOwner();
		if (owner instanceof Player player) {
			return this.damageSources().playerAttack(player);
		}
		if (owner instanceof LivingEntity living) {
			return this.damageSources().mobAttack(living);
		}
		return this.damageSources().lightningBolt();
	}

	private void spawnTrailParticles() {
		JudahOathItem.Variant v = getVariant();
		DustParticleOptions dust = new DustParticleOptions(new Vector3f(v.red(), v.green(), v.blue()), 1.25F);
		this.level().addParticle(dust, this.getX(), this.getY(), this.getZ(), 0, isFake() ? -0.05D : 0.08D, 0);
		if (this.tickCount % 2 == 0) {
			this.level().addParticle(v == JudahOathItem.Variant.KIRA ? ParticleTypes.SNOWFLAKE : ParticleTypes.END_ROD,
					this.getX() + (this.random.nextDouble() - 0.5D) * 0.25D,
					this.getY(),
					this.getZ() + (this.random.nextDouble() - 0.5D) * 0.25D,
					0, isFake() ? -0.04D : 0.06D, 0);
		}
	}

	@Override
	protected void onHit(HitResult result) {
	}

	@Override
	protected Item getDefaultItem() {
		return ExtraBotanyItems.judahOath;
	}

	private static Item itemFor(JudahOathItem.Variant variant) {
		return switch (variant) {
			case KIRA -> ExtraBotanyItems.judahOathKira;
			case SAKURA -> ExtraBotanyItems.judahOathSakura;
			default -> ExtraBotanyItems.judahOath;
		};
	}

	public JudahOathItem.Variant getVariant() {
		return JudahOathItem.Variant.byId(this.entityData.get(VARIANT));
	}

	public void setVariant(JudahOathItem.Variant variant) {
		this.entityData.set(VARIANT, variant.id());
		this.setItem(new ItemStack(itemFor(variant)));
	}

	public boolean isFake() {
		return this.entityData.get(FAKE);
	}

	public void setFake(boolean fake) {
		this.entityData.set(FAKE, fake);
	}

	public boolean isImpacted() {
		return this.entityData.get(IMPACTED);
	}

	public void setImpacted(boolean impacted) {
		this.entityData.set(IMPACTED, impacted);
	}

	public int getImpactTicks() {
		return this.entityData.get(IMPACT_TICKS);
	}

	public void setImpactTicks(int ticks) {
		this.entityData.set(IMPACT_TICKS, ticks);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(TAG_VARIANT, getVariant().id());
		tag.putFloat(TAG_DAMAGE, this.damage);
		tag.putBoolean(TAG_FAKE, isFake());
		tag.putBoolean(TAG_IMPACTED, isImpacted());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setVariant(JudahOathItem.Variant.byId(tag.getInt(TAG_VARIANT)));
		this.damage = tag.getFloat(TAG_DAMAGE);
		setFake(tag.getBoolean(TAG_FAKE));
		setImpacted(tag.getBoolean(TAG_IMPACTED));
	}
}
