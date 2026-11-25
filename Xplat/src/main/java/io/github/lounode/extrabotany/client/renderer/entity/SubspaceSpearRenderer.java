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

import io.github.lounode.extrabotany.client.model.ExtrabotanyModelLayers;
import io.github.lounode.extrabotany.client.model.SubspaceSpearModel;
import io.github.lounode.extrabotany.common.entity.SubspaceSpearEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class SubspaceSpearRenderer extends EntityRenderer<SubspaceSpearEntity> {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/spearsubspace.png");
	private static final ResourceLocation PORTAL_TEXTURE = prefix("textures/entity/subspace_portal.png");
	private static final int FULL_BRIGHT = 0xF000F0;

	private final SubspaceSpearModel model;

	public SubspaceSpearRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new SubspaceSpearModel(context.bakeLayer(ExtrabotanyModelLayers.SUBSPACE_SPEAR));
	}

	@Override
	public void render(SubspaceSpearEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		Vec3 vel = entity.getDeltaMovement();
		float yawRad;
		float pitchRad;
		if (vel.lengthSqr() > 1.0E-5D) {
			double horiz = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
			yawRad = (float) Mth.atan2(vel.x, vel.z);
			pitchRad = (float) Mth.atan2(vel.y, horiz);
		} else {
			yawRad = Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) * Mth.DEG_TO_RAD;
			pitchRad = Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) * Mth.DEG_TO_RAD;
		}

		float age = entity.tickCount + partialTick;

		poseStack.pushPose();
		poseStack.translate(0.0D, 0.25D, 0.0D);
		// Align local +Z with exact flight trajectory
		poseStack.mulPose(Axis.YP.rotation(yawRad));
		poseStack.mulPose(Axis.XP.rotation(-pitchRad));

		// 1. Render crisp fullbright 3D Subspace Spear model (tip at model -Y maps to local +Z via XP(-90))
		poseStack.pushPose();
		poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(age * 10.0F));
		poseStack.scale(0.78F, 0.78F, 0.78F);
		poseStack.translate(-1.0F / 16.0F, 0.0F, -1.0F / 16.0F);
		VertexConsumer modelVertex = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		this.model.renderToBuffer(poseStack, modelVertex, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		poseStack.popPose();

		// 2. Translucent golden-violet diamond blade aura sheath along the spear shaft
		VertexConsumer auraVertex = buffer.getBuffer(RenderType.lightning());
		poseStack.pushPose();
		poseStack.mulPose(Axis.ZP.rotationDegrees(age * 14.0F));
		Matrix4f auraMatrix = poseStack.last().pose();
		// Inner radiant golden core sheath
		drawBladeAuraCross(auraMatrix, auraVertex, 1.28F, 0.12F, -1.16F, 0.15F, 0x70FFFBEB, 0x9AFCD34D, 0x00F59E0B);
		// Outer royal void-violet spatial energy sheath
		drawBladeAuraCross(auraMatrix, auraVertex, 1.38F, 0.05F, -1.28F, 0.27F, 0x40E879F9, 0x6AA855F7, 0x007E22CE);
		poseStack.popPose();

		// 3. Two fast-spinning miniature golden sonic rings trailing behind the spearhead
		VertexConsumer ringVertex = buffer.getBuffer(RenderType.entityTranslucent(PORTAL_TEXTURE));

		poseStack.pushPose();
		poseStack.translate(0.0F, 0.0F, -0.32F);
		poseStack.mulPose(Axis.ZP.rotationDegrees(age * 28.0F));
		drawSonicRing(poseStack.last().pose(), ringVertex, 0.42F, 0xCCFFE082);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0.0F, 0.0F, -0.82F);
		poseStack.mulPose(Axis.ZP.rotationDegrees(-age * 36.0F));
		drawSonicRing(poseStack.last().pose(), ringVertex, 0.28F, 0xAAEEB4FF);
		poseStack.popPose();

		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	private static void drawBladeAuraCross(Matrix4f matrix, VertexConsumer vertex, float zTip, float zMid, float zTail,
			float halfW, int tipColor, int midColor, int tailColor) {
		float endW = 0.015F;
		// Horizontal plane (XZ)
		vertex.addVertex(matrix, -endW, 0.0F, zTip).setColor(tipColor);
		vertex.addVertex(matrix, -halfW, 0.0F, zMid).setColor(midColor);
		vertex.addVertex(matrix, halfW, 0.0F, zMid).setColor(midColor);
		vertex.addVertex(matrix, endW, 0.0F, zTip).setColor(tipColor);

		vertex.addVertex(matrix, -halfW, 0.0F, zMid).setColor(midColor);
		vertex.addVertex(matrix, -endW, 0.0F, zTail).setColor(tailColor);
		vertex.addVertex(matrix, endW, 0.0F, zTail).setColor(tailColor);
		vertex.addVertex(matrix, halfW, 0.0F, zMid).setColor(midColor);

		// Vertical plane (YZ)
		vertex.addVertex(matrix, 0.0F, -endW, zTip).setColor(tipColor);
		vertex.addVertex(matrix, 0.0F, -halfW, zMid).setColor(midColor);
		vertex.addVertex(matrix, 0.0F, halfW, zMid).setColor(midColor);
		vertex.addVertex(matrix, 0.0F, endW, zTip).setColor(tipColor);

		vertex.addVertex(matrix, 0.0F, -halfW, zMid).setColor(midColor);
		vertex.addVertex(matrix, 0.0F, -endW, zTail).setColor(tailColor);
		vertex.addVertex(matrix, 0.0F, endW, zTail).setColor(tailColor);
		vertex.addVertex(matrix, 0.0F, halfW, zMid).setColor(midColor);
	}

	private static void drawSonicRing(Matrix4f matrix, VertexConsumer vertex, float radius, int color) {
		vertex.addVertex(matrix, -radius, -radius, 0.0F).setColor(color).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
		vertex.addVertex(matrix, radius, -radius, 0.0F).setColor(color).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
		vertex.addVertex(matrix, radius, radius, 0.0F).setColor(color).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
		vertex.addVertex(matrix, -radius, radius, 0.0F).setColor(color).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
	}

	@Override
	public ResourceLocation getTextureLocation(SubspaceSpearEntity entity) {
		return TEXTURE;
	}
}
