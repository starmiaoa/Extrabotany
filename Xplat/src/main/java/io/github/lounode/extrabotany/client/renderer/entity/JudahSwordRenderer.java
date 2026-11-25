package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

import io.github.lounode.extrabotany.common.entity.JudahSwordEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class JudahSwordRenderer extends EntityRenderer<JudahSwordEntity> {
	private static final ResourceLocation RING_TEX = prefix("textures/entity/judah_domain_ring.png");

	public JudahSwordRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public boolean shouldRender(JudahSwordEntity entity, Frustum camera, double camX, double camY, double camZ) {
		return true;
	}

	@Override
	public void render(JudahSwordEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		Vec3 start = entity.getStartPos();
		Vec3 end = entity.getEndPos();
		Vec3 dir = end.subtract(start);
		if (dir.lengthSqr() < 1.0E-4D) {
			return;
		}

		boolean detonated = entity.isDetonated();
		float fade = 1.0F;
		float detT = 0.0F;
		if (detonated) {
			detT = Mth.clamp((entity.getDetonateTicks() + partialTick) / (float) JudahSwordEntity.DETONATE_LINGER_TICKS, 0.0F, 1.0F);
			fade = 1.0F - detT * detT;
		}
		if (fade <= 0.02F) {
			return;
		}

		double curX = Mth.lerp(partialTick, entity.xOld, entity.getX());
		double curY = Mth.lerp(partialTick, entity.yOld, entity.getY());
		double curZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());

		Vec3 relStart = new Vec3(start.x - curX, start.y - curY, start.z - curZ);
		Vec3 relTip = Vec3.ZERO;

		VertexConsumer v = buffer.getBuffer(RenderType.lightning());
		Matrix4f worldMat = poseStack.last().pose();

		float outerW = detonated ? Mth.lerp(detT, 0.62F, 0.08F) : 0.24F;
		float coreW = detonated ? Mth.lerp(detT, 0.26F, 0.03F) : 0.09F;
		int outerA = Mth.clamp(Math.round((detonated ? 215.0F : 165.0F) * fade), 0, 235);
		int coreA = Mth.clamp(Math.round((detonated ? 245.0F : 220.0F) * fade), 0, 255);

		drawCrossedSlashRibbon(worldMat, v, relStart, relTip, outerW, 255, 68, 132, outerA, 255, 132, 184, outerA);
		drawCrossedSlashRibbon(worldMat, v, relStart, relTip, coreW, 255, 224, 238, coreA, 255, 252, 255, coreA);

		if (!detonated) {
			float yawRad = (float) Mth.atan2(dir.x, dir.z);
			poseStack.pushPose();
			poseStack.mulPose(Axis.YP.rotation(yawRad));
			Matrix4f bladeMat = poseStack.last().pose();

			drawBladeDiamond(bladeMat, v, 1.45F, 0.15F, -1.10F, 0.14F, 255, 250, 252, 240, 255, 160, 202, 210);
			drawBladeDiamond(bladeMat, v, 1.65F, 0.05F, -1.55F, 0.36F, 255, 118, 168, 175, 235, 36, 88, 120);
			poseStack.popPose();
		}

		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	private static void drawCrossedSlashRibbon(Matrix4f mat, VertexConsumer v, Vec3 a, Vec3 b, float hw,
			int r0, int g0, int b0, int a0, int r1, int g1, int b1, int a1) {
		float ax = (float) a.x, ay = (float) a.y, az = (float) a.z;
		float bx = (float) b.x, by = (float) b.y, bz = (float) b.z;
		float dx = bx - ax;
		float dz = bz - az;
		float len = Mth.sqrt(dx * dx + dz * dz);
		if (len < 0.05F) {
			return;
		}
		float px = (-dz / len) * hw;
		float pz = (dx / len) * hw;

		v.vertex(mat, ax - px, ay, az - pz).color(r0, g0, b0, a0).endVertex();
		v.vertex(mat, bx - px, by, bz - pz).color(r1, g1, b1, a1).endVertex();
		v.vertex(mat, bx + px, by, bz + pz).color(r1, g1, b1, a1).endVertex();
		v.vertex(mat, ax + px, ay, az + pz).color(r0, g0, b0, a0).endVertex();

		v.vertex(mat, ax, ay - hw * 0.75F, az).color(r0, g0, b0, a0).endVertex();
		v.vertex(mat, bx, by - hw * 0.75F, bz).color(r1, g1, b1, a1).endVertex();
		v.vertex(mat, bx, by + hw * 1.15F, bz).color(r1, g1, b1, a1).endVertex();
		v.vertex(mat, ax, ay + hw * 1.15F, az).color(r0, g0, b0, a0).endVertex();
	}

	private static void drawBladeDiamond(Matrix4f mat, VertexConsumer v, float zTip, float zMid, float zTail, float hw,
			int tr, int tg, int tb, int ta, int mr, int mg, int mb, int ma) {
		float endW = 0.02F;
		v.vertex(mat, -endW, 0.0F, zTip).color(tr, tg, tb, ta).endVertex();
		v.vertex(mat, -hw, 0.0F, zMid).color(mr, mg, mb, ma).endVertex();
		v.vertex(mat, hw, 0.0F, zMid).color(mr, mg, mb, ma).endVertex();
		v.vertex(mat, endW, 0.0F, zTip).color(tr, tg, tb, ta).endVertex();

		v.vertex(mat, -hw, 0.0F, zMid).color(mr, mg, mb, ma).endVertex();
		v.vertex(mat, -endW, 0.0F, zTail).color(mr, mg, mb, 0).endVertex();
		v.vertex(mat, endW, 0.0F, zTail).color(mr, mg, mb, 0).endVertex();
		v.vertex(mat, hw, 0.0F, zMid).color(mr, mg, mb, ma).endVertex();

		float vh = hw * 0.55F;
		v.vertex(mat, 0.0F, -endW, zTip).color(tr, tg, tb, ta).endVertex();
		v.vertex(mat, 0.0F, -vh, zMid).color(mr, mg, mb, ma).endVertex();
		v.vertex(mat, 0.0F, vh, zMid).color(mr, mg, mb, ma).endVertex();
		v.vertex(mat, 0.0F, endW, zTip).color(tr, tg, tb, ta).endVertex();

		v.vertex(mat, 0.0F, -vh, zMid).color(mr, mg, mb, ma).endVertex();
		v.vertex(mat, 0.0F, -endW, zTail).color(mr, mg, mb, 0).endVertex();
		v.vertex(mat, 0.0F, endW, zTail).color(mr, mg, mb, 0).endVertex();
		v.vertex(mat, 0.0F, vh, zMid).color(mr, mg, mb, ma).endVertex();
	}

	@Override
	public ResourceLocation getTextureLocation(JudahSwordEntity entity) {
		return RING_TEX;
	}
}
