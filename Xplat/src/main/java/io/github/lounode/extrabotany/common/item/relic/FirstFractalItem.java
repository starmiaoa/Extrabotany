package io.github.lounode.extrabotany.common.item.relic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.api.mana.ManaItemHandler;

import io.github.lounode.extrabotany.common.entity.PhantomSwordEntity;

import java.util.UUID;

public class FirstFractalItem extends OldExbotanyRelicSwordItem {
	private static final double RANGE = 13D;
	private static final int MANA_PER_DAMAGE = 160;
	private static final UUID MOVEMENT_SPEED_MODIFIER = UUID.fromString("995829fa-94c0-41bd-b046-0468c509a48a");

	public FirstFractalItem(Properties properties) {
		super(Tiers.NETHERITE, 10, -1.6F, 0, true, properties);
	}

	@Override
	protected void useSword(Player player, Entity target) {
		Vec3 targetPos = resolveTargetPos(player, target, 80D);
		double angle = -Math.PI + 2 * Math.PI * player.level().random.nextDouble();
		float damage = (float) (player.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2F);

		for (int i = 0; i < 3; i++) {
			if (!ManaItemHandler.instance().requestManaExactForTool(player.getMainHandItem(), player, MANA_PER_DAMAGE, true)) {
				return;
			}
			Vec3 start = randomStart(player, targetPos, angle);
			player.level().addFreshEntity(new PhantomSwordEntity(player.level(), player, start, targetPos, 5 + 5 * i, damage));
			angle += 2 * Math.PI * player.level().random.nextDouble() * 0.08D + 2 * Math.PI * 0.17D;
		}

		Vec3 start = randomStart(player, targetPos, angle);
		player.level().addFreshEntity(new PhantomSwordEntity(player.level(), player, start, targetPos, 0, damage));
	}

	private Vec3 randomStart(Player player, Vec3 targetPos, double angle) {
		double pitch = 0.12D * Math.PI * player.level().random.nextDouble() + 0.28D * Math.PI;
		return new Vec3(
				targetPos.x + RANGE * Math.sin(pitch) * Math.cos(angle),
				targetPos.y + RANGE * Math.cos(pitch),
				targetPos.z + RANGE * Math.sin(pitch) * Math.sin(angle)
		);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
		Multimap<Attribute, AttributeModifier> ret = super.getDefaultAttributeModifiers(slot);
		if (slot == EquipmentSlot.MAINHAND) {
			ret = HashMultimap.create(ret);
			ret.put(Attributes.MOVEMENT_SPEED,
					new AttributeModifier(MOVEMENT_SPEED_MODIFIER, "First Fractal modifier", 0.3D, AttributeModifier.Operation.MULTIPLY_TOTAL));
		}
		return ret;
	}
}
