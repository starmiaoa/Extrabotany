package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.relic.JudahOathItem;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class JudahOathEntity extends ThrowableItemProjectile {
	private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(JudahOathEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> LANDED = SynchedEntityData.defineId(JudahOathEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> STANDBY_TICKS = SynchedEntityData.defineId(JudahOathEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> DEPLOY_YAW = SynchedEntityData.defineId(JudahOathEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> FIELD_RANGE = SynchedEntityData.defineId(JudahOathEntity.class, EntityDataSerializers.FLOAT);

	private static final String TAG_OWNER = "Owner";
	private static final String TAG_VARIANT = "Variant";
	private static final String TAG_LANDED = "Landed";
	private static final String TAG_STANDBY = "StandbyTicks";
	private static final String TAG_DEPLOY_YAW = "DeployYaw";

	public static final int MAX_STANDBY_TICKS = 160;
	public static final float MAX_RANGE = 13.0F;
	private static final float BASE_DAMAGE = 8.0F;

	@Nullable
	private UUID ownerUUID;
	@Nullable
	private Entity cachedOwner;
	private int fakeCount;
	private int attackCount;
	private int targetCursor;

	public JudahOathEntity(EntityType<? extends JudahOathEntity> entityType, Level level) {
		super(entityType, level);
	}

	public JudahOathEntity(Level level, LivingEntity owner, JudahOathItem.Variant variant) {
		super(ExtraBotanyEntityType.JUDAH_OATH, owner, level);
		setOwnerEntity(owner);
		setVariant(variant);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(VARIANT, JudahOathItem.Variant.JUDAH.id());
		builder.define(LANDED, false);
		builder.define(STANDBY_TICKS, 0);
		builder.define(DEPLOY_YAW, 0.0F);
		builder.define(FIELD_RANGE, 2.5F);
	}

	@Override
	public void tick() {
		super.tick();

		if (!isLanded()) {
			// Heavy ceremonial cross descent arc
			Vec3 vel = this.getDeltaMovement();
			this.setDeltaMovement(vel.x * 0.96D, Math.max(vel.y - 0.045D, -0.85D), vel.z * 0.96D);

			if (!this.level().isClientSide() && shouldPlantOnGround()) {
				plantCross();
			}
			if (this.level().isClientSide()) {
				spawnFlightParticles();
			}
		} else {
			this.setDeltaMovement(Vec3.ZERO);
			this.setNoGravity(true);

			int standby = getStandbyTicks() + 1;
			if (!this.level().isClientSide()) {
				setStandbyTicks(standby);
				float range = getFieldRange();
				if (range < MAX_RANGE) {
					setFieldRange(Math.min(MAX_RANGE, range + 0.65F));
				}
				tickStandbyServer(standby);
			} else {
				spawnDomainParticles(standby);
			}
		}

		if (!this.level().isClientSide()
				&& (getStandbyTicks() > MAX_STANDBY_TICKS || this.tickCount > 340
						|| !(getOwnerEntity() instanceof Player player) || !player.isAlive())) {
			this.discard();
		}
	}

	private boolean shouldPlantOnGround() {
		if (this.onGround()) {
			return true;
		}
		BlockPos belowPos = BlockPos.containing(this.getX(), this.getY() - 0.25D, this.getZ());
		BlockState belowState = this.level().getBlockState(belowPos);
		return !belowState.getCollisionShape(this.level(), belowPos).isEmpty();
	}

	private void plantCross() {
		if (isLanded()) {
			return;
		}
		setLanded(true);
		this.setDeltaMovement(Vec3.ZERO);
		this.setNoGravity(true);

		// Snap cleanly onto the surface of the supporting block
		BlockPos belowPos = BlockPos.containing(this.getX(), this.getY() - 0.25D, this.getZ());
		BlockState belowState = this.level().getBlockState(belowPos);
		if (!belowState.getCollisionShape(this.level(), belowPos).isEmpty()) {
			double topY = belowPos.getY() + belowState.getCollisionShape(this.level(), belowPos).max(net.minecraft.core.Direction.Axis.Y);
			this.setPos(this.getX(), Math.max(this.getY(), topY), this.getZ());
		}

		this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.75F, 0.72F);
		this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9F,
				getVariant() == JudahOathItem.Variant.KIRA ? 1.35F : (getVariant() == JudahOathItem.Variant.SAKURA ? 1.15F : 0.95F));

		if (this.level() instanceof ServerLevel sl) {
			JudahOathItem.Variant v = getVariant();
			DustParticleOptions dust = new DustParticleOptions(new Vector3f(v.red(), v.green(), v.blue()), 1.8F);
			sl.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.4D, this.getZ(), 1, 0, 0, 0, 0);
			for (int i = 0; i < 28; i++) {
				double a = (Math.PI * 2.0D * i) / 28.0D;
				sl.sendParticles(dust, this.getX() + Math.cos(a) * 1.4D, this.getY() + 0.25D, this.getZ() + Math.sin(a) * 1.4D,
						1, Math.cos(a) * 0.25D, 0.08D, Math.sin(a) * 0.25D, 0.15D);
			}
		}
	}

	private void tickStandbyServer(int standby) {
		JudahOathItem.Variant variant = getVariant();
		switch (variant) {
			case JUDAH -> tickJudahDomain(standby);
			case KIRA -> tickKiraDomain(standby);
			case SAKURA -> tickSakuraDomain(standby);
		}
	}

	/**
	 * 犹大的誓约 — 第十一神之键·约束之键【圣光制裁】
	 * 强控禁锢领域 + 13柄升空圣枪 + 13柄追踪神圣雷枪轮流贯穿领域内目标。
	 */
	private void tickJudahDomain(int standby) {
		Entity owner = getOwnerEntity();
		List<LivingEntity> enemies = findDomainEnemies(owner);

		// Apply Chains of Binding every 8 ticks
		if (standby % 8 == 0) {
			for (LivingEntity target : enemies) {
				target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 5));
				target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false));
				if (target.getMaxHealth() < 150.0F) {
					target.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.IMMOBILIZE, 22, 0));
				}
			}
		}

		// Launch 13 ascending golden light spears in a sacred halo around the cross
		if (standby >= 4 && standby % 4 == 0 && this.fakeCount < 13) {
			double angle = (Math.PI * 2.0D * this.fakeCount) / 13.0D;
			double r = 1.6D + (this.fakeCount % 2) * 0.65D;
			Vec3 launchPos = this.position().add(Math.cos(angle) * r, 0.8D, Math.sin(angle) * r);
			spawnSpear(launchPos, true, BASE_DAMAGE);
			this.level().playSound(null, launchPos.x, launchPos.y, launchPos.z(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.4F, 1.45F);
			this.fakeCount++;
		}

		// Rain 13 descending divine light spears cycling cleanly across targets
		if (standby >= 22 && (standby - 22) % 9 == 0 && this.attackCount < 13) {
			Vec3 dropPos;
			if (!enemies.isEmpty()) {
				LivingEntity chosen = enemies.get(this.targetCursor % enemies.size());
				this.targetCursor++;
				dropPos = chosen.position().add(0.0D, 13.0D, 0.0D);
			} else {
				double angle = (Math.PI * 2.0D * this.attackCount) / 13.0D + 0.25D;
				double r = 3.0D + (this.attackCount % 4) * 2.0D;
				dropPos = this.position().add(Math.cos(angle) * r, 13.0D, Math.sin(angle) * r);
			}
			spawnSpear(dropPos, false, BASE_DAMAGE);
			this.attackCount++;
		}
	}

	/**
	 * 犹大的誓约KIRA — 星辉极光誓约【冰星奏鸣】
	 * 极光庇护(友方迅捷+再生+微回血) + 极寒冻结(敌方霜冻+虚弱) + 双连发追踪星辉冰矛(AOE霜星爆裂)。
	 */
	private void tickKiraDomain(int standby) {
		Entity owner = getOwnerEntity();
		List<LivingEntity> enemies = findDomainEnemies(owner);

		// Ally Starlight Sanctuary aura every 20 ticks
		if (standby % 20 == 0) {
			double range = getFieldRange();
			List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(range, 6.0D, range));
			for (Player ally : players) {
				if (ally == owner || !DamageHandler.INSTANCE.checkPassable(ally, owner)) {
					ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 45, 1, false, false, true));
					ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 45, 0, false, false, true));
					if (ally.getHealth() < ally.getMaxHealth()) {
						ally.heal(1.0F);
					}
				}
			}
		}

		// Enemy Frost-Star Chill every 10 ticks
		if (standby % 10 == 0) {
			for (LivingEntity target : enemies) {
				target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 2));
				target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 30, 0));
				target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 60, target.getTicksFrozen() + 28));
			}
		}

		// Launch 12 ascending twin cyan starlight lances
		if (standby >= 6 && standby % 6 == 0 && this.fakeCount < 12) {
			double angle = (Math.PI * 2.0D * this.fakeCount) / 12.0D;
			for (int pair = 0; pair < 2; pair++) {
				double a = angle + pair * Math.PI;
				Vec3 launchPos = this.position().add(Math.cos(a) * 2.1D, 1.0D, Math.sin(a) * 2.1D);
				spawnSpear(launchPos, true, BASE_DAMAGE * 0.85F);
			}
			this.level().playSound(null, this.getX(), this.getY() + 1.0D, this.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.85F, 1.35F);
			this.fakeCount += 2;
		}

		// Rain 10 twin volleys (20 agile starlight lances) targeting up to 2 distinct enemies per volley
		if (standby >= 22 && (standby - 22) % 12 == 0 && this.attackCount < 10) {
			for (int k = 0; k < 2; k++) {
				Vec3 dropPos;
				if (!enemies.isEmpty()) {
					LivingEntity chosen = enemies.get((this.targetCursor + k) % enemies.size());
					double jitterX = (enemies.size() == 1) ? ((k == 0 ? -0.75D : 0.75D)) : 0.0D;
					double jitterZ = (enemies.size() == 1) ? ((k == 0 ? 0.55D : -0.55D)) : 0.0D;
					dropPos = chosen.position().add(jitterX, 12.5D + k * 1.5D, jitterZ);
				} else {
					double angle = (Math.PI * 2.0D * (this.attackCount * 2 + k)) / 20.0D;
					double r = 3.5D + (this.attackCount % 3) * 2.5D;
					dropPos = this.position().add(Math.cos(angle) * r, 12.5D, Math.sin(angle) * r);
				}
				spawnSpear(dropPos, false, BASE_DAMAGE * 0.85F);
			}
			if (!enemies.isEmpty()) {
				this.targetCursor = (this.targetCursor + 2) % enemies.size();
			}
			this.attackCount++;
		}
	}

	/**
	 * 樱花的誓约 — 绯樱轮舞誓约【五芒星绯樱剑阵】
	 * 樱花领域减速 + 3轮旋转五芒星绯樱剑阵(每轮5柄同步3D绯樱剑刃沿星芒轨迹斩裂并收束引爆)。
	 */
	private void tickSakuraDomain(int standby) {
		Entity owner = getOwnerEntity();
		if (standby % 10 == 0) {
			for (LivingEntity target : findDomainEnemies(owner)) {
				target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 28, 1));
			}
		}

		// 3 rotated pentagram waves at standby ticks 18, 56, 94
		if ((standby == 18 || standby == 56 || standby == 94) && this.attackCount < 3) {
			spawnSakuraStar(this.attackCount);
			this.attackCount++;
		}
	}

	private List<LivingEntity> findDomainEnemies(@Nullable Entity owner) {
		double r = getFieldRange();
		AABB box = new AABB(this.getX() - r, this.getY() - 4.0D, this.getZ() - r,
				this.getX() + r, this.getY() + 8.0D, this.getZ() + r);
		List<LivingEntity> raw = this.level().getEntitiesOfClass(LivingEntity.class, box);
		List<LivingEntity> filtered = DamageHandler.INSTANCE.getFilteredEntities(raw, owner);
		filtered.sort(Comparator.comparingDouble(e -> e.distanceToSqr(this)));
		return filtered;
	}

	private void spawnSpear(Vec3 pos, boolean fake, float damage) {
		JudahSpearEntity spear = new JudahSpearEntity(this.level(), getOwnerEntity(), getVariant(), damage, fake);
		spear.setPos(pos.x, pos.y, pos.z);
		this.level().addFreshEntity(spear);
	}

	private void spawnSakuraStar(int waveIndex) {
		Vec3 center = this.position();
		double radius = 11.0D;
		double angleStep = Math.PI * 2.0D / 5.0D;
		double baseAngle = Math.toRadians(getDeployYaw() + waveIndex * 36.0D);
		Vec3[] points = new Vec3[5];
		for (int i = 0; i < 5; i++) {
			double angle = angleStep * i + baseAngle;
			points[i] = center.add(Math.cos(angle) * radius, 1.15D, Math.sin(angle) * radius);
		}
		this.level().playSound(null, this.getX(), this.getY() + 1.0D, this.getZ(), ExtraBotanySounds.FLAMESCION_SLASH, SoundSource.PLAYERS, 0.95F, 1.05F + waveIndex * 0.1F);
		for (int i = 0; i < 5; i++) {
			Vec3 start = points[i];
			Vec3 end = points[(i + 2) % 5];
			JudahSwordEntity sword = new JudahSwordEntity(this.level(), getOwnerEntity(), start, end, 8.5F);
			sword.setPos(start.x, start.y, start.z);
			this.level().addFreshEntity(sword);
		}
	}

	private void spawnFlightParticles() {
		JudahOathItem.Variant v = getVariant();
		DustParticleOptions dust = new DustParticleOptions(new Vector3f(v.red(), v.green(), v.blue()), 1.3F);
		this.level().addParticle(dust, this.getX(), this.getY() + 0.5D, this.getZ(), 0, 0.02D, 0);
		this.level().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.5D, this.getZ(),
				(this.random.nextDouble() - 0.5D) * 0.08D, 0.04D, (this.random.nextDouble() - 0.5D) * 0.08D);
	}

	private void spawnDomainParticles(int standby) {
		JudahOathItem.Variant v = getVariant();
		float range = getFieldRange();
		DustParticleOptions dust = new DustParticleOptions(new Vector3f(v.red(), v.green(), v.blue()), 1.15F);

		// Smooth sparse perimeter particles (3D magic circle handles the main crisp ring!)
		for (int i = 0; i < 4; i++) {
			double rad = (standby * 0.09D) + (Math.PI * 0.5D * i);
			double x = this.getX() + Math.cos(rad) * range;
			double z = this.getZ() + Math.sin(rad) * range;
			this.level().addParticle(dust, x, this.getY() + 0.18D, z, 0, 0.05D, 0);
		}

		// Variant ambient particles inside the domain
		if (v == JudahOathItem.Variant.SAKURA) {
			for (int i = 0; i < 3; i++) {
				double a = this.random.nextDouble() * Math.PI * 2.0D;
				double r = Math.sqrt(this.random.nextDouble()) * range * 0.92D;
				this.level().addParticle(ParticleTypes.CHERRY_LEAVES,
						this.getX() + Math.cos(a) * r,
						this.getY() + 1.5D + this.random.nextDouble() * 3.5D,
						this.getZ() + Math.sin(a) * r,
						(this.random.nextDouble() - 0.5D) * 0.05D, -0.02D, (this.random.nextDouble() - 0.5D) * 0.05D);
			}
		} else if (v == JudahOathItem.Variant.KIRA) {
			for (int i = 0; i < 3; i++) {
				double a = this.random.nextDouble() * Math.PI * 2.0D;
				double r = Math.sqrt(this.random.nextDouble()) * range * 0.92D;
				this.level().addParticle(ParticleTypes.SNOWFLAKE,
						this.getX() + Math.cos(a) * r,
						this.getY() + 0.5D + this.random.nextDouble() * 3.0D,
						this.getZ() + Math.sin(a) * r,
						0, 0.04D, 0);
			}
		} else {
			if (standby % 2 == 0) {
				double a = this.random.nextDouble() * Math.PI * 2.0D;
				double r = 1.2D + this.random.nextDouble() * (range - 1.5D);
				this.level().addParticle(ParticleTypes.END_ROD,
						this.getX() + Math.cos(a) * r,
						this.getY() + 0.3D + this.random.nextDouble() * 2.0D,
						this.getZ() + Math.sin(a) * r,
						0, 0.06D, 0);
			}
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		if (!this.level().isClientSide() && !isLanded()) {
			plantCross();
		}
	}

	@Override
	protected void onHit(HitResult result) {
		if (result instanceof BlockHitResult bhr) {
			onHitBlock(bhr);
		}
	}

	public float getUnfoldProgress(float partialTick) {
		if (!isLanded()) {
			return 0.0F;
		}
		return Mth.clamp((getStandbyTicks() + partialTick) / 14.0F, 0.0F, 1.0F);
	}

	public float getFadeAlpha(float partialTick) {
		if (!isLanded()) {
			return 1.0F;
		}
		float s = getStandbyTicks() + partialTick;
		if (s > MAX_STANDBY_TICKS - 18.0F) {
			return Mth.clamp((MAX_STANDBY_TICKS - s) / 18.0F, 0.0F, 1.0F);
		}
		return 1.0F;
	}

	public boolean isLanded() {
		return this.entityData.get(LANDED);
	}

	public void setLanded(boolean landed) {
		this.entityData.set(LANDED, landed);
	}

	public int getStandbyTicks() {
		return this.entityData.get(STANDBY_TICKS);
	}

	public void setStandbyTicks(int ticks) {
		this.entityData.set(STANDBY_TICKS, ticks);
	}

	public float getDeployYaw() {
		return this.entityData.get(DEPLOY_YAW);
	}

	public void setDeployYaw(float yaw) {
		this.entityData.set(DEPLOY_YAW, yaw);
	}

	public float getFieldRange() {
		return this.entityData.get(FIELD_RANGE);
	}

	public void setFieldRange(float range) {
		this.entityData.set(FIELD_RANGE, range);
	}

	public void setOwnerEntity(@Nullable Entity owner) {
		if (owner != null) {
			this.ownerUUID = owner.getUUID();
			this.cachedOwner = owner;
		}
	}

	@Nullable
	public Entity getOwnerEntity() {
		Entity owner = this.getOwner();
		if (owner != null) {
			return owner;
		}
		if (this.cachedOwner != null && !this.cachedOwner.isRemoved()) {
			return this.cachedOwner;
		}
		if (this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
			this.cachedOwner = serverLevel.getEntity(this.ownerUUID);
			return this.cachedOwner;
		}
		return null;
	}

	public JudahOathItem.Variant getVariant() {
		return JudahOathItem.Variant.byId(this.entityData.get(VARIANT));
	}

	public void setVariant(JudahOathItem.Variant variant) {
		this.entityData.set(VARIANT, variant.id());
		this.setItem(new ItemStack(itemFor(variant)));
	}

	private static Item itemFor(JudahOathItem.Variant variant) {
		return switch (variant) {
			case KIRA -> ExtraBotanyItems.judahOathKira;
			case SAKURA -> ExtraBotanyItems.judahOathSakura;
			default -> ExtraBotanyItems.judahOath;
		};
	}

	@Override
	protected Item getDefaultItem() {
		return ExtraBotanyItems.judahOath;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (this.ownerUUID != null) {
			tag.putUUID(TAG_OWNER, this.ownerUUID);
		}
		tag.putInt(TAG_VARIANT, getVariant().id());
		tag.putBoolean(TAG_LANDED, isLanded());
		tag.putInt(TAG_STANDBY, getStandbyTicks());
		tag.putFloat(TAG_DEPLOY_YAW, getDeployYaw());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.hasUUID(TAG_OWNER)) {
			this.ownerUUID = tag.getUUID(TAG_OWNER);
			this.cachedOwner = null;
		}
		setVariant(JudahOathItem.Variant.byId(tag.getInt(TAG_VARIANT)));
		setLanded(tag.getBoolean(TAG_LANDED));
		setStandbyTicks(tag.getInt(TAG_STANDBY));
		setDeployYaw(tag.getFloat(TAG_DEPLOY_YAW));
	}
}
