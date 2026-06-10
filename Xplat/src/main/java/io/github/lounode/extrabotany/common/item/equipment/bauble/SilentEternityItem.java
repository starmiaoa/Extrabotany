package io.github.lounode.extrabotany.common.item.equipment.bauble;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.relic.RelicBaubleItem;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.List;

public class SilentEternityItem extends RelicBaubleItem {
	public static final int MAX_MANA = 666;
	private static final int FROST_RANGE = 4;
	private static final int STILL_TICKS_TO_HEAL = 15;
	private static final float HEAL_AMOUNT = 0.4F;
	private static final double MOVEMENT_EPSILON = 1.0E-4D;

	private static final String TAG_MANA = "mana";
	private static final String TAG_X = "posx";
	private static final String TAG_Y = "posy";
	private static final String TAG_Z = "posz";
	private static final String TAG_STOP_TICKS = "stopticks";

	public SilentEternityItem(Properties props) {
		super(props);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (!level.isClientSide && entity instanceof Player player) {
			Relic relic = XplatAbstractions.INSTANCE.findRelic(stack);
			if (relic != null) {
				relic.tickBinding(player);
			}
		}
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		if (!(entity instanceof Player player) || !isRightPlayer(stack, player)) {
			return;
		}

		setMana(stack, MAX_MANA);
		if (player.level().isClientSide) {
			return;
		}

		boolean wasOnGround = player.onGround();
		player.setOnGround(true);
		try {
			FrostWalkerEnchantment.onEntityMoved(player, player.level(), player.blockPosition(), FROST_RANGE);
			FrostRingItem.freezeLava(player, player.level(), player.blockPosition(), FROST_RANGE);
		} finally {
			player.setOnGround(wasOnGround);
		}

		if (isStill(stack, player)) {
			int stillTicks = getStopTicks(stack) + 1;
			setStopTicks(stack, stillTicks);
			if (stillTicks > STILL_TICKS_TO_HEAL) {
				player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + HEAL_AMOUNT));
				player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.ETERNITY, 10));
			}
		} else {
			setStopTicks(stack, 0);
		}

		setLastPosition(stack, player);
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return entity instanceof Player && super.canEquip(stack, entity)
				&& EquipmentHandler.findOrEmpty(ExtraBotanyItems.silentEternity, entity).isEmpty();
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack) {
		Multimap<Attribute, AttributeModifier> attributes = HashMultimap.create();
		attributes.put(Attributes.KNOCKBACK_RESISTANCE,
				new AttributeModifier(getBaubleUUID(stack), "Silent Eternity", 1.0D, AttributeModifier.Operation.ADDITION));
		return attributes;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, level, tooltip, flags);
		RelicImpl.addDefaultTooltip(stack, tooltip);
	}

	private static boolean isStill(ItemStack stack, Player player) {
		double dx = player.getX() - ItemNBTHelper.getDouble(stack, TAG_X, player.getX());
		double dy = player.getY() - ItemNBTHelper.getDouble(stack, TAG_Y, player.getY());
		double dz = player.getZ() - ItemNBTHelper.getDouble(stack, TAG_Z, player.getZ());
		return dx * dx + dy * dy + dz * dz <= MOVEMENT_EPSILON;
	}

	private static void setLastPosition(ItemStack stack, Player player) {
		ItemNBTHelper.setDouble(stack, TAG_X, player.getX());
		ItemNBTHelper.setDouble(stack, TAG_Y, player.getY());
		ItemNBTHelper.setDouble(stack, TAG_Z, player.getZ());
	}

	private static int getStopTicks(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_STOP_TICKS, 0);
	}

	private static void setStopTicks(ItemStack stack, int ticks) {
		ItemNBTHelper.setInt(stack, TAG_STOP_TICKS, ticks);
	}

	private static void setMana(ItemStack stack, int mana) {
		if (mana > 0) {
			ItemNBTHelper.setInt(stack, TAG_MANA, Math.min(mana, MAX_MANA));
		} else {
			ItemNBTHelper.removeEntry(stack, TAG_MANA);
		}
	}

	private static int getMana(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_MANA, 0);
	}

	private static boolean isRightPlayer(ItemStack stack, Player player) {
		Relic relic = XplatAbstractions.INSTANCE.findRelic(stack);
		return relic != null && relic.isRightPlayer(player);
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, null);
	}

	public static boolean shouldCancelHealing(LivingEntity entity) {
		if (!(entity instanceof Player player)) {
			return false;
		}

		ItemStack stack = EquipmentHandler.findOrEmpty(ExtraBotanyItems.silentEternity, player);
		return !stack.isEmpty() && isRightPlayer(stack, player);
	}

	public static class SilentEternityManaItem implements ManaItem {
		private final ItemStack stack;

		public SilentEternityManaItem(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public int getMana() {
			return SilentEternityItem.getMana(stack);
		}

		@Override
		public int getMaxMana() {
			return MAX_MANA;
		}

		@Override
		public void addMana(int mana) {
			setMana(stack, Math.max(0, Math.min(MAX_MANA, getMana() + mana)));
		}

		@Override
		public boolean canReceiveManaFromPool(BlockEntity pool) {
			return false;
		}

		@Override
		public boolean canReceiveManaFromItem(ItemStack otherStack) {
			return false;
		}

		@Override
		public boolean canExportManaToPool(BlockEntity pool) {
			return false;
		}

		@Override
		public boolean canExportManaToItem(ItemStack otherStack) {
			return true;
		}

		@Override
		public boolean isNoExport() {
			return false;
		}
	}
}
