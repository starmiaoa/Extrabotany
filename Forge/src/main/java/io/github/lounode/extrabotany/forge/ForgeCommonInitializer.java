package io.github.lounode.extrabotany.forge;

import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;

import org.slf4j.Logger;

import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.BotaniaRegistries;
import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaDiscountEvent;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.CustomCreativeTabContents;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;
import vazkii.botania.forge.CapabilityUtil;
import vazkii.botania.forge.integration.curios.CurioIntegration;

import io.github.lounode.extrabotany.api.ExtraBotaniaRegistries;
import io.github.lounode.extrabotany.api.ExtrabotanyForgeCapabilities;
import io.github.lounode.extrabotany.api.item.NatureEnergyItem;
import io.github.lounode.extrabotany.common.advancements.ExtrabotanyCriteriaTriggers;
import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.block_entity.ExtraBotanyBlockEntities;
import io.github.lounode.extrabotany.common.block.block_entity.LivingrockBarrelBlockEntity;
import io.github.lounode.extrabotany.common.block.block_entity.ManaBufferBlockEntity;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyBrews;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.brew.effect.EternityMobEffect;
import io.github.lounode.extrabotany.common.util.HerrscherCombatHelper;
import io.github.lounode.extrabotany.common.crafting.ExtraBotanyRecipeTypes;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyMemoryType;
import io.github.lounode.extrabotany.common.handler.OldExbotanyStatRewardHandler;
import io.github.lounode.extrabotany.common.impl.WindImpl;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.brew.InfiniteWineItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.AquaStoneItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.BaubleBoxSimulatorItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.FeatherOfJingweiItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.MoonPendantItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.NatureOrbItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.PureDaisyPendantItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.SilentEternityItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.SunRingItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.FlamescionWeaponItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.hammer.RheinHammerItem;
import io.github.lounode.extrabotany.common.item.relic.*;
import io.github.lounode.extrabotany.common.item.relic.void_archives.VoidArchivesItem;
import io.github.lounode.extrabotany.common.item.relic.voidcore.CoreOfTheVoidItem;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.common.loot.RewardBagManager;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import io.github.lounode.extrabotany.forge.fluid.ForgeExtraBotanyFluids;
import io.github.lounode.extrabotany.forge.network.ForgePacketHandler;
import io.github.lounode.extrabotany.forge.xplat.LivingrockBarrelFluidProvider;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

