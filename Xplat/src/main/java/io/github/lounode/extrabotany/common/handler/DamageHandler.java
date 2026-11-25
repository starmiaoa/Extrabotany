package io.github.lounode.extrabotany.common.handler;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

import vazkii.botania.common.handler.EquipmentHandler;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.List;
import java.util.stream.Collectors;

public class DamageHandler {
	public static final DamageHandler INSTANCE = new DamageHandler();

	public boolean checkPassable(Entity target, Entity source) {
		if (target == null || target == source) {
			return false;
		}
		if (target instanceof Player tp && (tp.isCreative() || tp.isSpectator())) {
			return false;
		}
		if (source instanceof Player sourcePlayer) {
			boolean sourceEquipped = !EquipmentHandler.findOrEmpty(ExtraBotanyItems.peaceAmulet, sourcePlayer).isEmpty();
			if (target instanceof Player targetPlayer) {
				if (!sourcePlayer.canHarmPlayer(targetPlayer)) {
					return false;
				}
				return !sourceEquipped && EquipmentHandler.findOrEmpty(ExtraBotanyItems.peaceAmulet, targetPlayer).isEmpty();
			}
			if (sourceEquipped && target instanceof Mob mob && !(mob instanceof Enemy)) {
				return false;
			}
		}

		if (source instanceof Mob) {
			return target instanceof Player;
		}

		return true;
	}

	public List<LivingEntity> getFilteredEntities(List<LivingEntity> entities, Entity source) {
		return entities.stream()
				.filter(living -> !living.isRemoved())
				.filter(living -> checkPassable(living, source))
				.collect(Collectors.toList());
	}
}
