package io.github.lounode.extrabotany.data.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import io.github.lounode.extrabotany.common.lib.ResourceLocationHelper;

import java.util.concurrent.CompletableFuture;

public class CraftingRecipeOverrideProvider implements DataProvider {
	private final PackOutput.PathProvider recipePathProvider;

	public CraftingRecipeOverrideProvider(PackOutput packOutput) {
		this.recipePathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
	}

	@Override
	public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
		ResourceLocation silverBullet = ResourceLocationHelper.prefix("silver_bullet");
		return DataProvider.saveStable(cache, silverBulletRecipe(), recipePathProvider.json(silverBullet));
	}

	private static JsonObject silverBulletRecipe() {
		JsonObject root = new JsonObject();
		root.addProperty("type", "minecraft:crafting_shaped");
		root.addProperty("category", "combat");

		JsonObject key = new JsonObject();
		key.add("C", item("extrabotany:the_chaos"));
		key.add("M", item("botania:mana_blaster"));
		key.add("P", tag("c:ingots/photonium"));
		key.add("S", tag("c:ingots/manasteel"));
		root.add("key", key);

		JsonArray pattern = new JsonArray();
		pattern.add("PPS");
		pattern.add(" MC");
		pattern.add("  P");
		root.add("pattern", pattern);

		JsonObject result = new JsonObject();
		result.addProperty("count", 1);
		result.addProperty("id", "extrabotany:silver_bullet");
		root.add("result", result);
		return root;
	}

	private static JsonObject item(String id) {
		JsonObject json = new JsonObject();
		json.addProperty("item", id);
		return json;
	}

	private static JsonObject tag(String id) {
		JsonObject json = new JsonObject();
		json.addProperty("tag", id);
		return json;
	}

	@Override
	public @NotNull String getName() {
		return "ExtraBotany crafting recipe overrides";
	}
}
