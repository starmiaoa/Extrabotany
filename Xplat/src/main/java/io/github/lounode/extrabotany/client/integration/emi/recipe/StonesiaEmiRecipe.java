package io.github.lounode.extrabotany.client.integration.emi.recipe;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;

import net.minecraft.network.chat.Component;

import io.github.lounode.extrabotany.api.recipe.ManaOutputRecipe;
import io.github.lounode.extrabotany.api.recipe.StonesiaRecipe;
import io.github.lounode.extrabotany.client.integration.emi.EmiExtrabotanyPlugin;
import io.github.lounode.extrabotany.client.integration.emi.ExtrabotanyEmiRecipe;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;

import java.util.List;

public class StonesiaEmiRecipe extends ExtrabotanyEmiRecipe implements ManaOutputRecipe {

	private static final EmiStack STONESIA = EmiStack.of(ExtrabotanyFlowerBlocks.stonesia);
	private final int manaOutput;

	public StonesiaEmiRecipe(StonesiaRecipe recipe) {
		super(EmiExtrabotanyPlugin.STONESIA, recipe);

		this.input = List.of(EmiIngredient.of(
				recipe.getInput().getDisplayedStacks().stream().map(EmiStack::of).toList()));
		this.output = List.of();
		this.manaOutput = recipe.getManaOutput();
	}

	@Override
	public int getManaOutput() {
		return manaOutput;
	}

	@Override
	public int getDisplayWidth() {
		return 96;
	}

	@Override
	public int getDisplayHeight() {
		return 44;
	}

	@Override
	public boolean supportsRecipeTree() {
		return false;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addSlot(input.get(0), 9, 12);
		widgets.addSlot(STONESIA, 40, 12).catalyst(true);

		Component manaText = Component.translatable("extrabotany.jei.stonesia.tip_mana_output", manaOutput);
		widgets.add(new TextWidget(manaText.getVisualOrderText(), 62, 16, 0x808080, false));
	}
}
