package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.common.proxy.Proxy;

public class JackieChanRingItem extends BaubleItem {

	public JackieChanRingItem(Properties props) {
		super(props);
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		entity.fallDistance = 0;

		if (entity.level().isClientSide()) {
			Proxy.INSTANCE.tickJackieChanRing(entity);
		}
	}
}
