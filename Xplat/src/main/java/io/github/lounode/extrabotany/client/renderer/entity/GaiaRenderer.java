package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import vazkii.botania.client.core.helper.CoreShaders;
import vazkii.botania.client.core.helper.RenderHelper;

import io.github.lounode.extrabotany.client.model.ArmorModels;
import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.common.entity.gaia.GaiaIII;

public class GaiaRenderer extends HumanoidMobRenderer<Gaia, HumanoidModel<Gaia>> {
	public static final float DEFAULT_GRAIN_INTENSITY = 0.05F;
	public static final float DEFAULT_DISFIGURATION = 0.025F;

	private final Model normalModel;
	private final Model slimModel;

	public GaiaRenderer(EntityRendererProvider.Context ctx) {
		super(ctx, new Model(ctx.bakeLayer(ModelLayers.PLAYER)), 0F);
		this.normalModel = (Model) this.getModel();
		this.slimModel = new Model(ctx.bakeLayer(ModelLayers.PLAYER_SLIM));
		// Call this here bc no other place with access to Context
		ArmorModels.init(ctx);
	}

	@Override
	public void render(@NotNull Gaia dopple, float yaw, float partialTicks, PoseStack ms, MultiBufferSource buffers, int light) {
		int invulTime = dopple.getInvulTime();
		boolean transitioning = dopple instanceof GaiaIII gaiaIII && gaiaIII.isTransitioning();
		float transitionProgress = transitioning ? ((GaiaIII) dopple).getTransitionProgress(partialTicks) : 0.0F;
		ShaderInstance shader = CoreShaders.doppleganger();
		if (shader != null) {
			float grainIntensity, disfiguration;
			if (invulTime > 0) {
				grainIntensity = invulTime > 20 ? 1F : invulTime * 0.05F;
				disfiguration = grainIntensity * 0.3F;
			} else {
				disfiguration = (DEFAULT_DISFIGURATION + dopple.hurtTime * ((1F - 0.15F) / 20F)) / 2F;
				grainIntensity = DEFAULT_GRAIN_INTENSITY + dopple.hurtTime * ((1F - 0.15F) / 10F);
			}

			if (transitioning) {
				grainIntensity = Mth.lerp(transitionProgress, 0.45F, 1.0F);
				disfiguration = 0.25F + transitionProgress * 0.55F + 0.15F * Mth.abs(Mth.sin((dopple.tickCount + partialTicks) * 0.85F));
			} else if (dopple instanceof GaiaIII gaiaiii && gaiaiii.getEgoFlicker() > 0) {
				// 二阶段每隔一阵闪现"本我"形态:强烈噪点/扭曲一闪(仅视觉,名称不变)。
				grainIntensity = 1F;
				disfiguration = 0.6F;
			}

			shader.safeGetUniform("BotaniaGrainIntensity").set(grainIntensity);
			shader.safeGetUniform("BotaniaDisfiguration").set(disfiguration);
		}

		var view = Minecraft.getInstance().getCameraEntity();
		if (view instanceof AbstractClientPlayer && DefaultPlayerSkin.getSkinModelName(view.getUUID()).equals("slim")) {
			this.model = slimModel;
		} else {
			this.model = normalModel;
		}

		if (transitioning) {
			ms.pushPose();
			float time = dopple.tickCount + partialTicks;
			float shakeAmp = 0.025F + transitionProgress * 0.075F;
			float ox = Mth.sin(time * 2.7F) * shakeAmp;
			float oy = Mth.cos(time * 3.3F) * shakeAmp * 0.5F;
			float oz = Mth.cos(time * 2.3F) * shakeAmp;
			ms.translate(ox, oy, oz);
			super.render(dopple, yaw, partialTicks, ms, buffers, light);
			ms.popPose();
			renderPhaseTransitionEffects(dopple, transitionProgress, partialTicks, ms, buffers);
		} else {
			super.render(dopple, yaw, partialTicks, ms, buffers, light);
		}

		//TODO BUG 多个盖亚会出现问题
		if (shader != null) {
			//shader.safeGetUniform("BotaniaGrainIntensity").set(DEFAULT_GRAIN_INTENSITY);
			//shader.safeGetUniform("BotaniaDisfiguration").set(DEFAULT_DISFIGURATION);
		}
	}

