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

import io.github.lounode.extrabotany.client.model.ExtrabotanyModelLayers;
import io.github.lounode.extrabotany.client.model.JudahSpearModel;
import io.github.lounode.extrabotany.common.entity.JudahSpearEntity;
import io.github.lounode.extrabotany.common.item.relic.JudahOathItem;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class JudahSpearRenderer extends EntityRenderer<JudahSpearEntity> {
	private static final ResourceLocation TEX_JUDAH = prefix("textures/entity/judah_spear.png");
	private static final ResourceLocation TEX_KIRA = prefix("textures/entity/judah_spear_kira.png");
	private static final ResourceLocation RING_TEX = prefix("textures/entity/judah_domain_ring.png");
	private static final int FULL_BRIGHT = 0x00F000F0;

	private final JudahSpearModel model;

	public JudahSpearRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new JudahSpearModel(context.bakeLayer(ExtrabotanyModelLayers.JUDAH_SPEAR));
	}

	@Override
	public boolean shouldRender(JudahSpearEntity entity, Frustum camera, double camX, double camY, double camZ) {
		return true;
	}

	@Override
	public void render(JudahSpearEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		float age = entity.tickCount + partialTick;
		JudahOathItem.Variant variant = entity.getVariant();
		boolean kira = variant == JudahOathItem.Variant.KIRA;
		boolean fake = entity.isFake();
		boolean impacted = entity.isImpacted();

		float fade = 1.0F;
		float impactT = 0.0F;
		if (impacted) {
			impactT = Mth.clamp((entity.getImpactTicks() + partialTick) / (float) JudahSpearEntity.IMPACT_LINGER_TICKS, 0.0F, 1.0F);
			fade = 1.0F - impactT * impactT;
		}
		if (fade <= 0.02F) {
			return;
		}

		int r = kira ? 72 : 255;
		int g = kira ? 228 : 214;
		int b = kira ? 255 : 64;

		poseStack.pushPose();

		// 1. 3D Spear Model (tip is at model -Y, so !fake points straight down; fake flips 180 deg to point straight up)
		poseStack.pushPose();
		poseStack.translate(0.0D, impacted ? 1.05D : 0.85D, 0.0D);
		if (fake) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		}
		poseStack.mulPose(Axis.YP.rotationDegrees(impacted ? entity.getId() * 37.0F : age * 18.0F));
		float scale = impacted ? 0.92F : 0.86F;
		poseStack.scale(scale, scale, scale);
		int alpha = Mth.clamp(Math.round(fade * 255.0F), 0, 255);
		VertexConsumer modelVertex = buffer.getBuffer(impacted ? RenderType.entityTranslucent(getTextureLocation(entity)) : RenderType.entityCutoutNoCull(getTextureLocation(entity)));
		this.model.renderToBuffer(poseStack, modelVertex, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, (alpha << 24) | 0x00FFFFFF);
		poseStack.popPose();

		// 2. Radiant Vertical Energy Sheath surrounding the spear
		VertexConsumer auraVertex = buffer.getBuffer(RenderType.lightning());
		poseStack.pushPose();
		poseStack.translate(0.0D, impacted ? 1.05D : 0.85D, 0.0D);
		if (fake) {
			poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		}
		poseStack.mulPose(Axis.YP.rotationDegrees(age * 14.0F));
		Matrix4f auraMat = poseStack.last().pose();
		drawVerticalAuraSheath(auraMat, auraVertex, -1.65F, -0.35F, 1.45F, 0.16F,
				255, 255, 245, Math.round(115 * fade),
				r, g, b, Math.round(145 * fade));
		drawVerticalAuraSheath(auraMat, auraVertex, -1.78F, -0.20F, 1.68F, 0.30F,
				r, g, b, Math.round(65 * fade),
				kira ? 48 : 255, kira ? 160 : 148, kira ? 255 : 24, Math.round(85 * fade));
		poseStack.popPose();

		// 3. Sonic Ring (in flight) OR Expanding Ground Impact Shockwave + Holy Light Pillar (on impact)
		VertexConsumer ringVertex = buffer.getBuffer(RenderType.entityTranslucent(RING_TEX));
		if (!impacted) {
			poseStack.pushPose();
			poseStack.translate(0.0D, fake ? 0.35D : 1.55D, 0.0D);
			poseStack.mulPose(Axis.YP.rotationDegrees(age * 24.0F));
			drawHorizontalQuad(poseStack.last().pose(), ringVertex, 0.65F, r, g, b, 185);
			poseStack.popPose();
		} else {
			float eased = 1.0F - (1.0F - impactT) * (1.0F - impactT);
			float ringR = Mth.lerp(eased, 0.6F, kira ? 3.4F : 2.3F);
			int ringA = Mth.clamp(Math.round(235.0F * fade), 0, 255);
			poseStack.pushPose();
			poseStack.translate(0.0D, 0.09D, 0.0D);
			poseStack.mulPose(Axis.YP.rotationDegrees(impactT * 90.0F));
			drawHorizontalQuad(poseStack.last().pose(), ringVertex, ringR, r, g, b, ringA);
			poseStack.popPose();

			// Vertical impact flash pillar
			float pillarW = Mth.lerp(impactT, 0.65F, 0.15F);
			drawLightPillar(poseStack, auraVertex, pillarW, 8.5F * (1.0F - impactT * 0.4F),
					255, kira ? 245 : 238, kira ? 255 : 170, Math.round(155.0F * fade));
		}

		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	private static void drawVerticalAuraSheath(Matrix4f mat, VertexConsumer v, float yTip, float yMid, float yTail, float hw,
			int tr, int tg, int tb, int ta, int mr, int mg, int mb, int ma) {
		float endW = 0.02F;
		// XY plane
		v.addVertex(mat, -endW, yTip, 0.0F).setColor(tr, tg, tb, ta);
		v.addVertex(mat, -hw, yMid, 0.0F).setColor(mr, mg, mb, ma);
		v.addVertex(mat, hw, yMid, 0.0F).setColor(mr, mg, mb, ma);
		v.addVertex(mat, endW, yTip, 0.0F).setColor(tr, tg, tb, ta);

		v.addVertex(mat, -hw, yMid, 0.0F).setColor(mr, mg, mb, ma);
		v.addVertex(mat, -endW, yTail, 0.0F).setColor(mr, mg, mb, 0);
		v.addVertex(mat, endW, yTail, 0.0F).setColor(mr, mg, mb, 0);
		v.addVertex(mat, hw, yMid, 0.0F).setColor(mr, mg, mb, ma);

		// ZY plane
		v.addVertex(mat, 0.0F, yTip, -endW).setColor(tr, tg, tb, ta);
		v.addVertex(mat, 0.0F, yMid, -hw).setColor(mr, mg, mb, ma);
		v.addVertex(mat, 0.0F, yMid, hw).setColor(mr, mg, mb, ma);
		v.addVertex(mat, 0.0F, yTip, endW).setColor(tr, tg, tb, ta);

		v.addVertex(mat, 0.0F, yMid, -hw).setColor(mr, mg, mb, ma);
		v.addVertex(mat, 0.0F, yTail, -endW).setColor(mr, mg, mb, 0);
		v.addVertex(mat, 0.0F, yTail, endW).setColor(mr, mg, mb, 0);
		v.addVertex(mat, 0.0F, yMid, hw).setColor(mr, mg, mb, ma);
	}

	private static void drawLightPillar(PoseStack poseStack, VertexConsumer v, float hw, float h, int r, int g, int b, int a) {
		if (a <= 2) {
			return;
		}
		Matrix4f mat = poseStack.last().pose();
		drawPillarQuad(mat, v, -hw, 0.0F, hw, 0.0F, h, r, g, b, a);
		drawPillarQuad(mat, v, hw, 0.0F, -hw, 0.0F, h, r, g, b, a);
		drawPillarQuad(mat, v, 0.0F, -hw, 0.0F, hw, h, r, g, b, a);
		drawPillarQuad(mat, v, 0.0F, hw, 0.0F, -hw, h, r, g, b, a);
	}

	private static void drawPillarQuad(Matrix4f mat, VertexConsumer v, float x0, float z0, float x1, float z1,
			float h, int r, int g, int b, int a) {
		v.addVertex(mat, x0, 0.0F, z0).setColor(r, g, b, a);
		v.addVertex(mat, x1, 0.0F, z1).setColor(r, g, b, a);
		v.addVertex(mat, x1, h, z1).setColor(r, g, b, 0);
		v.addVertex(mat, x0, h, z0).setColor(r, g, b, 0);
	}

	private static void drawHorizontalQuad(Matrix4f mat, VertexConsumer v, float half, int r, int g, int b, int a) {
		v.addVertex(mat, -half, 0.0F, -half).setColor(r, g, b, a).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
		v.addVertex(mat, half, 0.0F, -half).setColor(r, g, b, a).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
		v.addVertex(mat, half, 0.0F, half).setColor(r, g, b, a).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
		v.addVertex(mat, -half, 0.0F, half).setColor(r, g, b, a).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT).setNormal(0.0F, 1.0F, 0.0F);
	}

	@Override
	public ResourceLocation getTextureLocation(JudahSpearEntity entity) {
		return entity.getVariant() == JudahOathItem.Variant.KIRA ? TEX_KIRA : TEX_JUDAH;
	}
}
