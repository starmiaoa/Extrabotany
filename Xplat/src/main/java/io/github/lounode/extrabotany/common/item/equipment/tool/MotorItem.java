package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.entity.MotorEntity;

import java.util.List;
import java.util.function.Predicate;

public class MotorItem extends Item {
	private static final Predicate<Entity> COLLISION_CHECK = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);

	public MotorItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		HitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
		if (hit.getType() == HitResult.Type.MISS) {
			return InteractionResultHolder.pass(stack);
		}

		Vec3 look = player.getViewVector(1F);
		List<Entity> entities = level.getEntities(player,
				player.getBoundingBox().expandTowards(look.scale(5D)).inflate(1D),
				COLLISION_CHECK);
		if (!entities.isEmpty()) {
			Vec3 eye = player.getEyePosition(1F);
			for (Entity entity : entities) {
				AABB bounds = entity.getBoundingBox().inflate(entity.getPickRadius());
				if (bounds.contains(eye)) {
					return InteractionResultHolder.pass(stack);
				}
			}
		}

		if (hit.getType() != HitResult.Type.BLOCK) {
			return InteractionResultHolder.pass(stack);
		}

		MotorEntity motor = new MotorEntity(level, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z);
		motor.setYRot(player.getYRot());
		motor.setOwner(player.getUUID());
		if (!level.noCollision(motor, motor.getBoundingBox().inflate(-0.1D))) {
			return InteractionResultHolder.fail(stack);
		}

		if (!level.isClientSide()) {
			level.addFreshEntity(motor);
			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
}
