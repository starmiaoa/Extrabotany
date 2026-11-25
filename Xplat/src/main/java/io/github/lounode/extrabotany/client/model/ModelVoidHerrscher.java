package io.github.lounode.extrabotany.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

import io.github.lounode.extrabotany.common.entity.gaia.VoidHerrscher;

public class ModelVoidHerrscher extends HumanoidModel<VoidHerrscher> {
	private final ModelPart leftArmWear;
	private final ModelPart rightArmWear;
	private final ModelPart leftLegWear;
	private final ModelPart rightLegWear;
	private final ModelPart bodyWear;
	private final ModelPart leftWing;
	private final ModelPart rightWing;
	private final ModelPart shield1;
	private final ModelPart shield2;
	private final ModelPart shield3;

	public ModelVoidHerrscher(ModelPart root) {
		super(root);
		this.leftArmWear = root.getChild("left_arm_wear");
		this.rightArmWear = root.getChild("right_arm_wear");
		this.leftLegWear = root.getChild("left_leg_wear");
		this.rightLegWear = root.getChild("right_leg_wear");
		this.bodyWear = root.getChild("body_wear");
		this.leftWing = root.getChild("left_wing");
		this.rightWing = root.getChild("right_wing");
		this.shield1 = root.getChild("shield1");
		this.shield2 = root.getChild("shield2");
		this.shield3 = root.getChild("shield3");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("left_arm_wear", CubeListBuilder.create()
						.texOffs(48, 48).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
				PartPose.offset(5.0F, 2.0F, 0.0F));
		root.addOrReplaceChild("right_arm_wear", CubeListBuilder.create()
						.texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
				PartPose.offset(-5.0F, 2.0F, 0.0F));
		root.addOrReplaceChild("left_leg_wear", CubeListBuilder.create()
						.texOffs(0, 48).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
				PartPose.offset(1.9F, 12.0F, 0.0F));
		root.addOrReplaceChild("right_leg_wear", CubeListBuilder.create()
						.texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
				PartPose.offset(-1.9F, 12.0F, 0.0F));
		root.addOrReplaceChild("body_wear", CubeListBuilder.create()
						.texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
				PartPose.ZERO);
		root.addOrReplaceChild("left_wing", CubeListBuilder.create()
						.texOffs(0, 64).addBox(0.0F, 0.0F, 0.0F, 26.0F, 32.0F, 0.0F),
				PartPose.offsetAndRotation(-30.0F, -11.0F, 5.0F, 0.0F, 0.0F, -0.1745329F));
		root.addOrReplaceChild("right_wing", CubeListBuilder.create()
						.texOffs(52, 64).addBox(0.0F, 0.0F, 0.0F, 26.0F, 32.0F, 0.0F),
				PartPose.offsetAndRotation(4.0F, -15.8F, 5.0F, 0.0F, 0.0F, 0.1745329F));
		for (int i = 1; i <= 3; i++) {
			root.addOrReplaceChild("shield" + i, CubeListBuilder.create()
							.texOffs(0, 113).addBox(-6.0F, -1.0F, 10.5F, 12.0F, 11.0F, 0.0F),
					PartPose.ZERO);
		}
		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(VoidHerrscher entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

		boolean rankII = entity.isRankIIRenderState();
		boolean rankIII = entity.isRankIIIRenderState();
		boolean transitioning = entity.isTransitioning();
		int castState = entity.getCastState();

		if (transitioning || castState == VoidHerrscher.CAST_JUDGE) {
			float sway = Mth.sin(ageInTicks * 0.2F) * 0.08F;
			rightArm.xRot = -2.35F + sway;
			rightArm.zRot = 0.48F;
			leftArm.xRot = -2.35F - sway;
			leftArm.zRot = -0.48F;
			rightLeg.xRot = 0.18F + sway * 0.5F;
			leftLeg.xRot = 0.08F - sway * 0.5F;
			rightLeg.zRot = 0.04F;
			leftLeg.zRot = -0.04F;
		} else if (castState == VoidHerrscher.CAST_BARRAGE || castState == VoidHerrscher.CAST_COLLAPSE) {
			rightArm.xRot = -1.65F + Mth.sin(ageInTicks * 0.35F) * 0.12F;
			rightArm.zRot = 0.18F;
			leftArm.xRot = -0.45F;
			leftArm.zRot = -0.32F;
		} else if (rankII) {
			float breath = Mth.sin(ageInTicks * 0.10F);
			rightArm.xRot = -0.22F + breath * 0.06F;
			rightArm.zRot = 0.24F + breath * 0.05F;
			leftArm.xRot = -0.22F - breath * 0.06F;
			leftArm.zRot = -0.24F - breath * 0.05F;
			rightLeg.xRot = 0.16F + breath * 0.05F;
			leftLeg.xRot = 0.06F - breath * 0.04F;
			rightLeg.zRot = 0.03F;
			leftLeg.zRot = -0.03F;
		}

		copyProperties();

		boolean showWings = rankII || (transitioning && entity.getTransitionTargetRank() >= 2 && entity.getTransitionProgress(0.0F) > 0.35F);
		leftWing.visible = showWings;
		rightWing.visible = showWings;
		if (showWings) {
			float flapSpeed = rankIII ? 0.14F : 0.10F;
			float flap = Mth.sin(ageInTicks * flapSpeed) * (rankIII ? 0.16F : 0.11F);
			float unfold = transitioning && !rankII
					? Mth.clamp((entity.getTransitionProgress(0.0F) - 0.35F) / 0.55F, 0.0F, 1.0F)
					: 1.0F;
			float sweep = Mth.lerp(unfold, 0.85F, 0.22F);
			leftWing.yRot = sweep + flap;
			rightWing.yRot = -(sweep + flap);
			leftWing.zRot = -0.1745329F - flap * 0.35F;
			rightWing.zRot = 0.1745329F + flap * 0.35F;
		}

		int shields = entity.getRotatingShieldsRenderState();
		shield1.visible = shields > 0;
		shield2.visible = shields > 1;
		shield3.visible = shields > 2;
		if (shields > 0) {
			float divisor = Math.max(1, shields);
			float orbit = ageInTicks * 0.085F;
			shield1.yRot = orbit + Mth.TWO_PI / divisor;
			shield2.yRot = orbit + Mth.TWO_PI / divisor * 2.0F;
			shield3.yRot = orbit + Mth.TWO_PI / divisor * 3.0F;
			shield1.y = Mth.sin(ageInTicks * 0.12F) * 1.4F;
			shield2.y = Mth.sin(ageInTicks * 0.12F + 2.094F) * 1.4F;
			shield3.y = Mth.sin(ageInTicks * 0.12F + 4.188F) * 1.4F;
		}
	}

	public void renderBody(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		bodyWear.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		leftArmWear.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		rightArmWear.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		leftLegWear.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		rightLegWear.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		renderBody(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void renderWings(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		leftWing.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		rightWing.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void renderShields(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		shield1.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		shield2.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		shield3.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	private void copyProperties() {
		body.copyFrom(this.body);
		leftArmWear.copyFrom(this.leftArm);
		rightArmWear.copyFrom(this.rightArm);
		leftLegWear.copyFrom(this.leftLeg);
		rightLegWear.copyFrom(this.rightLeg);
		bodyWear.copyFrom(this.body);
	}
}
