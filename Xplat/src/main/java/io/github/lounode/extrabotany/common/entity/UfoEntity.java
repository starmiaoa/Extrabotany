package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.List;
import java.util.UUID;

public class UfoEntity extends Entity {
	private static final EntityDataAccessor<Integer> CAUGHT_ID = SynchedEntityData.defineId(UfoEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> DAMAGE_TAKEN = SynchedEntityData.defineId(UfoEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Boolean> ACCESSORY_MOUNT = SynchedEntityData.defineId(UfoEntity.class, EntityDataSerializers.BOOLEAN);
	private static final TagKey<EntityType<?>> BOSSES = TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("forge", "bosses"));
	private static final DustParticleOptions TRACTOR_BEAM_DUST = new DustParticleOptions(new Vector3f(0.10F, 0.95F, 1.0F), 1.0F);
	private static final DustParticleOptions HOVER_GLOW_DUST = new DustParticleOptions(new Vector3f(0.20F, 0.85F, 1.0F), 0.75F);

	private static final String TAG_CAUGHT_UUID = "CaughtUuid";
	private static final String TAG_DAMAGE_TAKEN = "DamageTaken";
	private static final String TAG_ACCESSORY_MOUNT = "AccessoryMount";

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
	private boolean upInputDown;
	private boolean downInputDown;
	@Nullable
	private UUID caughtUuid;

	private boolean hasLastTickPos;
	private double lastTickX;
	private double lastTickZ;
	private float prevBankRoll;
	private float bankRoll;
	private float prevBankPitch;
	private float bankPitch;

	public UfoEntity(EntityType<? extends UfoEntity> entityType, Level level) {
		super(entityType, level);
		this.blocksBuilding = true;
		this.setNoGravity(true);
	}

	public UfoEntity(Level level, double x, double y, double z) {
		this(ExtraBotanyEntityType.UFO, level);
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
		this.entityData.define(CAUGHT_ID, -1);
		this.entityData.define(DAMAGE_TAKEN, 0F);
		this.entityData.define(ACCESSORY_MOUNT, false);
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
		this.setNoGravity(true);
		super.tick();
		this.tickLerp();
		Entity passenger = getControllingPassenger();
		if (passenger instanceof Player player) {
			this.setYRot(player.getYRot());
			if (this.isControlledByLocalInstance()) {
				Vec3 target = getInputMotion(player);
				Vec3 current = this.getDeltaMovement();
				Vec3 next = new Vec3(
					Mth.lerp(0.32D, current.x, target.x),
					Mth.lerp(0.32D, current.y, target.y),
					Mth.lerp(0.32D, current.z, target.z)
				);
				if (target.lengthSqr() == 0.0D && next.lengthSqr() < 1.0E-5D) {
					next = Vec3.ZERO;
				}
				this.setDeltaMovement(next);
				this.move(MoverType.SELF, this.getDeltaMovement());
			} else {
				this.setDeltaMovement(Vec3.ZERO);
			}
		} else {
			clearInput();
			if (!this.level().isClientSide() && isAccessoryMount() && this.tickCount > 3) {
				this.discard();
				return;
			}
			this.setDeltaMovement(this.getDeltaMovement().scale(0.68D));
			if (this.getDeltaMovement().lengthSqr() < 1.0E-5D) {
				this.setDeltaMovement(Vec3.ZERO);
			}
			this.move(MoverType.SELF, this.getDeltaMovement());
		}

		updateBankAngles();

		if (this.level().isClientSide()) {
			spawnClientParticles();
		} else {
			updateCaughtEntity();
		}

		this.fallDistance = 0F;
	}

	private void updateBankAngles() {
		double moveDx = 0.0D;
		double moveDz = 0.0D;
		if (this.hasLastTickPos) {
			moveDx = this.getX() - this.lastTickX;
			moveDz = this.getZ() - this.lastTickZ;
		}
		this.lastTickX = this.getX();
		this.lastTickZ = this.getZ();
		this.hasLastTickPos = true;

		float yawRad = this.getYRot() * Mth.DEG_TO_RAD;
		double fwdX = -Mth.sin(yawRad);
		double fwdZ = Mth.cos(yawRad);
		double rightX = -Mth.cos(yawRad);
		double rightZ = -Mth.sin(yawRad);

		double fwdSpeed = moveDx * fwdX + moveDz * fwdZ;
		double rightSpeed = moveDx * rightX + moveDz * rightZ;

		float targetPitch = (float) Mth.clamp((fwdSpeed / 0.78D) * 12.0D, -12.0D, 12.0D);
		float targetRoll = (float) Mth.clamp((rightSpeed / 0.78D) * 14.0D, -14.0D, 14.0D);

		this.prevBankPitch = this.bankPitch;
		this.bankPitch = Mth.lerp(0.25F, this.bankPitch, targetPitch);

		this.prevBankRoll = this.bankRoll;
		this.bankRoll = Mth.lerp(0.25F, this.bankRoll, targetRoll);
	}

	private void spawnClientParticles() {
		if (this.isVehicle() && this.tickCount % 2 == 0) {
			float angle = (this.tickCount * 0.45F) % Mth.TWO_PI;
			double rx = this.getX() + Mth.cos(angle) * 0.95D;
			double rz = this.getZ() + Mth.sin(angle) * 0.95D;
			this.level().addParticle(HOVER_GLOW_DUST, rx, this.getY() + 0.05D, rz, 0.0D, -0.02D, 0.0D);
			if (this.tickCount % 6 == 0) {
				this.level().addParticle(ParticleTypes.END_ROD,
					this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D,
					this.getY() - 0.05D,
					this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D,
					0.0D, -0.04D, 0.0D);
			}
		}

		if (hasCaughtEntity()) {
			Entity caught = this.level().getEntity(getCaughtId());
			double bottomY = caught != null ? caught.getY() + caught.getBbHeight() * 0.5D : this.getY() - 2.6D;
			for (int i = 0; i < 3; i++) {
				double t = this.random.nextDouble();
				double angle = this.tickCount * 0.35D + i * 2.1D;
				double radius = Mth.lerp(t, 0.75D, 0.25D);
				double px = this.getX() + Math.cos(angle) * radius;
				double py = Mth.lerp(t, bottomY, this.getY() - 0.15D);
				double pz = this.getZ() + Math.sin(angle) * radius;
				this.level().addParticle(TRACTOR_BEAM_DUST, px, py, pz, 0.0D, 0.08D, 0.0D);
			}
			if (this.tickCount % 2 == 0) {
				this.level().addParticle(ParticleTypes.END_ROD,
					this.getX() + (this.random.nextDouble() - 0.5D) * 0.6D,
					bottomY,
					this.getZ() + (this.random.nextDouble() - 0.5D) * 0.6D,
					0.0D, 0.12D, 0.0D);
			}
		}
	}

	private Vec3 getInputMotion(Player player) {
		float yawRad = player.getYRot() * Mth.DEG_TO_RAD;
		Vec3 forward = new Vec3(-Mth.sin(yawRad), 0D, Mth.cos(yawRad));
		Vec3 left = new Vec3(Mth.cos(yawRad), 0D, Mth.sin(yawRad));

		double fwdIn = (forwardInputDown ? 1.0D : 0.0D) - (backInputDown ? 1.0D : 0.0D);
		double leftIn = (leftInputDown ? 1.0D : 0.0D) - (rightInputDown ? 1.0D : 0.0D);
		Vec3 horiz = forward.scale(fwdIn).add(left.scale(leftIn));
		if (horiz.lengthSqr() > 1.0E-4D) {
			horiz = horiz.normalize().scale(0.78D);
		}

		double vert = 0.0D;
		if (upInputDown) {
			vert += 0.48D;
		}
		if (downInputDown) {
			vert -= 0.48D;
		}
		if (this.onGround() && vert < 0.0D) {
			vert = 0.0D;
		}

		return new Vec3(horiz.x, vert, horiz.z);
	}

	private void updateCaughtEntity() {
		int id = getCaughtId();
		if (id == -1 && caughtUuid == null) {
			return;
		}
		Entity caught = caughtUuid != null && this.level() instanceof ServerLevel serverLevel
				? serverLevel.getEntity(caughtUuid)
				: this.level().getEntity(id);
		if (caught == null) {
			clearCaughtEntity();
			return;
		}
		if (!(caught instanceof LivingEntity living) || !isCapturable(living) || living.distanceTo(this) >= 16F) {
			clearCaughtEntity();
			return;
		}
		caughtUuid = caught.getUUID();
		if (id != caught.getId()) {
			setCaughtId(caught.getId());
		}

		Vec3 target = new Vec3(this.getX() - caught.getX(), this.getY() - 2.2D - caught.getY(), this.getZ() - caught.getZ());
		double dist = target.length();
		if (dist > 0.08D) {
			double pull = Math.min(0.65D, dist * 0.35D);
			caught.setDeltaMovement(target.scale(pull / dist));
		} else {
			caught.setDeltaMovement(Vec3.ZERO);
		}
		caught.hurtMarked = true;
		caught.fallDistance = 0F;
	}

	public void updateInput(boolean forward, boolean back, boolean left, boolean right, boolean up, boolean down, boolean catchPressed) {
		this.forwardInputDown = forward;
		this.backInputDown = back;
		this.leftInputDown = left;
		this.rightInputDown = right;
		this.upInputDown = up;
		this.downInputDown = down;
		if (catchPressed) {
			toggleCaughtEntity();
		}
	}

	private void clearInput() {
		this.forwardInputDown = false;
		this.backInputDown = false;
		this.leftInputDown = false;
		this.rightInputDown = false;
		this.upInputDown = false;
		this.downInputDown = false;
	}

	private boolean isCapturable(LivingEntity living) {
		return living.isAlive()
				&& !(living instanceof Player)
				&& !living.getType().is(BOSSES)
				&& !living.isPassenger()
				&& !this.hasPassenger(living);
	}

	private void toggleCaughtEntity() {
		if (this.level().isClientSide()) {
			return;
		}
		if (getCaughtId() != -1 || caughtUuid != null) {
			clearCaughtEntity();
			return;
		}
		AABB searchBox = new AABB(
			this.getX() - 2.0D, this.getY() - 16.0D, this.getZ() - 2.0D,
			this.getX() + 2.0D, this.getY() - 0.5D, this.getZ() + 2.0D
		);
		List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
		LivingEntity closest = null;
		double bestDistSqr = Double.MAX_VALUE;
		for (LivingEntity living : entities) {
			if (!isCapturable(living)) {
				continue;
			}
			double dx = living.getX() - this.getX();
			double dz = living.getZ() - this.getZ();
			if (dx * dx + dz * dz > 4.0D) {
				continue;
			}
			double distSqr = living.distanceToSqr(this);
			if (distSqr < bestDistSqr) {
				bestDistSqr = distSqr;
				closest = living;
			}
		}
		if (closest != null) {
			setCaughtEntity(closest);
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
					this.spawnAtLocation(new ItemStack(ExtraBotanyItems.cosmicCarKey));
				}
				this.discard();
			}
		}
		return true;
	}

	@Override
	protected void positionRider(Entity passenger, MoveFunction callback) {
		if (this.hasPassenger(passenger)) {
			callback.accept(passenger, this.getX(), this.getY() + 0.55D, this.getZ());
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
		return this.getPassengers().isEmpty();
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
		if (caughtUuid != null) {
			tag.putUUID(TAG_CAUGHT_UUID, caughtUuid);
		}
		tag.putFloat(TAG_DAMAGE_TAKEN, getDamageTaken());
		tag.putBoolean(TAG_ACCESSORY_MOUNT, isAccessoryMount());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		caughtUuid = tag.hasUUID(TAG_CAUGHT_UUID) ? tag.getUUID(TAG_CAUGHT_UUID) : null;
		setCaughtId(-1);
		setDamageTaken(tag.getFloat(TAG_DAMAGE_TAKEN));
		setAccessoryMount(tag.getBoolean(TAG_ACCESSORY_MOUNT));
	}

	public int getCaughtId() {
		return this.entityData.get(CAUGHT_ID);
	}

	public void setCaughtId(int id) {
		this.entityData.set(CAUGHT_ID, id);
	}

	public boolean hasCaughtEntity() {
		return getCaughtId() != -1;
	}

	public float getBankRoll(float partialTick) {
		return Mth.lerp(partialTick, this.prevBankRoll, this.bankRoll);
	}

	public float getBankPitch(float partialTick) {
		return Mth.lerp(partialTick, this.prevBankPitch, this.bankPitch);
	}

	private void setCaughtEntity(Entity entity) {
		caughtUuid = entity.getUUID();
		setCaughtId(entity.getId());
	}

	private void clearCaughtEntity() {
		caughtUuid = null;
		setCaughtId(-1);
	}

	private float getDamageTaken() {
		return this.entityData.get(DAMAGE_TAKEN);
	}

	private void setDamageTaken(float damage) {
		this.entityData.set(DAMAGE_TAKEN, damage);
	}

	public boolean isAccessoryMount() {
		return this.entityData.get(ACCESSORY_MOUNT);
	}

	public void setAccessoryMount(boolean accessoryMount) {
		this.entityData.set(ACCESSORY_MOUNT, accessoryMount);
	}
}
