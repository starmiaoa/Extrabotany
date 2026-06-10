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

import io.github.lounode.extrabotany.client.model.ExtrabotanyModelLayers;
import io.github.lounode.extrabotany.client.model.UfoModel;
import io.github.lounode.extrabotany.common.entity.UfoEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class UfoRenderer extends EntityRenderer<UfoEntity> {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/ufo.png");
	private final UfoModel model;

	public UfoRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new UfoModel(context.bakeLayer(ExtrabotanyModelLayers.UFO));
	}

	@Override
	public void render(UfoEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		poseStack.pushPose();
		poseStack.translate(0.0D, 2.5D, 0.0D);
		poseStack.mulPose(Axis.YP.rotationDegrees(180F - entityYaw));
		poseStack.scale(-1.35F, -1.35F, 1.35F);
		VertexConsumer vertex = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		this.model.setupAnim(entity, 0F, 0F, entity.tickCount + partialTick, 0F, 0F);
		this.model.renderToBuffer(poseStack, vertex, packedLight, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	@Override
	public ResourceLocation getTextureLocation(UfoEntity entity) {
		return TEXTURE;
	}
}
