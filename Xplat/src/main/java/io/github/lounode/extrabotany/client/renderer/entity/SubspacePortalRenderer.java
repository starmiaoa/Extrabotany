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
		drawQuad(poseStack.last().pose(), portalVertex, -0.5F, -0.5F, 0.5F, 0.5F, 0.0F, (mainAlpha << 24) | 0xFFFFFF);
		poseStack.popPose();

		// Counter-rotating secondary outer golden ring (front & back depth offset for genuine 3D parallax)
		float outerScale = baseScale * 1.15F;
		float depthOffset = Math.min(0.14F, baseScale * 0.045F);
		int ringAlpha = (int) (155.0F * alphaFactor);
		int ringColor = (ringAlpha << 24) | 0xFFE082;

		poseStack.pushPose();
		poseStack.translate(0.0F, 0.0F, depthOffset);
		poseStack.mulPose(Axis.ZP.rotationDegrees(-age * 5.2F + 22.5F));
		poseStack.scale(outerScale, outerScale, outerScale);
		drawQuad(poseStack.last().pose(), portalVertex, -0.5F, -0.5F, 0.5F, 0.5F, 0.0F, ringColor);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0.0F, 0.0F, -depthOffset);
		poseStack.mulPose(Axis.ZP.rotationDegrees(-age * 5.2F + 22.5F));
		poseStack.scale(outerScale, outerScale, outerScale);
		drawQuad(poseStack.last().pose(), portalVertex, -0.5F, -0.5F, 0.5F, 0.5F, 0.0F, ringColor);
		poseStack.popPose();

		// Additive golden-violet star core flare
		VertexConsumer glowVertex = buffer.getBuffer(RenderType.lightning());
		poseStack.pushPose();
		poseStack.mulPose(Axis.ZP.rotationDegrees(age * 6.5F));
		poseStack.scale(baseScale * 0.52F, baseScale * 0.52F, baseScale * 0.52F);
		int coreAlpha = (int) (115.0F * alphaFactor);
		drawStarCross(poseStack.last().pose(), glowVertex, 0.55F, 0.085F, 0.01F, (coreAlpha << 24) | 0xFCD34D, 0x00A855F7);
		drawStarCross(poseStack.last().pose(), glowVertex, 0.55F, 0.085F, -0.01F, (coreAlpha << 24) | 0xFCD34D, 0x00A855F7);
		poseStack.popPose();

		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	private static void drawQuad(Matrix4f matrix, VertexConsumer vertex, float minX, float minY, float maxX, float maxY, float z, int color) {
		vertex.addVertex(matrix, minX, minY, z).setColor(color).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
		vertex.addVertex(matrix, maxX, minY, z).setColor(color).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
		vertex.addVertex(matrix, maxX, maxY, z).setColor(color).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
		vertex.addVertex(matrix, minX, maxY, z).setColor(color).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
	}

	private static void drawStarCross(Matrix4f matrix, VertexConsumer vertex, float length, float halfWidth, float z, int centerColor, int tipColor) {
		// Horizontal diamond ray
		vertex.addVertex(matrix, -length, 0.0F, z).setColor(tipColor);
		vertex.addVertex(matrix, 0.0F, -halfWidth, z).setColor(centerColor);
		vertex.addVertex(matrix, length, 0.0F, z).setColor(tipColor);
		vertex.addVertex(matrix, 0.0F, halfWidth, z).setColor(centerColor);
		// Vertical diamond ray
		vertex.addVertex(matrix, 0.0F, -length, z).setColor(tipColor);
		vertex.addVertex(matrix, halfWidth, 0.0F, z).setColor(centerColor);
		vertex.addVertex(matrix, 0.0F, length, z).setColor(tipColor);
		vertex.addVertex(matrix, -halfWidth, 0.0F, z).setColor(centerColor);
	}

	@Override
	public ResourceLocation getTextureLocation(SubspaceEntity entity) {
		return TEXTURE;
	}
}
