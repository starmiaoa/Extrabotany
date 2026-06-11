package io.github.lounode.extrabotany.data.recipes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.flower.FloatingSpecialFlowerBlock;
import vazkii.botania.common.crafting.recipe.ManaUpgradeRecipe;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.common.lib.ConventionalBotaniaTags;
import vazkii.botania.common.lib.LibBlockNames;
import vazkii.botania.data.recipes.builder.CapturingRecipeOutput;
import vazkii.botania.common.crafting.recipe.WrappingRecipeSerializer;
import vazkii.botania.data.recipes.builder.WrapperRecipeBuilder;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.crafting.recipe.*;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.ExtraBotanyTags;
import io.github.lounode.extrabotany.common.lib.LibItemNames;
import io.github.lounode.extrabotany.common.lib.ResourceLocationHelper;

import java.util.Comparator;
import java.util.function.Function;

import static io.github.lounode.extrabotany.data.tags.BlockTagProvider.EXTRABOTANY_BLOCK;

public class CraftingRecipeProvider extends ExtraBotanyRecipeProvider {
	private static final TagKey<Item> C_INGOTS_ORICHALCOS = commonItemTag("ingots/orichalcos");
	private static final TagKey<Item> C_INGOTS_PHOTONIUM = commonItemTag("ingots/photonium");

	public CraftingRecipeProvider(PackOutput packOutput) {
		super(packOutput);
	}

	@Override
	public void buildRecipes(RecipeOutput consumer) {

		specialRecipe(consumer, PotionLensChangePotionRecipe.SERIALIZER, PotionLensChangePotionRecipe::new);
		specialRecipe(consumer, DasRheingoldChangeSoulBoundRecipe.SERIALIZER, DasRheingoldChangeSoulBoundRecipe::new);
		specialRecipe(consumer, WandOfTheForestExtendRecipe.SERIALIZER, WandOfTheForestExtendRecipe::new);

		registerMain(consumer);
		registerMisc(consumer);
		registerTools(consumer);
		registerArmors(consumer);
		registerTrinkets(consumer);
		registerLenses(consumer);
		registerConversions(consumer);
		registerDecor(consumer);
		registerFloatingFlowers(consumer);
	}

