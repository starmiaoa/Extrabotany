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
import io.github.lounode.extrabotany.client.model.SubspaceSpearModel;
import io.github.lounode.extrabotany.common.entity.SubspaceSpearEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class SubspaceSpearRenderer extends EntityRenderer<SubspaceSpearEntity> {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/spearsubspace.png");
	private final SubspaceSpearModel model;

	public SubspaceSpearRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new SubspaceSpearModel(context.bakeLayer(ExtrabotanyModelLayers.SUBSPACE_SPEAR));
	}

	@Override
	public void render(SubspaceSpearEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		poseStack.pushPose();
		poseStack.translate(0.5F, 1.5F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
		poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
		poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot()));
		poseStack.scale(0.07F, -0.07F, -0.07F);
		VertexConsumer vertex = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		this.model.renderToBuffer(poseStack, vertex, packedLight, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	@Override
	public ResourceLocation getTextureLocation(SubspaceSpearEntity entity) {
		return TEXTURE;
	}
}
