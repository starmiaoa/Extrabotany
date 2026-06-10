package io.github.lounode.extrabotany.client.core;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import io.github.lounode.extrabotany.api.item.CoreOfTheVoidVariant;

import java.util.Map;
import java.util.function.Consumer;

public interface ClientCoreOfTheVoidVariant extends CoreOfTheVoidVariant {

	void onModelInit(Map<ResourceLocation, Consumer<BakedModel>> consumer);

	void render(HumanoidModel<?> bipedModel, ItemStack stack, LivingEntity living, PoseStack ms, MultiBufferSource buffers, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch);
}
