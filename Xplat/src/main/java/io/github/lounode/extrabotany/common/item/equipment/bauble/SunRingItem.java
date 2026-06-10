package io.github.lounode.extrabotany.common.item.equipment.bauble;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class SunRingItem extends RelicBaubleItem {
	public SunRingItem(Properties props) {
		super(props);
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		if (!(entity instanceof Player player) || !isRightPlayer(stack, player)) {
			return;
		}

		if (BotaniaItems.miningRing instanceof RingOfTheMantleItem ring) {
			ring.onWornTick(stack, player);
		}
		if (BotaniaItems.auraRingGreater instanceof BandOfAuraItem ring) {
			ring.onWornTick(stack, player);
		}
		if (BotaniaItems.swapRing instanceof RingOfCorrectionItem ring) {
			ring.onWornTick(stack, player);
		}
		if (BotaniaItems.waterRing instanceof RingOfChordataItem ring) {
			ring.onWornTick(stack, player);
		}
		((DeathRingItem) ExtraBotanyItems.deathRing).onWornTick(stack, player);
		((FrostStarItem) ExtraBotanyItems.frostStar).onWornTick(stack, player);
		((DispersiveRingItem) ExtraBotanyItems.dispersiveRing).onWornTick(stack, player);
	}

	@Override
	public void onUnequipped(ItemStack stack, LivingEntity entity) {
		super.onUnequipped(stack, entity);
		if (BotaniaItems.miningRing instanceof RingOfTheMantleItem ring) {
			ring.onUnequipped(stack, entity);
		}
		if (BotaniaItems.waterRing instanceof RingOfChordataItem ring) {
			ring.onUnequipped(stack, entity);
		}
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return super.canEquip(stack, entity) && EquipmentHandler.findOrEmpty(ExtraBotanyItems.sunRing, entity).isEmpty();
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack) {
		Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
		EXplatAbstractions.INSTANCE.addSunRingReachModifiers(attributes, getBaubleUUID(stack));
		attributes.put(PixieHandler.PIXIE_SPAWN_CHANCE,
				new AttributeModifier(getBaubleUUID(stack), "Sun Ring pixie", 0.25D, AttributeModifier.Operation.ADDITION));
		return attributes;
	}

	private static boolean isRightPlayer(ItemStack stack, Player player) {
		Relic relic = XplatAbstractions.INSTANCE.findRelic(stack);
		return relic != null && relic.isRightPlayer(player);
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, prefix(LibAdvancementNames.PANDA_DO_NOT_WEAR_RINGS));
	}
}
