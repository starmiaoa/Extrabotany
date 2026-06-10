package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.entity.PhantomSwordEntity;

public class FirstFractalItem extends OldExbotanyRelicSwordItem {
	private static final double RANGE = 13D;

	public FirstFractalItem(Properties properties) {
		super(Tiers.NETHERITE, 10, -1.6F, 0, true, properties);
	}

	@Override
	protected void useSword(Player player, Entity target) {
		Vec3 targetPos = resolveTargetPos(player, target, 80D);
		double angle = -Math.PI + 2 * Math.PI * player.level().random.nextDouble();
		float damage = (float) (player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2F);

		for (int i = 0; i < 3; i++) {
			Vec3 start = randomStart(player, targetPos, angle);
			player.level().addFreshEntity(new PhantomSwordEntity(player.level(), player, start, targetPos, 5 + 5 * i, damage));
			angle += 2 * Math.PI * player.level().random.nextDouble() * 0.08D + 2 * Math.PI * 0.17D;
		}

		Vec3 start = randomStart(player, targetPos, angle);
		player.level().addFreshEntity(new PhantomSwordEntity(player.level(), player, start, targetPos, 0, damage));
	}

	private Vec3 randomStart(Player player, Vec3 targetPos, double angle) {
		double pitch = 0.12D * Math.PI * player.level().random.nextDouble() + 0.28D * Math.PI;
		return new Vec3(
				targetPos.x + RANGE * Math.sin(pitch) * Math.cos(angle),
				targetPos.y + RANGE * Math.cos(pitch),
				targetPos.z + RANGE * Math.sin(pitch) * Math.sin(angle)
		);
	}
}
