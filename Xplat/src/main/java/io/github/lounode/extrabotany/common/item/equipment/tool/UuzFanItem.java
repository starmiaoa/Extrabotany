package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import io.github.lounode.extrabotany.common.entity.ButterflyProjectileEntity;

public class UuzFanItem extends Item {
	private static final int COOLDOWN = 10;
	private static final float FAN_SPREAD = 25F;
	private static final float PROJECTILE_SPEED = 0.5F;
	private static final float PROJECTILE_INACCURACY = 1F;

	public UuzFanItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		player.getCooldowns().addCooldown(this, COOLDOWN);

		if (!level.isClientSide()) {
			for (int i = -1; i <= 1; i++) {
				ButterflyProjectileEntity projectile = new ButterflyProjectileEntity(level, player);
				projectile.setPos(player.getX(), player.getY(), player.getZ());
				projectile.shootFromRotation(player, player.getXRot(), player.getYRot() + FAN_SPREAD * i,
						0.0F, PROJECTILE_SPEED, PROJECTILE_INACCURACY);
				level.addFreshEntity(projectile);
			}
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