	private void registerMain(RecipeOutput consumer) {
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyItems.theChaos)
				.define('S', ExtraBotanyTags.Items.INGOTS_SHADOWIUM)
				.define('P', ExtraBotanyTags.Items.INGOTS_PHOTONIUM)
				.define('F', ExtraBotanyItems.spiritFragment)
				.pattern(" S ")
				.pattern("SFP")
				.pattern(" P ")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.nightmareFuel))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyItems.theOrigin)
				.define('S', ConventionalBotaniaTags.Items.TERRASTEEL_INGOTS)
				.define('P', ExtraBotanyTags.Items.INGOTS_AERIALITE)
				.define('F', ExtraBotanyItems.spiritFragment)
				.pattern(" S ")
				.pattern("SFP")
				.pattern(" P ")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.nightmareFuel))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyItems.theEnd)
				.define('S', ExtraBotanyTags.Items.INGOTS_ORICHALCOS)
				.define('P', BotaniaItems.gaiaIngot)
				.define('F', ExtraBotanyItems.spiritFragment)
				.pattern(" S ")
				.pattern("SFP")
				.pattern(" P ")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.nightmareFuel))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyItems.challengeTicket)
				.define('S', ExtraBotanyTags.Items.INGOTS_SHADOWIUM)
				.define('P', BotaniaItems.gaiaIngot)
				.define('F', ExtraBotanyTags.Items.INGOTS_PHOTONIUM)
				.define('A', BotaniaItems.lifeEssence)
				.pattern("ASA")
				.pattern("FPF")
				.pattern("ASA")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.gaiaIngot))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyItems.voidCaller, 3)
				.define('S', ExtraBotanyTags.Items.INGOTS_SHADOWIUM)
				.define('P', ExtraBotanyTags.Items.INGOTS_PHOTONIUM)
				.define('H', ExtraBotanyItems.heroMedal)
				.define('A', BotaniaItems.lifeEssence)
				.pattern("ASA")
				.pattern("PHP")
				.pattern("ASA")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.heroMedal))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyItems.dasRheingold)
				.define('S', BotaniaItems.manaweaveCloth)
				.define('P', Items.GOLD_INGOT)
				.define('A', BotaniaItems.lifeEssence)
				.pattern("ASA")
				.pattern("SPS")
				.pattern("ASA")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyItems.natureOrb)
				.define('S', ConventionalBotaniaTags.Items.DRAGONSTONE_GEMS)
				.define('P', BotaniaItems.manaPearl)
				.define('A', ConventionalBotaniaTags.Items.TERRASTEEL_INGOTS)
				.pattern("ASA")
				.pattern("SPS")
				.pattern("ASA")
				.unlockedBy("has_item", conditionsFromTag(ConventionalBotaniaTags.Items.TERRASTEEL_INGOTS))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyItems.universalPetal, 8)
				.define('P', BotaniaTags.Items.PETALS)
				.define('L', BotaniaItems.lifeEssence)
				.pattern("PPP")
				.pattern("PLP")
				.pattern("PPP")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.elementRune, 8)
				.requires(BotaniaItems.lifeEssence)
				.requires(BotaniaItems.runeAir)
				.requires(BotaniaItems.runeEarth)
				.requires(BotaniaItems.runeWater)
				.requires(BotaniaItems.runeFire)
				.requires(BotaniaItems.runeSpring)
				.requires(BotaniaItems.runeSummer)
				.requires(BotaniaItems.runeAutumn)
				.requires(BotaniaItems.runeWinter)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.sinRune, 8)
				.requires(BotaniaItems.lifeEssence)
				.requires(BotaniaItems.runeMana)
				.requires(BotaniaItems.runePride)
				.requires(BotaniaItems.runeGluttony)
				.requires(BotaniaItems.runeWrath)
				.requires(BotaniaItems.runeGreed)
				.requires(BotaniaItems.runeEnvy)
				.requires(BotaniaItems.runeLust)
				.requires(BotaniaItems.runeSloth)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(consumer);

	}

	private void registerTrinkets(RecipeOutput consumer) {
		wrap(ManaUpgradeRecipe.SERIALIZER, ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.manaRingMaster)
				.define('R', BotaniaItems.manaRingGreater)
				.define('O', ExtraBotanyTags.Items.INGOTS_ORICHALCOS)
				.define('H', ExtraBotanyItems.heroMedal)
				.define('Y', ExtraBotanyItems.theOrigin)
				.define('C', ExtraBotanyItems.theChaos)
				.define('E', ExtraBotanyItems.theEnd)
				.pattern("OHO")
				.pattern("YRE")
				.pattern("OCO")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaRingGreater))).save(consumer);
		// Core of God — faithful to ExtraMeteorP's old recipe (sunny quartz x8 + orichalcos + flügel tiara).
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.coreOfTheVoid)
				.define('Q', BotaniaBlocks.sunnyQuartz)
				.define('O', ExtraBotanyTags.Items.INGOTS_ORICHALCOS)
				.define('T', ExtraBotanyItems.emptyCoreOfTheVoid)
				.pattern("QOQ")
				.pattern("QTQ")
				.pattern("QQQ")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.emptyCoreOfTheVoid))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.camera)
				.define('B', ExtraBotanyTags.Items.INGOTS_SHADOWIUM)
				.define('G', BotaniaItems.gaiaIngot)
				.define('S', Items.SPYGLASS)
				.pattern("BBB")
				.pattern("BSB")
				.pattern("GGG")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.gaiaIngot))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.featherOfJingwei)
				.define('L', Items.LAVA_BUCKET)
				.define('F', Items.FEATHER)
				.define('H', ExtraBotanyItems.heroMedal)
				.define('B', Items.BLAZE_POWDER)
				.pattern("BLB")
				.pattern("BFB")
				.pattern("BHB")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.heroMedal))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.rodOfDiscord)
				.define('T', BotaniaItems.livingwoodTwig)
				.define('P', BotaniaItems.pixieDust)
				.define('C', ExtraBotanyItems.theChaos)
				.pattern(" PC")
				.pattern(" TP")
				.pattern("T  ")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.theChaos))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.cosmicCarKey)
				.define('M', ConventionalBotaniaTags.Items.MANASTEEL_INGOTS)
				.define('T', ExtraBotanyItems.theOrigin)
				.define('E', BotaniaItems.enderAirBottle)
				.define('P', Items.ENDER_EYE)
				.pattern("EPE")
				.pattern("MTM")
				.pattern("MMM")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.theOrigin))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.motor)
				.define('A', ExtraBotanyTags.Items.INGOTS_AERIALITE)
				.define('P', ExtraBotanyTags.Items.INGOTS_PHOTONIUM)
				.define('H', ExtraBotanyItems.heroMedal)
				.pattern("PAP")
				.pattern("PHP")
				.pattern("PAP")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.heroMedal))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ExtraBotanyItems.motorAccessory)
				.requires(ExtraBotanyItems.motor)
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.motor))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ExtraBotanyItems.motor)
				.requires(ExtraBotanyItems.motorAccessory)
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.motorAccessory))
				.save(consumer, prefix("motor_from_accessory"));
		ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ExtraBotanyItems.cosmicCarKeyAccessory)
				.requires(ExtraBotanyItems.cosmicCarKey)
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.cosmicCarKey))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ExtraBotanyItems.cosmicCarKey)
				.requires(ExtraBotanyItems.cosmicCarKeyAccessory)
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.cosmicCarKeyAccessory))
				.save(consumer, prefix("cosmic_car_key_from_accessory"));
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.shadowKatana)
				.define('W', BotaniaItems.livingwoodTwig)
				.define('S', ExtraBotanyTags.Items.INGOTS_SHADOWIUM)
				.pattern("S")
				.pattern("S")
				.pattern("W")
				.unlockedBy("has_item", conditionsFromTag(ExtraBotanyTags.Items.INGOTS_SHADOWIUM))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.photonShotgun)
				.define('P', ExtraBotanyTags.Items.INGOTS_PHOTONIUM)
				.define('M', BotaniaItems.manaGun)
				.define('D', ConventionalBotaniaTags.Items.MANA_DIAMOND_GEMS)
				.define('S', ConventionalBotaniaTags.Items.MANASTEEL_INGOTS)
				.pattern("PPD")
				.pattern("SM ")
				.pattern("PPD")
				.unlockedBy("has_item", conditionsFromTag(ExtraBotanyTags.Items.INGOTS_PHOTONIUM))
				.save(consumer);
		registerBottledTool(consumer, ExtraBotanyItems.bottledFlame, BotaniaTags.Items.getPetalTag(DyeColor.RED), Items.TORCH);
		registerBottledTool(consumer, ExtraBotanyItems.bottledStar, BotaniaTags.Items.getPetalTag(DyeColor.YELLOW), ExtraBotanyItems.spiritFragment);
		registerBottledTool(consumer, ExtraBotanyItems.bottledPixie, BotaniaTags.Items.getPetalTag(DyeColor.LIME), BotaniaItems.pixieDust);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyItems.manaGlassBottle, 3)
				.define('G', BotaniaBlocks.manaGlass)
				.pattern("G G")
				.pattern("G G")
				.pattern(" G ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.manaGlass))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.nineAndThreeQuartersRewardBag, 3)
				.requires(BotaniaItems.dice)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.dice))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.pureDaisyPendant)
				.define('P', BotaniaBlocks.pureDaisy)
				.define('E', ConventionalBotaniaTags.Items.ELEMENTIUM_NUGGETS)
				.pattern("PEP")
				.pattern(" P ")
				.pattern("   ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.pureDaisy))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.peaceAmulet)
				.define('W', BotaniaBlocks.livingwood)
				.define('R', BotaniaBlocks.livingrock)
				.pattern(" W ")
				.pattern("WRW")
				.pattern(" W ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.livingrock))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ExtraBotanyItems.dispersiveRing)
				.requires(BotaniaItems.manaRing)
				.requires(BotaniaItems.runeMana)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaRing))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.frostRing)
				.define('M', ConventionalBotaniaTags.Items.MANASTEEL_INGOTS)
				.define('I', Items.ICE)
				.pattern("IM ")
				.pattern("M M")
				.pattern(" M ")
				.unlockedBy("has_item", conditionsFromTag(ConventionalBotaniaTags.Items.MANASTEEL_INGOTS))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.curseRing)
				.define('M', ConventionalBotaniaTags.Items.MANASTEEL_INGOTS)
				.define('S', Items.WITHER_SKELETON_SKULL)
				.pattern("SM ")
				.pattern("M M")
				.pattern(" M ")
				.unlockedBy("has_item", conditionsFromTag(ConventionalBotaniaTags.Items.MANASTEEL_INGOTS))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.gemOfConquest)
				.define('G', BotaniaItems.gaiaIngot)
				.define('H', ExtraBotanyItems.heroMedal)
				.define('E', ExtraBotanyItems.theEnd)
				.define('S', ExtraBotanyItems.sinRune)
				.pattern("GSG")
				.pattern("EHE")
				.pattern("GSG")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.heroMedal))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.spiderRing)
				.define('M', ConventionalBotaniaTags.Items.MANASTEEL_INGOTS)
				.define('S', Items.SPIDER_EYE)
				.pattern("SM ")
				.pattern("M M")
				.pattern(" M ")
				.unlockedBy("has_item", conditionsFromTag(ConventionalBotaniaTags.Items.MANASTEEL_INGOTS))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.jackieChanRing)
				.define('M', ConventionalBotaniaTags.Items.MANASTEEL_INGOTS)
				.define('S', Items.STICKY_PISTON)
				.pattern("SM ")
				.pattern("M M")
				.pattern(" M ")
				.unlockedBy("has_item", conditionsFromTag(ConventionalBotaniaTags.Items.MANASTEEL_INGOTS))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, ExtraBotanyItems.parkourRing)
				.requires(ExtraBotanyItems.spiderRing)
				.requires(ExtraBotanyItems.jackieChanRing)
				.unlockedBy("has_item", conditionsFromTag(ConventionalBotaniaTags.Items.MANASTEEL_INGOTS))
				.save(consumer);
		registerCosmeticBauble(consumer, ExtraBotanyItems.foxEar, DyeColor.PINK);
		registerCosmeticBauble(consumer, ExtraBotanyItems.foxMask, DyeColor.WHITE);
		registerCosmeticBauble(consumer, ExtraBotanyItems.pylon, DyeColor.GREEN);
		registerCosmeticBauble(consumer, ExtraBotanyItems.goggleJungle, DyeColor.LIME);
		registerCosmeticBauble(consumer, ExtraBotanyItems.goggleOcean, DyeColor.CYAN);
		registerCosmeticBauble(consumer, ExtraBotanyItems.goggleSnowfield, DyeColor.WHITE);
		registerCosmeticBauble(consumer, ExtraBotanyItems.goggleStandard, DyeColor.LIGHT_GRAY);
		registerCosmeticBauble(consumer, ExtraBotanyItems.blackGlasses, DyeColor.BLACK);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.thugLife)
				.define('P', BotaniaTags.Items.getPetalTag(DyeColor.BLACK))
				.define('G', ExtraBotanyItems.blackGlasses)
				.define('S', Items.PAPER)
				.pattern("P P")
				.pattern("SGS")
				.pattern("P P")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.blackGlasses))
				.save(consumer);
		registerCosmeticBauble(consumer, ExtraBotanyItems.redScarf, DyeColor.RED);
		registerCosmeticBauble(consumer, ExtraBotanyItems.mask, DyeColor.GRAY);
		registerCosmeticBauble(consumer, ExtraBotanyItems.superCrown, DyeColor.YELLOW);

	}

	private void registerCosmeticBauble(RecipeOutput consumer, ItemLike result, DyeColor petalColor) {
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, result)
				.define('P', BotaniaTags.Items.getPetalTag(petalColor))
				.define('S', ExtraBotanyItems.spiritFragment)
				.pattern("PPP")
				.pattern("PSP")
				.pattern("PPP")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.spiritFragment))
				.save(consumer);
	}

	private void registerBottledTool(RecipeOutput consumer, ItemLike result, TagKey<Item> petalTag, ItemLike center) {
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, result)
				.define('A', petalTag)
				.define('B', center)
				.define('C', BotaniaBlocks.manaGlass)
				.pattern("AAA")
				.pattern("CBC")
				.pattern("CCC")
				.unlockedBy("has_item", conditionsFromItem(center))
				.save(consumer);
	}

	private void registerLenses(RecipeOutput consumer) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.lensPush)
				.requires(BotaniaItems.lensNormal)
				.requires(BotaniaItems.runeEarth)
				.requires(ConventionalBotaniaTags.Items.MANA_DUSTS)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.lensSmelt)
				.requires(BotaniaItems.lensNormal)
				.requires(BotaniaItems.runeFire)
				.requires(ConventionalBotaniaTags.Items.MANA_DUSTS)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.lensMana)
				.requires(BotaniaItems.lensNormal)
				.requires(BotaniaItems.runeMana)
				.requires(ConventionalBotaniaTags.Items.MANA_DUSTS)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.lensPotion)
				.requires(BotaniaItems.lensNormal)
				.requires(BotaniaItems.runeSpring)
				.requires(ConventionalBotaniaTags.Items.MANA_DUSTS)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.lensTrace)
				.requires(BotaniaItems.lensNormal)
				.requires(BotaniaItems.runeGreed)
				.requires(ConventionalBotaniaTags.Items.MANA_DUSTS)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(consumer);

	}

	private void registerTools(RecipeOutput consumer) {
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.failnaught)
				.define('O', ExtraBotanyTags.Items.INGOTS_ORICHALCOS)
				.define('S', BotaniaItems.manaString)
				.define('G', BotaniaItems.gaiaIngot)
				.pattern(" GS")
				.pattern("GOS")
				.pattern(" GS")
				.unlockedBy("has_item", conditionsFromTag(ExtraBotanyTags.Items.INGOTS_ORICHALCOS))
				.save(consumer);
		//ManaReader&WandExtend
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.manaReader)
				.define('D', ConventionalBotaniaTags.Items.MANA_DIAMOND_GEMS)
				.define('P', ConventionalBotaniaTags.Items.MANA_DUSTS)
				.define('S', BotaniaItems.livingwoodTwig)
				.pattern(" PD")
				.pattern(" SP")
				.pattern("S  ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.twigWand))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.binder)
				.define('X', ConventionalBotaniaTags.Items.MANA_DUSTS)
				.define('Y', ConventionalBotaniaTags.Items.DRAGONSTONE_GEMS)
				.define('Z', BotaniaItems.livingwoodTwig)
				.pattern(" XY")
				.pattern(" ZX")
				.pattern("Z  ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.livingwoodTwig))
				.save(consumer);
		/*
		ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, BotaniaItems.twigWand)
				.requires(BotaniaItems.twigWand)
				.requires(ExtraBotanyItems.manaReader)
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.manaReader))
				.save(WrapperResult.ofType(WandOfTheForestExtendRecipe.SERIALIZER, consumer), prefix("twig_wand_extension"));
		ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, BotaniaItems.dreamwoodWand)
				.requires(BotaniaItems.dreamwoodWand)
				.requires(ExtraBotanyItems.manaReader)
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.manaReader))
				.save(WrapperResult.ofType(WandOfTheForestExtendRecipe.SERIALIZER, consumer), prefix("dreamwood_wand_extension"));
		*/

		//Hammer
		hammer(ConventionalBotaniaTags.Items.MANASTEEL_INGOTS, BotaniaItems.livingwoodTwig, ExtraBotanyItems.manasteelHammer).save(consumer);
		hammer(ConventionalBotaniaTags.Items.ELEMENTIUM_INGOTS, BotaniaItems.dreamwoodTwig, ExtraBotanyItems.elementiumHammer).save(consumer);
		hammer(ConventionalBotaniaTags.Items.TERRASTEEL_INGOTS, BotaniaItems.livingwoodTwig, ExtraBotanyItems.terrasteelHammer).save(consumer);
		//Gaia hammer see smithing recipe
		hammer(ExtraBotanyTags.Items.INGOTS_PHOTONIUM, BotaniaItems.livingwoodTwig, ExtraBotanyItems.photoniumHammer).save(consumer);
		hammer(ExtraBotanyTags.Items.INGOTS_SHADOWIUM, BotaniaItems.livingwoodTwig, ExtraBotanyItems.shadowiumHammer).save(consumer);
		hammer(ExtraBotanyTags.Items.INGOTS_AERIALITE, BotaniaItems.dreamwoodTwig, ExtraBotanyItems.aerialiteHammer).save(consumer);
		//Orichalcos & Rhin see RunicAltar

		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.walkingCane)
				.define('T', BotaniaItems.livingwoodTwig)
				.define('G', Items.GOLD_BLOCK)
				.define('P', Items.PRISMARINE_CRYSTALS)
				.pattern("TGP")
				.pattern(" TG")
				.pattern("T T")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.livingwoodTwig))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyItems.magicFinger)
				.define('C', Items.CARROT)
				.define('H', ExtraBotanyItems.heroMedal)
				.define('P', BotaniaItems.manaPowder)
				.pattern(" P ")
				.pattern("PCP")
				.pattern(" H ")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.heroMedal))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ExtraBotanyItems.kingGarden)
				.define('M', ExtraBotanyItems.theOrigin)
				.define('T', ConventionalBotaniaTags.Items.TERRASTEEL_INGOTS)
				.define('C', Items.CHEST)
				.define('W', ItemTags.WOOL)
				.pattern("MTM")
				.pattern("MCM")
				.pattern("MWM")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.theOrigin))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.uuzFan)
				.define('O', ExtraBotanyItems.theOrigin)
				.define('E', ConventionalBotaniaTags.Items.ELEMENTIUM_INGOTS)
				.define('P', BotaniaItems.pixieDust)
				.define('W', BotaniaItems.manaweaveCloth)
				.pattern("PEP")
				.pattern("WOW")
				.pattern(" W ")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.theOrigin))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.flamescionWeapon)
				.define('O', ExtraBotanyTags.Items.INGOTS_ORICHALCOS)
				.define('U', ExtraBotanyItems.theUniverse)
				.define('E', ExtraBotanyItems.theEnd)
				.define('B', Items.BLAZE_ROD)
				.define('T', BotaniaItems.terraSword)
				.define('L', BotaniaItems.lifeEssence)
				.pattern("OUE")
				.pattern("BTL")
				.pattern("B O")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.theUniverse))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.silverBullet)
				.define('P', C_INGOTS_PHOTONIUM)
				.define('C', ExtraBotanyItems.theChaos)
				.define('M', BotaniaItems.manaGun)
				.define('S', ConventionalBotaniaTags.Items.MANASTEEL_INGOTS)
				.pattern("PPS")
				.pattern(" MC")
				.pattern("  P")
				.unlockedBy("has_item", conditionsFromTag(C_INGOTS_PHOTONIUM))
				.save(consumer);
	}

	private void registerArmors(RecipeOutput consumer) {
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.starryIdolHeadgear)
				.define('W', BotaniaItems.manaweaveCloth)
				.define('A', BotaniaItems.manasteelHelm)
				.pattern("WWW")
				.pattern("WAW")
				.pattern("WWW")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manasteelHelm))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.starryIdolSuit)
				.define('W', BotaniaItems.manaweaveCloth)
				.define('A', BotaniaItems.manasteelChest)
				.pattern("WWW")
				.pattern("WAW")
				.pattern("WWW")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manasteelChest))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.starryIdolSkirt)
				.define('W', BotaniaItems.manaweaveCloth)
				.define('A', BotaniaItems.manasteelLegs)
				.pattern("WWW")
				.pattern("WAW")
				.pattern("WWW")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manasteelLegs))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.starryIdolBoots)
				.define('W', BotaniaItems.manaweaveCloth)
				.define('A', BotaniaItems.manasteelBoots)
				.pattern("WWW")
				.pattern("WAW")
				.pattern("WWW")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manasteelBoots))
				.save(consumer);
		registerMikuArmorUpgrade(consumer, ExtraBotanyItems.mikuHelm, BotaniaItems.manasteelHelm);
		registerMikuArmorUpgrade(consumer, ExtraBotanyItems.mikuChest, BotaniaItems.manasteelChest);
		registerMikuArmorUpgrade(consumer, ExtraBotanyItems.mikuLegs, BotaniaItems.manasteelLegs);
		registerMikuArmorUpgrade(consumer, ExtraBotanyItems.mikuBoots, BotaniaItems.manasteelBoots);
		registerShootingGuardianArmorUpgrade(consumer, ExtraBotanyItems.shootingGuardianHelm, BotaniaItems.manasteelHelm, BotaniaItems.runeSpring);
		registerShootingGuardianArmorUpgrade(consumer, ExtraBotanyItems.shootingGuardianChest, BotaniaItems.manasteelChest, BotaniaItems.runeSummer);
		registerShootingGuardianArmorUpgrade(consumer, ExtraBotanyItems.shootingGuardianLegs, BotaniaItems.manasteelLegs, BotaniaItems.runeAutumn);
		registerShootingGuardianArmorUpgrade(consumer, ExtraBotanyItems.shootingGuardianBoots, BotaniaItems.manasteelBoots, BotaniaItems.runeWinter);
		registerSilentSagesArmorUpgrade(consumer, ExtraBotanyItems.silentSagesHelm, ExtraBotanyItems.mikuHelm);
		registerSilentSagesArmorUpgrade(consumer, ExtraBotanyItems.silentSagesChest, ExtraBotanyItems.mikuChest);
		registerSilentSagesArmorUpgrade(consumer, ExtraBotanyItems.silentSagesLegs, ExtraBotanyItems.mikuLegs);
		registerSilentSagesArmorUpgrade(consumer, ExtraBotanyItems.silentSagesBoots, ExtraBotanyItems.mikuBoots);
		registerSimpleArmorSet(consumer,
				Ingredient.of(ExtraBotanyItems.shadowium), "shadow_warrior", conditionsFromTag(ExtraBotanyTags.Items.INGOTS_SHADOWIUM));
		registerSimpleArmorSet(consumer,
				Ingredient.of(ExtraBotanyItems.photonium), "goblin_slayer", conditionsFromTag(ExtraBotanyTags.Items.INGOTS_PHOTONIUM));
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.pleiadesCombatMaidHeadgear)
				.define('I', BotaniaItems.gaiaIngot)
				.define('G', ExtraBotanyItems.dasRheingold)
				.define('T', BotaniaItems.terrasteelHelm)
				.pattern("III")
				.pattern("GTG")
				.pattern("   ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.pleiadesCombatMaidSuit)
				.define('I', BotaniaItems.gaiaIngot)
				.define('G', ExtraBotanyItems.dasRheingold)
				.define('T', BotaniaItems.terrasteelChest)
				.pattern("G G")
				.pattern("GTG")
				.pattern("III")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.pleiadesCombatMaidSkirt)
				.define('I', BotaniaItems.gaiaIngot)
				.define('G', ExtraBotanyItems.dasRheingold)
				.define('T', BotaniaItems.terrasteelLegs)
				.pattern("III")
				.pattern("GTG")
				.pattern("G G")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.pleiadesCombatMaidBoots)
				.define('I', BotaniaItems.gaiaIngot)
				.define('G', ExtraBotanyItems.dasRheingold)
				.define('T', BotaniaItems.terrasteelBoots)
				.pattern("GTG")
				.pattern("III")
				.pattern("   ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.manasteelShield)
				.define('I', ConventionalBotaniaTags.Items.MANASTEEL_INGOTS)
				.define('S', Items.SHIELD)
				.pattern("I I")
				.pattern("ISI")
				.pattern(" I ")
				.unlockedBy("has_item", conditionsFromTag(ConventionalBotaniaTags.Items.MANASTEEL_INGOTS))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.elementiumShield)
				.define('I', ConventionalBotaniaTags.Items.ELEMENTIUM_INGOTS)
				.define('S', Items.SHIELD)
				.pattern("I I")
				.pattern("ISI")
				.pattern(" I ")
				.unlockedBy("has_item", conditionsFromTag(ConventionalBotaniaTags.Items.ELEMENTIUM_INGOTS))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ExtraBotanyItems.terrasteelShield)
				.define('I', ConventionalBotaniaTags.Items.TERRASTEEL_INGOTS)
				.define('S', Items.SHIELD)
				.pattern("I I")
				.pattern("ISI")
				.pattern(" I ")
				.unlockedBy("has_item", conditionsFromTag(ConventionalBotaniaTags.Items.TERRASTEEL_INGOTS))
				.save(consumer);

	}

	private void registerMikuArmorUpgrade(RecipeOutput consumer, ItemLike result, ItemLike armor) {
		wrap(ManaUpgradeRecipe.SERIALIZER, ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result)
				.define('W', BotaniaItems.manaweaveCloth)
				.define('A', armor)
				.pattern("WWW")
				.pattern("WAW")
				.pattern("WWW")
				.unlockedBy("has_item", conditionsFromItem(armor))).save(consumer);
	}

	private void registerShootingGuardianArmorUpgrade(RecipeOutput consumer, ItemLike result, ItemLike armor, ItemLike rune) {
		wrap(ManaUpgradeRecipe.SERIALIZER, ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result)
				.define('T', BotaniaItems.livingwoodTwig)
				.define('S', ExtraBotanyTags.Items.INGOTS_AERIALITE)
				.define('R', rune)
				.define('A', armor)
				.pattern("TRT")
				.pattern("SAS")
				.pattern(" S ")
				.unlockedBy("has_item", conditionsFromTag(ExtraBotanyTags.Items.INGOTS_AERIALITE))).save(consumer);
	}

	private void registerSilentSagesArmorUpgrade(RecipeOutput consumer, ItemLike result, ItemLike armor) {
		wrap(ManaUpgradeRecipe.SERIALIZER, ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, result)
				.define('O', ExtraBotanyTags.Items.INGOTS_ORICHALCOS)
				.define('G', BotaniaItems.gaiaIngot)
				.define('M', BotaniaItems.manaTablet)
				.define('A', armor)
				.pattern("OGO")
				.pattern("MAM")
				.pattern("OGO")
				.unlockedBy("has_item", conditionsFromTag(ExtraBotanyTags.Items.INGOTS_ORICHALCOS))).save(consumer);
	}

	private void registerConversions(RecipeOutput consumer) {
		compression(ExtraBotanyItems.orichalcos, ExtraBotanyTags.Items.NUGGETS_ORICHALCOS)
				.save(consumer, prefix("conversions/orichalcos_from_nuggets"));

		compression(ExtraBotanyItems.photonium, ExtraBotanyTags.Items.NUGGETS_PHOTONIUM)
				.save(consumer, prefix("conversions/photonium_from_nuggets"));

		compression(ExtraBotanyItems.shadowium, ExtraBotanyTags.Items.NUGGETS_SHADOWIUM)
				.save(consumer, prefix("conversions/shadowium_from_nuggets"));

		compression(ExtraBotanyItems.aerialite, ExtraBotanyTags.Items.NUGGETS_AERIALITE)
				.save(consumer, prefix("conversions/aerialite_from_nuggets"));

		compression(ExtraBotanyBlocks.orichalcosBlock, ExtraBotanyTags.Items.INGOTS_ORICHALCOS).save(consumer);
		compression(ExtraBotanyBlocks.photoniumBlock, ExtraBotanyTags.Items.INGOTS_PHOTONIUM).save(consumer);
		compression(ExtraBotanyBlocks.shadowiumBlock, ExtraBotanyTags.Items.INGOTS_SHADOWIUM).save(consumer);
		compression(ExtraBotanyBlocks.aerialiteBlock, ExtraBotanyTags.Items.INGOTS_AERIALITE).save(consumer);

		deconstruct(consumer, ExtraBotanyItems.orichalcos, ExtraBotanyTags.Items.BLOCKS_ORICHALCOS, "orichalcos_block_deconstruct");
		deconstruct(consumer, ExtraBotanyItems.photonium, ExtraBotanyTags.Items.BLOCKS_PHOTONIUM, "photonium_block_deconstruct");
		deconstruct(consumer, ExtraBotanyItems.shadowium, ExtraBotanyTags.Items.BLOCKS_SHADOWIUM, "shadowium_block_deconstruct");
		deconstruct(consumer, ExtraBotanyItems.aerialite, ExtraBotanyTags.Items.BLOCKS_AERIALITE, "aerialite_block_deconstruct");

		deconstruct(consumer, ExtraBotanyItems.orichalcosNugget, ExtraBotanyTags.Items.INGOTS_ORICHALCOS, "orichalcos_to_nuggets");
		deconstruct(consumer, ExtraBotanyItems.photoniumNugget, ExtraBotanyTags.Items.INGOTS_PHOTONIUM, "photonium_to_nuggets");
		deconstruct(consumer, ExtraBotanyItems.shadowiumNugget, ExtraBotanyTags.Items.INGOTS_SHADOWIUM, "shadowium_to_nuggets");
		deconstruct(consumer, ExtraBotanyItems.aerialiteNugget, ExtraBotanyTags.Items.INGOTS_AERIALITE, "aerialite_to_nuggets");

	}

	private void registerDecor(RecipeOutput consumer) {
		registerForQuartz(consumer, LibItemNames.GAIA_QUARTZ, ExtraBotanyItems.gaiaQuartz);
		registerForQuartz(consumer, LibItemNames.ELEMENTIUM_QUARTZ, ExtraBotanyItems.elementiumQuartz);
	}

	protected void registerForQuartz(RecipeOutput consumer, String variant, ItemLike baseItem) {
		Block block = getBlockOrThrow(prefix(variant + "_block"));
		Block stairs = getBlockOrThrow(prefix(variant + LibBlockNames.STAIR_SUFFIX));
		Block slab = getBlockOrThrow(prefix(variant + LibBlockNames.SLAB_SUFFIX));
		Block chiseled = getBlockOrThrow(prefix("chiseled_" + variant + "_block"));
		Block bricks = getBlockOrThrow(prefix(variant + "_bricks"));
		Block pillar = getBlockOrThrow(prefix(variant + "_pillar"));
		Block smooth = getBlockOrThrow(prefix("smooth_" + variant));
		Block smoothStairs = getBlockOrThrow(prefix("smooth_" + variant + LibBlockNames.STAIR_SUFFIX));
		Block smoothSlab = getBlockOrThrow(prefix("smooth_" + variant + LibBlockNames.SLAB_SUFFIX));

		ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block)
				.define('Q', baseItem)
				.pattern("QQ")
				.pattern("QQ")
				.group("extrabotany:quartz_block")
				.unlockedBy("has_item", conditionsFromItem(baseItem))
				.save(consumer);
		stairs(stairs, block).group("extrabotany:quartz_stairs").save(consumer);
		slabShape(slab, block).group("extrabotany:quartz_slab").save(consumer);
		chiseled(chiseled, slab).group("extrabotany:quartz_chiseled")
				.unlockedBy("has_base_item", conditionsFromItem(block)).save(consumer);
		brick(bricks, block).group("extrabotany:quartz_bricks").save(consumer);
		pillar(pillar, block).group("extrabotany:quartz_pillar").save(consumer);
		//smooth: see SmeltingProvider
		stairs(smoothStairs, smooth).group("extrabotany:quartz_stairs").save(consumer);
		slabShape(smoothSlab, smooth).group("extrabotany:quartz_slab").save(consumer);
	}

	private void registerMisc(RecipeOutput consumer) {

		//Pedestal
		pedestal(BotaniaBlocks.livingrock, ExtraBotanyBlocks.livingrockPedestal)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.livingrock))
				.save(consumer);
		pedestal(Items.CALCITE, ExtraBotanyBlocks.calcitePedestal)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.livingrock))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyBlocks.manaCharger)
				.define('R', BotaniaBlocks.livingrockSlab)
				.define('S', BotaniaItems.livingwoodTwig)
				.pattern("   ")
				.pattern(" R ")
				.pattern("S S")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.livingrock))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyBlocks.manaBuffer)
				.define('K', BotaniaBlocks.fabulousPool)
				.define('M', BotaniaItems.lensNormal)
				.define('G', BotaniaItems.gaiaIngot)
				.pattern("KMK")
				.pattern("KGK")
				.pattern("KMK")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.fabulousPool))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyBlocks.quantumManaBuffer)
				.define('M', ExtraBotanyBlocks.manaBuffer)
				.define('O', C_INGOTS_ORICHALCOS)
				.pattern("MMM")
				.pattern("MOM")
				.pattern("MMM")
				.unlockedBy("has_item", conditionsFromTag(C_INGOTS_ORICHALCOS))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyBlocks.manaGenerator)
				.define('A', BotaniaBlocks.livingrock)
				.define('B', Items.LAPIS_BLOCK)
				.define('C', ExtraBotanyItems.heroMedal)
				.pattern("ABA")
				.pattern("BCB")
				.pattern("ABA")
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.heroMedal))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyBlocks.dimensionCatalyst)
				.define('R', BotaniaBlocks.livingrock)
				.define('P', Items.ENDER_PEARL)
				.define('C', BotaniaBlocks.alchemyCatalyst)
				.define('S', ExtraBotanyItems.theChaos)
				.define('Q', ExtraBotanyItems.elementiumQuartz)
				.pattern("RPR")
				.pattern("QCQ")
				.pattern("RSR")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.alchemyCatalyst))
				.save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ExtraBotanyItems.gaiaQuartz)
				.define('T', BotaniaItems.terrasteelNugget)
				.define('Q', Items.QUARTZ)
				.pattern("QQQ")
				.pattern("QTQ")
				.pattern("QQQ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.terrasteel))
				.save(consumer);
		wrapShapeless(consumer, ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.manaCocktail)
				.requires(ExtraBotanyItems.manaCocktail)
				.requires(BotaniaItems.brewFlask)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.brewFlask)),
				prefix("mana_cocktail_change_brew"), CopyBrewFormFlaskRecipe::new);
		wrapShapeless(consumer, ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.infiniteWine)
				.requires(ExtraBotanyItems.manaCocktail)
				.requires(ExtraBotanyItems.infiniteWine)
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.heroMedal)),
				prefix("infinite_wine_change_brew"), CopyBrewFromManaCocktailRecipe::new);
		wrapShapeless(consumer, ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.infiniteWine)
				.requires(ExtraBotanyItems.manaCocktail)
				.requires(ExtraBotanyItems.heroMedal)
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.heroMedal)),
				prefix("infinite_wine"), CopyBrewFromManaCocktailRecipe::new);
		wrapShapeless(consumer, ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.holyWaterGrenade)
				.requires(ExtraBotanyItems.manaCocktail)
				.requires(Items.POPPED_CHORUS_FRUIT)
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.manaCocktail)),
				prefix("holy_water_grenade"), CopyBrewFromManaCocktailRecipe::new);

		registerCandy(consumer);
	}

	private void registerCandy(RecipeOutput consumer) {
		// Halloween candies — faithful to ExtraMeteorP (paper + 3 sugar + pumpkin + a dye).
		ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ExtraBotanyItems.candyEins, 3)
				.requires(Items.PAPER)
				.requires(Items.SUGAR, 3)
				.requires(Items.PUMPKIN)
				.requires(Items.GLOWSTONE_DUST)
				.unlockedBy("has_item", conditionsFromItem(Items.PUMPKIN))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ExtraBotanyItems.candyZwei, 3)
				.requires(Items.PAPER)
				.requires(Items.SUGAR, 3)
				.requires(Items.PUMPKIN)
				.requires(Items.GREEN_DYE)
				.unlockedBy("has_item", conditionsFromItem(Items.PUMPKIN))
				.save(consumer);
		ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ExtraBotanyItems.candyDrei, 3)
				.requires(Items.PAPER)
				.requires(Items.SUGAR, 3)
				.requires(Items.PUMPKIN)
				.requires(Items.REDSTONE)
				.unlockedBy("has_item", conditionsFromItem(Items.PUMPKIN))
				.save(consumer);
		// Candy bag (Halloween Candy Night) — assorted candies wrapped in leather.
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ExtraBotanyItems.candyBag)
				.requires(Items.LEATHER)
				.requires(ExtraBotanyItems.candyEins)
				.requires(ExtraBotanyItems.candyZwei)
				.requires(ExtraBotanyItems.candyDrei)
				.unlockedBy("has_item", conditionsFromItem(ExtraBotanyItems.candyEins))
				.save(consumer);
		// Core of God — reset back to the default (Herrscher) variant.
		ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, ExtraBotanyItems.coreOfTheVoid)
				.requires(ExtraBotanyItems.coreOfTheVoid)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.flightTiara))
				.save(consumer, prefix("core_of_the_void_reset"));
	}

	private void registerFloatingFlowers(RecipeOutput consumer) {
		var floatings = BuiltInRegistries.BLOCK.stream().filter(EXTRABOTANY_BLOCK.and(b -> b instanceof FloatingSpecialFlowerBlock))
				.sorted(Comparator.comparing(BuiltInRegistries.BLOCK::getKey))
				.toArray(Block[]::new);

		for (var block : floatings) {
			ResourceLocation inputName = BuiltInRegistries.ITEM.getKey(block.asItem());
			Item origin = getItemOrThrow(ResourceLocation.fromNamespaceAndPath(inputName.getNamespace(), inputName.getPath().replace("floating_", "")));
			createFloatingFlowerRecipe(consumer, origin);
		}
	}

	protected ShapedRecipeBuilder hammer(ItemLike head, ItemLike handle, ItemLike hammer) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, hammer)
				.define('H', head)
				.define('S', handle)
				.pattern("HHH")
				.pattern("HHH")
				.pattern(" S ")
				.unlockedBy("has_item", conditionsFromItem(head));
	}

	protected ShapedRecipeBuilder hammer(TagKey<Item> item, ItemLike handle, ItemLike hammer) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, hammer)
				.define('H', item)
				.define('S', handle)
				.pattern("HHH")
				.pattern("HHH")
				.pattern(" S ")
				.unlockedBy("has_item", conditionsFromTag(item));
	}

	protected ShapedRecipeBuilder pedestal(ItemLike block, ItemLike pedestal) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, pedestal)
				.define('P', Items.GOLD_NUGGET)
				.define('C', block)
				.pattern("CPC")
				.pattern(" C ")
				.pattern("CCC");
	}

	private ShapedRecipeBuilder compression(ItemLike output, TagKey<Item> input) {
		return ShapedRecipeBuilder.shaped(output instanceof Block ? RecipeCategory.BUILDING_BLOCKS : RecipeCategory.MISC, output)
				.define('I', input)
				.pattern("III")
				.pattern("III")
				.pattern("III")
				.unlockedBy("has_item", conditionsFromTag(input));
	}

	private static void specialRecipe(RecipeOutput recipeOutput, RecipeSerializer<?> serializer, Function<CraftingBookCategory, Recipe<?>> factory) {
		SpecialRecipeBuilder.special(factory).save(recipeOutput, BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer));
	}

	private static <T extends Recipe<?>> WrapperRecipeBuilder<T> wrap(WrappingRecipeSerializer<T> serializer, RecipeBuilder recipeBuilder) {
		return WrapperRecipeBuilder.wrap(serializer, recipeBuilder);
	}

	private static <T extends Recipe<?>> void wrapShapeless(RecipeOutput recipeOutput, RecipeBuilder recipeBuilder,
			ResourceLocation id, java.util.function.Function<ShapelessRecipe, T> wrapper) {
		CapturingRecipeOutput capturingRecipeOutput = new CapturingRecipeOutput(recipeOutput);
		var captured = capturingRecipeOutput.captureSave(out -> recipeBuilder.save(out, id));
		if (!(captured.getMiddle() instanceof ShapelessRecipe shapelessRecipe)) {
			throw new IllegalArgumentException("Expected shapeless recipe for " + id + ", got " + captured.getMiddle().getType());
		}
		recipeOutput.accept(captured.getLeft(), wrapper.apply(shapelessRecipe), captured.getRight().orElse(null));
	}

	private static Block getBlockOrThrow(ResourceLocation id) {
		return BuiltInRegistries.BLOCK.getOptional(id).orElseThrow(() -> new IllegalStateException("Unknown block " + id));
	}

	private static Item getItemOrThrow(ResourceLocation id) {
		return BuiltInRegistries.ITEM.getOptional(id).orElseThrow(() -> new IllegalStateException("Unknown item " + id));
	}

	private static ShapedRecipeBuilder stairs(ItemLike stairs, ItemLike material) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, stairs, 4)
				.define('Q', material)
				.pattern("Q  ")
				.pattern("QQ ")
				.pattern("QQQ")
				.unlockedBy("has_item", conditionsFromItem(material));
	}

	private static ShapedRecipeBuilder slabShape(ItemLike slab, ItemLike material) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, slab, 6)
				.define('Q', material)
				.pattern("QQQ")
				.unlockedBy("has_item", conditionsFromItem(material));
	}

	private static ShapedRecipeBuilder chiseled(ItemLike chiseled, ItemLike slab) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, chiseled)
				.define('Q', slab)
				.pattern("Q")
				.pattern("Q");
	}

	private static ShapedRecipeBuilder brick(ItemLike bricks, ItemLike material) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, bricks, 4)
				.define('Q', material)
				.pattern("QQ")
				.pattern("QQ")
				.unlockedBy("has_item", conditionsFromItem(material));
	}

	private static ShapedRecipeBuilder pillar(ItemLike pillar, ItemLike material) {
		return ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, pillar, 2)
				.define('Q', material)
				.pattern("Q")
				.pattern("Q")
				.unlockedBy("has_item", conditionsFromItem(material));
	}

	private static void createFloatingFlowerRecipe(RecipeOutput recipeOutput, ItemLike flower) {
		ResourceLocation flowerId = BuiltInRegistries.ITEM.getKey(flower.asItem());
		Item floating = getItemOrThrow(ResourceLocation.fromNamespaceAndPath(flowerId.getNamespace(), "floating_" + flowerId.getPath()));
		ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, floating)
				.requires(BotaniaTags.Items.FLOATING_FLOWERS)
				.requires(flower)
				.group("botania:floating_flower")
				.unlockedBy("has_item", conditionsFromItem(flower))
				.save(recipeOutput, BuiltInRegistries.ITEM.getKey(floating));
	}

	private static void registerSimpleArmorSet(RecipeOutput recipeOutput, Ingredient material, String setName,
			net.minecraft.advancements.Criterion<?> criterion) {
		String itemPrefix = setName + "_";
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, getItemOrThrow(prefixStatic(itemPrefix + "helmet")))
				.define('S', material)
				.pattern("SSS")
				.pattern("S S")
				.unlockedBy("has_item", criterion)
				.save(recipeOutput);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, getItemOrThrow(prefixStatic(itemPrefix + "chestplate")))
				.define('S', material)
				.pattern("S S")
				.pattern("SSS")
				.pattern("SSS")
				.unlockedBy("has_item", criterion)
				.save(recipeOutput);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, getItemOrThrow(prefixStatic(itemPrefix + "leggings")))
				.define('S', material)
				.pattern("SSS")
				.pattern("S S")
				.pattern("S S")
				.unlockedBy("has_item", criterion)
				.save(recipeOutput);
		ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, getItemOrThrow(prefixStatic(itemPrefix + "boots")))
				.define('S', material)
				.pattern("S S")
				.pattern("S S")
				.unlockedBy("has_item", criterion)
				.save(recipeOutput);
	}

	private static void deconstruct(RecipeOutput recipeOutput, ItemLike output, TagKey<Item> input, String id) {
		ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, output, 9)
				.requires(input)
				.unlockedBy("has_item", conditionsFromTag(input))
				.save(recipeOutput, prefixStatic("conversions/" + id));
	}

	private static ResourceLocation prefixStatic(String path) {
		return ResourceLocationHelper.prefix(path);
	}

	private static TagKey<Item> commonItemTag(String path) {
		return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
	}

	protected ResourceLocation prefix(String path) {
		return ResourceLocationHelper.prefix(path);
	}

	@Override
	public String getName() {
		return "ExtraBotany crafting recipes";
	}
}
