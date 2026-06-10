package io.github.lounode.extrabotany.forge.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class XplatFlamescionDamageTypeProvider implements DataProvider {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path ROOT = Path.of(System.getProperty("user.dir")).toAbsolutePath().getParent().resolve("Xplat/src/generated/resources");
	private static final String FLAMESCION_FLAME = "extrabotany:flamescion_flame";
	private static final List<String> TAGS = List.of(
			"bypasses_armor",
			"bypasses_effects",
			"bypasses_enchantments",
			"bypasses_resistance",
			"bypasses_shield",
			"is_fire"
	);

	@Override
	public CompletableFuture<?> run(CachedOutput output) {
		return CompletableFuture.runAsync(() -> {
			try {
				writeDamageType(output);
				for (String tag : TAGS) {
					addToTag(output, tag);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private static void writeDamageType(CachedOutput output) throws IOException {
		JsonObject json = new JsonObject();
		json.addProperty("exhaustion", 0.1F);
		json.addProperty("message_id", "extrabotany.flamescion_flame");
		json.addProperty("scaling", "when_caused_by_living_non_player");
		DataProvider.saveStable(output, json, ROOT.resolve("data/extrabotany/damage_type/flamescion_flame.json"));
	}

	private static void addToTag(CachedOutput output, String tag) throws IOException {
		Path path = ROOT.resolve("data/minecraft/tags/damage_type/" + tag + ".json");
		JsonObject json;
		if (Files.exists(path)) {
			json = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
		} else {
			json = new JsonObject();
			json.addProperty("replace", false);
			json.add("values", new JsonArray());
		}
		JsonArray values = json.getAsJsonArray("values");
		for (int i = 0; i < values.size(); i++) {
			if (FLAMESCION_FLAME.equals(values.get(i).getAsString())) {
				DataProvider.saveStable(output, json, path);
				return;
			}
		}
		values.add(FLAMESCION_FLAME);
		DataProvider.saveStable(output, json, path);
	}

	@Override
	public String getName() {
		return "ExtraBotany Xplat Flamescion damage type";
	}
}
