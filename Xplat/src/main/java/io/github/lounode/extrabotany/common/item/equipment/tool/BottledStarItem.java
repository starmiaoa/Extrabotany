package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.ManaItemHandler;

import io.github.lounode.extrabotany.common.entity.BottledStarEntity;

public class BottledStarItem extends Item {
	private static final int MANA_PER_USE = 300;
	private static final int COOLDOWN = 100;

	public BottledStarItem(Properties properties) {
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

		BottledStarEntity star = new BottledStarEntity(level, serverPlayer);
		star.setPos(serverPlayer.getX(), serverPlayer.getY() + 1.2D, serverPlayer.getZ());
		level.addFreshEntity(star);
		serverPlayer.getCooldowns().addCooldown(this, COOLDOWN);
		return InteractionResultHolder.success(stack);
	}
}
