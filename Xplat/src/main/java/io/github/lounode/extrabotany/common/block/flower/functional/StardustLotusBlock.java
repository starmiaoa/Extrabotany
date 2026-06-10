package io.github.lounode.extrabotany.common.block.flower.functional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.common.block.flower.SpecialFlowerBlock;

import io.github.lounode.extrabotany.common.item.equipment.tool.BinderItem;

import java.util.function.Supplier;

public class StardustLotusBlock extends SpecialFlowerBlock {
	public StardustLotusBlock(Holder<MobEffect> effect, int effectDuration, Properties properties,
			Supplier<BlockEntityType<? extends SpecialFlowerBlockEntity>> blockEntityType) {
		super(effect, effectDuration, properties, blockEntityType);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		InteractionResult result = bindTarget(stack, level, pos, player);
		return result.consumesAction()
				? ItemInteractionResult.sidedSuccess(level.isClientSide())
				: ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	static InteractionResult bindTarget(ItemStack stack, Level level, BlockPos pos, Player player) {
		if (!(stack.getItem() instanceof BinderItem)) {
			return InteractionResult.PASS;
		}

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (!(blockEntity instanceof StardustLotusBlockEntity lotus)) {
			return InteractionResult.PASS;
		}

		ResourceLocation boundDimension = ResourceLocation.tryParse(BinderItem.getDimension(stack));
		if (boundDimension == null || !boundDimension.equals(level.dimension().location()) || BinderItem.getPosY(stack) == -1) {
			if (!level.isClientSide()) {
				player.displayClientMessage(Component.translatable("message.extrabotany.stardust_lotus.invalid_bind"), true);
			}
			return InteractionResult.sidedSuccess(level.isClientSide());
		}

		BlockPos target = new BlockPos(BinderItem.getPosX(stack), BinderItem.getPosY(stack), BinderItem.getPosZ(stack));
		if (!level.isClientSide()) {
			lotus.setTarget(target);
			player.displayClientMessage(Component.translatable("message.extrabotany.stardust_lotus.bind_to_pos",
					target.getX(), target.getY(), target.getZ()), true);
		}
		return InteractionResult.sidedSuccess(level.isClientSide());
	}
}
