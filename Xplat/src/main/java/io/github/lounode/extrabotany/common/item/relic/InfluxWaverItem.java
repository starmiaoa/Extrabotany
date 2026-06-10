package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.entity.InfluxWaverProjectileEntity;

public class InfluxWaverItem extends OldExbotanyRelicSwordItem {
	private static final int MANA_PER_USE = 500;

	public InfluxWaverItem(Properties properties) {
		super(Tiers.DIAMOND, 5, -2F, MANA_PER_USE, true, properties);
	}

	@Override
	protected void useSword(Player player, Entity target) {
		Vec3 targetPos = resolveTargetPos(player, target, 64D);
		player.level().addFreshEntity(new InfluxWaverProjectileEntity(player.level(), player, targetPos, 3));
	}
}
