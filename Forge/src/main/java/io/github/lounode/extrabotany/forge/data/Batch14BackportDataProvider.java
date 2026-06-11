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

public class Batch14BackportDataProvider implements DataProvider {
	private final PackOutput.PathProvider recipes;
	private final PackOutput.PathProvider lootTables;

	public Batch14BackportDataProvider(PackOutput output) {
		this.recipes = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
		this.lootTables = output.createPathProvider(PackOutput.Target.DATA_PACK, "loot_tables");
	}

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		List<CompletableFuture<?>> futures = new ArrayList<>();
		saveRecipe(futures, output, "voidcaller", shaped("minecraft:crafting_shaped", "misc",
				pattern("ASA", "PHP", "ASA"),
				key()
						.tag("S", "extrabotany:shadowium_ingots")
						.tag("P", "extrabotany:photonium_ingots")
						.item("H", "extrabotany:hero_medal")
						.item("A", "botania:life_essence"),
				"extrabotany:voidcaller", 3));
		saveRecipe(futures, output, "core_of_the_void", shaped("minecraft:crafting_shaped", "equipment",
				pattern("QOQ", "QTQ", "QQQ"),
				key()
						.item("Q", "botania:sunny_quartz")
						.tag("O", "extrabotany:orichalcos_ingots")
						.item("T", "extrabotany:empty_core_of_the_void"),
				"extrabotany:core_of_the_void", 1));
		saveLootTable(futures, output, "entities/void_herrscher", voidHerrscherLoot());
		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}

	private void saveRecipe(List<CompletableFuture<?>> futures, CachedOutput output, String name, JsonObject json) {
		Path path = this.recipes.json(new ResourceLocation("extrabotany", name));
		futures.add(DataProvider.saveStable(output, json, path));
	}

	private void saveLootTable(List<CompletableFuture<?>> futures, CachedOutput output, String name, JsonObject json) {
		Path path = this.lootTables.json(new ResourceLocation("extrabotany", name));
		futures.add(DataProvider.saveStable(output, json, path));
	}

	private static JsonObject shaped(String type, String category, JsonArray pattern, KeyBuilder key, String resultId, int count) {
		JsonObject json = new JsonObject();
		json.addProperty("type", type);
		json.addProperty("category", category);
		json.add("key", key.json);
		json.add("pattern", pattern);
		JsonObject result = new JsonObject();
		result.addProperty("item", resultId);
		if (count != 1) {
			result.addProperty("count", count);
		}
		json.add("result", result);
		json.addProperty("show_notification", true);
		return json;
	}

	private static JsonObject voidHerrscherLoot() {
		JsonObject table = lootTable();
		JsonArray pools = table.getAsJsonArray("pools");
		JsonArray killedByPlayer = new JsonArray();
		killedByPlayer.add(condition("botania:killed_by_player"));
		pools.add(pool(killedByPlayer, entries(item("extrabotany:limited_edition_supply_bag", setCount(3)))));
		pools.add(pool(killedByPlayer, entries(item("extrabotany:music_disc_herrscher_of_the_void"))));

		JsonArray coreConditions = new JsonArray();
		coreConditions.add(condition("botania:killed_by_player"));
		coreConditions.add(randomChance(0.02F));
		pools.add(pool(coreConditions, entries(item("extrabotany:core_of_the_void"))));
		table.addProperty("random_sequence", "extrabotany:entities/void_herrscher");
		return table;
	}

	private static JsonObject lootTable() {
		JsonObject json = new JsonObject();
		json.addProperty("type", "minecraft:entity");
		json.add("pools", new JsonArray());
		return json;
	}

	private static JsonObject pool(JsonArray conditions, JsonArray entries) {
		JsonObject json = new JsonObject();
		json.addProperty("bonus_rolls", 0.0F);
		json.add("conditions", conditions);
		json.add("entries", entries);
		json.addProperty("rolls", 1.0F);
		return json;
	}

	private static JsonArray entries(JsonObject... entries) {
		JsonArray json = new JsonArray();
		for (JsonObject entry : entries) {
			json.add(entry);
		}
		return json;
	}

	private static JsonObject item(String item) {
		JsonObject json = new JsonObject();
		json.addProperty("type", "minecraft:item");
		json.addProperty("name", item);
		return json;
	}

	private static JsonObject item(String item, JsonObject function) {
		JsonObject json = item(item);
		JsonArray functions = new JsonArray();
		functions.add(function);
		json.add("functions", functions);
		return json;
	}

	private static JsonObject condition(String condition) {
		JsonObject json = new JsonObject();
		json.addProperty("condition", condition);
		return json;
	}

	private static JsonObject randomChance(float chance) {
		JsonObject json = condition("minecraft:random_chance");
		json.addProperty("chance", chance);
		return json;
	}

	private static JsonObject function(String function) {
		JsonObject json = new JsonObject();
		json.addProperty("function", function);
		return json;
	}

	private static JsonObject setCount(int count) {
		JsonObject json = function("minecraft:set_count");
		json.addProperty("add", false);
		json.addProperty("count", (float) count);
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
		return "ExtraBotany batch 14 backport data";
	}
}
