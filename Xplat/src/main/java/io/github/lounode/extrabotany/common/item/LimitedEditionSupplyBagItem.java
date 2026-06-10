package io.github.lounode.extrabotany.common.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import io.github.lounode.extrabotany.common.world.LimitedEditionSupplyBagData;

public class LimitedEditionSupplyBagItem extends Item {
	public LimitedEditionSupplyBagItem(Properties properties) {
		super(properties);
	}

	@Override
	public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand usedHand) {
		ItemStack stack = player.getItemInHand(usedHand);

		if (level.isClientSide()) {
			return InteractionResultHolder.success(stack);
		}

		ItemStack reward = LimitedEditionSupplyBagData.get((ServerLevel) level).draw(player.getUUID(), player.getRandom()).copy();
		if (!player.isCreative()) {
			stack.shrink(1);
		}

		player.spawnAtLocation(reward).setNoPickUpDelay();
		level.playSound(null, player.getX(), player.getY(), player.getZ(), ExtraBotanySounds.REWARD_BAG_OPEN, SoundSource.PLAYERS, 0.8F, 1.0F);
		return InteractionResultHolder.success(stack);
	}
}
