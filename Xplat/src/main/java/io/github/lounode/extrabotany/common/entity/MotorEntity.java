package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MotorEntity extends Entity {
	private static final EntityDataAccessor<Float> DAMAGE_TAKEN = SynchedEntityData.defineId(MotorEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> TECTONIC_ENERGY = SynchedEntityData.defineId(MotorEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> CYCLONE_TICKS = SynchedEntityData.defineId(MotorEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> LEAN = SynchedEntityData.defineId(MotorEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> PITCH = SynchedEntityData.defineId(MotorEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(MotorEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Boolean> ACCESSORY_MOUNT = SynchedEntityData.defineId(MotorEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> CHARGED = SynchedEntityData.defineId(MotorEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> BOOSTING = SynchedEntityData.defineId(MotorEntity.class, EntityDataSerializers.BOOLEAN);

	private static final String TAG_DAMAGE_TAKEN = "DamageTaken";
	private static final String TAG_TECTONIC_ENERGY = "TectonicEnergy";
	private static final String TAG_CYCLONE_TICKS = "CycloneTicks";
	private static final String TAG_LEAN = "Lean";
	private static final String TAG_PITCH = "Pitch";
	private static final String TAG_OWNER = "Owner";
	private static final String TAG_ACCESSORY_MOUNT = "AccessoryMount";

	private static final DustParticleOptions KEY_OF_TRUTH_LASER = new DustParticleOptions(new Vector3f(0.0F, 0.90F, 1.0F), 1.1F);
	private static final DustParticleOptions CYCLONE_DUST = new DustParticleOptions(new Vector3f(0.25F, 0.92F, 1.0F), 1.0F);
	private static final double WHEEL_RADIUS = 0.42D;
	private static final int CYCLONE_DURATION = 15;

	private int lerpSteps;
	private double lerpX;
	private double lerpY;
	private double lerpZ;
	private double lerpYRot;
	private double lerpXRot;

	private boolean forwardInputDown;
	private boolean backInputDown;
	private boolean leftInputDown;
	private boolean rightInputDown;
	private boolean jumpInputDown;
	private boolean cyclonePressed;
	private int ridingTicks;
	private int lastRideOnSoundTick = -20;

	private boolean hasLastTickPos;
	private double lastTickX;
	private double lastTickZ;
	private double measuredHorizSpeed;

	private float prevLean;
	private float smoothLean;
	private float prevPitch;
	private float smoothPitch;
	private float prevWheelSpin;
	private float wheelSpin;
	private float prevCycloneSpin;
	private float cycloneSpin;

	public MotorEntity(EntityType<? extends MotorEntity> entityType, Level level) {
		super(entityType, level);
		this.blocksBuilding = true;
		this.setMaxUpStep(1.0F);
	}

	public MotorEntity(Level level, double x, double y, double z) {
		this(ExtraBotanyEntityType.MOTOR, level);
		this.setPos(x, y, z);
		this.xo = x;
		this.yo = y;
		this.zo = z;
		this.lastTickX = x;
		this.lastTickZ = z;
		this.hasLastTickPos = true;
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(DAMAGE_TAKEN, 0F);
		this.entityData.define(TECTONIC_ENERGY, 0);
		this.entityData.define(CYCLONE_TICKS, 0);
		this.entityData.define(LEAN, 0F);
		this.entityData.define(PITCH, 0F);
		this.entityData.define(OWNER, Optional.empty());
		this.entityData.define(ACCESSORY_MOUNT, false);
		this.entityData.define(CHARGED, false);
		this.entityData.define(BOOSTING, false);
	}

	@Override
	public float maxUpStep() {
		return 1.0F;
	}

	@Override
	public boolean ignoreExplosion() {
		return true;
	}

	@Override
	public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean teleport) {
		this.lerpX = x;
		this.lerpY = y;
		this.lerpZ = z;
		this.lerpYRot = yRot;
		this.lerpXRot = xRot;
		this.lerpSteps = 10;
	}

	private void tickLerp() {
		if (this.isControlledByLocalInstance()) {
			this.lerpSteps = 0;
			this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
		}
		if (this.lerpSteps > 0) {
			double d0 = this.getX() + (this.lerpX - this.getX()) / (double) this.lerpSteps;
			double d1 = this.getY() + (this.lerpY - this.getY()) / (double) this.lerpSteps;
			double d2 = this.getZ() + (this.lerpZ - this.getZ()) / (double) this.lerpSteps;
			double d3 = Mth.wrapDegrees(this.lerpYRot - (double) this.getYRot());
			this.setYRot(this.getYRot() + (float) d3 / (float) this.lerpSteps);
			this.setXRot(this.getXRot() + (float) (this.lerpXRot - (double) this.getXRot()) / (float) this.lerpSteps);
			this.lerpSteps--;
			this.setPos(d0, d1, d2);
			this.setRot(this.getYRot(), this.getXRot());
		}
	}

	@Override
	public void tick() {
		super.tick();
		this.tickLerp();
		this.setNoGravity(false);

		if (getControllingPassenger() instanceof Player player) {
			this.setYRot(player.getYRot());
			this.ridingTicks++;

			if (this.isControlledByLocalInstance()) {
				Vec3 motion = calculateRideMotion(player);
				this.setDeltaMovement(motion);
				this.move(MoverType.SELF, this.getDeltaMovement());
			} else {
				this.setDeltaMovement(Vec3.ZERO);
			}

			if (!this.level().isClientSide()) {
				tickServerEffects(player);
			}
		} else {
			this.ridingTicks = 0;
			clearInput();
			if (!this.level().isClientSide()) {
				if (isAccessoryMount() && this.tickCount > 3) {
					this.discard();
					return;
				}
				setCharged(false);
				setBoosting(false);
				setCycloneTicks(0);
				setLean(0F);
				setPitch(0F);
			}
			Vec3 motion = this.getDeltaMovement();
			double vy = motion.y;
			if (this.isInWater()) {
				vy = Math.max(vy, 0.04D);
			} else if (!this.onGround()) {
				vy = Math.max(-0.8D, vy - 0.04D);
			}
			this.setDeltaMovement(motion.x * 0.80D, vy, motion.z * 0.80D);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().multiply(0.90D, 0.98D, 0.90D));
		}

		updateMovementAndAnimationState();

		if (this.level().isClientSide()) {
			spawnClientParticles();
		}

		List<Entity> nearby = this.level().getEntities(this, this.getBoundingBox().inflate(0.2F, -0.01F, 0.2F),
			entity -> entity.isPushable() && !this.hasPassenger(entity));
		for (Entity entity : nearby) {
			this.push(entity);
		}

		this.fallDistance = 0F;
	}

	private void updateMovementAndAnimationState() {
		double moveDx = 0.0D;
		double moveDz = 0.0D;
		if (this.hasLastTickPos) {
			moveDx = this.getX() - this.lastTickX;
			moveDz = this.getZ() - this.lastTickZ;
		}
		this.lastTickX = this.getX();
		this.lastTickZ = this.getZ();
		this.hasLastTickPos = true;
		this.measuredHorizSpeed = Math.hypot(moveDx, moveDz);

		float yawRad = this.getYRot() * Mth.DEG_TO_RAD;
		double forwardDisp = moveDx * (-Mth.sin(yawRad)) + moveDz * Mth.cos(yawRad);

		this.prevWheelSpin = this.wheelSpin;
		this.wheelSpin += (float) (forwardDisp / WHEEL_RADIUS);

		this.prevLean = this.smoothLean;
		float targetLean = getLean();
		if (this.isControlledByLocalInstance() && getCycloneTicks() == 0) {
			if (leftInputDown && !rightInputDown) {
				targetLean = 12F;
			} else if (rightInputDown && !leftInputDown) {
				targetLean = -12F;
			} else {
				targetLean = 0F;
			}
		}
		this.smoothLean = Mth.lerp(0.25F, this.smoothLean, targetLean);

		this.prevPitch = this.smoothPitch;
		float targetPitch = getPitch();
		if (this.isControlledByLocalInstance() && isLocalBoosting()) {
			targetPitch = -6F;
		}
		this.smoothPitch = Mth.lerp(0.25F, this.smoothPitch, targetPitch);

		int cyc = getCycloneTicks();
		if (cyc > 0) {
			this.prevCycloneSpin = this.cycloneSpin;
			float targetSpin = ((CYCLONE_DURATION - cyc) / (float) CYCLONE_DURATION) * 360.0F;
			if (targetSpin > this.cycloneSpin) {
				this.cycloneSpin = targetSpin;
			} else {
				this.cycloneSpin = Math.min(360.0F, this.cycloneSpin + (360.0F / CYCLONE_DURATION));
			}
		} else if (this.cycloneSpin > 0F && this.cycloneSpin < 360F) {
			this.prevCycloneSpin = this.cycloneSpin;
			this.cycloneSpin = 360.0F;
		} else {
			this.prevCycloneSpin = 0F;
			this.cycloneSpin = 0F;
		}
	}

	private boolean isLocalBoosting() {
		return (this.ridingTicks >= 120 || isCharged()) && jumpInputDown && getTectonicEnergy() >= 200;
	}

	private Vec3 calculateRideMotion(Player player) {
		Vec3 current = this.getDeltaMovement();
		boolean boosting = isLocalBoosting();
		Vec3 forward = forwardVector(player, 1.0D);
		Vec3 left = forward.yRot(Mth.HALF_PI);

		double forwardTarget = 0.0D;
		if (boosting) {
			forwardTarget = 1.35D;
		} else if (forwardInputDown) {
			forwardTarget = 0.58D;
		} else if (backInputDown) {
			forwardTarget = -0.22D;
		}

		double strafeTarget = 0.0D;
		double strafeMag = (forwardInputDown || boosting) ? 0.28D : 0.24D;
		if (leftInputDown) {
			strafeTarget += strafeMag;
		}
		if (rightInputDown) {
			strafeTarget -= strafeMag;
		}

		Vec3 targetHoriz = forward.scale(forwardTarget).add(left.scale(strafeTarget));
		double maxHoriz = boosting ? 1.38D : 0.60D;
		double horizLen = targetHoriz.horizontalDistance();
		if (horizLen > maxHoriz) {
			targetHoriz = targetHoriz.scale(maxHoriz / horizLen);
		}

		double vx;
		double vz;
		boolean hasHorizInput = boosting || forwardInputDown || backInputDown || leftInputDown || rightInputDown;
		if (hasHorizInput) {
			double accel = boosting ? 0.28D : 0.24D;
			vx = Mth.lerp(accel, current.x, targetHoriz.x);
			vz = Mth.lerp(accel, current.z, targetHoriz.z);
		} else {
			vx = current.x * 0.80D;
			vz = current.z * 0.80D;
			if (vx * vx + vz * vz < 1.0E-4D) {
				vx = 0.0D;
				vz = 0.0D;
			}
		}

		double vy;
		if (boosting) {
			vy = this.horizontalCollision ? 0.03D : 0.0D;
			if (this.isInWater()) {
				vy = Math.max(vy, 0.08D);
			}
		} else if (this.isInWater()) {
			vy = Math.max(current.y, 0.08D);
		} else {
			vy = this.onGround() ? Math.max(0.0D, current.y) : Math.max(-0.8D, current.y - 0.05D);
			if (forwardInputDown && this.horizontalCollision) {
				vy = Math.max(vy, 0.08D);
			}
		}

		return new Vec3(vx, vy, vz);
	}

	private void spawnClientParticles() {
		float yawRad = this.getYRot() * Mth.DEG_TO_RAD;
		double fwdX = -Mth.sin(yawRad);
		double fwdZ = Mth.cos(yawRad);
		double rightX = -Mth.cos(yawRad);
		double rightZ = -Mth.sin(yawRad);

		boolean boosting = isBoosting() || (this.isControlledByLocalInstance() && isLocalBoosting());
		if (boosting) {
			for (int side = -1; side <= 1; side += 2) {
				double px = this.getX() - fwdX * 1.05D + rightX * (side * 0.30D);
				double py = this.getY() + 0.42D;
				double pz = this.getZ() - fwdZ * 1.05D + rightZ * (side * 0.30D);
				this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz,
					-fwdX * 0.35D + (this.random.nextDouble() - 0.5D) * 0.06D,
					0.04D + this.random.nextDouble() * 0.04D,
					-fwdZ * 0.35D + (this.random.nextDouble() - 0.5D) * 0.06D);
				this.level().addParticle(ParticleTypes.FLAME, px, py - 0.04D, pz,
					-fwdX * 0.28D, 0.03D, -fwdZ * 0.28D);
				if (this.tickCount % 2 == 0) {
					this.level().addParticle(ParticleTypes.END_ROD, px, py, pz,
						-fwdX * 0.22D, 0.02D, -fwdZ * 0.22D);
				}
			}
		} else if (this.isVehicle() && (forwardInputDown || this.measuredHorizSpeed > 0.18D)) {
			double side = (this.tickCount & 1) == 0 ? 0.28D : -0.28D;
			double px = this.getX() - fwdX * 1.02D + rightX * side;
			double py = this.getY() + 0.40D;
			double pz = this.getZ() - fwdZ * 1.02D + rightZ * side;
			this.level().addParticle(isCharged() ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
				px, py, pz, -fwdX * 0.18D, 0.05D, -fwdZ * 0.18D);
		}

		int cyc = getCycloneTicks();
		if (cyc > 0) {
			float baseAngle = ((CYCLONE_DURATION - cyc) / (float) CYCLONE_DURATION) * Mth.TWO_PI;
			for (int i = 0; i < 6; i++) {
				float angle = baseAngle + i * (Mth.TWO_PI / 6.0F);
				double radius = 2.6D + (i % 2) * 1.2D;
				double rx = this.getX() + Mth.cos(angle) * radius;
				double rz = this.getZ() + Mth.sin(angle) * radius;
				double ry = this.getY() + 0.35D + (i % 3) * 0.25D;
				double vx = -Mth.sin(angle) * 0.25D;
				double vz = Mth.cos(angle) * 0.25D;
				this.level().addParticle(ParticleTypes.SNOWFLAKE, rx, ry, rz, vx, 0.04D, vz);
				this.level().addParticle(CYCLONE_DUST, rx, ry + 0.1D, rz, vx * 0.5D, 0.02D, vz * 0.5D);
			}
			this.level().addParticle(ParticleTypes.CLOUD,
				this.getX() + (this.random.nextDouble() - 0.5D) * 3.0D,
				this.getY() + 0.4D,
				this.getZ() + (this.random.nextDouble() - 0.5D) * 3.0D,
				0.0D, 0.05D, 0.0D);
		}
	}

	private void tickServerEffects(Player player) {
		boolean activateCyclone = cyclonePressed;
		cyclonePressed = false;

		if (leftInputDown && !rightInputDown) {
			setLean(12F);
		} else if (rightInputDown && !leftInputDown) {
			setLean(-12F);
		} else {
			setLean(0F);
		}

		boolean charged = ridingTicks >= 120;
		setCharged(charged);

		if (charged) {
			setTectonicEnergy(Math.min(800, getTectonicEnergy() + 2));
			boolean boosting = jumpInputDown && getTectonicEnergy() >= 200;
			setBoosting(boosting);
			if (boosting) {
				setTectonicEnergy(Math.max(0, getTectonicEnergy() - 6));
				setPitch(-6F);
			} else {
				setPitch(0F);
			}

			if (activateCyclone && getCycloneTicks() == 0 && getTectonicEnergy() >= 400) {
				setCycloneTicks(CYCLONE_DURATION);
				setTectonicEnergy(getTectonicEnergy() - 400);
				this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
					ExtraBotanySounds.MOTOR_CYCLONE, SoundSource.PLAYERS, 1.2F, 1.0F);
			}

			tickCyclone(player);
			tickKeyOfTruthTurret(player);

			if (player.getHealth() < player.getMaxHealth() * 0.5F) {
				player.heal(0.5F);
			}
		} else {
			setBoosting(false);
			setPitch(0F);
		}
	}

	private Vec3 forwardVector(Player player, double speed) {
		float yaw = player.getYRot() * Mth.DEG_TO_RAD;
		return new Vec3(-Mth.sin(yaw) * speed, 0D, Mth.cos(yaw) * speed);
	}

	private void tickCyclone(Player player) {
		int ticks = getCycloneTicks();
		if (ticks <= 0) {
			return;
		}
		setCycloneTicks(ticks - 1);
		setPitch(-6F);
		setLean(ticks > 6 ? -14F : -6F);
		if (ticks == 12 || ticks == 6) {
			if (this.level() instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
					this.getX(), this.getY() + 0.5D, this.getZ(),
					16, 2.2D, 0.4D, 2.2D, 0.08D);
				serverLevel.sendParticles(ParticleTypes.CLOUD,
					this.getX(), this.getY() + 0.4D, this.getZ(),
					10, 2.0D, 0.3D, 2.0D, 0.04D);
			}
			for (LivingEntity living : getNearbyLiving(4.5F)) {
				if (!living.isAlive() || living == player || this.hasPassenger(living)
						|| living.distanceToSqr(this) > 4.5D * 4.5D
						|| !DamageHandler.INSTANCE.checkPassable(living, player)) {
					continue;
				}
				living.hurt(this.damageSources().indirectMagic(this, player), 4.5F);
				living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
				player.setLastHurtMob(living);
			}
		}
	}

	private void tickKeyOfTruthTurret(Player player) {
		if (this.tickCount % 15 != 0) {
			return;
		}
		LivingEntity closest = null;
		double bestDistSqr = 12.0D * 12.0D;
		for (LivingEntity living : getNearbyLiving(12F)) {
			if (!living.isAlive() || living == player || this.hasPassenger(living)) {
				continue;
			}
			if (!(living instanceof Enemy || player.getLastHurtMob() == living)) {
				continue;
			}
			double distSqr = living.distanceToSqr(this);
			if (distSqr > bestDistSqr) {
				continue;
			}
			if (!player.hasLineOfSight(living) || !DamageHandler.INSTANCE.checkPassable(living, player)) {
				continue;
			}
			bestDistSqr = distSqr;
			closest = living;
		}
		if (closest == null) {
			return;
		}

		if (this.level() instanceof ServerLevel serverLevel) {
			float yawRad = this.getYRot() * Mth.DEG_TO_RAD;
			double fwdX = -Mth.sin(yawRad);
			double fwdZ = Mth.cos(yawRad);
			double rightX = -Mth.cos(yawRad);
			double rightZ = -Mth.sin(yawRad);
			double side = ((this.tickCount / 15) & 1) == 0 ? 1.15D : -1.15D;

			Vec3 podPos = new Vec3(
				this.getX() + rightX * side - fwdX * 0.35D,
				this.getY() + 1.45D,
				this.getZ() + rightZ * side - fwdZ * 0.35D
			);
			Vec3 targetPos = closest.getBoundingBox().getCenter();
			Vec3 delta = targetPos.subtract(podPos);
			double dist = delta.length();
			int steps = Mth.clamp((int) (dist * 3.0D), 8, 36);
			for (int i = 0; i <= steps; i++) {
				double t = i / (double) steps;
				double px = podPos.x + delta.x * t;
				double py = podPos.y + delta.y * t;
				double pz = podPos.z + delta.z * t;
				serverLevel.sendParticles(KEY_OF_TRUTH_LASER, px, py, pz, 1, 0.02D, 0.02D, 0.02D, 0.0D);
				if (i % 3 == 0) {
					serverLevel.sendParticles(ParticleTypes.END_ROD, px, py, pz, 1, 0.0D, 0.0D, 0.0D, 0.0D);
				}
			}
			serverLevel.sendParticles(ParticleTypes.END_ROD, targetPos.x, targetPos.y, targetPos.z, 4, 0.15D, 0.15D, 0.15D, 0.04D);
		}

		this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
			ExtraBotanySounds.MOTOR_SHOOT, SoundSource.PLAYERS, 0.35F, 1.0F);
		closest.hurt(this.damageSources().indirectMagic(this, player), 4.0F);
		closest.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
		player.setLastHurtMob(closest);
	}

	private List<LivingEntity> getNearbyLiving(float range) {
		return this.level().getEntitiesOfClass(LivingEntity.class,
				new AABB(this.getX() - range, this.getY() - range, this.getZ() - range,
						this.getX() + range, this.getY() + range, this.getZ() + range));
	}

	@Override
	public void playerTouch(Player player) {
		super.playerTouch(player);
	}

	@Override
	public void push(Entity entity) {
		super.push(entity);
		damageOnCollision(entity);
	}

	private void damageOnCollision(Entity entity) {
		if (this.level().isClientSide() || this.measuredHorizSpeed <= 0.15D || !(getControllingPassenger() instanceof Player player)) {
			return;
		}
		if (!(entity instanceof LivingEntity living) || entity == player || this.hasPassenger(entity)) {
			return;
		}
		if (DamageHandler.INSTANCE.checkPassable(living, player) && living.hurt(this.damageSources().indirectMagic(this, player), 7F)) {
			living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2));
			player.setLastHurtMob(living);
		}
	}

	public void updateInput(boolean forward, boolean back, boolean left, boolean right, boolean jump, boolean cyclonePressed) {
		this.forwardInputDown = forward;
		this.backInputDown = back;
		this.leftInputDown = left;
		this.rightInputDown = right;
		this.jumpInputDown = jump;
		if (cyclonePressed) {
			this.cyclonePressed = true;
		}
	}

	public void clearInput() {
		this.forwardInputDown = false;
		this.backInputDown = false;
		this.leftInputDown = false;
		this.rightInputDown = false;
		this.jumpInputDown = false;
		this.cyclonePressed = false;
	}

	@Override
	protected void addPassenger(Entity passenger) {
		super.addPassenger(passenger);
		if (!this.level().isClientSide() && passenger instanceof Player && this.getControllingPassenger() == passenger) {
			if (this.tickCount - this.lastRideOnSoundTick >= 10) {
				this.lastRideOnSoundTick = this.tickCount;
				this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
					ExtraBotanySounds.MOTOR_RIDEON, SoundSource.PLAYERS, 1.5F, 1.0F);
			}
		}
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		if (player.isSecondaryUseActive()) {
			return InteractionResult.PASS;
		}
		if (!this.level().isClientSide()) {
			return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (isInvulnerableTo(source)) {
			return false;
		}
		if (!this.level().isClientSide() && !this.isRemoved()) {
			Entity attacker = source.getEntity();
			if (attacker != null && this.hasPassenger(attacker)) {
				return false;
			}
			float damage = getDamageTaken();
			if (attacker instanceof Player) {
				damage += amount * 10F;
				setDamageTaken(damage);
			}
			boolean creative = attacker instanceof Player player && player.getAbilities().instabuild;
			if (creative || damage > 40F) {
				if (!creative && !isAccessoryMount() && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
					this.spawnAtLocation(getItemStack());
				}
				this.discard();
			}
		}
		return true;
	}

	private ItemStack getItemStack() {
		ItemStack stack = new ItemStack(ExtraBotanyItems.motor);
		getOwner().ifPresent(owner -> stack.getOrCreateTag().putString("soulbindUUID", owner.toString()));
		return stack;
	}

	@Override
	protected void positionRider(Entity passenger, MoveFunction callback) {
		if (!this.hasPassenger(passenger)) {
			return;
		}
		float yawRad = this.getYRot() * Mth.DEG_TO_RAD;
		double fwdX = -Mth.sin(yawRad);
		double fwdZ = Mth.cos(yawRad);
		int seatIndex = this.getPassengers().indexOf(passenger);
		if (seatIndex == 0) {
			callback.accept(passenger, this.getX() - fwdX * 0.08D, this.getY() + 0.42D, this.getZ() - fwdZ * 0.08D);
		} else {
			callback.accept(passenger, this.getX() - fwdX * 0.78D, this.getY() + 0.46D, this.getZ() - fwdZ * 0.78D);
		}
	}

	@Nullable
	@Override
	public LivingEntity getControllingPassenger() {
		Entity passenger = this.getFirstPassenger();
		return passenger instanceof LivingEntity living ? living : null;
	}

	@Override
	protected boolean canAddPassenger(Entity passenger) {
		return this.getPassengers().size() < 2;
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public boolean canCollideWith(Entity entity) {
		return entity.canBeCollidedWith() && !this.isPassengerOfSameVehicle(entity);
	}

	@Override
	public boolean isPickable() {
		return !this.isRemoved();
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putFloat(TAG_DAMAGE_TAKEN, getDamageTaken());
		tag.putInt(TAG_TECTONIC_ENERGY, getTectonicEnergy());
		tag.putInt(TAG_CYCLONE_TICKS, getCycloneTicks());
		tag.putFloat(TAG_LEAN, getLean());
		tag.putFloat(TAG_PITCH, getPitch());
		tag.putBoolean(TAG_ACCESSORY_MOUNT, isAccessoryMount());
		getOwner().ifPresent(owner -> tag.putUUID(TAG_OWNER, owner));
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		setDamageTaken(tag.getFloat(TAG_DAMAGE_TAKEN));
		setTectonicEnergy(tag.getInt(TAG_TECTONIC_ENERGY));
		setCycloneTicks(tag.getInt(TAG_CYCLONE_TICKS));
		setLean(tag.getFloat(TAG_LEAN));
		setPitch(tag.getFloat(TAG_PITCH));
		setAccessoryMount(tag.getBoolean(TAG_ACCESSORY_MOUNT));
		if (tag.hasUUID(TAG_OWNER)) {
			setOwner(tag.getUUID(TAG_OWNER));
		}
	}

	public float getDamageTaken() {
		return this.entityData.get(DAMAGE_TAKEN);
	}

	private void setDamageTaken(float damage) {
		this.entityData.set(DAMAGE_TAKEN, damage);
	}

	public int getTectonicEnergy() {
		return this.entityData.get(TECTONIC_ENERGY);
	}

	public void setTectonicEnergy(int energy) {
		this.entityData.set(TECTONIC_ENERGY, Mth.clamp(energy, 0, 800));
	}

	public int getCycloneTicks() {
		return this.entityData.get(CYCLONE_TICKS);
	}

	public void setCycloneTicks(int ticks) {
		this.entityData.set(CYCLONE_TICKS, Math.max(0, ticks));
	}

	public float getLean() {
		return this.entityData.get(LEAN);
	}

	public float getLean(float partialTick) {
		return Mth.lerp(partialTick, this.prevLean, this.smoothLean);
	}

	private void setLean(float lean) {
		this.entityData.set(LEAN, lean);
	}

	public float getPitch() {
		return this.entityData.get(PITCH);
	}

	public float getPitch(float partialTick) {
		return Mth.lerp(partialTick, this.prevPitch, this.smoothPitch);
	}

	private void setPitch(float pitch) {
		this.entityData.set(PITCH, pitch);
	}

	public float getWheelSpin(float partialTick) {
		return Mth.lerp(partialTick, this.prevWheelSpin, this.wheelSpin);
	}

	public float getCycloneSpin(float partialTick) {
		return Mth.lerp(partialTick, this.prevCycloneSpin, this.cycloneSpin);
	}

	public boolean isCharged() {
		return this.entityData.get(CHARGED);
	}

	private void setCharged(boolean charged) {
		this.entityData.set(CHARGED, charged);
	}

	public boolean isBoosting() {
		return this.entityData.get(BOOSTING);
	}

	private void setBoosting(boolean boosting) {
		this.entityData.set(BOOSTING, boosting);
	}

	public Optional<UUID> getOwner() {
		return this.entityData.get(OWNER);
	}

	public void setOwner(@Nullable UUID owner) {
		this.entityData.set(OWNER, Optional.ofNullable(owner));
	}

	public int getRidingTicks() {
		return ridingTicks;
	}

	public boolean isAccessoryMount() {
		return this.entityData.get(ACCESSORY_MOUNT);
	}

	public void setAccessoryMount(boolean accessoryMount) {
		this.entityData.set(ACCESSORY_MOUNT, accessoryMount);
	}
}
