package io.github.lounode.extrabotany.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.block.flower.FloatingFlowerBaseBlock;
import vazkii.botania.common.block.flower.SpecialFlowerBlock;
import vazkii.botania.mixin.BlockModelGeneratorsAccessor;

import io.github.lounode.extrabotany.common.block.ExtraBotanyBlocks;
import io.github.lounode.extrabotany.common.block.PedestalBlock;
import io.github.lounode.extrabotany.common.fluid.ExtraBotanyFluids;
import io.github.lounode.extrabotany.common.lib.LibMisc;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;
import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefixBotania;
import static net.minecraft.data.models.model.ModelLocationUtils.getModelLocation;
import static net.minecraft.data.models.model.TextureMapping.getBlockTexture;

public class BlockstateProvider extends vazkii.botania.data.BlockstateProvider {
	public BlockstateProvider(PackOutput packOutput) {
		super(packOutput);
	}

	@NotNull
	@Override
	public String getName() {
		return "ExtraBotany Blockstates and Models";
	}

	@Override
	protected void registerStatesAndModels() {
		Set<Block> remainingBlocks = BuiltInRegistries.BLOCK.stream()
				.filter(b -> LibMisc.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(b).getNamespace()))
				.collect(Collectors.toSet());

		//ManaCharger
		manualModel(remainingBlocks, ExtraBotanyBlocks.manaCharger);
		manualModel(remainingBlocks, ExtraBotanyBlocks.powerFrame);
		manualModel(remainingBlocks, ExtraBotanyBlocks.livingrockBarrel);
		trophy(remainingBlocks);
		cocoonOfDesire(remainingBlocks);
		singleVariantBlockState(ExtraBotanyBlocks.manaBuffer,
				ModelTemplates.CUBE_BOTTOM_TOP.create(ExtraBotanyBlocks.manaBuffer,
						new TextureMapping()
								.put(TextureSlot.SIDE, prefix("block/manabarrel_side"))
								.put(TextureSlot.BOTTOM, prefix("block/manabarrel_bottom"))
								.put(TextureSlot.TOP, prefix("block/manabarrel_top"))
								.putForced(TextureSlot.PARTICLE, prefix("block/manabarrel_side")),
						this.modelOutput));
		remainingBlocks.remove(ExtraBotanyBlocks.manaBuffer);
		singleVariantBlockState(ExtraBotanyBlocks.quantumManaBuffer,
				ModelTemplates.CUBE_BOTTOM_TOP.create(ExtraBotanyBlocks.quantumManaBuffer,
						new TextureMapping()
								.put(TextureSlot.SIDE, prefix("block/quantummanabuffer_side"))
								.put(TextureSlot.BOTTOM, prefix("block/quantummanabuffer_down"))
								.put(TextureSlot.TOP, prefix("block/quantummanabuffer_up"))
								.putForced(TextureSlot.PARTICLE, prefix("block/quantummanabuffer_side")),
						this.modelOutput));
		remainingBlocks.remove(ExtraBotanyBlocks.quantumManaBuffer);
		singleVariantBlockState(ExtraBotanyBlocks.manaGenerator,
				ModelTemplates.CUBE_ALL.create(ExtraBotanyBlocks.manaGenerator,
						TextureMapping.cube(prefix("block/managenerator")),
						this.modelOutput));
		remainingBlocks.remove(ExtraBotanyBlocks.manaGenerator);

