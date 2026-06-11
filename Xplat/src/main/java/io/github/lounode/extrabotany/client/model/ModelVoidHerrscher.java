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
						.texOffs(0, 64).mirror().addBox(0.0F, 0.0F, 0.0F, 26.0F, 32.0F, 0.0F),
				PartPose.offsetAndRotation(-30.0F, -11.0F, 5.0F, 0.0F, 0.0F, -0.1745329F));
		root.addOrReplaceChild("right_wing", CubeListBuilder.create()
						.texOffs(52, 64).mirror().addBox(0.0F, 0.0F, 0.0F, 26.0F, 32.0F, 0.0F),
				PartPose.offsetAndRotation(4.0F, -15.8F, 5.0F, 0.0F, 0.0F, 0.1745329F));
		for (int i = 1; i <= 3; i++) {
			root.addOrReplaceChild("shield" + i, CubeListBuilder.create()
							.texOffs(0, 113).addBox(-6.0F, -1.0F, 9.0F, 12.0F, 11.0F, 0.0F),
					PartPose.ZERO);
		}
		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(VoidHerrscher entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		copyProperties();

		boolean rankII = entity.isRankIIRenderState();
		leftWing.visible = rankII;
		rightWing.visible = rankII;

		int shields = entity.getRotatingShieldsRenderState();
		shield1.visible = shields > 0;
		shield2.visible = shields > 1;
		shield3.visible = shields > 2;
		if (shields > 0) {
			float divisor = Math.max(1, shields);
			float base = Mth.sin(ageInTicks * 0.1F);
			shield1.yRot = base + Mth.TWO_PI / divisor;
			shield2.yRot = base + Mth.TWO_PI / divisor * 2.0F;
			shield3.yRot = base + Mth.TWO_PI / divisor * 3.0F;
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
