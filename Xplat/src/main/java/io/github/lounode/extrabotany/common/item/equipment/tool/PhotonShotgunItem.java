package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.entity.PhotonShotgunProjectileEntity;

public class PhotonShotgunItem extends Item {
	private static final int PELLET_COUNT = 12;
	private static final float SPREAD_DEGREES = 8F;
	private static final double SPEED = 3.2D;

	public PhotonShotgunItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!level.isClientSide()) {
			for (int i = 0; i < PELLET_COUNT; i++) {
				float pitch = player.getXRot() - SPREAD_DEGREES + level.random.nextFloat() * SPREAD_DEGREES * 2F;
				float yaw = player.getYRot() - SPREAD_DEGREES + level.random.nextFloat() * SPREAD_DEGREES * 2F;
				Vec3 velocity = Vec3.directionFromRotation(pitch, yaw).scale(SPEED);

				PhotonShotgunProjectileEntity pellet = new PhotonShotgunProjectileEntity(level, player);
				pellet.setPos(player.getX(), player.getEyeY() - 0.1D, player.getZ());
				pellet.setDeltaMovement(velocity);
				pellet.setXRot(pitch);
				pellet.setYRot(yaw);
				level.addFreshEntity(pellet);
			}
		}

		player.awardStat(Stats.ITEM_USED.get(this));
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
