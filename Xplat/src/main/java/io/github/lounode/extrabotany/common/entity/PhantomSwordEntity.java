package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

public class PhantomSwordEntity extends OldSwordProjectileEntity {
	private static final String TAG_DELAY = "Delay";
	private static final String TAG_DAMAGE = "Damage";
	private static final int LIFETIME = 80;

	private int delay;
	private float damage = 10F;

	public PhantomSwordEntity(EntityType<? extends PhantomSwordEntity> entityType, Level level) {
		super(entityType, level);
	}

	public PhantomSwordEntity(Level level, LivingEntity owner, Vec3 startPos, Vec3 targetPos, int delay, float damage) {
		super(ExtraBotanyEntityType.PHANTOM_SWORD, level, owner);
		this.setPos(startPos.x, startPos.y, startPos.z);
		this.delay = delay;
		this.damage = damage;
		this.setTargetPos(targetPos);
		if (delay <= 0) {
			this.shootAt(targetPos, 1.05D);
		}
	}

	@Override
	public void tick() {
		if (!this.level().isClientSide() && this.delay > 0) {
			this.delay--;
			if (this.delay == 0) {
				this.shootAt(this.getTargetPos(), 1.05D);
			}
		}
		super.tick();
	}

	@Override
	protected Item getDefaultItem() {
		return ExtraBotanyItems.firstFractal;
	}

	@Override
	protected int getHitStartTick() {
		return this.delay > 0 ? Integer.MAX_VALUE : 0;
	}

	@Override
	protected int getLifetime() {
		return LIFETIME;
	}

	@Override
	protected float getDamage() {
		return this.damage;
	}

	@Override
	protected void spawnTrailParticles() {
		this.level().addParticle(ParticleTypes.PORTAL,
				this.getX(), this.getY(), this.getZ(),
				0.0D, 0.0D, 0.0D);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(TAG_DELAY, this.delay);
		tag.putFloat(TAG_DAMAGE, this.damage);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.delay = tag.getInt(TAG_DELAY);
		this.damage = tag.getFloat(TAG_DAMAGE);
	}
}
