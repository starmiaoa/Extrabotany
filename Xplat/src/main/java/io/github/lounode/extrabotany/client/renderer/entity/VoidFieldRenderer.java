package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import org.joml.Matrix4f;

import io.github.lounode.extrabotany.common.entity.HerrscherVoidFieldEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class VoidFieldRenderer extends EntityRenderer<HerrscherVoidFieldEntity> {
	private static final ResourceLocation TEXTURE = prefix("textures/entity/void_field.png");

	public VoidFieldRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRender(HerrscherVoidFieldEntity entity, Frustum frustum, double camX, double camY, double camZ) {
		return true;
	}

	@Override
	public void render(HerrscherVoidFieldEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		float age = entity.tickCount + partialTick;
		float radius = entity.getFieldRadius();
		int mode = entity.getFieldMode();

		if (mode == HerrscherVoidFieldEntity.MODE_TIME_LOCK) {
			renderTimeLock(entity, age, radius, poseStack, buffer);
		} else {
			renderCollapse(entity, age, partialTick, radius, poseStack, buffer);
		}
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	private static void renderTimeLock(HerrscherVoidFieldEntity entity, float age, float radius, PoseStack poseStack, MultiBufferSource buffer) {
		float maxLife = entity.getMaxLife();
		float fadeIn = Mth.clamp(age / 6.0F, 0.0F, 1.0F);
		float fadeOut = Mth.clamp((maxLife - age) / 10.0F, 0.0F, 1.0F);
		float alphaF = fadeIn * fadeOut;
		if (alphaF <= 0.01F) {
			return;
		}
		int alpha = (int) (215.0F * alphaF);
		int groundColor = (alpha << 24) | 0xCFA6FF;
		int innerColor = ((int) (175.0F * alphaF) << 24) | 0xFFE27A;

		VertexConsumer texVc = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
		// Ground rotating clockwork sigil
		poseStack.pushPose();
		poseStack.translate(0.0D, 0.06D, 0.0D);
		poseStack.mulPose(Axis.YP.rotationDegrees(age * 2.2F));
		poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
		float r = radius * (0.94F + 0.04F * Mth.sin(age * 0.22F));
		doubleSidedTexturedQuad(poseStack.last().pose(), texVc, r, groundColor);
		poseStack.popPose();

		// Counter-rotating upper aura sigil
		poseStack.pushPose();
		poseStack.translate(0.0D, 0.16D, 0.0D);
		poseStack.mulPose(Axis.YP.rotationDegrees(-age * 4.0F));
		poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
		doubleSidedTexturedQuad(poseStack.last().pose(), texVc, r * 0.62F, innerColor);
		poseStack.popPose();

		// 3 tilted orbiting temporal rings
		VertexConsumer lightVc = buffer.getBuffer(RenderType.lightning());
		for (int i = 0; i < 3; i++) {
			poseStack.pushPose();
			poseStack.translate(0.0D, 1.05D, 0.0D);
			poseStack.mulPose(Axis.YP.rotationDegrees(age * 3.5F + i * 60.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(62.0F - i * 18.0F));
			drawRing(lightVc, poseStack.last().pose(), radius * 0.72F, 0.08F,
					i == 0 ? 1.0F : 0.74F, i == 0 ? 0.84F : 0.38F, i == 0 ? 0.34F : 1.0F, 0.50F * alphaF, 28);
			poseStack.popPose();
		}
	}

	private static void renderCollapse(HerrscherVoidFieldEntity entity, float age, float partialTick, float radius, PoseStack poseStack, MultiBufferSource buffer) {
		boolean detonated = entity.isDetonated();
		VertexConsumer texVc = buffer.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
		VertexConsumer lightVc = buffer.getBuffer(RenderType.lightning());

		if (!detonated) {
			float prog = entity.getCollapseProgress(partialTick);
			float pulse = 0.85F + 0.15F * Mth.sin(age * (0.45F + prog * 0.9F));
			int outerAlpha = (int) (Mth.lerp(prog, 145.0F, 240.0F) * pulse);
			int outerColor = (outerAlpha << 24) | 0xFF488A;
			int innerColor = (225 << 24) | 0xFFD860;

			// Exact damage-zone boundary sigil on the ground
			poseStack.pushPose();
			poseStack.translate(0.0D, 0.06D, 0.0D);
			poseStack.mulPose(Axis.YP.rotationDegrees(age * (2.5F + prog * 5.0F)));
			poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
			doubleSidedTexturedQuad(poseStack.last().pose(), texVc, radius, outerColor);
			poseStack.popPose();

			// Rapidly shrinking golden-crimson telegraph ring converging to center
			float shrinkR = Mth.lerp(prog, radius, 0.28F);
			poseStack.pushPose();
			poseStack.translate(0.0D, 0.12D, 0.0D);
			poseStack.mulPose(Axis.YP.rotationDegrees(-age * 9.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
			doubleSidedTexturedQuad(poseStack.last().pose(), texVc, shrinkR, innerColor);
			poseStack.popPose();

			// Crisp additive outer danger border + growing singularity core
			poseStack.pushPose();
			poseStack.translate(0.0D, 0.10D, 0.0D);
			drawRing(lightVc, poseStack.last().pose(), radius, 0.14F, 1.0F, 0.22F, 0.52F, 0.65F + prog * 0.25F, 36);
			drawRing(lightVc, poseStack.last().pose(), shrinkR, 0.16F, 1.0F, 0.84F, 0.30F, 0.80F, 32);
			poseStack.popPose();

			poseStack.pushPose();
			poseStack.translate(0.0D, 0.65D, 0.0D);
			poseStack.mulPose(Axis.YP.rotationDegrees(age * 11.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(age * 7.0F));
			drawSphere(lightVc, poseStack.last().pose(), 0.22F + prog * 0.58F, 0.82F, 0.20F, 1.0F, 0.45F + prog * 0.40F);
			poseStack.popPose();
		} else {
			// Post-detonation expanding white-gold shockwave ring
			int fuse = Math.max(10, entity.getMaxLife() - 10);
			float burstT = Mth.clamp((age - fuse) / 10.0F, 0.0F, 1.0F);
			float inv = 1.0F - burstT;
			if (inv <= 0.01F) {
				return;
			}
			poseStack.pushPose();
			poseStack.translate(0.0D, 0.35D, 0.0D);
			drawRing(lightVc, poseStack.last().pose(), radius * (0.45F + burstT * 0.85F), 0.38F * inv, 1.0F, 0.92F, 0.65F, 0.90F * inv, 36);
			poseStack.popPose();

			poseStack.pushPose();
			poseStack.translate(0.0D, 0.65D, 0.0D);
			drawSphere(lightVc, poseStack.last().pose(), radius * (0.25F + burstT * 0.45F), 1.0F, 0.38F, 0.72F, 0.55F * inv);
			poseStack.popPose();
		}
	}

	private static void doubleSidedTexturedQuad(Matrix4f matrix, VertexConsumer vertex, float halfSize, int color) {
		vertex.addVertex(matrix, -halfSize, -halfSize, 0.0F).setColor(color).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, 1.0F);
		vertex.addVertex(matrix, halfSize, -halfSize, 0.0F).setColor(color).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, 1.0F);
		vertex.addVertex(matrix, halfSize, halfSize, 0.0F).setColor(color).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, 1.0F);
		vertex.addVertex(matrix, -halfSize, halfSize, 0.0F).setColor(color).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, 1.0F);

		vertex.addVertex(matrix, -halfSize, halfSize, 0.0F).setColor(color).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, -1.0F);
		vertex.addVertex(matrix, halfSize, halfSize, 0.0F).setColor(color).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, -1.0F);
		vertex.addVertex(matrix, halfSize, -halfSize, 0.0F).setColor(color).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, -1.0F);
		vertex.addVertex(matrix, -halfSize, -halfSize, 0.0F).setColor(color).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, -1.0F);
	}

	private static void drawRing(VertexConsumer vc, Matrix4f mat, float radius, float halfWidth,
			float r, float g, float b, float alpha, int segments) {
		float innerR = Math.max(0.05F, radius - halfWidth);
		float outerR = radius + halfWidth;
		for (int i = 0; i < segments; i++) {
			float a0 = (float) (i * Math.PI * 2.0 / segments);
			float a1 = (float) ((i + 1) * Math.PI * 2.0 / segments);
			float c0 = Mth.cos(a0);
			float s0 = Mth.sin(a0);
			float c1 = Mth.cos(a1);
			float s1 = Mth.sin(a1);
			emitQuad(vc, mat, c0 * innerR, 0, s0 * innerR, c1 * innerR, 0, s1 * innerR, c1 * radius, 0, s1 * radius, c0 * radius, 0, s0 * radius, r, g, b, 0.0F, alpha);
			emitQuad(vc, mat, c0 * radius, 0, s0 * radius, c1 * radius, 0, s1 * radius, c1 * outerR, 0, s1 * outerR, c0 * outerR, 0, s0 * outerR, r, g, b, alpha, 0.0F);
		}
	}

	private static void drawSphere(VertexConsumer vc, Matrix4f mat, float radius, float r, float g, float b, float alpha) {
		int stacks = 5;
		int slices = 10;
		for (int i = 0; i < stacks; i++) {
			float phi0 = (float) (Math.PI * i / stacks - Math.PI * 0.5);
			float phi1 = (float) (Math.PI * (i + 1) / stacks - Math.PI * 0.5);
			float y0 = Mth.sin(phi0) * radius;
			float y1 = Mth.sin(phi1) * radius;
			float rad0 = Mth.cos(phi0) * radius;
			float rad1 = Mth.cos(phi1) * radius;
			for (int j = 0; j < slices; j++) {
				float th0 = (float) (Math.PI * 2.0 * j / slices);
				float th1 = (float) (Math.PI * 2.0 * (j + 1) / slices);
				emitQuad(vc, mat,
						Mth.cos(th0) * rad0, y0, Mth.sin(th0) * rad0,
						Mth.cos(th1) * rad0, y0, Mth.sin(th1) * rad0,
						Mth.cos(th1) * rad1, y1, Mth.sin(th1) * rad1,
						Mth.cos(th0) * rad1, y1, Mth.sin(th0) * rad1,
						r, g, b, alpha, alpha);
			}
		}
	}

	private static void emitQuad(VertexConsumer vc, Matrix4f mat,
			float x0, float y0, float z0,
			float x1, float y1, float z1,
			float x2, float y2, float z2,
			float x3, float y3, float z3,
			float r, float g, float b, float a0, float a1) {
		vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a0);
		vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a0);
		vc.addVertex(mat, x2, y2, z2).setColor(r, g, b, a1);
		vc.addVertex(mat, x3, y3, z3).setColor(r, g, b, a1);

		vc.addVertex(mat, x3, y3, z3).setColor(r, g, b, a1);
		vc.addVertex(mat, x2, y2, z2).setColor(r, g, b, a1);
		vc.addVertex(mat, x1, y1, z1).setColor(r, g, b, a0);
		vc.addVertex(mat, x0, y0, z0).setColor(r, g, b, a0);
	}

	@Override
	public ResourceLocation getTextureLocation(HerrscherVoidFieldEntity entity) {
		return TEXTURE;
	}
}
