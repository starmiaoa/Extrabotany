package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.api.recipe.PureDaisyRecipe;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import io.github.lounode.extrabotany.common.event.entity.player.PlayerInteractEventWrapper;
import io.github.lounode.extrabotany.common.util.PlayerUtil;
import io.github.lounode.extrabotany.common.util.ItemStackDataHelper;

public class PureDaisyPendantItem extends BaubleItem {

	public static int TRANSFORM_MANA = 50;
	private static final String TAG_USE_COUNT = "usecount";
	private static final String TAG_COOLDOWN = "cooldown";
	private static final int USES_PER_COOLDOWN = 64;
	private static final int COOLDOWN_TICKS = 3000;
	private static final int DAYTIME_HEAL_INTERVAL = 120;

	public PureDaisyPendantItem(Properties props) {
		super(props);
	}

	public static void onPlayerTick(Player player) {
		ItemStack stack = EquipmentHandler.findOrEmpty(s -> s.getItem() instanceof PureDaisyPendantItem, player);
		if (stack.isEmpty()) {
			return;
		}

		int cooldown = getCooldown(stack);
		if (cooldown > 0) {
			setCooldown(stack, cooldown - 1);
		}

		if (!player.level().isClientSide && player.level().isDay() && player.level().getDayTime() % DAYTIME_HEAL_INTERVAL == 0) {
			player.heal(1F);
		}
	}

	public static class EventHandler {

		public static void onPlayerInteract(PlayerInteractEventWrapper.RightClickBlock event) {
			if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
				return;
			}
			if (!serverPlayer.getMainHandItem().isEmpty()) {
				return;
			}
			BlockPos pos = event.getPos();
			Level level = event.getLevel();
			BlockState state = level.getBlockState(pos);
			ItemStack stack = EquipmentHandler.findOrEmpty(s -> s.getItem() instanceof PureDaisyPendantItem, serverPlayer);
			if (stack.isEmpty()) {
				return;
			}
			if (getCooldown(stack) > 0) {
				return;
			}
			BlockState output = getDaisyRecipe(level, state);
			if (output == null) {
				return;
			}

			if (output.is(state.getBlock())) {
				return;
			}
			if (!ManaItemHandler.instance().requestManaExactForTool(stack, serverPlayer, TRANSFORM_MANA, true)) {
				return;
			}
			if (PlayerUtil.tryBreakBlock(serverPlayer, ItemStack.EMPTY, level, pos)) {
				level.setBlockAndUpdate(pos, output.getBlock().withPropertiesOf(state));
				level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(output));
				level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
				incrementUseCount(stack);
			}
		}

		@Nullable
		private static BlockState getDaisyRecipe(Level level, BlockState input) {
			for (RecipeHolder<PureDaisyRecipe> holder : level.getRecipeManager().getAllRecipesFor(BotaniaRecipeTypes.PURE_DAISY_TYPE)) {
				if (!(holder.value() instanceof PureDaisyRecipe recipe)) {
					continue;
				}
				if (recipe.getInput().test(input)) {
					return recipe.getOutput().pick(level.getRandom());
				}
			}
			return null;
		}
	}

	private static void incrementUseCount(ItemStack stack) {
		int count = getUseCount(stack) + 1;
		setUseCount(stack, count);
		if (count % USES_PER_COOLDOWN == 0) {
			setCooldown(stack, COOLDOWN_TICKS);
		}
	}

	public static int getUseCount(ItemStack stack) {
		return ItemStackDataHelper.getInt(stack, TAG_USE_COUNT, 0);
	}

	public static void setUseCount(ItemStack stack, int count) {
		ItemStackDataHelper.setInt(stack, TAG_USE_COUNT, count);
	}

	public static int getCooldown(ItemStack stack) {
		return ItemStackDataHelper.getInt(stack, TAG_COOLDOWN, 0);
	}

	public static void setCooldown(ItemStack stack, int cooldown) {
		if (cooldown <= 0) {
			ItemStackDataHelper.removeEntry(stack, TAG_COOLDOWN);
		} else {
			ItemStackDataHelper.setInt(stack, TAG_COOLDOWN, cooldown);
		}
	}
}
