package io.github.lounode.extrabotany.common.brew.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class TimeLockMobEffect extends MobEffect {
	public TimeLockMobEffect(MobEffectCategory category, int color) {
		super(category, color);
		addAttributeModifier(Attributes.MOVEMENT_SPEED, prefix("timelock_movement_speed"), -1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
	}
}
