package io.github.lounode.extrabotany.common.item.equipment.armor.old;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ShootingGuardianArmorItem extends OldExbotanyArmorItem {
	public ShootingGuardianArmorItem(Type type, Properties properties) {
		super(Variant.SHOOTING_GUARDIAN, type, properties);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
		Multimap<Attribute, AttributeModifier> ret = super.getDefaultAttributeModifiers(slot);
		if (slot != getEquipmentSlot()) {
			return ret;
		}

		ret = HashMultimap.create(ret);
		ret.put(Attributes.FLYING_SPEED, new AttributeModifier(modifierUuid("flying_speed"), "Shooting Guardian flying speed " + type, 0.10D, AttributeModifier.Operation.MULTIPLY_BASE));
		ret.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(modifierUuid("movement_speed"), "Shooting Guardian movement speed " + type, 0.10D, AttributeModifier.Operation.MULTIPLY_BASE));
		ret.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(modifierUuid("attack_damage"), "Shooting Guardian attack damage " + type, 0.10D, AttributeModifier.Operation.MULTIPLY_BASE));
		ret.put(Attributes.ATTACK_SPEED, new AttributeModifier(modifierUuid("attack_speed"), "Shooting Guardian attack speed " + type, 0.03D, AttributeModifier.Operation.MULTIPLY_BASE));
		return ret;
	}
}
