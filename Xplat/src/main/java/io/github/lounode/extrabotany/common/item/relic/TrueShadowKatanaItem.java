package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.entity.TrueShadowKatanaProjectileEntity;

public class TrueShadowKatanaItem extends OldExbotanyRelicSwordItem {
	private static final int MANA_PER_USE = 800;

	public TrueShadowKatanaItem(Properties properties) {
		super(Tiers.DIAMOND, 5, -2F, MANA_PER_USE, true, properties);
	}

	@Override
	protected void useSword(Player player, Entity target) {
		LivingEntity found = findNearbyTarget(player, target, 8D);
		Vec3 targetPos = found == null ? resolveTargetPos(player, target, 64D) : found.position().add(0, 1, 0);
		Vec3 look = player.getLookAngle().multiply(1, 0, 1);
		if (look.lengthSqr() < 1.0E-4D) {
			look = Vec3.directionFromRotation(0, player.getYRot());
		}
		Vec3 forward = look.normalize().scale(1.75D);
		Vec3 side = new Vec3(-forward.z, 0, forward.x).normalize();
		Vec3 base = player.position().add(0, player.getBbHeight() * 0.55D, 0).add(forward);

		for (int i = -1; i <= 1; i++) {
			Vec3 start = base.add(side.scale(i * 1.4D)).add(0, Math.abs(i) * 0.2D, 0);
			player.level().addFreshEntity(new TrueShadowKatanaProjectileEntity(player.level(), player, start, targetPos));
		}
	}
}
