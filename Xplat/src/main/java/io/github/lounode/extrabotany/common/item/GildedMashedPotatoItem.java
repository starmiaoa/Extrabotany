package io.github.lounode.extrabotany.common.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

public class GildedMashedPotatoItem extends Item {
	public static final FoodProperties FOOD = new FoodProperties.Builder()
			.nutrition(4)
			.saturationModifier(0.2F)
			.alwaysEdible()
			.effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 3), 1.0F)
			.effect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 600, 3), 1.0F)
			.effect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 1), 1.0F)
			.build();

	public GildedMashedPotatoItem(Properties properties) {
		super(properties);
	}
}
