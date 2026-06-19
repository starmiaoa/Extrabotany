package io.github.lounode.extrabotany.common.entity.gaia.behavior;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.player.Player;

import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.common.entity.gaia.GaiaIII;

/**
 * 本我(盖亚III 二阶段)远程换武器攻击。朝最近玩家定时发射,按 egoStage 切换武器池:
 * <ul>
 *   <li>EGO-0:真影刃(3 连散射) / 真泰拉之刃(单发)</li>
 *   <li>EGO-1:再加 波涌之刃(连锁) / 狂星之怒(5 陨星)</li>
 *   <li>EGO-2:最初分型(幻影剑)</li>
 * </ul>
 * 发射逻辑镜像各 relic 的 {@code useSword},弹体自带"非玩家所有者=Boss 真伤"分支,无需额外伤害处理。
 */
public class GaiaEgoSwordAttack<E extends Gaia> extends Behavior<E> {

	public GaiaEgoSwordAttack() {
		super(ImmutableMap.of());
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E gaia) {
		if (!(gaia instanceof GaiaIII ego) || !ego.isEgo()) {
			return false;
		}
		// 无敌期间(本我无敌波/出场)不进行远程攻击。
		if (gaia.getInvulTime() != 0) {
			return false;
		}
		return gaia.tickCount % attackInterval(ego.getEgoStage()) == 0;
	}

	@Override
	protected void start(ServerLevel level, E gaia, long gameTime) {
		if (!(gaia instanceof GaiaIII ego)) {
			return;
		}
		Player target = pickTarget(ego);
		if (target == null) {
			return;
		}
		fireWeapon(ego, target);
	}

	// 老 EGO 各阶段攻击间隔:stage0 ~65-80 / stage1 ~50-65 / stage2 ~35-50 tick。这里取偏快的固定值。
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
			weapon = 4; // 最初分型
		} else if (stage == 1) {
			weapon = rand.nextInt(4); // 真影刃 / 真泰拉 / 波涌 / 狂星
		} else {
			weapon = rand.nextInt(2); // 真影刃 / 真泰拉
		}
		EgoWeaponFire.fire(gaia, target, weapon);
	}
}
