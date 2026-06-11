package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.relic.RelicImpl;

import io.github.lounode.extrabotany.common.event.entity.player.AttackEntityEventWrapper;
import io.github.lounode.extrabotany.common.event.entity.player.PlayerInteractEventWrapper;
import io.github.lounode.extrabotany.network.serverbound.LeftClickPacketOldRelicSword;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import io.github.lounode.extrabotany.xplat.ExClientXplatAbstractions;

import java.util.Comparator;
import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public abstract class OldExbotanyRelicSwordItem extends SwordItem {
	private static final double RAYTRACE_RANGE = 80D;

	private final int manaPerUse;
	private final boolean relic;

	protected OldExbotanyRelicSwordItem(Tier tier, int attackDamage, float attackSpeed, int manaPerUse, boolean relic, Properties properties) {
		this(tier, attackDamage, attackSpeed, manaPerUse, relic, properties, SwordItem.createAttributes(tier, attackDamage, attackSpeed));
	}

	protected OldExbotanyRelicSwordItem(Tier tier, int attackDamage, float attackSpeed, int manaPerUse, boolean relic, Properties properties,
			ItemAttributeModifiers attributes) {
		super(tier, properties.attributes(attributes));
		this.manaPerUse = manaPerUse;
		this.relic = relic;
	}

	public static void leftClick(PlayerInteractEventWrapper.LeftClickEmpty event) {
		ItemStack stack = event.getItemStack();
		if (!stack.isEmpty() && (stack.getItem() instanceof OldExbotanyRelicSwordItem || stack.getItem() instanceof SpearOfSubspaceItem)) {
			ExClientXplatAbstractions.INSTANCE.sendToServer(LeftClickPacketOldRelicSword.INSTANCE);
		}
	}

	public static void attackEntity(AttackEntityEventWrapper event) {
		Player player = event.getEntity();
		if (!player.level().isClientSide && player.getMainHandItem().getItem() instanceof OldExbotanyRelicSwordItem sword) {
			sword.tryUse(player, event.getTarget(), player.getAttackStrengthScale(0F));
		}
	}

	public static void leftClickBlock(PlayerInteractEventWrapper.LeftClickBlock event) {
		Player player = event.getEntity();
		if (!player.level().isClientSide && event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
				&& player.getMainHandItem().getItem() instanceof OldExbotanyRelicSwordItem sword) {
			sword.tryUse(player, null, player.getAttackStrengthScale(0F));
		}
	}

	public static void tryUseFromPacket(ServerPlayer player, float attackStrength) {
		if (player.getMainHandItem().getItem() instanceof OldExbotanyRelicSwordItem sword) {
			sword.tryUse(player, null, attackStrength);
		}
	}

	private void tryUse(Player player, Entity target, float attackStrength) {
		ItemStack stack = player.getMainHandItem();
		if (player.isSpectator() || stack.getItem() != this || attackStrength != 1F) {
			return;
		}
		if (this.relic) {
			Relic relic = EXplatAbstractions.INSTANCE.findRelic(stack);
			if (relic == null || !relic.isRightPlayer(player)) {
				return;
			}
		}
		ResourceLocation required = getRequiredAdvancement();
		if (required != null && player instanceof ServerPlayer serverPlayer
				&& !hasAdvancement(serverPlayer, required)) {
			player.displayClientMessage(Component.translatable("extrabotany.message.advancement_required").withStyle(ChatFormatting.RED), true);
			return;
		}
		if (this.manaPerUse > 0 && !ManaItemHandler.instance().requestManaExactForTool(stack, player, this.manaPerUse, true)) {
			return;
		}

		useSword(player, target);
	}

	private static boolean hasAdvancement(ServerPlayer player, ResourceLocation id) {
		var advancement = player.server.getAdvancements().get(id);
		return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
	}

	protected Vec3 resolveTargetPos(LivingEntity user, Entity target, double range) {
		if (target != null) {
			return target.position().add(0, target.getBbHeight() * 0.5D, 0);
		}

		Vec3 eye = user.getEyePosition();
		Vec3 look = user.getLookAngle();
		Vec3 end = eye.add(look.scale(range));
		HitResult hit = user.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user));
		return hit.getType() == HitResult.Type.MISS ? end : hit.getLocation().add(0, 1, 0);
	}

	protected LivingEntity findNearbyTarget(LivingEntity user, Entity explicitTarget, double range) {
		if (explicitTarget instanceof LivingEntity living && DamageHandlerLike.canDamage(living, user)) {
			return living;
		}

		AABB area = user.getBoundingBox().inflate(range);
		return user.level().getEntitiesOfClass(LivingEntity.class, area).stream()
				.filter(target -> DamageHandlerLike.canDamage(target, user))
				.min(Comparator.comparingDouble(user::distanceToSqr))
				.orElse(null);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		if (!level.isClientSide && this.relic && entity instanceof Player player) {
			Relic relic = EXplatAbstractions.INSTANCE.findRelic(stack);
			if (relic != null) {
				relic.tickBinding(player);
			}
		}
		super.inventoryTick(stack, level, entity, slot, selected);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		if (this.relic) {
			tooltip.add(Component.empty());
			RelicImpl.addDefaultTooltip(stack, tooltip);
		} else {
			tooltip.add(Component.translatable("tooltip.extrabotany.old_exbotany_sword").withStyle(ChatFormatting.GRAY));
		}
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, null);
	}

	protected abstract void useSword(Player player, Entity target);

	protected ResourceLocation getRequiredAdvancement() {
		return null;
	}

	private static final class DamageHandlerLike {
		private static boolean canDamage(LivingEntity target, Entity source) {
			if (target == source || !target.isAlive()) {
				return false;
			}
			if (source instanceof Player player && target instanceof Player other) {
				return player.canHarmPlayer(other);
			}
			if (source instanceof Mob) {
				return target instanceof Player;
			}
			DamageSource sourceDamage = source instanceof Player player
					? source.damageSources().playerAttack(player)
					: source.damageSources().generic();
			return !target.isInvulnerableTo(sourceDamage);
		}
	}
}
