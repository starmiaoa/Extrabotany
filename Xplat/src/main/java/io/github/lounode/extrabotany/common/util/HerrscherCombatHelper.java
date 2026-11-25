package io.github.lounode.extrabotany.common.util;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public final class HerrscherCombatHelper {
	private HerrscherCombatHelper() {}

	public static boolean isFinitePositive(float amount) {
		return Float.isFinite(amount) && amount > 0F;
	}

	public static void dealTrueMagicDamage(LivingEntity target, @Nullable Entity source, float amount) {
		if (!target.isAlive() || !isFinitePositive(amount) || target.isInvulnerable()) {
			return;
		}
		if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
			return;
		}
		DamageSource damageSource = target.damageSources().indirectMagic(source == null ? target : source, source);
		float health = target.getHealth();
		if (health <= amount) {
			float absorption = Math.max(0F, target.getAbsorptionAmount());
			float lethalAmount = Math.max(amount, health + absorption + 20F);
			target.hurt(damageSource, lethalAmount);
			if (target.isAlive() && !(target instanceof Player)) {
				target.setHealth(0F);
				target.die(damageSource);
			}
			return;
		}
		target.setHealth(Math.max(1F, health - amount));
		target.hurt(damageSource, 0.01F);
	}

	@Nullable
	public static ItemEntity dropWithPickupDelay(Player player, ItemStack stack, int pickupDelay) {
		if (stack.isEmpty()) {
			return null;
		}
		ItemEntity dropped = player.drop(stack, false, true);
		if (dropped != null) {
			dropped.setPickUpDelay(Math.max(0, pickupDelay));
		}
		return dropped;
	}

	public static void award(ServerPlayer player, String mainAdvancement) {
		AdvancementHolder advancement = player.server.getAdvancements().get(prefix("main/" + mainAdvancement));
		if (advancement != null) {
			for (String criterion : player.getAdvancements().getOrStartProgress(advancement).getRemainingCriteria()) {
				player.getAdvancements().award(advancement, criterion);
			}
		}
	}

	public static ResourceLocation mainAdvancement(String name) {
		return prefix("main/" + name);
	}
}
