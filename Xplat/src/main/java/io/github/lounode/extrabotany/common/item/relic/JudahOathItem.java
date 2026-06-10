package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.relic.RelicImpl;

import io.github.lounode.extrabotany.common.entity.JudahOathEntity;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import java.util.List;

public class JudahOathItem extends SwordItem {
	private static final int MANA_PER_USE = 2500;

	private final Variant variant;

	public JudahOathItem(Variant variant, Properties properties) {
		super(Tiers.DIAMOND, properties.attributes(SwordItem.createAttributes(Tiers.DIAMOND, 12, -3.6F)));
		this.variant = variant;
	}

	public Variant getVariant() {
		return this.variant;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide()) {
			return InteractionResultHolder.sidedSuccess(stack, true);
		}

		Relic relic = EXplatAbstractions.INSTANCE.findRelic(stack);
		if (relic != null) {
			relic.tickBinding(player);
		}
		if (relic == null || !relic.isRightPlayer(player) || !ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_USE, true)) {
			return InteractionResultHolder.fail(stack);
		}

		player.getCooldowns().addCooldown(this, 80);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.8F, 0.8F);
		JudahOathEntity judah = new JudahOathEntity(level, player, this.variant);
		judah.setPos(player.getX(), player.getY() + 1D, player.getZ());
		judah.setYRot(player.getYRot());
		judah.shootFromRotation(player, player.getXRot(), player.getYRot(), 0F, 0.5F, 0F);
		level.addFreshEntity(judah);

		return InteractionResultHolder.sidedSuccess(stack, false);
	}

	private boolean isRightPlayer(Player player, ItemStack stack) {
		Relic relic = EXplatAbstractions.INSTANCE.findRelic(stack);
		return relic != null && relic.isRightPlayer(player);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		if (!level.isClientSide && entity instanceof Player player) {
			Relic relic = EXplatAbstractions.INSTANCE.findRelic(stack);
			if (relic != null) {
				relic.tickBinding(player);
			}
		}
		super.inventoryTick(stack, level, entity, slot, selected);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		tooltip.add(Component.translatable("tooltip.extrabotany.judah_oath").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.empty());
		RelicImpl.addDefaultTooltip(stack, tooltip);
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, null);
	}

	public enum Variant {
		JUDAH(0, 0.85F, 0.6F, 0.02F),
		KIRA(1, 0.01F, 0.6F, 0.75F),
		SAKURA(2, 1F, 0.8F, 0.8F);

		private final int id;
		private final float red;
		private final float green;
		private final float blue;

		Variant(int id, float red, float green, float blue) {
			this.id = id;
			this.red = red;
			this.green = green;
			this.blue = blue;
		}

		public int id() {
			return this.id;
		}

		public float red() {
			return this.red;
		}

		public float green() {
			return this.green;
		}

		public float blue() {
			return this.blue;
		}

		public static Variant byId(int id) {
			for (Variant variant : values()) {
				if (variant.id == id) {
					return variant;
				}
			}
			return JUDAH;
		}
	}
}
