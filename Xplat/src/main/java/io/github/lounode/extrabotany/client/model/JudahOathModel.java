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

import io.github.lounode.extrabotany.common.entity.JudahOathEntity;

public class JudahOathModel extends EntityModel<JudahOathEntity> {
	private final ModelPart body;
	private final ModelPart leftWing;
	private final ModelPart rightWing;
	private final ModelPart coreCrystal;
	private final ModelPart bodyGlow;

	public JudahOathModel(ModelPart root) {
		this.body = root.getChild("body");
		this.leftWing = root.getChild("left_wing");
		this.rightWing = root.getChild("right_wing");
		this.coreCrystal = root.getChild("core_crystal");
		this.bodyGlow = root.getChild("body_glow");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		// 1. Cruciform Battle-Cross Body (grounded flush at Y = 24.0F)
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-6.0F, -3.0F, -4.0F, 12.0F, 3.0F, 8.0F, CubeDeformation.NONE)
				.texOffs(41, 0).addBox(-4.5F, -6.0F, -3.5F, 9.0F, 3.0F, 7.0F, CubeDeformation.NONE)
				.texOffs(0, 12).addBox(-3.5F, -30.0F, -3.0F, 7.0F, 24.0F, 6.0F, CubeDeformation.NONE)
				.texOffs(27, 12).addBox(-2.0F, -28.0F, -3.6F, 4.0F, 20.0F, 1.0F, CubeDeformation.NONE)
				.texOffs(27, 12).addBox(-2.0F, -28.0F, 2.6F, 4.0F, 20.0F, 1.0F, CubeDeformation.NONE)
				.texOffs(74, 0).addBox(-4.5F, -33.0F, -3.5F, 9.0F, 3.0F, 7.0F, CubeDeformation.NONE)
				.texOffs(107, 0).addBox(-2.0F, -37.0F, -2.0F, 4.0F, 4.0F, 4.0F, CubeDeformation.NONE)
				.texOffs(107, 9).addBox(-1.0F, -40.0F, -1.0F, 2.0F, 3.0F, 2.0F, CubeDeformation.NONE)
				.texOffs(38, 12).addBox(-12.0F, -26.0F, -3.0F, 24.0F, 6.0F, 6.0F, CubeDeformation.NONE)
				.texOffs(99, 15).addBox(12.0F, -27.0F, -3.5F, 3.0F, 8.0F, 7.0F, CubeDeformation.NONE)
				.texOffs(99, 15).mirror().addBox(-15.0F, -27.0F, -3.5F, 3.0F, 8.0F, 7.0F, CubeDeformation.NONE)
				.texOffs(38, 25).addBox(-4.5F, -27.5F, -3.8F, 9.0F, 9.0F, 1.0F, CubeDeformation.NONE)
				.texOffs(38, 25).addBox(-4.5F, -27.5F, 2.8F, 9.0F, 9.0F, 1.0F, CubeDeformation.NONE),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		body.addOrReplaceChild("lower_strut_r", CubeListBuilder.create()
				.texOffs(59, 25).addBox(0.0F, 0.0F, -1.5F, 10.0F, 2.0F, 3.0F, CubeDeformation.NONE),
				PartPose.offsetAndRotation(3.0F, -16.0F, 0.0F, 0.0F, 0.0F, 0.58F));
		body.addOrReplaceChild("lower_strut_l", CubeListBuilder.create()
				.texOffs(59, 25).mirror().addBox(-10.0F, 0.0F, -1.5F, 10.0F, 2.0F, 3.0F, CubeDeformation.NONE),
				PartPose.offsetAndRotation(-3.0F, -16.0F, 0.0F, 0.0F, 0.0F, -0.58F));

		body.addOrReplaceChild("lower_ribbon_r", CubeListBuilder.create()
				.texOffs(86, 32).addBox(-1.0F, -1.5F, -1.0F, 5.0F, 3.0F, 2.0F, CubeDeformation.NONE),
				PartPose.offsetAndRotation(11.5F, -10.5F, 0.0F, 0.0F, 0.0F, -0.22F));
		body.addOrReplaceChild("lower_ribbon_l", CubeListBuilder.create()
				.texOffs(86, 32).mirror().addBox(-4.0F, -1.5F, -1.0F, 5.0F, 3.0F, 2.0F, CubeDeformation.NONE),
				PartPose.offsetAndRotation(-11.5F, -10.5F, 0.0F, 0.0F, 0.0F, 0.22F));

