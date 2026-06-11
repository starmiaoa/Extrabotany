package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
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
		this.floorY = source.getY() + 5;
		this.setType(type);
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide()) {
			level().addParticle(ParticleTypes.END_ROD, getX(), getY(), getZ(), 0, 0, 0);
			return;
		}
		if (tickCount > 201 || !hasHostAround()) {
			discard();
			return;
		}
		setPos(getX(), Math.max(getY() - 0.01D, floorY), getZ());
		if (target != null && level() instanceof ServerLevel serverLevel && serverLevel.getEntity(target) instanceof Player player) {
			if (tickCount > 70) {
				keepInsideArena(player);
				double dx = player.getX() - source.getX();
				double dz = player.getZ() - source.getZ();
				if (dx * dx + dz * dz > 20D * 20D) {
					player.teleportTo(source.getX(), source.getY(), source.getZ());
				}
			}
			if (tickCount % 50 == 0) {
				player.hurt(damageSources().magic(), 1F);
			}
			if (tickCount == 200) {
				HerrscherCombatHelper.dealTrueMagicDamage(player, this, 1F);
			}
		}
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(TYPE, 0);
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
		AABB area = new AABB(blockPosition()).inflate(15D);
		return !level().getEntitiesOfClass(VoidHerrscher.class, area, Entity::isAlive).isEmpty()
				|| !level().getEntitiesOfClass(GaiaIII.class, area, Entity::isAlive).isEmpty();
	}

	private void keepInsideArena(Player player) {
		Vec3 center = new Vec3(source.getX() + 0.5D, source.getY() + 0.5D, source.getZ() + 0.5D);
		Vec3 playerCenter = player.position().add(0, player.getBbHeight() * 0.5D, 0);
		Vec3 delta = center.subtract(playerCenter);
		if (delta.lengthSqr() < 4D * 4D) {
			return;
		}
		Vec3 motion = delta.normalize();
		player.setDeltaMovement(motion.x, 0.2D, motion.z);
		player.hurtMarked = true;
		if (player.getVehicle() != null) {
			player.getVehicle().setDeltaMovement(motion.x, 0.2D, motion.z);
			player.getVehicle().hurtMarked = true;
		}
		player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 400, 4));
	}
}
