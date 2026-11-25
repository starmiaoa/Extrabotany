package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.common.helper.ItemNBTHelper;

import io.github.lounode.eventwrapper.event.entity.living.LivingAttackEventWrapper;
import io.github.lounode.eventwrapper.event.entity.player.AttackEntityEventWrapper;
import io.github.lounode.eventwrapper.event.entity.player.PlayerInteractEventWrapper;
import io.github.lounode.eventwrapper.eventbus.api.EventBusSubscriberWrapper;
import io.github.lounode.eventwrapper.eventbus.api.SubscribeEventWrapper;
import io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.entity.FlamescionSlashEntity;
import io.github.lounode.extrabotany.common.entity.FlamescionSwordEntity;
import io.github.lounode.extrabotany.common.entity.FlamescionUltEntity;
import io.github.lounode.extrabotany.common.entity.FlamescionVoidEntity;
import io.github.lounode.extrabotany.common.entity.StrengthenSlashEntity;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPacketFlamescion;
import io.github.lounode.extrabotany.xplat.ExClientXplatAbstractions;

import java.util.List;

@EventBusSubscriberWrapper
public class FlamescionWeaponItem extends SwordItem {
	public static final int MAX_ENERGY = 600;
	private static final String TAG_ENERGY = "flamescion_energy";
	private static final String TAG_OVERLOADED = "flamescion_overloaded";

	public FlamescionWeaponItem(Tier tier, Properties properties) {
		super(tier, 7, -1.8F, properties);
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return getEnergy(stack) > 0 || isOverloaded(stack);
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(13.0F * getEnergy(stack) / MAX_ENERGY);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return isOverloaded(stack) ? 0xFF3B30 : 0xFF9F1C;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
		super.inventoryTick(stack, level, entity, slotId, isSelected);
		if (level.isClientSide()) {
			return;
		}
		if (isOverloaded(stack)) {
			int energy = getEnergy(stack);
			if (energy > 0) {
				setEnergy(stack, Math.max(0, energy - 4));
			} else {
				setOverloaded(stack, false);
			}
		} else {
			int energy = getEnergy(stack);
			boolean activelyBurning = isSelected && entity instanceof Player player && isFlamescionMode(player);
			if (!activelyBurning && energy > 0) {
				setEnergy(stack, Math.max(0, energy - 1));
			}
		}
	}

	@SubscribeEventWrapper
	public static void leftClick(PlayerInteractEventWrapper.LeftClickEmpty event) {
		ItemStack stack = event.getItemStack();
		if (!stack.isEmpty() && stack.is(ExtraBotanyItems.flamescionWeapon)) {
			ExClientXplatAbstractions.INSTANCE.sendToServer(LeftClickPacketFlamescion.INSTANCE);
		}
	}

	@SubscribeEventWrapper
	public static void attackEntity(AttackEntityEventWrapper event) {
		Player player = event.getEntity();
		if (!player.level().isClientSide && player.getMainHandItem().is(ExtraBotanyItems.flamescionWeapon)) {
			tryStrengthenAttack(player, player.getMainHandItem(), player.getAttackStrengthScale(0F));
		}
	}

