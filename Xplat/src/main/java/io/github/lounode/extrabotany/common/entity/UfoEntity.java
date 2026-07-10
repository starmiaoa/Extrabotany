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
import java.util.UUID;

public class UfoEntity extends Entity {
	private static final EntityDataAccessor<Integer> CAUGHT_ID = SynchedEntityData.defineId(UfoEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> DAMAGE_TAKEN = SynchedEntityData.defineId(UfoEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Boolean> ACCESSORY_MOUNT = SynchedEntityData.defineId(UfoEntity.class, EntityDataSerializers.BOOLEAN);
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
	@Nullable
	private UUID caughtUuid;

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
	public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
		this.lerpX = x;
		this.lerpY = y;
		this.lerpZ = z;
		this.lerpYRot = yRot;
		this.lerpXRot = xRot;
		this.lerpSteps = 10;
	}

	@Override
	public double lerpTargetX() {
		return this.lerpSteps > 0 ? this.lerpX : this.getX();
	}

	@Override
	public double lerpTargetY() {
		return this.lerpSteps > 0 ? this.lerpY : this.getY();
	}

	@Override
	public double lerpTargetZ() {
		return this.lerpSteps > 0 ? this.lerpZ : this.getZ();
	}

	@Override
	public float lerpTargetXRot() {
		return this.lerpSteps > 0 ? (float) this.lerpXRot : this.getXRot();
	}

	@Override
	public float lerpTargetYRot() {
		return this.lerpSteps > 0 ? (float) this.lerpYRot : this.getYRot();
	}

	private void tickLerp() {
		if (this.isControlledByLocalInstance()) {
			this.lerpSteps = 0;
			this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
		}
		if (this.lerpSteps > 0) {
			this.lerpPositionAndRotationStep(this.lerpSteps, this.lerpX, this.lerpY, this.lerpZ, this.lerpYRot, this.lerpXRot);
			this.lerpSteps--;
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
			// 与原版船一致:仅控制端(驾驶玩家所在端,即客户端)计算并 move(),
			// 再由原版 ServerboundMoveVehiclePacket 把位置同步到服务端。服务端/远端
			// 绝不能自己 move,否则飞碟一 tick 被移动两次(tick + 那个包)会触发
			// 「moved wrongly」反作弊,把它弹回上一个 good 位置 —— 下车瞬移回起点正因如此。
			if (this.isControlledByLocalInstance()) {
				this.setDeltaMovement(getInputMotion(player));
				this.move(MoverType.SELF, this.getDeltaMovement());
				this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
			} else {
				this.setDeltaMovement(Vec3.ZERO);
			}
		} else {
			if (!this.level().isClientSide() && isAccessoryMount() && this.tickCount > 3) {
				this.discard();
				return;
			}
			this.setDeltaMovement(this.getDeltaMovement().scale(0.6D));
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
		}

		if (!this.level().isClientSide()) {
			if (!(passenger instanceof Player)) {
				clearInput();
			}
			updateCaughtEntity();
		}

		this.fallDistance = 0F;
	}

	private Vec3 getInputMotion(Player player) {
		double speed = 0.75D;
		Vec3 look = player.getLookAngle();
		Vec3 flat = new Vec3(look.x, 0D, look.z);
		flat = flat.lengthSqr() > 1.0E-4D
				? flat.normalize()
				: new Vec3(-Mth.sin(player.getYRot() * Mth.DEG_TO_RAD), 0D, Mth.cos(player.getYRot() * Mth.DEG_TO_RAD));
		Vec3 motion = Vec3.ZERO;
		if (forwardInputDown) {
			// 前进 = 朝视线方向飞:抬头上升、低头下降
			motion = motion.add(look.scale(speed));
		}
		if (backInputDown) {
			motion = motion.add(flat.scale(-0.45D));
		}
		if (leftInputDown) {
			motion = motion.add(flat.yRot((float) (Math.PI * 0.5D)).scale(0.55D));
		}
		if (rightInputDown) {
			motion = motion.add(flat.yRot((float) -(Math.PI * 0.5D)).scale(0.55D));
		}
		if (upInputDown) {
			// 空格纯垂直上升,保证贴地也能起飞
			motion = motion.add(0D, 0.4D, 0D);
		}
		// 贴地时禁止净向下:防止低头按前进把飞碟摁在地上起不来
		if (this.onGround() && motion.y < 0D) {
			motion = new Vec3(motion.x, 0D, motion.z);
		}
		return motion;
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
			setCaughtId(-1);
			return;
		}
		if (!(caught instanceof LivingEntity living) || !living.isAlive() || living.distanceTo(this) >= 16F || caught.isPassenger()) {
			clearCaughtEntity();
			return;
		}
		caughtUuid = caught.getUUID();
		if (id != caught.getId()) {
			setCaughtId(caught.getId());
		}
		Vec3 target = new Vec3(this.getX() - caught.getX(), this.getY() - 2F - caught.getY(), this.getZ() - caught.getZ());
		if (target.lengthSqr() > 0.0001D) {
			caught.setDeltaMovement(target.normalize().scale(0.75D));
		}
		if (upInputDown) {
			caught.setPos(caught.getX(), caught.getY() + 0.33D, caught.getZ());
		}
		caught.fallDistance = 0F;
	}

	public void updateInput(boolean forward, boolean back, boolean left, boolean right, boolean up, boolean catchPressed) {
		this.forwardInputDown = forward;
		this.backInputDown = back;
		this.leftInputDown = left;
		this.rightInputDown = right;
		this.upInputDown = up;
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
	}

	private void toggleCaughtEntity() {
		if (this.level().isClientSide()) {
			return;
		}
		if (getCaughtId() != -1 || caughtUuid != null) {
			clearCaughtEntity();
			return;
		}
		List<LivingEntity> entities = getEntitiesBelow(this.blockPosition(), this.level());
		for (LivingEntity living : entities) {
			if (living.isAlive() && living != getControllingPassenger() && !living.isPassenger()) {
				setCaughtEntity(living);
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
