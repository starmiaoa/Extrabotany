package io.github.lounode.extrabotany.common.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;

public final class FrostWalkerEffectHelper {
	private FrostWalkerEffectHelper() {}

	public static void apply(LivingEntity entity, Level level, int blockRange) {
		boolean wasOnGround = entity.onGround();
		entity.setOnGround(true);
		try {
			FrostWalkerEnchantment.onEntityMoved(entity, level, entity.blockPosition(), blockRange);
		} finally {
			entity.setOnGround(wasOnGround);
		}
	}
}
