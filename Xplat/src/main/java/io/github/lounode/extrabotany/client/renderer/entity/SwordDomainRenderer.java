package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

import io.github.lounode.extrabotany.common.entity.SwordDomainEntity;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class SwordDomainRenderer extends EntityRenderer<SwordDomainEntity> {
	private static final ResourceLocation[] TEXTURES = new ResourceLocation[10];

	static {
		for (int i = 0; i < TEXTURES.length; i++) {
			TEXTURES[i] = prefix("textures/item/sworddomain_" + i + ".png");
		}
	}

	public SwordDomainRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(SwordDomainEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		ResourceLocation texture = getTextureLocation(entity);
		Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
		double dx = camera.x - entity.getX();
		double dz = camera.z - entity.getZ();
		float faceCamera = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
		float age = entity.tickCount + partialTick;

		poseStack.pushPose();
		poseStack.translate(0.0D, 0.9D, 0.0D);
		poseStack.mulPose(Axis.YP.rotationDegrees(faceCamera));
		poseStack.mulPose(Axis.ZP.rotationDegrees(age * 6.0F));
		float scale = 1.2F + Math.min(age / 30.0F, 1.0F) * 0.6F;
		poseStack.scale(scale, scale, scale);
		VertexConsumer vertex = buffer.getBuffer(RenderType.entityTranslucent(texture));
		Matrix4f matrix = poseStack.last().pose();
		quad(matrix, vertex, -0.5F, -0.5F, 0.5F, 0.5F, 0xEEFFFFFF);
		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	private static void quad(Matrix4f matrix, VertexConsumer vertex, float minX, float minY, float maxX, float maxY, int color) {
		vertex.addVertex(matrix, minX, minY, 0.0F).setColor(color).setUv(0.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, 1.0F);
		vertex.addVertex(matrix, maxX, minY, 0.0F).setColor(color).setUv(1.0F, 1.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, 1.0F);
		vertex.addVertex(matrix, maxX, maxY, 0.0F).setColor(color).setUv(1.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, 1.0F);
		vertex.addVertex(matrix, minX, maxY, 0.0F).setColor(color).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(0.0F, 0.0F, 1.0F);
	}

	@Override
	public ResourceLocation getTextureLocation(SwordDomainEntity entity) {
		return TEXTURES[Math.floorMod(entity.getDomainType(), TEXTURES.length)];
	}
}
