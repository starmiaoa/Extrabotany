package io.github.lounode.extrabotany.common.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.client.fx.WispParticleData;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.util.HerrscherCombatHelper;

import java.util.List;

public class TrueTerrabladeProjectileEntity extends OldSwordProjectileEntity {
	private static final int LIFETIME = 60;
	private static final float DAMAGE = 11F;

	public TrueTerrabladeProjectileEntity(EntityType<? extends TrueTerrabladeProjectileEntity> entityType, Level level) {
		super(entityType, level);
	}

	public TrueTerrabladeProjectileEntity(Level level, LivingEntity owner, Vec3 targetPos) {
		super(ExtraBotanyEntityType.TRUE_TERRABLADE_PROJECTILE, level, owner);
		this.setPos(owner.getX(), owner.getY() + 1.1D, owner.getZ());
		this.shootAt(targetPos, 0.8D);
	}

	@Override
	protected Item getDefaultItem() {
		return ExtraBotanyItems.trueTerrablade;
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
	protected boolean discardOnHit() {
		return false;
	}

	@Override
	protected void hitEntities() {
		if (this.tickCount % 3 != 0) {
			return;
		}
		AABB axis = new AABB(this.getX(), this.getY(), this.getZ(), this.xOld, this.yOld, this.zOld).inflate(getHitRadius());
		List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, axis);
		for (LivingEntity target : DamageHandler.INSTANCE.getFilteredEntities(targets, this.getOwner())) {
			damageTarget(target);
		}
	}

	@Override
	protected boolean damageTarget(LivingEntity target) {
		if (this.getOwner() instanceof Player) {
			return super.damageTarget(target);
		}
		if (target.invulnerableTime == 0) {
			HerrscherCombatHelper.dealTrueMagicDamage(target, this.getOwner(), 2.5F);
		}
		return target.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 7F);
	}

	@Override
	protected void spawnTrailParticles() {
		if (this.tickCount % 2 == 0) {
			this.level().addParticle(WispParticleData.wisp(0.3F, 0.1F, 0.95F, 0.1F, 1F),
					this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
		}
	}
}
