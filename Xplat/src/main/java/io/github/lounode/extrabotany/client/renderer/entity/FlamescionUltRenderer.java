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

import org.joml.Matrix3f;
import org.joml.Matrix4f;

import io.github.lounode.extrabotany.common.entity.FlamescionUltEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class FlamescionUltRenderer extends EntityRenderer<FlamescionUltEntity> {
	private static final ResourceLocation VORTEX_TEXTURE = prefix("textures/entity/flamescion_vortex.png");
	private static final ResourceLocation WEAPON_TEXTURE = prefix("textures/item/flamescion_weapon.png");
	private static final ResourceLocation RING_TEXTURE = prefix("textures/entity/flamescion_ult_ring.png");
	private static final int FULL_BRIGHT = 0xF000F0;

	public FlamescionUltRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRender(FlamescionUltEntity entity, Frustum camera, double camX, double camY, double camZ) {
		return true;
	}

	@Override
	public void render(FlamescionUltEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		float age = entity.tickCount + partialTick;
		if (age > 76.0F) {
			return;
		}

		poseStack.pushPose();

		// 1. Huge expanding & rotating ground Herrscher sigil (radius up to 8.5 blocks -> diameter 17.0)
		float sigilOpen = Mth.clamp(age / 16.0F, 0.0F, 1.0F);
		float sigilFade = age > 60.0F ? Mth.clamp((75.0F - age) / 15.0F, 0.0F, 1.0F) : 1.0F;
		float sigilPulse = 1.0F + (float) Math.sin(age * 0.25F) * 0.04F;
		float sigilRadius = (1.5F + 7.0F * (1.0F - (1.0F - sigilOpen) * (1.0F - sigilOpen))) * sigilPulse;
		int sigilAlpha = Mth.clamp((int) (235.0F * sigilOpen * sigilFade), 0, 255);
		int innerSigilAlpha = Mth.clamp((int) (185.0F * sigilOpen * sigilFade), 0, 255);

		if (sigilAlpha > 2) {
			VertexConsumer vortexVertex = buffer.getBuffer(RenderType.entityTranslucent(VORTEX_TEXTURE));
			poseStack.pushPose();
			poseStack.translate(0.0D, 0.08D, 0.0D);
			poseStack.mulPose(Axis.YP.rotationDegrees(age * 2.2F));
			drawHorizontalQuad(poseStack.last(), vortexVertex, sigilRadius, 255, 235, 195, sigilAlpha);
			poseStack.popPose();

			poseStack.pushPose();
			poseStack.translate(0.0D, 0.24D, 0.0D);
			poseStack.mulPose(Axis.YP.rotationDegrees(-age * 3.8F));
			drawHorizontalQuad(poseStack.last(), vortexVertex, sigilRadius * 0.72F, 255, 160, 55, innerSigilAlpha);
			poseStack.popPose();
		}

		// 2. Giant hovering -> plunging blazing greatsword (6.5F scale!)
		float swordAlphaFactor = Mth.clamp(age / 8.0F, 0.0F, 1.0F) * (age > 58.0F ? Mth.clamp((72.0F - age) / 14.0F, 0.0F, 1.0F) : 1.0F);
		if (swordAlphaFactor > 0.01F) {
			float swordY;
			float swordSpin;
			if (age < 23.0F) {
				// Hovering majestically overhead while charging
				float hoverT = age / 23.0F;
				swordY = 13.5F - hoverT * 2.5F + (float) Math.sin(age * 0.35F) * 0.35F;
				swordSpin = age * 4.5F;
			} else if (age < 29.0F) {
				// High-speed plunge into the center of the domain!
				float plungeT = (age - 23.0F) / 6.0F;
				float eased = plungeT * plungeT * plungeT;
				swordY = Mth.lerp(eased, 11.0F, 2.8F);
				swordSpin = 103.5F + plungeT * 180.0F;
			} else {
				// Impaled blazing blade pulsing before final detonation
				float shake = age < 36.0F ? (float) Math.sin(age * 2.8F) * 0.12F * (1.0F - (age - 29.0F) / 7.0F) : 0.0F;
				swordY = 2.8F + shake;
				swordSpin = 283.5F;
			}

			int swordAlpha = Mth.clamp((int) (255.0F * swordAlphaFactor), 0, 255);
			VertexConsumer weaponVertex = buffer.getBuffer(RenderType.entityTranslucent(WEAPON_TEXTURE));
			float swordScale = 6.5F;

			poseStack.pushPose();
			poseStack.translate(0.0D, swordY, 0.0D);
			poseStack.mulPose(Axis.YP.rotationDegrees(swordSpin));
			for (int i = 0; i < 2; i++) {
				poseStack.pushPose();
				poseStack.mulPose(Axis.YP.rotationDegrees(i * 90.0F));
				// Weapon texture tip points (+1, +1); -135 deg around Z aims the blade straight down (-Y)
				poseStack.mulPose(Axis.ZP.rotationDegrees(-135.0F));
				poseStack.scale(swordScale, swordScale, swordScale);
				drawVerticalQuad(poseStack.last(), weaponVertex, 0.5F, 255, 248, 225, swordAlpha);
				poseStack.popPose();
			}
			poseStack.popPose();
		}

		// 3. Expanding blazing shockwave rings (Phase 2 plunge impacts & Phase 3 supernova)
		VertexConsumer ringVertex = buffer.getBuffer(RenderType.entityTranslucent(RING_TEXTURE));
		renderShockwaveRing(poseStack, ringVertex, age, 27.0F, 16.0F, 1.5F, 10.5F, 0.35D, 255, 230, 160);
		renderShockwaveRing(poseStack, ringVertex, age, 39.0F, 15.0F, 1.5F, 11.0F, 0.55D, 255, 190, 95);
		renderShockwaveRing(poseStack, ringVertex, age, 56.0F, 18.0F, 1.5F, 13.5F, 0.45D, 255, 245, 210);
		renderShockwaveRing(poseStack, ringVertex, age, 58.0F, 17.0F, 1.2F, 12.0F, 1.2D, 255, 135, 55);

		// 4. Vertical translucent golden-crimson light pillars during detonation (ticks 45-74)
		if (age >= 45.0F && age < 74.0F) {
			float pillarIn = Mth.clamp((age - 45.0F) / 6.0F, 0.0F, 1.0F);
			float pillarOut = age > 60.0F ? Mth.clamp((74.0F - age) / 14.0F, 0.0F, 1.0F) : 1.0F;
			float detBoost = (age >= 56.0F && age <= 65.0F) ? 1.35F : 1.0F;
			float pillarIntensity = pillarIn * pillarOut;
			if (pillarIntensity > 0.01F) {
				VertexConsumer pillarVertex = buffer.getBuffer(RenderType.lightning());
				// Colossal central solar core beam
				int coreAlpha = Mth.clamp((int) (145.0F * pillarIntensity * detBoost), 0, 220);
				drawLightPillar(poseStack, pillarVertex, 0.0F, 0.0F, 1.45F * detBoost, 16.0F * pillarIn,
						255, 232, 145, coreAlpha, 255, 120, 30);

				// 8 orbiting golden-crimson solar pillars around the Herrscher sigil
				int outerAlpha = Mth.clamp((int) (115.0F * pillarIntensity * detBoost), 0, 195);
				float orbitRadius = 5.5F;
				for (int i = 0; i < 8; i++) {
					float angle = (float) ((Math.PI * 2.0D * i) / 8.0D) + age * 0.035F;
					float px = Mth.cos(angle) * orbitRadius;
					float pz = Mth.sin(angle) * orbitRadius;
					boolean golden = (i % 2 == 0);
					drawLightPillar(poseStack, pillarVertex, px, pz, 0.75F, 11.5F * pillarIn,
							255, golden ? 205 : 110, golden ? 75 : 35, outerAlpha,
							230, 45, 25);
				}
			}
		}

		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	private static void renderShockwaveRing(PoseStack poseStack, VertexConsumer vertex, float age,
			float startTick, float duration, float minRadius, float maxRadius, double yOffset,
			int r, int g, int b) {
		if (age < startTick || age > startTick + duration) {
			return;
		}
		float t = Mth.clamp((age - startTick) / duration, 0.0F, 1.0F);
		float eased = 1.0F - (1.0F - t) * (1.0F - t);
		float radius = Mth.lerp(eased, minRadius, maxRadius);
		int alpha = Mth.clamp((int) (245.0F * (1.0F - t * t)), 0, 255);
		if (alpha <= 2) {
			return;
		}
		poseStack.pushPose();
		poseStack.translate(0.0D, yOffset, 0.0D);
		poseStack.mulPose(Axis.YP.rotationDegrees(t * 75.0F));
		drawHorizontalQuad(poseStack.last(), vertex, radius, r, g, b, alpha);
		poseStack.popPose();
	}

	private static void drawLightPillar(PoseStack poseStack, VertexConsumer vertex, float x, float z,
			float halfWidth, float height, int br, int bg, int bb, int baseAlpha, int tr, int tg, int tb) {
		poseStack.pushPose();
		poseStack.translate(x, 0.0D, z);
		Matrix4f mat = poseStack.last().pose();
		// Crossed double vertical planes so every pillar looks volumetric from 360 degrees
		drawPillarQuad(mat, vertex, -halfWidth, 0.0F, halfWidth, 0.0F, height, br, bg, bb, baseAlpha, tr, tg, tb);
		drawPillarQuad(mat, vertex, halfWidth, 0.0F, -halfWidth, 0.0F, height, br, bg, bb, baseAlpha, tr, tg, tb);
		drawPillarQuad(mat, vertex, 0.0F, -halfWidth, 0.0F, halfWidth, height, br, bg, bb, baseAlpha, tr, tg, tb);
		drawPillarQuad(mat, vertex, 0.0F, halfWidth, 0.0F, -halfWidth, height, br, bg, bb, baseAlpha, tr, tg, tb);
		poseStack.popPose();
	}

	private static void drawPillarQuad(Matrix4f mat, VertexConsumer vertex, float x0, float z0, float x1, float z1,
			float height, int br, int bg, int bb, int baseAlpha, int tr, int tg, int tb) {
		vertex.vertex(mat, x0, 0.0F, z0).color(br, bg, bb, baseAlpha).endVertex();
		vertex.vertex(mat, x1, 0.0F, z1).color(br, bg, bb, baseAlpha).endVertex();
		vertex.vertex(mat, x1, height, z1).color(tr, tg, tb, 0).endVertex();
		vertex.vertex(mat, x0, height, z0).color(tr, tg, tb, 0).endVertex();
	}

	private static void drawHorizontalQuad(PoseStack.Pose pose, VertexConsumer vertex, float half, int r, int g, int b, int a) {
		Matrix4f matrix = pose.pose();
		Matrix3f normal = pose.normal();
		vertex.vertex(matrix, -half, 0.0F, -half).color(r, g, b, a).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
		vertex.vertex(matrix, half, 0.0F, -half).color(r, g, b, a).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
		vertex.vertex(matrix, half, 0.0F, half).color(r, g, b, a).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
		vertex.vertex(matrix, -half, 0.0F, half).color(r, g, b, a).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
	}

	private static void drawVerticalQuad(PoseStack.Pose pose, VertexConsumer vertex, float half, int r, int g, int b, int a) {
		Matrix4f matrix = pose.pose();
		Matrix3f normal = pose.normal();
		vertex.vertex(matrix, -half, -half, 0.0F).color(r, g, b, a).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
		vertex.vertex(matrix, half, -half, 0.0F).color(r, g, b, a).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
		vertex.vertex(matrix, half, half, 0.0F).color(r, g, b, a).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
		vertex.vertex(matrix, -half, half, 0.0F).color(r, g, b, a).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(FULL_BRIGHT).normal(normal, 0.0F, 0.0F, 1.0F).endVertex();
	}

	@Override
	public ResourceLocation getTextureLocation(FlamescionUltEntity entity) {
		return VORTEX_TEXTURE;
	}
}
