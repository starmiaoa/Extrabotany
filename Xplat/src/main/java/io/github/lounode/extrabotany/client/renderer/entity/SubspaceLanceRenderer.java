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

import io.github.lounode.extrabotany.common.entity.SubspaceLanceEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class SubspaceLanceRenderer extends EntityRenderer<SubspaceLanceEntity> {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/spearsubspace.png");

	public SubspaceLanceRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(SubspaceLanceEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		poseStack.pushPose();
		poseStack.translate(0.0D, 1.6D, 0.0D);
		float spin = (entity.tickCount + partialTick) * 4.0F;
		VertexConsumer vertex = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
		for (int i = 0; i < 4; i++) {
			poseStack.pushPose();
			poseStack.mulPose(Axis.YP.rotationDegrees(spin + i * 45.0F));
			quad(poseStack.last().pose(), vertex, -0.35F, -2.2F, 0.35F, 2.2F, 0xDDFFFFFF);
			poseStack.popPose();
		}
		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	private static void quad(Matrix4f matrix, VertexConsumer vertex, float minX, float minY, float maxX, float maxY, int color) {
		int a = color >>> 24;
		int r = color >> 16 & 255;
		int g = color >> 8 & 255;
		int b = color & 255;
		vertex.vertex(matrix, minX, minY, 0.0F).color(r, g, b, a).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0F, 1.0F, 0.0F).endVertex();
		vertex.vertex(matrix, maxX, minY, 0.0F).color(r, g, b, a).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0F, 1.0F, 0.0F).endVertex();
		vertex.vertex(matrix, maxX, maxY, 0.0F).color(r, g, b, a).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0F, 1.0F, 0.0F).endVertex();
		vertex.vertex(matrix, minX, maxY, 0.0F).color(r, g, b, a).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0F, 1.0F, 0.0F).endVertex();
	}

	@Override
	public ResourceLocation getTextureLocation(SubspaceLanceEntity entity) {
		return TEXTURE;
	}
}
