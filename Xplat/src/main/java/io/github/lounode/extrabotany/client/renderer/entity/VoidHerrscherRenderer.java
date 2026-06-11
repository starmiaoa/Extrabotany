package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import io.github.lounode.extrabotany.client.model.ExtrabotanyModelLayers;
import io.github.lounode.extrabotany.client.model.ModelVoidHerrscher;
import io.github.lounode.extrabotany.common.entity.gaia.VoidHerrscher;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class VoidHerrscherRenderer extends HumanoidMobRenderer<VoidHerrscher, ModelVoidHerrscher> {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/voidherrscher.png");
	private static final ResourceLocation WING_TEXTURE = prefix("textures/entity/wing.png");

	public VoidHerrscherRenderer(EntityRendererProvider.Context context) {
		super(context, new ModelVoidHerrscher(context.bakeLayer(ExtrabotanyModelLayers.VOID_HERRSCHER)), 0.0F);
		addLayer(new WingLayer(this));
		addLayer(new ShieldLayer(this));
	}

	@NotNull
	@Override
	public ResourceLocation getTextureLocation(@NotNull VoidHerrscher entity) {
		return TEXTURE;
	}

	@Override
	protected RenderType getRenderType(VoidHerrscher entity, boolean bodyVisible, boolean translucent, boolean glowing) {
		return RenderType.entityTranslucent(getTextureLocation(entity));
	}

	private static class WingLayer extends RenderLayer<VoidHerrscher, ModelVoidHerrscher> {
		WingLayer(VoidHerrscherRenderer renderer) {
			super(renderer);
		}

		@Override
		public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, VoidHerrscher entity,
				float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
			if (!entity.isRankIIRenderState()) {
				return;
			}
			getParentModel().renderWings(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(WING_TEXTURE)),
					packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		}
	}

	private static class ShieldLayer extends RenderLayer<VoidHerrscher, ModelVoidHerrscher> {
		ShieldLayer(VoidHerrscherRenderer renderer) {
			super(renderer);
		}

		@Override
		public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, VoidHerrscher entity,
				float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
			if (entity.getRotatingShieldsRenderState() <= 0) {
				return;
			}
			getParentModel().renderShields(poseStack, buffer.getBuffer(RenderType.entityTranslucent(WING_TEXTURE)),
					0xF000F0, OverlayTexture.NO_OVERLAY, 0x99FFFFFF);
		}
	}
}
