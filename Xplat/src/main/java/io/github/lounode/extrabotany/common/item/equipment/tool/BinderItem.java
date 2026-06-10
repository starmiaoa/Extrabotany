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

import io.github.lounode.extrabotany.common.util.ItemStackDataHelper;

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
		ResourceLocation dimension = context.getLevel().dimension().location();
		ItemStackDataHelper.setInt(stack, TAG_X, pos.getX());
		ItemStackDataHelper.setInt(stack, TAG_Y, pos.getY());
		ItemStackDataHelper.setInt(stack, TAG_Z, pos.getZ());
		ItemStackDataHelper.setString(stack, TAG_DIM, dimension.toString());
		return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, context, tooltip, flags);
		tooltip.add(Component.translatable("item.extrabotany.binder.bindx", getPosX(stack)).withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("item.extrabotany.binder.bindy", getPosY(stack)).withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("item.extrabotany.binder.bindz", getPosZ(stack)).withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.translatable("item.extrabotany.binder.binddim", getDimension(stack)).withStyle(ChatFormatting.GRAY));
	}

	public static int getPosX(ItemStack stack) {
		return ItemStackDataHelper.getInt(stack, TAG_X, 0);
	}

	public static int getPosY(ItemStack stack) {
		return ItemStackDataHelper.getInt(stack, TAG_Y, -1);
	}

	public static int getPosZ(ItemStack stack) {
		return ItemStackDataHelper.getInt(stack, TAG_Z, 0);
	}

	public static String getDimension(ItemStack stack) {
		return ItemStackDataHelper.getString(stack, TAG_DIM, "minecraft:overworld");
	}
}
