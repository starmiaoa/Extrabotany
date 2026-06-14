package io.github.lounode.extrabotany.forge.mixin.client;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Compatibility guard for ModernFix's {@code dynamic_resources} optimisation.
 * <p>
 * ModernFix injects at the HEAD of {@code ModelBakery#getModel(ResourceLocation)} (its handler is
 * named {@code getOrLoadModelDynamic}) and immediately calls
 * {@code modelLocation.equals(MISSING_MODEL_LOCATION)}, which throws an NPE when the location is
 * {@code null}. A {@code null} can reach {@code getModel} while the extra models we register via
 * {@code ModelEvent.RegisterAdditional} are walked inside a {@code ModelBakery} constructor that
 * other coremods (e.g. Voidscape) have rewritten. The vanilla method already treats
 * {@code MISSING_MODEL_LOCATION} as a terminal, so substituting it for a {@code null} key is safe
 * and non-recursive.
 * <p>
 * Priority 1100 (&gt; ModernFix's default 1000) makes this HEAD callback run first, so the guard
 * short-circuits before ModernFix dereferences the null. Client-only; see the {@code client}
 * array in {@code extrabotany_forge.mixins.json}.
 */
@Mixin(value = ModelBakery.class, priority = 1100)
public abstract class ModelBakeryNullGuardMixin {

	@Shadow
	@Final
	public static ModelResourceLocation MISSING_MODEL_LOCATION;

	@Shadow
	public abstract UnbakedModel getModel(ResourceLocation modelLocation);

	@Inject(method = "getModel(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/resources/model/UnbakedModel;", at = @At("HEAD"), cancellable = true)
	private void extrabotany$guardNullModelLocation(ResourceLocation modelLocation, CallbackInfoReturnable<UnbakedModel> cir) {
		if (modelLocation == null) {
			cir.setReturnValue(this.getModel(MISSING_MODEL_LOCATION));
		}
	}
}
