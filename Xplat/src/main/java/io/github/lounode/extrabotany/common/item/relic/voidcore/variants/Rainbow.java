package io.github.lounode.extrabotany.common.item.relic.voidcore.variants;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import io.github.lounode.extrabotany.api.item.ClientCoreOfTheVoidVariant;

import java.util.Map;
import java.util.function.Consumer;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

/**
 * The "Rainbow" wings (originally ExtraMeteorP's {@code elfwing}, meta 2 of the old Core of God).
 * Each side is built from {@value #LAYERS} progressively smaller, fanned-out wing layers,
 * reproducing the old multi-layered rainbow look.
 */
public class Rainbow implements ClientCoreOfTheVoidVariant {

	private static final String ID = "rainbow";
	private static final int LAYERS = 7;

	private final ResourceLocation[] models = new ResourceLocation[LAYERS];
	private final BakedModel[] wings = new BakedModel[LAYERS];

	public Rainbow() {
		for (int i = 0; i < LAYERS; i++) {
			models[i] = prefix("wing/rainbow_" + (i + 1));
		}
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void onModelInit(Map<ResourceLocation, Consumer<BakedModel>> consumer) {
		for (int i = 0; i < LAYERS; i++) {
			int idx = i;
			consumer.put(models[i], bakedModel -> this.wings[idx] = bakedModel);
		}
	}

	@Override
	public void render(HumanoidModel<?> bipedModel, ItemStack stack, LivingEntity living, PoseStack ms, MultiBufferSource buffers, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		for (BakedModel wing : wings) {
			if (wing == null) {
				return;
			}
		}

		boolean flying = living instanceof Player player && player.getAbilities().flying;
		float flap = 12F + (float) ((Math.sin((double) (living.tickCount + partialTicks) * (flying ? 0.2F : 0.12F)) + 0.4F) * (flying ? 30F : 5F));
		flap *= 0.25F;

		ms.pushPose();

		// attach to body
		bipedModel.body.translateAndRotate(ms);

		// position on body
		ms.translate(0, 0.2, 0.2);

		for (int side = 0; side < 2; side++) {
			ms.pushPose();
			ms.mulPose(Axis.YP.rotationDegrees(side == 0 ? flap : 180 - flap));

			// move so flapping about the edge instead of center of texture
			ms.translate(-1, 0, 0);

			// fan out the layers, each smaller than the last
			for (int layer = 0; layer < LAYERS; layer++) {
				ms.pushPose();
				ms.mulPose(Axis.ZP.rotationDegrees(-8F * layer));
				ms.mulPose(Axis.XP.rotationDegrees(1.6F * layer));

				float scale = 1.5F * (float) Math.pow(0.85F, layer);
				ms.scale(scale, -scale, -scale);
				Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.NONE, false, ms, buffers, light, OverlayTexture.NO_OVERLAY, wings[layer]);
				ms.popPose();
			}

			ms.popPose();
		}

		ms.popPose();
	}
}
