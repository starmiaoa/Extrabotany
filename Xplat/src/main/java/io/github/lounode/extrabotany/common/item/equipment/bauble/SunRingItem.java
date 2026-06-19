package io.github.lounode.extrabotany.common.item.equipment.bauble;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.api.item.Relic;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.handler.PixieHandler;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.bauble.BandOfAuraItem;
import vazkii.botania.common.item.equipment.bauble.RingOfChordataItem;
import vazkii.botania.common.item.equipment.bauble.RingOfCorrectionItem;
import vazkii.botania.common.item.equipment.bauble.RingOfTheMantleItem;
import vazkii.botania.common.item.relic.RelicBaubleItem;
import vazkii.botania.common.item.relic.RelicImpl;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class SunRingItem extends RelicBaubleItem {
	private static final ResourceLocation REACH_ID = prefix("sun_ring_reach");
	private static final ResourceLocation PIXIE_ID = prefix("sun_ring_pixie");

	public SunRingItem(Properties props) {
		super(props);
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		if (!(entity instanceof Player player) || !isRightPlayer(stack, player)) {
			return;
		}

		if (BotaniaItems.RING_OF_THE_MANTLE instanceof RingOfTheMantleItem ring) {
			ring.onWornTick(stack, player);
		}
		if (BotaniaItems.GREATER_BAND_OF_AURA instanceof BandOfAuraItem ring) {
			ring.onWornTick(stack, player);
		}
		if (BotaniaItems.RING_OF_CORRECTION instanceof RingOfCorrectionItem ring) {
			ring.onWornTick(stack, player);
		}
		if (BotaniaItems.RING_OF_CHORDATA instanceof RingOfChordataItem ring) {
			ring.onWornTick(stack, player);
		}
		((DeathRingItem) ExtraBotanyItems.deathRing).onWornTick(stack, player);
		((FrostStarItem) ExtraBotanyItems.frostStar).onWornTick(stack, player);
		((DispersiveRingItem) ExtraBotanyItems.dispersiveRing).onWornTick(stack, player);
	}

	@Override
	public void onUnequipped(ItemStack stack, LivingEntity entity) {
		super.onUnequipped(stack, entity);
		if (BotaniaItems.RING_OF_THE_MANTLE instanceof RingOfTheMantleItem ring) {
			ring.onUnequipped(stack, entity);
		}
		if (BotaniaItems.RING_OF_CHORDATA instanceof RingOfChordataItem ring) {
			ring.onUnequipped(stack, entity);
		}
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return super.canEquip(stack, entity) && EquipmentHandler.findOrEmpty(ExtraBotanyItems.sunRing, entity).isEmpty();
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack, ResourceLocation slotIdentifier) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		attributes.put(Attributes.BLOCK_INTERACTION_RANGE,
				new AttributeModifier(REACH_ID, 3.5D, AttributeModifier.Operation.ADD_VALUE));
		attributes.put(Attributes.ENTITY_INTERACTION_RANGE,
				new AttributeModifier(REACH_ID, 3.5D, AttributeModifier.Operation.ADD_VALUE));
		attributes.put(PixieHandler.PIXIE_SPAWN_CHANCE, PixieHandler.makeModifier(PIXIE_ID, 0.25D));
		return attributes;
	}

	private static boolean isRightPlayer(ItemStack stack, Player player) {
		Relic relic = Relic.LOOKUP.find(stack);
		return relic != null && relic.isRightPlayer(player);
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, prefix(LibAdvancementNames.PANDA_DO_NOT_WEAR_RINGS));
	}
}
