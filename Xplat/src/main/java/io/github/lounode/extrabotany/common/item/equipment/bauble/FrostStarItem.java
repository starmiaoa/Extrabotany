package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.phys.AABB;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.EquipmentHandler;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

public class FrostStarItem extends SimpleBaubleItem {
	public static final int RANGE = 6;
	public static final int FROST_RANGE = 8;
	public static final int MANA_PER_TARGET = 30;
	public static final int INTERVAL = 20;

	public FrostStarItem(Properties props) {
		super(props);
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		if (!(entity instanceof ServerPlayer player)) {
			return;
		}

		FrostWalkerEnchantment.onEntityMoved(player, player.level(), player.blockPosition(), FROST_RANGE);
		if (player.tickCount % INTERVAL != 0) {
			return;
		}

		AABB area = player.getBoundingBox().inflate(RANGE);
		for (LivingEntity living : player.level().getEntitiesOfClass(LivingEntity.class, area)) {
			if (living == player || !player.hasLineOfSight(living) || !DamageHandler.INSTANCE.checkPassable(living, player)) {
				continue;
			}
			if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_TARGET, true)) {
				break;
			}
			living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 4));
		}
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return EquipmentHandler.findOrEmpty(ExtraBotanyItems.frostStar, entity).isEmpty();
	}
}
