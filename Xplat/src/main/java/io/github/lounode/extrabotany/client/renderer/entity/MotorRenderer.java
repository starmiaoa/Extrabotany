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
		this.shadowRadius = 0.8F;
		this.model = new MotorModel(context.bakeLayer(ExtrabotanyModelLayers.MOTOR));
	}

	@Override
	public void render(MotorEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		poseStack.pushPose();
		// Rotate around ground contact center (0, 0, 0) so corner lean & slope pitch stay tire-planted
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw + entity.getCycloneSpin(partialTick)));
		poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getLean(partialTick)));
		poseStack.mulPose(Axis.XP.rotationDegrees(entity.getPitch(partialTick)));
		poseStack.translate(0.0D, 1.5D, 0.0D);
		poseStack.scale(-1.0F, -1.0F, 1.0F);

		float ageInTicks = entity.tickCount + partialTick;
		this.model.setupAnim(entity, partialTick, ageInTicks);

		VertexConsumer vertex = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		this.model.renderBase(poseStack, vertex, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		this.model.renderGlow(poseStack, vertex, OverlayTexture.NO_OVERLAY);

		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	@Override
	public ResourceLocation getTextureLocation(MotorEntity entity) {
		return TEXTURE;
	}
}
