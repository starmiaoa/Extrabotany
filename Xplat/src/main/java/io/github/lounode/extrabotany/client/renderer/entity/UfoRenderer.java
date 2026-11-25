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

import io.github.lounode.extrabotany.client.model.ExtrabotanyModelLayers;
import io.github.lounode.extrabotany.client.model.UfoModel;
import io.github.lounode.extrabotany.common.entity.UfoEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class UfoRenderer extends EntityRenderer<UfoEntity> {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/ufo.png");
	private final UfoModel model;

	public UfoRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.9F;
		this.model = new UfoModel(context.bakeLayer(ExtrabotanyModelLayers.UFO));
	}

	@Override
	public void render(UfoEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		float ageInTicks = entity.tickCount + partialTick;
		float hoverBob = Mth.sin(ageInTicks * 0.12F) * 0.06F;

		poseStack.pushPose();
		// Pivot banking around saucer center of mass (Y = 0.45 + bob); total Y translation = 1.50 + bob
		poseStack.translate(0.0D, 0.45D + hoverBob, 0.0D);
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
		poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getBankRoll(partialTick)));
		poseStack.mulPose(Axis.XP.rotationDegrees(entity.getBankPitch(partialTick)));
		poseStack.translate(0.0D, 1.05D, 0.0D);
		poseStack.scale(-1.2F, -1.2F, 1.2F);

		this.model.setupAnim(entity, 0.0F, 0.0F, ageInTicks, 0.0F, 0.0F);

		// 1. Solid armored saucer hull + pulsing fullbright Martian beacons & thrusters
		VertexConsumer solidBuffer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		this.model.renderHull(poseStack, solidBuffer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		this.model.renderGlow(poseStack, solidBuffer, OverlayTexture.NO_OVERLAY);

		// 2. Dynamic semi-transparent abduction tractor beam (rendered only while carrying an entity)
		if (entity.hasCaughtEntity()) {
			VertexConsumer beamBuffer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
			this.model.renderTractorBeam(poseStack, beamBuffer, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, this.model.getBeamAlpha());
		}

		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	@Override
	public ResourceLocation getTextureLocation(UfoEntity entity) {
		return TEXTURE;
	}
}
