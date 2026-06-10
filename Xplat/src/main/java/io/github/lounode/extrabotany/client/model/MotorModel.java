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
import net.minecraft.util.Mth;

import io.github.lounode.extrabotany.common.entity.MotorEntity;

public class MotorModel extends EntityModel<MotorEntity> {
	private final ModelPart body;
	private final ModelPart frontWheel;
	private final ModelPart backWheel;

	public MotorModel(ModelPart root) {
		this.body = root.getChild("body");
		this.frontWheel = root.getChild("front_wheel");
		this.backWheel = root.getChild("back_wheel");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 23).addBox(-2.5F, -9.0F, -11.0F, 5.0F, 4.0F, 16.0F, new CubeDeformation(0F))
				.texOffs(0, 0).addBox(-3.0F, -5.0F, -11.0F, 6.0F, 2.0F, 21.0F, new CubeDeformation(0F))
				.texOffs(0, 32).addBox(-3.0F, -11.5F, -17.0F, 6.0F, 1.0F, 2.0F, new CubeDeformation(0F))
				.texOffs(36, 45).addBox(-3.0F, -11.5F, 13.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0F))
				.texOffs(81, 11).addBox(-3.0F, -14.0F, -7.5F, 6.0F, 3.0F, 7.0F, new CubeDeformation(0F))
				.texOffs(85, 77).addBox(-3.0F, -11.0F, 2.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0F))
				.texOffs(0, 63).addBox(-2.5F, -7.0F, 8.0F, 5.0F, 3.0F, 11.0F, new CubeDeformation(0F))
				.texOffs(46, 38).addBox(3.0F, -7.5F, -16.0F, 1.0F, 2.0F, 16.0F, new CubeDeformation(0F))
				.texOffs(46, 38).mirror().addBox(-4.0F, -7.5F, -16.0F, 1.0F, 2.0F, 16.0F, new CubeDeformation(0F))
				.texOffs(18, 45).addBox(3.0F, -4.5F, -16.0F, 1.0F, 2.0F, 16.0F, new CubeDeformation(0F))
				.texOffs(18, 45).mirror().addBox(-4.0F, -4.5F, -16.0F, 1.0F, 2.0F, 16.0F, new CubeDeformation(0F))
				.texOffs(58, 59).addBox(-6.5F, -4.0F, 9.0F, 3.0F, 3.0F, 13.0F, new CubeDeformation(0F))
				.texOffs(58, 59).mirror().addBox(3.5F, -4.0F, 9.0F, 3.0F, 3.0F, 13.0F, new CubeDeformation(0F)),
				PartPose.offset(0F, 24F, 0F));
		root.addOrReplaceChild("front_wheel", wheel(), PartPose.offset(0F, 24F, -21F));
		root.addOrReplaceChild("back_wheel", wheel(), PartPose.offset(0F, 24F, 12F));
		return LayerDefinition.create(mesh, 128, 128);
	}

	private static CubeListBuilder wheel() {
		return CubeListBuilder.create()
				.texOffs(68, 96).addBox(-3.0F, -5.0F, -2.0F, 6.0F, 10.0F, 4.0F, new CubeDeformation(0F))
				.texOffs(48, 92).addBox(-3.0F, -2.0F, -5.0F, 6.0F, 4.0F, 10.0F, new CubeDeformation(0F))
				.texOffs(18, 51).addBox(2.01F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0F))
				.texOffs(0, 51).addBox(-3.01F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0F));
	}

	@Override
	public void setupAnim(MotorEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		float wheelSpin = entity.getRidingTicks() * 0.7F;
		if (entity.getDeltaMovement().horizontalDistanceSqr() > 0.0001D) {
			this.frontWheel.xRot = wheelSpin;
			this.backWheel.xRot = wheelSpin;
		} else {
			this.frontWheel.xRot = Mth.lerp(0.15F, this.frontWheel.xRot, 0F);
			this.backWheel.xRot = Mth.lerp(0.15F, this.backWheel.xRot, 0F);
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
		body.render(poseStack, buffer, packedLight, packedOverlay, color);
		frontWheel.render(poseStack, buffer, packedLight, packedOverlay, color);
		backWheel.render(poseStack, buffer, packedLight, packedOverlay, color);
	}
}
