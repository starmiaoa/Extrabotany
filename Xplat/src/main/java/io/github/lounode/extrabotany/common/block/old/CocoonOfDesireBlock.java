package io.github.lounode.extrabotany.common.block.old;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.BotaniaWaterloggedBlock;

import io.github.lounode.extrabotany.common.block.block_entity.CocoonOfDesireBlockEntity;
import io.github.lounode.extrabotany.common.block.block_entity.ExtraBotanyBlockEntities;

public class CocoonOfDesireBlock extends BotaniaWaterloggedBlock implements EntityBlock {
	private static final VoxelShape SHAPE = box(3.0D, 0.0D, 3.0D, 13.0D, 14.0D, 13.0D);

	public CocoonOfDesireBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return SHAPE;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof CocoonOfDesireBlockEntity cocoon)) {
			return InteractionResult.PASS;
		}

		return extractStoredItem(level, pos, player, cocoon);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof CocoonOfDesireBlockEntity cocoon)) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}

		if (!cocoon.getItem().isEmpty()) {
			return extractStoredItem(level, pos, player, cocoon).consumesAction()
					? ItemInteractionResult.sidedSuccess(level.isClientSide())
					: ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}

		if (stack.isEmpty()) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}

		if (!level.isClientSide()) {
			cocoon.setItem(stack.split(1));
			playPickupSound(level, pos);
		}
		return ItemInteractionResult.sidedSuccess(level.isClientSide());
	}

	private static InteractionResult extractStoredItem(Level level, BlockPos pos, Player player, CocoonOfDesireBlockEntity cocoon) {
		ItemStack stored = cocoon.getItem();
		if (stored.isEmpty()) {
			return InteractionResult.PASS;
		}

		if (!level.isClientSide()) {
			ItemStack extracted = stored.copy();
			cocoon.clearItem();
			if (!player.getInventory().add(extracted)) {
				Containers.dropItemStack(level, player.getX(), player.getY(), player.getZ(), extracted);
			}
			playPickupSound(level, pos);
		}
		return InteractionResult.sidedSuccess(level.isClientSide());
	}

	private static void playPickupSound(Level level, BlockPos pos) {
		level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5F,
				(level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new CocoonOfDesireBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) {
			return null;
		}
		return createTickerHelper(type, ExtraBotanyBlockEntities.COCOON_OF_DESIRE, CocoonOfDesireBlockEntity::serverTick);
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			if (level.getBlockEntity(pos) instanceof CocoonOfDesireBlockEntity cocoon && !cocoon.getItem().isEmpty()) {
				Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), cocoon.getItem().copy());
				cocoon.clearItem();
			}
			super.onRemove(state, level, pos, newState, isMoving);
		}
	}
}
