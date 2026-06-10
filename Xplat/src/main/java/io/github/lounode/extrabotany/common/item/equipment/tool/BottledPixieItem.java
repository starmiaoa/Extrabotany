package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.entity.PixieEntity;

public class BottledPixieItem extends Item {
	private static final int MANA_PER_USE = 300;
	private static final int COOLDOWN = 240;
	private static final double RANGE = 16D;
	private static final float DAMAGE = 3.5F;

	public BottledPixieItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResultHolder.success(stack);
		}
		if (!ManaItemHandler.instance().requestManaExactForTool(stack, serverPlayer, MANA_PER_USE, true)) {
			return InteractionResultHolder.fail(stack);
		}

		LivingEntity target = findTarget(serverPlayer);
		PixieEntity pixie = new PixieEntity(level);
		pixie.setPos(serverPlayer.getX(), serverPlayer.getY() + 1.5D, serverPlayer.getZ());
		pixie.setProps(target != null ? target : serverPlayer, serverPlayer, 1, DAMAGE);
		pixie.finalizeSpawn((ServerLevelAccessor) level, level.getCurrentDifficultyAt(pixie.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
		level.addFreshEntity(pixie);
		serverPlayer.getCooldowns().addCooldown(this, COOLDOWN);
		return InteractionResultHolder.success(stack);
	}

	private LivingEntity findTarget(ServerPlayer player) {
		AABB area = player.getBoundingBox().inflate(RANGE);
		return player.level().getEntitiesOfClass(Monster.class, area).stream()
				.filter(monster -> monster.isAlive() && player.hasLineOfSight(monster))
				.min((left, right) -> Double.compare(player.distanceToSqr(left), player.distanceToSqr(right)))
				.orElse(null);
	}
}
