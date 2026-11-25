package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import org.joml.Matrix4f;

import io.github.lounode.extrabotany.common.entity.SubspaceEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class SubspacePortalRenderer extends EntityRenderer<SubspaceEntity> {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/subspace_portal.png");
	private static final int FULL_BRIGHT = 0xF000F0;

	public SubspacePortalRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(SubspaceEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		float openScale = entity.getOpenScale(partialTick);
		if (openScale <= 0.005F) {
			return;
		}

		float age = entity.tickCount + partialTick;
		float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
		float baseScale = Math.max(0.35F, entity.getSize()) * openScale * 2.2F;
		float alphaFactor = Mth.clamp(openScale * 1.15F, 0.0F, 1.0F);

		poseStack.pushPose();
		poseStack.translate(0.0D, -0.5D, 0.0D);
		poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));

		VertexConsumer portalVertex = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));

		// Primary luminous Herrscher rift gate
		poseStack.pushPose();
		poseStack.mulPose(Axis.ZP.rotationDegrees(age * 3.8F));
		poseStack.scale(baseScale, baseScale, baseScale);
		int mainAlpha = (int) (245.0F * alphaFactor);
		drawQuad(poseStack.last().pose(), portalVertex, -0.5F, -0.5F, 0.5F, 0.5F, 0.0F, 255, 255, 255, mainAlpha);
		poseStack.popPose();

		// Counter-rotating secondary outer golden ring (front & back depth offset for genuine 3D parallax)
		float outerScale = baseScale * 1.15F;
		float depthOffset = Math.min(0.14F, baseScale * 0.045F);
		int ringAlpha = (int) (155.0F * alphaFactor);

		poseStack.pushPose();
		poseStack.translate(0.0F, 0.0F, depthOffset);
		poseStack.mulPose(Axis.ZP.rotationDegrees(-age * 5.2F + 22.5F));
		poseStack.scale(outerScale, outerScale, outerScale);
		drawQuad(poseStack.last().pose(), portalVertex, -0.5F, -0.5F, 0.5F, 0.5F, 0.0F, 255, 224, 130, ringAlpha);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0.0F, 0.0F, -depthOffset);
		poseStack.mulPose(Axis.ZP.rotationDegrees(-age * 5.2F + 22.5F));
		poseStack.scale(outerScale, outerScale, outerScale);
		drawQuad(poseStack.last().pose(), portalVertex, -0.5F, -0.5F, 0.5F, 0.5F, 0.0F, 255, 224, 130, ringAlpha);
		poseStack.popPose();

		// Additive golden-violet star core flare
		VertexConsumer glowVertex = buffer.getBuffer(RenderType.lightning());
		poseStack.pushPose();
		poseStack.mulPose(Axis.ZP.rotationDegrees(age * 6.5F));
		poseStack.scale(baseScale * 0.52F, baseScale * 0.52F, baseScale * 0.52F);
		int coreAlpha = (int) (115.0F * alphaFactor);
		drawStarCross(poseStack.last().pose(), glowVertex, 0.55F, 0.085F, 0.01F, 252, 211, 77, coreAlpha, 168, 85, 247, 0);
		drawStarCross(poseStack.last().pose(), glowVertex, 0.55F, 0.085F, -0.01F, 252, 211, 77, coreAlpha, 168, 85, 247, 0);
		poseStack.popPose();

		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	private static void drawQuad(Matrix4f matrix, VertexConsumer vertex, float minX, float minY, float maxX, float maxY, float z, int r, int g, int b, int a) {
		vertex.vertex(matrix, minX, minY, z).color(r, g, b, a).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(0.0F, 1.0F, 0.0F).endVertex();
		vertex.vertex(matrix, maxX, minY, z).color(r, g, b, a).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(0.0F, 1.0F, 0.0F).endVertex();
		vertex.vertex(matrix, maxX, maxY, z).color(r, g, b, a).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(0.0F, 1.0F, 0.0F).endVertex();
		vertex.vertex(matrix, minX, maxY, z).color(r, g, b, a).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(0.0F, 1.0F, 0.0F).endVertex();
	}

	private static void drawStarCross(Matrix4f matrix, VertexConsumer vertex, float length, float halfWidth, float z,
			int cr, int cg, int cb, int ca, int tr, int tg, int tb, int ta) {
		// Horizontal diamond ray
		vertex.vertex(matrix, -length, 0.0F, z).color(tr, tg, tb, ta).endVertex();
		vertex.vertex(matrix, 0.0F, -halfWidth, z).color(cr, cg, cb, ca).endVertex();
		vertex.vertex(matrix, length, 0.0F, z).color(tr, tg, tb, ta).endVertex();
		vertex.vertex(matrix, 0.0F, halfWidth, z).color(cr, cg, cb, ca).endVertex();
		// Vertical diamond ray
		vertex.vertex(matrix, 0.0F, -length, z).color(tr, tg, tb, ta).endVertex();
		vertex.vertex(matrix, halfWidth, 0.0F, z).color(cr, cg, cb, ca).endVertex();
		vertex.vertex(matrix, 0.0F, length, z).color(tr, tg, tb, ta).endVertex();
		vertex.vertex(matrix, -halfWidth, 0.0F, z).color(cr, cg, cb, ca).endVertex();
	}

	@Override
	public ResourceLocation getTextureLocation(SubspaceEntity entity) {
		return TEXTURE;
	}
}