	@SubscribeEventWrapper
	public static void onLivingAttack(LivingAttackEventWrapper event) {
		Entity source = event.getSource().getEntity();
		if (!(source instanceof Player player)
				|| event.getSource().getDirectEntity() != player
				|| !isFlamescionMode(player)
				|| player.level().isClientSide()) {
			return;
		}
		LivingEntity target = event.getEntity();
		FlamescionSlashEntity slash = new FlamescionSlashEntity(player.level(), player);
		slash.setPos(target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ());
		player.level().addFreshEntity(slash);
		player.level().playSound(null, target.blockPosition(), ExtraBotanySounds.FLAMESCION_SLASH,
				SoundSource.PLAYERS, 0.85F, 0.95F + player.getRandom().nextFloat() * 0.2F);
		player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.INCANDESCENCE, 60));
		target.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 30));
		target.setSecondsOnFire(3);
	}

	public static void onPlayerTick(Player player) {
		ItemStack stack = player.getMainHandItem();
		if (!stack.is(ExtraBotanyItems.flamescionWeapon)) {
			return;
		}
		if (isFlamescionMode(player)) {
			if (!player.onGround() && !player.isFallFlying() && !player.getAbilities().flying && player.getDeltaMovement().y < -0.15D) {
				Vec3 motion = player.getDeltaMovement();
				player.setDeltaMovement(motion.x, motion.y * 0.78D, motion.z);
				player.fallDistance = 0F;
			}
			if (player.level().isClientSide()) {
				if (player.tickCount % 3 == 0) {
					double angle = player.tickCount * 0.35D;
					player.level().addParticle(ParticleTypes.FLAME,
							player.getX() + Math.cos(angle) * 0.65D,
							player.getY() + 0.4D + player.getRandom().nextDouble() * 0.9D,
							player.getZ() + Math.sin(angle) * 0.65D,
							0.0D, 0.03D, 0.0D);
				}
				return;
			}
			int energy = getEnergy(stack);
			if (energy < MAX_ENERGY) {
				int next = Math.min(MAX_ENERGY, energy + 2);
				setEnergy(stack, next);
				if (next >= MAX_ENERGY) {
					setOverloaded(stack, true);
				}
			} else {
				setOverloaded(stack, true);
			}
		}
	}

	public static boolean tryUseFromPacket(ServerPlayer player, float attackStrength) {
		ItemStack stack = player.getMainHandItem();
		if (stack.is(ExtraBotanyItems.flamescionWeapon)) {
			return tryStrengthenAttack(player, stack, attackStrength);
		}
		return false;
	}

	private static boolean tryStrengthenAttack(Player player, ItemStack stack, float attackStrength) {
		if (player.isSpectator() || attackStrength < 0.85F || isOverloaded(stack)) {
			return false;
		}
		if (player.hasEffect(ExtraBotanyMobEffects.FLAMESCION)) {
			for (int i = -1; i <= 1; i++) {
				StrengthenSlashEntity slash = new StrengthenSlashEntity(player.level(), player);
				Vec3 direction = player.getLookAngle().yRot((float) Math.toRadians(18.0F * i)).normalize();
				slash.setPos(player.getX(), player.getEyeY() - 0.35D, player.getZ());
				slash.setDeltaMovement(direction.scale(1.35D));
				slash.setFlightDirection(direction);
				player.level().addFreshEntity(slash);
			}
			player.level().playSound(null, player.blockPosition(), ExtraBotanySounds.FLAMESCION_SLASH,
					SoundSource.PLAYERS, 1.0F, 1.15F);
			player.removeEffect(ExtraBotanyMobEffects.FLAMESCION);
			return true;
		} else if (isFlamescionMode(player)) {
			FlamescionSwordEntity sword = new FlamescionSwordEntity(player.level(), player);
			Vec3 direction = player.getLookAngle().normalize();
			sword.setPos(player.getX(), player.getEyeY() - 0.35D, player.getZ());
			sword.setDeltaMovement(direction.scale(1.25D));
			sword.setFlightDirection(direction);
			player.level().addFreshEntity(sword);
			player.level().playSound(null, player.blockPosition(), ExtraBotanySounds.FLAMESCION_SLASH,
					SoundSource.PLAYERS, 0.9F, 0.95F);
			return true;
		}
		return false;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (isOverloaded(stack)) {
			return InteractionResultHolder.pass(stack);
		}
		if (player.isShiftKeyDown() && !isFlamescionMode(player)) {
			if (!level.isClientSide()) {
				for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(3.5D))) {
					if (!DamageHandler.INSTANCE.checkPassable(target, player)) {
						continue;
					}
					target.setDeltaMovement(target.getDeltaMovement().add(0.0D, 0.75D, 0.0D));
					target.hurtMarked = true;
					target.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 60));
					target.setSecondsOnFire(4);
				}
				player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.INCANDESCENCE, 140));
				level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.95F, 1.1F);
				level.playSound(null, player.blockPosition(), ExtraBotanySounds.FLAMESCION_SLASH, SoundSource.PLAYERS, 0.9F, 1.05F);
				if (level instanceof ServerLevel serverLevel) {
					for (int i = 0; i < 24; i++) {
						double angle = (Math.PI * 2.0D * i) / 24.0D;
						double px = player.getX() + Math.cos(angle) * 2.2D;
						double pz = player.getZ() + Math.sin(angle) * 2.2D;
						serverLevel.sendParticles(ParticleTypes.FLAME, px, player.getY() + 0.5D, pz, 2, 0.15D, 0.25D, 0.15D, 0.06D);
						if (i % 3 == 0) {
							serverLevel.sendParticles(ParticleTypes.LAVA, px, player.getY() + 0.6D, pz, 1, 0.1D, 0.15D, 0.1D, 0.02D);
						}
					}
				}
			}
			return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
		}
		if (isFlamescionMode(player)) {
			Vec3 targetPos = raytraceAhead(level, player, 5.0D);
			if (!level.isClientSide()) {
				FlamescionVoidEntity flameVoid = new FlamescionVoidEntity(level, player);
				flameVoid.setPos(targetPos.x, targetPos.y, targetPos.z);
				level.addFreshEntity(flameVoid);
				player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.INCANDESCENCE, 100));
				level.playSound(null, player.blockPosition(), ExtraBotanySounds.FLAMESCION_SLASH, SoundSource.PLAYERS, 0.85F, 0.8F);
			}
			player.getCooldowns().addCooldown(this, 35);
			return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
		}
		return InteractionResultHolder.pass(stack);
	}

	public static void castUltimate(Player player) {
		ItemStack stack = player.getMainHandItem();
		if (player.level().isClientSide || !stack.is(ExtraBotanyItems.flamescionWeapon) || !isFlamescionMode(player)) {
			return;
		}
		Vec3 ahead = raytraceAhead(player.level(), player, 5.0D);
		Vec3 downEnd = new Vec3(ahead.x, ahead.y - 4.0D, ahead.z);
		BlockHitResult groundHit = player.level().clip(new ClipContext(ahead, downEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		double spawnY = groundHit.getType() != HitResult.Type.MISS
				? groundHit.getLocation().y + 0.15D
				: Math.max(ahead.y - 1.0D, player.getY() + 0.15D);

		FlamescionUltEntity ult = new FlamescionUltEntity(player.level(), player);
		ult.setPos(ahead.x, spawnY, ahead.z);
		player.level().addFreshEntity(ult);
		setEnergy(stack, MAX_ENERGY);
		setOverloaded(stack, true);
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 45, 3));
		player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 45, 0));
	}

	public static void castShiftDash(Player player) {
		ItemStack stack = player.getMainHandItem();
		if (player.level().isClientSide || !stack.is(ExtraBotanyItems.flamescionWeapon) || !isFlamescionMode(player)
				|| player.getCooldowns().isOnCooldown(stack.getItem())) {
			return;
		}

		Vec3 look = player.getLookAngle().normalize();
		Vec3 start = player.position();
		Vec3 dash = look.scale(5.0D);
		Vec3 end = start.add(dash);
		Vec3 eyeStart = player.getEyePosition();
		Vec3 eyeEnd = eyeStart.add(dash);
		BlockHitResult blockHit = player.level().clip(new ClipContext(eyeStart, eyeEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		if (blockHit.getType() != HitResult.Type.MISS) {
			Vec3 safeEye = blockHit.getLocation().subtract(look.scale(0.6D));
			end = new Vec3(safeEye.x, safeEye.y - player.getEyeHeight(), safeEye.z);
			if (end.distanceToSqr(start) > dash.lengthSqr()) {
				end = start;
			}
		}
		player.teleportTo(end.x, end.y, end.z);
		player.fallDistance = 0F;

		if (player.level() instanceof ServerLevel serverLevel) {
			int steps = 18;
			for (int i = 0; i <= steps; i++) {
				double t = (double) i / steps;
				double px = Mth.lerp(t, start.x, end.x);
				double py = Mth.lerp(t, start.y, end.y) + 0.9D;
				double pz = Mth.lerp(t, start.z, end.z);
				serverLevel.sendParticles(ParticleTypes.FLAME, px, py, pz, 3, 0.2D, 0.25D, 0.2D, 0.04D);
				if (i % 2 == 0) {
					serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, px, py, pz, 1, 0.15D, 0.15D, 0.15D, 0.02D);
				}
				if (i % 3 == 0) {
					serverLevel.sendParticles(ParticleTypes.LAVA, px, py, pz, 1, 0.1D, 0.1D, 0.1D, 0.01D);
				}
			}
		}

		player.level().playSound(null, player.blockPosition(), ExtraBotanySounds.FLAMESCION_SLASH,
				SoundSource.PLAYERS, 0.95F, 1.35F);

		boolean hit = false;
		Vec3 rayStart = start.subtract(look.scale(1.5D));
		Vec3 rayEnd = end.add(look.scale(1.5D));
		for (LivingEntity living : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(7.5D))) {
			if (!DamageHandler.INSTANCE.checkPassable(living, player)) {
				continue;
			}
			if (living.getBoundingBox().inflate(2.4D).clip(rayStart, rayEnd).isPresent()) {
				living.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 50));
				living.setSecondsOnFire(4);
				living.invulnerableTime = 0;
				living.hurt(ExtraBotanyDamageTypes.Sources.flamescionFlameDamage(player.level().registryAccess()), 8.0F);
				hit = true;
			}
		}
		if (hit) {
			player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.INCANDESCENCE, 100));
			player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.FLAMESCION, 200));
		} else {
			player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.FLAMESCION, 100));
		}
		player.getCooldowns().addCooldown(stack.getItem(), 20);
	}

	private static Vec3 raytraceAhead(Level level, Player player, double distance) {
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle().normalize();
		Vec3 end = eye.add(look.scale(distance));
		BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		if (hit.getType() != HitResult.Type.MISS) {
			return hit.getLocation().subtract(look.scale(0.6D));
		}
		return end;
	}

	public static boolean isFlamescionMode(Player player) {
		return player.getMainHandItem().is(ExtraBotanyItems.flamescionWeapon)
				&& player.hasEffect(ExtraBotanyMobEffects.INCANDESCENCE)
				&& !isOverloaded(player.getMainHandItem());
	}

	public static int getEnergy(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_ENERGY, 0);
	}

	public static void setEnergy(ItemStack stack, int energy) {
		ItemNBTHelper.setInt(stack, TAG_ENERGY, Mth.clamp(energy, 0, MAX_ENERGY));
	}

	public static boolean isOverloaded(ItemStack stack) {
		return ItemNBTHelper.getBoolean(stack, TAG_OVERLOADED, false);
	}

	public static void setOverloaded(ItemStack stack, boolean overloaded) {
		ItemNBTHelper.setBoolean(stack, TAG_OVERLOADED, overloaded);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flags) {
		tooltip.add(Component.translatable("tooltip.extrabotany.flamescion_weapon.energy", getEnergy(stack), MAX_ENERGY).withStyle(ChatFormatting.GRAY));
		if (isOverloaded(stack)) {
			tooltip.add(Component.translatable("tooltip.extrabotany.flamescion_weapon.overloaded").withStyle(ChatFormatting.RED));
		}
	}
}
