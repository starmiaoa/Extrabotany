package io.github.lounode.extrabotany.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
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

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UfoEntity extends Entity {
	private static final EntityDataAccessor<Integer> CAUGHT_ID = SynchedEntityData.defineId(UfoEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> DAMAGE_TAKEN = SynchedEntityData.defineId(UfoEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Boolean> ACCESSORY_MOUNT = SynchedEntityData.defineId(UfoEntity.class, EntityDataSerializers.BOOLEAN);
	private static final String TAG_CAUGHT_ID = "CaughtId";
	private static final String TAG_DAMAGE_TAKEN = "DamageTaken";
	private static final String TAG_ACCESSORY_MOUNT = "AccessoryMount";

	private boolean forwardInputDown;
	private boolean backInputDown;
	private boolean leftInputDown;
	private boolean rightInputDown;
	private boolean upInputDown;
	private boolean downInputDown;

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
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(CAUGHT_ID, -1);
		builder.define(DAMAGE_TAKEN, 0F);
		builder.define(ACCESSORY_MOUNT, false);
	}

	@Override
	public void tick() {
		this.setNoGravity(true);
		super.tick();
		Entity passenger = getControllingPassenger();
		if (!this.level().isClientSide()) {
			if (passenger instanceof Player player) {
				this.setYRot(player.getYRot());
				Vec3 motion = getInputMotion(player);
				this.setDeltaMovement(motion);
			} else if (isAccessoryMount() && this.tickCount > 3) {
				this.discard();
				return;
			} else {
				this.setDeltaMovement(this.getDeltaMovement().scale(0.6D));
			}
			updateCaughtEntity();
		}

		this.move(MoverType.SELF, this.getDeltaMovement());
		this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
		this.fallDistance = 0F;
	}

	private Vec3 getInputMotion(Player player) {
		float yaw = player.getYRot() * Mth.DEG_TO_RAD;
		float pitch = player.getXRot() * Mth.DEG_TO_RAD;
		double speed = 0.75D;
		Vec3 forward = new Vec3(-Mth.sin(yaw) * Mth.cos(pitch), 0D, Mth.cos(yaw) * Mth.cos(pitch)).scale(speed);
		Vec3 left = forward.yRot((float) (Math.PI * 0.5D)).scale(0.75D);
		Vec3 right = forward.yRot((float) -(Math.PI * 0.5D)).scale(0.75D);
		Vec3 back = forward.yRot((float) Math.PI).scale(0.6D);
		Vec3 motion = Vec3.ZERO;
		if (forwardInputDown) {
			motion = motion.add(forward);
		}
		if (backInputDown) {
			motion = motion.add(back);
		}
		if (leftInputDown) {
			motion = motion.add(left);
		}
		if (rightInputDown) {
			motion = motion.add(right);
		}
		if (upInputDown) {
			motion = motion.add(0D, 0.35D, 0D);
		} else if (downInputDown) {
			motion = motion.add(0D, -0.35D, 0D);
		}
		return motion;
	}

	private void updateCaughtEntity() {
		int id = getCaughtId();
		if (id == -1) {
			return;
		}
		Entity caught = this.level().getEntity(id);
		if (!(caught instanceof LivingEntity living) || !living.isAlive() || living.distanceTo(this) >= 16F || caught.isPassenger()) {
			setCaughtId(-1);
			return;
		}
		Vec3 target = new Vec3(this.getX() - caught.getX(), this.getY() - 2F - caught.getY(), this.getZ() - caught.getZ());
		if (target.lengthSqr() > 0.0001D) {
			caught.setDeltaMovement(target.normalize().scale(0.75D));
		}
		if (upInputDown) {
			caught.setPos(caught.getX(), caught.getY() + 0.33D, caught.getZ());
		} else if (downInputDown) {
			caught.setPos(caught.getX(), caught.getY() - 0.37D, caught.getZ());
		}
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

	private void toggleCaughtEntity() {
		if (this.level().isClientSide()) {
			return;
		}
		if (getCaughtId() != -1) {
			setCaughtId(-1);
			return;
		}
		List<LivingEntity> entities = getEntitiesBelow(this.blockPosition(), this.level());
		for (LivingEntity living : entities) {
			if (living.isAlive() && living != getControllingPassenger() && !living.isPassenger()) {
				setCaughtId(living.getId());
				return;
			}
		}
	}

	public static List<LivingEntity> getEntitiesBelow(BlockPos source, Level level) {
		return level.getEntitiesOfClass(LivingEntity.class,
				new AABB(source.getX() - 1.5F, source.getY() - 16F, source.getZ() - 1.5F,
						source.getX() + 2F, source.getY() - 0.5F, source.getZ() + 2F));
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
			callback.accept(passenger, this.getX(), this.getY() + 0.6D, this.getZ());
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
	public boolean canCollideWith(Entity entity) {
		return entity.canBeCollidedWith() && !this.isPassengerOfSameVehicle(entity);
	}

	@Override
	public boolean isPickable() {
		return !this.isRemoved();
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		tag.putInt(TAG_CAUGHT_ID, getCaughtId());
		tag.putFloat(TAG_DAMAGE_TAKEN, getDamageTaken());
		tag.putBoolean(TAG_ACCESSORY_MOUNT, isAccessoryMount());
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		setCaughtId(tag.getInt(TAG_CAUGHT_ID));
		setDamageTaken(tag.getFloat(TAG_DAMAGE_TAKEN));
		setAccessoryMount(tag.getBoolean(TAG_ACCESSORY_MOUNT));
	}

	public int getCaughtId() {
		return this.entityData.get(CAUGHT_ID);
	}

	public void setCaughtId(int id) {
		this.entityData.set(CAUGHT_ID, id);
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
