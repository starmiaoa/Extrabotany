package io.github.lounode.extrabotany.common.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.client.fx.WispParticleData;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.util.HerrscherCombatHelper;

public class TrueShadowKatanaProjectileEntity extends OldSwordProjectileEntity {
	private static final int LIFETIME = 40;
	private static final float DAMAGE = 5F;

	public TrueShadowKatanaProjectileEntity(EntityType<? extends TrueShadowKatanaProjectileEntity> entityType, Level level) {
		super(entityType, level);
	}

	public TrueShadowKatanaProjectileEntity(Level level, LivingEntity owner, Vec3 startPos, Vec3 targetPos) {
		super(ExtraBotanyEntityType.TRUE_SHADOW_KATANA_PROJECTILE, level, owner);
		this.setPos(startPos.x, startPos.y, startPos.z);
		this.shootAt(targetPos, 0.75D);
	}

	@Override
	protected Item getDefaultItem() {
		return ExtraBotanyItems.trueShadowKatana;
	}

	@Override
	protected int getHitStartTick() {
		return 4;
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
	protected boolean shouldRespectInvulnerability() {
		return false;
	}

	@Override
	protected boolean damageTarget(LivingEntity target) {
		target.invulnerableTime = 0;
		if (this.getOwner() instanceof Player) {
			return super.damageTarget(target);
		}
		HerrscherCombatHelper.dealTrueMagicDamage(target, this.getOwner(), 2F);
		return target.hurt(this.damageSources().indirectMagic(this, this.getOwner()), 5.5F);
	}

	@Override
	protected void spawnTrailParticles() {
		if (this.tickCount % 2 == 0) {
			this.level().addParticle(WispParticleData.wisp(0.15F, 0F, 0F, 0F, 1F),
					this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
		}
	}
}
