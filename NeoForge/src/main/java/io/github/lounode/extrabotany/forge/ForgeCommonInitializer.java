package io.github.lounode.extrabotany.forge;

import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import org.slf4j.Logger;

import vazkii.botania.api.neoforge.BotaniaNeoForgeCapabilities;
import vazkii.botania.api.BotaniaRegistries;
import vazkii.botania.api.block.Wandable;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSparkAttachable;
import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.CustomCreativeTabContents;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;
import vazkii.botania.neoforge.integration.curios.CurioIntegration;

import io.github.lounode.extrabotany.api.ExtraBotaniaRegistries;
import io.github.lounode.extrabotany.api.ExtrabotanyForgeCapabilities;
import io.github.lounode.extrabotany.api.item.NatureEnergyItem;
import io.github.lounode.extrabotany.common.block.block_entity.LivingrockBarrelBlockEntity;
import io.github.lounode.extrabotany.common.advancements.ExtrabotanyCriteriaTriggers;
import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.block_entity.ExtraBotanyBlockEntities;
import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyBrews;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.crafting.ExtraBotanyRecipeTypes;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyEntityType;
import io.github.lounode.extrabotany.common.entity.ExtraBotanyMemoryType;
import io.github.lounode.extrabotany.forge.fluid.NeoForgeExtraBotanyFluids;
import io.github.lounode.extrabotany.common.impl.WindImpl;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.brew.InfiniteWineItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.BaubleBoxSimulatorItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.NatureOrbItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.MoonPendantItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.SilentEternityItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.SunRingItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.hammer.RheinHammerItem;
import io.github.lounode.extrabotany.common.item.material.ArmorsMaterial;
import io.github.lounode.extrabotany.common.item.relic.*;
import io.github.lounode.extrabotany.common.item.relic.void_archives.VoidArchivesItem;
import io.github.lounode.extrabotany.common.item.relic.voidcore.CoreOfTheVoidItem;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.common.loot.RewardBagManager;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import io.github.lounode.extrabotany.forge.network.ForgePacketHandler;
import io.github.lounode.extrabotany.forge.event.NeoForgeEventBridge;
import io.github.lounode.extrabotany.forge.xplat.LivingrockBarrelFluidHandler;
import net.neoforged.neoforge.capabilities.Capabilities;

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

	public ForgeCommonInitializer(IEventBus modBus, ModContainer modContainer) {
		coreInit(modContainer);
		registryInit(modBus);
		modBus.register(this);
		modBus.addListener(ForgePacketHandler::registerPayloadHandlers);
	}

	@SubscribeEvent
	public void commonSetup(FMLCommonSetupEvent evt) {
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

	private void coreInit(ModContainer modContainer) {
		ForgeExtrabotanyConfig.setup(modContainer);
	}

	private void registryInit(IEventBus modEventBus) {
		bind(modEventBus, Registries.SOUND_EVENT, ExtraBotanySounds::init);
		bindRegistry(modEventBus, Registries.ARMOR_MATERIAL, ArmorsMaterial::registerArmorMaterials);
		//Block&ItemBlock&Items
		bind(modEventBus, NeoForgeExtraBotanyFluids.fluidTypeRegistryKey(), NeoForgeExtraBotanyFluids::registerFluidTypes);
		bind(modEventBus, Registries.FLUID, NeoForgeExtraBotanyFluids::registerFluids);
		bind(modEventBus, Registries.BLOCK, NeoForgeExtraBotanyFluids::registerBlocks);
		bindForItems(modEventBus, NeoForgeExtraBotanyFluids::registerItems);
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
		bindRegistry(modEventBus, Registries.MOB_EFFECT, ExtraBotanyMobEffects::registerPotions);
		bind(modEventBus, BotaniaRegistries.BREWS, ExtraBotanyBrews::submitRegistrations);

		// Rest
		//registerDatas(modEventBus);

		bind(modEventBus, Registries.TRIGGER_TYPE, ExtrabotanyCriteriaTriggers::init);

		//Creative tab
		bind(modEventBus, Registries.CREATIVE_MODE_TAB, consumer -> {
			consumer.accept(CreativeModeTab.builder()
					.title(Component.translatable("itemGroup.extrabotany").withStyle(style -> style.withColor(ChatFormatting.WHITE)))
					.hideTitle()
					.icon(() -> new ItemStack(ExtraBotanyItems.zadkiel))
					//.withTabsBefore(CreativeModeTabs.NATURAL_BLOCKS)
					.backgroundTexture(prefix("textures/gui/container/creative_inventory/tab_extrabotany.png"))
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
		IEventBus bus = NeoForge.EVENT_BUS;

		bus.addListener((LevelTickEvent.Post event) -> WindImpl.EventHandler.onLevelTick(event.getLevel()));
		NeoForgeEventBridge.register(bus);
		bus.addListener((RegisterCommandsEvent event) -> ForgeExtrabotanyCommands.register(event.getDispatcher()));

		RewardBagManager.registerListener();
	}

	@SubscribeEvent
	private void attachCapabilities(RegisterCapabilitiesEvent e) {
		attachItemCaps(e);
		attachBlockCaps(e);
	}

	private void attachBlockCaps(RegisterCapabilitiesEvent e) {
		e.registerBlockEntity(blockApi(ManaReceiver.LOOKUP), ExtraBotanyBlockEntities.MANA_BUFFER, (be, direction) -> be);
		e.registerBlockEntity(blockApi(ManaSparkAttachable.LOOKUP), ExtraBotanyBlockEntities.MANA_BUFFER, (be, ignored) -> be);
		e.registerBlockEntity(blockApi(Wandable.LOOKUP), ExtraBotanyBlockEntities.MANA_BUFFER, (be, direction) -> be);
		e.registerBlockEntity(blockApi(ManaReceiver.LOOKUP), ExtraBotanyBlockEntities.QUANTUM_MANA_BUFFER, (be, direction) -> be);
		e.registerBlockEntity(blockApi(ManaSparkAttachable.LOOKUP), ExtraBotanyBlockEntities.QUANTUM_MANA_BUFFER, (be, ignored) -> be);
		e.registerBlockEntity(blockApi(Wandable.LOOKUP), ExtraBotanyBlockEntities.QUANTUM_MANA_BUFFER, (be, direction) -> be);
		e.registerBlockEntity(blockApi(Wandable.LOOKUP), ExtraBotanyBlockEntities.MANA_GENERATOR, (be, direction) -> be);
		e.registerBlockEntity(blockApi(ManaReceiver.LOOKUP), ExtraBotanyBlockEntities.MANA_LIQUEFACTION, (be, direction) -> be);
		e.registerBlockEntity(blockApi(ManaSparkAttachable.LOOKUP), ExtraBotanyBlockEntities.MANA_LIQUEFACTION, (be, ignored) -> be);
		e.registerBlockEntity(blockApi(Wandable.LOOKUP), ExtraBotanyBlockEntities.MANA_LIQUEFACTION, (be, direction) -> be);
		e.registerBlockEntity(Capabilities.FluidHandler.BLOCK, ExtraBotanyBlockEntities.LIVINGROCK_BARREL,
				(LivingrockBarrelBlockEntity be, Direction direction) -> new LivingrockBarrelFluidHandler(be));
	}

	private void attachItemCaps(RegisterCapabilitiesEvent e) {
		if (EquipmentHandler.instance instanceof CurioIntegration ci) {
			Item[] baubleItems = BuiltInRegistries.ITEM.stream()
					.filter(item -> item instanceof BaubleItem)
					.toArray(Item[]::new);
			ci.initCapability(e, baubleItems);
		}

		attachMappedItemCaps(e, BotaniaNeoForgeCapabilities.getItemApiLookupById(ManaItem.LOOKUP), MANA_ITEM.get());
		attachMappedItemCaps(e, ExtrabotanyForgeCapabilities.NATURE_ENERGY_ITEM, NATURE_ENERGY_ITEM.get());
		attachMappedItemCaps(e, BotaniaNeoForgeCapabilities.getItemApiLookupById(Relic.LOOKUP), RELIC.get());
	}

	private static <T> void attachMappedItemCaps(RegisterCapabilitiesEvent e, ItemCapability<T, Void> capability,
			Map<Item, Function<ItemStack, T>> itemProviderMap) {
		itemProviderMap.forEach((item, provider) -> e.registerItem(
				capability, (stack, context) -> provider.apply(stack), item));
	}

	private static <A, C> BlockCapability<A, C> blockApi(vazkii.botania.api.capability.BlockApiWithContext<A, C> api) {
		return BlockCapability.create(api.getId(), api.getApiClass(), api.getContextClass());
	}

	private static <A> BlockCapability<A, Void> blockApi(vazkii.botania.api.capability.BlockApiNoContext<A> api) {
		return BlockCapability.create(api.getId(), api.getApiClass(), Void.class);
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

	private static <T> void bindRegistry(IEventBus modEventBus, ResourceKey<Registry<T>> registryKey, Consumer<Registry<T>> source) {
		modEventBus.addListener((RegisterEvent event) -> {
			Registry<T> registry = event.getRegistry(registryKey);
			if (registry != null) {
				source.accept(registry);
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
