package io.github.lounode.extrabotany.client.core;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import io.github.lounode.extrabotany.api.ExtraBotanyAPI;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class ExtraBotanyModels {
	public static final ResourceLocation TRUE_TERRABLADE_PROJECTILE = model("true_terrablade_projectile");
	public static final ResourceLocation TRUE_SHADOW_KATANA_PROJECTILE = model("true_shadow_katana_projectile");
	public static final ResourceLocation INFLUX_WAVER_PROJECTILE = model("influx_waver_projectile");
	// INSTANCE 必须声明在上面三个 projectile RL 之后:构造器会把它们放进 modelConsumers,
	// 若 INSTANCE 在前,静态初始化顺序导致此时三者仍是 null → modelConsumers 出现 null 键
	// → onModelBake 遍历到它时 standaloneModel(null) 抛 NPE,中断整个烘焙,翅膀全部取不到模型。
	public static final ExtraBotanyModels INSTANCE = new ExtraBotanyModels();
	private final Map<ResourceLocation, Function<BakedModel, BakedModel>> afterBakeModifiers;
	private final Map<ResourceLocation, Consumer<BakedModel>> modelConsumers;
	// 缓存烘焙后的弹刃模型;ModernFix dynamic_resources 下 getModelManager().getModel(standalone)
	// 会返回缺失模型(紫黑块),故在烘焙事件里捕获并由渲染器从这里取。
	private final Map<ResourceLocation, BakedModel> bakedProjectiles;

	public boolean registeredModels = false;

	private ExtraBotanyModels() {
		afterBakeModifiers = new HashMap<>();

		bakedProjectiles = new HashMap<>();
		modelConsumers = new HashMap<>();
		modelConsumers.put(TRUE_TERRABLADE_PROJECTILE, model -> bakedProjectiles.put(TRUE_TERRABLADE_PROJECTILE, model));
		modelConsumers.put(TRUE_SHADOW_KATANA_PROJECTILE, model -> bakedProjectiles.put(TRUE_SHADOW_KATANA_PROJECTILE, model));
		modelConsumers.put(INFLUX_WAVER_PROJECTILE, model -> bakedProjectiles.put(INFLUX_WAVER_PROJECTILE, model));

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
		afterBakeModifiers.forEach((rl, modifier) -> {
			if (rl == null) {
				return;
			}
			ResourceLocation key = map.containsKey(rl) ? rl : standaloneModel(rl);
			map.computeIfPresent(key, (k, baked) -> modifier.apply(baked));
		});
		modelConsumers.forEach((rl, consumer) -> consumer.accept(resolveBaked(map, rl)));
	}

	// 取 RegisterAdditional 模型的 baked 实例。务必"先裸 ResourceLocation,再 standalone MRL":
	//  - ModernFix dynamic_resources 下 map 是惰性 provider,用裸 RL 取会触发 models/<path>.json 的正确加载
	//    (与 Botania MiscellaneousModels 一致,它的 tiara_wing 就这么取);若先用
	//    ModelResourceLocation(rl,"standalone") 取,ModernFix 会把它当 blockstate 解析(找 blockstates/<path>.json)
	//    → ModelMissingException → 返回 missing 模型(紫黑块,这正是律者翅膀炸材质的原因)。
	//  - 无 ModernFix 的普通环境:additional 模型键是 ModelResourceLocation(rl,"standalone"),裸 RL 取不到 → fallback 命中。
	// 绝不遍历 map.keySet():在惰性 provider 上会触发全量烘焙。
	@Nullable
	private static BakedModel resolveBaked(Map<ResourceLocation, BakedModel> map, ResourceLocation rl) {
		if (rl == null) {
			return null;
		}
		BakedModel baked = map.get(rl);
		if (baked != null) {
			return baked;
		}
		return map.get(standaloneModel(rl));
	}

	public BakedModel modifyModelAfterbake(BakedModel bakedModel, ResourceLocation id) {
		modelConsumers.getOrDefault(id, model -> {}).accept(bakedModel);
		return afterBakeModifiers.getOrDefault(id, Function.identity()).apply(bakedModel);
	}

	@Nullable
	public BakedModel getBakedModel(ResourceLocation id) {
		return bakedProjectiles.get(id);
	}

	private static ModelResourceLocation standaloneModel(ResourceLocation id) {
		return new ModelResourceLocation(id, "standalone");
	}

	private static ResourceLocation model(String name) {
		return new ResourceLocation("extrabotany", "icon/" + name);
	}
}
