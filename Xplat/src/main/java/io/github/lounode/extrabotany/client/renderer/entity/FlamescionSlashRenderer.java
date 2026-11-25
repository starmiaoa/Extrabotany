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

import org.joml.Matrix4f;

import io.github.lounode.extrabotany.common.entity.FlamescionSlashEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class FlamescionSlashRenderer extends EntityRenderer<FlamescionSlashEntity> {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/flamescion_slash.png");
	private static final int FULL_BRIGHT = 0xF000F0;

	public FlamescionSlashRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRender(FlamescionSlashEntity entity, Frustum camera, double camX, double camY, double camZ) {
		return true;
	}

	@Override
	public void render(FlamescionSlashEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		float age = entity.tickCount + partialTick;
		float progress = Mth.clamp(age / 10.0F, 0.0F, 1.0F);
		float fadeIn = Mth.clamp(age / 1.5F, 0.0F, 1.0F);
		float fadeOut = 1.0F - progress * progress;
		float alphaFactor = fadeIn * fadeOut;
		if (alphaFactor <= 0.01F) {
			return;
		}

		float scale = 2.1F + progress * 2.1F;
		float sweepAngle = -35.0F + progress * 70.0F;
		int mainAlpha = Mth.clamp((int) (245.0F * alphaFactor), 0, 255);
		int echoAlpha = Mth.clamp((int) (175.0F * alphaFactor), 0, 255);

		poseStack.pushPose();
		poseStack.translate(0.0D, 0.25D, 0.0D);
		poseStack.mulPose(Axis.YP.rotationDegrees(entity.getPitch()));
		poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getRotation()));

		VertexConsumer vertex = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));

		// Primary sweeping blazing crescent plane
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(sweepAngle));
		poseStack.scale(scale, scale, scale);
		drawHorizontalQuad(poseStack.last().pose(), vertex, 0.5F, 255, 245, 220, mainAlpha);
		poseStack.popPose();

		// Secondary crossed tilted plane so the slash reads sharply from every camera angle
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(sweepAngle - 12.0F));
		poseStack.mulPose(Axis.XP.rotationDegrees(48.0F));
		float echoScale = scale * 0.88F;
		poseStack.scale(echoScale, echoScale, echoScale);
		drawHorizontalQuad(poseStack.last().pose(), vertex, 0.5F, 255, 190, 95, echoAlpha);
		poseStack.popPose();

		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	private static void drawHorizontalQuad(Matrix4f matrix, VertexConsumer vertex, float half, int r, int g, int b, int a) {
		vertex.addVertex(matrix, -half, 0.0F, -half).setColor(r, g, b, a).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
		vertex.addVertex(matrix, half, 0.0F, -half).setColor(r, g, b, a).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
		vertex.addVertex(matrix, half, 0.0F, half).setColor(r, g, b, a).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
		vertex.addVertex(matrix, -half, 0.0F, half).setColor(r, g, b, a).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
	}

	@Override
	public ResourceLocation getTextureLocation(FlamescionSlashEntity entity) {
		return TEXTURE;
	}
}
