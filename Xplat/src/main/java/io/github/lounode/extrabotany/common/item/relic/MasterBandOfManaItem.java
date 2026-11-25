package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaBarTooltip;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.CustomCreativeTabContents;
import vazkii.botania.common.item.relic.RelicBaubleItem;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.common.advancements.ManaChargeTrigger;
import io.github.lounode.extrabotany.common.lib.LibAdvancementNames;

import java.util.List;
import java.util.Optional;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class MasterBandOfManaItem extends RelicBaubleItem implements CustomCreativeTabContents {
	protected static final long MAX_MANA = Long.MAX_VALUE;
	public static final long ADVANCEMENT_PHASE1_REQUIRE = Integer.MAX_VALUE;
	public static final long ADVANCEMENT_PHASE2_REQUIRE = MAX_MANA;

	private static final String TAG_MANA = "mana";

	public MasterBandOfManaItem(Properties properties) {
		super(properties);
	}

	@Override
	public void addToCreativeTab(Item me, CreativeModeTab.Output output) {
		output.accept(this);

		ItemStack full = new ItemStack(this);
		setMana(full, MAX_MANA);
		output.accept(full);
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		return Optional.of(new ManaBarTooltip(getFractionForDisplay(stack)));
	}

	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, world, tooltip, flags);
		ExtendManaItemImpl manaItem = getManaItem(stack);
		if (manaItem == null) {
			return;
		}
		tooltip.add(Component.literal(""));
		String manaString = manaItem.getRealMana() + "/" + manaItem.getRealMaxMana();
		tooltip.add(Component.translatable("message.extrabotany.actionbar.mana_left", manaString).withStyle(ChatFormatting.GRAY));
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean held) {
		super.inventoryTick(stack, world, entity, slot, held);
		if (!world.isClientSide && entity instanceof ServerPlayer player) {
			var relic = XplatAbstractions.INSTANCE.findRelic(stack);
			if (relic != null && relic.isRightPlayer(player) && player.tickCount % 10 == 0) {
				ExtendManaItemImpl manaItem = getManaItem(stack);
				if (manaItem != null) {
					ManaChargeTrigger.INSTANCE.trigger(player, stack, manaItem.getRealMana());
				}
			}
		}
	}

	protected static void setMana(ItemStack stack, long mana) {
		if (mana > 0) {
			ItemNBTHelper.setLong(stack, TAG_MANA, mana);
		} else {
			ItemNBTHelper.removeEntry(stack, TAG_MANA);
		}
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, prefix(LibAdvancementNames.PANDA_DO_NOT_WEAR_RINGS));
	}

	public static class ExtendManaItemImpl implements ManaItem {
		protected final ItemStack stack;

		public ExtendManaItemImpl(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public int getMana() {
			//We let mana always below INT_MAX to make charge success
			long mana = getRealMana();
			int maxCharge = 100_000_000;
			if (mana > Integer.MAX_VALUE - maxCharge) {
				if (mana < MAX_MANA - maxCharge) {
					return Integer.MAX_VALUE - maxCharge;
				}
				return Integer.MAX_VALUE - (int) ((getRealMaxMana() - mana));
			}
			return (int) mana;
		}

		public long getRealMana() {
			return ItemNBTHelper.getLong(stack, TAG_MANA, 0);
		}

		@Override
		public int getMaxMana() {
			return Integer.MAX_VALUE;
		}

		public long getRealMaxMana() {
			return MAX_MANA;
		}

		@Override
		public void addMana(int mana) {
			long realMax = getRealMaxMana();
			long current = Math.min(realMax, Math.max(0L, getRealMana()));
			long updated;

			if (mana > 0) {
				long maxAdd = realMax - current;
				updated = (mana >= maxAdd) ? realMax : (current + mana);
			} else if (mana < 0) {
				long sub = -(long) mana;
				updated = (sub >= current) ? 0L : (current - sub);
			} else {
				return;
			}

			setMana(stack, updated);
		}

		@Override
		public boolean canReceiveManaFromPool(BlockEntity pool) {
			return true;
		}

		@Override
		public boolean canReceiveManaFromItem(ItemStack otherStack) {
			return true;
		}

		@Override
		public boolean canExportManaToPool(BlockEntity pool) {
			return true;
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

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return false;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round(13 * getFractionForDisplay(stack));
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return Mth.hsvToRgb(getFractionForDisplay(stack) / 3.0F, 1.0F, 1.0F);
	}

	@Nullable
	private ExtendManaItemImpl getManaItem(ItemStack stack) {
		if (XplatAbstractions.INSTANCE.findManaItem(stack) instanceof ExtendManaItemImpl impl) {
			return impl;
		}
		return null;
	}

	private float getFractionForDisplay(ItemStack stack) {
		ExtendManaItemImpl manaItem = getManaItem(stack);
		if (manaItem == null || manaItem.getRealMaxMana() <= 0) {
			return 0F;
		}
		return (float) manaItem.getRealMana() / (float) manaItem.getRealMaxMana();
	}

}
