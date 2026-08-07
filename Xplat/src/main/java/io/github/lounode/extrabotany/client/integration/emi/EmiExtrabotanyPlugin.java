package io.github.lounode.extrabotany.client.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiCraftingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.apache.commons.lang3.StringUtils;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.client.integration.emi.BotaniaEmiPlugin;
import vazkii.botania.common.brew.BotaniaBrews;
import vazkii.botania.common.item.BotaniaItems;

import io.github.lounode.extrabotany.api.recipe.ManaOutputRecipe;
import io.github.lounode.extrabotany.client.integration.emi.recipe.PedestalEmiRecipe;
import io.github.lounode.extrabotany.client.integration.emi.recipe.StonesiaEmiRecipe;
import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.brew.BrewUtil;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyBrews;
import io.github.lounode.extrabotany.common.crafting.ExtraBotanyRecipeTypes;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.brew.ManaCocktailItem;

import java.util.Comparator;
import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

@EmiEntrypoint
public class EmiExtrabotanyPlugin implements EmiPlugin {

	private static final Comparator<EmiRecipe> BY_ID = Comparator.comparing(
			EmiRecipe::getId,
			Comparator.nullsLast(Comparator.naturalOrder())
	);
	private static final Comparator<EmiRecipe> BY_GROUP =
			Comparator.comparing(emiRecipe -> emiRecipe instanceof ExtrabotanyEmiRecipe ber ? ber.getGroup() : "");
	private static final Comparator<EmiRecipe> BY_CATALYST =
			Comparator.comparing(emiRecipe -> emiRecipe.getCatalysts()
					.stream()
					.flatMap(emiIngredient -> emiIngredient.getEmiStacks().stream())
					.map(emiStack -> emiStack.getId().toString())
					.filter(StringUtils::isNotEmpty)
					.findFirst()
					.orElse(""));

	private static final Comparator<EmiRecipe> BY_SMASH_TOOLS = (l, r) -> {
		if (l instanceof PedestalEmiRecipe lp && r instanceof PedestalEmiRecipe rp) {
			return lp.getSmashToolsKey().compareTo(rp.getSmashToolsKey());
		}

		return 0;
	};

	private static final Comparator<EmiRecipe> BY_MANA_OUTPUT = Comparator.<EmiRecipe, Integer>comparing(
			emiRecipe -> emiRecipe instanceof ManaOutputRecipe recipe ? recipe.getManaOutput() : 0).reversed();

	//Category
	public static final EmiRecipeCategory PEDESTAL_SMASH = createCategory("pedestal_smash",
			EmiStack.of(ExtraBotanyBlocks.livingrockPedestal), BY_SMASH_TOOLS.thenComparing(BY_GROUP).thenComparing(BY_ID));
	public static final EmiRecipeCategory STONESIA = createCategory("stonesia",
			EmiStack.of(ExtrabotanyFlowerBlocks.stonesia), BY_MANA_OUTPUT.thenComparing(BY_GROUP).thenComparing(BY_ID));

