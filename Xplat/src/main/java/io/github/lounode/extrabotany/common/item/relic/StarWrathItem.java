package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.entity.StarWrathFallingStarEntity;

public class StarWrathItem extends OldExbotanyRelicSwordItem {
	private static final int MANA_PER_USE = 500;

	public StarWrathItem(Properties properties) {
		super(Tiers.DIAMOND, 6, -1.6F, MANA_PER_USE, true, properties);
	}

	@Override
	protected void useSword(Player player, Entity target) {
		Vec3 targetPos = resolveTargetPos(player, target, 64D);
		for (int i = 0; i < 5; i++) {
			Vec3 impact = targetPos.add((0.5D - player.level().random.nextDouble()) * 6D, 0, (0.5D - player.level().random.nextDouble()) * 6D);
			Vec3 motionSource = new Vec3(
					(0.5D * player.level().random.nextDouble() - 0.25D) * 18D,
					24D,
					(0.5D * player.level().random.nextDouble() - 0.25D) * 18D
			);
			Vec3 start = impact.add(motionSource);
			Vec3 velocity = motionSource.normalize().reverse().scale(1.5D);

			StarWrathFallingStarEntity star = new StarWrathFallingStarEntity(player.level(), player);
			star.setPos(start.x, start.y, start.z);
			star.setDeltaMovement(velocity);
			player.level().addFreshEntity(star);
		}
	}
}
