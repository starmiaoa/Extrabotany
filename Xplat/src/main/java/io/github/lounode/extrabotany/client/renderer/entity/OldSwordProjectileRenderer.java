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

import io.github.lounode.extrabotany.common.entity.OldSwordProjectileEntity;

public class OldSwordProjectileRenderer<T extends OldSwordProjectileEntity> extends EntityRenderer<T> {
	private final ResourceLocation texture;

	public OldSwordProjectileRenderer(EntityRendererProvider.Context context, ResourceLocation texture) {
		super(context);
		this.texture = texture;
	}

	@Override
	public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
		poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
		poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F));
		poseStack.scale(1.15F, 1.15F, 1.15F);
		VertexConsumer vertex = buffer.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
		Matrix4f matrix = poseStack.last().pose();
		quad(matrix, vertex);
		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	private static void quad(Matrix4f matrix, VertexConsumer vertex) {
		vertex.addVertex(matrix, -0.5F, -0.5F, 0.0F).setColor(0xFFFFFFFF).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, 1.0F);
		vertex.addVertex(matrix, 0.5F, -0.5F, 0.0F).setColor(0xFFFFFFFF).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, 1.0F);
		vertex.addVertex(matrix, 0.5F, 0.5F, 0.0F).setColor(0xFFFFFFFF).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, 1.0F);
		vertex.addVertex(matrix, -0.5F, 0.5F, 0.0F).setColor(0xFFFFFFFF).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, 1.0F);
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return texture;
	}
}
