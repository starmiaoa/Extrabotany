package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.entity.gaia.GaiaIII;
import io.github.lounode.extrabotany.common.entity.gaia.VoidHerrscher;
import io.github.lounode.extrabotany.common.util.HerrscherCombatHelper;

import java.util.UUID;

public class SwordDomainEntity extends Entity {
	private static final String TAG_TARGET = "Target";
	private static final String TAG_SOURCE = "Source";
	private static final String TAG_FLOOR_Y = "FloorY";
	private static final String TAG_TYPE = "Type";
	private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(SwordDomainEntity.class, EntityDataSerializers.INT);
	private UUID target;
	private BlockPos source = BlockPos.ZERO;
	private int floorY;

	public SwordDomainEntity(EntityType<? extends SwordDomainEntity> type, Level level) {
		super(type, level);
	}

	public SwordDomainEntity(Level level, UUID target, BlockPos source, int type) {
		this(ExtraBotanyEntityType.SWORD_DOMAIN, level);
		this.target = target;
		this.source = source;
		this.floorY = source.getY() + 4;
		this.setType(type);
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			if (this.tickCount % 2 == 0) {
				level().addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), 0, -0.02D, 0);
			}
			return;
		}
		if (tickCount > 181 || !hasHostAround()) {
			discard();
			return;
		}
		setPos(getX(), Math.max(getY() - 0.015D, floorY), getZ());
		if (target != null && level() instanceof ServerLevel serverLevel && serverLevel.getEntity(target) instanceof Player player && player.isAlive() && !player.isCreative() && !player.isSpectator()) {
			if (tickCount > 45) {
				keepInsideDomain(player);
			}
			if (getDomainType() == 0) {
				if (tickCount % 60 == 0) {
					player.hurt(damageSources().magic(), 3.0F);
					serverLevel.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0D, player.getZ(), 8, 0.35D, 0.5D, 0.35D, 0.08D);
				}
				if (tickCount == 180) {
					player.hurt(damageSources().magic(), 5.0F);
					HerrscherCombatHelper.dealTrueMagicDamage(player, this, 4.0F);
					level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THUNDER, SoundSource.HOSTILE, 0.7F, 1.5F);
					serverLevel.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.2D, player.getZ(), 20, 0.6D, 1.2D, 0.6D, 0.14D);
				}
			}
		}
	}

	@Override
	protected void defineSynchedData() {
		entityData.define(TYPE, 0);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		if (tag.hasUUID(TAG_TARGET)) {
			target = tag.getUUID(TAG_TARGET);
		}
		if (tag.contains(TAG_SOURCE)) {
			source = BlockPos.of(tag.getLong(TAG_SOURCE));
		}
		floorY = tag.getInt(TAG_FLOOR_Y);
		setType(tag.getInt(TAG_TYPE));
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		if (target != null) {
			tag.putUUID(TAG_TARGET, target);
		}
		tag.putLong(TAG_SOURCE, source.asLong());
		tag.putInt(TAG_FLOOR_Y, floorY);
		tag.putInt(TAG_TYPE, getDomainType());
	}

	public int getDomainType() {
		return entityData.get(TYPE);
	}

	public void setType(int type) {
		entityData.set(TYPE, Math.floorMod(type, 10));
	}

	private boolean hasHostAround() {
		AABB area = new AABB(blockPosition()).inflate(20D);
		return !level().getEntitiesOfClass(VoidHerrscher.class, area, Entity::isAlive).isEmpty()
				|| !level().getEntitiesOfClass(GaiaIII.class, area, Entity::isAlive).isEmpty();
	}

	private void keepInsideDomain(Player player) {
		double dx = player.getX() - (source.getX() + 0.5D);
		double dz = player.getZ() - (source.getZ() + 0.5D);
		double horizSq = dx * dx + dz * dz;
		if (horizSq <= 10.5D * 10.5D) {
			return;
		}
		if (horizSq > 18.0D * 18.0D) {
			player.teleportTo(source.getX() + 0.5D, source.getY() + 1.0D, source.getZ() + 0.5D);
			return;
		}
		Vec3 inward = new Vec3(-dx, 0.0D, -dz).normalize().scale(0.45D);
		player.setDeltaMovement(inward.x, 0.15D, inward.z);
		player.hurtMarked = true;
		if (player.getVehicle() != null) {
			player.getVehicle().setDeltaMovement(inward.x, 0.15D, inward.z);
			player.getVehicle().hurtMarked = true;
		}
		if (tickCount % 20 == 0) {
			player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 120, 1));
		}
	}
}
