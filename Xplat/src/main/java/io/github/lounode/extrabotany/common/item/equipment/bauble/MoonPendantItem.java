package io.github.lounode.extrabotany.common.item.equipment.bauble;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import vazkii.botania.api.item.Relic;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.bauble.NimbusAmuletItem;
import vazkii.botania.common.item.equipment.bauble.PyroclastPendantItem;
import vazkii.botania.common.item.equipment.bauble.SnowflakePendantItem;
import vazkii.botania.common.item.equipment.bauble.ThirdEyeItem;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;

import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class MoonPendantItem extends NimbusAmuletItem {
	public MoonPendantItem(Properties props) {
		super(props);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (!level.isClientSide && entity instanceof Player player) {
			Relic relic = XplatAbstractions.INSTANCE.findRelic(stack);
			if (relic != null) {
				relic.tickBinding(player);
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, level, tooltip, flags);
		RelicImpl.addDefaultTooltip(stack, tooltip);
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		if (!(entity instanceof Player player) || !isRightPlayer(stack, player)) {
			return;
		}

		super.onWornTick(stack, entity);
		if (BotaniaItems.superLavaPendant instanceof PyroclastPendantItem pendant) {
			pendant.onWornTick(stack, entity);
		}
		if (BotaniaItems.icePendant instanceof SnowflakePendantItem pendant) {
			pendant.onWornTick(stack, entity);
		}
		if (BotaniaItems.itemFinder instanceof ThirdEyeItem finder) {
			finder.onWornTick(stack, entity);
		}
	}

	public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity entity) {
		return true;
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return entity instanceof Player player
				&& isRightPlayer(stack, player)
				&& EquipmentHandler.findOrEmpty(ExtraBotanyItems.moonPendant, entity).isEmpty();
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack) {
		Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
		attributes.put(Attributes.KNOCKBACK_RESISTANCE,
				new AttributeModifier(getBaubleUUID(stack), "Moon Pendant", 1.0D, AttributeModifier.Operation.ADDITION));
		return attributes;
	}

	private static boolean isRightPlayer(ItemStack stack, Player player) {
		Relic relic = XplatAbstractions.INSTANCE.findRelic(stack);
		return relic != null && relic.isRightPlayer(player);
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, prefix(LibAdvancementNames.PANDA_DO_NOT_WEAR_RINGS));
	}

	public static boolean shouldCancelFireDamage(DamageSource source, LivingEntity entity) {
		return source.is(DamageTypeTags.IS_FIRE)
				&& !EquipmentHandler.findOrEmpty(ExtraBotanyItems.moonPendant, entity).isEmpty();
	}
}
