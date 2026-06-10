package io.github.lounode.extrabotany.common.block.flower.functional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.common.block.flower.FloatingSpecialFlowerBlock;

import java.util.function.Supplier;

public class FloatingStardustLotusBlock extends FloatingSpecialFlowerBlock {
	public FloatingStardustLotusBlock(Properties properties,
			Supplier<BlockEntityType<? extends SpecialFlowerBlockEntity>> blockEntityType) {
		super(properties, blockEntityType);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		InteractionResult result = StardustLotusBlock.bindTarget(stack, level, pos, player);
		return result.consumesAction()
				? ItemInteractionResult.sidedSuccess(level.isClientSide())
				: ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}
}
