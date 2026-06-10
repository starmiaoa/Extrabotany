package io.github.lounode.extrabotany.common.item.equipment.bauble;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import vazkii.botania.common.handler.EquipmentHandler;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class GemOfConquestItem extends SimpleBaubleItem {
	private static final ResourceLocation ATTACK_DAMAGE_ID = prefix("gem_of_conquest_attack_damage");
	private static final ResourceLocation ATTACK_SPEED_ID = prefix("gem_of_conquest_attack_speed");
	private static final ResourceLocation MOVEMENT_SPEED_ID = prefix("gem_of_conquest_movement_speed");
	private static final int EFFECT_INTERVAL = 40;
	private static final int RECENT_ATTACK_WINDOW = 100;

	public GemOfConquestItem(Properties props) {
		super(props);
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack, ResourceLocation slotIdentifier) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		attributes.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ATTACK_DAMAGE_ID, 0.15D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		attributes.put(Attributes.ATTACK_SPEED, new AttributeModifier(ATTACK_SPEED_ID, 0.10D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		attributes.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(MOVEMENT_SPEED_ID, 0.05D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		return attributes;
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		if (!(entity instanceof ServerPlayer player) || player.tickCount % EFFECT_INTERVAL != 0) {
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
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		tooltip.add(Component.translatable("tooltip.extrabotany.gem_of_conquest").withStyle(ChatFormatting.GRAY));
		super.appendHoverText(stack, context, tooltip, flags);
	}
}
