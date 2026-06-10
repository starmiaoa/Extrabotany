package io.github.lounode.extrabotany.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.ManaItemHandler;

public class ManaDrinkItem extends Item {
	public static final int MANA_GIVEN = 10000;

	public ManaDrinkItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (entity instanceof ServerPlayer serverPlayer) {
			CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
			serverPlayer.awardStat(Stats.ITEM_USED.get(this));
		}

		if (!level.isClientSide && entity instanceof Player player) {
			if (player.getHealth() < player.getMaxHealth()) {
				player.heal(5.0F);
			}
			player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 0));
			player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 0));
			player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 0));
			player.addEffect(new MobEffectInstance(MobEffects.JUMP, 1200, 0));
			ManaItemHandler.instance().dispatchManaExact(stack, player, MANA_GIVEN, true);

			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
				ItemStack bottle = new ItemStack(ExtraBotanyItems.manaGlassBottle);
				if (stack.isEmpty()) {
					return bottle;
				}
				if (!player.getInventory().add(bottle)) {
					player.drop(bottle, false);
				}
			}
		}

		return stack;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 32;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.DRINK;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
		player.startUsingItem(usedHand);
		return InteractionResultHolder.consume(player.getItemInHand(usedHand));
	}
}
