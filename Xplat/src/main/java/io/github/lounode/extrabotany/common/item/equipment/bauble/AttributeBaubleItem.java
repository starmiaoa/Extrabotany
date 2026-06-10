package io.github.lounode.extrabotany.common.item.equipment.bauble;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public class AttributeBaubleItem extends SimpleBaubleItem {
	private final List<Entry> modifiers;

	public AttributeBaubleItem(Properties props, List<Entry> modifiers) {
		super(props);
		this.modifiers = modifiers;
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack) {
		Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
		for (Entry entry : modifiers) {
			attributes.put(entry.attribute(),
					new AttributeModifier(entry.id(), entry.name(), entry.amount(), entry.operation()));
		}
		return attributes;
	}

	public record Entry(Attribute attribute, UUID id, String name, double amount, AttributeModifier.Operation operation) {}
}
