package io.github.lounode.extrabotany.common.brew.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class TimeLockMobEffect extends MobEffect {
	public TimeLockMobEffect(MobEffectCategory category, int color) {
		super(category, color);
		addAttributeModifier(Attributes.MOVEMENT_SPEED, "8EE5201A-7CC0-47D0-9989-D4D1D2A3D020", -1.0D, AttributeModifier.Operation.MULTIPLY_TOTAL);
	}
}
