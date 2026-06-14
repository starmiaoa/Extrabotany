package io.github.lounode.extrabotany.client.core;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import io.github.lounode.extrabotany.api.ExtraBotanyAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExtraBotanyModels {
	public static final ExtraBotanyModels INSTANCE = new ExtraBotanyModels();
	public static final ResourceLocation TRUE_TERRABLADE_PROJECTILE = model("true_terrablade_projectile");
	public static final ResourceLocation TRUE_SHADOW_KATANA_PROJECTILE = model("true_shadow_katana_projectile");
	public static final ResourceLocation INFLUX_WAVER_PROJECTILE = model("influx_waver_projectile");
	private final Map<ResourceLocation, Function<BakedModel, BakedModel>> afterBakeModifiers;
	private final Map<ResourceLocation, Consumer<BakedModel>> modelConsumers;

	public boolean registeredModels = false;

	private ExtraBotanyModels() {
		afterBakeModifiers = new HashMap<>();

		modelConsumers = new HashMap<>();
		modelConsumers.put(TRUE_TERRABLADE_PROJECTILE, model -> {});
		modelConsumers.put(TRUE_SHADOW_KATANA_PROJECTILE, model -> {});
		modelConsumers.put(INFLUX_WAVER_PROJECTILE, model -> {});

		for (var variant : ExtraBotanyAPI.instance().getCOVVariants().values()) {
			if (variant instanceof ClientCoreOfTheVoidVariant clientVariant) {
				clientVariant.onModelInit(modelConsumers);
			}
		}

	}

	public void onModelRegister(ResourceManager rm, Consumer<ResourceLocation> consumer) {
		// Defensive: never hand a null id to ModelEvent.RegisterAdditional. A null would reach
		// ModelBakery.getOrLoadModel and NPE under ModernFix's dynamic_resources hook.
		modelConsumers.keySet().stream().filter(Objects::nonNull).forEach(consumer);

		if (!registeredModels) {
			registeredModels = true;
		}
	}

	public void onModelBake(ModelBakery loader, Map<ResourceLocation, BakedModel> map) {
		if (!registeredModels) {
			ExtraBotanyAPI.LOGGER.error("Additional models failed to register! Aborting baking models to avoid early crashing.");
			return;
		}
		afterBakeModifiers.forEach((resourceLocation, afterBakeModifier) -> map.computeIfPresent(resourceLocation, (resourceLoc, bakedModel) -> afterBakeModifier.apply(bakedModel)));
		modelConsumers.forEach((resourceLocation, bakedModelConsumer) -> bakedModelConsumer.accept(map.get(resourceLocation)));
	}

	public BakedModel modifyModelAfterbake(BakedModel bakedModel, ResourceLocation id) {
		modelConsumers.getOrDefault(id, model -> {}).accept(bakedModel);
		return afterBakeModifiers.getOrDefault(id, Function.identity()).apply(bakedModel);
	}

	private static ResourceLocation model(String name) {
		return new ResourceLocation("extrabotany", "icon/" + name);
	}
}
