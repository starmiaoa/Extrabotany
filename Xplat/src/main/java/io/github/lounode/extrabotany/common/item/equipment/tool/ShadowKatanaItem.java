package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.entity.PixieEntity;

import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.util.AttributeUtil;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class ShadowKatanaItem extends SwordItem {
	private static final int MANA_PER_DAMAGE = 60;
	private static final int MANA_PER_PIXIE = 100;
	private static final float PIXIE_DAMAGE = 3.5F;
	private static final ResourceLocation NIGHT_DAMAGE_ID = prefix("shadow_katana_night_damage");
	private static final ResourceLocation NIGHT_ATTACK_SPEED_ID = prefix("shadow_katana_night_attack_speed");

	public ShadowKatanaItem(Tier tier, Properties properties) {
		super(tier, properties.attributes(SwordItem.createAttributes(tier, 3, -2.4F)));
	}

	public static void attackEntity(AttackEntityEvent event) {
		Player player = event.getEntity();
		if (player.level().isClientSide || player.isSpectator() || player.getAttackStrengthScale(0F) != 1F) {
			return;
		}
		ItemStack stack = player.getMainHandItem();
		if (!stack.is(ExtraBotanyItems.shadowKatana) || !(event.getTarget() instanceof LivingEntity target) || !target.isAlive()) {
			return;
		}
		if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_PIXIE, true)) {
			return;
		}

		PixieEntity pixie = new PixieEntity(player.level(), false);
		pixie.setPos(player.getX(), player.getY() + 1.5D, player.getZ());
		pixie.setProps(target, player, PIXIE_DAMAGE);
		pixie.finalizeSpawn((ServerLevelAccessor) player.level(), player.level().getCurrentDifficultyAt(pixie.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
		player.level().addFreshEntity(pixie);
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (!attacker.level().isDay() && !attacker.level().isClientSide) {
			attacker.heal(2F);
		}
		return super.hurtEnemy(stack, target, attacker);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (!level.isClientSide && entity instanceof ServerPlayer player && stack.getDamageValue() > 0
				&& ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_DAMAGE, true)) {
			stack.setDamageValue(stack.getDamageValue() - 1);
		}
		updateNightAttributes(stack, !level.isDay());
	}

	private static void updateNightAttributes(ItemStack stack, boolean night) {
		AttributeUtil.removeAttributeModifier(stack, NIGHT_DAMAGE_ID);
		AttributeUtil.removeAttributeModifier(stack, NIGHT_ATTACK_SPEED_ID);
		if (!night) {
			return;
		}
		AttributeUtil.addAttributeModifier(stack, Attributes.ATTACK_DAMAGE,
				new AttributeModifier(NIGHT_DAMAGE_ID, 10D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlot.MAINHAND);
		AttributeUtil.addAttributeModifier(stack, Attributes.ATTACK_SPEED,
				new AttributeModifier(NIGHT_ATTACK_SPEED_ID, 0.15D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE), EquipmentSlot.MAINHAND);
	}
}
