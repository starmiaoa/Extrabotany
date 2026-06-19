package io.github.lounode.extrabotany.client.renderer.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import io.github.lounode.extrabotany.common.entity.EgoMinionEntity;

public class EgoMinionRenderer extends HumanoidMobRenderer<EgoMinionEntity, HumanoidModel<EgoMinionEntity>> {
	public EgoMinionRenderer(EntityRendererProvider.Context ctx) {
		super(ctx, new HumanoidModel<>(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5F);
	}

	@NotNull
	@Override
	public ResourceLocation getTextureLocation(@NotNull EgoMinionEntity entity) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.getCameraEntity() instanceof AbstractClientPlayer clientPlayer) {
			return clientPlayer.getSkin().texture();
		}
		return DefaultPlayerSkin.get(entity.getUUID()).texture();
	}
}
