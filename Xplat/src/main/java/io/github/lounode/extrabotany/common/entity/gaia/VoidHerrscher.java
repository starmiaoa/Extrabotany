package io.github.lounode.extrabotany.common.entity.gaia;

import com.mojang.serialization.Dynamic;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.helper.PlayerHelper;

import io.github.lounode.extrabotany.api.gaia.GaiaArena;
import io.github.lounode.extrabotany.common.bossevents.ServerGaiaBossEvent;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.entity.*;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import io.github.lounode.extrabotany.common.util.HerrscherCombatHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VoidHerrscher extends Gaia {
	public static final float ARENA_RANGE = 15F;
	public static final int ARENA_HEIGHT = 7;
	public static final float MAX_HP = 500F;
	private static final float DAMAGE_CAP = 28F;
	private static final float RANK_II_THRESHOLD = 0.65F;
	private static final float RANK_III_THRESHOLD = 0.25F;
	private static final float RANK_III_HEAL_RATIO = 0.85F;
	private static final int MAX_ACTIVE_LANCES = 5;

	public static final int RANK_II_TRANSITION_TICKS = 45;
	public static final int RANK_III_TRANSITION_TICKS = 55;
	public static final int JUDGE_CHARGE_TICKS = 40;

	public static final int CAST_NONE = 0;
	public static final int CAST_BARRAGE = 1;
	public static final int CAST_COLLAPSE = 2;
	public static final int CAST_JUDGE = 3;

	private static final DustParticleOptions GOLD_DUST = new DustParticleOptions(new Vector3f(1.0F, 0.84F, 0.31F), 1.25F);
	private static final DustParticleOptions VIOLET_DUST = new DustParticleOptions(new Vector3f(0.70F, 0.23F, 1.0F), 1.35F);
	private static final DustParticleOptions CRIMSON_DUST = new DustParticleOptions(new Vector3f(1.0F, 0.24F, 0.56F), 1.3F);

	private static final Set<String> SUPPORTERS = Set.of("Akasha", "Sirin", "Selene", "Helios", "Aster", "Nyx", "Iris");

	private static final String TAG_SHIELD_LAYERS = "ShieldLayers";
	private static final String TAG_ROTATING_SHIELDS = "RotatingShields";
	private static final String TAG_RANK_II = "RankII";
	private static final String TAG_RANK_III = "RankIII";
	private static final String TAG_DAMAGE_TAKEN = "HerrscherDamageTaken";
	private static final String TAG_TP_DELAY = "HerrscherTpDelay";
	private static final String TAG_DODGE_CD = "DodgeCd";
	private static final String TAG_SKILL_CD = "SkillCd";
	private static final String TAG_SKILL_TYPE = "SkillType";
	private static final String TAG_SUPPORT_CD = "SupportCd";
	private static final String TAG_EMERGE_LANCE_DONE = "EmergeLanceDone";
	private static final String TAG_TRANSITION_TICKS = "TransitionTicks";
	private static final String TAG_TRANSITION_TARGET = "TransitionTarget";
	private static final String TAG_JUDGE_CHARGE = "JudgeCharge";
	private static final String TAG_SUPPORTERS = "Supporters";

	private static final EntityDataAccessor<Integer> ROTATING_SHIELDS = SynchedEntityData.defineId(VoidHerrscher.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> RANK_II = SynchedEntityData.defineId(VoidHerrscher.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> RANK_III = SynchedEntityData.defineId(VoidHerrscher.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> SHIELD_LAYERS_SYNC = SynchedEntityData.defineId(VoidHerrscher.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> TRANSITION_TICKS = SynchedEntityData.defineId(VoidHerrscher.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> TRANSITION_TARGET = SynchedEntityData.defineId(VoidHerrscher.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> CAST_STATE = SynchedEntityData.defineId(VoidHerrscher.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> CAST_TICKS = SynchedEntityData.defineId(VoidHerrscher.class, EntityDataSerializers.INT);

	private int shieldLayers = 3;
	private int rotatingShields = 0;
	private boolean rankII;
	private boolean rankIII;
	private float damageTaken;
	private int tpDelay = 95;
	private int dodgeCd = 240;
	private int skillCd = 140;
	private int skillType = 0;
	private int supportCd = 280;
	private int judgeChargeTicks = 0;
	private int castAnimTicks = 0;
	private boolean emergeLanceDone;
	private final List<String> contributors = new LinkedList<>(SUPPORTERS);

	public VoidHerrscher(EntityType<? extends VoidHerrscher> type, Level world) {
		super(type, world);
		this.xpReward = 2000;
		this.bossEvent = (ServerGaiaBossEvent) new ServerGaiaBossEvent(type.getDescription(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS).setCreateWorldFog(true);
	}

	public VoidHerrscher(EntityType<? extends VoidHerrscher> type, Level world, BlockPos source) {
		super(type, world, source);
	}

	public VoidHerrscher(Level world, BlockPos source) {
		this(ExtraBotanyEntityType.VOID_HERRSCHER, world, source);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MOVEMENT_SPEED, 0.42)
				.add(Attributes.MAX_HEALTH, MAX_HP)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
	}

	public static boolean spawn(Player player, ItemStack stack, Level world, BlockPos pos) {
		GaiaArena arena = GaiaArena.of(GlobalPos.of(world.dimension(), pos), ARENA_RANGE, ARENA_HEIGHT);
		if (!arena.checksModern(player, world, stack)) {
			return false;
		}
		if (arena.countGaiaAround(world, VoidHerrscher.class) > 0) {
			return false;
		}
		if (!arena.checkGuardianInventoryStrict(world, ExtraBotanyItems.firstFractal)) {
			if (!world.isClientSide()) {
				player.sendSystemMessage(Component.translatable("extrabotany.message.guardian_no_response").withStyle(ChatFormatting.RED));
			}
			return false;
		}

		if (!world.isClientSide()) {
			VoidHerrscher boss = new VoidHerrscher(world, pos);
			boss.setArena(arena);
			boss.setPos(pos.getX() + 0.5D, pos.getY() + 3D, pos.getZ() + 0.5D);
			boss.getBrain().setMemoryWithExpiry(MemoryModuleType.IS_EMERGING, Unit.INSTANCE, (long) VoidHerrscherAI.EMERGE_TIME);
			boss.setInvulTime(VoidHerrscherAI.EMERGE_TIME);
			boss.setHealth(1F);
			boss.bossEvent.setProgress(0F);

			List<Player> playersAround = arena.getPlayersAround(world);
			int playerCount = Math.max(1, playersAround.size());
			boss.playerCount = playerCount;
			boss.bossEvent.setPlayerCount(playerCount);
			float healthMultiplier = 1.0F + 0.45F * (playerCount - 1);
			boss.getAttribute(Attributes.MAX_HEALTH).setBaseValue(MAX_HP * healthMultiplier);
			boss.getAttribute(Attributes.ARMOR).setBaseValue(world.getDifficulty() == Difficulty.HARD ? 16 : 8);

			boss.syncAllRenderStates();
			boss.finalizeSpawn((ServerLevelAccessor) world, world.getCurrentDifficultyAt(boss.blockPosition()), MobSpawnType.EVENT, null, null);
			world.addFreshEntity(boss);
			world.playSound(null, pos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 6F, 0.25F);
			world.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 3F, 0.75F);
			for (Player nearbyPlayer : playersAround) {
				if (nearbyPlayer instanceof ServerPlayer serverPlayer) {
					CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, boss);
				}
			}
		}
		return true;
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return VoidHerrscherAI.makeBrain(this, dynamic);
	}

	@Override
	protected void initMemories(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
		VoidHerrscherAI.initMemories(this, level.getLevel(), getHome().pos());
	}

	@Override
	protected void updateAI() {
		VoidHerrscherAI.updateActivity(this);
	}

	@Override
	public int getEmergeTime() {
		return VoidHerrscherAI.EMERGE_TIME;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		entityData.define(ROTATING_SHIELDS, 0);
		entityData.define(RANK_II, false);
		entityData.define(RANK_III, false);
		entityData.define(SHIELD_LAYERS_SYNC, 3);
		entityData.define(TRANSITION_TICKS, 0);
		entityData.define(TRANSITION_TARGET, 0);
		entityData.define(CAST_STATE, CAST_NONE);
		entityData.define(CAST_TICKS, 0);
	}

	@Override
	public void aiStep() {
		super.aiStep();
		if (level().isClientSide()) {
			if (isTransitioning()) {
				spawnTransitionParticles();
			} else if (getCastState() == CAST_JUDGE) {
				spawnJudgeChargeParticles();
			} else if (isRankIIRenderState() && tickCount % 4 == 0) {
				spawnIdleHerrscherAura();
			}
		}
	}

	@Override
	protected void customServerAiStep() {
		super.customServerAiStep();
		if (isNoAi()) {
			return;
		}
		if (level().getDifficulty() == Difficulty.PEACEFUL) {
			discard();
			return;
		}
		tickHerrscher();
	}

	private void tickHerrscher() {
		if (getBrain().hasMemoryValue(MemoryModuleType.IS_EMERGING)) {
			return;
		}

		int trans = getTransitionTicks();
		if (trans > 0) {
			tickPhaseTransition(trans);
			return;
		}

		if (judgeChargeTicks > 0) {
			tickJudgeCharge();
			return;
		}

		if (castAnimTicks > 0) {
			castAnimTicks--;
			entityData.set(CAST_TICKS, castAnimTicks);
			if (castAnimTicks == 0) {
				entityData.set(CAST_STATE, CAST_NONE);
			}
		}

		float healthRatio = getHealth() / Math.max(1F, getMaxHealth());
		if (!rankII && healthRatio <= RANK_II_THRESHOLD) {
			beginRankIITransition();
			return;
		}
		if (rankII && !rankIII && healthRatio <= RANK_III_THRESHOLD) {
			beginRankIIITransition();
			return;
		}

		BlockPos home = getHome().pos();
		if (!emergeLanceDone && tickCount >= VoidHerrscherAI.EMERGE_TIME + 70) {
			spawnLance(home.getX() + 0.5D, home.getY() + 12D, home.getZ() + 0.5D, 3.5F, 720);
			emergeLanceDone = true;
		}

		if (rotatingShields >= 3) {
			reflectProjectiles();
		}

		Player focus = nearestPlayer();
		if (focus != null) {
			faceTarget(focus);
		}

		for (Player player : getPlayersAround()) {
			if (!player.getAbilities().instabuild && player.getY() > home.getY() + 4.5D && player.getDeltaMovement().y > 0.05D) {
				Vec3 v = player.getDeltaMovement();
				player.setDeltaMovement(v.x, -0.18D, v.z);
				player.hurtMarked = true;
			}
			clearPotions(player);
			int amplifier = (rankIII ? 2 : rankII ? 1 : 0) + (tickCount >= 2400 ? 1 : 0);
			if (tickCount % 40 == 0) {
				player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.WITCH_CURSE, 120, amplifier, true, true));
			}
			if (player.distanceToSqr(home.getX() + 0.5D, player.getY(), home.getZ() + 0.5D) > ARENA_RANGE * ARENA_RANGE) {
				player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 200, 3));
			}
		}

		double desiredY = home.getY() + (rankIII ? 3.2D : rankII ? 2.4D : 1.0D) + Math.sin(tickCount * 0.08D) * 0.35D;
		if (rankII) {
			double dy = desiredY - getY();
			setDeltaMovement(getDeltaMovement().x * 0.82D, Mth.clamp(dy * 0.14D, -0.25D, 0.28D), getDeltaMovement().z * 0.82D);
		}

		int barrageInterval = rankIII ? 62 : rankII ? 70 : 80;
		if (tickCount % barrageInterval == 0) {
			spawnSubspaceFanBarrage();
		}

		int missileInterval = rankIII ? 70 : rankII ? 85 : 100;
		if (tickCount > 60 && tickCount % missileInterval == 0) {
			spawnMissile(rankIII ? 2 : rankII ? 1 : 0);
			if (rankIII) {
				healFinite(0.6F);
			}
		}

		if (rankII && tickCount > 240 && tickCount % (rankIII ? 260 : 320) == 0) {
			spawnLandmines();
		}

		if (--tpDelay <= 0) {
			tacticalTeleport();
			tpDelay = rankIII ? 80 : rankII ? 95 : 110;
		}
		if (dodgeCd > 0) {
			dodgeCd--;
		}
		if (--skillCd <= 0) {
			runSkill();
		} else if (skillCd == 60) {
			warnSkill();
		}
		if (rankIII && --supportCd <= 0) {
			supportCd = 320;
			runSupport();
		}
	}

	private void beginRankIITransition() {
		rankII = true;
		entityData.set(TRANSITION_TARGET, 2);
		entityData.set(TRANSITION_TICKS, RANK_II_TRANSITION_TICKS);
		setInvulTime(RANK_II_TRANSITION_TICKS + 10);
		setDeltaMovement(Vec3.ZERO);
		BlockPos home = getHome().pos();
		teleportTo(home.getX() + 0.5D, home.getY() + 2.2D, home.getZ() + 0.5D);
		playSound(SoundEvents.BEACON_POWER_SELECT, 2.5F, 0.7F);
		playSound(SoundEvents.ENDERMAN_TELEPORT, 1.5F, 0.6F);
		broadcast("extrabotany.message.herrscher.rank2");
	}

	private void beginRankIIITransition() {
		rankIII = true;
		entityData.set(TRANSITION_TARGET, 3);
		entityData.set(TRANSITION_TICKS, RANK_III_TRANSITION_TICKS);
		setInvulTime(RANK_III_TRANSITION_TICKS + 15);
		setDeltaMovement(Vec3.ZERO);
		BlockPos home = getHome().pos();
		teleportTo(home.getX() + 0.5D, home.getY() + 2.6D, home.getZ() + 0.5D);
		playSound(SoundEvents.WITHER_SPAWN, 1.6F, 1.25F);
		playSound(SoundEvents.BEACON_ACTIVATE, 2.5F, 0.85F);
		broadcast("extrabotany.message.herrscher.rank3");
	}

	private void tickPhaseTransition(int remaining) {
		int targetRank = getTransitionTargetRank();
		int total = targetRank >= 3 ? RANK_III_TRANSITION_TICKS : RANK_II_TRANSITION_TICKS;
		float progress = Mth.clamp(1.0F - (remaining - 1) / (float) total, 0.0F, 1.0F);
		BlockPos home = getHome().pos();
		double targetY = home.getY() + (targetRank >= 3 ? 4.4D : 3.4D);
		double hoverY = Mth.lerp(progress, home.getY() + 2.2D, targetY);
		setPos(home.getX() + 0.5D, hoverY, home.getZ() + 0.5D);
		setDeltaMovement(Vec3.ZERO);
		setInvulTime(Math.max(getInvulTime(), remaining + 4));

		if (targetRank >= 3 && !getPlayersWhoAttacked().isEmpty()) {
			float targetHp = getMaxHealth() * RANK_III_HEAL_RATIO;
			if (getHealth() < targetHp) {
				float step = (targetHp - getHealth()) / Math.max(1, remaining);
				setHealth(Math.min(targetHp, getHealth() + Math.max(step, 2.0F)));
			}
		}

		if (remaining == total / 2) {
			playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 2.0F, 0.75F);
		}

		int next = remaining - 1;
		entityData.set(TRANSITION_TICKS, next);
		if (next <= 0) {
			finishPhaseTransition(targetRank);
		}
	}

	private void finishPhaseTransition(int targetRank) {
		entityData.set(TRANSITION_TARGET, 0);
		BlockPos home = getHome().pos();

		if (targetRank == 2) {
			entityData.set(RANK_II, true);
			shieldLayers = 3;
			rotatingShields = Math.max(rotatingShields, 2);
			syncAllRenderStates();
			pushNearbyPlayers(home, 6.5D, 0.95D);
			spawnRingLances(2, 6.5D, 750);
			skillCd = 90;
			skillType = 0;
		} else {
			entityData.set(RANK_II, true);
			entityData.set(RANK_III, true);
			shieldLayers = 4;
			rotatingShields = 3;
			if (!getPlayersWhoAttacked().isEmpty()) {
				setHealth(Math.max(getHealth(), getMaxHealth() * RANK_III_HEAL_RATIO));
			}
			syncAllRenderStates();
			pushNearbyPlayers(home, 8.0D, 1.2D);
			spawnRingLances(4, 7.5D, 800);
			skillCd = 110;
			skillType = 3;
		}

		playSound(SoundEvents.GENERIC_EXPLODE, 2.2F, 0.8F);
		playSound(SoundEvents.TRIDENT_THUNDER, 1.8F, 1.1F);
		if (level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(GOLD_DUST, getX(), getY() + 1.0D, getZ(), 45, 2.2D, 1.2D, 2.2D, 0.12D);
			serverLevel.sendParticles(VIOLET_DUST, getX(), getY() + 1.0D, getZ(), 45, 2.2D, 1.2D, 2.2D, 0.12D);
			serverLevel.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.0D, getZ(), 32, 1.8D, 1.0D, 1.8D, 0.25D);
		}
	}

	private void pushNearbyPlayers(BlockPos home, double range, double strength) {
		for (Player player : getPlayersAround()) {
			if (player.isCreative() || player.isSpectator()) {
				continue;
			}
			Vec3 away = player.position().subtract(home.getX() + 0.5D, player.getY(), home.getZ() + 0.5D);
			double dist = away.length();
			if (dist < range) {
				Vec3 dir = dist > 0.1D ? away.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
				player.setDeltaMovement(dir.x * strength, 0.42D, dir.z * strength);
				player.hurtMarked = true;
			}
		}
	}

	private void startJudgeCharge() {
		judgeChargeTicks = JUDGE_CHARGE_TICKS;
		entityData.set(CAST_STATE, CAST_JUDGE);
		entityData.set(CAST_TICKS, JUDGE_CHARGE_TICKS);
		setInvulTime(JUDGE_CHARGE_TICKS + 15);
		BlockPos home = getHome().pos();
		teleportTo(home.getX() + 0.5D, home.getY() + 4.0D, home.getZ() + 0.5D);
		setDeltaMovement(Vec3.ZERO);
		Player focus = nearestPlayer();
		if (focus != null) {
			faceTarget(focus);
		}
		playSound(SoundEvents.BEACON_AMBIENT, 2.0F, 1.5F);
		playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 2.0F, 0.65F);
		broadcast("extrabotany.message.herrscher.skill_judge");
	}

	private void tickJudgeCharge() {
		BlockPos home = getHome().pos();
		setPos(home.getX() + 0.5D, home.getY() + 4.0D + Math.sin(tickCount * 0.2D) * 0.18D, home.getZ() + 0.5D);
		setDeltaMovement(Vec3.ZERO);
		Player focus = nearestPlayer();
		if (focus != null) {
			faceTarget(focus);
		}
		judgeChargeTicks--;
		entityData.set(CAST_TICKS, judgeChargeTicks);
		if (judgeChargeTicks == 10) {
			playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 2.4F, 1.65F);
		}
		if (judgeChargeTicks <= 0) {
			entityData.set(CAST_STATE, CAST_NONE);
			executeVoidJudge();
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (!HerrscherCombatHelper.isFinitePositive(amount)) {
			return false;
		}
		if (source.is(DamageTypes.GENERIC_KILL)) {
			return super.hurt(source, amount);
		}
		Entity attacker = source.getEntity();
		if (!(attacker instanceof Player player) || !PlayerHelper.isTruePlayer(player) || getInvulTime() > 0 || isTransitioning()) {
			return false;
		}
		BlockPos home = getHome().pos();
		if (player.distanceToSqr(home.getX() + 0.5D, player.getY(), home.getZ() + 0.5D) > ARENA_RANGE * ARENA_RANGE) {
			player.teleportTo(home.getX() + 0.5D, home.getY() + 1D, home.getZ() + 0.5D);
			return false;
		}
		markPlayerAttacked(player);

		if (shieldLayers > 0) {
			shieldLayers--;
			syncShieldLayers();
			playSound(SoundEvents.AMETHYST_CLUSTER_BREAK, 1.2F, 1.25F + random.nextFloat() * 0.25F);
			if (level() instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(GOLD_DUST, getX(), getY() + 1.1D, getZ(), 14, 0.55D, 0.7D, 0.55D, 0.08D);
				serverLevel.sendParticles(VIOLET_DUST, getX(), getY() + 1.1D, getZ(), 14, 0.55D, 0.7D, 0.55D, 0.08D);
			}
			setInvulTime(4);
			return false;
		}

		if (dodgeCd <= 0) {
			HerrscherVoidFieldEntity field = new HerrscherVoidFieldEntity(level());
			field.setPos(player.getX(), player.getY(), player.getZ());
			field.configure(HerrscherVoidFieldEntity.MODE_TIME_LOCK, 2.8F, 65, 9.0F, this);
			level().addFreshEntity(field);
			dodgeCd = 300;
			tacticalTeleport();
			playSound(SoundEvents.ENDERMAN_TELEPORT, 1.3F, 1.4F);
			spawnTargetedSpearPortal(player, 10.5F, 2.0D);
			return false;
		}

		float adjusted = amount;
		for (int i = 0; i < rotatingShields; i++) {
			adjusted *= 0.88F;
		}
		if (rotatingShields > 0) {
			if (adjusted > 18F) {
				rotatingShields = Math.max(0, rotatingShields - 1);
				syncRotatingShields();
				playSound(SoundEvents.GLASS_BREAK, 1.4F, 0.9F);
				playSound(SoundEvents.AMETHYST_BLOCK_BREAK, 1.4F, 1.1F);
				if (level() instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(GOLD_DUST, getX(), getY() + 1.2D, getZ(), 24, 0.9D, 0.9D, 0.9D, 0.14D);
					serverLevel.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.2D, getZ(), 16, 0.8D, 0.8D, 0.8D, 0.18D);
				}
				broadcast("extrabotany.message.herrscher.shield_break");
			}
		}

		float capped = Math.min(getDamageCap(), adjusted);
		damageTaken += capped;
		if (damageTaken >= 95F) {
			damageTaken = 0F;
			HerrscherVoidFieldEntity collapse = new HerrscherVoidFieldEntity(level());
			collapse.setPos(getX(), Math.max(home.getY() + 0.1D, player.getY()), getZ());
			collapse.configure(HerrscherVoidFieldEntity.MODE_COLLAPSE, 3.4F, 42, rankIII ? 11.5F : 9.5F, this);
			level().addFreshEntity(collapse);
			tacticalTeleport();
			tpDelay = 75;
		}
		if (skillCd > 75) {
			skillCd -= 10;
		}

		boolean result = super.hurt(source, capped);
		setInvulTime(Math.max(getInvulTime(), 6));
		Vec3 away = position().subtract(player.position());
		if (away.lengthSqr() > 1.0E-6D) {
			Vec3 motion = away.normalize().scale(0.18D);
			setDeltaMovement(motion.x, 0.18D, motion.z);
		}
		return result;
	}

	@Override
	protected void actuallyHurt(DamageSource source, float amount) {
		if (source.is(DamageTypes.GENERIC_KILL)) {
			super.actuallyHurt(source, amount);
			return;
		}
		if (HerrscherCombatHelper.isFinitePositive(amount)) {
			tpDelay = Math.max(tpDelay - 3, 0);
			super.actuallyHurt(source, Math.min(getDamageCap(), amount));
		}
	}

	@Override
	public void heal(float amount) {
		healFinite(amount);
	}

	private void healFinite(float amount) {
		if (HerrscherCombatHelper.isFinitePositive(amount)) {
			super.heal(amount);
		}
	}

	private void setCastPose(int state, int ticks) {
		this.castAnimTicks = ticks;
		this.entityData.set(CAST_STATE, state);
		this.entityData.set(CAST_TICKS, ticks);
	}

	private void spawnSubspaceFanBarrage() {
		Player target = nearestPlayer();
		if (target == null) {
			return;
		}
		faceTarget(target);
		setCastPose(CAST_BARRAGE, 22);

		int portalCount = rankIII ? 7 : rankII ? 5 : 3;
		Vec3 toTarget = target.position().add(0, target.getBbHeight() * 0.5D, 0).subtract(position().add(0, 1.8D, 0));
		Vec3 horiz = new Vec3(toTarget.x, 0.0D, toTarget.z);
		if (horiz.lengthSqr() < 1.0E-4D) {
			horiz = getLookAngle().multiply(1D, 0D, 1D);
		}
		horiz = horiz.normalize();
		Vec3 right = new Vec3(-horiz.z, 0.0D, horiz.x);

		float facingYaw = (float) (Mth.atan2(-horiz.x, horiz.z) * Mth.RAD_TO_DEG);
		float dmg = rankIII ? 11.5F : rankII ? 10.5F : 9.5F;
		double speed = rankIII ? 2.15D : rankII ? 2.0D : 1.85D;

		for (int i = 0; i < portalCount; i++) {
			float frac = portalCount == 1 ? 0.0F : ((float) i / (portalCount - 1)) * 2.0F - 1.0F;
			double sideOffset = frac * (rankIII ? 3.2D : 2.5D);
			double heightOffset = 1.9D + (1.0D - Math.abs(frac)) * 1.15D;
			double backOffset = -0.6D - Math.abs(frac) * 0.65D;

			Vec3 spawnPos = position()
					.add(right.scale(sideOffset))
					.add(horiz.scale(backOffset))
					.add(0.0D, heightOffset, 0.0D);

			Vec3 aim = target.position().add(0, target.getBbHeight() * 0.5D, 0)
					.add(target.getDeltaMovement().scale(4.5D))
					.subtract(spawnPos.add(0.0D, -0.75D, 0.0D));

			SubspaceEntity portal = new SubspaceEntity(level(), this);
			int delay = 6 + Math.abs(i - portalCount / 2) * 3;
			portal.configure(SubspaceEntity.TYPE_SPEAR, 26, delay, 10, 0.50F + random.nextFloat() * 0.14F, Mth.wrapDegrees(-facingYaw + 180F));
			portal.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
			portal.setYRot(facingYaw);
			portal.setAimDirection(aim);
			portal.setSpearDamage(dmg);
			portal.setSpearSpeed(speed);
			level().addFreshEntity(portal);
		}
	}

	private void spawnTargetedSpearPortal(Player target, float damage, double speed) {
		Vec3 spawnPos = position().add((random.nextDouble() - 0.5D) * 2.0D, 2.2D, (random.nextDouble() - 0.5D) * 2.0D);
		Vec3 aim = target.position().add(0, target.getBbHeight() * 0.5D, 0).subtract(spawnPos.add(0, -0.75D, 0));
		float yaw = (float) (Mth.atan2(-aim.x, aim.z) * Mth.RAD_TO_DEG);
		SubspaceEntity portal = new SubspaceEntity(level(), this);
		portal.configure(SubspaceEntity.TYPE_SPEAR, 28, 6, 10, 0.55F, Mth.wrapDegrees(-yaw + 180F));
		portal.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
		portal.setYRot(yaw);
		portal.setAimDirection(aim);
		portal.setSpearDamage(damage);
		portal.setSpearSpeed(speed);
		level().addFreshEntity(portal);
	}

	private void spawnCrossfirePortals() {
		Player target = randomPlayer();
		if (target == null) {
			return;
		}
		setCastPose(CAST_BARRAGE, 28);
		int count = rankIII ? 10 : rankII ? 8 : 6;
		BlockPos home = getHome().pos();
		double radius = 9.5D;
		double baseAngle = random.nextDouble() * Mth.TWO_PI;
		for (int i = 0; i < count; i++) {
			double a = baseAngle + i * (Mth.TWO_PI / count);
			double px = home.getX() + 0.5D + Math.cos(a) * radius;
			double pz = home.getZ() + 0.5D + Math.sin(a) * radius;
			double py = home.getY() + 2.8D + (i % 2) * 1.1D;
			Vec3 aim = target.position().add(0, target.getBbHeight() * 0.5D, 0).subtract(px, py - 0.75D, pz);
			float yaw = (float) (Mth.atan2(-aim.x, aim.z) * Mth.RAD_TO_DEG);
			SubspaceEntity portal = new SubspaceEntity(level(), this);
			portal.configure(SubspaceEntity.TYPE_SPEAR, 30, 8 + i * 2, 10, 0.72F, Mth.wrapDegrees(-yaw + 180F));
			portal.setPos(px, py, pz);
			portal.setYRot(yaw);
			portal.setAimDirection(aim);
			portal.setSpearDamage(rankIII ? 11.0F : 9.5F);
			portal.setSpearSpeed(1.85D);
			level().addFreshEntity(portal);
		}
		playSound(ExtraBotanySounds.SPEAR_OF_SUBSPACE_USE, 1.0F, 1.15F);
	}

	private int countActiveLances() {
		return level().getEntitiesOfClass(SubspaceLanceEntity.class, getBoundingBox().inflate(32D),
				lance -> lance.isAlive() && lance.getOwner() == this).size();
	}

	private void spawnRingLances(int count, double radius, int life) {
		BlockPos home = getHome().pos();
		double offset = random.nextDouble() * Mth.TWO_PI;
		for (int i = 0; i < count; i++) {
			if (countActiveLances() >= MAX_ACTIVE_LANCES) {
				break;
			}
			double a = offset + i * (Mth.TWO_PI / count);
			double x = home.getX() + 0.5D + Math.cos(a) * radius;
			double z = home.getZ() + 0.5D + Math.sin(a) * radius;
			spawnLance(x, home.getY() + 12.5D, z, 3.8F, life);
		}
	}

	private void spawnNearTargetLance(Vec3 targetPos) {
		if (countActiveLances() >= MAX_ACTIVE_LANCES) {
			return;
		}
		BlockPos home = getHome().pos();
		double a = random.nextDouble() * Mth.TWO_PI;
		double dist = 1.6D + random.nextDouble() * 1.4D;
		double x = Mth.clamp(targetPos.x + Math.cos(a) * dist, home.getX() - ARENA_RANGE + 2.0D, home.getX() + ARENA_RANGE - 2.0D);
		double z = Mth.clamp(targetPos.z + Math.sin(a) * dist, home.getZ() - ARENA_RANGE + 2.0D, home.getZ() + ARENA_RANGE - 2.0D);
		spawnLance(x, home.getY() + 13D, z, 4.0F, 680);
	}

	private void spawnLance(double x, double y, double z, float damage, int life) {
		SubspaceLanceEntity lance = new SubspaceLanceEntity(level(), damage, life);
		lance.setOwner(this);
		lance.setPos(x, y, z);
		level().addFreshEntity(lance);
	}

	private void spawnLandmines() {
		List<Player> players = getPlayersAround();
		int count = (rankIII ? 7 : 5) + Math.max(0, players.size() - 1) * 2;
		BlockPos home = getHome().pos();
		for (int i = 0; i < count; i++) {
			SkullLandMineEntity mine;
			if (i % 5 == 0) {
				mine = new SkullLandMineEntity.Danger(level(), this);
			} else if (i % 7 == 0) {
				mine = new SkullLandMineEntity.Disarm(level(), this);
			} else {
				mine = new SkullLandMineEntity.Default(level(), this);
			}
			mine.setDamage(6F);
			double x = home.getX() + 0.5D + (random.nextDouble() - 0.5D) * 18D;
			double z = home.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 18D;
			double y = players.isEmpty() ? home.getY() + 1D : players.get(random.nextInt(players.size())).getY();
			mine.setPos(x, y, z);
			level().addFreshEntity(mine);
		}
	}

	private void spawnMissile(int type) {
		Player target = randomPlayer();
		if (target == null) {
			return;
		}
		SkullMissileEntity missile = new SkullMissileEntity(level(), this);
		missile.setTarget(target);
		missile.setFire(type >= 1);
		missile.setDamage(level().getDifficulty() == Difficulty.HARD ? 6.5F : 5F);
		if (type >= 2) {
			missile.setEffect(true);
			missile.setTrueDamage(level().getDifficulty() == Difficulty.HARD ? 2.5F : 2F);
		}
		missile.setPos(getX() + (random.nextDouble() - 0.5D) * 0.2D, getY() + 1.8D, getZ() + (random.nextDouble() - 0.5D) * 0.2D);
		if (missile.findTarget()) {
			playSound(BotaniaSounds.missile, 0.55F, 0.85F + random.nextFloat() * 0.2F);
			level().addFreshEntity(missile);
		}
	}

	private void spawnSwordDomain() {
		List<UUID> targets = new ArrayList<>(getPlayersWhoAttacked());
		if (targets.isEmpty()) {
			getPlayersAround().forEach(player -> targets.add(player.getUUID()));
		}
		BlockPos home = getHome().pos();
		int count = rankIII ? 10 : 8;
		double radius = 6.5D;
		for (int i = 0; i < count; i++) {
			double angle = i * (Mth.TWO_PI / count);
			UUID target = targets.isEmpty() ? null : targets.get(Math.min(i, targets.size() - 1));
			SwordDomainEntity sword = new SwordDomainEntity(level(), target, home, i);
			sword.setPos(home.getX() + 0.5D + Math.cos(angle) * radius, home.getY() + 7.5D, home.getZ() + 0.5D + Math.sin(angle) * radius);
			level().addFreshEntity(sword);
		}
		playSound(SoundEvents.BEACON_ACTIVATE, 1.4F, 1.35F);
		broadcast("extrabotany.message.herrscher.skill_swords");
	}

	private void runSkill() {
		switch (skillType) {
			case 0 -> {
				setCastPose(CAST_COLLAPSE, 24);
				List<Player> players = getPlayersAround().stream().filter(p -> p.isAlive() && !p.isCreative() && !p.isSpectator()).toList();
				if (!players.isEmpty()) {
					Player target = players.get(random.nextInt(players.size()));
					spawnCollapseField(target.position(), 3.4F);
					if (rankII) {
						Vec3 lead = target.position().add(target.getDeltaMovement().scale(12.0D));
						spawnCollapseField(lead, 3.0F);
					}
					spawnNearTargetLance(target.position());
				}
				skillCd = rankIII ? 210 : rankII ? 235 : 260;
				skillType = 1;
			}
			case 1 -> {
				spawnCrossfirePortals();
				skillCd = rankIII ? 200 : rankII ? 230 : 250;
				skillType = rankII ? 2 : 0;
			}
			case 2 -> {
				if (!rankII) {
					skillCd = 180;
					skillType = 0;
					return;
				}
				spawnSwordDomain();
				skillCd = rankIII ? 220 : 260;
				skillType = 3;
			}
			default -> {
				if (!rankII) {
					skillCd = 180;
					skillType = 0;
					return;
				}
				startJudgeCharge();
				skillCd = rankIII ? 270 : 320;
				skillType = 0;
			}
		}
	}

	private void spawnCollapseField(Vec3 pos, float radius) {
		BlockPos home = getHome().pos();
		double x = Mth.clamp(pos.x, home.getX() - ARENA_RANGE + 2.0D, home.getX() + ARENA_RANGE - 2.0D);
		double z = Mth.clamp(pos.z, home.getZ() - ARENA_RANGE + 2.0D, home.getZ() + ARENA_RANGE - 2.0D);
		HerrscherVoidFieldEntity field = new HerrscherVoidFieldEntity(level());
		field.setPos(x, Math.max(home.getY() + 0.05D, pos.y), z);
		field.configure(HerrscherVoidFieldEntity.MODE_COLLAPSE, radius, 42, rankIII ? 11.5F : 9.5F, this);
		level().addFreshEntity(field);
	}

	private void executeVoidJudge() {
		BlockPos home = getHome().pos();
		Player focus = nearestPlayer();
		Vec3 forward;
		if (focus != null) {
			faceTarget(focus);
			Vec3 diff = focus.position().subtract(position()).multiply(1D, 0D, 1D);
			forward = diff.lengthSqr() > 1.0E-4D ? diff.normalize() : new Vec3(0D, 0D, 1D);
		} else {
			forward = new Vec3(0D, 0D, 1D);
		}
		Vec3 right = new Vec3(-forward.z, 0D, forward.x);
		float portalYaw = (float) (Mth.atan2(-forward.x, forward.z) * Mth.RAD_TO_DEG);

		playSound(ExtraBotanySounds.SPEAR_OF_SUBSPACE_USE, 1.8F, 0.9F);
		playSound(SoundEvents.AMETHYST_CLUSTER_BREAK, 2.2F, 1.65F);
		playSound(SoundEvents.BEACON_DEACTIVATE, 2.0F, 1.35F);
		if (level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(GOLD_DUST, getX(), getY() + 1.4D, getZ(), 40, 2.0D, 1.2D, 2.0D, 0.14D);
			serverLevel.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1.4D, getZ(), 30, 1.6D, 1.0D, 1.6D, 0.22D);
		}

		int outerCount = rankIII ? 12 : 9;
		int innerCount = rankIII ? 10 : 7;
		spawnJudgePortalArc(outerCount, 7.2D, -3.2D, 2.2D, forward, right, portalYaw, 2.4F, 10);
		spawnJudgePortalArc(innerCount, 4.8D, -1.8D, 1.3D, forward, right, portalYaw, 1.9F, 4);

		List<Player> players = getPlayersAround().stream().filter(p -> p.isAlive() && !p.isCreative() && !p.isSpectator()).toList();
		for (int i = 0; i < Math.min(2, players.size() + 1); i++) {
			if (!players.isEmpty()) {
				Player target = players.get(i % players.size());
				Vec3 offset = i == 0 ? Vec3.ZERO : new Vec3((random.nextDouble() - 0.5D) * 5.0D, 0D, (random.nextDouble() - 0.5D) * 5.0D);
				spawnCollapseField(target.position().add(offset), 3.2F);
			}
		}

		setPos(home.getX() + 0.5D, home.getY() + 4.0D, home.getZ() + 0.5D);
	}

	private void spawnJudgePortalArc(int count, double arcRadius, double backDist, double baseYOffset,
			Vec3 forward, Vec3 right, float portalYaw, float portalSize, int baseDelay) {
		for (int i = 0; i < count; i++) {
			double frac = count == 1 ? 0.5D : (double) i / (count - 1);
			double angle = -Math.PI * 0.44D + frac * (Math.PI * 0.88D);
			double side = Math.sin(angle) * arcRadius;
			double up = Math.cos(angle) * arcRadius * 0.72D + baseYOffset;
			Vec3 pos = position()
					.add(forward.scale(backDist))
					.add(right.scale(side))
					.add(0.0D, up, 0.0D);

			SubspaceEntity portal = new SubspaceEntity(level(), this);
			portal.configure(
					SubspaceEntity.TYPE_DOMAIN,
					92,
					baseDelay + (i % 4) * 4 + random.nextInt(4),
					rankIII ? 18 : 22,
					portalSize + random.nextFloat() * 0.35F,
					Mth.wrapDegrees(-portalYaw + 180F));
			portal.setPos(pos.x, pos.y, pos.z);
			portal.setYRot(portalYaw);
			portal.setAimDirection(forward.add(0.0D, -0.35D, 0.0D));
			portal.setSpearDamage(rankIII ? 10.5F : 9.0F);
			portal.setSpearSpeed(1.38D);
			portal.setMaxShots(3);
			level().addFreshEntity(portal);
		}
	}

	private void runSupport() {
		List<Player> players = getPlayersAround().stream().filter(Player::isAlive).toList();
		if (players.isEmpty() || contributors.isEmpty()) {
			return;
		}
		int index = random.nextInt(contributors.size());
		String supporter = contributors.remove(index);
		Player player = players.get(random.nextInt(players.size()));
		int effect = random.nextInt(7);
		int variant = 0;
		switch (effect) {
			case 0 -> {
				variant = random.nextInt(2);
				player.heal(player.getMaxHealth() * 0.25F);
			}
			case 1 -> {
				if (shieldLayers > 0) {
					shieldLayers--;
					syncShieldLayers();
				} else {
					super.hurt(damageSources().playerAttack(player), 18F);
				}
			}
			case 2 -> tpDelay += 160;
			case 3 -> {
				variant = random.nextInt(2);
				skillCd += 110;
			}
			case 4 -> {
				variant = random.nextInt(3);
				supportCd = 200;
			}
			case 5 -> {
				variant = random.nextInt(2);
				player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 4));
			}
			case 6 -> {
				variant = random.nextInt(3);
				applyCandy(player, variant);
				applyCandy(player, variant);
			}
			default -> {}
		}
		player.sendSystemMessage(Component.translatable("extrabotany.message.herrscher.support" + effect + variant, supporter).withStyle(ChatFormatting.AQUA));
	}

	private void applyCandy(Player player, int variant) {
		var effect = switch (variant) {
			case 1 -> MobEffects.JUMP;
			case 2 -> MobEffects.DIG_SPEED;
			default -> MobEffects.MOVEMENT_SPEED;
		};
		if (player.getHealth() < player.getMaxHealth()) {
			player.heal(4.0F);
		}
		player.addEffect(new MobEffectInstance(effect, 200, 1));
	}

	private void warnSkill() {
		if (skillType == 3 && rankII) {
			broadcast("extrabotany.message.herrscher.warning_judge");
		} else if (skillType == 0) {
			broadcast("extrabotany.message.herrscher.warning_collapse");
		} else {
			broadcast("extrabotany.message.herrscher.warning_target");
		}
	}

	private void clearPotions(Player player) {
		var toRemove = player.getActiveEffects().stream()
				.filter(effect -> effect.getDuration() < 160 && effect.isAmbient() && effect.getEffect().isBeneficial())
				.map(MobEffectInstance::getEffect)
				.distinct()
				.toList();
		for (var effect : toRemove) {
			player.removeEffect(effect);
		}
	}

	private void tacticalTeleport() {
		BlockPos home = getHome().pos();
		Player nearest = nearestPlayer();
		double angle;
		if (nearest != null) {
			double playerAngle = Mth.atan2(nearest.getZ() - (home.getZ() + 0.5D), nearest.getX() - (home.getX() + 0.5D));
			angle = playerAngle + Math.PI * (0.55D + random.nextDouble() * 0.9D);
		} else {
			angle = random.nextDouble() * Mth.TWO_PI;
		}
		double dist = 5.0D + random.nextDouble() * 5.0D;
		double x = home.getX() + 0.5D + Math.cos(angle) * dist;
		double y = home.getY() + (rankIII ? 3.2D : rankII ? 2.4D : 1.0D);
		double z = home.getZ() + 0.5D + Math.sin(angle) * dist;

		if (level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(VIOLET_DUST, getX(), getY() + 1.0D, getZ(), 12, 0.4D, 0.8D, 0.4D, 0.05D);
			serverLevel.sendParticles(GOLD_DUST, x, y + 1.0D, z, 12, 0.4D, 0.8D, 0.4D, 0.05D);
		}
		teleportTo(x, y, z);
		playSound(SoundEvents.ENDERMAN_TELEPORT, 0.75F, 1.35F);
	}

	private void reflectProjectiles() {
		for (Projectile projectile : level().getEntitiesOfClass(Projectile.class, getBoundingBox().inflate(2.6D))) {
			if (projectile.getOwner() == this || projectile instanceof SubspaceSpearEntity || projectile instanceof SkullMissileEntity) {
				continue;
			}
			Vec3 away = projectile.position().subtract(position().add(0, 1.0D, 0));
			Vec3 reflect = away.lengthSqr() > 1.0E-4D ? away.normalize().scale(1.5D) : getLookAngle().scale(1.5D);
			projectile.setDeltaMovement(reflect);
			projectile.hurtMarked = true;
			playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.6F, 1.8F);
		}
	}

	private void faceTarget(Player target) {
		double dx = target.getX() - getX();
		double dz = target.getZ() - getZ();
		float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
		setYRot(yaw);
		setYHeadRot(yaw);
		yBodyRot = yaw;
	}

	@Nullable
	private Player nearestPlayer() {
		return getPlayersAround().stream()
				.filter(p -> p.isAlive() && !p.isSpectator())
				.min(Comparator.comparingDouble(this::distanceToSqr))
				.orElse(null);
	}

	@Nullable
	private Player randomPlayer() {
		List<Player> players = getPlayersAround().stream().filter(p -> p.isAlive() && !p.isSpectator()).toList();
		return players.isEmpty() ? null : players.get(random.nextInt(players.size()));
	}

	private void broadcast(String key) {
		getPlayersAround().forEach(player -> player.sendSystemMessage(Component.translatable(key).withStyle(ChatFormatting.LIGHT_PURPLE)));
	}

	private void spawnTransitionParticles() {
		float progress = getTransitionProgress(0.0F);
		double radius = Mth.lerp(progress, 3.8D, 1.1D);
		double baseAngle = tickCount * 0.42D;
		for (int i = 0; i < 3; i++) {
			double a = baseAngle + i * (Mth.TWO_PI / 3.0D);
			double yOff = ((tickCount * 0.16D + i * 0.9D) % 3.2D);
			level().addParticle(i == 0 ? GOLD_DUST : VIOLET_DUST,
					getX() + Math.cos(a) * radius,
					getY() + yOff,
					getZ() + Math.sin(a) * radius,
					-Math.cos(a) * 0.06D, 0.10D, -Math.sin(a) * 0.06D);
		}
		if (progress > 0.65F) {
			level().addParticle(ParticleTypes.END_ROD,
					getX() + (random.nextDouble() - 0.5D) * 1.4D,
					getY() + 0.5D + random.nextDouble() * 1.6D,
					getZ() + (random.nextDouble() - 0.5D) * 1.4D,
					0.0D, 0.12D, 0.0D);
		}
	}

	private void spawnJudgeChargeParticles() {
		double angle = tickCount * 0.55D;
		for (int i = 0; i < 4; i++) {
			double a = angle + i * (Math.PI * 0.5D);
			double r = 2.6D;
			level().addParticle(i % 2 == 0 ? GOLD_DUST : CRIMSON_DUST,
					getX() + Math.cos(a) * r,
					getY() + 1.2D + Math.sin(tickCount * 0.3D + i) * 0.6D,
					getZ() + Math.sin(a) * r,
					-Math.cos(a) * 0.14D, 0.02D, -Math.sin(a) * 0.14D);
		}
	}

	private void spawnIdleHerrscherAura() {
		double a = random.nextDouble() * Mth.TWO_PI;
		double r = 0.7D + random.nextDouble() * 0.6D;
		level().addParticle(isRankIIIRenderState() && random.nextBoolean() ? GOLD_DUST : VIOLET_DUST,
				getX() + Math.cos(a) * r,
				getY() + 0.3D + random.nextDouble() * 1.6D,
				getZ() + Math.sin(a) * r,
				0.0D, 0.04D, 0.0D);
	}

	@Override
	protected void dropFromLootTable(DamageSource source, boolean wasRecentlyHit) {
		for (UUID uuid : getPlayersWhoAttacked()) {
			Player player = level().getPlayerByUUID(uuid);
			if (player == null) {
				continue;
			}
			player.spawnAtLocation(new ItemStack(ExtraBotanyItems.pandorasBox));
			player.spawnAtLocation(new ItemStack(ExtraBotanyItems.limitedEditionSupplyBag, 3));
			player.spawnAtLocation(new ItemStack(ExtraBotanyItems.recordHerrscherOfTheVoid));
			if (random.nextFloat() < 0.02F) {
				player.spawnAtLocation(new ItemStack(ExtraBotanyItems.coreOfTheVoid));
			}
		}
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		if (!level().isClientSide()) {
			for (UUID uuid : getPlayersWhoAttacked()) {
				if (level().getPlayerByUUID(uuid) instanceof ServerPlayer player) {
					HerrscherCombatHelper.award(player, LibAdvancementNames.HERRSCHER_DEFEAT);
					if (tickCount < 20 * 180) {
						HerrscherCombatHelper.award(player, LibAdvancementNames.ENDGAME_GOAL);
					}
				}
			}
			playSound(SoundEvents.GENERIC_EXPLODE, 20F, (1F + (random.nextFloat() - random.nextFloat()) * 0.2F) * 0.7F);
			if (level() instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY(), getZ(), 1, 1D, 0D, 0D, 0D);
			}
		}
	}

	@Override
	public void remove(RemovalReason reason) {
		if (!level().isClientSide() && reason.shouldDestroy()) {
			clearBattleEntities();
		}
		super.remove(reason);
	}

	private void clearBattleEntities() {
		for (Entity e : level().getEntitiesOfClass(Entity.class, getBoundingBox().inflate(64D), this::isOwnedBattleEntity)) {
			e.discard();
		}
	}

	private boolean isOwnedBattleEntity(Entity e) {
		if (e instanceof SubspaceLanceEntity lance) {
			return lance.getOwner() == this;
		}
		if (e instanceof SubspaceEntity subspace) {
			return subspace.getOwner() == this;
		}
		if (e instanceof SubspaceSpearEntity spear) {
			return spear.getOwner() == this;
		}
		if (e instanceof MagicLandMineEntity mine) {
			return mine.getOwner() == this;
		}
		if (e instanceof SkullMissileEntity missile) {
			return missile.getOwner() == this;
		}
		if (e instanceof HerrscherVoidFieldEntity field) {
			return field.getOwner() == null || field.getOwner() == this;
		}
		return e instanceof SwordDomainEntity;
	}

	@Override
	public float getDamageCap() {
		float cap = DAMAGE_CAP;
		for (int i = 0; i < rotatingShields; i++) {
			cap *= 0.88F;
		}
		return cap;
	}

	public boolean isRankIIRenderState() {
		return entityData.get(RANK_II);
	}

	public boolean isRankIIIRenderState() {
		return entityData.get(RANK_III);
	}

	public int getRotatingShieldsRenderState() {
		return Mth.clamp(entityData.get(ROTATING_SHIELDS), 0, 3);
	}

	public int getShieldLayersRenderState() {
		return Math.max(0, entityData.get(SHIELD_LAYERS_SYNC));
	}

	public int getTransitionTicks() {
		return entityData.get(TRANSITION_TICKS);
	}

	public int getTransitionTargetRank() {
		return entityData.get(TRANSITION_TARGET);
	}

	public boolean isTransitioning() {
		return getTransitionTicks() > 0;
	}

	public float getTransitionProgress(float partialTick) {
		int ticks = getTransitionTicks();
		if (ticks <= 0) {
			return 0.0F;
		}
		int total = getTransitionTargetRank() >= 3 ? RANK_III_TRANSITION_TICKS : RANK_II_TRANSITION_TICKS;
		float remaining = Math.max(0.0F, ticks - partialTick);
		return Mth.clamp(1.0F - remaining / (float) total, 0.0F, 1.0F);
	}

	public int getCastState() {
		return entityData.get(CAST_STATE);
	}

	public int getCastTicks() {
		return entityData.get(CAST_TICKS);
	}

	private void syncRotatingShields() {
		entityData.set(ROTATING_SHIELDS, Mth.clamp(rotatingShields, 0, 3));
	}

	private void syncShieldLayers() {
		entityData.set(SHIELD_LAYERS_SYNC, Math.max(0, shieldLayers));
	}

	private void syncAllRenderStates() {
		entityData.set(RANK_II, rankII || rankIII);
		entityData.set(RANK_III, rankIII);
		syncRotatingShields();
		syncShieldLayers();
	}

	@Override
	public float getArenaRange() {
		return ARENA_RANGE;
	}

	@Override
	public SoundEvent getBGM() {
		return ExtraBotanySounds.MUSIC_HERRSCHER;
	}

	// 持有"最初分型"即可带其它模组物品打空之律者、且不被缴械(不依赖成就)。
	@Override
	public net.minecraft.world.item.Item getGuardianBypassItem() {
		return ExtraBotanyItems.firstFractal;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(TAG_SHIELD_LAYERS, shieldLayers);
		tag.putInt(TAG_ROTATING_SHIELDS, rotatingShields);
		tag.putBoolean(TAG_RANK_II, rankII);
		tag.putBoolean(TAG_RANK_III, rankIII);
		tag.putFloat(TAG_DAMAGE_TAKEN, damageTaken);
		tag.putInt(TAG_TP_DELAY, tpDelay);
		tag.putInt(TAG_DODGE_CD, dodgeCd);
		tag.putInt(TAG_SKILL_CD, skillCd);
		tag.putInt(TAG_SKILL_TYPE, skillType);
		tag.putInt(TAG_SUPPORT_CD, supportCd);
		tag.putBoolean(TAG_EMERGE_LANCE_DONE, emergeLanceDone);
		tag.putInt(TAG_TRANSITION_TICKS, getTransitionTicks());
		tag.putInt(TAG_TRANSITION_TARGET, getTransitionTargetRank());
		tag.putInt(TAG_JUDGE_CHARGE, judgeChargeTicks);
		ListTag list = new ListTag();
		for (String contributor : contributors) {
			list.add(StringTag.valueOf(contributor));
		}
		tag.put(TAG_SUPPORTERS, list);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		shieldLayers = tag.getInt(TAG_SHIELD_LAYERS);
		rotatingShields = Mth.clamp(tag.getInt(TAG_ROTATING_SHIELDS), 0, 3);
		rankII = tag.getBoolean(TAG_RANK_II);
		rankIII = tag.getBoolean(TAG_RANK_III);
		syncAllRenderStates();
		damageTaken = tag.getFloat(TAG_DAMAGE_TAKEN);
		tpDelay = tag.getInt(TAG_TP_DELAY);
		dodgeCd = tag.getInt(TAG_DODGE_CD);
		skillCd = tag.getInt(TAG_SKILL_CD);
		skillType = tag.getInt(TAG_SKILL_TYPE);
		supportCd = tag.getInt(TAG_SUPPORT_CD);
		emergeLanceDone = tag.getBoolean(TAG_EMERGE_LANCE_DONE);
		entityData.set(TRANSITION_TICKS, tag.getInt(TAG_TRANSITION_TICKS));
		entityData.set(TRANSITION_TARGET, tag.getInt(TAG_TRANSITION_TARGET));
		judgeChargeTicks = tag.getInt(TAG_JUDGE_CHARGE);
		if (tag.contains(TAG_SUPPORTERS)) {
			contributors.clear();
			ListTag list = tag.getList(TAG_SUPPORTERS, Tag.TAG_STRING);
			for (Tag value : list) {
				contributors.add(value.getAsString());
			}
		}
	}
}
