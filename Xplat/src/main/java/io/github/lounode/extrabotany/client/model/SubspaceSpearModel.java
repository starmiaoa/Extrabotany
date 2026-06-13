package io.github.lounode.extrabotany.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import io.github.lounode.extrabotany.common.entity.SubspaceSpearEntity;

public class SubspaceSpearModel extends EntityModel<SubspaceSpearEntity> {
	private final ModelPart spear;

	public SubspaceSpearModel(ModelPart root) {
		this.spear = root.getChild("spear");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition spear = root.addOrReplaceChild("spear", CubeListBuilder.create()
				.texOffs(10, 0).addBox(0.0F, -0.5F, 0.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(10, 0).addBox(0.0F, 0.0F, -0.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(10, 0).addBox(-0.5F, 0.0F, 0.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(26, 0).addBox(0.5F, 7.0F, 0.5F, 1.0F, 16.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(26, 0).addBox(0.0F, 8.0F, 0.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(26, 0).addBox(1.0F, 6.0F, 1.0F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(26, 0).addBox(1.0F, 5.0F, 0.0F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(26, 0).addBox(0.0F, 7.0F, 1.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(0.0F, -12.0F, 0.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(1.0F, -14.0F, 0.0F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(0.0F, -16.0F, 1.0F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(1.0F, -20.0F, 1.0F, 1.0F, 14.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(0.5F, -24.0F, 0.5F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.ZERO);
		spear.addOrReplaceChild("shape4", CubeListBuilder.create()
				.texOffs(5, 0).addBox(0.0F, -0.5F, -2.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.rotation(-0.7853982F, 0.2617994F, 0.0F));
		spear.addOrReplaceChild("shape5", CubeListBuilder.create()
				.texOffs(5, 0).addBox(0.0F, 0.0F, -2.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.rotation(0.1919862F, 0.2617994F, 0.0F));
		spear.addOrReplaceChild("shape6", CubeListBuilder.create()
				.texOffs(21, 0).addBox(0.0F, -1.5F, 3.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.rotation(0.1919862F, 0.2617994F, 0.0F));
		spear.addOrReplaceChild("shape7", CubeListBuilder.create()
				.texOffs(21, 0).addBox(0.0F, -1.5F, 4.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.rotation(-0.7853982F, 0.2617994F, 0.0F));
		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	public void setupAnim(SubspaceSpearEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		this.spear.render(poseStack, buffer, packedLight, packedOverlay, color);
	}
}
