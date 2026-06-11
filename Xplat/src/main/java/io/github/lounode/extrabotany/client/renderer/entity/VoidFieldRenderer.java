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

import org.joml.Matrix4f;

import io.github.lounode.extrabotany.common.entity.HerrscherVoidFieldEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class VoidFieldRenderer extends EntityRenderer<HerrscherVoidFieldEntity> {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/wing.png");

	public VoidFieldRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(HerrscherVoidFieldEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		float age = entity.tickCount + partialTick;
		float pulse = 1.2F + (float) Math.sin(age * 0.25F) * 0.15F;
		int alpha = (int) (80.0F * Math.max(0.0F, 1.0F - age / 60.0F));
		int color = (alpha << 24) | 0x8A5CFF;

		poseStack.pushPose();
		poseStack.translate(0.0D, 1.0D, 0.0D);
		poseStack.scale(pulse, pulse, pulse);
		VertexConsumer vertex = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
		for (int i = 0; i < 3; i++) {
			poseStack.pushPose();
			poseStack.mulPose(Axis.YP.rotationDegrees(age * 3.0F + i * 60.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(90.0F - i * 35.0F));
			quad(poseStack.last().pose(), vertex, color);
			poseStack.popPose();
		}
		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	private static void quad(Matrix4f matrix, VertexConsumer vertex, int color) {
		int a = color >>> 24;
		int r = color >> 16 & 255;
		int g = color >> 8 & 255;
		int b = color & 255;
		vertex.vertex(matrix, -1.5F, -1.5F, 0.0F).color(r, g, b, a).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0F, 0.0F, 1.0F).endVertex();
		vertex.vertex(matrix, 1.5F, -1.5F, 0.0F).color(r, g, b, a).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0F, 0.0F, 1.0F).endVertex();
		vertex.vertex(matrix, 1.5F, 1.5F, 0.0F).color(r, g, b, a).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0F, 0.0F, 1.0F).endVertex();
		vertex.vertex(matrix, -1.5F, 1.5F, 0.0F).color(r, g, b, a).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0F, 0.0F, 1.0F).endVertex();
	}

	@Override
	public ResourceLocation getTextureLocation(HerrscherVoidFieldEntity entity) {
		return TEXTURE;
	}
}
