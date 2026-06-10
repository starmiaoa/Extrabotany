package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.entity.TrueTerrabladeProjectileEntity;

public class TrueTerrabladeItem extends OldExbotanyRelicSwordItem {
	private static final int MANA_PER_USE = 400;

	public TrueTerrabladeItem(Properties properties) {
		super(Tiers.DIAMOND, 5, -2F, MANA_PER_USE, true, properties);
	}

	@Override
	protected void useSword(Player player, Entity target) {
		Vec3 targetPos = resolveTargetPos(player, target, 80D);
		player.level().addFreshEntity(new TrueTerrabladeProjectileEntity(player.level(), player, targetPos));
	}
}