		//Pedestal
		var pedestalTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/pedestal")), Optional.empty(),
				TextureSlot.TOP, TextureSlot.UP, TextureSlot.DOWN, TextureSlot.SIDE, TextureSlot.PARTICLE);
		takeAll(remainingBlocks, b -> b instanceof PedestalBlock).forEach(b -> singleVariantBlockState(b,
				pedestalTemplate.create(b, new TextureMapping()
						.put(TextureSlot.TOP, getBlockTexture(b, "_top"))
						.put(TextureSlot.UP, getBlockTexture(b, "_up"))
						.put(TextureSlot.DOWN, getBlockTexture(b, "_down"))
						.put(TextureSlot.SIDE, getBlockTexture(b, "_side"))
						.put(TextureSlot.PARTICLE, getBlockTexture(b, "_down")), this.modelOutput))
		);
		//Quartz
		for (String variant : new String[] { "gaia", "elementium" }) {
			ResourceLocation quartzId = prefix(variant + "_quartz_block");
			Block quartz = BuiltInRegistries.BLOCK.get(quartzId);
			singleVariantBlockState(quartz,
					ModelTemplates.CUBE_BOTTOM_TOP.create(quartz, TextureMapping.cubeBottomTop(quartz), this.modelOutput));

			ResourceLocation pillarId = prefix(variant + "_quartz_pillar");
			Block pillar = BuiltInRegistries.BLOCK.get(pillarId);
			var pillarModel = ModelTemplates.CUBE_COLUMN.create(pillar,
					TextureMapping.column(getBlockTexture(pillar, ""), getBlockTexture(pillar, "_top")),
					this.modelOutput);
			this.blockStateGenerators.add(BlockModelGeneratorsAccessor.botania_createAxisAlignedPillarBlock(pillar, pillarModel));

			ResourceLocation chiseledId = prefix("chiseled_" + variant + "_quartz_block");
			Block chiseled = BuiltInRegistries.BLOCK.get(chiseledId);
			singleVariantBlockState(chiseled,
					ModelTemplates.CUBE_COLUMN.create(chiseled, new TextureMapping()
							.put(TextureSlot.SIDE, getBlockTexture(chiseled, ""))
							.put(TextureSlot.END, getBlockTexture(chiseled, "_top")), this.modelOutput));

			ResourceLocation bricksId = prefix(variant + "_quartz_bricks");
			Block bricks = BuiltInRegistries.BLOCK.get(bricksId);
			singleVariantBlockState(bricks,
					ModelTemplates.CUBE_ALL.create(bricks, TextureMapping.cube(bricks), this.modelOutput));

			ResourceLocation smoothId = prefix("smooth_" + variant + "_quartz");
			Block smooth = BuiltInRegistries.BLOCK.get(smoothId);
			singleVariantBlockState(smooth,
					ModelTemplates.CUBE_ALL.create(smooth, TextureMapping.cube(prefix("block/" + variant + "_quartz_block_bottom")), this.modelOutput));

			remainingBlocks.remove(quartz);
			remainingBlocks.remove(pillar);
			remainingBlocks.remove(chiseled);
			remainingBlocks.remove(bricks);
			remainingBlocks.remove(smooth);
		}

		takeAll(remainingBlocks, b -> b instanceof StairBlock).forEach(b -> {
			String name = BuiltInRegistries.BLOCK.getKey(b).getPath();
			String baseName = name.substring(0, name.length() - vazkii.botania.common.lib.LibBlockNames.STAIRS_SUFFIX.length());
			boolean quartz = name.contains("quartz");
			if (quartz) {
				baseName = baseName + "_block";
				ResourceLocation side = prefix("block/" + baseName + "_side");
				//ResourceLocation bottom = prefix("block/" + baseName + "_bottom");
				ResourceLocation top = prefix("block/" + baseName + "_top");
				boolean smooth = name.contains("smooth");
				if (!smooth) {
					stairsBlock(new HashSet<>(), b, side, top, top);
				} else {
					ResourceLocation bottom = prefix("block/" + baseName.replace("smooth_", "") + "_bottom");
					stairsBlock(new HashSet<>(), b, bottom, bottom, bottom);
				}
			} else {
				var tex = prefix("block/" + baseName);
				stairsBlock(new HashSet<>(), b, tex, tex, tex);
			}
		});

		takeAll(remainingBlocks, b -> b instanceof SlabBlock).forEach(slabBlock -> {
			String name = BuiltInRegistries.BLOCK.getKey(slabBlock).getPath();
			String baseName = name.substring(0, name.length() - vazkii.botania.common.lib.LibBlockNames.SLAB_SUFFIX.length());
			Block base = BuiltInRegistries.BLOCK.get(prefix(baseName));
			boolean quartz = name.contains("quartz");
			if (quartz) {
				base = BuiltInRegistries.BLOCK.get(prefix(baseName.replace("smooth_", "") + "_block"));
				var side = getBlockTexture(base, "_side");
				var bottom = getBlockTexture(base, "_bottom");
				var top = getBlockTexture(base, "_top");
				var doubleModel = getModelLocation(base);
				boolean smooth = name.contains("smooth");
				if (!smooth) {
					slabBlock(new HashSet<>(), slabBlock, doubleModel, side, top, top);
				} else {
					slabBlock(new HashSet<>(), slabBlock, doubleModel, bottom, bottom, bottom);
				}
			} else {
				var baseTex = getBlockTexture(base);
				var doubleModel = getModelLocation(base);
				slabBlock(new HashSet<>(), slabBlock, doubleModel, baseTex, baseTex, baseTex);
			}
		});
		/*
		takeAll(remainingBlocks, b -> b instanceof WallBlock).forEach(wallBlock -> {
			String name = BuiltInRegistries.BLOCK.getKey(wallBlock).getPath();
			String baseName = name.substring(0, name.length() - vazkii.botania.common.lib.LibBlockNames.WALL_SUFFIX.length());
			Block base = BuiltInRegistries.BLOCK.get(prefix(baseName));
			var baseTexture = getBlockTexture(base);
			wallBlock(new HashSet<>(), wallBlock, baseTexture);
		});
		
		*/
		Predicate<Block> flowers = b -> b instanceof SpecialFlowerBlock;
		ModelTemplate crossTemplate = new ModelTemplate(Optional.of(prefixBotania("block/shapes/cross")), Optional.empty(), TextureSlot.CROSS);
		takeAll(remainingBlocks, flowers).forEach(b -> {
			singleVariantBlockState(b, crossTemplate.create(b, TextureMapping.cross(b), this.modelOutput));
		});

		takeAll(remainingBlocks, b -> b instanceof FloatingFlowerBaseBlock).forEach(b -> {
			// Models generated by FloatingFlowerModelProvider
			singleVariantBlockState(b, getModelLocation(b));
		});

		Block fluidedMana = BuiltInRegistries.BLOCK.get(ExtraBotanyFluids.FLUIDED_MANA_ID);
		if (remainingBlocks.remove(fluidedMana)) {
			singleVariantBlockState(fluidedMana,
					ModelTemplates.CUBE_ALL.create(fluidedMana,
							TextureMapping.cube(prefix("block/fluid/fluidedmana_still")),
							this.modelOutput));
		}

		remainingBlocks.forEach(this::cubeAllNoRemove);
	}

	private void trophy(Set<Block> remainingBlocks) {
		ResourceLocation model = getModelLocation(ExtraBotanyBlocks.trophy);
		this.blockStateGenerators.add(MultiVariantGenerator.multiVariant(ExtraBotanyBlocks.trophy)
				.with(PropertyDispatch.property(HorizontalDirectionalBlock.FACING)
						.select(Direction.SOUTH, Variant.variant().with(VariantProperties.MODEL, model))
						.select(Direction.WEST, Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
						.select(Direction.NORTH, Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
						.select(Direction.EAST, Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))));
		remainingBlocks.remove(ExtraBotanyBlocks.trophy);
	}

	private void cocoonOfDesire(Set<Block> remainingBlocks) {
		ResourceLocation model = getModelLocation(ExtraBotanyBlocks.cocoonOfDesire);
		this.modelOutput.accept(model, BlockstateProvider::cocoonOfDesireModel);
		singleVariantBlockState(ExtraBotanyBlocks.cocoonOfDesire, model);
		remainingBlocks.remove(ExtraBotanyBlocks.cocoonOfDesire);
	}

	private static JsonObject cocoonOfDesireModel() {
		JsonObject root = new JsonObject();
		root.addProperty("parent", "minecraft:block/block");

		JsonObject textures = new JsonObject();
		textures.addProperty("bottom", "extrabotany:block/cocoon_top");
		textures.addProperty("top", "extrabotany:block/cocoon_top");
		textures.addProperty("north", "extrabotany:block/cocoon_side");
		textures.addProperty("south", "extrabotany:block/cocoon_side");
		textures.addProperty("west", "extrabotany:block/cocoon_side");
		textures.addProperty("east", "extrabotany:block/cocoon_side");
		textures.addProperty("particle", "extrabotany:block/cocoon_side");
		root.add("textures", textures);

		JsonObject element = new JsonObject();
		element.add("from", jsonArray(3.0F, 0.0F, 3.0F));
		element.add("to", jsonArray(13.0F, 14.0F, 13.0F));

		JsonObject faces = new JsonObject();
		faces.add("west", face("#west", 3.0F, 1.0F, 13.0F, 15.0F));
		faces.add("north", face("#north", 3.0F, 1.0F, 13.0F, 15.0F));
		faces.add("south", face("#south", 3.0F, 1.0F, 13.0F, 15.0F));
		faces.add("east", face("#east", 3.0F, 1.0F, 13.0F, 15.0F));
		faces.add("up", face("#top", 3.0F, 3.0F, 13.0F, 13.0F));
		faces.add("down", face("#bottom", 3.0F, 3.0F, 13.0F, 13.0F));
		element.add("faces", faces);

		JsonArray elements = new JsonArray();
		elements.add(element);
		root.add("elements", elements);
		return root;
	}

	private static JsonObject face(String texture, float u1, float v1, float u2, float v2) {
		JsonObject face = new JsonObject();
		face.addProperty("texture", texture);
		face.add("uv", jsonArray(u1, v1, u2, v2));
		return face;
	}

	private static JsonArray jsonArray(float... values) {
		JsonArray array = new JsonArray();
		for (float value : values) {
			array.add(value);
		}
		return array;
	}
}
