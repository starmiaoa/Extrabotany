package io.github.lounode.extrabotany.common.entity.gaia;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.schedule.Activity;

import io.github.lounode.extrabotany.common.entity.gaia.behavior.GaiaCleanPlayerUnstableEffects;
import io.github.lounode.extrabotany.common.entity.gaia.behavior.GaiaSmashBlocksAround;

public class VoidHerrscherAI extends GaiaAI {
	public static final int EMERGE_TIME = 180;

	public VoidHerrscherAI() {}

	public static void updateActivity(Gaia gaia) {
		gaia.getBrain().setActiveActivityToFirstValid(ImmutableList.of(
				Activity.EMERGE,
				Activity.FIGHT
		));
	}

	protected static void initMemories(Gaia gaia, ServerLevel level, BlockPos pos) {
		// Void Herrscher orchestrates all movement, teleports, and attacks directly in VoidHerrscher.tickHerrscher().
	}

	protected static Brain<?> makeBrain(Gaia gaia, Dynamic<?> ops) {
		Brain.Provider<VoidHerrscher> provider = Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
		Brain<VoidHerrscher> brain = provider.makeBrain(ops);
		initCoreActivity(brain);
		initSpawnActivity(brain, EMERGE_TIME);
		initFightActivity(brain);

		brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
		brain.setDefaultActivity(Activity.EMERGE);
		brain.useDefaultActivity();
		return brain;
	}

	protected static void initCoreActivity(Brain<? extends Gaia> brain) {
		brain.addActivity(Activity.CORE, 0, ImmutableList.of(
				new Swim(0.8F)
		));
	}

	private static void initFightActivity(Brain<? extends Gaia> brain) {
		brain.addActivity(Activity.FIGHT, 10, ImmutableList.of(
				new GaiaSmashBlocksAround<>(),
				new GaiaCleanPlayerUnstableEffects<>()
		));
	}
}
