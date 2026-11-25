package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import io.github.lounode.extrabotany.common.entity.FlamescionVoidEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class FlamescionVoidRenderer extends EntityRenderer<FlamescionVoidEntity> {
	private static final ResourceLocation VORTEX_TEXTURE = prefix("textures/entity/flamescion_vortex.png");
	private static final ResourceLocation RING_TEXTURE = prefix("textures/entity/flamescion_ult_ring.png");
	private static final int FULL_BRIGHT = 0xF000F0;

	public FlamescionVoidRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRender(FlamescionVoidEntity entity, Frustum camera, double camX, double camY, double camZ) {
		return true;
	}

	@Override
	public void render(FlamescionVoidEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		float age = entity.tickCount + partialTick;
		float open = Mth.clamp(age / 5.0F, 0.0F, 1.0F);
		float close = age > 44.0F ? Mth.clamp((50.0F - age) / 6.0F, 0.0F, 1.0F) : 1.0F;
		float alphaFactor = open * close;
		if (alphaFactor <= 0.01F) {
			return;
		}

		float pulse = 1.0F + (float) Math.sin(age * 0.35F) * 0.1F;
		float outerScale = (4.8F * open) * pulse;
		float innerScale = (3.4F * open) * (1.0F - (float) Math.sin(age * 0.35F) * 0.08F);

		int mainAlpha = Mth.clamp((int) (235.0F * alphaFactor), 0, 255);
		int innerAlpha = Mth.clamp((int) (195.0F * alphaFactor), 0, 255);
		int billboardAlpha = Mth.clamp((int) (145.0F * alphaFactor), 0, 255);

		poseStack.pushPose();

		VertexConsumer vortexVertex = buffer.getBuffer(RenderType.entityTranslucent(VORTEX_TEXTURE));

		// Layer 1: Primary horizontal blazing Herrscher vortex sigil
		poseStack.pushPose();
		poseStack.translate(0.0D, 0.12D, 0.0D);
		poseStack.mulPose(Axis.YP.rotationDegrees(age * 4.8F));
		poseStack.scale(outerScale, outerScale, outerScale);
		drawHorizontalQuad(poseStack.last(), vortexVertex, 0.5F, 255, 240, 200, mainAlpha);
		poseStack.popPose();

		// Layer 2: Counter-rotating elevated inner sigil
		poseStack.pushPose();
		poseStack.translate(0.0D, 0.42D, 0.0D);
		poseStack.mulPose(Axis.YP.rotationDegrees(-age * 7.2F));
		poseStack.scale(innerScale, innerScale, innerScale);
		drawHorizontalQuad(poseStack.last(), vortexVertex, 0.5F, 255, 165, 65, innerAlpha);
		poseStack.popPose();

		// Layer 3: Camera-facing billboard fiery core so the vortex pops at eye-level
		poseStack.pushPose();
		poseStack.translate(0.0D, 0.72D, 0.0D);
		poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
		poseStack.mulPose(Axis.ZP.rotationDegrees(age * 6.0F));
		float coreScale = 2.6F * open * pulse;
		poseStack.scale(coreScale, coreScale, coreScale);
		drawVerticalQuad(poseStack.last(), vortexVertex, 0.5F, 255, 210, 120, billboardAlpha);
		poseStack.popPose();

		// Final fiery burst shockwave ring (ticks 44-50)
		if (age >= 44.0F) {
			float burstProgress = Mth.clamp((age - 44.0F) / 6.0F, 0.0F, 1.0F);
			float ringScale = 2.0F + burstProgress * 7.5F;
			int ringAlpha = Mth.clamp((int) (240.0F * (1.0F - burstProgress)), 0, 255);
			VertexConsumer ringVertex = buffer.getBuffer(RenderType.entityTranslucent(RING_TEXTURE));
			poseStack.pushPose();
			poseStack.translate(0.0D, 0.35D, 0.0D);
			poseStack.mulPose(Axis.YP.rotationDegrees(age * 5.0F));
			poseStack.scale(ringScale, ringScale, ringScale);
			drawHorizontalQuad(poseStack.last(), ringVertex, 0.5F, 255, 235, 180, ringAlpha);
			poseStack.popPose();
		}

		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	private static void drawHorizontalQuad(PoseStack.Pose pose, VertexConsumer vertex, float half, int r, int g, int b, int a) {
		Matrix4f matrix = pose.pose();
		Matrix3f normal = pose.normal();
		vertex.vertex(matrix, -half, 0.0F, -half).color(r, g, b, a).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
		vertex.vertex(matrix, half, 0.0F, -half).color(r, g, b, a).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
		vertex.vertex(matrix, half, 0.0F, half).color(r, g, b, a).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
		vertex.vertex(matrix, -half, 0.0F, half).color(r, g, b, a).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
	}

	private static void drawVerticalQuad(PoseStack.Pose pose, VertexConsumer vertex, float half, int r, int g, int b, int a) {
		Matrix4f matrix = pose.pose();
		Matrix3f normal = pose.normal();
		vertex.vertex(matrix, -half, -half, 0.0F).color(r, g, b, a).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
		vertex.vertex(matrix, half, -half, 0.0F).color(r, g, b, a).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
		vertex.vertex(matrix, half, half, 0.0F).color(r, g, b, a).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
		vertex.vertex(matrix, -half, half, 0.0F).color(r, g, b, a).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
	}

	@Override
	public ResourceLocation getTextureLocation(FlamescionVoidEntity entity) {
		return VORTEX_TEXTURE;
	}
}
