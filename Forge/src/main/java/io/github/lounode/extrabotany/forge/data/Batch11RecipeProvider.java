package io.github.lounode.extrabotany.forge.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Batch11RecipeProvider implements DataProvider {
	private final PackOutput.PathProvider recipes;

	public Batch11RecipeProvider(PackOutput output) {
		this.recipes = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		List<CompletableFuture<?>> futures = new ArrayList<>();
		save(futures, output, "photon_shotgun", shaped("minecraft:crafting_shaped", "equipment",
				pattern("PPD", "SM ", "PPD"),
				key()
						.tag("P", "extrabotany:photonium_ingots")
						.item("M", "botania:mana_gun")
						.tag("D", "botania:mana_diamond_gems")
						.tag("S", "botania:manasteel_ingots"),
				"extrabotany:photon_shotgun"));
		save(futures, output, "thug_life", shaped("minecraft:crafting_shaped", "equipment",
				pattern("P P", "SGS", "P P"),
				key()
						.tag("P", "botania:petals/black")
						.item("G", "extrabotany:black_glasses")
						.item("S", "minecraft:paper"),
				"extrabotany:thug_life"));
		save(futures, output, "uuz_fan", shaped("minecraft:crafting_shaped", "equipment",
				pattern("PEP", "WOW", " W "),
				key()
						.item("O", "extrabotany:the_origin")
						.tag("E", "botania:elementium_ingots")
						.item("P", "botania:pixie_dust")
						.item("W", "botania:manaweave_cloth"),
				"extrabotany:uuz_fan"));
		save(futures, output, "flamescion_weapon", shaped("minecraft:crafting_shaped", "equipment",
				pattern("OUE", "BTL", "B O"),
				key()
						.tag("O", "extrabotany:orichalcos_ingots")
						.item("U", "extrabotany:the_universe")
						.item("E", "extrabotany:the_end")
						.item("B", "minecraft:blaze_rod")
						.item("T", "botania:terra_sword")
						.item("L", "botania:life_essence"),
				"extrabotany:flamescion_weapon"));
		saveSilentSages(futures, output, "silentsages_helm", "miku_helm");
		saveSilentSages(futures, output, "silentsages_chest", "miku_chest");
		saveSilentSages(futures, output, "silentsages_legs", "miku_legs");
		saveSilentSages(futures, output, "silentsages_boots", "miku_boots");
		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}

	private void saveSilentSages(List<CompletableFuture<?>> futures, CachedOutput output, String result, String base) {
		save(futures, output, result, shaped("botania:mana_upgrade", "equipment",
				pattern("OOO", "OAO", "OOO"),
				key()
						.tag("O", "extrabotany:orichalcos_ingots")
						.item("A", "extrabotany:" + base),
				"extrabotany:" + result));
	}

	private void save(List<CompletableFuture<?>> futures, CachedOutput output, String name, JsonObject json) {
		Path path = this.recipes.json(new ResourceLocation("extrabotany", name));
		futures.add(DataProvider.saveStable(output, json, path));
	}

	private static JsonObject shaped(String type, String category, JsonArray pattern, KeyBuilder key, String resultId) {
		JsonObject json = new JsonObject();
		json.addProperty("type", type);
		json.addProperty("category", category);
		json.add("key", key.json);
		json.add("pattern", pattern);
		JsonObject result = new JsonObject();
		result.addProperty("item", resultId);
		json.add("result", result);
		json.addProperty("show_notification", true);
		return json;
	}

	private static JsonArray pattern(String... rows) {
		JsonArray pattern = new JsonArray();
		for (String row : rows) {
			pattern.add(row);
		}
		return pattern;
	}

	private static KeyBuilder key() {
		return new KeyBuilder();
	}

	private static final class KeyBuilder {
		private final JsonObject json = new JsonObject();

		private KeyBuilder item(String symbol, String id) {
			JsonObject value = new JsonObject();
			value.addProperty("item", id);
			this.json.add(symbol, value);
			return this;
		}

		private KeyBuilder tag(String symbol, String id) {
			JsonObject value = new JsonObject();
			value.addProperty("tag", id);
			this.json.add(symbol, value);
			return this;
		}
	}

	@Override
	public String getName() {
		return "ExtraBotany batch 11 recipes";
	}
}
