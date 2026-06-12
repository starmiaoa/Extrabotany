package io.github.lounode.extrabotany.common.entity.gaia;

import com.mojang.serialization.Dynamic;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.common.handler.BotaniaSounds;

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

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class VoidHerrscher extends Gaia {
	public static final float ARENA_RANGE = 15F;
	public static final int ARENA_HEIGHT = 7;
	public static final float MAX_HP = 400F;
	private static final float DAMAGE_CAP = 25F;
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
	private static final String TAG_SUPPORTERS = "Supporters";
	private static final EntityDataAccessor<Integer> ROTATING_SHIELDS = SynchedEntityData.defineId(VoidHerrscher.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> RANK_II = SynchedEntityData.defineId(VoidHerrscher.class, EntityDataSerializers.BOOLEAN);

	private int shieldLayers = 5;
	private int rotatingShields = 0;
	private boolean rankII;
	private boolean rankIII;
	private float damageTaken;
	private int tpDelay = 90;
	private int dodgeCd = 300;
	private int skillCd = 200;
	private int skillType = 0;
	private int supportCd = 300;
	private boolean emergeLanceDone;
	private final List<String> contributors = new LinkedList<>(SUPPORTERS);

	public VoidHerrscher(EntityType<? extends VoidHerrscher> type, Level world) {
		super(type, world);
		this.xpReward = 1725;
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
				.add(Attributes.MOVEMENT_SPEED, 0.4)
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
		if (!arena.checkGuardianInventoryStrict(world, prefix("main/" + LibAdvancementNames.FIRST_FRACTAL_OBTAIN))) {
			if (!world.isClientSide()) {
				player.sendSystemMessage(Component.translatable("extrabotany.message.guardian_no_response").withStyle(ChatFormatting.RED));
			}
			return false;
		}

		if (!world.isClientSide()) {
			VoidHerrscher boss = new VoidHerrscher(world, pos);
			boss.setArena(arena);
			boss.setPos(pos.getX() + 0.5D, pos.getY() + 3D, pos.getZ() + 0.5D);
			boss.getBrain().setMemoryWithExpiry(MemoryModuleType.IS_EMERGING, Unit.INSTANCE, (long) GaiaIIIAI.EMERGE_TIME);
			boss.setInvulTime(GaiaIIIAI.EMERGE_TIME);
			boss.setHealth(1F);
			boss.bossEvent.setProgress(0F);

			List<Player> playersAround = arena.getPlayersAround(world);
			int playerCount = Math.max(1, playersAround.size());
			boss.playerCount = playerCount;
			boss.bossEvent.setPlayerCount(playerCount);
			boss.getAttribute(Attributes.MAX_HEALTH).setBaseValue(MAX_HP * playerCount);
			if (world.getDifficulty() == Difficulty.HARD) {
				boss.getAttribute(Attributes.ARMOR).setBaseValue(15);
			}

			boss.finalizeSpawn((ServerLevelAccessor) world, world.getCurrentDifficultyAt(boss.blockPosition()), MobSpawnType.EVENT, null, null);
			world.addFreshEntity(boss);
			world.playSound(null, pos, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 10F, 0.1F);
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
		return GaiaIIIAI.makeBrain(this, dynamic);
	}

	@Override
	protected void initMemories(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
		GaiaIIIAI.initMemories(this, level.getLevel(), getHome().pos());
	}

	@Override
	protected void updateAI() {
		GaiaIIIAI.updateActivity(this);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(ROTATING_SHIELDS, 0);
		this.entityData.define(RANK_II, false);
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
		float healthRatio = getHealth() / Math.max(1F, getMaxHealth());
		if (!rankII && healthRatio <= 0.8F) {
			enterRankII();
		}
		if (!rankIII && healthRatio <= 0.25F) {
			enterRankIII();
		}
		BlockPos home = getHome().pos();
		if (!emergeLanceDone && tickCount >= GaiaIIIAI.EMERGE_TIME + 100) {
			spawnLance(home.getX() + 0.5D, home.getY() + 12D, home.getZ() + 0.5D, 3F, 4800);
			emergeLanceDone = true;
		}
		if (rotatingShields >= 3) {
			reflectProjectiles();
		}
		for (Player player : getPlayersAround()) {
			if (player.getDeltaMovement().y > 0D && !player.getAbilities().instabuild) {
				player.setDeltaMovement(player.getDeltaMovement().multiply(1D, -1D, 1D));
			}
			clearPotions(player);
			int amplifier = 1 + (rankIII ? 2 : 0) + (tickCount >= 1800 ? 2 : 0);
			player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.WITCH_CURSE, 200, Math.max(1, amplifier), true, true));
			if (player.distanceToSqr(home.getX() + 0.5D, player.getY(), home.getZ() + 0.5D) > ARENA_RANGE * ARENA_RANGE) {
				player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 400, 4));
			}
		}
		if (rankII && getY() < home.getY() + 2D) {
			setDeltaMovement(getDeltaMovement().add(0, 0.1D, 0));
		}
		if (tickCount % 72 == 0) {
			spawnSpearSubspace();
		}
		if (tickCount > 200 && tickCount % (rankIII ? 220 : rankII ? 260 : 310) == 0) {
			spawnLandmines();
		}
		if (tickCount > 60 && tickCount % 60 == 0) {
			spawnMissile(0);
			if (random.nextFloat() < 0.4F) {
				spawnMissile(1);
			}
		}
		if (rankII && tickCount % 110 == 0) {
			spawnMissile(1);
		}
		if (rankIII && tickCount % 80 == 0) {
			spawnMissile(2);
			spawnMissile(2);
			healFinite(1F);
		}
		if (--tpDelay <= 0) {
			randomTeleport();
			tpDelay = rankIII ? 75 : 90;
		}
		if (dodgeCd > 0) {
			dodgeCd--;
		}
		if (rankII && --skillCd <= 0) {
			runSkill();
		} else if (rankII && skillCd == 100) {
			warnSkill();
		}
		if (rankIII && --supportCd <= 0) {
			runSupport();
			supportCd = 400;
		}
	}

	private void enterRankII() {
		rankII = true;
		entityData.set(RANK_II, true);
		shieldLayers = 5;
		rotatingShields = Math.max(rotatingShields, 1);
		syncRotatingShields();
		spawnSwordDomain();
		for (int i = 0; i < 2; i++) {
			spawnRandomLance();
		}
		broadcast("extrabotany.message.herrscher.rank2");
	}

	private void enterRankIII() {
		rankIII = true;
		rotatingShields = 3;
		entityData.set(RANK_II, true);
		syncRotatingShields();
		for (int i = 0; i < 4; i++) {
			spawnRandomLance();
		}
		if (!getPlayersWhoAttacked().isEmpty()) {
			super.heal(getMaxHealth());
		}
		broadcast("extrabotany.message.herrscher.rank3");
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
		if (!(attacker instanceof Player player) || !PlayerHelper.isTruePlayer(player) || getInvulTime() > 0) {
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
			return false;
		}
		if (dodgeCd <= 0) {
			HerrscherVoidFieldEntity field = new HerrscherVoidFieldEntity(level());
			field.setPos(player.getX(), player.getY(), player.getZ());
			level().addFreshEntity(field);
			dodgeCd = 300;
			spawnSpearSubspace();
			if (random.nextFloat() < 0.2F) {
				spawnRandomLance(player.position());
			}
			return false;
		}
		float adjusted = amount;
		for (int i = 0; i < rotatingShields; i++) {
			adjusted *= 0.85F;
		}
		if (rotatingShields > 0) {
			player.getActiveEffects().forEach(effect -> addEffect(new MobEffectInstance(effect)));
			if (adjusted > 20F) {
				rotatingShields = Math.max(0, rotatingShields - 1);
				syncRotatingShields();
				dodgeCd = 0;
			}
		}
		float capped = Math.min(getDamageCap(), adjusted);
		damageTaken += capped;
		if (damageTaken >= 80F) {
			damageTaken = 0F;
			randomTeleport();
			tpDelay = 65;
			HerrscherCombatHelper.dealTrueMagicDamage(player, this, player.getMaxHealth() * 0.1F + 6F);
		}
		if (skillCd > 80) {
			skillCd -= 15;
		}
		boolean result = super.hurt(source, capped);
		setInvulTime(getInvulTime() + 10);
		Vec3 away = position().subtract(player.position());
		if (away.lengthSqr() > 1.0E-6D) {
			Vec3 motion = away.normalize().scale(0.2D);
			setDeltaMovement(motion.x, 0.25D, motion.z);
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
			tpDelay = Math.max(tpDelay - 4, 0);
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

	private void spawnSpearSubspace() {
		SubspaceEntity subspace = new SubspaceEntity(level(), this);
		subspace.configure(SubspaceEntity.TYPE_SPEAR, 32, 12, 10, 0.40F + random.nextFloat() * 0.15F, Mth.wrapDegrees(-getYRot() + 180F));
		subspace.setPos(getX(), getY() + 1.9D, getZ());
		subspace.setYRot(getYRot());
		level().addFreshEntity(subspace);
	}

	private void spawnRandomLance() {
		BlockPos home = getHome().pos();
		double x = home.getX() + (random.nextDouble() - 0.5D) * ARENA_RANGE;
		double y = (rankII ? home.getY() + 2D : home.getY()) + 12D;
		double z = home.getZ() + (random.nextDouble() - 0.5D) * ARENA_RANGE;
		spawnLance(x, y, z, 4F, 1200);
	}

	private void spawnRandomLance(Vec3 ignored) {
		spawnRandomLance();
	}

	private void spawnLance(double x, double y, double z, float damage, int life) {
		SubspaceLanceEntity lance = new SubspaceLanceEntity(level(), damage, life);
		lance.setOwner(this);
		lance.setPos(x, y, z);
		level().addFreshEntity(lance);
	}

	private void spawnLandmines() {
		List<Player> players = getPlayersAround();
		int count = (level().getDifficulty() == Difficulty.HARD ? 9 : 7) + 3 * Math.max(1, players.size()) + (rankIII ? 6 : rankII ? 2 : 0);
		BlockPos home = getHome().pos();
		for (int i = 0; i < count; i++) {
			SkullLandMineEntity mine;
			if (i % 6 == 0) {
				mine = new SkullLandMineEntity.Danger(level(), this);
			} else if (i % 8 == 0) {
				mine = new SkullLandMineEntity.Disarm(level(), this);
			} else {
				mine = new SkullLandMineEntity.Default(level(), this);
			}
			mine.setDamage(6F);
			double x = home.getX() + 0.5D + (random.nextDouble() - 0.5D) * 20D;
			double z = home.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 20D;
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
		missile.setDamage(level().getDifficulty() == Difficulty.HARD ? 7F : 5F);
		if (type >= 2) {
			missile.setEffect(true);
			missile.setTrueDamage(level().getDifficulty() == Difficulty.HARD ? 3F : 2F);
		}
		missile.setPos(getX() + (random.nextDouble() - 0.5D) * 0.1D, getY() + 1.8D + (random.nextDouble() - 0.5D) * 0.1D, getZ() + (random.nextDouble() - 0.5D) * 0.1D);
		if (missile.findTarget()) {
			playSound(BotaniaSounds.missile, 0.6F, 0.8F + random.nextFloat() * 0.2F);
			level().addFreshEntity(missile);
		}
	}

	private void spawnSwordDomain() {
		List<UUID> targets = new ArrayList<>(getPlayersWhoAttacked());
		if (targets.isEmpty()) {
			getPlayersAround().forEach(player -> targets.add(player.getUUID()));
		}
		BlockPos home = getHome().pos();
		for (int i = 0; i < 8; i++) {
			double angle = Math.toRadians(i * 45D);
			UUID target = targets.isEmpty() ? null : targets.get(Math.min(i, targets.size() - 1));
			SwordDomainEntity sword = new SwordDomainEntity(level(), target, home, i);
			sword.setPos(home.getX() + 0.5D - Math.cos(angle) * 5D, home.getY() + 7D, home.getZ() + 0.5D - Math.sin(angle) * 5D + 2D);
			level().addFreshEntity(sword);
		}
	}

	private void runSkill() {
		switch (skillType) {
			case 0 -> {
				Player target = randomPlayer();
				if (target != null) {
					target.getCooldowns().addCooldown(target.getMainHandItem().getItem(), 120);
					HerrscherCombatHelper.dealTrueMagicDamage(target, this, target.getMaxHealth() * 0.2F + 6F);
					spawnRandomLance(target.position());
				}
				skillCd = 270;
				skillType = rankIII ? 1 : random.nextInt(2);
			}
			case 1 -> {
				if (!rankIII) {
					return;
				}
				spawnVoidJudge();
				skillCd = 250;
				skillType = 2;
			}
			case 2 -> {
				spawnSwordDomain();
				skillCd = 290;
				skillType = rankIII ? 3 : 0;
			}
			default -> {
				spawnRandomLance();
				skillCd = 180;
				skillType = 0;
			}
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
			case 1 -> hurt(damageSources().playerAttack(player), 15F);
			case 2 -> tpDelay += 200;
			case 3 -> {
				variant = random.nextInt(2);
				skillCd += 120;
			}
			case 4 -> {
				variant = random.nextInt(3);
				supportCd = 200;
			}
			case 5 -> {
				variant = random.nextInt(2);
				player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 5));
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
		if (skillType == 1) {
			broadcast("extrabotany.message.herrscher.warning_judge");
		} else {
			broadcast("extrabotany.message.herrscher.warning_target");
		}
	}

	private void spawnVoidJudge() {
		BlockPos home = getHome().pos();
		setInvulTime(120);
		teleportTo(home.getX(), home.getY() + 2D, home.getZ());
		for (int i = 0; i < 24; i++) {
			Vec3 look = getLookAngle().multiply(1D, 0D, 1D);
			double playerRot = Math.toRadians(getYRot() + 90F);
			if (look.lengthSqr() < 1.0E-6D) {
				look = new Vec3(Math.cos(playerRot), 0D, Math.sin(playerRot));
			}
			look = look.normalize().scale(-2D);
			int div = i / 8;
			int mod = i % 8;
			Vec3 pl = position().add(0D, getBbHeight() * 0.5D, 0D).add(look).add(0D, 1.6D, div * 0.1D);
			Vec3 axis = look.normalize().cross(new Vec3(-1D, 0D, -1D));
			if (axis.lengthSqr() < 1.0E-6D) {
				axis = new Vec3(1D, 0D, 0D);
			}
			axis = axis.normalize();
			double rot = mod * Math.PI / 7D - Math.PI / 2D;
			Vec3 axis1 = rotateAroundAxis(axis.scale(div * 3.5D + 5D), look.normalize(), rot);
			if (axis1.y < 0D) {
				axis1 = axis1.multiply(1D, -1D, 1D);
			}
			Vec3 end = pl.add(axis1);
			SubspaceEntity subspace = new SubspaceEntity(level(), this);
			subspace.configure(SubspaceEntity.TYPE_DOMAIN, 120, 15 + random.nextInt(12), 10 + random.nextInt(10), 3.0F + random.nextFloat() * 0.5F, Mth.wrapDegrees(-getYRot() + 180F));
			subspace.setPos(end.x, end.y - 0.5D + random.nextFloat(), end.z);
			subspace.setYRot(getYRot());
			level().addFreshEntity(subspace);
			if (i == 1) {
				playSound(ExtraBotanySounds.SPEAR_OF_SUBSPACE_USE, 1F, 1F);
			}
		}
	}

	private static Vec3 rotateAroundAxis(Vec3 vector, Vec3 axis, double radians) {
		double cos = Math.cos(radians);
		double sin = Math.sin(radians);
		return vector.scale(cos)
				.add(axis.cross(vector).scale(sin))
				.add(axis.scale(axis.dot(vector) * (1D - cos)));
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

	private void randomTeleport() {
		BlockPos home = getHome().pos();
		double x = home.getX() + 0.5D + (random.nextDouble() - 0.5D) * 15D;
		double y = home.getY() + (rankII ? 2D : 1D);
		double z = home.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 15D;
		teleportTo(x, y, z);
	}

	private void reflectProjectiles() {
		for (Projectile projectile : level().getEntitiesOfClass(Projectile.class, getBoundingBox().inflate(2.5D))) {
			if (projectile.getOwner() == this || projectile instanceof SubspaceSpearEntity || projectile instanceof SkullMissileEntity) {
				continue;
			}
			Vec3 look = getLookAngle().scale(1.5D);
			projectile.setDeltaMovement(look);
			projectile.hurtMarked = true;
		}
	}

	@Nullable
	private Player randomPlayer() {
		List<Player> players = getPlayersAround().stream().filter(Player::isAlive).toList();
		return players.isEmpty() ? null : players.get(random.nextInt(players.size()));
	}

	private void broadcast(String key) {
		getPlayersAround().forEach(player -> player.sendSystemMessage(Component.translatable(key).withStyle(ChatFormatting.LIGHT_PURPLE)));
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
				serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER, getX(), getY(), getZ(), 1, 1D, 0D, 0D, 0D);
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
		if (e instanceof MagicLandMineEntity mine) {
			return mine.getOwner() == this;
		}
		if (e instanceof SkullMissileEntity missile) {
			return missile.getOwner() == this;
		}
		return e instanceof HerrscherVoidFieldEntity || e instanceof SwordDomainEntity;
	}

	@Override
	public float getDamageCap() {
		float cap = DAMAGE_CAP;
		for (int i = 0; i < rotatingShields; i++) {
			cap *= 0.85F;
		}
		return cap;
	}

	public boolean isRankIIRenderState() {
		return entityData.get(RANK_II);
	}

	public int getRotatingShieldsRenderState() {
		return Mth.clamp(entityData.get(ROTATING_SHIELDS), 0, 3);
	}

	private void syncRotatingShields() {
		entityData.set(ROTATING_SHIELDS, Mth.clamp(rotatingShields, 0, 3));
	}

	@Override
	public float getArenaRange() {
		return ARENA_RANGE;
	}

	@Override
	public SoundEvent getBGM() {
		return ExtraBotanySounds.MUSIC_HERRSCHER;
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
		entityData.set(RANK_II, rankII || rankIII);
		syncRotatingShields();
		damageTaken = tag.getFloat(TAG_DAMAGE_TAKEN);
		tpDelay = tag.getInt(TAG_TP_DELAY);
		dodgeCd = tag.getInt(TAG_DODGE_CD);
		skillCd = tag.getInt(TAG_SKILL_CD);
		skillType = tag.getInt(TAG_SKILL_TYPE);
		supportCd = tag.getInt(TAG_SUPPORT_CD);
		emergeLanceDone = tag.getBoolean(TAG_EMERGE_LANCE_DONE);
		if (tag.contains(TAG_SUPPORTERS)) {
			contributors.clear();
			ListTag list = tag.getList(TAG_SUPPORTERS, Tag.TAG_STRING);
			for (Tag value : list) {
				contributors.add(value.getAsString());
			}
		}
	}
}
