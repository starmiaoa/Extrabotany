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
import io.github.lounode.extrabotany.client.model.MotorModel;
import io.github.lounode.extrabotany.common.entity.MotorEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class MotorRenderer extends EntityRenderer<MotorEntity> {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/motor.png");
	private final MotorModel model;

	public MotorRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new MotorModel(context.bakeLayer(ExtrabotanyModelLayers.MOTOR));
	}

	@Override
	public void render(MotorEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		poseStack.pushPose();
		poseStack.translate(0.0D, 1.5D, 0.0D);
		poseStack.mulPose(Axis.YP.rotationDegrees(180F - entityYaw));
		poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getLean()));
		poseStack.mulPose(Axis.XP.rotationDegrees(entity.getPitch()));
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		VertexConsumer vertex = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		this.model.setupAnim(entity, 0F, 0F, entity.tickCount + partialTick, 0F, 0F);
		this.model.renderToBuffer(poseStack, vertex, packedLight, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	@Override
	public ResourceLocation getTextureLocation(MotorEntity entity) {
		return TEXTURE;
	}
}
