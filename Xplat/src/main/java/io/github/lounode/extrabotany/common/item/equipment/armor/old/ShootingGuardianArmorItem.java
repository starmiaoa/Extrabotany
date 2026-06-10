package io.github.lounode.extrabotany.common.item.equipment.armor.old;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class ShootingGuardianArmorItem extends OldExbotanyArmorItem {
	public ShootingGuardianArmorItem(Type type, Properties properties) {
		super(Variant.SHOOTING_GUARDIAN, type, properties);
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers() {
		EquipmentSlotGroup slotGroup = EquipmentSlotGroup.bySlot(type.getSlot());
		return super.getDefaultAttributeModifiers()
				.withModifierAdded(Attributes.FLYING_SPEED,
						new AttributeModifier(modifierId("flying_speed"), 0.10D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
						slotGroup)
				.withModifierAdded(Attributes.MOVEMENT_SPEED,
						new AttributeModifier(modifierId("movement_speed"), 0.10D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
						slotGroup)
				.withModifierAdded(Attributes.ATTACK_DAMAGE,
						new AttributeModifier(modifierId("attack_damage"), 0.10D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
						slotGroup)
				.withModifierAdded(Attributes.ATTACK_SPEED,
						new AttributeModifier(modifierId("attack_speed"), 0.03D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
						slotGroup);
	}
}
