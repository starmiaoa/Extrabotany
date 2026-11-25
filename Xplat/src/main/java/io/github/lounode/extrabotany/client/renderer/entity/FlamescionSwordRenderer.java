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

import io.github.lounode.extrabotany.common.entity.FlamescionSwordEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class FlamescionSwordRenderer extends EntityRenderer<FlamescionSwordEntity> {
	private static final ResourceLocation WEAPON_TEXTURE = prefix("textures/item/flamescion_weapon.png");
	private static final ResourceLocation AURA_TEXTURE = prefix("textures/entity/flamescion_slash.png");
	private static final int FULL_BRIGHT = 0xF000F0;

	public FlamescionSwordRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRender(FlamescionSwordEntity entity, Frustum camera, double camX, double camY, double camZ) {
		return true;
	}

	@Override
	public void render(FlamescionSwordEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		float age = entity.tickCount + partialTick;
		float lifeProgress = Mth.clamp(age / 26.0F, 0.0F, 1.0F);
		float fadeOut = lifeProgress > 0.78F ? Mth.clamp((1.0F - lifeProgress) / 0.22F, 0.0F, 1.0F) : 1.0F;
		if (fadeOut <= 0.01F) {
			return;
		}

		Vec3 velocity = entity.getDeltaMovement();
		float yaw;
		float angleFromUp;
		if (velocity.lengthSqr() > 1.0E-6D) {
			double horizontal = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
			yaw = (float) (Mth.atan2(velocity.x, velocity.z) * Mth.RAD_TO_DEG);
			angleFromUp = (float) (Mth.atan2(horizontal, velocity.y) * Mth.RAD_TO_DEG);
		} else {
			yaw = entity.getFlightYaw();
			angleFromUp = 90.0F - entity.getFlightPitch();
		}

		int swordAlpha = Mth.clamp((int) (250.0F * fadeOut), 0, 255);
		int auraAlpha = Mth.clamp((int) (185.0F * fadeOut), 0, 255);

		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
		poseStack.mulPose(Axis.XP.rotationDegrees(angleFromUp));

		// Glowing fullbright flying greatsword energy projection (crossed planes for full 3D visibility)
		VertexConsumer swordVertex = buffer.getBuffer(RenderType.entityTranslucent(WEAPON_TEXTURE));
		float swordScale = 2.6F;
		for (int i = 0; i < 2; i++) {
			poseStack.pushPose();
			poseStack.mulPose(Axis.YP.rotationDegrees(i * 90.0F));
			poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F));
			poseStack.scale(swordScale, swordScale, swordScale);
			drawQuad(poseStack.last(), swordVertex, -0.5F, -0.5F, 0.5F, 0.5F, 255, 245, 225, swordAlpha);
			poseStack.popPose();
		}

		// Fiery plasma aura halo wrapping the blade tip (+Y in local aligned frame)
		VertexConsumer auraVertex = buffer.getBuffer(RenderType.entityTranslucent(AURA_TEXTURE));
		float auraScale = 2.3F + (float) Math.sin(age * 0.45F) * 0.15F;
		for (int i = 0; i < 2; i++) {
			poseStack.pushPose();
			poseStack.translate(0.0D, 0.35D, 0.0D);
			poseStack.mulPose(Axis.YP.rotationDegrees(i * 90.0F + age * 8.0F));
			poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
			poseStack.scale(auraScale, auraScale, auraScale);
			drawQuad(poseStack.last(), auraVertex, -0.5F, -0.5F, 0.5F, 0.5F, 255, 200, 100, auraAlpha);
			poseStack.popPose();
		}

		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	private static void drawQuad(PoseStack.Pose pose, VertexConsumer vertex, float minX, float minY, float maxX, float maxY, int r, int g, int b, int a) {
		Matrix4f matrix = pose.pose();
		Matrix3f normal = pose.normal();
		vertex.vertex(matrix, minX, minY, 0.0F).color(r, g, b, a).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
		vertex.vertex(matrix, maxX, minY, 0.0F).color(r, g, b, a).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
		vertex.vertex(matrix, maxX, maxY, 0.0F).color(r, g, b, a).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
		vertex.vertex(matrix, minX, maxY, 0.0F).color(r, g, b, a).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
	}

	@Override
	public ResourceLocation getTextureLocation(FlamescionSwordEntity entity) {
		return WEAPON_TEXTURE;
	}
}
