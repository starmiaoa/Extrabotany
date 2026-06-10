package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.relic.JudahOathItem;

import java.util.List;

public class JudahSpearEntity extends ThrowableItemProjectile {
	private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(JudahSpearEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> FAKE = SynchedEntityData.defineId(JudahSpearEntity.class, EntityDataSerializers.BOOLEAN);
	private static final String TAG_VARIANT = "Variant";
	private static final String TAG_DAMAGE = "Damage";
	private static final String TAG_FAKE = "Fake";

	private float damage = 7F;
	private boolean hit;

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
	}

	@Override
	public void tick() {
		super.tick();
		this.setNoGravity(true);
		spawnParticles();
		if (isFake()) {
			this.setPos(this.getX(), this.getY() + 0.75D, this.getZ());
		} else {
			this.setPos(this.getX(), this.getY() - 0.95D, this.getZ());
		}

		if (!this.level().isClientSide() && !isFake()) {
			hitEntities();
		}
		if (!this.level().isClientSide() && this.tickCount > 100) {
			this.discard();
		}
	}

	private void hitEntities() {
		if (this.hit) {
			return;
		}
		Entity owner = this.getOwner();
		AABB axis = new AABB(this.getX(), this.getY() - 6D, this.getZ(), this.xOld, this.yOld + 5D, this.zOld).inflate(1.3D, 0D, 1.3D);
		List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, axis);
		for (LivingEntity target : DamageHandler.INSTANCE.getFilteredEntities(targets, owner)) {
			target.hurt(makeDamageSource(), this.damage * 1.6F);
			dealTrueDamage(target, this.damage * 0.15F);
			this.hit = true;
			break;
		}
	}

	private void dealTrueDamage(LivingEntity target, float amount) {
		if (!target.isAlive() || amount <= 0F) {
			return;
		}
		float health = target.getHealth();
		if (health <= amount) {
			target.hurt(this.damageSources().indirectMagic(this, this.getOwner()), Float.MAX_VALUE);
		} else {
			target.setHealth(Math.max(1F, health - amount));
			target.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 0.01F);
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

	private void spawnParticles() {
		JudahOathItem.Variant variant = getVariant();
		this.level().addParticle(ParticleTypes.ENCHANT,
				this.getX(), this.getY(), this.getZ(),
				variant.red() * 0.02D, variant.green() * 0.02D, variant.blue() * 0.02D);
	}

	@Override
	protected void onHit(HitResult result) {
	}

	@Override
	protected Item getDefaultItem() {
		// Called from defineSynchedData() during construction, before entityData exists; must not read VARIANT here.
		return ExtraBotanyItems.judahOath;
	}

	private static Item itemFor(JudahOathItem.Variant variant) {
		return switch (variant) {
			case KIRA -> ExtraBotanyItems.judahOathKira;
			case SAKURA -> ExtraBotanyItems.judahOathSakura;
			default -> ExtraBotanyItems.judahOath;
		};
	}

	private JudahOathItem.Variant getVariant() {
		return JudahOathItem.Variant.byId(this.entityData.get(VARIANT));
	}

	private void setVariant(JudahOathItem.Variant variant) {
		this.entityData.set(VARIANT, variant.id());
		this.setItem(new ItemStack(itemFor(variant)));
	}

	private boolean isFake() {
		return this.entityData.get(FAKE);
	}

	private void setFake(boolean fake) {
		this.entityData.set(FAKE, fake);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(TAG_VARIANT, getVariant().id());
		tag.putFloat(TAG_DAMAGE, this.damage);
		tag.putBoolean(TAG_FAKE, isFake());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setVariant(JudahOathItem.Variant.byId(tag.getInt(TAG_VARIANT)));
		this.damage = tag.getFloat(TAG_DAMAGE);
		setFake(tag.getBoolean(TAG_FAKE));
	}
}