	@Override
	public void register(EmiRegistry registry) {
		registry.addCategory(PEDESTAL_SMASH);
		registry.addCategory(STONESIA);

		for (var pedestal : ExtraBotanyBlocks.ALL_PEDESTALS) {
			registry.addWorkstation(PEDESTAL_SMASH, EmiStack.of(pedestal));
		}

		registry.addWorkstation(STONESIA, EmiStack.of(ExtrabotanyFlowerBlocks.stonesia));
		registry.addWorkstation(STONESIA, EmiStack.of(ExtrabotanyFlowerBlocks.stonesiaFloating));

		// Catalysts that belong to Botania / vanilla categories (mirror JEI registerRecipeCatalysts)
		registry.addWorkstation(BotaniaEmiPlugin.PURE_DAISY, EmiStack.of(ExtraBotanyItems.pureDaisyPendant));
		registry.addWorkstation(BotaniaEmiPlugin.MANA_INFUSION, EmiStack.of(ExtraBotanyItems.lensMana));
		registry.addWorkstation(VanillaEmiRecipeCategories.SMELTING, EmiStack.of(ExtraBotanyItems.lensSmelt));

		registry.setDefaultComparison(ExtraBotanyItems.manaCocktail, Comparison.compareComponents());
		registry.setDefaultComparison(ExtraBotanyItems.infiniteWine, Comparison.compareComponents());
		registry.setDefaultComparison(ExtraBotanyItems.holyWaterGrenade, Comparison.compareComponents());
		registry.setDefaultComparison(ExtraBotanyItems.coreOfTheVoid, Comparison.compareComponents());

		//Add Recipe
		for (var recipe : registry.getRecipeManager().getAllRecipesFor(ExtraBotanyRecipeTypes.PEDESTAL_SMASH_TYPE)) {
			registry.addRecipe(new PedestalEmiRecipe(recipe));
		}

		for (var recipe : registry.getRecipeManager().getAllRecipesFor(ExtraBotanyRecipeTypes.STONESIA_RECIPE_TYPE)) {
			if (!recipe.value().getInput().getDisplayed().isEmpty()) {
				registry.addRecipe(new StonesiaEmiRecipe(recipe));
			}
		}

		registerBrewCraftingRecipes(registry);

		// Hide the internal Das Rheingold re-bind recipe (mirror JEI onRuntimeAvailable)
		registry.removeRecipes(prefix("das_rheingold_change_bind"));
	}

	private void registerBrewCraftingRecipes(EmiRegistry registry) {
		for (var brew : BotaniaAPI.instance().getBrewRegistry()) {
			if (brew == BotaniaBrews.FALLBACK) {
				continue;
			}

			ItemStack flask = BotaniaItems.BREW_FLASK.getDefaultInstance();
			BrewUtil.setBrew(flask, brew);

			// mana cocktail: combine a mana drink with this brew (skip the cocktail brew itself)
			if (brew != ExtraBotanyBrews.manaCocktail) {
				ItemStack cocktailResult = ExtraBotanyItems.manaCocktail.getDefaultInstance();
				BrewUtil.setBrew(cocktailResult, brew);
				registry.addRecipe(new EmiCraftingRecipe(
						List.of(EmiStack.of(ExtraBotanyItems.manaDrink), EmiStack.of(flask)),
						EmiStack.of(cocktailResult),
						syntheticBrewRecipeId("mana_cocktail_change_brew", brew),
						true));
			}

			ItemStack cocktail = ExtraBotanyItems.manaCocktail.getDefaultInstance();
			BrewUtil.setBrew(cocktail, brew);
			if (brew == ExtraBotanyBrews.manaCocktail) {
				cocktail = ManaCocktailItem.getDefaultCocktail();
			}

			ItemStack wine = ExtraBotanyItems.infiniteWine.getDefaultInstance();
			BrewUtil.setBrew(wine, brew);
			registry.addRecipe(new EmiCraftingRecipe(
					List.of(EmiStack.of(cocktail), EmiStack.of(ExtraBotanyItems.heroMedal.getDefaultInstance())),
					EmiStack.of(wine),
					syntheticBrewRecipeId("infinite_wine", brew),
					true));

			ItemStack grenade = ExtraBotanyItems.holyWaterGrenade.getDefaultInstance();
			BrewUtil.setBrew(grenade, brew);
			registry.addRecipe(new EmiCraftingRecipe(
					List.of(EmiStack.of(cocktail), EmiStack.of(Items.POPPED_CHORUS_FRUIT.getDefaultInstance())),
					EmiStack.of(grenade),
					syntheticBrewRecipeId("holy_water_grenade", brew),
					true));
		}
	}

	private static ResourceLocation syntheticBrewRecipeId(String prefixPath, Brew brew) {
		ResourceLocation brewId = BotaniaAPI.instance().getBrewRegistry().getKey(brew);
		return prefix(prefixPath + "/" + brewId.getNamespace() + "/" + brewId.getPath());
	}

	private static EmiRecipeCategory createCategory(String idPath, EmiRenderable icon, Comparator<EmiRecipe> comp) {
		return new EmiRecipeCategory(prefix(idPath), icon, icon, comp);
	}
}
