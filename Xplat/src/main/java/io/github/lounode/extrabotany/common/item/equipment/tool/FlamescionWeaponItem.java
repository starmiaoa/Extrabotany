package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.entity.FlamescionSlashEntity;
import io.github.lounode.extrabotany.common.entity.FlamescionSwordEntity;
import io.github.lounode.extrabotany.common.entity.FlamescionUltEntity;
import io.github.lounode.extrabotany.common.entity.FlamescionVoidEntity;
import io.github.lounode.extrabotany.common.entity.StrengthenSlashEntity;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.util.ItemStackDataHelper;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPacketFlamescion;
import io.github.lounode.extrabotany.xplat.ExClientXplatAbstractions;

import java.util.List;

public class FlamescionWeaponItem extends SwordItem {
	public static final int MAX_ENERGY = 600;
	private static final String TAG_ENERGY = "flamescion_energy";
	private static final String TAG_OVERLOADED = "flamescion_overloaded";

	public FlamescionWeaponItem(Tier tier, Properties properties) {
		super(tier, properties.attributes(SwordItem.createAttributes(tier, 5, -1.6F)));
	}

	public static void leftClick(PlayerInteractEvent.LeftClickEmpty event) {
		ItemStack stack = event.getItemStack();
		if (!stack.isEmpty() && stack.is(ExtraBotanyItems.flamescionWeapon)) {
			ExClientXplatAbstractions.INSTANCE.sendToServer(LeftClickPacketFlamescion.INSTANCE);
		}
	}

	public static void attackEntity(AttackEntityEvent event) {
		Player player = event.getEntity();
		if (!player.level().isClientSide && player.getMainHandItem().is(ExtraBotanyItems.flamescionWeapon)) {
			tryStrengthenAttack(player, player.getMainHandItem(), player.getAttackStrengthScale(0F));
		}
	}

