package io.github.lounode.extrabotany.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.mana.ManaItemHandler;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.util.FrostWalkerEffectHelper;

import java.util.List;

public class ZadkielItem extends Item {
	private static final int FROST_RANGE = 6;
	private static final int PASSIVE_SLOW_RANGE = 5;
	private static final int PASSIVE_SLOW_INTERVAL = 40;
	private static final int SPIKE_MANA_COST = 250;
	private static final int SPIKE_COOLDOWN = 60;
	private static final int SPIKE_COUNT = 8;
	private static final double SPIKE_SPACING = 1.25D;

	public ZadkielItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, @NotNull InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!player.getAbilities().instabuild &&
				!ManaItemHandler.instance().requestManaExactForTool(stack, player, SPIKE_MANA_COST, true)) {
			return InteractionResultHolder.fail(stack);
		}

		if (!world.isClientSide) {
			ServerLevel serverWorld = (ServerLevel) world;
			spawnIceSpikes(serverWorld, player);
			player.getCooldowns().addCooldown(this, SPIKE_COOLDOWN);
		}

		return InteractionResultHolder.success(stack);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
		super.inventoryTick(stack, level, entity, slotId, isSelected);
		if (level.isClientSide()) {
			return;
		}
		if (!(entity instanceof LivingEntity living)) {
			return;
		}
		FrostWalkerEffectHelper.apply(living, stack, FROST_RANGE);

		if (!(living instanceof Player player) || player.tickCount % PASSIVE_SLOW_INTERVAL != 0 || !isHeld(stack, player)) {
			return;
		}
		for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(PASSIVE_SLOW_RANGE))) {
			if (target == player || !player.hasLineOfSight(target) || !DamageHandler.INSTANCE.checkPassable(target, player)) {
				continue;
			}
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
		}
	}

	private static boolean isHeld(ItemStack stack, Player player) {
		return player.getMainHandItem() == stack || player.getOffhandItem() == stack;
	}

	private static void spawnIceSpikes(ServerLevel level, Player player) {
		Vec3 direction = player.getLookAngle();
		Vec3 horizontal = new Vec3(direction.x, 0.0D, direction.z);
		if (horizontal.lengthSqr() < 1.0E-4D) {
			horizontal = Vec3.directionFromRotation(0.0F, player.getYRot());
		}
		horizontal = horizontal.normalize();

		for (int i = 0; i < SPIKE_COUNT; i++) {
			double distance = 1.75D + i * SPIKE_SPACING;
			Vec3 start = player.position().add(horizontal.scale(distance)).add(0.0D, 2.0D, 0.0D);
			BlockHitResult hit = level.clip(new ClipContext(
					start, start.add(0.0D, -8.0D, 0.0D),
					ClipContext.Block.COLLIDER,
					ClipContext.Fluid.NONE,
					player
			));

			Vec3 pos = hit.getLocation();
			double y = hit.getBlockPos().getY() + 1.0D;
			int warmup = i * 2;
			EvokerFangs fangs = new EvokerFangs(level, pos.x(), y, pos.z(), (float) Math.toRadians(player.getYRot()), warmup, player);
			level.addFreshEntity(fangs);
			level.sendParticles(ParticleTypes.SNOWFLAKE, pos.x(), y + 0.5D, pos.z(), 18, 0.35D, 0.45D, 0.35D, 0.04D);
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		tooltip.add(Component.translatable("tooltip.extrabotany.zadkiel").withStyle(ChatFormatting.GRAY));
		super.appendHoverText(stack, context, tooltip, flags);
	}
}
