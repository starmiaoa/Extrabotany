package io.github.lounode.extrabotany.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class CandyBagItem extends RewardBagItem {
	public CandyBagItem(Properties builder, ResourceLocation lootTable) {
		super(builder, lootTable);
	}

	@Override
	public Component getName(ItemStack stack) {
		if (SeasonalItemHelper.isChristmas()) {
			return Component.translatable(getDescriptionId(stack) + ".christmas");
		}
		return super.getName(stack);
	}
}
