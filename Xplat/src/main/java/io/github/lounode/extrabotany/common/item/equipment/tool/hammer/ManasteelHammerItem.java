package io.github.lounode.extrabotany.common.item.equipment.tool.hammer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.SortableTool;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.equipment.CustomDamageItem;
import vazkii.botania.common.item.equipment.tool.ToolCommons;

import java.util.function.Consumer;

public class ManasteelHammerItem extends PickaxeItem implements CustomDamageItem, SortableTool {
	private static final int MANA_PER_DAMAGE = 60;

	public ManasteelHammerItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
		super(tier, properties.attributes(PickaxeItem.createAttributes(tier, attackDamageModifier, attackSpeedModifier)));
	}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
		int manaPerDamage = ((ManasteelHammerItem) stack.getItem()).getManaPerDamage();
		return ToolCommons.damageItemIfPossible(stack, amount, entity, manaPerDamage);
	}

	public int getManaPerDamage() {
		return MANA_PER_DAMAGE;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		if (!world.isClientSide && entity instanceof Player player && stack.getDamageValue() > 0 && ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_DAMAGE * 2, true)) {
			stack.setDamageValue(stack.getDamageValue() - 1);
		}
	}

	@Override
	public int getSortingPriority(ItemStack stack, BlockState state) {
		Tier tier = getTier();
		int tierPriority = 0;
		if (tier == BotaniaAPI.instance().getManasteelItemTier()) {
			tierPriority = 10;
		} else if (tier == BotaniaAPI.instance().getElementiumItemTier()) {
			tierPriority = 11;
		} else if (tier == BotaniaAPI.instance().getTerrasteelItemTier()) {
			tierPriority = 20;
		}

		int efficiency = 0;
		for (var entry : stack.getEnchantments().entrySet()) {
			if (entry.getKey().is(Enchantments.EFFICIENCY)) {
				efficiency = entry.getIntValue();
				break;
			}
		}
		return tierPriority * 100 + efficiency;
	}
}
