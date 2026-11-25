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
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import io.github.lounode.extrabotany.common.entity.StrengthenSlashEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class StrengthenSlashRenderer extends EntityRenderer<StrengthenSlashEntity> {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/flamescion_slash.png");
	private static final int FULL_BRIGHT = 0xF000F0;

	public StrengthenSlashRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRender(StrengthenSlashEntity entity, Frustum camera, double camX, double camY, double camZ) {
		return true;
	}

	@Override
	public void render(StrengthenSlashEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		float age = entity.tickCount + partialTick;
		float lifeProgress = Mth.clamp(age / 18.0F, 0.0F, 1.0F);
		float fadeIn = Mth.clamp(age / 2.0F, 0.0F, 1.0F);
		float fadeOut = lifeProgress > 0.72F ? Mth.clamp((1.0F - lifeProgress) / 0.28F, 0.0F, 1.0F) : 1.0F;
		float alphaFactor = fadeIn * fadeOut;
		if (alphaFactor <= 0.01F) {
			return;
		}

		Vec3 velocity = entity.getDeltaMovement();
		float yaw;
		float pitch;
		if (velocity.lengthSqr() > 1.0E-6D) {
			double horizontal = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
			yaw = (float) (Mth.atan2(velocity.x, velocity.z) * Mth.RAD_TO_DEG);
			pitch = (float) (Mth.atan2(velocity.y, horizontal) * Mth.RAD_TO_DEG);
		} else {
			yaw = entity.getSlashYaw();
			pitch = entity.getSlashPitch();
		}

		float widthScale = 3.4F + lifeProgress * 1.1F;
		float lengthScale = 3.0F + lifeProgress * 0.7F;
		int coreAlpha = Mth.clamp((int) (250.0F * alphaFactor), 0, 255);
		int layerAlpha = Mth.clamp((int) (190.0F * alphaFactor), 0, 255);

		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
		poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));

		VertexConsumer vertex = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));

		// Double-layer curved blazing crescent energy wave aligned to flight (+Z)
		poseStack.pushPose();
		poseStack.mulPose(Axis.ZP.rotationDegrees(18.0F));
		poseStack.scale(widthScale, 1.0F, lengthScale);
		drawForwardCrescent(poseStack.last(), vertex, 0.5F, 255, 250, 230, coreAlpha);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.mulPose(Axis.ZP.rotationDegrees(-18.0F));
		poseStack.scale(widthScale * 0.94F, 1.0F, lengthScale * 0.94F);
		drawForwardCrescent(poseStack.last(), vertex, 0.5F, 255, 195, 90, layerAlpha);
		poseStack.popPose();

		// Inner incandescent core blade plane
		poseStack.pushPose();
		poseStack.scale(widthScale * 0.78F, 1.0F, lengthScale * 1.05F);
		drawForwardCrescent(poseStack.last(), vertex, 0.5F, 255, 255, 255, coreAlpha);
		poseStack.popPose();

		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	/**
	 * Maps texture +U (crescent apex) to local +Z (flight direction) so the blazing
	 * cutting edge slices straight ahead along the projectile's trajectory.
	 */
	private static void drawForwardCrescent(PoseStack.Pose pose, VertexConsumer vertex, float half, int r, int g, int b, int a) {
		Matrix4f matrix = pose.pose();
		Matrix3f normal = pose.normal();
		vertex.vertex(matrix, -half, 0.0F, -half).color(r, g, b, a).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
		vertex.vertex(matrix, half, 0.0F, -half).color(r, g, b, a).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
		vertex.vertex(matrix, half, 0.0F, half).color(r, g, b, a).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
		vertex.vertex(matrix, -half, 0.0F, half).color(r, g, b, a).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
	}

	@Override
	public ResourceLocation getTextureLocation(StrengthenSlashEntity entity) {
		return TEXTURE;
	}
}
