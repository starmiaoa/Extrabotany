package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.EquipmentHandler;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

public class DeathRingItem extends SimpleBaubleItem {
	public static final int RANGE = 6;
	public static final int MANA_PER_DAMAGE = 80;
	public static final int INTERVAL = 30;

	public DeathRingItem(Properties props) {
		super(props);
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		if (!(entity instanceof ServerPlayer player) || player.tickCount % INTERVAL != 0) {
			return;
		}

		AABB area = player.getBoundingBox().inflate(RANGE);
		for (LivingEntity living : player.level().getEntitiesOfClass(LivingEntity.class, area)) {
			if (living == player || !player.hasLineOfSight(living) || !DamageHandler.INSTANCE.checkPassable(living, player)) {
				continue;
			}
			if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_DAMAGE, true)) {
				break;
			}
			living.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1));
			living.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 60, 1));
			living.setHealth(Math.max(1.0F, living.getHealth() - 0.5F));
			living.hurt(player.damageSources().indirectMagic(player, player), 0.01F);
		}
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return EquipmentHandler.findOrEmpty(ExtraBotanyItems.deathRing, entity).isEmpty();
	}
}
