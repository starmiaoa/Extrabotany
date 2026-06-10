package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.api.mana.ManaItemHandler;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.tool.FlyingBoatItem;

import org.jetbrains.annotations.Nullable;

public class FlyingBoatEntity extends Entity {
	private static final EntityDataAccessor<Integer> TIME_SINCE_HIT = SynchedEntityData.defineId(FlyingBoatEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> FORWARD_DIRECTION = SynchedEntityData.defineId(FlyingBoatEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> DAMAGE_TAKEN = SynchedEntityData.defineId(FlyingBoatEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(FlyingBoatEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> LEFT_PADDLE = SynchedEntityData.defineId(FlyingBoatEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> RIGHT_PADDLE = SynchedEntityData.defineId(FlyingBoatEntity.class, EntityDataSerializers.BOOLEAN);

	private static final String TAG_TIME_SINCE_HIT = "TimeSinceHit";
	private static final String TAG_FORWARD_DIRECTION = "ForwardDirection";
	private static final String TAG_DAMAGE_TAKEN = "DamageTaken";
	private static final String TAG_VARIANT = "Variant";

	private final float[] paddlePositions = new float[2];
	private float deltaRotation;

	private boolean forwardInputDown;
	private boolean backInputDown;
	private boolean leftInputDown;
	private boolean rightInputDown;
	private boolean upInputDown;
	private boolean downInputDown;

	public FlyingBoatEntity(EntityType<? extends FlyingBoatEntity> entityType, Level level) {
		super(entityType, level);
		this.blocksBuilding = true;
	}

	public FlyingBoatEntity(Level level, double x, double y, double z, FlyingBoatItem.Variant variant) {
		this(ExtraBotanyEntityType.FLYING_BOAT, level);
		this.setPos(x, y, z);
		this.xo = x;
		this.yo = y;
		this.zo = z;
		setVariant(variant);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(TIME_SINCE_HIT, 0);
		builder.define(FORWARD_DIRECTION, 1);
		builder.define(DAMAGE_TAKEN, 0F);
		builder.define(VARIANT, FlyingBoatItem.Variant.MANASTEEL.ordinal());
		builder.define(LEFT_PADDLE, false);
		builder.define(RIGHT_PADDLE, false);
	}

	@Override
	public void tick() {
		super.tick();
		tickHurtState();

		if (!this.level().isClientSide()) {
			if (getControllingPassenger() instanceof Player player) {
				tickControlled(player);
			} else {
				setPaddleState(false, false);
				setDeltaMovement(getDeltaMovement().multiply(0.88D, 1D, 0.88D));
				if (this.level().getBlockState(blockPosition().below()).isAir()) {
					setDeltaMovement(getDeltaMovement().add(0D, -0.15D, 0D));
				}
			}
		}

		tickPaddles();
		move(MoverType.SELF, getDeltaMovement());
		setDeltaMovement(getDeltaMovement().multiply(0.9D, 1D, 0.9D));
		this.fallDistance = 0F;
	}

	private void tickHurtState() {
		if (getTimeSinceHit() > 0) {
			setTimeSinceHit(getTimeSinceHit() - 1);
		}
		if (getDamageTaken() > 0F) {
			setDamageTaken(getDamageTaken() - 1F);
		}
	}

	private void tickControlled(Player player) {
		if (!ManaItemHandler.instance().requestManaExact(new ItemStack(ExtraBotanyItems.flyingBoat), player, 1, true)) {
			this.ejectPassengers();
			setDeltaMovement(Vec3.ZERO);
			return;
		}

		FlyingBoatItem.Variant variant = getVariant();
		if (leftInputDown) {
			this.deltaRotation -= 1F;
		}
		if (rightInputDown) {
			this.deltaRotation += 1F;
		}
		this.deltaRotation *= 0.9F;
		setYRot(getYRot() + this.deltaRotation);

		double speed = 0D;
		if ((leftInputDown != rightInputDown) && !forwardInputDown && !backInputDown) {
			speed += 0.005D;
		}
		if (forwardInputDown) {
			speed += 0.05D * 1.25D * variant.forwardMultiplier();
		}
		if (backInputDown) {
			speed -= 0.005D * 1.45D * variant.backMultiplier();
		}

		Vec3 motion = getDeltaMovement().multiply(0.9D, 1D, 0.9D).add(forwardVector(speed));
		int height = getHeightAboveGround();
		if (upInputDown && height <= variant.maxHeight()) {
			motion = new Vec3(motion.x, 0.35D, motion.z);
		} else if (downInputDown) {
			motion = new Vec3(motion.x, -0.35D, motion.z);
		} else if (this.level().getBlockState(blockPosition().below()).isAir()) {
			motion = new Vec3(motion.x, -0.15D, motion.z);
		} else {
			motion = new Vec3(motion.x, 0D, motion.z);
		}
		setDeltaMovement(motion);
		setPaddleState((rightInputDown && !leftInputDown) || forwardInputDown, (leftInputDown && !rightInputDown) || forwardInputDown);
	}

	private Vec3 forwardVector(double speed) {
		float yaw = getYRot() * Mth.DEG_TO_RAD;
		return new Vec3(-Mth.sin(yaw) * speed, 0D, Mth.cos(yaw) * speed);
	}

	private int getHeightAboveGround() {
		BlockPos.MutableBlockPos pos = blockPosition().mutable();
		int max = Math.min(255, pos.getY() - this.level().getMinBuildHeight());
		for (int i = 0; i <= max; i++) {
			if (!this.level().getBlockState(pos).isAir() && !this.level().getBlockState(pos).is(Blocks.WATER)) {
				return i;
			}
			pos.move(0, -1, 0);
		}
		return max;
	}

	public void updateInput(boolean forward, boolean back, boolean left, boolean right, boolean up, boolean down) {
		this.forwardInputDown = forward;
		this.backInputDown = back;
		this.leftInputDown = left;
		this.rightInputDown = right;
		this.upInputDown = up;
		this.downInputDown = down;
	}

	private void tickPaddles() {
		for (int i = 0; i < this.paddlePositions.length; i++) {
			if (getPaddleState(i)) {
				this.paddlePositions[i] += 0.3926991F;
			} else {
				this.paddlePositions[i] = 0F;
			}
		}
	}

	private void setPaddleState(boolean left, boolean right) {
		this.entityData.set(LEFT_PADDLE, left);
		this.entityData.set(RIGHT_PADDLE, right);
	}

	private boolean getPaddleState(int side) {
		return side == 0 ? this.entityData.get(LEFT_PADDLE) : this.entityData.get(RIGHT_PADDLE);
	}

	public float getRowingTime(int side, float partialTick) {
		if (!getPaddleState(side)) {
			return 0F;
		}
		return Mth.lerp(partialTick, this.paddlePositions[side] - 0.3926991F, this.paddlePositions[side]);
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
			float damage = getDamageTaken() + amount * 10F;
			setDamageTaken(damage);
			setForwardDirection(-getForwardDirection());
			setTimeSinceHit(10);
			boolean creative = attacker instanceof Player player && player.getAbilities().instabuild;
			if (creative || damage > 40F) {
				if (!creative && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
					this.spawnAtLocation(getDropStack());
				}
				this.discard();
			}
		}
		return true;
	}

	private ItemStack getDropStack() {
		return new ItemStack(getVariant().item());
	}

	@Override
	protected void positionRider(Entity passenger, MoveFunction callback) {
		if (this.hasPassenger(passenger)) {
			int index = this.getPassengers().indexOf(passenger);
			double offset = index == 0 ? 0.2D : -0.6D;
			Vec3 seat = new Vec3(offset, 0D, 0D).yRot(-getYRot() * Mth.DEG_TO_RAD - Mth.HALF_PI);
			callback.accept(passenger, this.getX() + seat.x, this.getY() - 0.025D, this.getZ() + seat.z);
			passenger.setYRot(passenger.getYRot() + this.deltaRotation);
			passenger.setYHeadRot(passenger.getYHeadRot() + this.deltaRotation);
			clampPassengerYaw(passenger);
		}
	}

	private void clampPassengerYaw(Entity passenger) {
		passenger.setYBodyRot(this.getYRot());
		float yaw = Mth.wrapDegrees(passenger.getYRot() - this.getYRot());
		float clampedYaw = Mth.clamp(yaw, -105F, 105F);
		passenger.yRotO += clampedYaw - yaw;
		passenger.setYRot(passenger.getYRot() + clampedYaw - yaw);
		passenger.setYHeadRot(passenger.getYRot());
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
	public boolean canCollideWith(Entity entity) {
		return entity.canBeCollidedWith() && !this.isPassengerOfSameVehicle(entity);
	}

	@Override
	public boolean isPickable() {
		return !this.isRemoved();
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putInt(TAG_TIME_SINCE_HIT, getTimeSinceHit());
		tag.putInt(TAG_FORWARD_DIRECTION, getForwardDirection());
		tag.putFloat(TAG_DAMAGE_TAKEN, getDamageTaken());
		tag.putString(TAG_VARIANT, getVariant().getSerializedName());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		setTimeSinceHit(tag.getInt(TAG_TIME_SINCE_HIT));
		setForwardDirection(tag.getInt(TAG_FORWARD_DIRECTION));
		setDamageTaken(tag.getFloat(TAG_DAMAGE_TAKEN));
		setVariant(FlyingBoatItem.Variant.byName(tag.getString(TAG_VARIANT)));
	}

	public FlyingBoatItem.Variant getVariant() {
		return FlyingBoatItem.Variant.byId(this.entityData.get(VARIANT));
	}

	public void setVariant(FlyingBoatItem.Variant variant) {
		this.entityData.set(VARIANT, variant.ordinal());
	}

	public int getTimeSinceHit() {
		return this.entityData.get(TIME_SINCE_HIT);
	}

	private void setTimeSinceHit(int timeSinceHit) {
		this.entityData.set(TIME_SINCE_HIT, timeSinceHit);
	}

	public int getForwardDirection() {
		return this.entityData.get(FORWARD_DIRECTION);
	}

	private void setForwardDirection(int forwardDirection) {
		this.entityData.set(FORWARD_DIRECTION, forwardDirection);
	}

	public float getDamageTaken() {
		return this.entityData.get(DAMAGE_TAKEN);
	}

	private void setDamageTaken(float damage) {
		this.entityData.set(DAMAGE_TAKEN, damage);
	}
}
