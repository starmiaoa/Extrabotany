package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.BotaniaSounds;

import io.github.lounode.extrabotany.common.entity.PhantomSwordEntity;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class FirstFractalItem extends OldExbotanyRelicSwordItem {
	private static final double RANGE = 13D;
	private static final int MANA_PER_DAMAGE = 160;

	public FirstFractalItem(Properties properties) {
		super(Tiers.NETHERITE, 7, -1.6F, 0, true, properties,
				SwordItem.createAttributes(Tiers.NETHERITE, 7, -1.6F)
						.withModifierAdded(Attributes.MOVEMENT_SPEED,
								new AttributeModifier(prefix("first_fractal_movement_speed"), 0.3D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
								EquipmentSlotGroup.bySlot(EquipmentSlot.MAINHAND))
						.withModifierAdded(Attributes.ATTACK_SPEED,
								new AttributeModifier(prefix("first_fractal_attack_speed"), 0.15D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
								EquipmentSlotGroup.bySlot(EquipmentSlot.MAINHAND))
						.withModifierAdded(Attributes.ENTITY_INTERACTION_RANGE,
								new AttributeModifier(prefix("first_fractal_interaction_range"), 5.0D, AttributeModifier.Operation.ADD_VALUE),
								EquipmentSlotGroup.bySlot(EquipmentSlot.MAINHAND)));
	}

	@Override
	protected void useSword(Player player, Entity target) {
		if (!player.getAbilities().instabuild && !ManaItemHandler.instance()
				.requestManaExactForTool(player.getMainHandItem(), player, MANA_PER_DAMAGE * 4, true)) {
			return;
		}
		Vec3 targetPos = resolveTargetPos(player, target, 80D);
		double angle = -Math.PI + 2 * Math.PI * player.level().random.nextDouble();

		for (int i = 0; i < 3; i++) {
			Vec3 start = randomStart(player, targetPos, angle);
			player.level().addFreshEntity(new PhantomSwordEntity(player.level(), player, start, targetPos, 5 + 5 * i));
			angle += 2 * Math.PI * player.level().random.nextDouble() * 0.08D + 2 * Math.PI * 0.17D;
		}

		Vec3 start = randomStart(player, targetPos, angle);
		player.level().addFreshEntity(new PhantomSwordEntity(player.level(), player, start, targetPos, 0, 9));
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), BotaniaSounds.terraBlade, SoundSource.PLAYERS, 0.4F, 1.4F);
	}

	private Vec3 randomStart(Player player, Vec3 targetPos, double angle) {
		double pitch = 0.6D * Math.PI * player.level().random.nextDouble();
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
	protected ResourceLocation getRequiredAdvancement() {
		return prefix("main/" + LibAdvancementNames.HERRSCHER_DEFEAT);
	}
}
