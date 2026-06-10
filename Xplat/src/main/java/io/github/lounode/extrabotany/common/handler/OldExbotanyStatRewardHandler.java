package io.github.lounode.extrabotany.common.handler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.helper.PlayerHelper;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.common.lib.LibMisc;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public final class OldExbotanyStatRewardHandler {
	private static final ResourceLocation ALL_STATS_ID = advancement(LibAdvancementNames.ALL_STATS);
	private static final String CODE_TRIGGERED = "code_triggered";

	private static final String[] STAT_ADVANCEMENTS = {
			LibAdvancementNames.SENBON_ZAKURA,
			LibAdvancementNames.CRAFT_PEDESTAL,
			LibAdvancementNames.KURUKURU,
			LibAdvancementNames.GOODTEK,
			LibAdvancementNames.DEEP_DARK_FANTASY,
			LibAdvancementNames.STYGIAN_TWINS,
			LibAdvancementNames.GAIA_TRIAL,
			LibAdvancementNames.THE_SOURCE_OF_HONKAI,
			LibAdvancementNames.THE_ORIGINAL_DIVINE_KEY,
			LibAdvancementNames.I_SEE_EVERYTHING,
			LibAdvancementNames.ONE_PUNCH,
			LibAdvancementNames.SOULSTEEL,
			LibAdvancementNames.PANDA_DO_NOT_WEAR_RINGS,
			LibAdvancementNames.OVERLOAD,
			LibAdvancementNames.LOAD_OF_RING,
			LibAdvancementNames.HUNDRED_BLOCK_PIERCE,
			LibAdvancementNames.SKY_IS_NOT_THE_LIMIT,
			LibAdvancementNames.POTATO_SERVER,
			LibAdvancementNames.RHEIN_KRAFT,
			LibAdvancementNames.SPONGE_HAMMER,
			LibAdvancementNames.OMG_ITS_MIKU,
			LibAdvancementNames.SEVEN_SAMURAI,
			LibAdvancementNames.GOBLIN_KILLER,
			LibAdvancementNames.CUTE_DRESS,
			LibAdvancementNames.CORRUPTION,
			LibAdvancementNames.A_BALDRUPT,
			LibAdvancementNames.MUROMI_SAN,
			LibAdvancementNames.ITS_ONLY_THE_FAIRY_TALE,
			LibAdvancementNames.PROJECT_DIVA_DESU
	};

	private OldExbotanyStatRewardHandler() {}

	public static void onAdvancementEarned(ServerPlayer player, ResourceLocation advancementId) {
		if (!LibMisc.MOD_ID.equals(advancementId.getNamespace())) {
			return;
		}
		if (ALL_STATS_ID.equals(advancementId)) {
			dropTrophy(player);
			return;
		}
		if (isTrackedStat(advancementId)) {
			grantAllStatsIfComplete(player);
		}
	}

	private static boolean isTrackedStat(ResourceLocation advancementId) {
		for (String stat : STAT_ADVANCEMENTS) {
			if (advancement(stat).equals(advancementId)) {
				return true;
			}
		}
		return false;
	}

	private static void grantAllStatsIfComplete(ServerPlayer player) {
		if (PlayerHelper.hasAdvancement(player, ALL_STATS_ID)) {
			return;
		}
		for (String stat : STAT_ADVANCEMENTS) {
			if (!PlayerHelper.hasAdvancement(player, advancement(stat))) {
				return;
			}
		}
		PlayerHelper.grantCriterion(player, ALL_STATS_ID, CODE_TRIGGERED);
	}

	private static void dropTrophy(ServerPlayer player) {
		ItemEntity entity = player.drop(new ItemStack(ExtraBotanyBlocks.trophy), false);
		if (entity != null) {
			entity.setNoPickUpDelay();
		}
	}

	private static ResourceLocation advancement(String name) {
		return prefix("main/" + name);
	}
}
