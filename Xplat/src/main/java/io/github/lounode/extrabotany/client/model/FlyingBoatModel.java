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

import io.github.lounode.extrabotany.common.entity.FlyingBoatEntity;

public class FlyingBoatModel extends EntityModel<FlyingBoatEntity> {
	private final ModelPart root;
	private final ModelPart leftPaddle;
	private final ModelPart rightPaddle;

	public FlyingBoatModel(ModelPart root) {
		this.root = root.getChild("boat");
		this.leftPaddle = this.root.getChild("left_paddle");
		this.rightPaddle = this.root.getChild("right_paddle");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition boat = root.addOrReplaceChild("boat", CubeListBuilder.create(), PartPose.ZERO);
		boat.addOrReplaceChild("bottom", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-14.0F, -3.0F, -8.0F, 28.0F, 4.0F, 16.0F, new CubeDeformation(0F)),
				PartPose.offset(0F, 4F, 0F));
		boat.addOrReplaceChild("back", CubeListBuilder.create()
				.texOffs(0, 20).addBox(-14.0F, -8.0F, -1.0F, 28.0F, 8.0F, 2.0F, new CubeDeformation(0F)),
				PartPose.offset(0F, 4F, 9F));
		boat.addOrReplaceChild("front", CubeListBuilder.create()
				.texOffs(0, 20).addBox(-14.0F, -8.0F, -1.0F, 28.0F, 8.0F, 2.0F, new CubeDeformation(0F)),
				PartPose.offsetAndRotation(0F, 4F, -9F, 0F, (float) Math.PI, 0F));
		boat.addOrReplaceChild("left", CubeListBuilder.create()
				.texOffs(0, 30).addBox(-1.0F, -8.0F, -8.0F, 2.0F, 8.0F, 16.0F, new CubeDeformation(0F)),
				PartPose.offsetAndRotation(15F, 4F, 0F, 0F, 0F, -0.12F));
		boat.addOrReplaceChild("right", CubeListBuilder.create()
				.texOffs(0, 30).addBox(-1.0F, -8.0F, -8.0F, 2.0F, 8.0F, 16.0F, new CubeDeformation(0F)),
				PartPose.offsetAndRotation(-15F, 4F, 0F, 0F, (float) Math.PI, 0.12F));
		boat.addOrReplaceChild("left_paddle", paddle(), PartPose.offsetAndRotation(16F, 1F, 0F, 0F, 0F, -0.45F));
		boat.addOrReplaceChild("right_paddle", paddle(), PartPose.offsetAndRotation(-16F, 1F, 0F, 0F, (float) Math.PI, 0.45F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	private static CubeListBuilder paddle() {
		return CubeListBuilder.create()
				.texOffs(40, 0).addBox(-1.0F, -12.0F, -1.0F, 2.0F, 18.0F, 2.0F, new CubeDeformation(0F))
				.texOffs(48, 0).addBox(-3.0F, -17.0F, -1.0F, 6.0F, 6.0F, 2.0F, new CubeDeformation(0F));
	}

	@Override
	public void setupAnim(FlyingBoatEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.leftPaddle.xRot = (float) Math.sin(entity.getRowingTime(0, limbSwing)) * 0.35F;
		this.rightPaddle.xRot = (float) Math.sin(entity.getRowingTime(1, limbSwing) + Math.PI) * 0.35F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		this.root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}
