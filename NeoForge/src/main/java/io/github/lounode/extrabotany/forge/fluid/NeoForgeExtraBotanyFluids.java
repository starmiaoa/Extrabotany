package io.github.lounode.extrabotany.forge.fluid;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import io.github.lounode.extrabotany.common.fluid.ExtraBotanyFluids;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public final class NeoForgeExtraBotanyFluids {
	private static FluidType fluidedManaType;
	private static FlowingFluid fluidedMana;
	private static FlowingFluid flowingFluidedMana;
	private static ManaLiquidBlock fluidedManaBlock;
	private static Item fluidedManaBucket;

	private NeoForgeExtraBotanyFluids() {}

	public static void registerFluidTypes(BiConsumer<FluidType, ResourceLocation> consumer) {
		consumer.accept(fluidedManaType(), ExtraBotanyFluids.FLUIDED_MANA_ID);
	}

	public static void registerFluids(BiConsumer<Fluid, ResourceLocation> consumer) {
		consumer.accept(sourceFluid(), ExtraBotanyFluids.FLUIDED_MANA_ID);
		consumer.accept(flowingFluid(), ExtraBotanyFluids.FLOWING_FLUIDED_MANA_ID);
		ExtraBotanyFluids.setFluidedMana(sourceFluid());
	}

	public static void registerBlocks(BiConsumer<Block, ResourceLocation> consumer) {
		consumer.accept(fluidBlock(), ExtraBotanyFluids.FLUIDED_MANA_ID);
	}

	public static void registerItems(BiConsumer<Item, ResourceLocation> consumer) {
		consumer.accept(bucket(), ExtraBotanyFluids.FLUIDED_MANA_BUCKET_ID);
	}

	public static ResourceKey<Registry<FluidType>> fluidTypeRegistryKey() {
		return NeoForgeRegistries.Keys.FLUID_TYPES;
	}

	public static ResourceLocation stillTexture() {
		return prefix("block/fluid/fluidedmana_still");
	}

	public static ResourceLocation flowingTexture() {
		return prefix("block/fluid/fluidedmana_flow");
	}

	public static FluidType fluidedManaType() {
		if (fluidedManaType == null) {
			fluidedManaType = new FluidType(FluidType.Properties.create()
					.lightLevel(12)
					.temperature(100)
					.viscosity(1200));
		}
		return fluidedManaType;
	}

	private static FlowingFluid sourceFluid() {
		if (fluidedMana == null) {
			fluidedMana = new BaseFlowingFluid.Source(fluidedManaProperties());
		}
		return fluidedMana;
	}

	private static FlowingFluid flowingFluid() {
		if (flowingFluidedMana == null) {
			flowingFluidedMana = new BaseFlowingFluid.Flowing(fluidedManaProperties());
		}
		return flowingFluidedMana;
	}

	private static Item bucket() {
		if (fluidedManaBucket == null) {
			fluidedManaBucket = new BucketItem(
					sourceFluid(),
					new Item.Properties()
							.craftRemainder(Items.BUCKET)
							.stacksTo(1));
		}
		return fluidedManaBucket;
	}

	private static LiquidBlock fluidBlock() {
		if (fluidedManaBlock == null) {
			fluidedManaBlock = new ManaLiquidBlock(
					() -> sourceFluid(),
					BlockBehaviour.Properties.of()
							.mapColor(MapColor.COLOR_PURPLE)
							.replaceable()
							.noCollission()
							.strength(100.0F)
							.lightLevel(state -> 12)
							.noLootTable()
							.liquid());
		}
		return fluidedManaBlock;
	}

	private static BaseFlowingFluid.Properties fluidedManaProperties() {
		return new BaseFlowingFluid.Properties(
				NeoForgeExtraBotanyFluids::fluidedManaType,
				NeoForgeExtraBotanyFluids::sourceFluid,
				NeoForgeExtraBotanyFluids::flowingFluid)
						.bucket(NeoForgeExtraBotanyFluids::bucket)
						.block(NeoForgeExtraBotanyFluids::fluidBlock)
						.slopeFindDistance(4)
						.levelDecreasePerBlock(1)
						.explosionResistance(100.0F)
						.tickRate(5);
	}

	public static class ManaLiquidBlock extends LiquidBlock {
		public ManaLiquidBlock(Supplier<? extends FlowingFluid> fluid, Properties properties) {
			super(fluid.get(), properties);
		}
	}
}