@Mod(LibMisc.MOD_ID)
public class ForgeCommonInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();

	public ForgeCommonInitializer() {
		ModLoadingContext context = ModLoadingContext.get();
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

		coreInit(context);
		registryInit(modBus);
		modBus.addListener(this::commonSetup);
	}

	public void commonSetup(FMLCommonSetupEvent evt) {
		ForgePacketHandler.init();
		registerEvents();

		evt.enqueueWork(() -> {
			BiConsumer<ResourceLocation, Supplier<? extends Block>> consumer = (resourceLocation, blockSupplier) -> ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(resourceLocation, blockSupplier);
			ExtraBotanyBlocks.registerFlowerPotPlants(consumer);
			ExtrabotanyFlowerBlocks.registerFlowerPotPlants(consumer);
		});

		//Integration
		if (ModList.get().isLoaded("tconstruc")) {
			//modEventBus.addListener(this::registerTinkersMaterials);
		}
	}

	private void coreInit(ModLoadingContext context) {
		ForgeExtrabotanyConfig.setup(context);
	}

	private void registryInit(IEventBus modEventBus) {
		bind(modEventBus, Registries.SOUND_EVENT, ExtraBotanySounds::init);
		//Block&ItemBlock&Items
		bind(modEventBus, ForgeExtraBotanyFluids.fluidTypeRegistryKey(), ForgeExtraBotanyFluids::registerFluidTypes);
		bind(modEventBus, Registries.FLUID, ForgeExtraBotanyFluids::registerFluids);
		bind(modEventBus, Registries.BLOCK, ForgeExtraBotanyFluids::registerBlocks);
		bindForItems(modEventBus, ForgeExtraBotanyFluids::registerItems);
		bind(modEventBus, Registries.BLOCK, ExtraBotanyBlocks::registerBlocks);
		bindForItems(modEventBus, ExtraBotanyBlocks::registerItemBlocks);
		bind(modEventBus, Registries.BLOCK_ENTITY_TYPE, ExtraBotanyBlockEntities::registerTiles);
		bindForItems(modEventBus, ExtraBotanyItems::registerItems);

		bind(modEventBus, Registries.BLOCK, ExtrabotanyFlowerBlocks::registerBlocks);
		bindForItems(modEventBus, ExtrabotanyFlowerBlocks::registerItemBlocks);
		bind(modEventBus, Registries.BLOCK_ENTITY_TYPE, ExtrabotanyFlowerBlocks::registerTEs);

		//GUI & Recipe
		bind(modEventBus, Registries.RECIPE_SERIALIZER, ExtraBotanyItems::registerRecipeSerializers);
		bind(modEventBus, Registries.RECIPE_TYPE, ExtraBotanyRecipeTypes::submitRecipeTypes);
		bind(modEventBus, Registries.RECIPE_SERIALIZER, ExtraBotanyRecipeTypes::submitRecipeSerializers);

		// Entities
		bind(modEventBus, Registries.ENTITY_TYPE, ExtraBotanyEntityType::registerEntities);
		modEventBus.addListener((EntityAttributeCreationEvent e) -> ExtraBotanyEntityType.registerAttributes((type, builder) -> e.put(type, builder.build())));
		bind(modEventBus, Registries.MEMORY_MODULE_TYPE, ExtraBotanyMemoryType::registerMemories);

		// Potions
		bind(modEventBus, Registries.MOB_EFFECT, ExtraBotanyMobEffects::registerPotions);
		bind(modEventBus, BotaniaRegistries.BREWS, ExtraBotanyBrews::submitRegistrations);

		// Rest
		//registerDatas(modEventBus);

		ExtrabotanyCriteriaTriggers.init();

		//Creative tab
		bind(modEventBus, Registries.CREATIVE_MODE_TAB, consumer -> {
			consumer.accept(CreativeModeTab.builder()
					.title(Component.translatable("itemGroup.extrabotany").withStyle(style -> style.withColor(ChatFormatting.WHITE)))
					.hideTitle()
					.icon(() -> new ItemStack(ExtraBotanyItems.zadkiel))
					//.withTabsBefore(CreativeModeTabs.NATURAL_BLOCKS)
					.backgroundSuffix("extrabotany.png")
					//.withSearchBar()
					.build(),
					ExtraBotaniaRegistries.EXTRA_BOTANIA_TAB_KEY.location());
		});

		modEventBus.addListener((BuildCreativeModeTabContentsEvent e) -> {
			if (e.getTabKey() == ExtraBotaniaRegistries.EXTRA_BOTANIA_TAB_KEY) {
				for (Item item : this.itemsToAddToCreativeTab) {
					if (item instanceof CustomCreativeTabContents cc) {
						cc.addToCreativeTab(item, e);
					} else if (item instanceof BlockItem bi && bi.getBlock() instanceof CustomCreativeTabContents cc) {
						cc.addToCreativeTab(item, e);
					} else {
						e.accept(item);
					}
				}
			}
		});

	}

	private void registerEvents() {
		IEventBus bus = MinecraftForge.EVENT_BUS;
		bus.addGenericListener(ItemStack.class, this::attachItemCaps);
		bus.addGenericListener(Level.class, this::attachLevelCaps);
		bus.addGenericListener(BlockEntity.class, this::attachBlockEntityCaps);

		bus.addListener((TickEvent.LevelTickEvent event) -> WindImpl.EventHandler.onLevelTick(event.level));
		bus.addListener(this::onFinalizeSpawn);
		bus.addListener(this::onManaDiscount);
		bus.addListener(this::onLivingAttack);
		bus.addListener(this::onLivingHeal);
		bus.addListener(this::onPlayerTick);
		bus.addListener(this::onLeftClickBlock);
		bus.addListener(this::onAdvancementEarned);
		bus.addListener((RegisterCommandsEvent event) -> ForgeExtrabotanyCommands.register(event.getDispatcher()));

		RewardBagManager.registerListener();
	}

	private void onLivingAttack(LivingAttackEvent event) {
		if (MoonPendantItem.shouldCancelFireDamage(event.getSource(), event.getEntity())
				|| EternityMobEffect.shouldCancelDamage(event.getEntity())) {
			event.setCanceled(true);
		}
	}

	private void onLivingHeal(LivingHealEvent event) {
		if (SilentEternityItem.shouldCancelHealing(event.getEntity())) {
			event.setAmount(0);
		}
	}

	private void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			HerrscherCombatHelper.sanitizeHealth(event.player);
			EternityMobEffect.onPlayerTick(event.player);
			FlamescionWeaponItem.onPlayerTick(event.player);
			PureDaisyPendantItem.onPlayerTick(event.player);
		}
	}

	private void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		FeatherOfJingweiItem.leftClickBlock(event.getEntity());
	}

	private void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
		if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
			OldExbotanyStatRewardHandler.onAdvancementEarned(serverPlayer, event.getAdvancement().getId());
		}
	}

	private void onManaDiscount(ManaDiscountEvent event) {
		if (AquaStoneItem.hasDiscount(event.getEntityPlayer())) {
			event.setDiscount(event.getDiscount() + AquaStoneItem.MANA_DISCOUNT);
		}
	}

	private void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
		var config = ExtraBotanyConfig.common();
		if (config == null || !config.enableCandyBagMobSpawn()) {
			return;
		}

		var mob = event.getEntity();
		if (!(mob instanceof Zombie || mob instanceof AbstractSkeleton)) {
			return;
		}
		if (!mob.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty()) {
			return;
		}
		if (mob.getRandom().nextDouble() >= config.candyBagMobSpawnChance()) {
			return;
		}

		mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ExtraBotanyItems.candyBag));
		mob.setDropChance(EquipmentSlot.OFFHAND, 1.0F);
	}

	private void attachLevelCaps(AttachCapabilitiesEvent<Level> event) {

	}

	private void attachBlockEntityCaps(AttachCapabilitiesEvent<BlockEntity> event) {
		if (event.getObject() instanceof ManaBufferBlockEntity buffer) {
			event.addCapability(prefix("mana_receiver"),
					CapabilityUtil.makeProvider(BotaniaForgeCapabilities.MANA_RECEIVER, buffer));
			event.addCapability(prefix("spark_attachable"),
					CapabilityUtil.makeProvider(BotaniaForgeCapabilities.SPARK_ATTACHABLE, buffer));
			event.addCapability(prefix("wandable"),
					CapabilityUtil.makeProvider(BotaniaForgeCapabilities.WANDABLE, buffer));
		} else if (event.getObject() instanceof io.github.lounode.extrabotany.common.block.block_entity.ManaGeneratorBlockEntity generator) {
			event.addCapability(prefix("wandable"),
					CapabilityUtil.makeProvider(BotaniaForgeCapabilities.WANDABLE, generator));
		} else if (event.getObject() instanceof io.github.lounode.extrabotany.common.block.block_entity.ManaLiquefactionBlockEntity liquefaction) {
			event.addCapability(prefix("mana_receiver"),
					CapabilityUtil.makeProvider(BotaniaForgeCapabilities.MANA_RECEIVER, liquefaction));
			event.addCapability(prefix("spark_attachable"),
					CapabilityUtil.makeProvider(BotaniaForgeCapabilities.SPARK_ATTACHABLE, liquefaction));
			event.addCapability(prefix("wandable"),
					CapabilityUtil.makeProvider(BotaniaForgeCapabilities.WANDABLE, liquefaction));
		} else if (event.getObject() instanceof LivingrockBarrelBlockEntity barrel) {
			event.addCapability(prefix("elfjar_fluid"), new LivingrockBarrelFluidProvider(barrel));
		}
	}

	private void attachItemCaps(AttachCapabilitiesEvent<ItemStack> e) {
		var stack = e.getObject();

		if (stack.getItem() instanceof BaubleItem
				&& EquipmentHandler.instance instanceof CurioIntegration ci) {
			e.addCapability(prefix("curio"), ci.initCapability(stack));
		}

		var makeManaItem = MANA_ITEM.get().get(stack.getItem());
		if (makeManaItem != null) {
			e.addCapability(prefix("mana_item"),
					CapabilityUtil.makeProvider(BotaniaForgeCapabilities.MANA_ITEM, makeManaItem.apply(stack)));
		}

		var makeNatureEnergyItem = NATURE_ENERGY_ITEM.get().get(stack.getItem());
		if (makeNatureEnergyItem != null) {
			e.addCapability(prefix("nature_energy_item"),
					CapabilityUtil.makeProvider(ExtrabotanyForgeCapabilities.NATURE_ENERGY_ITEM, makeNatureEnergyItem.apply(stack)));
		}

		var makeRelic = RELIC.get().get(stack.getItem());
		if (makeRelic != null) {
			e.addCapability(prefix("relic"),
					CapabilityUtil.makeProvider(BotaniaForgeCapabilities.RELIC, makeRelic.apply(stack)));
		}
	}

	private static final Supplier<Map<Item, Function<ItemStack, NatureEnergyItem>>> NATURE_ENERGY_ITEM = Suppliers.memoize(() -> Map.of(
			ExtraBotanyItems.natureOrb, NatureOrbItem.NatureEnergyImpl::new
	));

	private static final Supplier<Map<Item, Function<ItemStack, ManaItem>>> MANA_ITEM = Suppliers.memoize(() -> Map.of(
			ExtraBotanyItems.manaRingMaster, MasterBandOfManaItem.ExtendManaItemImpl::new,
			ExtraBotanyItems.silentEternity, SilentEternityItem.SilentEternityManaItem::new
	));
	private static final Supplier<Map<Item, Function<ItemStack, Relic>>> RELIC = Suppliers.memoize(() -> Map.ofEntries(
			Map.entry(ExtraBotanyItems.manaRingMaster, MasterBandOfManaItem::makeRelic),
			Map.entry(ExtraBotanyItems.camera, CameraItem::makeRelic),
			Map.entry(ExtraBotanyItems.failnaught, FailnaughtItem::makeRelic),
			Map.entry(ExtraBotanyItems.excalibur, ExcaliburItem::makeRelic),
			Map.entry(ExtraBotanyItems.trueTerrablade, OldExbotanyRelicSwordItem::makeRelic),
			Map.entry(ExtraBotanyItems.trueShadowKatana, OldExbotanyRelicSwordItem::makeRelic),
			Map.entry(ExtraBotanyItems.influxWaver, OldExbotanyRelicSwordItem::makeRelic),
			Map.entry(ExtraBotanyItems.starWrath, OldExbotanyRelicSwordItem::makeRelic),
			Map.entry(ExtraBotanyItems.firstFractal, OldExbotanyRelicSwordItem::makeRelic),
			Map.entry(ExtraBotanyItems.spearOfSubspace, SpearOfSubspaceItem::makeRelic),
			Map.entry(ExtraBotanyItems.judahOath, JudahOathItem::makeRelic),
			Map.entry(ExtraBotanyItems.judahOathKira, JudahOathItem::makeRelic),
			Map.entry(ExtraBotanyItems.judahOathSakura, JudahOathItem::makeRelic),
			Map.entry(ExtraBotanyItems.coreOfTheVoid, CoreOfTheVoidItem::makeRelic),
			Map.entry(ExtraBotanyItems.pandorasBox, PandorasBoxItem::makeRelic),
			Map.entry(ExtraBotanyItems.infiniteWine, InfiniteWineItem::makeRelic),
			Map.entry(ExtraBotanyItems.voidArchives, VoidArchivesItem::makeRelic),
			Map.entry(ExtraBotanyItems.rheinHammer, RheinHammerItem::makeRelic),
			Map.entry(ExtraBotanyItems.achillesShield, AchillesShieldItem::makeRelic),
			Map.entry(ExtraBotanyItems.sunRing, SunRingItem::makeRelic),
			Map.entry(ExtraBotanyItems.moonPendant, MoonPendantItem::makeRelic),
			Map.entry(ExtraBotanyItems.elvenKing, BaubleBoxSimulatorItem::makeRelic),
			Map.entry(ExtraBotanyItems.allForOne, BaubleBoxSimulatorItem::makeRelic),
			Map.entry(ExtraBotanyItems.silentEternity, SilentEternityItem::makeRelic)
	));

	private static <T> void bind(IEventBus modEventBus, ResourceKey<Registry<T>> registry, Consumer<BiConsumer<T, ResourceLocation>> source) {
		modEventBus.addListener((RegisterEvent event) -> {
			if (registry.equals(event.getRegistryKey())) {
				source.accept((t, rl) -> event.register(registry, rl, () -> t));
			}
		});
	}

	private final Set<Item> itemsToAddToCreativeTab = new LinkedHashSet<>();

	private void bindForItems(IEventBus modEventBus, Consumer<BiConsumer<Item, ResourceLocation>> source) {
		modEventBus.addListener((RegisterEvent event) -> {
			if (event.getRegistryKey().equals(Registries.ITEM)) {
				source.accept((t, rl) -> {
					itemsToAddToCreativeTab.add(t);
					event.register(Registries.ITEM, rl, () -> t);
				});
			}
		});
	}
}
