package io.github.lounode.extrabotany.common.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.RecipeSerializer;

import vazkii.botania.common.item.CustomCreativeTabContents;
import vazkii.botania.common.item.lens.Lens;
import vazkii.botania.common.item.lens.LensItem;
import vazkii.botania.xplat.XplatAbstractions;

import io.github.lounode.extrabotany.common.crafting.recipe.*;
import io.github.lounode.extrabotany.common.item.brew.HolyWaterGrenadeItem;
import io.github.lounode.extrabotany.common.item.brew.InfiniteWineItem;
import io.github.lounode.extrabotany.common.item.brew.ManaCocktailItem;
import io.github.lounode.extrabotany.common.item.brew.ManaGlassBottleItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.goblin_slayer.GoblinSlayerArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.goblin_slayer.GoblinSlayerHelmetItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.old.OldExbotanyArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.old.ShootingGuardianArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid.PleiadesCombatMaidArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid.PleiadesCombatMaidSuitItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid.SanguinePleiadesCombatMaidSuitItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.shadow_warrior.ShadowWarriorArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.shadow_warrior.ShadowWarriorHelmetItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.starry_idol.StarryIdolArmorItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.starry_idol.StarryIdolHeadgearItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.*;
import io.github.lounode.extrabotany.common.item.equipment.shield.ElementiumShieldItem;
import io.github.lounode.extrabotany.common.item.equipment.shield.ManasteelShieldItem;
import io.github.lounode.extrabotany.common.item.equipment.shield.TerrasteelShieldItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.BinderItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.MagicFingerItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.BottledPixieItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.BottledStarItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.CosmicCarKeyItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.FlamescionWeaponItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.FlyingBoatItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.KingGardenItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.MotorItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.PhotonShotgunItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.RodOfDiscordItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.ShadowKatanaItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.SilverBulletItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.UuzFanItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.hammer.*;
import io.github.lounode.extrabotany.common.item.lens.*;
import io.github.lounode.extrabotany.common.item.material.ChallangeTicketItem;
import io.github.lounode.extrabotany.common.item.material.GildedPotatoItem;
import io.github.lounode.extrabotany.common.item.material.HammerTiers;
import io.github.lounode.extrabotany.common.item.record.ExtraBotanyJukeboxSongs;
import io.github.lounode.extrabotany.common.item.relic.*;
import io.github.lounode.extrabotany.common.item.relic.void_archives.VoidArchivesItem;
import io.github.lounode.extrabotany.common.item.relic.voidcore.CoreOfTheVoidItem;
import io.github.lounode.extrabotany.common.lib.LibItemNames;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public final class ExtraBotanyItems {
	public static final Map<ResourceLocation, Item> ALL = new LinkedHashMap<>();

	//Mana Item
	public static final Item zadkiel = make(prefix(LibItemNames.ZADKIEL), new ZadkielItem(unstackable()));
	public static final Item manaReader = make(prefix(LibItemNames.MANA_READER), new ManaReaderItem(unstackable()));
	public static final Item natureOrb = make(prefix(LibItemNames.NATURE_ORB), new NatureOrbItem(unstackable().rarity(Rarity.UNCOMMON)));
	public static final Item featherOfJingwei = make(prefix(LibItemNames.FEATHER_OF_JINGWEI), new FeatherOfJingweiItem(unstackable()));
	public static final Item magicFinger = make(prefix(LibItemNames.MAGIC_FINGER), new MagicFingerItem(unstackable()));
	public static final Item walkingCane = make(prefix(LibItemNames.WALKING_CANE), new WalkingCaneItem(unstackable()));
	public static final Item rodOfDiscord = make(prefix(LibItemNames.ROD_OF_DISCORD), new RodOfDiscordItem(unstackableCustomDamage()));
	public static final Item shadowKatana = make(prefix(LibItemNames.SHADOW_KATANA), new ShadowKatanaItem(Tiers.IRON, unstackableCustomDamage()));
	public static final Item uuzFan = make(prefix(LibItemNames.UUZ_FAN), new UuzFanItem(unstackable()));
	public static final Item binder = make(prefix(LibItemNames.BINDER), new BinderItem(unstackable()));
	public static final Item bottledFlame = make(prefix(LibItemNames.BOTTLED_FLAME), new BottledFlameItem(unstackable()));
	public static final Item bottledStar = make(prefix(LibItemNames.BOTTLED_STAR), new BottledStarItem(unstackable()));
	public static final Item bottledPixie = make(prefix(LibItemNames.BOTTLED_PIXIE), new BottledPixieItem(unstackable()));
	public static final Item photonShotgun = make(prefix(LibItemNames.PHOTON_SHOTGUN), new PhotonShotgunItem(unstackable()));
	public static final Item silverBullet = make(prefix(LibItemNames.SILVER_BULLET), new SilverBulletItem(unstackable()));
	public static final Item kingGarden = make(prefix(LibItemNames.KING_GARDEN), new KingGardenItem(unstackable().rarity(Rarity.EPIC).fireResistant()));
	public static final Item flamescionWeapon = make(prefix(LibItemNames.FLAMESCION_WEAPON), new FlamescionWeaponItem(Tiers.NETHERITE, unstackable().rarity(Rarity.EPIC).fireResistant()));
	public static final Item cosmicCarKey = make(prefix(LibItemNames.COSMIC_CAR_KEY), new CosmicCarKeyItem(unstackable().rarity(Rarity.EPIC)));
	public static final Item motor = make(prefix(LibItemNames.MOTOR), new MotorItem(unstackable().rarity(Rarity.EPIC)));
	public static final Item flyingBoat = make(prefix(LibItemNames.FLYING_BOAT), new FlyingBoatItem(FlyingBoatItem.Variant.MANASTEEL, unstackable().rarity(Rarity.UNCOMMON)));
	public static final Item elementiumFlyingBoat = make(prefix(LibItemNames.ELEMENTIUM_FLYING_BOAT), new FlyingBoatItem(FlyingBoatItem.Variant.ELEMENTIUM, unstackable().rarity(Rarity.UNCOMMON)));
	public static final Item terrasteelFlyingBoat = make(prefix(LibItemNames.TERRASTEEL_FLYING_BOAT), new FlyingBoatItem(FlyingBoatItem.Variant.TERRASTEEL, unstackable().rarity(Rarity.RARE).fireResistant()));
	public static final Item motorAccessory = make(prefix(LibItemNames.MOTOR_ACCESSORY), new MountAccessoryItem(MountAccessoryItem.Variant.MOTOR, unstackable().rarity(Rarity.EPIC)));
	public static final Item cosmicCarKeyAccessory = make(prefix(LibItemNames.COSMIC_CAR_KEY_ACCESSORY), new MountAccessoryItem(MountAccessoryItem.Variant.COSMIC_CAR_KEY, unstackable().rarity(Rarity.EPIC)));
	public static final Item silentEternity = make(prefix(LibItemNames.SILENT_ETERNITY), new SilentEternityItem(unstackable().rarity(Rarity.EPIC).fireResistant()));
	public static final Item manaRingMaster = make(prefix(LibItemNames.MANA_RING_MASTER), new MasterBandOfManaItem(unstackable().rarity(Rarity.UNCOMMON).fireResistant()));
	public static final Item frostRing = make(prefix(LibItemNames.FROST_RING), new FrostRingItem(unstackable()));
	public static final Item frostStar = make(prefix(LibItemNames.FROST_STAR), new FrostStarItem(unstackable()));
	public static final Item curseRing = make(prefix(LibItemNames.CURSE_RING), new CurseRingItem(unstackable()));
	public static final Item spiderRing = make(prefix(LibItemNames.SPIDER_RING), new SpiderRingItem(unstackable()));
	public static final Item jackieChanRing = make(prefix(LibItemNames.JACKIE_CHAN_RING), new JackieChanRingItem(unstackable()));
	public static final Item parkourRing = make(prefix(LibItemNames.PARKOUR_RING), new ParkourRingItem(unstackable()));
	public static final Item dispersiveRing = make(prefix(LibItemNames.DISPERSIVE_RING), new DispersiveRingItem(unstackable()));
	public static final Item pureDaisyPendant = make(prefix(LibItemNames.PURE_DAISY_PENDANT), new PureDaisyPendantItem(unstackable()));
	public static final Item coreOfTheVoid = make(prefix(LibItemNames.CORE_OF_THE_VOID), new CoreOfTheVoidItem(unstackable().rarity(Rarity.UNCOMMON)));
	public static final Item voidArchives = make(prefix(LibItemNames.VOID_ARCHIVES), new VoidArchivesItem(unstackable().rarity(Rarity.EPIC)));

	//Hammer
	public static final Item manasteelHammer = make(prefix(LibItemNames.MANASTEEL_HAMMER),
			new ManasteelHammerItem(HammerTiers.MANASTEEL, 6, -3.1F, unstackableCustomDamage()));
	public static final Item elementiumHammer = make(prefix(LibItemNames.ELEMENTIUM_HAMMER),
			new ElementiumHammerItem(HammerTiers.ELEMENTIUM, 6, -3.1F, unstackableCustomDamage()));
	public static final Item terrasteelHammer = make(prefix(LibItemNames.TERRASTEEL_HAMMER),
			new TerrasteelHammerItem(HammerTiers.TERRASTEEL, 5, -3.0F, unstackableCustomDamage().rarity(Rarity.UNCOMMON)));
	public static final Item gaiaHammer = make(prefix(LibItemNames.GAIA_HAMMER),
			new GaiaHammerItem(HammerTiers.GAIA, 5, -3.0F, unstackableCustomDamage().rarity(Rarity.UNCOMMON).fireResistant()));
	public static final Item photoniumHammer = make(prefix(LibItemNames.PHOTONIUM_HAMMER),
			new PhotoniumHammerItem(HammerTiers.PHOTONIUM, 6, -3.1F, unstackableCustomDamage()));
	public static final Item shadowiumHammer = make(prefix(LibItemNames.SHADOWIUM_HAMMER),
			new ShadowiumHammerItem(HammerTiers.SHADOWIUM, 6, -3.1F, unstackableCustomDamage()));
	public static final Item aerialiteHammer = make(prefix(LibItemNames.AERIALITE_HAMMER),
			new AerialiteHammerItem(HammerTiers.AERIALITE, 5, -3.0F, unstackableCustomDamage().rarity(Rarity.UNCOMMON)));
	public static final Item orichalcosHammer = make(prefix(LibItemNames.ORICHALCOS_HAMMER),
			new OrichalcosHammer(HammerTiers.ORICHALCOS, 5, -3.0F, unstackableCustomDamage().rarity(Rarity.EPIC).fireResistant()));
	public static final Item rheinHammer = make(prefix(LibItemNames.RHEIN_HAMMER),
			new RheinHammerItem(unstackableCustomDamage().rarity(Rarity.EPIC).fireResistant()));
	//Shield
	public static final Item manasteelShield = make(prefix(LibItemNames.MANASTEEL_SHIELD),
			new ManasteelShieldItem(unstackableCustomDamage()));
	public static final Item elementiumShield = make(prefix(LibItemNames.ELEMENTIUM_SHIELD),
			new ElementiumShieldItem(unstackableCustomDamage()));
	public static final Item terrasteelShield = make(prefix(LibItemNames.TERRASTEEL_SHIELD),
			new TerrasteelShieldItem(unstackableCustomDamage().rarity(Rarity.UNCOMMON)));
	public static final Item achillesShield = make(prefix(LibItemNames.ACHILLES_SHIELD),
			new AchillesShieldItem(unstackableCustomDamage().rarity(Rarity.EPIC).fireResistant()));

	//Armor
	public static final Item starryIdolHeadgear = make(prefix(LibItemNames.STARRY_IDOL_HEADGEAR),
			new StarryIdolHeadgearItem(armorProps(ArmorItem.Type.HELMET, 16)));
	public static final Item starryIdolSuit = make(prefix(LibItemNames.STARRY_IDOL_SUIT),
			new StarryIdolArmorItem(ArmorItem.Type.CHESTPLATE, armorProps(ArmorItem.Type.CHESTPLATE, 16)));
	public static final Item starryIdolSkirt = make(prefix(LibItemNames.STARRY_IDOL_SKIRT),
			new StarryIdolArmorItem(ArmorItem.Type.LEGGINGS, armorProps(ArmorItem.Type.LEGGINGS, 16)));
	public static final Item starryIdolBoots = make(prefix(LibItemNames.STARRY_IDOL_BOOTS),
			new StarryIdolArmorItem(ArmorItem.Type.BOOTS, armorProps(ArmorItem.Type.BOOTS, 16)));

	public static final Item pleiadesCombatMaidHeadgear = make(prefix(LibItemNames.PLEIADES_COMBAT_MAID_HEADGEAR),
			new PleiadesCombatMaidArmorItem(ArmorItem.Type.HELMET, armorProps(ArmorItem.Type.HELMET, 50)));
	public static final Item pleiadesCombatMaidSuit = make(prefix(LibItemNames.PLEIADES_COMBAT_MAID_SUIT),
			new PleiadesCombatMaidSuitItem(armorProps(ArmorItem.Type.CHESTPLATE, 50)));
	public static final Item pleiadesCombatMaidSkirt = make(prefix(LibItemNames.PLEIADES_COMBAT_MAID_SKIRT),
			new PleiadesCombatMaidArmorItem(ArmorItem.Type.LEGGINGS, armorProps(ArmorItem.Type.LEGGINGS, 50)));
	public static final Item pleiadesCombatMaidBoots = make(prefix(LibItemNames.PLEIADES_COMBAT_MAID_BOOTS),
			new PleiadesCombatMaidArmorItem(ArmorItem.Type.BOOTS, armorProps(ArmorItem.Type.BOOTS, 50)));
	public static final Item sanguinePleiadesCombatMaidSuit = make(prefix(LibItemNames.SANGUINE_PLEIADES_COMBAT_MAID_SUIT),
			new SanguinePleiadesCombatMaidSuitItem(armorProps(ArmorItem.Type.CHESTPLATE, 50)));

	public static final Item goblinSlayerHelmet = make(prefix(LibItemNames.GOBLIN_SLAYER_HELMET),
			new GoblinSlayerHelmetItem(armorProps(ArmorItem.Type.HELMET, 21)));
	public static final Item goblinSlayerChestplate = make(prefix(LibItemNames.GOBLIN_SLAYER_CHESTPLATE),
			new GoblinSlayerArmorItem(ArmorItem.Type.CHESTPLATE, armorProps(ArmorItem.Type.CHESTPLATE, 21)));
	public static final Item goblinSlayerLeggings = make(prefix(LibItemNames.GOBLIN_SLAYER_LEGGINGS),
			new GoblinSlayerArmorItem(ArmorItem.Type.LEGGINGS, armorProps(ArmorItem.Type.LEGGINGS, 21)));
	public static final Item goblinSlayerBoots = make(prefix(LibItemNames.GOBLIN_SLAYER_BOOTS),
			new GoblinSlayerArmorItem(ArmorItem.Type.BOOTS, armorProps(ArmorItem.Type.BOOTS, 21)));

	public static final Item shadowWarriorHelmet = make(prefix(LibItemNames.SHADOW_WARRIOR_HELMET),
			new ShadowWarriorHelmetItem(armorProps(ArmorItem.Type.HELMET, 23)));
	public static final Item shadowWarriorChestplate = make(prefix(LibItemNames.SHADOW_WARRIOR_CHESTPLATE),
			new ShadowWarriorArmorItem(ArmorItem.Type.CHESTPLATE, armorProps(ArmorItem.Type.CHESTPLATE, 23)));
	public static final Item shadowWarriorLeggings = make(prefix(LibItemNames.SHADOW_WARRIOR_LEGGINGS),
			new ShadowWarriorArmorItem(ArmorItem.Type.LEGGINGS, armorProps(ArmorItem.Type.LEGGINGS, 23)));
	public static final Item shadowWarriorBoots = make(prefix(LibItemNames.SHADOW_WARRIOR_BOOTS),
			new ShadowWarriorArmorItem(ArmorItem.Type.BOOTS, armorProps(ArmorItem.Type.BOOTS, 23)));

	public static final Item mikuHelm = make(prefix(LibItemNames.MIKU_HELM),
			new OldExbotanyArmorItem(OldExbotanyArmorItem.Variant.MIKU, ArmorItem.Type.HELMET, armorProps(ArmorItem.Type.HELMET, 5)));
	public static final Item mikuChest = make(prefix(LibItemNames.MIKU_CHEST),
			new OldExbotanyArmorItem(OldExbotanyArmorItem.Variant.MIKU, ArmorItem.Type.CHESTPLATE, armorProps(ArmorItem.Type.CHESTPLATE, 5)));
	public static final Item mikuLegs = make(prefix(LibItemNames.MIKU_LEGS),
			new OldExbotanyArmorItem(OldExbotanyArmorItem.Variant.MIKU, ArmorItem.Type.LEGGINGS, armorProps(ArmorItem.Type.LEGGINGS, 5)));
	public static final Item mikuBoots = make(prefix(LibItemNames.MIKU_BOOTS),
			new OldExbotanyArmorItem(OldExbotanyArmorItem.Variant.MIKU, ArmorItem.Type.BOOTS, armorProps(ArmorItem.Type.BOOTS, 5)));

	public static final Item shootingGuardianHelm = make(prefix(LibItemNames.SHOOTING_GUARDIAN_HELM),
			new ShootingGuardianArmorItem(ArmorItem.Type.HELMET, armorProps(ArmorItem.Type.HELMET, 34)));
	public static final Item shootingGuardianChest = make(prefix(LibItemNames.SHOOTING_GUARDIAN_CHEST),
			new ShootingGuardianArmorItem(ArmorItem.Type.CHESTPLATE, armorProps(ArmorItem.Type.CHESTPLATE, 34)));
	public static final Item shootingGuardianLegs = make(prefix(LibItemNames.SHOOTING_GUARDIAN_LEGS),
			new ShootingGuardianArmorItem(ArmorItem.Type.LEGGINGS, armorProps(ArmorItem.Type.LEGGINGS, 34)));
	public static final Item shootingGuardianBoots = make(prefix(LibItemNames.SHOOTING_GUARDIAN_BOOTS),
			new ShootingGuardianArmorItem(ArmorItem.Type.BOOTS, armorProps(ArmorItem.Type.BOOTS, 34)));

	public static final Item silentSagesHelm = make(prefix(LibItemNames.SILENT_SAGES_HELM),
			new OldExbotanyArmorItem(OldExbotanyArmorItem.Variant.SILENT_SAGES, ArmorItem.Type.HELMET, armorProps(ArmorItem.Type.HELMET, 50)));
	public static final Item silentSagesChest = make(prefix(LibItemNames.SILENT_SAGES_CHEST),
			new OldExbotanyArmorItem(OldExbotanyArmorItem.Variant.SILENT_SAGES, ArmorItem.Type.CHESTPLATE, armorProps(ArmorItem.Type.CHESTPLATE, 50)));
	public static final Item silentSagesLegs = make(prefix(LibItemNames.SILENT_SAGES_LEGS),
			new OldExbotanyArmorItem(OldExbotanyArmorItem.Variant.SILENT_SAGES, ArmorItem.Type.LEGGINGS, armorProps(ArmorItem.Type.LEGGINGS, 50)));
	public static final Item silentSagesBoots = make(prefix(LibItemNames.SILENT_SAGES_BOOTS),
			new OldExbotanyArmorItem(OldExbotanyArmorItem.Variant.SILENT_SAGES, ArmorItem.Type.BOOTS, armorProps(ArmorItem.Type.BOOTS, 50)));

	//Relic
	public static final Item camera = make(prefix(LibItemNames.CAMERA), new CameraItem(unstackable().rarity(Rarity.UNCOMMON).fireResistant()));
	public static final Item failnaught = make(prefix(LibItemNames.FAILNAUGHT), new FailnaughtItem(unstackable().rarity(Rarity.UNCOMMON).fireResistant()));
	public static final Item excalibur = make(prefix(LibItemNames.EXCALIBUR), new ExcaliburItem(unstackable().rarity(Rarity.UNCOMMON).fireResistant()));
	public static final Item trueTerrablade = make(prefix(LibItemNames.TRUE_TERRABLADE), new TrueTerrabladeItem(unstackable().rarity(Rarity.EPIC).fireResistant()));
	public static final Item trueShadowKatana = make(prefix(LibItemNames.TRUE_SHADOW_KATANA), new TrueShadowKatanaItem(unstackable().rarity(Rarity.EPIC).fireResistant()));
	public static final Item influxWaver = make(prefix(LibItemNames.INFLUX_WAVER), new InfluxWaverItem(unstackable().rarity(Rarity.EPIC).fireResistant()));
	public static final Item starWrath = make(prefix(LibItemNames.STAR_WRATH), new StarWrathItem(unstackable().rarity(Rarity.EPIC).fireResistant()));
	public static final Item firstFractal = make(prefix(LibItemNames.FIRST_FRACTAL), new FirstFractalItem(unstackable().rarity(Rarity.EPIC).fireResistant()));
	public static final Item spearOfSubspace = make(prefix(LibItemNames.SPEAR_OF_SUBSPACE), new SpearOfSubspaceItem(unstackable().rarity(Rarity.EPIC).fireResistant()));
	public static final Item judahOath = make(prefix(LibItemNames.JUDAH_OATH), new JudahOathItem(JudahOathItem.Variant.JUDAH, unstackable().rarity(Rarity.EPIC).fireResistant()));
	public static final Item judahOathKira = make(prefix(LibItemNames.JUDAH_OATH_KIRA), new JudahOathItem(JudahOathItem.Variant.KIRA, unstackable().rarity(Rarity.EPIC).fireResistant()));
	public static final Item judahOathSakura = make(prefix(LibItemNames.JUDAH_OATH_SAKURA), new JudahOathItem(JudahOathItem.Variant.SAKURA, unstackable().rarity(Rarity.EPIC).fireResistant()));
	//Armor

	//Material
	public static final Item orichalcos = make(prefix(LibItemNames.ORICHALCOS), new Item(defaultBuilder().rarity(Rarity.EPIC).fireResistant()));
	public static final Item photonium = make(prefix(LibItemNames.PHOTONIUM), new Item(defaultBuilder()));
	public static final Item shadowium = make(prefix(LibItemNames.SHADOWIUM), new Item(defaultBuilder()));
	public static final Item aerialite = make(prefix(LibItemNames.AERIALITE_INGOT), new Item(defaultBuilder()));
	public static final Item orichalcosNugget = make(prefix(LibItemNames.ORICHALCOS_NUGGET), new Item(defaultBuilder().fireResistant()));
	public static final Item photoniumNugget = make(prefix(LibItemNames.PHOTONIUM_NUGGET), new Item(defaultBuilder()));
	public static final Item shadowiumNugget = make(prefix(LibItemNames.SHADOWIUM_NUGGET), new Item(defaultBuilder()));
	public static final Item aerialiteNugget = make(prefix(LibItemNames.AERIALITE_NUGGET), new Item(defaultBuilder()));
	public static final Item gaiaQuartz = make(prefix(LibItemNames.GAIA_QUARTZ), new Item(defaultBuilder()));
	public static final Item elementiumQuartz = make(prefix(LibItemNames.ELEMENTIUM_QUARTZ), new Item(defaultBuilder()));

	public static final Item lensPush = make(prefix(LibItemNames.LENS_PUSH), new LensItem(stackTo16(), new PushLens(), LensItem.PROP_INTERACTION | LensItem.PROP_TOUCH));
	public static final Item lensSmelt = make(prefix(LibItemNames.LENS_SMELT), new LensItem(stackTo16(), new SmeltLens(), LensItem.PROP_TOUCH | LensItem.PROP_INTERACTION));
	public static final Item lensMana = make(prefix(LibItemNames.LENS_MANA), new LensItem(stackTo16(), new ManaLens(), LensItem.PROP_TOUCH | LensItem.PROP_INTERACTION));
	public static final Item lensPotion = make(prefix(LibItemNames.LENS_POTION), new PotionLensItem(stackTo16(), new PotionLens(), LensItem.PROP_INTERACTION));
	public static final Item lensTrace = make(prefix(LibItemNames.LENS_TRACE), new LensItem(stackTo16(), new TraceLens(), LensItem.PROP_TOUCH | LensItem.PROP_INTERACTION));
	public static final Item lensSuperconductor = make(prefix(LibItemNames.LENS_SUPERCONDUCTOR), new LensItem(stackTo16(), new SuperconductorLens(), LensItem.PROP_TOUCH | LensItem.PROP_INTERACTION));
	public static final Item dasRheingold = make(prefix(LibItemNames.DAS_RHEINGOLD), new DasRheingoldItem(defaultBuilder()));
	public static final Item gildedPotato = make(prefix(LibItemNames.GILDED_POTATO), new GildedPotatoItem(defaultBuilder()));
	public static final Item gildedPotatoMashed = make(prefix(LibItemNames.GILDED_POTATO_MASHED), new GildedMashedPotatoItem(defaultBuilder().food(GildedMashedPotatoItem.FOOD)));
	public static final Item gemOfConquest = make(prefix(LibItemNames.GEM_OF_CONQUEST), new GemOfConquestItem(unstackable().rarity(Rarity.EPIC)));
	public static final Item universalPetal = make(prefix(LibItemNames.UNIVERSAL_PETAL), new Item(defaultBuilder()));
	public static final Item elementRune = make(prefix(LibItemNames.ELEMENT_RUNE), new Item(defaultBuilder()));
	public static final Item sinRune = make(prefix(LibItemNames.SIN_RUNE), new Item(defaultBuilder()));
	public static final Item manaDrink = make(prefix(LibItemNames.MANA_DRINK), new ManaDrinkItem(defaultBuilder()
			.food(new FoodProperties.Builder().nutrition(0).saturationModifier(0F).alwaysEdible().build())));
	public static final Item friedChicken = make(prefix(LibItemNames.FRIED_CHICKEN), new Item(defaultBuilder()
					.food(new FoodProperties.Builder()
							.nutrition(8)
							.saturationModifier(0.5F)
							.alwaysEdible()
							.build()
					)
	));
	public static final Item heroMedal = make(prefix(LibItemNames.HERO_MEDAL), new Item(defaultBuilder().rarity(Rarity.UNCOMMON)));//OT
	public static final Item challengeTicket = make(prefix(LibItemNames.CHALLENGE_TICKET), new ChallangeTicketItem(defaultBuilder()));
	public static final Item nightmareFuel = make(prefix(LibItemNames.NIGHTMARE_FUEL), new NightmareFuelItem(defaultBuilder()
			.food(new FoodProperties.Builder().nutrition(0).saturationModifier(0.3F).alwaysEdible()
					.effect(new MobEffectInstance(MobEffects.HARM, 1, 1), 1.0F)
					.effect(new MobEffectInstance(MobEffects.BLINDNESS, 500, 1), 1.0F)
					.effect(new MobEffectInstance(MobEffects.WEAKNESS, 500, 1), 1.0F)
					.effect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 500, 1), 1.0F)
					.effect(new MobEffectInstance(MobEffects.UNLUCK, 500, 1), 1.0F)
					.build()
			)
	));
	public static final Item spiritFuel = make(prefix(LibItemNames.SPIRIT_FUEL), new SpiritFuelItem(defaultBuilder()
			.food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.3F).alwaysEdible()
					.effect(new MobEffectInstance(MobEffects.HEAL, 1, 1), 1.0F)
					.effect(new MobEffectInstance(MobEffects.NIGHT_VISION, 500, 1), 1.0F)
					.effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 500, 1), 1.0F)
					.effect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 500, 1), 1.0F)
					.effect(new MobEffectInstance(MobEffects.LUCK, 500, 1), 1.0F)
					.build()
			)
	));
	public static final Item spiritFragment = make(prefix(LibItemNames.SPIRIT_FRAGMENT), new Item(defaultBuilder()));
	public static final Item manaGlassBottle = make(prefix(LibItemNames.MANA_GLASS_BOTTLE), new ManaGlassBottleItem(defaultBuilder()));
	public static final Item peaceAmulet = make(prefix(LibItemNames.PEACE_AMULET), new PeaceAmuletItem(unstackable()));
	public static final Item aeroStone = make(prefix(LibItemNames.AERO_STONE), new ElementalStoneItem(unstackable(),
			List.of(new AttributeBaubleItem.Entry(Attributes.MOVEMENT_SPEED, prefix("aero_stone_movement_speed"), 0.15D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
					new AttributeBaubleItem.Entry(Attributes.FLYING_SPEED, prefix("aero_stone_flying_speed"), 0.15D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))));
	public static final Item aquaStone = make(prefix(LibItemNames.AQUA_STONE), new AquaStoneItem(unstackable()));
	public static final Item earthStone = make(prefix(LibItemNames.EARTH_STONE), new ElementalStoneItem(unstackable(),
			List.of(new AttributeBaubleItem.Entry(Attributes.ARMOR, prefix("earth_stone_armor"), 4.0D, AttributeModifier.Operation.ADD_VALUE))));
	public static final Item ignisStone = make(prefix(LibItemNames.IGNIS_STONE), new ElementalStoneItem(unstackable(),
			List.of(new AttributeBaubleItem.Entry(Attributes.ATTACK_DAMAGE, prefix("ignis_stone_attack_damage"), 0.10D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))));
	public static final Item theCommunity = make(prefix(LibItemNames.THE_COMMUNITY), new CommunityStoneItem(unstackable(),
			List.of(new AttributeBaubleItem.Entry(Attributes.ARMOR, prefix("the_community_armor"), 4.0D, AttributeModifier.Operation.ADD_VALUE),
					new AttributeBaubleItem.Entry(Attributes.ATTACK_DAMAGE, prefix("the_community_attack_damage"), 0.10D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
					new AttributeBaubleItem.Entry(Attributes.MOVEMENT_SPEED, prefix("the_community_movement_speed"), 0.15D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
					new AttributeBaubleItem.Entry(Attributes.FLYING_SPEED, prefix("the_community_flying_speed"), 0.15D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))));
	public static final Item powerGlove = make(prefix(LibItemNames.POWER_GLOVE), new AttributeBaubleItem(unstackable(),
			List.of(new AttributeBaubleItem.Entry(Attributes.ATTACK_SPEED, prefix("power_glove_attack_speed"), 0.12D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL))));
	public static final Item deathRing = make(prefix(LibItemNames.DEATH_RING), new DeathRingItem(unstackable()));
	public static final Item potatoChips = make(prefix(LibItemNames.POTATO_CHIPS), new PotatoChipsItem(unstackable()));
	public static final Item sunRing = make(prefix(LibItemNames.SUN_RING), new SunRingItem(unstackable().rarity(Rarity.EPIC).fireResistant()));
	public static final Item moonPendant = make(prefix(LibItemNames.MOON_PENDANT), new MoonPendantItem(unstackable().rarity(Rarity.EPIC).fireResistant()));
	public static final Item foxEar = make(prefix(LibItemNames.FOX_EAR), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.FOX_EAR, unstackable()));
	public static final Item foxMask = make(prefix(LibItemNames.FOX_MASK), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.FOX_MASK, unstackable()));
	public static final Item pylon = make(prefix(LibItemNames.PYLON), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.PYLON, unstackable()));
	public static final Item goggleJungle = make(prefix(LibItemNames.GOGGLE_JUNGLE), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.GOGGLE, unstackable()));
	public static final Item goggleOcean = make(prefix(LibItemNames.GOGGLE_OCEAN), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.GOGGLE, unstackable()));
	public static final Item goggleSnowfield = make(prefix(LibItemNames.GOGGLE_SNOWFIELD), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.GOGGLE, unstackable()));
	public static final Item goggleStandard = make(prefix(LibItemNames.GOGGLE_STANDARD), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.GOGGLE, unstackable()));
	public static final Item blackGlasses = make(prefix(LibItemNames.BLACK_GLASSES), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.BLACK_GLASSES, unstackable()));
	public static final Item thugLife = make(prefix(LibItemNames.THUG_LIFE), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.THUG_LIFE, unstackable()));
	public static final Item redScarf = make(prefix(LibItemNames.RED_SCARF), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.RED_SCARF, unstackable()));
	public static final Item superCrown = make(prefix(LibItemNames.SUPER_CROWN), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.SUPER_CROWN, unstackable()));
	public static final Item mask = make(prefix(LibItemNames.MASK), new CosmeticBaubleItem(CosmeticBaubleItem.Variant.MASK, unstackable()));

	public static final Item theChaos = make(prefix(LibItemNames.THE_CHAOS), new Item(defaultBuilder()));
	public static final Item theOrigin = make(prefix(LibItemNames.THE_ORIGIN), new Item(defaultBuilder()));
	public static final Item theEnd = make(prefix(LibItemNames.THE_END), new Item(defaultBuilder()));
	public static final Item theUniverse = make(prefix(LibItemNames.THE_UNIVERSE), new Item(defaultBuilder()));

	public static final Item einsRewardBag = make(prefix(LibItemNames.EINS_REWARD_BAG), new RewardBagItem(defaultBuilder(), prefix("eins")));
	public static final Item zweiRewardBag = make(prefix(LibItemNames.ZWEI_REWARD_BAG), new RewardBagItem(defaultBuilder(), prefix("zwei")));
	public static final Item dreiRewardBag = make(prefix(LibItemNames.DREI_REWARD_BAG), new RewardBagItem(defaultBuilder(), prefix("drei")));
	public static final Item vierRewardBag = make(prefix(LibItemNames.VIER_REWARD_BAG), new RewardBagItem(defaultBuilder(), prefix("vier")));
	public static final Item nineAndThreeQuartersRewardBag = make(prefix(LibItemNames.NINE_AND_THREE_QUARTERS_REWARD_BAG), new RewardBagItem(defaultBuilder(), prefix("nine_and_three_quarters")));
	public static final Item limitedEditionSupplyBag = make(prefix(LibItemNames.LIMITED_EDITION_SUPPLY_BAG), new LimitedEditionSupplyBagItem(defaultBuilder().rarity(Rarity.RARE)));
	public static final Item pandorasBox = make(prefix(LibItemNames.PANDORAS_BOX), new PandorasBoxItem(unstackable().rarity(Rarity.UNCOMMON), prefix("pandoras_box")));
	public static final Item candyBag = make(prefix(LibItemNames.CANDY_BAG), new CandyBagItem(defaultBuilder(), prefix("candy")));
	public static final Item candyEins = make(prefix(LibItemNames.CANDY_EINS), new CandyItem(defaultBuilder(), MobEffects.MOVEMENT_SPEED));
	public static final Item candyZwei = make(prefix(LibItemNames.CANDY_ZWEI), new CandyItem(defaultBuilder(), MobEffects.JUMP));
	public static final Item candyDrei = make(prefix(LibItemNames.CANDY_DREI), new CandyItem(defaultBuilder(), MobEffects.DIG_SPEED));

	public static final Item recordGaia3 = make(prefix(LibItemNames.RECORD_GAIA3), new Item(unstackable().rarity(Rarity.EPIC).jukeboxPlayable(ExtraBotanyJukeboxSongs.GAIA_3)));
	public static final Item recordHerrscherOfTheVoid = make(prefix(LibItemNames.RECORD_HERRSCHER_OF_THE_VOID), new Item(unstackable().rarity(Rarity.EPIC).jukeboxPlayable(ExtraBotanyJukeboxSongs.HERRSCHER_OF_THE_VOID)));

	//Brews
	public static final Item manaCocktail = make(prefix(LibItemNames.MANA_COCKTAIL), new ManaCocktailItem(unstackable().craftRemainder(manaGlassBottle), 8, 32, () -> manaGlassBottle));
	public static final Item infiniteWine = make(prefix(LibItemNames.INFINITE_WINE), new InfiniteWineItem(unstackable().rarity(Rarity.RARE).craftRemainder(manaGlassBottle), 12, 18, () -> manaGlassBottle));
	public static final Item holyWaterGrenade = make(prefix(LibItemNames.HOLY_WATER_GRENADE), new HolyWaterGrenadeItem(defaultBuilder().stacksTo(32)));

	//For tags
	public static final Item[] REWARD_BAGS = {
			einsRewardBag, zweiRewardBag, dreiRewardBag, vierRewardBag,
			nineAndThreeQuartersRewardBag, limitedEditionSupplyBag, pandorasBox, candyBag,
	};
	public static final Item[] HAMMERS = {
			manasteelHammer, elementiumHammer, terrasteelHammer, gaiaHammer,
			photoniumHammer, shadowiumHammer, aerialiteHammer, orichalcosHammer,
			rheinHammer
	};
	public static final Item[] PICKAXES = {

	};

	public static final Item[] SWORDS = {
			excalibur, shadowKatana, flamescionWeapon, trueTerrablade, trueShadowKatana, influxWaver, starWrath, firstFractal, spearOfSubspace,
			judahOath, judahOathKira, judahOathSakura
	};
	public static final Item[] BOWS = {
			failnaught
	};
	public static final Item[] SHIELDS = {
			manasteelShield, elementiumShield, terrasteelShield, achillesShield
	};
	public static final Item[] MANA_USING_ITEM = {
			camera, failnaught, excalibur, trueTerrablade, trueShadowKatana, influxWaver, starWrath, firstFractal, spearOfSubspace,
			judahOath, judahOathKira, judahOathSakura,
			featherOfJingwei, magicFinger, rodOfDiscord, bottledStar, bottledPixie, photonShotgun, silverBullet, kingGarden, flamescionWeapon, cosmicCarKey, motor,
			flyingBoat, elementiumFlyingBoat, terrasteelFlyingBoat,
			silentEternity, coreOfTheVoid, voidArchives,
			dispersiveRing, curseRing, pureDaisyPendant, frostStar, deathRing, potatoChips, sunRing, moonPendant,
			starryIdolHeadgear, starryIdolSuit, starryIdolSkirt, starryIdolBoots,
			pleiadesCombatMaidHeadgear, pleiadesCombatMaidSuit, pleiadesCombatMaidSkirt, pleiadesCombatMaidBoots, sanguinePleiadesCombatMaidSuit,
			shadowWarriorHelmet, shadowWarriorChestplate, shadowWarriorLeggings, shadowWarriorBoots,
			goblinSlayerHelmet, goblinSlayerChestplate, goblinSlayerLeggings, goblinSlayerBoots,
			mikuHelm, mikuChest, mikuLegs, mikuBoots,
			shootingGuardianHelm, shootingGuardianChest, shootingGuardianLegs, shootingGuardianBoots,
			silentSagesHelm, silentSagesChest, silentSagesLegs, silentSagesBoots
	};

	public static final Item[] REPLICATOR_BLACKLIST = {
			failnaught, manaRingMaster, excalibur, trueTerrablade, trueShadowKatana, influxWaver, starWrath, firstFractal, spearOfSubspace,
			judahOath, judahOathKira, judahOathSakura, silentEternity
	};
	//Bauble
	public static final Item[] RINGS = {
			manaRingMaster, frostRing, dispersiveRing, curseRing,
			spiderRing, jackieChanRing, parkourRing, sunRing
	};

	public static final Item[] BODY = {
			coreOfTheVoid, redScarf
	};

	public static final Item[] HEAD = {
			foxEar, foxMask, pylon, goggleJungle, goggleOcean, goggleSnowfield, goggleStandard, blackGlasses, thugLife, superCrown, mask
	};

	public static final Item[] ALL_SLOT = {
			featherOfJingwei, natureOrb, motorAccessory, cosmicCarKeyAccessory,
			peaceAmulet, aeroStone, aquaStone, earthStone, ignisStone, theCommunity, powerGlove, frostStar, deathRing, potatoChips, moonPendant, bottledFlame, silentEternity, gemOfConquest
	};

	private static <T extends Item> T make(ResourceLocation id, T item) {
		var old = ALL.put(id, item);
		if (old != null) {
			throw new IllegalArgumentException("Typo? Duplicate id " + id);
		}
		return item;
	}

	public static Item.Properties defaultBuilderCustomDamage() {
		return XplatAbstractions.INSTANCE.defaultItemBuilderWithCustomDamageOnFabric();
	}

	public static Item.Properties unstackableCustomDamage() {
		return defaultBuilderCustomDamage().stacksTo(1);
	}

	public static Item.Properties armorProps(ArmorItem.Type type, int durabilityMultiplier) {
		return unstackableCustomDamage().durability(type.getDurability(durabilityMultiplier));
	}

	public static Item.Properties defaultBuilder() {
		return XplatAbstractions.INSTANCE.defaultItemBuilder();
	}

	private static Item.Properties stackTo16() {
		return defaultBuilder().stacksTo(16);
	}

	private static Item.Properties stackTo4() {
		return defaultBuilder().stacksTo(4);
	}

	private static Item.Properties unstackable() {
		return defaultBuilder().stacksTo(1);
	}

	public static void registerRecipeSerializers(BiConsumer<RecipeSerializer<?>, ResourceLocation> r) {
		r.accept(WandOfTheForestExtendRecipe.SERIALIZER, prefix("wand_of_the_forest_extension"));
		r.accept(CopyBrewFormFlaskRecipe.SERIALIZER, prefix("copy_brew_from_flask"));
		r.accept(CopyBrewFromManaCocktailRecipe.SERIALIZER, prefix("copy_brew_from_mana_cocktail"));
		r.accept(DasRheingoldChangeSoulBoundRecipe.SERIALIZER, prefix("das_rheingold_change_bind"));
		r.accept(PotionLensChangePotionRecipe.SERIALIZER, prefix("potion_lens_change_potion"));
	}

	public static void registerItems(BiConsumer<Item, ResourceLocation> r) {
		for (var e : ALL.entrySet()) {
			r.accept(e.getValue(), e.getKey());
		}
	}

	private static class HiddenLensItem extends LensItem implements CustomCreativeTabContents {

		public HiddenLensItem(Properties builder, Lens lens, int props) {
			super(builder, lens, props);
		}

		@Override
		public void addToCreativeTab(Item me, CreativeModeTab.Output output) {

		}
	}
}
