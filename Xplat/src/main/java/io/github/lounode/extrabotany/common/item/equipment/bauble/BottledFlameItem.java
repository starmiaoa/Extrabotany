package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

import vazkii.botania.common.handler.EquipmentHandler;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

public class BottledFlameItem extends SimpleBaubleItem {
	private static final int LIGHT_THRESHOLD = 3;

	public BottledFlameItem(Properties props) {
		super(props);
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		if (!(entity instanceof ServerPlayer player) || player.tickCount % 10 != 0) {
			return;
		}

		Level level = player.level();
		BlockPos pos = player.blockPosition();
		if (level.getMaxLocalRawBrightness(pos) > LIGHT_THRESHOLD || !canPlaceTorch(level, pos, null)) {
			return;
		}

		int slot = findTorch(player);
		if (slot < 0) {
			return;
		}

		ItemStack torch = player.getInventory().getItem(slot);
		ItemStack offhand = player.getItemInHand(InteractionHand.OFF_HAND);
		player.setItemInHand(InteractionHand.OFF_HAND, torch);
		UseOnContext context = new UseOnContext(player, InteractionHand.OFF_HAND,
				new TorchPlacementHitResult(pos.getCenter(), Direction.UP, pos.below(), false));
		torch.useOn(context);
		player.setItemInHand(InteractionHand.OFF_HAND, offhand);
	}

	private boolean canPlaceTorch(Level level, BlockPos pos) {
		return canPlaceTorch(level, pos, null);
	}

	private boolean canPlaceTorch(Level level, BlockPos pos, BlockState torchState) {
		BlockState below = level.getBlockState(pos.below());
		BlockState here = level.getBlockState(pos);
		return here.isAir()
				&& !below.isAir()
				&& !(below.getBlock() instanceof LiquidBlock)
				&& !(here.getBlock() instanceof LiquidBlock)
				&& !below.liquid()
				&& !here.liquid()
				&& (torchState == null || torchState.canSurvive(level, pos));
	}

	private int findTorch(ServerPlayer player) {
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (isTorch(stack) && stack.getItem() instanceof BlockItem blockItem
					&& canPlaceTorch(player.level(), player.blockPosition(), blockItem.getBlock().defaultBlockState())) {
				return i;
			}
		}
		return -1;
	}

	private static boolean isTorch(ItemStack stack) {
		if (stack.isEmpty()) {
			return false;
		}
		Item item = stack.getItem();
		return item instanceof BlockItem && BuiltInRegistries.ITEM.getKey(item).getPath().contains("torch");
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return EquipmentHandler.findOrEmpty(ExtraBotanyItems.bottledFlame, entity).isEmpty();
	}
}