		// 2. Deployable Mechanical Wing-Caskets
		root.addOrReplaceChild("right_wing", CubeListBuilder.create()
				.texOffs(0, 44).addBox(0.0F, -2.0F, -3.5F, 3.0F, 14.0F, 7.0F, CubeDeformation.NONE)
				.texOffs(21, 44).addBox(2.5F, 1.0F, -2.0F, 2.0F, 10.0F, 4.0F, CubeDeformation.NONE),
				PartPose.offset(3.5F, 4.0F, 0.0F));
		root.addOrReplaceChild("left_wing", CubeListBuilder.create()
				.texOffs(0, 44).mirror().addBox(-3.0F, -2.0F, -3.5F, 3.0F, 14.0F, 7.0F, CubeDeformation.NONE)
				.texOffs(21, 44).mirror().addBox(-4.5F, 1.0F, -2.0F, 2.0F, 10.0F, 4.0F, CubeDeformation.NONE),
				PartPose.offset(-3.5F, 4.0F, 0.0F));

		// 3. Floating Emissive Relic Core Crystal
		root.addOrReplaceChild("core_crystal", CubeListBuilder.create()
				.texOffs(36, 44).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 4.0F, CubeDeformation.NONE)
				.texOffs(53, 44).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 2.0F, 6.0F, CubeDeformation.NONE),
				PartPose.offset(0.0F, -18.0F, 0.0F));

		// 4. Fullbright Emissive Rune Veins & Core Accents
		root.addOrReplaceChild("body_glow", CubeListBuilder.create()
				.texOffs(0, 68).addBox(-1.0F, -29.0F, -3.9F, 2.0F, 22.0F, 1.0F, CubeDeformation.NONE)
				.texOffs(0, 68).addBox(-1.0F, -29.0F, 2.9F, 2.0F, 22.0F, 1.0F, CubeDeformation.NONE)
				.texOffs(8, 68).addBox(-11.0F, -24.0F, -3.5F, 22.0F, 2.0F, 1.0F, CubeDeformation.NONE)
				.texOffs(8, 68).addBox(-11.0F, -24.0F, 2.5F, 22.0F, 2.0F, 1.0F, CubeDeformation.NONE)
				.texOffs(8, 72).addBox(-2.5F, -25.5F, -4.1F, 5.0F, 5.0F, 1.0F, CubeDeformation.NONE)
				.texOffs(8, 72).addBox(-2.5F, -25.5F, 3.1F, 5.0F, 5.0F, 1.0F, CubeDeformation.NONE)
				.texOffs(22, 72).addBox(-1.5F, -36.5F, -2.3F, 3.0F, 3.0F, 1.0F, CubeDeformation.NONE)
				.texOffs(22, 72).addBox(-1.5F, -36.5F, 1.3F, 3.0F, 3.0F, 1.0F, CubeDeformation.NONE)
				.texOffs(32, 72).addBox(-5.0F, -3.5F, -4.3F, 10.0F, 1.0F, 1.0F, CubeDeformation.NONE)
				.texOffs(32, 72).addBox(-5.0F, -3.5F, 3.3F, 10.0F, 1.0F, 1.0F, CubeDeformation.NONE),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(JudahOathEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		float unfold = entity.getUnfoldProgress(ageInTicks - entity.tickCount);
		float eased = 1.0F - (1.0F - unfold) * (1.0F - unfold);

		this.rightWing.x = 3.5F + eased * 3.8F;
		this.rightWing.zRot = -eased * 0.36F;
		this.rightWing.yRot = -eased * 0.28F;

		this.leftWing.x = -3.5F - eased * 3.8F;
		this.leftWing.zRot = eased * 0.36F;
		this.leftWing.yRot = eased * 0.28F;

		this.coreCrystal.visible = unfold > 0.02F;
		this.coreCrystal.y = -15.0F - eased * 6.5F + Mth.sin(ageInTicks * 0.16F) * 1.1F;
		this.coreCrystal.yRot = ageInTicks * 0.11F;
		this.coreCrystal.xRot = Mth.sin(ageInTicks * 0.08F) * 0.18F;
	}

	public void renderBase(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float r, float g, float b, float a) {
		this.body.render(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);
		this.leftWing.render(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);
		this.rightWing.render(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);
	}

	public void renderGlow(PoseStack poseStack, VertexConsumer buffer, int packedOverlay, float r, float g, float b, float a) {
		int fullBright = 0x00F000F0;
		this.bodyGlow.render(poseStack, buffer, fullBright, packedOverlay, r, g, b, a);
		if (this.coreCrystal.visible) {
			this.coreCrystal.render(poseStack, buffer, fullBright, packedOverlay, r, g, b, a);
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float r, float g, float b, float a) {
		renderBase(poseStack, buffer, packedLight, packedOverlay, r, g, b, a);
		renderGlow(poseStack, buffer, packedOverlay, r, g, b, a);
	}
}
