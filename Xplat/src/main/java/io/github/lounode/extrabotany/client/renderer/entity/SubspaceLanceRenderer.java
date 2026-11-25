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

import org.joml.Matrix4f;

import io.github.lounode.extrabotany.client.model.ExtrabotanyModelLayers;
import io.github.lounode.extrabotany.client.model.SubspaceSpearModel;
import io.github.lounode.extrabotany.common.entity.SubspaceLanceEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class SubspaceLanceRenderer extends EntityRenderer<SubspaceLanceEntity> {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/spearsubspace.png");
	private static final ResourceLocation PORTAL_TEXTURE = prefix("textures/entity/subspace_portal.png");
	private static final int FULL_BRIGHT = 0xF000F0;

	private final SubspaceSpearModel model;

	public SubspaceLanceRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new SubspaceSpearModel(context.bakeLayer(ExtrabotanyModelLayers.SUBSPACE_SPEAR));
	}

	@Override
	public void render(SubspaceLanceEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		float age = entity.tickCount + partialTick;
		float hover = Mth.sin(age * 0.12F) * 0.10F;
		float arrival = Mth.clamp(age / 10.0F, 0.0F, 1.0F);
		float easeArrival = 1.0F - (1.0F - arrival) * (1.0F - arrival) * (1.0F - arrival);
		float pulse = 1.0F + 0.07F * Mth.sin(age * 0.22F);

		// 1. Dramatic towering 3D vertical fullbright Subspace Lance
		poseStack.pushPose();
		poseStack.translate(0.0D, 1.90D + hover, 0.0D);
		poseStack.mulPose(Axis.YP.rotationDegrees(age * 2.5F));
		poseStack.scale(1.15F, 1.15F, 1.15F);
		poseStack.translate(-1.0F / 16.0F, 0.0F, -1.0F / 16.0F);
		VertexConsumer modelVertex = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
		this.model.renderToBuffer(poseStack, modelVertex, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		poseStack.popPose();

		// 2. Rotating glowing Herrscher Subspace Portal Ring flat on the ground beneath the lance
		VertexConsumer portalVertex = buffer.getBuffer(RenderType.entityTranslucent(PORTAL_TEXTURE));

		poseStack.pushPose();
		poseStack.translate(0.0D, 0.04D, 0.0D);
		poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(age * 3.0F));
		float outerRingScale = 2.4F * easeArrival * pulse;
		poseStack.scale(outerRingScale, outerRingScale, outerRingScale);
		drawQuad(poseStack.last().pose(), portalVertex, -0.5F, -0.5F, 0.5F, 0.5F, 0xEEFFFFFF);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0.0D, 0.08D, 0.0D);
		poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(-age * 5.2F + 22.5F));
		float innerRingScale = 1.65F * easeArrival * (1.0F + 0.05F * Mth.cos(age * 0.28F));
		poseStack.scale(innerRingScale, innerRingScale, innerRingScale);
		drawQuad(poseStack.last().pose(), portalVertex, -0.5F, -0.5F, 0.5F, 0.5F, 0xBBFFE082);
		poseStack.popPose();

		// 3. Translucent pulsating golden-violet vertical energy pillar + 3 orbiting void shards
		VertexConsumer glowVertex = buffer.getBuffer(RenderType.lightning());

		// Vertical energy pillar (inner golden core + outer royal void-violet fins)
		float coreHalfW = 0.16F * (0.92F + 0.14F * Mth.sin(age * 0.30F)) * easeArrival;
		float outerHalfW = 0.44F * (0.90F + 0.14F * Mth.cos(age * 0.22F)) * easeArrival;
		int goldBase = ((int) (96.0F * easeArrival) << 24) | 0xFCD34D;
		int goldMid = ((int) (60.0F * easeArrival) << 24) | 0xF59E0B;
		int violetBase = ((int) (78.0F * easeArrival) << 24) | 0xA855F7;
		int violetMid = ((int) (46.0F * easeArrival) << 24) | 0x7E22CE;

		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(age * 2.0F));
		drawPillarBeam(poseStack.last().pose(), glowVertex, coreHalfW, 0.05F, 1.90F, 3.85F, goldBase, goldMid, 0x00FCD34D);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(-age * 1.6F + 45.0F));
		drawPillarBeam(poseStack.last().pose(), glowVertex, outerHalfW, 0.05F, 2.00F, 4.20F, violetBase, violetMid, 0x007E22CE);
		poseStack.popPose();

		// 3 orbiting void shards
		for (int i = 0; i < 3; i++) {
			float theta = age * 0.085F + i * (Mth.TWO_PI / 3.0F);
			float orbitR = (0.95F + 0.10F * Mth.sin(age * 0.18F + i)) * easeArrival;
			float shardX = Mth.cos(theta) * orbitR;
			float shardZ = Mth.sin(theta) * orbitR;
			float shardY = 1.30F + 0.42F * Mth.sin(age * 0.14F + i * 2.1F);

			poseStack.pushPose();
			poseStack.translate(shardX, shardY, shardZ);
			poseStack.mulPose(Axis.YP.rotationDegrees(age * 6.0F + i * 120.0F));
			poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(age * 0.12F + i) * 14.0F));
			Matrix4f shardMatrix = poseStack.last().pose();
			drawVoidShard(shardMatrix, glowVertex, 0.10F, 0.28F, 0xCCFCD34D, 0x22FFFBEB);
			drawVoidShard(shardMatrix, glowVertex, 0.18F, 0.44F, 0x88A855F7, 0x00E879F9);
			poseStack.popPose();
		}

		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	private static void drawQuad(Matrix4f matrix, VertexConsumer vertex, float minX, float minY, float maxX, float maxY, int color) {
		vertex.addVertex(matrix, minX, minY, 0.0F).setColor(color).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
		vertex.addVertex(matrix, maxX, minY, 0.0F).setColor(color).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
		vertex.addVertex(matrix, maxX, maxY, 0.0F).setColor(color).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
		vertex.addVertex(matrix, minX, maxY, 0.0F).setColor(color).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
	}

	private static void drawPillarBeam(Matrix4f matrix, VertexConsumer vertex, float halfW, float yBottom, float yMid, float yTop,
			int bottomColor, int midColor, int topColor) {
		float topW = halfW * 0.35F;
		// Plane 1 (XY)
		vertex.addVertex(matrix, -halfW, yBottom, 0.0F).setColor(bottomColor);
		vertex.addVertex(matrix, halfW, yBottom, 0.0F).setColor(bottomColor);
		vertex.addVertex(matrix, halfW * 0.85F, yMid, 0.0F).setColor(midColor);
		vertex.addVertex(matrix, -halfW * 0.85F, yMid, 0.0F).setColor(midColor);

		vertex.addVertex(matrix, -halfW * 0.85F, yMid, 0.0F).setColor(midColor);
		vertex.addVertex(matrix, halfW * 0.85F, yMid, 0.0F).setColor(midColor);
		vertex.addVertex(matrix, topW, yTop, 0.0F).setColor(topColor);
		vertex.addVertex(matrix, -topW, yTop, 0.0F).setColor(topColor);

		// Plane 2 (ZY)
		vertex.addVertex(matrix, 0.0F, yBottom, -halfW).setColor(bottomColor);
		vertex.addVertex(matrix, 0.0F, yBottom, halfW).setColor(bottomColor);
		vertex.addVertex(matrix, 0.0F, yMid, halfW * 0.85F).setColor(midColor);
		vertex.addVertex(matrix, 0.0F, yMid, -halfW * 0.85F).setColor(midColor);

		vertex.addVertex(matrix, 0.0F, yMid, -halfW * 0.85F).setColor(midColor);
		vertex.addVertex(matrix, 0.0F, yMid, halfW * 0.85F).setColor(midColor);
		vertex.addVertex(matrix, 0.0F, yTop, topW).setColor(topColor);
		vertex.addVertex(matrix, 0.0F, yTop, -topW).setColor(topColor);
	}

	private static void drawVoidShard(Matrix4f matrix, VertexConsumer vertex, float halfW, float halfH, int midColor, int tipColor) {
		float tipW = 0.012F;
		// Plane 1 (XY)
		vertex.addVertex(matrix, -halfW, 0.0F, 0.0F).setColor(midColor);
		vertex.addVertex(matrix, halfW, 0.0F, 0.0F).setColor(midColor);
		vertex.addVertex(matrix, tipW, halfH, 0.0F).setColor(tipColor);
		vertex.addVertex(matrix, -tipW, halfH, 0.0F).setColor(tipColor);

		vertex.addVertex(matrix, -tipW, -halfH, 0.0F).setColor(tipColor);
		vertex.addVertex(matrix, tipW, -halfH, 0.0F).setColor(tipColor);
		vertex.addVertex(matrix, halfW, 0.0F, 0.0F).setColor(midColor);
		vertex.addVertex(matrix, -halfW, 0.0F, 0.0F).setColor(midColor);

		// Plane 2 (ZY)
		vertex.addVertex(matrix, 0.0F, 0.0F, -halfW).setColor(midColor);
		vertex.addVertex(matrix, 0.0F, 0.0F, halfW).setColor(midColor);
		vertex.addVertex(matrix, 0.0F, halfH, tipW).setColor(tipColor);
		vertex.addVertex(matrix, 0.0F, halfH, -tipW).setColor(tipColor);

		vertex.addVertex(matrix, 0.0F, -halfH, -tipW).setColor(tipColor);
		vertex.addVertex(matrix, 0.0F, -halfH, tipW).setColor(tipColor);
		vertex.addVertex(matrix, 0.0F, 0.0F, halfW).setColor(midColor);
		vertex.addVertex(matrix, 0.0F, 0.0F, -halfW).setColor(midColor);
	}

	@Override
	public ResourceLocation getTextureLocation(SubspaceLanceEntity entity) {
		return TEXTURE;
	}
}
