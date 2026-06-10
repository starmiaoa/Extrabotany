package io.github.lounode.extrabotany.common.item.equipment.bauble;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;

import vazkii.botania.api.item.BlockChangedListenerBauble;
import vazkii.botania.api.item.Relic;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.bauble.NimbusAmuletItem;
import vazkii.botania.common.item.equipment.bauble.PyroclastPendantItem;
import vazkii.botania.common.item.equipment.bauble.SnowflakePendantItem;
import vazkii.botania.common.item.equipment.bauble.ThirdEyeItem;
import vazkii.botania.common.item.relic.RelicImpl;

import io.github.lounode.extrabotany.common.event.entity.living.LivingAttackEventWrapper;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;

import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class MoonPendantItem extends NimbusAmuletItem implements BlockChangedListenerBauble {
	private static final ResourceLocation KNOCKBACK_RESISTANCE_ID = prefix("moon_pendant_knockback_resistance");

	public MoonPendantItem(Properties props) {
		super(props);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (!level.isClientSide && entity instanceof Player player) {
			Relic relic = Relic.LOOKUP.find(stack);
			if (relic != null) {
				relic.tickBinding(player);
			}
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltip, flags);
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

	@Override
	public void onChangedBlock(ItemStack stack, LivingEntity entity, ServerLevel level, BlockPos pos) {
		if (BotaniaItems.icePendant instanceof SnowflakePendantItem pendant) {
			pendant.onChangedBlock(stack, entity, level, pos);
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
	public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack, ResourceLocation slotIdentifier) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		attributes.put(Attributes.KNOCKBACK_RESISTANCE,
				new AttributeModifier(KNOCKBACK_RESISTANCE_ID, 1.0D, AttributeModifier.Operation.ADD_VALUE));
		return attributes;
	}

	private static boolean isRightPlayer(ItemStack stack, Player player) {
		Relic relic = Relic.LOOKUP.find(stack);
		return relic != null && relic.isRightPlayer(player);
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, prefix(LibAdvancementNames.PANDA_DO_NOT_WEAR_RINGS));
	}

	public static class EventHandler {
		public static void onLivingAttack(LivingAttackEventWrapper event) {
			if (!isFireDamage(event.getSource())) {
				return;
			}
			if (!EquipmentHandler.findOrEmpty(ExtraBotanyItems.moonPendant, event.getEntity()).isEmpty()) {
				event.setAmount(0);
				event.setCanceled(true);
			}
		}

		private static boolean isFireDamage(DamageSource source) {
			return source.is(DamageTypeTags.IS_FIRE);
		}
	}
}
