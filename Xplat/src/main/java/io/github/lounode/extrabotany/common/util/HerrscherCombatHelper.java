package io.github.lounode.extrabotany.common.util;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
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

	/**
	 * NaN/Infinity 血量消毒器。空之律者/盖亚Ⅲ 的真伤致死历史上会把玩家血量写成 NaN,
	 * 表现为:无法死亡、没有生命值、第三人称不断抽搐、无法与物品交互、/kill 无效、退出重进也恢复不了。
	 * 每 tick 在服务端兜底检测,一旦发现血量为 NaN/Infinity 则直接判定死亡(类似 NaNHealthFixer)。
	 * 最大生命和吸收量如果是 NaN 则先复位为安全值,再让实体正常死亡。
	 */
	public static void sanitizeHealth(LivingEntity entity) {
		if (entity.level().isClientSide()) {
			return;
		}
		// 先修复最大生命属性,防止后续 kill 逻辑读到 NaN 最大血量
		AttributeInstance maxAttr = entity.getAttribute(Attributes.MAX_HEALTH);
		if (maxAttr != null && !Double.isFinite(maxAttr.getValue())) {
			maxAttr.setBaseValue(20.0D);
		}
		// 修复吸收量
		if (!Float.isFinite(entity.getAbsorptionAmount())) {
			entity.setAbsorptionAmount(0F);
		}
		// NaN/Inf 血量 → 直接死亡,而非恢复满血
		if (!Float.isFinite(entity.getHealth())) {
			entity.setHealth(0F);
			// 直接走死亡流程,绕开 hurt() 的减伤数学,避免 kill() 内部 Float.MAX_VALUE 再被算成 NaN 而循环
			entity.die(entity.damageSources().genericKill());
		}
	}

	public static void dealTrueMagicDamage(LivingEntity target, @Nullable Entity source, float amount) {
		if (!target.isAlive() || !isFinitePositive(amount)) {
			return;
		}
		float health = target.getHealth();
		if (health <= amount) {
			target.hurt(target.damageSources().indirectMagic(source == null ? target : source, source), Float.MAX_VALUE);
			return;
		}
		target.setHealth(Math.max(1F, health - amount));
		target.hurt(target.damageSources().indirectMagic(source == null ? target : source, source), 0.01F);
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
