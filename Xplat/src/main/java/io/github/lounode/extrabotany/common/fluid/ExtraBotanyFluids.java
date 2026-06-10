package io.github.lounode.extrabotany.common.fluid;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public final class ExtraBotanyFluids {
	public static final String FLUIDED_MANA = "fluidedmana";
	public static final String FLOWING_FLUIDED_MANA = "flowing_fluidedmana";
	public static final String FLUIDED_MANA_BUCKET = "fluidedmana_bucket";
	public static final ResourceLocation FLUIDED_MANA_ID = prefix(FLUIDED_MANA);
	public static final ResourceLocation FLOWING_FLUIDED_MANA_ID = prefix(FLOWING_FLUIDED_MANA);
	public static final ResourceLocation FLUIDED_MANA_BUCKET_ID = prefix(FLUIDED_MANA_BUCKET);

	private static Fluid fluidedMana = Fluids.EMPTY;

	private ExtraBotanyFluids() {}

	public static Fluid fluidedMana() {
		return fluidedMana;
	}

	public static void setFluidedMana(Fluid fluid) {
		fluidedMana = fluid == null ? Fluids.EMPTY : fluid;
	}
}
