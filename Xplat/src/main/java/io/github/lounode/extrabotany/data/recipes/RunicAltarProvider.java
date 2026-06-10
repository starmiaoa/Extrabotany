package io.github.lounode.extrabotany.data.recipes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.mana.ManaPoolBlock;
import vazkii.botania.common.crafting.RunicAltarRecipe;
import vazkii.botania.common.item.BotaniaItems;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class RunicAltarProvider extends ExtraBotanyRecipeProvider {

	public RunicAltarProvider(PackOutput packOutput) {
		super(packOutput);
	}

	@Override
	public String getName() {
		return "ExtraBotany runic altar recipes";
	}

	@Override
	protected void buildRecipes(RecipeOutput recipeOutput) {
		//Zadkiel
		save(recipeOutput, idFor("zadkiel"), new ItemStack(ExtraBotanyItems.zadkiel), 500000,
				Ingredient.of(Items.ICE),
				Ingredient.of(Items.BLUE_ICE),
				Ingredient.of(Items.PACKED_ICE),
				Ingredient.of(Items.SNOW_BLOCK),
				Ingredient.of(Items.POWDER_SNOW_BUCKET),
				Ingredient.of(Items.TOTEM_OF_UNDYING)
		);
		save(recipeOutput, idFor("orichalcos_ingot"), new ItemStack(ExtraBotanyItems.orichalcos), 150000,
				Ingredient.of(ExtraBotanyItems.heroMedal),
				Ingredient.of(ExtraBotanyItems.gildedPotatoMashed),
				Ingredient.of(BotaniaItems.gaiaIngot),
				Ingredient.of(BotaniaItems.gaiaIngot),
				Ingredient.of(BotaniaItems.lifeEssence),
				Ingredient.of(BotaniaItems.lifeEssence),
				Ingredient.of(BotaniaItems.lifeEssence),
				Ingredient.of(BotaniaItems.lifeEssence)
		);
		save(recipeOutput, idFor("shadowium_ingot"), new ItemStack(ExtraBotanyItems.shadowium), 4200,
				Ingredient.of(BotaniaItems.elementium),
				Ingredient.of(ExtraBotanyItems.gildedPotatoMashed),
				Ingredient.of(ExtraBotanyItems.nightmareFuel),
				Ingredient.of(ExtraBotanyItems.nightmareFuel),
				Ingredient.of(ExtraBotanyItems.nightmareFuel)
		);
		save(recipeOutput, idFor("photonium_ingot"), new ItemStack(ExtraBotanyItems.photonium), 4200,
				Ingredient.of(BotaniaItems.elementium),
				Ingredient.of(ExtraBotanyItems.gildedPotatoMashed),
				Ingredient.of(ExtraBotanyItems.spiritFragment),
				Ingredient.of(ExtraBotanyItems.spiritFragment),
				Ingredient.of(ExtraBotanyItems.spiritFragment)
		);
		save(recipeOutput, idFor("gilded_potato"), new ItemStack(ExtraBotanyItems.gildedPotato), 800,
				Ingredient.of(Items.POTATO),
				Ingredient.of(Items.GOLD_NUGGET)
		);
		save(recipeOutput, idFor(ExtraBotanyItems.orichalcosHammer), new ItemStack(ExtraBotanyItems.orichalcosHammer), ManaPoolBlock.MAX_MANA,
				Ingredient.of(ExtraBotanyItems.orichalcos),
				Ingredient.of(ExtraBotanyItems.gildedPotatoMashed),
				Ingredient.of(ExtraBotanyItems.theChaos),
				Ingredient.of(ExtraBotanyItems.theOrigin),
				Ingredient.of(ExtraBotanyItems.theEnd)
		);
		save(recipeOutput, idFor(ExtraBotanyItems.aeroStone), new ItemStack(ExtraBotanyItems.aeroStone), 2000,
				Ingredient.of(BotaniaItems.runeAir),
				Ingredient.of(BotaniaItems.runeAir),
				Ingredient.of(BotaniaItems.manaDiamond),
				Ingredient.of(Items.LAPIS_LAZULI),
				Ingredient.of(Items.QUARTZ)
		);
		save(recipeOutput, idFor(ExtraBotanyItems.aquaStone), new ItemStack(ExtraBotanyItems.aquaStone), 2000,
				Ingredient.of(BotaniaItems.runeWater),
				Ingredient.of(BotaniaItems.runeWater),
				Ingredient.of(BotaniaItems.manaDiamond),
				Ingredient.of(Items.LAPIS_LAZULI),
				Ingredient.of(Items.QUARTZ)
		);
		save(recipeOutput, idFor(ExtraBotanyItems.earthStone), new ItemStack(ExtraBotanyItems.earthStone), 2000,
				Ingredient.of(BotaniaItems.runeEarth),
				Ingredient.of(BotaniaItems.runeEarth),
				Ingredient.of(BotaniaItems.manaDiamond),
				Ingredient.of(Items.LAPIS_LAZULI),
				Ingredient.of(Items.QUARTZ)
		);
		save(recipeOutput, idFor(ExtraBotanyItems.ignisStone), new ItemStack(ExtraBotanyItems.ignisStone), 2000,
				Ingredient.of(BotaniaItems.runeFire),
				Ingredient.of(BotaniaItems.runeFire),
				Ingredient.of(BotaniaItems.manaDiamond),
				Ingredient.of(Items.LAPIS_LAZULI),
				Ingredient.of(Items.QUARTZ)
		);
		save(recipeOutput, idFor(ExtraBotanyItems.powerGlove), new ItemStack(ExtraBotanyItems.powerGlove), 2000,
				Ingredient.of(BotaniaItems.runeWrath),
				Ingredient.of(BotaniaItems.manaDiamond),
				Ingredient.of(BotaniaItems.manaweaveCloth),
				Ingredient.of(BotaniaItems.manaweaveCloth),
				Ingredient.of(BotaniaItems.manaweaveCloth)
		);
		save(recipeOutput, idFor(ExtraBotanyItems.deathRing), new ItemStack(ExtraBotanyItems.deathRing), 2000,
				Ingredient.of(BotaniaItems.runeEnvy),
				Ingredient.of(BotaniaItems.manaSteel),
				Ingredient.of(BotaniaItems.manaSteel),
				Ingredient.of(BotaniaItems.manaDiamond),
				Ingredient.of(Items.WITHER_SKELETON_SKULL)
		);
		save(recipeOutput, idFor(ExtraBotanyItems.frostStar), new ItemStack(ExtraBotanyItems.frostStar), 2000,
				Ingredient.of(BotaniaItems.runeMana),
				Ingredient.of(BotaniaItems.manaSteel),
				Ingredient.of(BotaniaItems.manaSteel),
				Ingredient.of(Items.ICE),
				Ingredient.of(Items.ICE)
		);
		save(recipeOutput, idFor(ExtraBotanyItems.theCommunity), new ItemStack(ExtraBotanyItems.theCommunity), 10000,
				Ingredient.of(ExtraBotanyItems.theChaos),
				Ingredient.of(ExtraBotanyItems.aeroStone),
				Ingredient.of(ExtraBotanyItems.aquaStone),
				Ingredient.of(ExtraBotanyItems.earthStone),
				Ingredient.of(ExtraBotanyItems.ignisStone)
		);
		save(recipeOutput, idFor(ExtraBotanyItems.sunRing), new ItemStack(ExtraBotanyItems.sunRing), 500000,
				Ingredient.of(ExtraBotanyItems.theEnd),
				Ingredient.of(ExtraBotanyItems.frostStar),
				Ingredient.of(ExtraBotanyItems.deathRing),
				Ingredient.of(BotaniaItems.auraRingGreater),
				Ingredient.of(BotaniaItems.miningRing),
				Ingredient.of(BotaniaItems.pixieRing),
				Ingredient.of(BotaniaItems.swapRing),
				Ingredient.of(BotaniaItems.waterRing),
				Ingredient.of(ExtraBotanyItems.dispersiveRing),
				Ingredient.of(BotaniaItems.reachRing)
		);
		save(recipeOutput, idFor(ExtraBotanyItems.moonPendant), new ItemStack(ExtraBotanyItems.moonPendant), 500000,
				Ingredient.of(ExtraBotanyItems.theOrigin),
				Ingredient.of(BotaniaItems.knockbackBelt),
				Ingredient.of(BotaniaItems.superCloudPendant),
				Ingredient.of(BotaniaItems.superLavaPendant),
				Ingredient.of(BotaniaItems.icePendant),
				Ingredient.of(BotaniaItems.itemFinder)
		);
		save(recipeOutput, idFor(ExtraBotanyItems.potatoChips), new ItemStack(ExtraBotanyItems.potatoChips), 50000,
				Ingredient.of(ExtraBotanyItems.gildedPotato),
				Ingredient.of(BotaniaItems.tinyPotatoMask),
				Ingredient.of(BotaniaBlocks.tinyPotato),
				Ingredient.of(BotaniaItems.runeMana),
				Ingredient.of(Items.TOTEM_OF_UNDYING),
				Ingredient.of(Items.TOTEM_OF_UNDYING),
				Ingredient.of(Items.TOTEM_OF_UNDYING),
				Ingredient.of(Items.CAMPFIRE),
				Ingredient.of(Items.BLAST_FURNACE)
		);

	}

	private static void save(RecipeOutput recipeOutput, ResourceLocation id, ItemStack output, int mana, Ingredient... inputs) {
		recipeOutput.accept(id, new RunicAltarRecipe(output, Ingredient.of(BotaniaBlocks.livingrock), mana, inputs, new Ingredient[0]), null);
	}

	private static ResourceLocation idFor(String s) {
		return prefix("runic_altar/" + s);
	}

	private static ResourceLocation idFor(ItemLike itemLike) {
		return prefix("runic_altar/" + BuiltInRegistries.ITEM.getKey(itemLike.asItem()).getPath());
	}
}
