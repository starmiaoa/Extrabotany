package io.github.lounode.extrabotany.common.item.equipment.bauble;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import vazkii.botania.common.handler.EquipmentHandler;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.List;
import java.util.UUID;

public class GemOfConquestItem extends SimpleBaubleItem {
	private static final UUID ATTACK_DAMAGE_MODIFIER = UUID.fromString("8ee5201a-7cc0-47d0-9989-d4d1d2a3d010");
	private static final UUID ATTACK_SPEED_MODIFIER = UUID.fromString("8ee5201a-7cc0-47d0-9989-d4d1d2a3d011");
	private static final UUID MOVEMENT_SPEED_MODIFIER = UUID.fromString("8ee5201a-7cc0-47d0-9989-d4d1d2a3d012");
	private static final int EFFECT_INTERVAL = 40;
	private static final int RECENT_ATTACK_WINDOW = 100;

	public GemOfConquestItem(Properties props) {
		super(props);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack) {
		Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
		attributes.put(Attributes.ATTACK_DAMAGE,
				new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Gem of Conquest attack damage", 0.15D, AttributeModifier.Operation.MULTIPLY_TOTAL));
		attributes.put(Attributes.ATTACK_SPEED,
				new AttributeModifier(ATTACK_SPEED_MODIFIER, "Gem of Conquest attack speed", 0.10D, AttributeModifier.Operation.MULTIPLY_TOTAL));
		attributes.put(Attributes.MOVEMENT_SPEED,
				new AttributeModifier(MOVEMENT_SPEED_MODIFIER, "Gem of Conquest movement speed", 0.05D, AttributeModifier.Operation.MULTIPLY_TOTAL));
		return attributes;
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		if (!(entity instanceof Player player) || player.level().isClientSide || player.tickCount % EFFECT_INTERVAL != 0) {
			return;
		}
		LivingEntity lastHurt = player.getLastHurtMob();
		if (lastHurt != null && lastHurt.isAlive() && player.tickCount - player.getLastHurtMobTimestamp() <= RECENT_ATTACK_WINDOW) {
			player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, true, false, true));
		}
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return EquipmentHandler.findOrEmpty(ExtraBotanyItems.gemOfConquest, entity).isEmpty();
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flags) {
		tooltip.add(Component.translatable("tooltip.extrabotany.gem_of_conquest").withStyle(ChatFormatting.GRAY));
		super.appendHoverText(stack, level, tooltip, flags);
	}
}
