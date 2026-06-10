package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.EquipmentHandler;

import io.github.lounode.extrabotany.common.event.entity.living.LivingDeathEventWrapper;
import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

public class PotatoChipsItem extends SimpleBaubleItem {
	public static final int MANA_PER_USE = 3000;
	public static final int COOLDOWN = 600;
	public static final int BOSS_COOLDOWN = 12000;

	public PotatoChipsItem(Properties props) {
		super(props);
	}

	public static class EventHandler {
		public static void onPlayerDeath(LivingDeathEventWrapper event) {
			if (!(event.getEntity() instanceof Player player)) {
				return;
			}

			ItemStack chips = EquipmentHandler.findOrEmpty(ExtraBotanyItems.potatoChips, player);
			if (chips.isEmpty() || player.getCooldowns().isOnCooldown(chips.getItem())) {
				return;
			}
			if (!ManaItemHandler.instance().requestManaExactForTool(chips, player, MANA_PER_USE, true)) {
				return;
			}

			event.setCanceled(true);
			player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
			player.setHealth(5.0F);
			player.removeAllEffects();
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
			player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
			player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
			player.getCooldowns().addCooldown(chips.getItem(), isBossSource(event) ? BOSS_COOLDOWN : COOLDOWN);
		}

		private static boolean isBossSource(LivingDeathEventWrapper event) {
			Entity source = event.getSource().getEntity();
			return source instanceof EnderDragon || source instanceof WitherBoss || source instanceof Gaia;
		}
	}
}
