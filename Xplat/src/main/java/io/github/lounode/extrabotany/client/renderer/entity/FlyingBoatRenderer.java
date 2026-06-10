package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import io.github.lounode.extrabotany.client.model.ExtrabotanyModelLayers;
import io.github.lounode.extrabotany.client.model.FlyingBoatModel;
import io.github.lounode.extrabotany.common.entity.FlyingBoatEntity;
import io.github.lounode.extrabotany.common.item.equipment.tool.FlyingBoatItem;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class FlyingBoatRenderer extends EntityRenderer<FlyingBoatEntity> {
	private static final ResourceLocation MANASTEEL = prefix("textures/entity/flying_boat/manasteel.png");
	private static final ResourceLocation ELEMENTIUM = prefix("textures/entity/flying_boat/elementium.png");
	private static final ResourceLocation TERRASTEEL = prefix("textures/entity/flying_boat/terrasteel.png");
	private final FlyingBoatModel model;

	public FlyingBoatRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.5F;
		this.model = new FlyingBoatModel(context.bakeLayer(ExtrabotanyModelLayers.FLYING_BOAT));
	}

	@Override
	public void render(FlyingBoatEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		poseStack.pushPose();
		poseStack.translate(0.0D, 0.55D, 0.0D);
		poseStack.mulPose(Axis.YP.rotationDegrees(180F - entityYaw));
		float timeSinceHit = (float) entity.getTimeSinceHit() - partialTick;
		float damageTaken = entity.getDamageTaken() - partialTick;
		if (damageTaken < 0F) {
			damageTaken = 0F;
		}
		if (timeSinceHit > 0F) {
			poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(timeSinceHit) * timeSinceHit * damageTaken / 10F * (float) entity.getForwardDirection()));
		}
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		VertexConsumer vertex = buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
		this.model.setupAnim(entity, partialTick, 0F, entity.tickCount + partialTick, 0F, 0F);
		this.model.renderToBuffer(poseStack, vertex, packedLight, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
		poseStack.popPose();
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
	}

	@Override
	public ResourceLocation getTextureLocation(FlyingBoatEntity entity) {
		FlyingBoatItem.Variant variant = entity.getVariant();
		return switch (variant) {
			case ELEMENTIUM -> ELEMENTIUM;
			case TERRASTEEL -> TERRASTEEL;
			case MANASTEEL -> MANASTEEL;
		};
	}
}
