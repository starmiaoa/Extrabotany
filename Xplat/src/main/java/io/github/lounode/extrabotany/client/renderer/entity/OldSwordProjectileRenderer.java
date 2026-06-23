package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.client.ClientHooks;

import io.github.lounode.extrabotany.client.core.ExtraBotanyModels;
import io.github.lounode.extrabotany.common.entity.OldSwordProjectileEntity;

public class OldSwordProjectileRenderer<T extends OldSwordProjectileEntity> extends EntityRenderer<T> {
	private static final int FULLBRIGHT = 0xF000F0;
	private static final int COLOR = 0xFFFFFF | ((int) (0.9F * 255F) << 24);
	private final ResourceLocation modelLocation;

	public OldSwordProjectileRenderer(EntityRendererProvider.Context context, ResourceLocation modelLocation) {
		super(context);
		this.modelLocation = modelLocation;
	}

	@Override
	public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		BakedModel model = ExtraBotanyModels.INSTANCE.getBakedModel(this.modelLocation);
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
		model = ClientHooks.handleCameraTransforms(poseStack, model, ItemDisplayContext.NONE, false);
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
		float red = (float) (COLOR >> 16 & 255) / 255.0F;
		float green = (float) (COLOR >> 8 & 255) / 255.0F;
		float blue = (float) (COLOR & 255) / 255.0F;
		float alpha = ((COLOR >> 24) & 255) / 255.0F;
		for (BakedQuad quad : quads) {
			vertex.putBulkData(poseStack.last(), quad, red, green, blue, alpha, FULLBRIGHT, OverlayTexture.NO_OVERLAY);
		}
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return this.modelLocation;
	}
}
