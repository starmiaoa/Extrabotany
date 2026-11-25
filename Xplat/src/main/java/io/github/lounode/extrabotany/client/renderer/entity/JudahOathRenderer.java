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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

import io.github.lounode.extrabotany.client.model.ExtrabotanyModelLayers;
import io.github.lounode.extrabotany.client.model.JudahOathModel;
import io.github.lounode.extrabotany.common.entity.JudahOathEntity;
import io.github.lounode.extrabotany.common.item.relic.JudahOathItem;

import java.util.Comparator;
import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class JudahOathRenderer extends EntityRenderer<JudahOathEntity> {
	private static final ResourceLocation TEX_JUDAH = prefix("textures/entity/judah_oath.png");
	private static final ResourceLocation TEX_KIRA = prefix("textures/entity/judah_oath_kira.png");
	private static final ResourceLocation TEX_SAKURA = prefix("textures/entity/judah_oath_sakura.png");
	private static final ResourceLocation RING_TEX = prefix("textures/entity/judah_domain_ring.png");
	private static final int FULL_BRIGHT = 0x00F000F0;

	private final JudahOathModel model;

	public JudahOathRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.model = new JudahOathModel(context.bakeLayer(ExtrabotanyModelLayers.JUDAH_OATH));
		this.shadowRadius = 0.75F;
	}

	@Override
	public boolean shouldRender(JudahOathEntity entity, Frustum camera, double camX, double camY, double camZ) {
		return true;
	}

	@Override
	public void render(JudahOathEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		float age = entity.tickCount + partialTick;
		JudahOathItem.Variant variant = entity.getVariant();
		float fade = entity.getFadeAlpha(partialTick);
		if (fade <= 0.01F) {
			return;
		}

		ResourceLocation tex = getTextureLocation(entity);
		int r = Math.round(variant.red() * 255.0F);
		int g = Math.round(variant.green() * 255.0F);
		int b = Math.round(variant.blue() * 255.0F);
		int baseAlpha = Mth.clamp(Math.round(fade * 255.0F), 0, 255);
		int baseColor = (baseAlpha << 24) | 0x00FFFFFF;

		// 1. Render 3D Mechanized Battle-Cross Model
		poseStack.pushPose();
		float modelScale = 1.18F;
		float squashY = 1.0F;
		if (entity.isLanded()) {
			float st = entity.getStandbyTicks() + partialTick;
			if (st < 7.0F) {
				squashY = 1.0F - 0.14F * Mth.sin((st / 7.0F) * Mth.PI);
			}
			poseStack.mulPose(Axis.YP.rotationDegrees(entity.getDeployYaw()));
		} else {
			poseStack.translate(0.0D, 0.85D, 0.0D);
			poseStack.mulPose(Axis.YP.rotationDegrees(entity.getDeployYaw()));
			poseStack.mulPose(Axis.XP.rotationDegrees(age * 16.0F));
		}

		poseStack.scale(-modelScale, -modelScale * squashY, modelScale);
		poseStack.translate(0.0D, -1.501D, 0.0D);

		this.model.setupAnim(entity, 0.0F, 0.0F, age, 0.0F, 0.0F);
		VertexConsumer baseConsumer = buffer.getBuffer(fade < 0.99F ? RenderType.entityTranslucent(tex) : RenderType.entityCutoutNoCull(tex));
		this.model.renderBase(poseStack, baseConsumer, packedLight, OverlayTexture.NO_OVERLAY, baseColor);

		float glowPulse = 0.85F + 0.15F * Mth.sin(age * 0.22F);
		int glowAlpha = Mth.clamp(Math.round(fade * glowPulse * 255.0F), 0, 255);
		VertexConsumer glowConsumer = buffer.getBuffer(RenderType.entityTranslucent(tex));
		this.model.renderGlow(poseStack, glowConsumer, OverlayTexture.NO_OVERLAY, (glowAlpha << 24) | 0x00FFFFFF);
		poseStack.popPose();

		// 2. Deployed Domain 3D VFX
		if (entity.isLanded()) {
			float standby = entity.getStandbyTicks() + partialTick;
			float open = Mth.clamp(standby / 16.0F, 0.0F, 1.0F);
			float range = Mth.lerp(1.0F - (1.0F - open) * (1.0F - open), 2.5F, Math.max(2.5F, entity.getFieldRange()));
			int ringAlpha = Mth.clamp(Math.round(210.0F * open * fade), 0, 255);

			if (ringAlpha > 3) {
				VertexConsumer ringVertex = buffer.getBuffer(RenderType.entityTranslucent(RING_TEX));
				// Outer expanding sacred domain circle
				poseStack.pushPose();
				poseStack.translate(0.0D, 0.06D, 0.0D);
				poseStack.mulPose(Axis.YP.rotationDegrees(age * 1.4F));
				drawHorizontalQuad(poseStack.last().pose(), ringVertex, range, r, g, b, ringAlpha);
				poseStack.popPose();

				// Inner counter-rotating sacred rune circle
				poseStack.pushPose();
				poseStack.translate(0.0D, 0.11D, 0.0D);
				poseStack.mulPose(Axis.YP.rotationDegrees(-age * 2.6F));
				drawHorizontalQuad(poseStack.last().pose(), ringVertex, range * 0.56F,
						Math.min(255, r + 40), Math.min(255, g + 40), Math.min(255, b + 40), (int) (ringAlpha * 0.78F));
				poseStack.popPose();
			}

			switch (variant) {
				case JUDAH -> renderJudahVfx(entity, age, standby, range, fade, poseStack, buffer);
				case KIRA -> renderKiraVfx(age, standby, range, fade, poseStack, buffer);
				case SAKURA -> renderSakuraVfx(entity, age, standby, fade, poseStack, buffer);
			}
		}

		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	private void renderJudahVfx(JudahOathEntity entity, float age, float standby, float range, float fade,
			PoseStack poseStack, MultiBufferSource buffer) {
		// Sacred golden halo hovering above the cross crown
		VertexConsumer ringVertex = buffer.getBuffer(RenderType.entityTranslucent(RING_TEX));
		int haloAlpha = Mth.clamp(Math.round(215.0F * fade), 0, 255);
		poseStack.pushPose();
		poseStack.translate(0.0D, 3.25D + Mth.sin(age * 0.12F) * 0.12D, 0.0D);
		poseStack.mulPose(Axis.YP.rotationDegrees(age * 3.5F));
		drawHorizontalQuad(poseStack.last().pose(), ringVertex, 2.35F, 255, 224, 96, haloAlpha);
		poseStack.popPose();

		// Radiant Golden Chains of Binding tethering nearby enemies inside the domain!
		VertexConsumer chainVertex = buffer.getBuffer(RenderType.lightning());
		double r = Math.min(range, JudahOathEntity.MAX_RANGE);
		AABB search = new AABB(entity.getX() - r, entity.getY() - 3.0D, entity.getZ() - r,
				entity.getX() + r, entity.getY() + 7.0D, entity.getZ() + r);
		List<LivingEntity> targets = entity.level().getEntitiesOfClass(LivingEntity.class, search,
				e -> e.isAlive() && !(e instanceof Player));
		targets.sort(Comparator.comparingDouble(e -> e.distanceToSqr(entity)));

		Vec3 origin = new Vec3(0.0D, 2.25D, 0.0D);
		Matrix4f mat = poseStack.last().pose();
		int maxChains = Math.min(6, targets.size());
		int chainAlpha = Mth.clamp(Math.round(190.0F * fade), 0, 220);

		for (int i = 0; i < maxChains; i++) {
			LivingEntity target = targets.get(i);
			Vec3 relTarget = new Vec3(
					target.getX() - entity.getX(),
					target.getY() + target.getBbHeight() * 0.55D - entity.getY(),
					target.getZ() - entity.getZ());
			Vec3 mid = origin.add(relTarget).scale(0.5D).add(0.0D, 0.65D + 0.18D * Mth.sin(age * 0.25F + i), 0.0D);
			drawEnergyRibbonCurve(mat, chainVertex, origin, mid, relTarget, 0.075F, 255, 224, 96, chainAlpha, 255, 250, 200);
		}

		// Initial holy light pillar on deployment
		if (standby < 26.0F) {
			float pt = 1.0F - standby / 26.0F;
			drawLightPillar(poseStack, chainVertex, 1.35F * pt + 0.35F, 14.0F, 255, 226, 110, (int) (145.0F * pt * fade));
		}
	}

	private void renderKiraVfx(float age, float standby, float range, float fade,
			PoseStack poseStack, MultiBufferSource buffer) {
		VertexConsumer ringVertex = buffer.getBuffer(RenderType.entityTranslucent(RING_TEX));
		int alpha = Mth.clamp(Math.round(215.0F * fade), 0, 255);

		// Twin counter-rotating tilted Aurora Prism Rings orbiting the cross
		poseStack.pushPose();
		poseStack.translate(0.0D, 1.95D, 0.0D);
		poseStack.mulPose(Axis.ZP.rotationDegrees(24.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(age * 4.2F));
		drawHorizontalQuad(poseStack.last().pose(), ringVertex, 2.65F, 92, 232, 255, alpha);
		poseStack.popPose();

		poseStack.pushPose();
		poseStack.translate(0.0D, 1.95D, 0.0D);
		poseStack.mulPose(Axis.XP.rotationDegrees(-24.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(-age * 5.1F));
		drawHorizontalQuad(poseStack.last().pose(), ringVertex, 2.05F, 205, 248, 255, (int) (alpha * 0.85F));
		poseStack.popPose();

		// Soft azure sanctuary light pillar rising continuously from the crystal core
		VertexConsumer beamVertex = buffer.getBuffer(RenderType.lightning());
		float pulse = 0.78F + 0.22F * Mth.sin(age * 0.15F);
		drawLightPillar(poseStack, beamVertex, 0.95F, 9.5F, 72, 218, 255, Mth.clamp(Math.round(85.0F * pulse * fade), 0, 140));
	}

	private void renderSakuraVfx(JudahOathEntity entity, float age, float standby, float fade,
			PoseStack poseStack, MultiBufferSource buffer) {
		VertexConsumer lineVertex = buffer.getBuffer(RenderType.lightning());
		Matrix4f mat = poseStack.last().pose();

		// Glowing 3D Sakura Pentagram magic array on the ground (radius 11.0, rotates per wave)
		int wave = standby >= 94.0F ? 2 : (standby >= 56.0F ? 1 : 0);
		double baseAngle = Math.toRadians(entity.getDeployYaw() + wave * 36.0D);
		double radius = 11.0D;
		double step = Math.PI * 2.0D / 5.0D;
		Vec3[] pts = new Vec3[5];
		for (int i = 0; i < 5; i++) {
			double a = baseAngle + step * i;
			pts[i] = new Vec3(Math.cos(a) * radius, 0.14D, Math.sin(a) * radius);
		}

		float open = Mth.clamp(standby / 18.0F, 0.0F, 1.0F);
		float pulse = 0.78F + 0.22F * Mth.sin(age * 0.22F);
		int starAlpha = Mth.clamp(Math.round(165.0F * open * pulse * fade), 0, 215);
		for (int i = 0; i < 5; i++) {
			Vec3 p0 = pts[i].scale(open);
			Vec3 p1 = pts[(i + 2) % 5].scale(open);
			drawFlatBeam(mat, lineVertex,
					(float) p0.x, 0.14F, (float) p0.z,
					(float) p1.x, 0.14F, (float) p1.z,
					0.16F, 255, 118, 168, starAlpha);
		}

		// Subtle rose-gold blossom aura pillar at center
		drawLightPillar(poseStack, lineVertex, 0.85F, 7.5F, 255, 134, 178, Mth.clamp(Math.round(78.0F * pulse * fade), 0, 130));
	}

	private static void drawEnergyRibbonCurve(Matrix4f mat, VertexConsumer v, Vec3 p0, Vec3 p1, Vec3 p2,
			float halfW, int r, int g, int b, int a, int cr, int cg, int cb) {
		int segs = 6;
		Vec3 prev = p0;
		for (int s = 1; s <= segs; s++) {
			float t = (float) s / segs;
			float it = 1.0F - t;
			Vec3 cur = new Vec3(
					it * it * p0.x + 2.0F * it * t * p1.x + t * t * p2.x,
					it * it * p0.y + 2.0F * it * t * p1.y + t * t * p2.y,
					it * it * p0.z + 2.0F * it * t * p1.z + t * t * p2.z);
			drawRibbonSegment(mat, v, prev, cur, halfW, r, g, b, a);
			drawRibbonSegment(mat, v, prev, cur, halfW * 0.45F, cr, cg, cb, Math.min(255, a + 35));
			prev = cur;
		}
	}

	private static void drawRibbonSegment(Matrix4f mat, VertexConsumer v, Vec3 a, Vec3 b, float hw, int r, int g, int bl, int al) {
		float ax = (float) a.x, ay = (float) a.y, az = (float) a.z;
		float bx = (float) b.x, by = (float) b.y, bz = (float) b.z;
		// Vertical ribbon
		v.addVertex(mat, ax, ay - hw, az).setColor(r, g, bl, al);
		v.addVertex(mat, bx, by - hw, bz).setColor(r, g, bl, al);
		v.addVertex(mat, bx, by + hw, bz).setColor(r, g, bl, al);
		v.addVertex(mat, ax, ay + hw, az).setColor(r, g, bl, al);
		// Horizontal ribbon
		float dx = bx - ax;
		float dz = bz - az;
		float len = Mth.sqrt(dx * dx + dz * dz);
		float px = len > 1.0E-4F ? (-dz / len) * hw : hw;
		float pz = len > 1.0E-4F ? (dx / len) * hw : 0.0F;
		v.addVertex(mat, ax - px, ay, az - pz).setColor(r, g, bl, al);
		v.addVertex(mat, bx - px, by, bz - pz).setColor(r, g, bl, al);
		v.addVertex(mat, bx + px, by, bz + pz).setColor(r, g, bl, al);
		v.addVertex(mat, ax + px, ay, az + pz).setColor(r, g, bl, al);
	}

	private static void drawFlatBeam(Matrix4f mat, VertexConsumer v, float x0, float y0, float z0,
			float x1, float y1, float z1, float hw, int r, int g, int b, int a) {
		float dx = x1 - x0;
		float dz = z1 - z0;
		float len = Mth.sqrt(dx * dx + dz * dz);
		if (len < 1.0E-4F) {
			return;
		}
		float px = (-dz / len) * hw;
		float pz = (dx / len) * hw;
		v.addVertex(mat, x0 - px, y0, z0 - pz).setColor(r, g, b, a);
		v.addVertex(mat, x1 - px, y1, z1 - pz).setColor(r, g, b, a);
		v.addVertex(mat, x1 + px, y1, z1 + pz).setColor(r, g, b, a);
		v.addVertex(mat, x0 + px, y0, z0 + pz).setColor(r, g, b, a);
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
	public ResourceLocation getTextureLocation(JudahOathEntity entity) {
		return switch (entity.getVariant()) {
			case KIRA -> TEX_KIRA;
			case SAKURA -> TEX_SAKURA;
			default -> TEX_JUDAH;
		};
	}
}
