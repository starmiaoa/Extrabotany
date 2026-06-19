package io.github.lounode.extrabotany.common.entity.gaia.behavior;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.player.Player;

import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.common.entity.gaia.GaiaIII;

public class GaiaEgoSwordAttack<E extends Gaia> extends Behavior<E> {
	public GaiaEgoSwordAttack() {
		super(ImmutableMap.of());
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E gaia) {
		if (!(gaia instanceof GaiaIII ego) || !ego.isEgo()) {
			return false;
		}
		return gaia.getInvulTime() == 0 && gaia.tickCount % attackInterval(ego.getEgoStage()) == 0;
	}

	@Override
	protected void start(ServerLevel level, E gaia, long gameTime) {
		if (!(gaia instanceof GaiaIII ego)) {
			return;
		}
		Player target = pickTarget(ego);
		if (target != null) {
			fireWeapon(ego, target);
		}
	}

	private int attackInterval(int egoStage) {
		return switch (egoStage) {
			case 0 -> 70;
			case 1 -> 55;
			default -> 45;
		};
	}

	private Player pickTarget(Gaia gaia) {
		Player best = null;
		double bestDist = Double.MAX_VALUE;
		for (Player player : gaia.getPlayersAround()) {
			if (!player.isAlive() || player.isSpectator()) {
				continue;
			}
			double dist = player.distanceToSqr(gaia);
			if (dist < bestDist) {
				bestDist = dist;
				best = player;
			}
		}
		return best;
	}

	private void fireWeapon(GaiaIII gaia, Player target) {
		RandomSource rand = gaia.getRandom();
		int stage = gaia.getEgoStage();
		int weapon;
		if (stage >= 2) {
			weapon = 4;
		} else if (stage == 1) {
			weapon = rand.nextInt(4);
		} else {
			weapon = rand.nextInt(2);
		}
		EgoWeaponFire.fire(gaia, target, weapon);
	}
}
