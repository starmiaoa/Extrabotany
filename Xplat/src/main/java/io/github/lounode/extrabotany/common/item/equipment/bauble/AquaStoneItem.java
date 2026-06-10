package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.handler.EquipmentHandler;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

public class AquaStoneItem extends SimpleBaubleItem {
	public static final float MANA_DISCOUNT = 0.1F;

	public AquaStoneItem(Properties props) {
		super(props);
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return EquipmentHandler.findOrEmpty(ExtraBotanyItems.aquaStone, entity).isEmpty()
				&& EquipmentHandler.findOrEmpty(ExtraBotanyItems.theCommunity, entity).isEmpty();
	}

	public static boolean hasDiscount(LivingEntity entity) {
		return !EquipmentHandler.findOrEmpty(ExtraBotanyItems.aquaStone, entity).isEmpty()
				|| !EquipmentHandler.findOrEmpty(ExtraBotanyItems.theCommunity, entity).isEmpty();
	}
}
