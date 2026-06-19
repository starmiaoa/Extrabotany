package io.github.lounode.extrabotany.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.Difficulty;
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

import io.github.lounode.extrabotany.common.entity.gaia.behavior.EgoWeaponFire;

import java.util.List;

/**
 * 本我(盖亚III 二阶段 EGO-2)召唤的仆从。忠实移植 1.16 {@code EntityEGOMinion}:
 * <ul>
 *   <li>环绕目标玩家游走(半径 3.5,按类型/时间旋转);卡住 ≥60t 强制传送贴位。</li>
 *   <li>按 minionType(0..3)用对应 relic 远程攻击,冷却 90~130t。</li>
 *   <li>血量 ≤50% 时向反方向逃逸 + 每 40t 自我回血 2。</li>
 *   <li>持续清除自身有害效果;逐击伤害上限 20、按周围仆从数量减伤。</li>
 *   <li>有自定义名(由 Boss 召唤时赋名),和平难度/无宿主即消失。</li>
 * </ul>
 */
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
	private java.util.UUID summonerUuid;
	private int attackCooldown = 0;
	private int stuckTicks = 0;

	public EgoMinionEntity(EntityType<? extends EgoMinionEntity> type, Level level) {
		super(type, level);
		this.xpReward = 0;
	}

	/** 记住召唤者(本我),用于本我消失/死亡后自我清除。 */
	public void setSummoner(io.github.lounode.extrabotany.common.entity.gaia.GaiaIII boss) {
		this.summonerUuid = boss.getUUID();
	}

	// 宿主(本我)是否仍存活。无 UUID 视为已失主。
	private boolean summonerAlive() {
		if (this.summonerUuid == null) {
			return false;
		}
		if (this.level() instanceof net.minecraft.server.level.ServerLevel server) {
			return server.getEntity(this.summonerUuid) instanceof io.github.lounode.extrabotany.common.entity.gaia.GaiaIII boss
					&& boss.isAlive();
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
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(MINION_TYPE, 0);
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
		// 和平难度,或宿主(本我)已消失/死亡 → 仆从随之消失。
		if (this.level().getDifficulty() == Difficulty.PEACEFUL || !summonerAlive()) {
			this.discard();
			return;
		}

		LivingEntity target = getTarget();
		if (target == null || !(target instanceof Player) || !target.isAlive()) {
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
		List<Player> players = this.level().getEntitiesOfClass(Player.class, area,
				p -> p.isAlive() && !p.isSpectator());
		return players.isEmpty() ? null : players.get(0);
	}

	// 绕目标玩家游走:基于玩家朝向偏移一个环绕向量,按 minionType + 时间旋转;
	// 血量 ≤50% 改为向反方向逃逸并自我回血。卡住 ≥60t 直接传送贴到目标位。
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
		this.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
		// 仆从只用 0..3(真影刃/真泰拉/波涌/狂星),不含最初分型(4)。
		EgoWeaponFire.fire(this, target, getMinionType() % 4);
		return true;
	}

	private void clearHarmfulEffects() {
		this.getActiveEffects().stream()
				.filter(e -> e.getEffect().getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL)
				.map(e -> e.getEffect())
				.toList()
				.forEach(this::removeEffect);
	}

	@Override
	public boolean hurt(@NotNull DamageSource source, float amount) {
		// 逐击上限 20,并按 8 格内仆从数量减伤(最多 60%),与 1.16 一致。
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
	public boolean canBeLeashed(Player player) {
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
