package io.github.lounode.extrabotany.common.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import org.jetbrains.annotations.Nullable;

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

	private static final String TAG_DAMAGE_TAKEN = "DamageTaken";
	private static final String TAG_TECTONIC_ENERGY = "TectonicEnergy";
	private static final String TAG_CYCLONE_TICKS = "CycloneTicks";
	private static final String TAG_LEAN = "Lean";
	private static final String TAG_PITCH = "Pitch";
	private static final String TAG_OWNER = "Owner";
	private static final String TAG_ACCESSORY_MOUNT = "AccessoryMount";

	private boolean forwardInputDown;
	private boolean backInputDown;
	private boolean leftInputDown;
	private boolean rightInputDown;
	private boolean jumpInputDown;
	private boolean cyclonePressed;
	private int ridingTicks;

	public MotorEntity(EntityType<? extends MotorEntity> entityType, Level level) {
		super(entityType, level);
		this.blocksBuilding = true;
	}

	public MotorEntity(Level level, double x, double y, double z) {
		this(ExtraBotanyEntityType.MOTOR, level);
		this.setPos(x, y, z);
		this.xo = x;
		this.yo = y;
		this.zo = z;
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
	}

	@Override
	public void tick() {
		super.tick();
		this.setNoGravity(false);
		if (!this.level().isClientSide()) {
			if (getControllingPassenger() instanceof Player player) {
				tickControlled(player);
			} else if (isAccessoryMount() && this.tickCount > 3) {
				this.discard();
				return;
			} else {
				this.ridingTicks = 0;
				setLean(0F);
				setPitch(0F);
				this.setDeltaMovement(this.getDeltaMovement().multiply(0.85D, 1D, 0.85D));
			}
		}

		if (!this.onGround() && !this.isInWater()) {
			this.setDeltaMovement(this.getDeltaMovement().add(0D, -0.04D, 0D));
		}
		this.move(MoverType.SELF, this.getDeltaMovement());
		this.setDeltaMovement(this.getDeltaMovement().multiply(0.9D, 0.98D, 0.9D));
		this.fallDistance = 0F;
	}

	private void tickControlled(Player player) {
		this.setYRot(player.getYRot());
		this.ridingTicks++;
		if (leftInputDown) {
			setLean(5F);
		} else if (rightInputDown) {
			setLean(-5F);
		} else {
			setLean(0F);
		}

		Vec3 motion = calculateGroundMotion(player);
		if (forwardInputDown && this.horizontalCollision) {
			motion = motion.add(0D, 0.08D, 0D);
		}

		if (ridingTicks >= 120) {
			setTectonicEnergy(Math.min(800, getTectonicEnergy() + 2));
			if (jumpInputDown && getTectonicEnergy() >= 200) {
				setTectonicEnergy(Math.max(0, getTectonicEnergy() - 6));
				Vec3 boost = forwardVector(player, 1.65D);
				motion = motion.add(boost.x, 0.04D, boost.z);
				setPitch(-5F);
			} else {
				setPitch(0F);
			}
			if (cyclonePressed && getCycloneTicks() == 0 && getTectonicEnergy() >= 400) {
				setCycloneTicks(15);
				setTectonicEnergy(getTectonicEnergy() - 400);
			}
			tickCyclone(player);
			tickPassiveCombat(player);
			if (player.getHealth() < player.getMaxHealth() * 0.5F) {
				player.heal(0.5F);
			}
		}

		this.setDeltaMovement(motion);
	}

	private Vec3 calculateGroundMotion(Player player) {
		Vec3 current = this.getDeltaMovement();
		double speed = 0.28D;
		Vec3 motion = new Vec3(0D, current.y, 0D);
		if (forwardInputDown) {
			Vec3 forward = forwardVector(player, speed);
			motion = motion.add(forward.x, 0D, forward.z);
		}
		if (backInputDown) {
			Vec3 back = forwardVector(player, -0.08D);
			motion = motion.add(back.x, 0D, back.z);
		}
		if (leftInputDown && !forwardInputDown && !backInputDown) {
			Vec3 side = forwardVector(player, 0.06D).yRot((float) (Math.PI * 0.5D));
			motion = motion.add(side.x, 0D, side.z);
		}
		if (rightInputDown && !forwardInputDown && !backInputDown) {
			Vec3 side = forwardVector(player, 0.06D).yRot((float) -(Math.PI * 0.5D));
			motion = motion.add(side.x, 0D, side.z);
		}
		return motion;
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
		setLean(ticks > 6 ? -12F : -5F);
		if (ticks == 12 || ticks == 6) {
			for (LivingEntity living : getNearbyLiving(4F)) {
				if (living == player || this.hasPassenger(living) || !DamageHandler.INSTANCE.checkPassable(living, player)) {
					continue;
				}
				living.hurt(this.damageSources().indirectMagic(this, player), 4.5F);
				living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
				player.setLastHurtMob(living);
			}
		}
	}

	private void tickPassiveCombat(Player player) {
		if (this.tickCount % 15 != 0) {
			return;
		}
		for (LivingEntity living : getNearbyLiving(12F)) {
			if (living == player || this.hasPassenger(living) || !DamageHandler.INSTANCE.checkPassable(living, player)) {
				continue;
			}
			if (living instanceof Enemy || player.getLastHurtMob() == living) {
				living.hurt(this.damageSources().indirectMagic(this, player), 3.5F);
				living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
				player.setLastHurtMob(living);
				break;
			}
		}
	}

	private List<LivingEntity> getNearbyLiving(float range) {
		return this.level().getEntitiesOfClass(LivingEntity.class,
				new AABB(this.getX() - range, this.getY() - range, this.getZ() - range,
						this.getX() + range, this.getY() + range, this.getZ() + range));
	}

	@Override
	public void push(Entity entity) {
		super.push(entity);
		damageOnCollision(entity);
	}

	private void damageOnCollision(Entity entity) {
		if (this.level().isClientSide() || !(getControllingPassenger() instanceof Player player)) {
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
		this.cyclonePressed = cyclonePressed;
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
		getOwner().ifPresent(owner -> stack.getOrCreateTag().putUUID("soulbindUUID", owner));
		return stack;
	}

	@Override
	protected void positionRider(Entity passenger, MoveFunction callback) {
		if (this.hasPassenger(passenger)) {
			callback.accept(passenger, this.getX(), this.getY() + 0.35D, this.getZ());
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

	private void setLean(float lean) {
		this.entityData.set(LEAN, lean);
	}

	public float getPitch() {
		return this.entityData.get(PITCH);
	}

	private void setPitch(float pitch) {
		this.entityData.set(PITCH, pitch);
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
