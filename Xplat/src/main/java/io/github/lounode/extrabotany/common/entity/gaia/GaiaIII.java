package io.github.lounode.extrabotany.common.entity.gaia;

import com.mojang.serialization.Dynamic;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Unit;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.handler.BotaniaSounds;

import io.github.lounode.extrabotany.api.gaia.GaiaArena;
import io.github.lounode.extrabotany.common.entity.EgoMinionEntity;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.SkullLandMineEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import java.util.List;

public class GaiaIII extends Gaia {
	public static final float ARENA_RANGE = 15F;
	public static final int ARENA_HEIGHT = 7;
	public static final float MAX_HP = 600F;
	private static final float DAMAGE_CAP = 30;

	private static final EntityDataAccessor<Integer> BIG_PHASE = SynchedEntityData.defineId(GaiaIII.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> EGO_STAGE = SynchedEntityData.defineId(GaiaIII.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> EGO_FLICKER = SynchedEntityData.defineId(GaiaIII.class, EntityDataSerializers.INT);

	private static final String TAG_BIG_PHASE = "BigPhase";
	private static final String TAG_EGO_STAGE = "EgoStage";
	private static final String TAG_IN_FOG = "InFogBarrage";
	private static final String TAG_FOG_WAVE_INDEX = "FogWaveIndex";
	private static final String TAG_FOG_WAVE_TIMER = "FogWaveTimer";
	private static final String TAG_IN_MINION = "InMinionPhase";
	private static final String TAG_MINION_TIMER = "MinionPhaseTimer";
	private static final String TAG_FLICKER_CD = "FlickerCooldown";

	public static final float PHASE_EGO_THRESHOLD = 2F / 3F;
	public static final float EGO_STAGE_1_THRESHOLD = 1F / 2F;
	public static final float EGO_STAGE_2_THRESHOLD = 1F / 6F;

	private static final int FLICKER_INTERVAL = 240;
	private static final int FLICKER_DURATION = 8;
	private static final Component EGO_NAME = Component.translatable("entity.extrabotany.ego");
	private int flickerCooldown = FLICKER_INTERVAL;
	private boolean nameShowingEgo;

	private static final int FOG_WAVE_COUNT = 6;
	private static final int FOG_WAVE_INTERVAL = 60;
	private boolean inFogBarrage;
	private int fogWaveIndex;
	private int fogWaveTimer;

	private static final BlockPos[] MINION_OFFSETS = {
			new BlockPos(6, 1, 6), new BlockPos(6, 1, -6),
			new BlockPos(-6, 1, 6), new BlockPos(-6, 1, -6)
	};
	private static final int MINION_PHASE_MAX_TICKS = 600;
	private boolean inMinionPhase;
	private int minionPhaseTimer;

	public GaiaIII(EntityType<? extends GaiaIII> type, Level world) {
		super(type, world);
		this.xpReward = 1000;
	}

	public GaiaIII(EntityType<? extends GaiaIII> type, Level world, BlockPos source) {
		super(type, world, source);
	}

	public GaiaIII(Level world, BlockPos source) {
		this(ExtraBotanyEntityType.GAIA_III, world, source);
	}

	public static boolean spawn(Player player, ItemStack stack, Level world, BlockPos pos) {
		GaiaArena arena = GaiaArena.of(GlobalPos.of(world.dimension(), pos), ARENA_RANGE, ARENA_HEIGHT);
		if (!arena.checksModern(player, world, stack)) {
			return false;
		}
		if (!arena.checkGuardianInventoryStrict(world, ExtraBotanyItems.voidArchives)) {
			if (!world.isClientSide()) {
				player.sendSystemMessage(Component.translatable("extrabotany.message.guardian_no_response").withStyle(ChatFormatting.RED));
			}
			return false;
		}

		if (!world.isClientSide()) {
			GaiaIII gaia = new GaiaIII(world, pos);
			gaia.setArena(arena);
			gaia.setPos(pos.getX() + 0.5, pos.getY() + 3, pos.getZ() + 0.5);

			gaia.getBrain().setMemoryWithExpiry(MemoryModuleType.IS_EMERGING, Unit.INSTANCE, (long) GaiaIIIAI.EMERGE_TIME);
			gaia.setInvulTime(GaiaIIIAI.EMERGE_TIME);
			gaia.setHealth(1F);
			gaia.bossEvent.setProgress(0.0F);

			List<Player> playersAround = arena.getPlayersAround(world);
			int playerCount = playersAround.size();
			gaia.playerCount = playerCount;
			gaia.bossEvent.setPlayerCount(playerCount);

			float healthMultiplier = 1;
			if (playerCount > 1) {
				healthMultiplier += playerCount * 0.25F;
			}
			gaia.getAttribute(Attributes.MAX_HEALTH).setBaseValue(MAX_HP * healthMultiplier);
			gaia.getAttribute(Attributes.ARMOR).setBaseValue(30);

			gaia.playSound(BotaniaSounds.gaiaSummon, 0.05F, 1F);
			gaia.finalizeSpawn((ServerLevelAccessor) world, world.getCurrentDifficultyAt(gaia.blockPosition()), MobSpawnType.EVENT, null);
			world.addFreshEntity(gaia);

			for (Player nearbyPlayer : playersAround) {
				if (nearbyPlayer instanceof ServerPlayer serverPlayer) {
					CriteriaTriggers.SUMMONED_ENTITY.trigger(serverPlayer, gaia);
				}
			}
		}
		return true;
	}

	public static AttributeSupplier.Builder createGaiaAttributes() {
		return Monster.createMonsterAttributes()
				.add(Attributes.MOVEMENT_SPEED, 0.4)
				.add(Attributes.MAX_HEALTH, MAX_HP)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
	}

	@Override
	protected Brain<?> makeBrain(Dynamic<?> dynamic) {
		return GaiaIIIAI.makeBrain(this, dynamic);
	}

	@Override
	protected void initMemories(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
		GaiaIIIAI.initMemories(this, level.getLevel(), getHome().pos());
	}

	@Override
	protected void updateAI() {
		GaiaIIIAI.updateActivity(this);
	}

	@Override
	public ResourceKey<LootTable> getDefaultLootTable() {
		return this.getType().getDefaultLootTable();
	}

	@Override
	public void die(DamageSource source) {
		discardMinions();
		super.die(source);
	}

	@Override
	public void remove(RemovalReason reason) {
		if (!level().isClientSide()) {
			discardMinions();
		}
		super.remove(reason);
	}

	private void discardMinions() {
		AABB area = new AABB(getHome().pos()).inflate(getArenaRange() + 8);
		for (EgoMinionEntity minion : level().getEntitiesOfClass(EgoMinionEntity.class, area)) {
			minion.discard();
		}
	}

	@Override
	public float getDamageCap() {
		return DAMAGE_CAP;
	}

	@Override
	public int getEmergeTime() {
		return GaiaIIIAI.EMERGE_TIME;
	}

	@Override
	public SoundEvent getBGM() {
		return ExtraBotanySounds.MUSIC_GAIA3;
	}

	@Override
	public Item getGuardianBypassItem() {
		return ExtraBotanyItems.voidArchives;
	}

	@Override
	@Nullable
	public SoundEvent getEgoBGM() {
		return null;
	}

	@Override
	public boolean isEgoPhase() {
		return false;
	}

	public int getBigPhase() {
		return entityData.get(BIG_PHASE);
	}

	public void setBigPhase(int phase) {
		entityData.set(BIG_PHASE, phase);
	}

	public boolean isEgo() {
		return getBigPhase() >= 2;
	}

	public int getEgoStage() {
		return entityData.get(EGO_STAGE);
	}

	public void setEgoStage(int stage) {
		entityData.set(EGO_STAGE, stage);
	}

	public int getEgoFlicker() {
		return entityData.get(EGO_FLICKER);
	}

	public void setEgoFlicker(int ticks) {
		entityData.set(EGO_FLICKER, ticks);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(BIG_PHASE, 1);
		builder.define(EGO_STAGE, 0);
		builder.define(EGO_FLICKER, 0);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag cmp) {
		super.addAdditionalSaveData(cmp);
		cmp.putInt(TAG_BIG_PHASE, getBigPhase());
		cmp.putInt(TAG_EGO_STAGE, getEgoStage());
		cmp.putBoolean(TAG_IN_FOG, inFogBarrage);
		cmp.putInt(TAG_FOG_WAVE_INDEX, fogWaveIndex);
		cmp.putInt(TAG_FOG_WAVE_TIMER, fogWaveTimer);
		cmp.putBoolean(TAG_IN_MINION, inMinionPhase);
		cmp.putInt(TAG_MINION_TIMER, minionPhaseTimer);
		cmp.putInt(TAG_FLICKER_CD, flickerCooldown);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag cmp) {
		super.readAdditionalSaveData(cmp);
		if (cmp.contains(TAG_BIG_PHASE)) {
			setBigPhase(cmp.getInt(TAG_BIG_PHASE));
		}
		if (cmp.contains(TAG_EGO_STAGE)) {
			setEgoStage(cmp.getInt(TAG_EGO_STAGE));
		}
		inFogBarrage = cmp.getBoolean(TAG_IN_FOG);
		fogWaveIndex = cmp.getInt(TAG_FOG_WAVE_INDEX);
		fogWaveTimer = cmp.getInt(TAG_FOG_WAVE_TIMER);
		inMinionPhase = cmp.getBoolean(TAG_IN_MINION);
		minionPhaseTimer = cmp.getInt(TAG_MINION_TIMER);
		if (cmp.contains(TAG_FLICKER_CD)) {
			flickerCooldown = cmp.getInt(TAG_FLICKER_CD);
		}
	}

	@Override
	protected void customServerAiStep() {
		super.customServerAiStep();
		tickFogBarrage();
		tickMinionPhase();
		tickEgoPhases();
		if (isEgo()) {
			tickFlicker();
		}
	}

	private void tickEgoPhases() {
		if (getInvulTime() != 0) {
			return;
		}
		float frac = getHealth() / getMaxHealth();

		if (getBigPhase() == 1 && frac <= PHASE_EGO_THRESHOLD) {
			enterEgoPhase();
		}

		if (getBigPhase() >= 2) {
			if (getEgoStage() < 1 && frac <= EGO_STAGE_1_THRESHOLD) {
				setEgoStage(1);
				startFogBarrage();
			}
			if (getEgoStage() < 2 && frac <= EGO_STAGE_2_THRESHOLD) {
				setEgoStage(2);
				startMinionPhase();
			}
		}
	}

	private void enterEgoPhase() {
		setBigPhase(2);
		setEgoStage(0);
		setEgoFlicker(0);
		flickerCooldown = FLICKER_INTERVAL;
		playSound(BotaniaSounds.gaiaSummon, 1F, 0.6F);
	}

	private void tickFlicker() {
		int flicker = getEgoFlicker();
		if (flicker > 0) {
			setEgoFlicker(flicker - 1);
		}
		if (flickerCooldown > 0) {
			flickerCooldown--;
		} else {
			setEgoFlicker(FLICKER_DURATION);
			flickerCooldown = FLICKER_INTERVAL;
		}
		boolean showEgo = getEgoFlicker() > 0;
		if (showEgo != nameShowingEgo) {
			nameShowingEgo = showEgo;
			bossEvent.setName(showEgo ? EGO_NAME : getType().getDescription());
		}
	}

	private void startFogBarrage() {
		inFogBarrage = true;
		fogWaveIndex = 0;
		fogWaveTimer = 0;
		setInvulTime(FOG_WAVE_COUNT * FOG_WAVE_INTERVAL + 70);
		playSound(BotaniaSounds.gaiaSummon, 1F, 0.8F);
	}

	private void tickFogBarrage() {
		if (!inFogBarrage) {
			return;
		}
		BlockPos home = getHome().pos();
		setPos(home.getX() + 0.5, home.getY() + 3, home.getZ() + 0.5);
		setDeltaMovement(Vec3.ZERO);

		if (fogWaveIndex < FOG_WAVE_COUNT) {
			if (fogWaveTimer <= 0) {
				spawnFogWave(fogWaveIndex);
				fogWaveIndex++;
				fogWaveTimer = FOG_WAVE_INTERVAL;
			} else {
				fogWaveTimer--;
			}
		} else if (getInvulTime() <= 0) {
			inFogBarrage = false;
		}
	}

	private void spawnFogMine(Vec3 pos, int oldType) {
		SkullLandMineEntity mine = switch (oldType) {
			case 1 -> new SkullLandMineEntity.Disarm(level(), this);
			case 2 -> new SkullLandMineEntity.Danger(level(), this);
			default -> new SkullLandMineEntity.Default(level(), this);
		};
		mine.setPos(pos.x, pos.y, pos.z);
		level().addFreshEntity(mine);
	}

	private void spawnFogWave(int wave) {
		BlockPos home = getHome().pos();
		Vec3 center = new Vec3(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
		Vec3 unit = new Vec3(2, 0, 0);
		switch (wave) {
			case 0 -> {
				for (int i = 0; i < 8; i++) {
					unit = unit.yRot((float) (Math.PI / 4F * i));
					for (int j = 0; j < 8; j++) {
						spawnFogMine(center.add(unit.scale(j + 1)), j % 4 == 0 ? 2 : 0);
					}
				}
			}
			case 1 -> {
				for (int i = 0; i < 5; i++) {
					for (int j = 0; j < 16; j++) {
						Vec3 u = unit.add(new Vec3(3, 0, 0).multiply(i, 0, 0)).yRot((float) (Math.PI / 8F * j));
						spawnFogMine(center.add(u), i % 3);
					}
				}
			}
			case 2 -> {
				for (int i = 0; i < 72; i++) {
					double p = i * Math.PI / 12F;
					double r = 1 + p;
					spawnFogMine(new Vec3(center.x + r * Math.cos(p), center.y, center.z + r * Math.sin(p)), i % 5 == 0 ? 2 : 0);
				}
			}
			case 3 -> {
				for (int i = 0; i < 80; i++) {
					double p = i * Math.PI / 80F;
					double r = 24 * Math.sin(5F * p);
					spawnFogMine(new Vec3(center.x + r * Math.cos(p), center.y, center.z + r * Math.sin(p)), i % 4 == 0 ? 2 : 0);
				}
			}
			case 4 -> {
				for (int i = 0; i < 8; i++) {
					Vec3 ringCenter = center.add(unit.multiply(6, 0, 6).yRot((float) (Math.PI / 4F * i)));
					for (int j = 0; j < 16; j++) {
						Vec3 u = unit.multiply(3, 0, 3).yRot((float) (Math.PI / 8F * j));
						spawnFogMine(ringCenter.add(u), i % 3);
					}
				}
			}
			default -> {
				for (int i = 0; i < 6; i++) {
					Vec3 mp = center.add(unit.multiply(5, 0, 5).yRot((float) (Math.PI * 2 / 6F * i)));
					spawnFogMine(mp, 0);
					for (int j = 0; j < 16; j++) {
						spawnFogMine(mp.add(unit.multiply(2, 0, 2).yRot((float) (Math.PI / 8F * j))), 2);
					}
				}
			}
		}
	}

	private void startMinionPhase() {
		inMinionPhase = true;
		minionPhaseTimer = 0;
		setInvulTime(60);
		BlockPos home = getHome().pos();
		if (level() instanceof ServerLevel server) {
			int type = 0;
			for (BlockPos off : MINION_OFFSETS) {
				EgoMinionEntity minion = new EgoMinionEntity(ExtraBotanyEntityType.EGO_MINION, server);
				minion.setMinionType(type);
				minion.setSummoner(this);
				minion.setCustomName(EgoMinionEntity.pickName(type));
				minion.setCustomNameVisible(true);
				minion.getAttribute(Attributes.MAX_HEALTH).setBaseValue(60D * Math.max(1, playerCount));
				minion.setHealth(minion.getMaxHealth());
				minion.setPos(home.getX() + off.getX() + 0.5, home.getY() + off.getY(), home.getZ() + off.getZ() + 0.5);
				minion.finalizeSpawn(server, server.getCurrentDifficultyAt(minion.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
				server.addFreshEntity(minion);
				type++;
			}
		}
		playSound(BotaniaSounds.gaiaSummon, 1F, 0.7F);
	}

	private void tickMinionPhase() {
		if (!inMinionPhase) {
			return;
		}
		BlockPos home = getHome().pos();
		setPos(home.getX() + 0.5, home.getY() + 3, home.getZ() + 0.5);
		setDeltaMovement(Vec3.ZERO);
		minionPhaseTimer++;
		if (countLivingMinions() > 0 && minionPhaseTimer < MINION_PHASE_MAX_TICKS) {
			setInvulTime(Math.max(getInvulTime(), 40));
			healDuringMinionPhase(0.25F);
		} else {
			inMinionPhase = false;
			setInvulTime(0);
		}
	}

	private int countLivingMinions() {
		AABB area = new AABB(getHome().pos()).inflate(getArenaRange() + 4);
		return level().getEntitiesOfClass(EgoMinionEntity.class, area, EgoMinionEntity::isAlive).size();
	}

	private void healDuringMinionPhase(float amount) {
		float cap = getMaxHealth() * PHASE_EGO_THRESHOLD;
		if (getHealth() < cap) {
			setHealth(Math.min(cap, getHealth() + amount));
		}
	}
}
