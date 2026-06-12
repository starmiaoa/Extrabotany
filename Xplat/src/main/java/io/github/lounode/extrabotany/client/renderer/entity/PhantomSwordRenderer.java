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
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

import io.github.lounode.extrabotany.common.entity.PhantomSwordEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class PhantomSwordRenderer extends EntityRenderer<PhantomSwordEntity> {
	private static final ResourceLocation[] TEXTURES = new ResourceLocation[10];

	static {
		for (int i = 0; i < TEXTURES.length; i++) {
			TEXTURES[i] = prefix("textures/item/sworddomain_" + i + ".png");
		}
	}

	public PhantomSwordRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(PhantomSwordEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		// The old version did not render swords that were still waiting on their launch delay.
		if (entity.getDelay() > 0) {
			return;
		}
		float alpha = entity.isFake() ? Math.max(0F, 0.6F - (entity.tickCount + partialTick) * 0.015F) : 1F;
		if (alpha <= 0F) {
			return;
		}
		poseStack.pushPose();
		// Point the sprite's blade (drawn diagonally, rolled onto local +Y) along the flight direction.
		Vec3 velocity = entity.getDeltaMovement();
		float yaw;
		float angleFromUp;
		if (velocity.lengthSqr() > 1.0E-6D) {
			double horizontal = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
			yaw = (float) (Mth.atan2(velocity.x, velocity.z) * Mth.RAD_TO_DEG);
			angleFromUp = (float) (Mth.atan2(horizontal, velocity.y) * Mth.RAD_TO_DEG);
		} else {
			yaw = entity.getYRot();
			angleFromUp = 90F - entity.getXRot();
		}
		poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
		poseStack.mulPose(Axis.XP.rotationDegrees(angleFromUp));
		poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F));
		float scale = 1.5F;
		poseStack.scale(scale, scale, scale);
		VertexConsumer vertex = buffer.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
		Matrix4f matrix = poseStack.last().pose();
		quad(matrix, vertex, -0.5F, -0.5F, 0.5F, 0.5F, ((int) (alpha * 255F) << 24) | 0xFFFFFF);
		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	private static void quad(Matrix4f matrix, VertexConsumer vertex, float minX, float minY, float maxX, float maxY, int color) {
		int a = color >>> 24;
		int r = color >> 16 & 255;
		int g = color >> 8 & 255;
		int b = color & 255;
		vertex.vertex(matrix, minX, minY, 0.0F).color(r, g, b, a).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0F, 0.0F, 1.0F).endVertex();
		vertex.vertex(matrix, maxX, minY, 0.0F).color(r, g, b, a).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0F, 0.0F, 1.0F).endVertex();
		vertex.vertex(matrix, maxX, maxY, 0.0F).color(r, g, b, a).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0F, 0.0F, 1.0F).endVertex();
		vertex.vertex(matrix, minX, maxY, 0.0F).color(r, g, b, a).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0F, 0.0F, 1.0F).endVertex();
	}

	@Override
	public ResourceLocation getTextureLocation(PhantomSwordEntity entity) {
		return TEXTURES[Math.floorMod(entity.getVariety(), TEXTURES.length)];
	}
}
