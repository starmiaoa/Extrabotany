package io.github.lounode.extrabotany.common.item.equipment.armor.old;

import com.google.common.base.Suppliers;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.mana.ManaDiscountArmor;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.equipment.tool.ToolCommons;

import io.github.lounode.extrabotany.api.ExtraBotanyAPI;
import io.github.lounode.extrabotany.client.lib.ResourcesLib;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.equipment.armor.starry_idol.StarryIdolArmorItem;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class OldExbotanyArmorItem extends StarryIdolArmorItem implements ManaDiscountArmor {
	private static final int MANA_PER_DAMAGE = 70;
	private static final int MANA_PER_REPAIR = MANA_PER_DAMAGE * 2;

	public enum Variant {
		MIKU("miku",
				() -> ExtraBotanyAPI.instance().getMikuArmorMaterial(),
				ResourcesLib.MODEL_MIKU,
				Suppliers.memoize(() -> new ItemStack[] {
						new ItemStack(ExtraBotanyItems.mikuHelm),
						new ItemStack(ExtraBotanyItems.mikuChest),
						new ItemStack(ExtraBotanyItems.mikuLegs),
						new ItemStack(ExtraBotanyItems.mikuBoots)
				})),
		SHOOTING_GUARDIAN("shootingguardian",
				() -> ExtraBotanyAPI.instance().getShootingGuardianArmorMaterial(),
				ResourcesLib.MODEL_SHOOTING_GUARDIAN,
				Suppliers.memoize(() -> new ItemStack[] {
						new ItemStack(ExtraBotanyItems.shootingGuardianHelm),
						new ItemStack(ExtraBotanyItems.shootingGuardianChest),
						new ItemStack(ExtraBotanyItems.shootingGuardianLegs),
						new ItemStack(ExtraBotanyItems.shootingGuardianBoots)
				})),
		SILENT_SAGES("silentsages",
				() -> ExtraBotanyAPI.instance().getSilentSagesArmorMaterial(),
				ResourcesLib.MODEL_SHADOW_WARRIOR,
				Suppliers.memoize(() -> new ItemStack[] {
						new ItemStack(ExtraBotanyItems.silentSagesHelm),
						new ItemStack(ExtraBotanyItems.silentSagesChest),
						new ItemStack(ExtraBotanyItems.silentSagesLegs),
						new ItemStack(ExtraBotanyItems.silentSagesBoots)
				}));

		private final String id;
		private final Supplier<Holder<ArmorMaterial>> material;
		private final String armorTexture;
		private final Supplier<ItemStack[]> armorSet;

		Variant(String id, Supplier<Holder<ArmorMaterial>> material, String armorTexture, Supplier<ItemStack[]> armorSet) {
			this.id = id;
			this.material = material;
			this.armorTexture = armorTexture;
			this.armorSet = armorSet;
		}
	}

	protected final Variant variant;

	public OldExbotanyArmorItem(Variant variant, Type type, Properties properties) {
		super(variant.material.get(), type, properties);
		this.variant = variant;
	}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
		return ToolCommons.damageItemIfPossible(stack, amount, entity, getManaPerDamage());
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
		triggerAdvancement(entity);
		tickOldManaRepair(stack, world, entity);
	}

	private void tickOldManaRepair(ItemStack stack, Level world, Entity entity) {
		if (!(entity instanceof Player player) || world.isClientSide() || stack.getDamageValue() <= 0) {
			return;
		}

		if (ManaItemHandler.instance().requestManaExact(stack, player, MANA_PER_REPAIR, true)) {
			stack.setDamageValue(stack.getDamageValue() - 1);
		}
	}

	@Override
	public float getDiscount(ItemStack stack, int slot, Player player, @Nullable ItemStack tool) {
		return hasArmorSet(player) ? 0.15F : 0F;
	}

	@Override
	public String getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
		if (variant == Variant.SHOOTING_GUARDIAN && slot == EquipmentSlot.HEAD) {
			return ResourcesLib.MODEL_SHOOTING_GUARDIAN_HELMET;
		}
		return variant.armorTexture;
	}

	@Override
	public MutableComponent getArmorSetName() {
		return Component.translatable("extrabotany.armorset." + variant.id + ".name");
	}

	@Override
	public void addArmorSetDescription(ItemStack stack, List<Component> list, boolean hasArmorSet) {
		ChatFormatting style = hasArmorSet ? ChatFormatting.AQUA : ChatFormatting.GRAY;
		if (variant == Variant.SHOOTING_GUARDIAN) {
			for (int i = 0; i < 4; i++) {
				list.add(Component.translatable("extrabotany.armorset.shootingguardian.desc" + i).withStyle(style));
			}
			return;
		}
		list.add(Component.translatable("extrabotany.armorset." + variant.id + ".desc").withStyle(style));
	}

	@Override
	public boolean hasArmorSetItem(Player player, EquipmentSlot slot) {
		if (player == null || player.getInventory() == null || player.getInventory().armor == null) {
			return false;
		}

		ItemStack stack = player.getItemBySlot(slot);
		if (stack.isEmpty()) {
			return false;
		}

		return switch (slot) {
			case HEAD -> stack.is(getArmorSetStacks()[0].getItem());
			case CHEST -> stack.is(getArmorSetStacks()[1].getItem());
			case LEGS -> stack.is(getArmorSetStacks()[2].getItem());
			case FEET -> stack.is(getArmorSetStacks()[3].getItem());
			default -> false;
		};
	}

	@Override
	public ItemStack[] getArmorSetStacks() {
		return variant.armorSet.get();
	}

	@Override
	public int getManaPerDamage() {
		return MANA_PER_DAMAGE;
	}

	public ResourceLocation modifierId(String name) {
		return prefix(variant.id + "_" + name + "." + type.getName());
	}
}
