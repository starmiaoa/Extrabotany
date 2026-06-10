package io.github.lounode.extrabotany.common.item.equipment.bauble;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.chat.Component;

import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.relic.RelicBaubleItem;
import vazkii.botania.common.item.relic.RelicImpl;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.event.entity.living.LivingHealEventWrapper;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.util.FrostWalkerEffectHelper;
import io.github.lounode.extrabotany.common.util.ItemStackDataHelper;

import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class SilentEternityItem extends RelicBaubleItem {
	public static final int MAX_MANA = 666;
	private static final int FROST_RANGE = 4;
	private static final int STILL_TICKS_TO_HEAL = 15;
	private static final float HEAL_AMOUNT = 0.4F;
	private static final double MOVEMENT_EPSILON = 1.0E-4D;
	private static final ResourceLocation KNOCKBACK_RESISTANCE_ID = prefix("silent_eternity_knockback_resistance");

	private static final String TAG_MANA = "mana";
	private static final String TAG_X = "posx";
	private static final String TAG_Y = "posy";
	private static final String TAG_Z = "posz";
	private static final String TAG_STOP_TICKS = "stopticks";

	public SilentEternityItem(Properties props) {
		super(props);
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
			FrostWalkerEffectHelper.apply(player, stack, FROST_RANGE);
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
	public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack, ResourceLocation slotIdentifier) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		attributes.put(Attributes.KNOCKBACK_RESISTANCE,
				new AttributeModifier(KNOCKBACK_RESISTANCE_ID, 1.0D, AttributeModifier.Operation.ADD_VALUE));
		return attributes;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltip, flags);
		RelicImpl.addDefaultTooltip(stack, tooltip);
	}

	private static boolean isStill(ItemStack stack, Player player) {
		double dx = player.getX() - getDouble(stack, TAG_X, player.getX());
		double dy = player.getY() - getDouble(stack, TAG_Y, player.getY());
		double dz = player.getZ() - getDouble(stack, TAG_Z, player.getZ());
		return dx * dx + dy * dy + dz * dz <= MOVEMENT_EPSILON;
	}

	private static void setLastPosition(ItemStack stack, Player player) {
		setDouble(stack, TAG_X, player.getX());
		setDouble(stack, TAG_Y, player.getY());
		setDouble(stack, TAG_Z, player.getZ());
	}

	private static int getStopTicks(ItemStack stack) {
		return ItemStackDataHelper.getInt(stack, TAG_STOP_TICKS, 0);
	}

	private static void setStopTicks(ItemStack stack, int ticks) {
		ItemStackDataHelper.setInt(stack, TAG_STOP_TICKS, ticks);
	}

	private static void setMana(ItemStack stack, int mana) {
		if (mana > 0) {
			ItemStackDataHelper.setInt(stack, TAG_MANA, Math.min(mana, MAX_MANA));
		} else {
			ItemStackDataHelper.removeEntry(stack, TAG_MANA);
		}
	}

	private static int getMana(ItemStack stack) {
		return ItemStackDataHelper.getInt(stack, TAG_MANA, 0);
	}

	private static void setDouble(ItemStack stack, String key, double value) {
		ItemStackDataHelper.setLong(stack, key, Double.doubleToLongBits(value));
	}

	private static double getDouble(ItemStack stack, String key, double fallback) {
		long bits = ItemStackDataHelper.getLong(stack, key, Double.doubleToLongBits(fallback));
		return Double.longBitsToDouble(bits);
	}

	private static boolean isRightPlayer(ItemStack stack, Player player) {
		Relic relic = Relic.LOOKUP.find(stack);
		return relic != null && relic.isRightPlayer(player);
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, null);
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
		public boolean acceptDispatchedManaFromItem(ItemStack otherStack) {
			return false;
		}

		@Override
		public boolean refuseRequestedManaFromItem(ItemStack otherStack) {
			return true;
		}

		@Override
		public boolean canDrainManaToPool(BlockEntity pool) {
			return false;
		}

		@Override
		public boolean canSendRequestedManaToItem(ItemStack otherStack) {
			return true;
		}

		@Override
		public boolean isNoExport() {
			return false;
		}
	}

	public static class EventHandler {
		public static void onLivingHeal(LivingHealEventWrapper event) {
			if (!(event.getEntity() instanceof Player player)) {
				return;
			}

			ItemStack stack = EquipmentHandler.findOrEmpty(ExtraBotanyItems.silentEternity, player);
			if (!stack.isEmpty() && isRightPlayer(stack, player)) {
				event.setAmount(0);
			}
		}
	}
}
