package io.github.lounode.extrabotany.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.entity.gaia.GaiaIII;
import io.github.lounode.extrabotany.common.entity.gaia.behavior.EgoWeaponFire;

import java.util.List;
import java.util.UUID;

public class EgoMinionEntity extends Monster {
	private static final float DAMAGE_CAP = 20F;
	private static final float ORBIT_RADIUS = 3.5F;
	private static final int STUCK_TELEPORT = 60;
	private static final int HEAL_INTERVAL = 40;
	private static final float HEAL_AMOUNT = 2F;
	private static final float RANGE = 16F;

	private static final EntityDataAccessor<Integer> MINION_TYPE = SynchedEntityData.defineId(EgoMinionEntity.class, EntityDataSerializers.INT);
	private static final String TAG_TYPE = "MinionType";
	private static final String TAG_SUMMONER = "Summoner";

	@Nullable
	private UUID summonerUuid;
	private int attackCooldown;
	private int stuckTicks;

	public EgoMinionEntity(EntityType<? extends EgoMinionEntity> type, Level level) {
		super(type, level);
		this.xpReward = 0;
	}

	public void setSummoner(GaiaIII boss) {
		this.summonerUuid = boss.getUUID();
	}

	private boolean summonerAlive() {
		if (this.summonerUuid == null) {
			return false;
		}
		if (this.level() instanceof net.minecraft.server.level.ServerLevel server) {
			return server.getEntity(this.summonerUuid) instanceof GaiaIII boss && boss.isAlive();
		}
		return true;
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MAX_HEALTH, 60D)
				.add(Attributes.MOVEMENT_SPEED, 0.4D)
				.add(Attributes.ARMOR, 10D)
				.add(Attributes.FOLLOW_RANGE, 32D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, RANGE));
		this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(MINION_TYPE, 0);
	}

	public int getMinionType() {
		return this.entityData.get(MINION_TYPE);
	}

	public void setMinionType(int type) {
		this.entityData.set(MINION_TYPE, type);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (this.level().isClientSide()) {
			return;
		}

		clearHarmfulEffects();
		if (this.level().getDifficulty() == Difficulty.PEACEFUL || !summonerAlive()) {
			this.discard();
			return;
		}

		LivingEntity target = getTarget();
		if (!(target instanceof Player) || !target.isAlive()) {
			target = findNearestPlayer();
			setTarget(target);
		}

		if (target != null) {
			orbit(target);
		}

		if (attackCooldown > 0) {
			attackCooldown--;
		} else if (target instanceof Player player && fireAtTarget(player)) {
			attackCooldown = 90 + this.getRandom().nextInt(40);
		}
	}

	@Nullable
	private Player findNearestPlayer() {
		AABB area = this.getBoundingBox().inflate(RANGE);
		List<Player> players = this.level().getEntitiesOfClass(Player.class, area, p -> p.isAlive() && !p.isSpectator());
		return players.isEmpty() ? null : players.get(0);
	}

	private void orbit(LivingEntity target) {
		Vec3 lookVec = target.getLookAngle().multiply(1, 0, 1);
		if (lookVec.x == 0 && lookVec.z == 0) {
			double rot = Math.toRadians(target.getYRot() + 90);
			lookVec = new Vec3(Math.cos(rot), 0, Math.sin(rot));
		}
		lookVec = lookVec.normalize().multiply(ORBIT_RADIUS, 0, ORBIT_RADIUS);

		if (this.getHealth() <= this.getMaxHealth() * 0.5F) {
			lookVec = lookVec.multiply(-2F, 0, -2F);
			if (this.tickCount % HEAL_INTERVAL == 0) {
				this.heal(HEAL_AMOUNT);
			}
		}

		double angle = Math.PI / 2F * getMinionType() + Math.floor(this.tickCount / 100D) * Math.PI / 4F;
		lookVec = lookVec.yRot((float) angle);
		Vec3 targetPos = target.position().add(lookVec);

		if (this.position().distanceTo(targetPos) >= 0.5F) {
			this.getMoveControl().setWantedPosition(targetPos.x, targetPos.y, targetPos.z, 0.7F);
			stuckTicks++;
		} else {
			stuckTicks = 0;
		}
		if (stuckTicks >= STUCK_TELEPORT) {
			this.teleportTo(targetPos.x, targetPos.y, targetPos.z);
			stuckTicks = 0;
		}
	}

	private boolean fireAtTarget(Player target) {
		this.swing(InteractionHand.MAIN_HAND);
		EgoWeaponFire.fire(this, target, getMinionType() % 4);
		return true;
	}

	private void clearHarmfulEffects() {
		this.getActiveEffects().stream()
				.filter(e -> e.getEffect().value().getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL)
				.map(e -> e.getEffect())
				.toList()
				.forEach(this::removeEffect);
	}

	@Override
	public boolean hurt(@NotNull DamageSource source, float amount) {
		AABB area = this.getBoundingBox().inflate(8D);
		int minions = this.level().getEntitiesOfClass(EgoMinionEntity.class, area).size();
		float resistance = Math.min(0.6F, minions * 0.15F);
		return super.hurt(source, Math.min(DAMAGE_CAP, amount * (1F - resistance)));
	}

	@Override
	public boolean removeWhenFarAway(double dist) {
		return false;
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}

	@Override
	public boolean isPersistenceRequired() {
		return true;
	}

	public static Component pickName(int index) {
		String[] pool = {"ExtraMeteorP", "Vazkii", "Notch", "LexManos"};
		return Component.literal(pool[index % pool.length]);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(TAG_TYPE, getMinionType());
		if (this.summonerUuid != null) {
			tag.putUUID(TAG_SUMMONER, this.summonerUuid);
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		setMinionType(tag.getInt(TAG_TYPE));
		this.summonerUuid = tag.hasUUID(TAG_SUMMONER) ? tag.getUUID(TAG_SUMMONER) : null;
	}
}
