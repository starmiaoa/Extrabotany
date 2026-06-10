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
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.ForgeRegistries;

import io.github.lounode.extrabotany.common.fluid.ExtraBotanyFluids;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public final class ForgeExtraBotanyFluids {
	private static FluidType fluidedManaType;
	private static FlowingFluid fluidedMana;
	private static FlowingFluid flowingFluidedMana;
	private static ManaLiquidBlock fluidedManaBlock;
	private static Item fluidedManaBucket;

	private ForgeExtraBotanyFluids() {}

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
		return ForgeRegistries.Keys.FLUID_TYPES;
	}

	public static ResourceLocation stillTexture() {
		return prefix("block/fluid/fluidedmana_still");
	}

	public static ResourceLocation flowingTexture() {
		return prefix("block/fluid/fluidedmana_flow");
	}

	private static FluidType fluidedManaType() {
		if (fluidedManaType == null) {
			fluidedManaType = new ManaFluidType(FluidType.Properties.create()
					.lightLevel(12)
					.temperature(100)
					.viscosity(1200));
		}
		return fluidedManaType;
	}

	private static FlowingFluid sourceFluid() {
		if (fluidedMana == null) {
			fluidedMana = new ForgeFlowingFluid.Source(fluidedManaProperties());
		}
		return fluidedMana;
	}

	private static FlowingFluid flowingFluid() {
		if (flowingFluidedMana == null) {
			flowingFluidedMana = new ForgeFlowingFluid.Flowing(fluidedManaProperties());
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
					ForgeExtraBotanyFluids::sourceFluid,
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

	private static ForgeFlowingFluid.Properties fluidedManaProperties() {
		return new ForgeFlowingFluid.Properties(
				ForgeExtraBotanyFluids::fluidedManaType,
				ForgeExtraBotanyFluids::sourceFluid,
				ForgeExtraBotanyFluids::flowingFluid)
						.bucket(ForgeExtraBotanyFluids::bucket)
						.block(ForgeExtraBotanyFluids::fluidBlock)
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

	private static class ManaFluidType extends FluidType {
		private ManaFluidType(Properties properties) {
			super(properties);
		}

		@Override
		public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
			consumer.accept(new IClientFluidTypeExtensions() {
				@Override
				public ResourceLocation getStillTexture() {
					return stillTexture();
				}

				@Override
				public ResourceLocation getFlowingTexture() {
					return flowingTexture();
				}
			});
		}
	}
}
