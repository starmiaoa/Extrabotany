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
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
		poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
		poseStack.mulPose(Axis.ZP.rotationDegrees(45.0F));
		float scale = entity.isFake() ? 0.95F : 1.15F;
		poseStack.scale(scale, scale, scale);
		VertexConsumer vertex = buffer.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
		Matrix4f matrix = poseStack.last().pose();
		quad(matrix, vertex, -0.5F, -0.5F, 0.5F, 0.5F, entity.isFake() ? 0x88FFFFFF : 0xFFFFFFFF);
		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	private static void quad(Matrix4f matrix, VertexConsumer vertex, float minX, float minY, float maxX, float maxY, int color) {
		vertex.addVertex(matrix, minX, minY, 0.0F).setColor(color).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, 1.0F);
		vertex.addVertex(matrix, maxX, minY, 0.0F).setColor(color).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, 1.0F);
		vertex.addVertex(matrix, maxX, maxY, 0.0F).setColor(color).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, 1.0F);
		vertex.addVertex(matrix, minX, maxY, 0.0F).setColor(color).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, 1.0F);
	}

	@Override
	public ResourceLocation getTextureLocation(PhantomSwordEntity entity) {
		return TEXTURES[Math.floorMod(entity.getVariety(), TEXTURES.length)];
	}
}
