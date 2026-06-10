package io.github.lounode.extrabotany.common.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

public class ButterflyProjectileEntity extends ThrowableItemProjectile {
	private static final int LIFETIME = 100;

	public ButterflyProjectileEntity(EntityType<? extends ButterflyProjectileEntity> entityType, Level level) {
		super(entityType, level);
		this.setNoGravity(true);
	}

	public ButterflyProjectileEntity(Level level, LivingEntity owner) {
		super(ExtraBotanyEntityType.BUTTERFLY_PROJECTILE, owner, level);
		this.setNoGravity(true);
	}

	@Override
	public void tick() {
		super.tick();
		this.setNoGravity(true);
		if (!this.level().isClientSide() && this.tickCount > LIFETIME) {
			this.discard();
		}
	}

	@Override
	protected Item getDefaultItem() {
		return ExtraBotanyItems.uuzFan;
	}

	@Override
	protected void onHit(HitResult result) {
	}
}
