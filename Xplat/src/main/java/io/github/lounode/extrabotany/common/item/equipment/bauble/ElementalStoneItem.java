package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.handler.EquipmentHandler;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.List;

public class ElementalStoneItem extends AttributeBaubleItem {
	public ElementalStoneItem(Properties props, List<Entry> modifiers) {
		super(props, modifiers);
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return EquipmentHandler.findOrEmpty(stack.getItem(), entity).isEmpty()
				&& EquipmentHandler.findOrEmpty(ExtraBotanyItems.theCommunity, entity).isEmpty();
	}
}
