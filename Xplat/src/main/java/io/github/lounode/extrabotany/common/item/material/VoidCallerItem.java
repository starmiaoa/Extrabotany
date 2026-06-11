package io.github.lounode.extrabotany.common.item.material;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import org.jetbrains.annotations.NotNull;

import io.github.lounode.extrabotany.common.entity.gaia.VoidHerrscher;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

public class VoidCallerItem extends Item {
	public VoidCallerItem(Properties properties) {
		super(properties);
	}

	@Override
	public @NotNull InteractionResult useOn(UseOnContext ctx) {
		ItemStack stack = ctx.getItemInHand();
		Player player = ctx.getPlayer();
		if (player == null || !stack.is(ExtraBotanyItems.voidCaller) || !player.isShiftKeyDown()) {
			return super.useOn(ctx);
		}

		if (VoidHerrscher.spawn(player, stack, ctx.getLevel(), ctx.getClickedPos())) {
			stack.shrink(player.isCreative() ? 0 : 1);
			return InteractionResult.sidedSuccess(ctx.getLevel().isClientSide());
		}
		return InteractionResult.CONSUME;
	}
}
