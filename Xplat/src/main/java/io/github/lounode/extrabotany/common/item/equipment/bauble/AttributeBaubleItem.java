package io.github.lounode.extrabotany.common.item.equipment.bauble;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class AttributeBaubleItem extends SimpleBaubleItem {
	private final List<Entry> modifiers;

	public AttributeBaubleItem(Properties props, List<Entry> modifiers) {
		super(props);
		this.modifiers = modifiers;
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack, ResourceLocation slotIdentifier) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		for (Entry entry : modifiers) {
			attributes.put(entry.attribute(),
					new AttributeModifier(entry.id(), entry.amount(), entry.operation()));
		}
		return attributes;
	}

	public record Entry(Holder<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {}
}
