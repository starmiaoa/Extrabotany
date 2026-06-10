package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import vazkii.botania.common.helper.ItemNBTHelper;

import io.github.lounode.extrabotany.common.block.flower.functional.StardustLotusBlockEntity;

import java.util.List;

public class BinderItem extends Item {
	private static final String TAG_X = "posx";
	private static final String TAG_Y = "posy";
	private static final String TAG_Z = "posz";
	private static final String TAG_DIM = "dim";

	public BinderItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getPlayer() == null || !context.getPlayer().isShiftKeyDown()) {
			return InteractionResult.PASS;
		}

		ItemStack stack = context.getItemInHand();
		BlockPos pos = context.getClickedPos();
		if (context.getLevel().getBlockEntity(pos) instanceof StardustLotusBlockEntity) {
			return StardustLotusBlockEntity.bindTarget(stack, context.getLevel(), pos, context.getPlayer());
		}

		ResourceLocation dimension = context.getLevel().dimension().location();
		ItemNBTHelper.setInt(stack, TAG_X, pos.getX());
		ItemNBTHelper.setInt(stack, TAG_Y, pos.getY());
		ItemNBTHelper.setInt(stack, TAG_Z, pos.getZ());
		ItemNBTHelper.setString(stack, TAG_DIM, dimension.toString());
		return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, level, tooltip, flags);
		tooltip.add(Component.translatable("item.extrabotany.binder.bindx", getPosX(stack)).withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("item.extrabotany.binder.bindy", getPosY(stack)).withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("item.extrabotany.binder.bindz", getPosZ(stack)).withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("item.extrabotany.binder.binddim", getDimension(stack)).withStyle(ChatFormatting.GRAY));
	}

	public static int getPosX(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_X, 0);
	}

	public static int getPosY(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_Y, -1);
	}

	public static int getPosZ(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_Z, 0);
	}

	public static String getDimension(ItemStack stack) {
		return ItemNBTHelper.getString(stack, TAG_DIM, "minecraft:overworld");
	}
}
