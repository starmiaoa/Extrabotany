package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.BlockPos;
import vazkii.botania.client.fx.WispParticleData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.List;

public class InfluxWaverProjectileEntity extends OldSwordProjectileEntity {
	private static final String TAG_STRIKES = "Strikes";
	private static final String TAG_REMOVE_AT = "RemoveAt";
	private static final String TAG_NEXT = "Next";
	private static final int LIFETIME = 60;
	private static final float DAMAGE = 12F;

	private int strikeTimes;
	private int removeAt = -1;
	private BlockPos next = BlockPos.ZERO;

	public InfluxWaverProjectileEntity(EntityType<? extends InfluxWaverProjectileEntity> entityType, Level level) {
		super(entityType, level);
	}

	public InfluxWaverProjectileEntity(Level level, LivingEntity owner, Vec3 targetPos, int strikeTimes) {
		super(ExtraBotanyEntityType.INFLUX_WAVER_PROJECTILE, level, owner);
		this.setPos(owner.getX(), owner.getY() + 1.1D, owner.getZ());
		this.strikeTimes = strikeTimes;
		this.shootAt(targetPos, 0.7D);
	}

	private InfluxWaverProjectileEntity makeNext(BlockPos targetPos) {
		InfluxWaverProjectileEntity projectile = new InfluxWaverProjectileEntity(this.level(), (LivingEntity) this.getOwner(), Vec3.atCenterOf(targetPos), this.strikeTimes - 1);
		double angle = -Math.PI + 2 * Math.PI * this.level().random.nextDouble();
		double pitch = 0.12D * Math.PI * this.level().random.nextDouble() + 0.28D * Math.PI;
		double range = 6D;
		projectile.setPos(
				targetPos.getX() + range * Math.sin(pitch) * Math.cos(angle),
				targetPos.getY() + range * Math.cos(pitch),
				targetPos.getZ() + range * Math.sin(pitch) * Math.sin(angle)
		);
		projectile.shootAt(Vec3.atCenterOf(targetPos), 0.8D);
		return projectile;
	}

	@Override
	public void tick() {
		if (!this.level().isClientSide() && this.removeAt != -1 && this.tickCount >= this.removeAt + 4) {
			if (this.getOwner() instanceof LivingEntity && !this.next.equals(BlockPos.ZERO)) {
				this.level().addFreshEntity(makeNext(this.next));
			}
			this.discard();
			return;
		}
		super.tick();
	}

	@Override
	protected void hitEntities() {
		if (this.removeAt != -1) {
			return;
		}
		super.hitEntities();
	}

	@Override
	protected boolean damageTarget(LivingEntity target) {
		target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
		return super.damageTarget(target);
	}

	@Override
	protected void onDamagedTarget(LivingEntity target) {
		if (this.strikeTimes <= 0) {
			return;
		}
		if (target.isAlive()) {
			this.next = target.blockPosition().above();
			this.removeAt = this.tickCount;
			return;
		}

		AABB search = this.getBoundingBox().inflate(5D);
		List<LivingEntity> others = this.level().getEntitiesOfClass(LivingEntity.class, search);
		List<LivingEntity> filtered = DamageHandler.INSTANCE.getFilteredEntities(others, this.getOwner());
		if (!filtered.isEmpty()) {
			this.next = filtered.get(0).blockPosition().above();
			this.removeAt = this.tickCount;
		}
	}

	@Override
	protected boolean discardOnHit() {
		return false;
	}

	@Override
	protected Item getDefaultItem() {
		return ExtraBotanyItems.influxWaver;
	}

	@Override
	protected int getLifetime() {
		return LIFETIME;
	}

	@Override
	protected float getDamage() {
		return DAMAGE;
	}

	@Override
	protected void spawnTrailParticles() {
		if (this.tickCount % 2 == 0) {
			this.level().addParticle(WispParticleData.wisp(0.3F, 0.1F, 0.1F, 0.85F, 1F),
					this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(TAG_STRIKES, this.strikeTimes);
		tag.putInt(TAG_REMOVE_AT, this.removeAt);
		tag.putLong(TAG_NEXT, this.next.asLong());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.strikeTimes = tag.getInt(TAG_STRIKES);
		this.removeAt = tag.getInt(TAG_REMOVE_AT);
		this.next = BlockPos.of(tag.getLong(TAG_NEXT));
	}
}
