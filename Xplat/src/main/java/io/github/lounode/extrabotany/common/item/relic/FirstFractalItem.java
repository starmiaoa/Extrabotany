package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.BotaniaSounds;

import io.github.lounode.extrabotany.common.entity.PhantomSwordEntity;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;
import io.github.lounode.extrabotany.common.util.HerrscherCombatHelper;

import java.util.List;
import java.util.Optional;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class FirstFractalItem extends OldExbotanyRelicSwordItem {
	private static final double RANGE = 12.5D;
	private static final int MANA_PER_DAMAGE = 100;

	public FirstFractalItem(Properties properties) {
		super(Tiers.NETHERITE, 16, -1.2F, 0, true, properties,
				SwordItem.createAttributes(Tiers.NETHERITE, 16, -1.2F)
						.withModifierAdded(Attributes.MOVEMENT_SPEED,
								new AttributeModifier(prefix("first_fractal_movement_speed"), 0.35D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
								EquipmentSlotGroup.bySlot(EquipmentSlot.MAINHAND))
						.withModifierAdded(Attributes.ATTACK_SPEED,
								new AttributeModifier(prefix("first_fractal_attack_speed"), 0.25D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
								EquipmentSlotGroup.bySlot(EquipmentSlot.MAINHAND))
						.withModifierAdded(Attributes.ENTITY_INTERACTION_RANGE,
								new AttributeModifier(prefix("first_fractal_interaction_range"), 6.0D, AttributeModifier.Operation.ADD_VALUE),
								EquipmentSlotGroup.bySlot(EquipmentSlot.MAINHAND)));
	}

	@Override
	protected float getMinAttackStrength() {
		return 0.65F;
	}

	@Override
	protected boolean ignoresInvulnerability() {
		return true;
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (!attacker.level().isClientSide() && attacker instanceof Player player) {
			float atk = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
			target.invulnerableTime = 0;
			HerrscherCombatHelper.dealTrueMagicDamage(target, player, Math.max(3.0F, atk * 0.2F));
			target.invulnerableTime = 0;
		}
		return super.hurtEnemy(stack, target, attacker);
	}

	@Override
	protected void useSword(Player player, Entity target) {
		if (!player.getAbilities().instabuild && !ManaItemHandler.instance()
				.requestManaExactForTool(player.getMainHandItem(), player, MANA_PER_DAMAGE * 4, true)) {
			return;
		}

		LivingEntity lockedTarget = target instanceof LivingEntity living ? living : findAimedTarget(player, 48.0D);
		Vec3 targetPos = lockedTarget != null
				? lockedTarget.position().add(0.0D, lockedTarget.getBbHeight() * 0.5D, 0.0D)
				: resolveTargetPos(player, target, 80.0D);

		double baseAngle = -Math.PI + 2.0D * Math.PI * player.level().random.nextDouble();

		// 2 immediate converging vanguard fractal blades (variety 9 + random)
		for (int i = 0; i < 2; i++) {
			double a = baseAngle + i * Math.PI;
			Vec3 start = randomStart(player, targetPos, a);
			PhantomSwordEntity blade = (i == 0)
					? new PhantomSwordEntity(player.level(), player, start, targetPos, 0, 9)
					: new PhantomSwordEntity(player.level(), player, start, targetPos, 0);
			blade.setLockedTarget(lockedTarget);
			player.level().addFreshEntity(blade);
		}

		// 5 rapid spiral follow-up fractal blades that re-lock onto moving targets at launch
		double angle = baseAngle + Math.PI * 0.2D;
		for (int i = 0; i < 5; i++) {
			Vec3 start = randomStart(player, targetPos, angle);
			PhantomSwordEntity blade = new PhantomSwordEntity(player.level(), player, start, targetPos, 2 + 2 * i);
			blade.setLockedTarget(lockedTarget);
			player.level().addFreshEntity(blade);
			angle += (Math.PI * 2.0D / 5.0D) + (player.level().random.nextDouble() - 0.5D) * 0.22D;
		}

		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), BotaniaSounds.TERRABLADE, SoundSource.PLAYERS, 0.45F, 1.45F);
	}

	private LivingEntity findAimedTarget(Player player, double maxRange) {
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle();
		Vec3 end = eye.add(look.scale(maxRange));
		AABB search = player.getBoundingBox().expandTowards(look.scale(maxRange)).inflate(3.5D);
		List<LivingEntity> candidates = DamageHandler.INSTANCE.getFilteredEntities(
				player.level().getEntitiesOfClass(LivingEntity.class, search), player);
		LivingEntity best = null;
		double bestDist = Double.MAX_VALUE;
		for (LivingEntity candidate : candidates) {
			Optional<Vec3> hit = candidate.getBoundingBox().inflate(2.2D).clip(eye, end);
			if (hit.isPresent()) {
				double d = eye.distanceToSqr(hit.get());
				if (d < bestDist) {
					bestDist = d;
					best = candidate;
				}
			}
		}
		return best;
	}

	private Vec3 randomStart(Player player, Vec3 targetPos, double angle) {
		// Upper hemisphere arc (0.14π ~ 0.46π) so fractal blades always strike down cleanly without burying underground
		double pitch = (0.14D + 0.32D * player.level().random.nextDouble()) * Math.PI;
		return new Vec3(
				targetPos.x + RANGE * Math.sin(pitch) * Math.cos(angle),
				targetPos.y + RANGE * Math.cos(pitch),
				targetPos.z + RANGE * Math.sin(pitch) * Math.sin(angle)
		);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (!level.isClientSide && entity instanceof Player player && stack.getDamageValue() > 0
				&& ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_DAMAGE * 2, true)) {
			stack.setDamageValue(stack.getDamageValue() - 1);
		}
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		tooltip.add(Component.translatable("tooltip.extrabotany.first_fractal").withStyle(ChatFormatting.AQUA));
		super.appendHoverText(stack, context, tooltip, flags);
	}

	@Override
	protected ResourceLocation getRequiredAdvancement() {
		return prefix("main/" + LibAdvancementNames.HERRSCHER_DEFEAT);
	}
}
