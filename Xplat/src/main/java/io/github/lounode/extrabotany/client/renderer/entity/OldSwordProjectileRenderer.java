package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import io.github.lounode.extrabotany.common.entity.OldSwordProjectileEntity;

/**
 * 老剑投射物(真泰拉/真影刃/波涌)的渲染器。
 *
 * <p>这些投射物原本通过 {@code getModelManager().getModel(icon/xxx_projectile#standalone)} 取模型,
 * 但 ATM9 的 ModernFix dynamic_resources 不会保留这些 standalone 模型 → 取到缺失模型(紫黑)或 null(全透明)。
 * 改为直接渲染**对应 relic 物品**的烘焙模型({@link net.minecraft.client.renderer.ItemModelShaper#getItemModel}):
 * 物品模型走核心物品管线、必定烘焙,且投射物模型本就是 {@code item/generated + 同贴图},视觉一致。
 */
public class OldSwordProjectileRenderer<T extends OldSwordProjectileEntity> extends EntityRenderer<T> {
	private static final int FULLBRIGHT = 0xF000F0;
	private static final int OVERLAY = OverlayTexture.NO_OVERLAY;
	private static final float ALPHA = 0.9F;
	private final Item item;

	public OldSwordProjectileRenderer(EntityRendererProvider.Context context, Item item) {
		super(context);
		this.item = item;
	}

	@Override
	public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		BakedModel model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(this.item);
		if (model == null) {
			return;
		}
		poseStack.pushPose();
		poseStack.scale(1.2F, 1.2F, 1.2F);
		// Net Y rotation must equal -atan2(dz,dx) like the 1.16 original. The entity yaw is the
		// vanilla atan2(dx,dz) = 90 - atan2(dz,dx), so subtract 90 (not add) to avoid the 180° flip
		// that made the blade fly backwards.
		poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot() - 90F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getXRot()));
		poseStack.mulPose(Axis.ZP.rotationDegrees(-45F));
		poseStack.translate(-0.5F, -0.5F, -0.5F);
		renderModel(model, poseStack, buffer.getBuffer(Sheets.translucentItemSheet()));
		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	private static void renderModel(BakedModel model, PoseStack poseStack, VertexConsumer vertex) {
		RandomSource random = RandomSource.create();
		for (Direction direction : Direction.values()) {
			random.setSeed(42L);
			renderQuads(model.getQuads((BlockState) null, direction, random), poseStack, vertex);
		}
		random.setSeed(42L);
		renderQuads(model.getQuads((BlockState) null, null, random), poseStack, vertex);
	}

	private static void renderQuads(Iterable<BakedQuad> quads, PoseStack poseStack, VertexConsumer vertex) {
		for (BakedQuad quad : quads) {
			renderQuad(quad, poseStack.last(), vertex);
		}
	}

	private static void renderQuad(BakedQuad quad, PoseStack.Pose pose, VertexConsumer vertex) {
		int[] data = quad.getVertices();
		Matrix4f matrix = pose.pose();
		Matrix3f normalMatrix = pose.normal();
		Vector3f normal = quad.getDirection().step();
		normal.mul(normalMatrix);
		for (int i = 0; i < 4; i++) {
			int offset = i * 8;
			float x = Float.intBitsToFloat(data[offset]);
			float y = Float.intBitsToFloat(data[offset + 1]);
			float z = Float.intBitsToFloat(data[offset + 2]);
			float u = Float.intBitsToFloat(data[offset + 4]);
			float v = Float.intBitsToFloat(data[offset + 5]);
			vertex.vertex(matrix, x, y, z)
					.color(1.0F, 1.0F, 1.0F, ALPHA)
					.uv(u, v)
					.overlayCoords(OVERLAY)
					.uv2(FULLBRIGHT)
					.normal(normal.x(), normal.y(), normal.z())
					.endVertex();
		}
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return InventoryMenu.BLOCK_ATLAS;
	}
}
