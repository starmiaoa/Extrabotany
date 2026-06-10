package io.github.lounode.extrabotany.common.item.equipment.tool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.annotations.SoftImplement;
import vazkii.botania.common.entity.PixieEntity;
import vazkii.botania.common.helper.ItemNBTHelper;

import io.github.lounode.eventwrapper.event.entity.player.AttackEntityEventWrapper;
import io.github.lounode.eventwrapper.eventbus.api.EventBusSubscriberWrapper;
import io.github.lounode.eventwrapper.eventbus.api.SubscribeEventWrapper;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.UUID;

@EventBusSubscriberWrapper
public class ShadowKatanaItem extends SwordItem {
	private static final int MANA_PER_DAMAGE = 60;
	private static final int MANA_PER_PIXIE = 100;
	private static final float PIXIE_DAMAGE = 3.5F;
	private static final String TAG_NIGHT = "extrabotany_night";
	private static final UUID NIGHT_DAMAGE_MODIFIER = UUID.fromString("8ee5201a-7cc0-47d0-9989-d4d1d2a3d013");
	private static final UUID NIGHT_ATTACK_SPEED_MODIFIER = UUID.fromString("8ee5201a-7cc0-47d0-9989-d4d1d2a3d014");

	public ShadowKatanaItem(Tier tier, Properties properties) {
		super(tier, 3, -2.4F, properties);
	}

	@SubscribeEventWrapper
	public static void attackEntity(AttackEntityEventWrapper event) {
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

		PixieEntity pixie = new PixieEntity(player.level());
		pixie.setPos(player.getX(), player.getY() + 1.5D, player.getZ());
		pixie.setProps(target, player, 0, PIXIE_DAMAGE);
		pixie.finalizeSpawn((ServerLevelAccessor) player.level(), player.level().getCurrentDifficultyAt(pixie.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
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
		if (!level.isClientSide && entity instanceof Player player && stack.getDamageValue() > 0
				&& ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_DAMAGE, true)) {
			stack.setDamageValue(stack.getDamageValue() - 1);
		}
		ItemNBTHelper.setBoolean(stack, TAG_NIGHT, !level.isDay());
	}

	@SoftImplement("IForgeItem")
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		Multimap<Attribute, AttributeModifier> ret = super.getDefaultAttributeModifiers(slot);
		if (slot == EquipmentSlot.MAINHAND && ItemNBTHelper.getBoolean(stack, TAG_NIGHT, false)) {
			ret = HashMultimap.create(ret);
			ret.put(Attributes.ATTACK_DAMAGE,
					new AttributeModifier(NIGHT_DAMAGE_MODIFIER, "Shadow Katana night damage", 10D, AttributeModifier.Operation.ADDITION));
			ret.put(Attributes.ATTACK_SPEED,
					new AttributeModifier(NIGHT_ATTACK_SPEED_MODIFIER, "Shadow Katana night attack speed", 0.15D, AttributeModifier.Operation.MULTIPLY_BASE));
		}
		return ret;
	}

	@SoftImplement("FabricItem")
	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
		return getAttributeModifiers(slot, stack);
	}
}
