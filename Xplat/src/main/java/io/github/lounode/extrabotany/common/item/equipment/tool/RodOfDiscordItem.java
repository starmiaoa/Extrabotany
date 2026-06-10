package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.api.mana.ManaItemHandler;

public class RodOfDiscordItem extends Item {
	private static final int MANA_PER_USE = 2000;
	private static final double RANGE = 64D;
	private static final int COOLDOWN_DAMAGE = 80;
	private static final int NAUSEA_TICKS = 100;

	public RodOfDiscordItem(Properties properties) {
		super(properties.durability(81));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		HitResult hit = player.pick(RANGE, 1F, false);
		if (!(hit instanceof BlockHitResult blockHit) || blockHit.getType() == HitResult.Type.MISS) {
			return InteractionResultHolder.pass(stack);
		}
		if (!level.isClientSide()) {
			if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_USE, true)) {
				return InteractionResultHolder.fail(stack);
			}
			Vec3 end = blockHit.getLocation();
			player.teleportTo(end.x(), end.y() + 1D, end.z());
			level.playSound(null, player.getX(), player.getY(), player.getZ(),
					SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1F, 3F);
			player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, NAUSEA_TICKS));
			if (stack.getDamageValue() > 0) {
				player.setHealth(Math.max(1F, player.getHealth() - player.getMaxHealth() / 6F));
			}
			stack.setDamageValue(COOLDOWN_DAMAGE);
		}
		return InteractionResultHolder.sidedSuccess(stack, true);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (!level.isClientSide() && stack.getDamageValue() > 0) {
			stack.setDamageValue(stack.getDamageValue() - 1);
		}
	}
}