	public static void onLivingAttack(LivingIncomingDamageEvent event) {
		Entity source = event.getSource().getEntity();
		if (!(source instanceof Player player) || !isFlamescionMode(player)) {
			return;
		}
		FlamescionSlashEntity slash = new FlamescionSlashEntity(player.level(), player);
		LivingEntity target = event.getEntity();
		slash.setPos(target.getX(), target.getY() + 1F, target.getZ());
		player.level().addFreshEntity(slash);
		player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.INCANDESCENCE, 30));
		target.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 30));
	}

	public static void onPlayerTick(Player player) {
		ItemStack stack = player.getMainHandItem();
		if (!stack.is(ExtraBotanyItems.flamescionWeapon)) {
			return;
		}
		if (isFlamescionMode(player)) {
			int energy = getEnergy(stack);
			if (energy < MAX_ENERGY) {
				setEnergy(stack, Math.min(MAX_ENERGY, energy + 2));
			} else {
				setOverloaded(stack, true);
			}
		}
		if (isOverloaded(stack)) {
			int energy = getEnergy(stack);
			if (energy > 0) {
				setEnergy(stack, Math.max(0, energy - 3));
			} else {
				setOverloaded(stack, false);
			}
		}
	}

	public static void tryUseFromPacket(ServerPlayer player, float attackStrength) {
		ItemStack stack = player.getMainHandItem();
		if (stack.is(ExtraBotanyItems.flamescionWeapon)) {
			tryStrengthenAttack(player, stack, attackStrength);
		}
	}

	private static void tryStrengthenAttack(Player player, ItemStack stack, float attackStrength) {
		if (player.isSpectator() || attackStrength != 1F || isOverloaded(stack)) {
			return;
		}
		if (player.hasEffect(ExtraBotanyMobEffects.FLAMESCION)) {
			for (int i = 0; i < 3; i++) {
				StrengthenSlashEntity slash = new StrengthenSlashEntity(player.level(), player);
				Vec3 direction = player.getLookAngle().yRot((float) Math.toRadians(-15F + 15F * i)).normalize();
				slash.setPos(player.getX(), player.getY() + 0.5F, player.getZ());
				slash.setDeltaMovement(direction.scale(1.2D));
				player.level().addFreshEntity(slash);
			}
			player.removeEffect(ExtraBotanyMobEffects.FLAMESCION);
		} else if (isFlamescionMode(player)) {
			FlamescionSwordEntity sword = new FlamescionSwordEntity(player.level(), player);
			Vec3 direction = player.getLookAngle().normalize();
			sword.setPos(player.getX(), player.getY() + 0.5F, player.getZ());
			sword.setDeltaMovement(direction.scale(1.0D));
			player.level().addFreshEntity(sword);
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (isOverloaded(stack)) {
			return InteractionResultHolder.pass(stack);
		}
		if (player.isShiftKeyDown() && !isFlamescionMode(player)) {
			if (player.onGround()) {
				for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(3D))) {
					if (target == player) {
						continue;
					}
					target.setDeltaMovement(target.getDeltaMovement().add(0, 1D, 0));
					target.hurtMarked = true;
					target.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 60));
				}
			}
			player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.INCANDESCENCE, 60));
			return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
		}
		if (isFlamescionMode(player)) {
			Vec3 targetPos = player.position().add(player.getLookAngle().scale(5D));
			if (!level.isClientSide()) {
				FlamescionVoidEntity flameVoid = new FlamescionVoidEntity(level, player);
				flameVoid.setPos(targetPos.x, targetPos.y, targetPos.z);
				level.addFreshEntity(flameVoid);
			}
			player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.INCANDESCENCE, 80));
			player.getCooldowns().addCooldown(this, 40);
			return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
		}
		return InteractionResultHolder.pass(stack);
	}

	public static void castUltimate(Player player) {
		ItemStack stack = player.getMainHandItem();
		if (player.level().isClientSide || !stack.is(ExtraBotanyItems.flamescionWeapon) || !isFlamescionMode(player)) {
			return;
		}
		Vec3 spawnPoint = player.position().add(player.getLookAngle().normalize().scale(5D));
		FlamescionUltEntity ult = new FlamescionUltEntity(player.level(), player);
		ult.setPos(spawnPoint.x, player.getY() + 0.25D, spawnPoint.z);
		player.level().addFreshEntity(ult);
		setEnergy(stack, MAX_ENERGY);
		setOverloaded(stack, true);
		player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 40));
	}

	public static void castShiftDash(Player player) {
		ItemStack stack = player.getMainHandItem();
		if (player.level().isClientSide || !stack.is(ExtraBotanyItems.flamescionWeapon) || !isFlamescionMode(player)
				|| player.getCooldowns().isOnCooldown(stack.getItem())) {
			return;
		}

		Vec3 look = player.getLookAngle().normalize();
		Vec3 start = player.position();
		Vec3 dash = look.scale(4D);
		Vec3 end = start.add(dash);
		player.teleportTo(end.x, end.y, end.z);

		boolean hit = false;
		Vec3 rayStart = start.subtract(dash);
		Vec3 rayEnd = end.add(dash);
		for (LivingEntity living : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8D))) {
			if (living == player) {
				continue;
			}
			if (living.getBoundingBox().inflate(4D).clip(rayStart, rayEnd).isPresent()) {
				living.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 40));
				living.invulnerableTime = 0;
				living.hurt(ExtraBotanyDamageTypes.Sources.flamescionFlameDamage(player.level().registryAccess()), 6F);
				hit = true;
			}
		}
		if (hit) {
			player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.INCANDESCENCE, 80));
			player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.FLAMESCION, 200));
		}
		player.getCooldowns().addCooldown(stack.getItem(), 20);
	}

	public static boolean isFlamescionMode(Player player) {
		return !player.onGround()
				&& player.getMainHandItem().is(ExtraBotanyItems.flamescionWeapon)
				&& player.hasEffect(ExtraBotanyMobEffects.INCANDESCENCE)
				&& !isOverloaded(player.getMainHandItem());
	}

	public static int getEnergy(ItemStack stack) {
		return ItemStackDataHelper.getInt(stack, TAG_ENERGY, 0);
	}

	public static void setEnergy(ItemStack stack, int energy) {
		ItemStackDataHelper.setInt(stack, TAG_ENERGY, Mth.clamp(energy, 0, MAX_ENERGY));
	}

	public static boolean isOverloaded(ItemStack stack) {
		return ItemStackDataHelper.getBoolean(stack, TAG_OVERLOADED, false);
	}

	public static void setOverloaded(ItemStack stack, boolean overloaded) {
		ItemStackDataHelper.setBoolean(stack, TAG_OVERLOADED, overloaded);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		tooltip.add(Component.translatable("tooltip.extrabotany.flamescion_weapon.energy", getEnergy(stack), MAX_ENERGY).withStyle(ChatFormatting.GRAY));
		if (isOverloaded(stack)) {
			tooltip.add(Component.translatable("tooltip.extrabotany.flamescion_weapon.overloaded").withStyle(ChatFormatting.RED));
		}
	}
}
