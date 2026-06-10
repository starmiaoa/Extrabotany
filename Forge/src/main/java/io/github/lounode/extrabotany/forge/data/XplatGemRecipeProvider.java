package io.github.lounode.extrabotany.forge.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class XplatGemRecipeProvider implements DataProvider {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path ROOT = Path.of(System.getProperty("user.dir")).toAbsolutePath().getParent().resolve("Xplat/src/generated/resources");

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		return CompletableFuture.runAsync(() -> {
			try {
				writeGemRecipe(output);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private static void writeGemRecipe(CachedOutput output) throws IOException {
		JsonObject json = new JsonObject();
		json.addProperty("type", "minecraft:crafting_shaped");
		json.addProperty("category", "combat");

		JsonArray pattern = new JsonArray();
		pattern.add("GSG");
		pattern.add("EHE");
		pattern.add("GSG");
		json.add("pattern", pattern);

		JsonObject key = new JsonObject();
		key.add("G", item("botania:gaia_ingot"));
		key.add("H", item("extrabotany:hero_medal"));
		key.add("E", item("extrabotany:the_end"));
		key.add("S", item("extrabotany:sin_rune"));
		json.add("key", key);

		JsonObject result = new JsonObject();
		result.addProperty("item", "extrabotany:gem_of_conquest");
		json.add("result", result);

		Path path = ROOT.resolve("data/extrabotany/recipes/gem_of_conquest.json");
		Files.createDirectories(path.getParent());
		Files.writeString(path, GSON.toJson(json));
	}

	private static JsonObject item(String id) {
		JsonObject json = new JsonObject();
		json.addProperty("item", id);
		return json;
	}

	@Override
	public String getName() {
		return "ExtraBotany Xplat Gem recipes";
	}
}
