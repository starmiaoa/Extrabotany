package io.github.lounode.extrabotany.common.entity.gaia.behavior;

import com.google.common.collect.ImmutableMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.common.util.HerrscherCombatHelper;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.ArrayList;
import java.util.List;

import static io.github.lounode.extrabotany.api.gaia.GaiaArena.checkFeasibility;
import static io.github.lounode.extrabotany.api.gaia.GaiaArena.checkGuardianAllowed;

public class GaiaDisarm<E extends Gaia> extends Behavior<E> {

	public GaiaDisarm() {
		super(ImmutableMap.of(
				MemoryModuleType.NEAREST_PLAYERS, MemoryStatus.VALUE_PRESENT
		));
	}

	@Override
	protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
		return true;
	}

	@Override
	protected void tick(ServerLevel level, E gaia, long gameTime) {
		List<Player> players = getPlayers(gaia);
		for (Player player : players) {
			disArm(player, gaia);
		}
	}

	protected void disArm(Player player, E gaia) {
		if (player.isCreative() || ExtraBotanyConfig.common().disableGaiaDisArm()
				|| !ExtraBotanyConfig.common().guardianItemCheck()) {
			return;
		}
		// 持有 Boss 放行物品(虚空万藏/最初分型)→ 不缴械(不依赖成就,整合包删成就也有效)。
		net.minecraft.world.item.Item bypassItem = gaia.getGuardianBypassItem();
		if (bypassItem != null && io.github.lounode.extrabotany.api.gaia.GaiaArena.playerHasItem(player, bypassItem)) {
			return;
		}
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			final ItemStack stack = player.getInventory().getItem(i);
			if (!checkFeasibility(stack) && !checkGuardianAllowed(stack)) {
				ItemStack copy = stack.copy();
				HerrscherCombatHelper.dropWithPickupDelay(player, copy, 90);
				player.getInventory().setItem(i, ItemStack.EMPTY);
			}
		}
	}

	protected List<Player> getPlayers(Gaia gaia) {
		return gaia.getBrain().getMemory(MemoryModuleType.NEAREST_PLAYERS).orElse(new ArrayList<>());
	}
}
