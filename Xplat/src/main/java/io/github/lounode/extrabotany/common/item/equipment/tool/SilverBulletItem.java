package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import vazkii.botania.api.mana.BasicLensItem;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.common.advancements.ManaBlasterTrigger;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.item.ManaBlasterItem;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

public class SilverBulletItem extends ManaBlasterItem {
	private static final int MAX_MANA = 240;
	private static final int TICKS_BEFORE_MANA_LOSS = 80;
	private static final float MANA_LOSS_PER_TICK = 3.0F;
	private static final float GRAVITY = 0.0F;
	private static final float MOTION_MODIFIER = 7.5F;
	private static final int COLOR = 0x87CEFA;
	private static final int COOLDOWN = 30;

	public SilverBulletItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player.isSecondaryUseActive() || !hasShootingGuardianSet(player)) {
			return super.use(level, player, hand);
		}

		if (player.getCooldowns().isOnCooldown(this)) {
			return InteractionResultHolder.pass(stack);
		}

		if (!level.isClientSide()) {
			ManaBurstEntity burst = getFreeBurst(player, stack, hand);
			level.playSound(null, player.getX(), player.getY(), player.getZ(), BotaniaSounds.manaBlaster, SoundSource.PLAYERS, 1.0F, 1.0F);
			level.addFreshEntity(burst);
			if (player instanceof ServerPlayer serverPlayer) {
				ManaBlasterTrigger.INSTANCE.trigger(serverPlayer, stack);
			}
		}

		player.getCooldowns().addCooldown(this, COOLDOWN);
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public BurstProperties getBurstProps(Player player, ItemStack stack, boolean request, InteractionHand hand) {
		BurstProperties props = new BurstProperties(MAX_MANA, TICKS_BEFORE_MANA_LOSS, MANA_LOSS_PER_TICK, GRAVITY, MOTION_MODIFIER, COLOR);
		ItemStack lens = getLens(stack);
		if (!lens.isEmpty() && lens.getItem() instanceof BasicLensItem lensItem) {
			lensItem.apply(lens, props, player.level());
		}
		return props;
	}

	private ManaBurstEntity getFreeBurst(Player player, ItemStack stack, InteractionHand hand) {
		ManaBurstEntity burst = new ManaBurstEntity(player);
		BurstProperties props = getBurstProps(player, stack, false, hand);
		burst.setSourceLens(getLens(stack));
		burst.setColor(props.color);
		burst.setMana(props.maxMana);
		burst.setStartingMana(props.maxMana);
		burst.setMinManaLoss(props.ticksBeforeManaLoss);
		burst.setManaLossPerTick(props.manaLossPerTick);
		burst.setGravity(props.gravity);
		burst.setDeltaMovement(burst.getDeltaMovement().scale(props.motionModifier));
		return burst;
	}

	private static boolean hasShootingGuardianSet(Player player) {
		return player.getItemBySlot(EquipmentSlot.HEAD).is(ExtraBotanyItems.shootingGuardianHelm)
				&& player.getItemBySlot(EquipmentSlot.CHEST).is(ExtraBotanyItems.shootingGuardianChest)
				&& player.getItemBySlot(EquipmentSlot.LEGS).is(ExtraBotanyItems.shootingGuardianLegs)
				&& player.getItemBySlot(EquipmentSlot.FEET).is(ExtraBotanyItems.shootingGuardianBoots);
	}
}
