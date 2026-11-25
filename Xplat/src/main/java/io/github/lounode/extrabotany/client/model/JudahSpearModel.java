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

import io.github.lounode.extrabotany.common.entity.JudahSpearEntity;

public class JudahSpearModel extends EntityModel<JudahSpearEntity> {
	private final ModelPart rootPart;

	public JudahSpearModel(ModelPart root) {
		this.rootPart = root.getChild("spear");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		PartDefinition spear = root.addOrReplaceChild("spear", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.0F, -12.0F, -1.0F, 2.0F, 24.0F, 2.0F, CubeDeformation.NONE)
				.texOffs(9, 0).addBox(-2.0F, -17.0F, -2.0F, 4.0F, 5.0F, 4.0F, CubeDeformation.NONE)
				.texOffs(26, 0).addBox(-1.5F, -22.0F, -1.5F, 3.0F, 5.0F, 3.0F, CubeDeformation.NONE)
				.texOffs(39, 0).addBox(-1.0F, -27.0F, -1.0F, 2.0F, 5.0F, 2.0F, CubeDeformation.NONE)
				.texOffs(9, 10).addBox(-2.5F, -13.0F, -2.5F, 5.0F, 2.0F, 5.0F, CubeDeformation.NONE)
				.texOffs(38, 10).addBox(-1.5F, 12.0F, -1.5F, 3.0F, 4.0F, 3.0F, CubeDeformation.NONE)
				.texOffs(9, 18).addBox(-0.5F, 14.0F, -2.5F, 1.0F, 5.0F, 5.0F, CubeDeformation.NONE)
				.texOffs(22, 18).addBox(-2.5F, 14.0F, -0.5F, 5.0F, 5.0F, 1.0F, CubeDeformation.NONE),
				PartPose.ZERO);

		spear.addOrReplaceChild("barb_n", CubeListBuilder.create()
				.texOffs(30, 10).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 6.0F, 1.0F, CubeDeformation.NONE),
				PartPose.offsetAndRotation(0.0F, -13.0F, -2.0F, -0.38F, 0.0F, 0.0F));
		spear.addOrReplaceChild("barb_s", CubeListBuilder.create()
				.texOffs(30, 10).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 6.0F, 1.0F, CubeDeformation.NONE),
				PartPose.offsetAndRotation(0.0F, -13.0F, 2.0F, 0.38F, 0.0F, 0.0F));
		spear.addOrReplaceChild("barb_e", CubeListBuilder.create()
				.texOffs(30, 10).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 6.0F, 1.0F, CubeDeformation.NONE),
				PartPose.offsetAndRotation(2.0F, -13.0F, 0.0F, 0.38F, (float) (Math.PI * 0.5D), 0.0F));
		spear.addOrReplaceChild("barb_w", CubeListBuilder.create()
				.texOffs(30, 10).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 6.0F, 1.0F, CubeDeformation.NONE),
				PartPose.offsetAndRotation(-2.0F, -13.0F, 0.0F, -0.38F, (float) (Math.PI * 0.5D), 0.0F));

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(JudahSpearEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float r, float g, float b, float a) {
		this.rootPart.render(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);
	}
}
