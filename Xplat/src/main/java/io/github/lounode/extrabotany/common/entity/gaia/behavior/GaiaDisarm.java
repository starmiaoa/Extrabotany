package io.github.lounode.extrabotany.common.entity.gaia.behavior;

import com.google.common.collect.ImmutableMap;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.helper.PlayerHelper;

import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.common.util.HerrscherCombatHelper;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.ArrayList;
import java.util.List;

import static io.github.lounode.extrabotany.api.gaia.GaiaArena.checkFeasibility;
import static io.github.lounode.extrabotany.api.gaia.GaiaArena.checkGuardianAllowed;

public class GaiaDisarm<E extends Gaia> extends Behavior<E> {

	// Same per-player exemption as the summon gate: once the gate is lifted, the fight must not strip items either.
	private final ResourceLocation bypassAdvancement;

	public GaiaDisarm(ResourceLocation bypassAdvancement) {
		super(ImmutableMap.of(
				MemoryModuleType.NEAREST_PLAYERS, MemoryStatus.VALUE_PRESENT
		));
		this.bypassAdvancement = bypassAdvancement;
	}

	@Override
	protected boolean canStillUse(ServerLevel level, E entity, long gameTime) {
		return true;
	}

	@Override
	protected void tick(ServerLevel level, E gaia, long gameTime) {
		List<Player> players = getPlayers(gaia);
		for (Player player : players) {
			disArm(player);
		}
	}

	protected void disArm(Player player) {
		if (player.isCreative() || ExtraBotanyConfig.common().disableGaiaDisArm()) {
			return;
		}
		if (bypassAdvancement != null && player instanceof ServerPlayer serverPlayer
				&& PlayerHelper.hasAdvancement(serverPlayer, bypassAdvancement)) {
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