	private static void renderPhaseTransitionEffects(Gaia dopple, float progress, float partialTicks, PoseStack ms, MultiBufferSource buffers) {
		VertexConsumer vc = buffers.getBuffer(RenderType.lightning());
		float time = dopple.tickCount + partialTicks;
		float fadeIn = Mth.clamp(progress / 0.15F, 0.0F, 1.0F);
		float centerY = dopple.getBbHeight() * 0.5F;

		// Vertical ascending golden-violet beam pillar connecting boss down toward the beacon
		float dropToBeacon = (float) (dopple.getY() - dopple.getHome().pos().getY());
		float pillarBottomY = -Mth.clamp(dropToBeacon, 1.5F, 6.0F);
		float pillarTopY = dopple.getBbHeight() + 1.8F + progress * 1.2F;

		ms.pushPose();
		ms.mulPose(Axis.YP.rotationDegrees(time * 2.5F));
		drawBeamPillar(vc, ms.last().pose(), pillarBottomY, pillarTopY, 0.52F + 0.24F * progress,
				0.878F, 0.251F, 0.984F, 0.28F * fadeIn);
		ms.mulPose(Axis.YP.rotationDegrees(-time * 5.0F));
		drawBeamPillar(vc, ms.last().pose(), pillarBottomY, pillarTopY, 0.24F + 0.12F * progress,
				1.0F, 0.835F, 0.310F, 0.42F * fadeIn);
		ms.popPose();

		// Ring 1: outer golden-amber (#FFD54F) horizontal ring expanding & spinning
		ms.pushPose();
		ms.translate(0.0F, centerY, 0.0F);
		ms.mulPose(Axis.YP.rotationDegrees(time * 5.0F));
		float r1 = Mth.lerp(progress, 1.2F, 2.8F);
		drawMagicRing(vc, ms.last().pose(), r1, 0.26F, 1.0F, 0.835F, 0.310F, 0.70F * fadeIn, 32);
		ms.popPose();

		// Ring 2: tilted magenta-violet (#E040FB) counter-spinning ring
		ms.pushPose();
		ms.translate(0.0F, centerY, 0.0F);
		ms.mulPose(Axis.XP.rotationDegrees(52.0F));
		ms.mulPose(Axis.ZP.rotationDegrees(25.0F));
		ms.mulPose(Axis.YP.rotationDegrees(-time * 8.5F));
		float r2 = Mth.lerp(progress, 1.5F, 2.2F);
		drawMagicRing(vc, ms.last().pose(), r2, 0.22F, 0.878F, 0.251F, 0.984F, 0.65F * fadeIn, 32);
		ms.popPose();

		// Ring 3: tilted cyan-white (#80DEEA) fast inner halo
		ms.pushPose();
		ms.translate(0.0F, centerY, 0.0F);
		ms.mulPose(Axis.XP.rotationDegrees(-58.0F));
		ms.mulPose(Axis.ZP.rotationDegrees(-32.0F));
		ms.mulPose(Axis.YP.rotationDegrees(time * 14.0F));
		float r3 = Mth.lerp(progress, 1.0F, 1.55F);
		drawMagicRing(vc, ms.last().pose(), r3, 0.16F, 0.502F, 0.871F, 0.918F, 0.72F * fadeIn, 28);
		ms.popPose();

		// Climax burst (>80% progress): rapidly expanding shockwave ring + golden-white flash core
		if (progress > 0.80F) {
			float climax = Mth.clamp((progress - 0.80F) / 0.20F, 0.0F, 1.0F);

			ms.pushPose();
			ms.translate(0.0F, centerY, 0.0F);
			float shockRadius = 1.2F + climax * 4.6F;
			float shockWidth = 0.42F * (1.0F - climax * 0.40F);
			float shockAlpha = 0.85F * (1.0F - climax * 0.55F);
			drawMagicRing(vc, ms.last().pose(), shockRadius, shockWidth, 1.0F, 0.96F, 0.78F, shockAlpha, 36);
			ms.popPose();

			ms.pushPose();
			ms.translate(0.0F, centerY, 0.0F);
			ms.mulPose(Axis.YP.rotationDegrees(time * 18.0F));
			ms.mulPose(Axis.XP.rotationDegrees(time * 11.0F));
			float coreRadius = 0.55F + climax * 1.25F;
			float coreAlpha = (0.35F + 0.50F * climax) * (1.0F - climax * 0.25F);
			drawFlashCore(vc, ms.last().pose(), coreRadius, 1.0F, 0.96F, 0.82F, coreAlpha);
			ms.popPose();
		}
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

			// Inner feathered ribbon (inner edge -> bright core)
			emitDoubleSidedQuad(vc, mat,
					c0 * innerR, 0.0F, s0 * innerR,
					c1 * innerR, 0.0F, s1 * innerR,
					c1 * radius, 0.0F, s1 * radius,
					c0 * radius, 0.0F, s0 * radius,
					r, g, b, 0.0F,
					r, g, b, alpha);
			// Outer feathered ribbon (bright core -> outer edge)
			emitDoubleSidedQuad(vc, mat,
					c0 * radius, 0.0F, s0 * radius,
					c1 * radius, 0.0F, s1 * radius,
					c1 * outerR, 0.0F, s1 * outerR,
					c0 * outerR, 0.0F, s0 * outerR,
					r, g, b, alpha,
					r, g, b, 0.0F);
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

			emitDoubleSidedQuad(vc, mat,
					x0, bottomY, z0,
					x1, bottomY, z1,
					x1, midBottom, z1,
					x0, midBottom, z0,
					r, g, b, 0.0F,
					r, g, b, alpha);
			emitDoubleSidedQuad(vc, mat,
					x0, midBottom, z0,
					x1, midBottom, z1,
					x1, midTop, z1,
					x0, midTop, z0,
					r, g, b, alpha,
					r, g, b, alpha);
			emitDoubleSidedQuad(vc, mat,
					x0, midTop, z0,
					x1, midTop, z1,
					x1, topY, z1,
					x0, topY, z0,
					r, g, b, alpha,
					r, g, b, 0.0F);
		}
	}

	private static void drawFlashCore(VertexConsumer vc, Matrix4f mat, float radius,
			float r, float g, float b, float alpha) {
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
				float c0 = Mth.cos(th0);
				float s0 = Mth.sin(th0);
				float c1 = Mth.cos(th1);
				float s1 = Mth.sin(th1);

				emitDoubleSidedQuad(vc, mat,
						c0 * rad0, y0, s0 * rad0,
						c1 * rad0, y0, s1 * rad0,
						c1 * rad1, y1, s1 * rad1,
						c0 * rad1, y1, s0 * rad1,
						r, g, b, alpha,
						r, g, b, alpha);
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
		emitPosColor(vc, mat, x0, y0, z0, r0, g0, b0, a0);
		emitPosColor(vc, mat, x1, y1, z1, r0, g0, b0, a0);
		emitPosColor(vc, mat, x2, y2, z2, r1, g1, b1, a1);
		emitPosColor(vc, mat, x3, y3, z3, r1, g1, b1, a1);

		emitPosColor(vc, mat, x3, y3, z3, r1, g1, b1, a1);
		emitPosColor(vc, mat, x2, y2, z2, r1, g1, b1, a1);
		emitPosColor(vc, mat, x1, y1, z1, r0, g0, b0, a0);
		emitPosColor(vc, mat, x0, y0, z0, r0, g0, b0, a0);
	}

	private static void emitPosColor(VertexConsumer vc, Matrix4f mat, float x, float y, float z,
			float r, float g, float b, float a) {
		vc.vertex(mat, x, y, z).color(r, g, b, a).endVertex();
	}

	@NotNull
	@Override
	public ResourceLocation getTextureLocation(@NotNull Gaia entity) {
		Minecraft mc = Minecraft.getInstance();

		if (!(mc.getCameraEntity() instanceof AbstractClientPlayer clientPlayer)) {
			return DefaultPlayerSkin.getDefaultSkin(entity.getUUID());
		}

		return clientPlayer.getSkinTextureLocation();
	}

	@Override
	protected boolean isBodyVisible(Gaia dopple) {
		return true;
	}

	private static class Model extends HumanoidModel<Gaia> {
		Model(ModelPart root) {
			super(root, RenderHelper::getDopplegangerLayer);
		}
	}

}
