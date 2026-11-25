package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import io.github.lounode.extrabotany.client.model.ExtrabotanyModelLayers;
import io.github.lounode.extrabotany.client.model.ModelVoidHerrscher;
import io.github.lounode.extrabotany.common.entity.gaia.VoidHerrscher;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class VoidHerrscherRenderer extends HumanoidMobRenderer<VoidHerrscher, ModelVoidHerrscher> {
	// wing.png is the complete 128x128 atlas (64x64 skin + golden wings + shields), matching ModelVoidHerrscher's 128x128 UV layout.
	private static final ResourceLocation ATLAS_TEXTURE = prefix("textures/entity/wing.png");
	private static final ResourceLocation FIELD_TEXTURE = prefix("textures/entity/void_field.png");

	public VoidHerrscherRenderer(EntityRendererProvider.Context context) {
		super(context, new ModelVoidHerrscher(context.bakeLayer(ExtrabotanyModelLayers.VOID_HERRSCHER)), 0.0F);
		addLayer(new WingLayer(this));
		addLayer(new ShieldLayer(this));
	}

	@Override
	public boolean shouldRender(VoidHerrscher entity, Frustum frustum, double camX, double camY, double camZ) {
		return true;
	}

	@Override
	public void render(@NotNull VoidHerrscher entity, float yaw, float partialTicks, PoseStack ms, MultiBufferSource buffers, int light) {
		boolean transitioning = entity.isTransitioning();
		float transProg = transitioning ? entity.getTransitionProgress(partialTicks) : 0.0F;
		float time = entity.tickCount + partialTicks;

		ms.pushPose();
		if (transitioning) {
			float amp = 0.02F + transProg * 0.06F;
			ms.translate(
					Mth.sin(time * 2.9F) * amp,
					Mth.cos(time * 3.4F) * amp * 0.5F,
					Mth.cos(time * 2.5F) * amp);
		} else if (entity.isRankIIRenderState()) {
			ms.translate(0.0F, Mth.sin(time * 0.10F) * 0.12F, 0.0F);
		}
		super.render(entity, yaw, partialTicks, ms, buffers, light);
		ms.popPose();

		renderHerrscherEffects(entity, time, partialTicks, ms, buffers);
	}

	private static void renderHerrscherEffects(VoidHerrscher entity, float time, float partialTicks, PoseStack ms, MultiBufferSource buffers) {
		boolean rankII = entity.isRankIIRenderState();
		boolean rankIII = entity.isRankIIIRenderState();
		boolean transitioning = entity.isTransitioning();
		int shieldLayers = entity.getShieldLayersRenderState();
		int castState = entity.getCastState();

		float centerY = entity.getBbHeight() * 0.55F;

		// 1. Visible Void Prism Barrier (shows remaining immunity shield layers)
		if (shieldLayers > 0 && !transitioning) {
			VertexConsumer vc = buffers.getBuffer(RenderType.lightning());
			float alpha = 0.26F + Math.min(shieldLayers, 4) * 0.11F;
			ms.pushPose();
			ms.translate(0.0F, centerY, 0.0F);
			ms.mulPose(Axis.YP.rotationDegrees(time * (3.5F + shieldLayers * 0.9F)));
			ms.mulPose(Axis.XP.rotationDegrees(22.0F));
			drawMagicRing(vc, ms.last().pose(), 1.15F, 0.14F, 0.78F, 0.42F, 1.0F, alpha, 6);
			ms.mulPose(Axis.XP.rotationDegrees(-44.0F));
			ms.mulPose(Axis.YP.rotationDegrees(-time * 7.0F));
			drawMagicRing(vc, ms.last().pose(), 1.05F, 0.10F, 1.0F, 0.85F, 0.35F, alpha * 0.9F, 6);
			ms.popPose();
		}

		// 2. Herrscher Halo & Ground Sigil (Rank II golden halo; Rank III dual crown + floating void sigil)
		if (rankII && !transitioning) {
			VertexConsumer vc = buffers.getBuffer(RenderType.lightning());
			ms.pushPose();
			ms.translate(0.0F, entity.getBbHeight() + 0.35F, 0.0F);
			ms.mulPose(Axis.XP.rotationDegrees(14.0F));
			ms.mulPose(Axis.YP.rotationDegrees(time * 2.8F));
			drawMagicRing(vc, ms.last().pose(), rankIII ? 0.82F : 0.65F, 0.09F, 1.0F, 0.84F, 0.31F, 0.72F, 24);
			if (rankIII) {
				ms.mulPose(Axis.YP.rotationDegrees(-time * 6.5F));
				drawMagicRing(vc, ms.last().pose(), 1.12F, 0.07F, 0.76F, 0.32F, 1.0F, 0.58F, 24);
			}
			ms.popPose();

			if (rankIII) {
				renderTexturedSigil(ms, buffers, 0.08F, 1.75F, time * 1.4F, 0xBBDFB8FF);
			}
		}

		// 3. Void Judge Ultimate Snap Charge Aura
		if (castState == VoidHerrscher.CAST_JUDGE) {
			float chargeProg = 1.0F - Mth.clamp(entity.getCastTicks() / (float) VoidHerrscher.JUDGE_CHARGE_TICKS, 0.0F, 1.0F);
			renderTexturedSigil(ms, buffers, 0.05F, 1.8F + chargeProg * 2.2F, -time * 5.0F, 0xDDFFD970);
			VertexConsumer vc = buffers.getBuffer(RenderType.lightning());

			ms.pushPose();
			ms.translate(0.0F, centerY, 0.0F);
			ms.mulPose(Axis.YP.rotationDegrees(time * 9.0F));
			drawMagicRing(vc, ms.last().pose(), Mth.lerp(chargeProg, 3.4F, 1.3F), 0.24F, 1.0F, 0.84F, 0.30F, 0.82F, 32);
			ms.mulPose(Axis.XP.rotationDegrees(58.0F));
			ms.mulPose(Axis.YP.rotationDegrees(-time * 13.0F));
			drawMagicRing(vc, ms.last().pose(), Mth.lerp(chargeProg, 2.8F, 1.1F), 0.20F, 1.0F, 0.24F, 0.56F, 0.75F, 28);
			drawFlashCore(vc, ms.last().pose(), 0.25F + chargeProg * 0.65F, 1.0F, 0.92F, 0.62F, 0.35F + chargeProg * 0.45F);
			ms.popPose();
		}

		// 4. Phase Transition Ascension Pillar & Shockwave (Rank II Awakening / Rank III Final Herrscher)
		if (transitioning) {
			float progress = entity.getTransitionProgress(partialTicks);
			boolean toRankIII = entity.getTransitionTargetRank() >= 3;
			float fadeIn = Mth.clamp(progress / 0.15F, 0.0F, 1.0F);
			VertexConsumer vc = buffers.getBuffer(RenderType.lightning());

			renderTexturedSigil(ms, buffers, -0.35F, Mth.lerp(progress, 1.6F, toRankIII ? 4.0F : 3.0F), time * 4.5F,
					toRankIII ? 0xDDFF60B8 : 0xDDFFD55A);

			float pillarBottom = -3.8F;
			float pillarTop = entity.getBbHeight() + 2.4F + progress * 1.6F;
			ms.pushPose();
			ms.mulPose(Axis.YP.rotationDegrees(time * 3.0F));
			drawBeamPillar(vc, ms.last().pose(), pillarBottom, pillarTop, (toRankIII ? 0.68F : 0.52F) + 0.22F * progress,
					toRankIII ? 0.92F : 0.72F, 0.24F, 1.0F, 0.30F * fadeIn);
			ms.mulPose(Axis.YP.rotationDegrees(-time * 6.0F));
			drawBeamPillar(vc, ms.last().pose(), pillarBottom, pillarTop, 0.26F + 0.14F * progress,
					1.0F, 0.85F, 0.32F, 0.46F * fadeIn);
			ms.popPose();

			// Outer golden ring
			ms.pushPose();
			ms.translate(0.0F, centerY, 0.0F);
			ms.mulPose(Axis.YP.rotationDegrees(time * 5.5F));
			drawMagicRing(vc, ms.last().pose(), Mth.lerp(progress, 1.2F, toRankIII ? 3.3F : 2.6F), 0.26F, 1.0F, 0.84F, 0.31F, 0.75F * fadeIn, 32);
			ms.popPose();

			// Tilted violet-crimson counter ring
			ms.pushPose();
			ms.translate(0.0F, centerY, 0.0F);
			ms.mulPose(Axis.XP.rotationDegrees(50.0F));
			ms.mulPose(Axis.ZP.rotationDegrees(28.0F));
			ms.mulPose(Axis.YP.rotationDegrees(-time * 9.0F));
			drawMagicRing(vc, ms.last().pose(), Mth.lerp(progress, 1.5F, 2.3F), 0.22F,
					toRankIII ? 1.0F : 0.76F, 0.26F, toRankIII ? 0.58F : 1.0F, 0.68F * fadeIn, 30);
			ms.popPose();

			// Climax explosion ring (>78% progress)
			if (progress > 0.78F) {
				float climax = Mth.clamp((progress - 0.78F) / 0.22F, 0.0F, 1.0F);
				ms.pushPose();
				ms.translate(0.0F, centerY, 0.0F);
				drawMagicRing(vc, ms.last().pose(), 1.3F + climax * 5.0F, 0.45F * (1.0F - climax * 0.4F),
						1.0F, 0.94F, 0.70F, 0.88F * (1.0F - climax * 0.55F), 36);
				drawFlashCore(vc, ms.last().pose(), 0.5F + climax * 1.2F, 1.0F, 0.95F, 0.78F, (0.35F + 0.50F * climax) * (1.0F - climax * 0.25F));
				ms.popPose();
			}
		}
	}

	private static void renderTexturedSigil(PoseStack ms, MultiBufferSource buffers, float yOffset, float radius, float rotDeg, int color) {
		ms.pushPose();
		ms.translate(0.0F, yOffset, 0.0F);
		ms.mulPose(Axis.YP.rotationDegrees(rotDeg));
		ms.mulPose(Axis.XP.rotationDegrees(90.0F));
		VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucentEmissive(FIELD_TEXTURE));
		Matrix4f mat = ms.last().pose();
		vc.addVertex(mat, -radius, -radius, 0.0F).setColor(color).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, 1.0F);
		vc.addVertex(mat, radius, -radius, 0.0F).setColor(color).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, 1.0F);
		vc.addVertex(mat, radius, radius, 0.0F).setColor(color).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, 1.0F);
		vc.addVertex(mat, -radius, radius, 0.0F).setColor(color).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, 1.0F);
		// Back face so visible from below while levitating
		vc.addVertex(mat, -radius, radius, 0.0F).setColor(color).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, -1.0F);
		vc.addVertex(mat, radius, radius, 0.0F).setColor(color).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, -1.0F);
		vc.addVertex(mat, radius, -radius, 0.0F).setColor(color).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, -1.0F);
		vc.addVertex(mat, -radius, -radius, 0.0F).setColor(color).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, -1.0F);
		ms.popPose();
	}

	private static void drawMagicRing(VertexConsumer vc, Matrix4f mat, float radius, float halfWidth,
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
			emitDoubleSidedQuad(vc, mat,
					c0 * innerR, 0.0F, s0 * innerR,
					c1 * innerR, 0.0F, s1 * innerR,
					c1 * radius, 0.0F, s1 * radius,
					c0 * radius, 0.0F, s0 * radius,
					r, g, b, 0.0F, r, g, b, alpha);
			emitDoubleSidedQuad(vc, mat,
					c0 * radius, 0.0F, s0 * radius,
					c1 * radius, 0.0F, s1 * radius,
					c1 * outerR, 0.0F, s1 * outerR,
					c0 * outerR, 0.0F, s0 * outerR,
					r, g, b, alpha, r, g, b, 0.0F);
		}
	}

	private static void drawBeamPillar(VertexConsumer vc, Matrix4f mat, float bottomY, float topY, float radius,
			float r, float g, float b, float alpha) {
		int sides = 8;
		float midBottom = Mth.lerp(0.18F, bottomY, topY);
		float midTop = Mth.lerp(0.82F, bottomY, topY);
		for (int i = 0; i < sides; i++) {
			float a0 = (float) (i * Math.PI * 2.0 / sides);
			float a1 = (float) ((i + 1) * Math.PI * 2.0 / sides);
			float x0 = Mth.cos(a0) * radius;
			float z0 = Mth.sin(a0) * radius;
			float x1 = Mth.cos(a1) * radius;
			float z1 = Mth.sin(a1) * radius;
			emitDoubleSidedQuad(vc, mat, x0, bottomY, z0, x1, bottomY, z1, x1, midBottom, z1, x0, midBottom, z0, r, g, b, 0.0F, r, g, b, alpha);
			emitDoubleSidedQuad(vc, mat, x0, midBottom, z0, x1, midBottom, z1, x1, midTop, z1, x0, midTop, z0, r, g, b, alpha, r, g, b, alpha);
			emitDoubleSidedQuad(vc, mat, x0, midTop, z0, x1, midTop, z1, x1, topY, z1, x0, topY, z0, r, g, b, alpha, r, g, b, 0.0F);
		}
	}

	private static void drawFlashCore(VertexConsumer vc, Matrix4f mat, float radius, float r, float g, float b, float alpha) {
		int stacks = 6;
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
				emitDoubleSidedQuad(vc, mat,
						Mth.cos(th0) * rad0, y0, Mth.sin(th0) * rad0,
						Mth.cos(th1) * rad0, y0, Mth.sin(th1) * rad0,
						Mth.cos(th1) * rad1, y1, Mth.sin(th1) * rad1,
						Mth.cos(th0) * rad1, y1, Mth.sin(th0) * rad1,
						r, g, b, alpha, r, g, b, alpha);
			}
		}
	}

	private static void emitDoubleSidedQuad(VertexConsumer vc, Matrix4f mat,
			float x0, float y0, float z0,
			float x1, float y1, float z1,
			float x2, float y2, float z2,
			float x3, float y3, float z3,
			float r0, float g0, float b0, float a0,
			float r1, float g1, float b1, float a1) {
		vc.addVertex(mat, x0, y0, z0).setColor(r0, g0, b0, a0);
		vc.addVertex(mat, x1, y1, z1).setColor(r0, g0, b0, a0);
		vc.addVertex(mat, x2, y2, z2).setColor(r1, g1, b1, a1);
		vc.addVertex(mat, x3, y3, z3).setColor(r1, g1, b1, a1);

		vc.addVertex(mat, x3, y3, z3).setColor(r1, g1, b1, a1);
		vc.addVertex(mat, x2, y2, z2).setColor(r1, g1, b1, a1);
		vc.addVertex(mat, x1, y1, z1).setColor(r0, g0, b0, a0);
		vc.addVertex(mat, x0, y0, z0).setColor(r0, g0, b0, a0);
	}

	@NotNull
	@Override
	public ResourceLocation getTextureLocation(@NotNull VoidHerrscher entity) {
		return ATLAS_TEXTURE;
	}

	@Override
	protected RenderType getRenderType(VoidHerrscher entity, boolean bodyVisible, boolean translucent, boolean glowing) {
		return RenderType.entityTranslucent(getTextureLocation(entity));
	}

	private static class WingLayer extends RenderLayer<VoidHerrscher, ModelVoidHerrscher> {
		WingLayer(VoidHerrscherRenderer renderer) {
			super(renderer);
		}

		@Override
		public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, VoidHerrscher entity,
				float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
			boolean show = entity.isRankIIRenderState()
					|| (entity.isTransitioning() && entity.getTransitionTargetRank() >= 2 && entity.getTransitionProgress(partialTick) > 0.35F);
			if (!show) {
				return;
			}
			// Fullbright glowing golden wings
			getParentModel().renderWings(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(ATLAS_TEXTURE)),
					0xF000F0, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
			if (entity.isRankIIIRenderState()) {
				poseStack.pushPose();
				poseStack.scale(1.04F, 1.04F, 1.04F);
				getParentModel().renderWings(poseStack, buffer.getBuffer(RenderType.entityTranslucentEmissive(ATLAS_TEXTURE)),
						0xF000F0, OverlayTexture.NO_OVERLAY, 0x66FFE082);
				poseStack.popPose();
			}
		}
	}

	private static class ShieldLayer extends RenderLayer<VoidHerrscher, ModelVoidHerrscher> {
		ShieldLayer(VoidHerrscherRenderer renderer) {
			super(renderer);
		}

		@Override
		public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, VoidHerrscher entity,
				float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
			if (entity.getRotatingShieldsRenderState() <= 0) {
				return;
			}
			getParentModel().renderShields(poseStack, buffer.getBuffer(RenderType.entityTranslucentEmissive(ATLAS_TEXTURE)),
					0xF000F0, OverlayTexture.NO_OVERLAY, 0xCCFFFFFF);
		}
	}
}
