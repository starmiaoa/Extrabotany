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
	private static final int WHEEL_SEGMENTS = 8;
	private static final float SEGMENT_STEP = Mth.TWO_PI / WHEEL_SEGMENTS;

	private final ModelPart body;
	private final ModelPart bodyGlow;
	private final ModelPart boostFlames;
	private final ModelPart frontWheel;
	private final ModelPart frontWheelGlow;
	private final ModelPart backWheel;
	private final ModelPart backWheelGlow;
	private final ModelPart leftWingPod;
	private final ModelPart leftWingPodGlow;
	private final ModelPart rightWingPod;
	private final ModelPart rightWingPodGlow;

	private float glowRed = 1.0F;
	private float glowGreen = 1.0F;
	private float glowBlue = 1.0F;

	public MotorModel(ModelPart root) {
		this.body = root.getChild("body");
		this.bodyGlow = root.getChild("body_glow");
		this.boostFlames = this.bodyGlow.getChild("boost_flames");
		this.frontWheel = root.getChild("front_wheel");
		this.frontWheelGlow = root.getChild("front_wheel_glow");
		this.backWheel = root.getChild("back_wheel");
		this.backWheelGlow = root.getChild("back_wheel_glow");
		this.leftWingPod = root.getChild("left_wing_pod");
		this.leftWingPodGlow = root.getChild("left_wing_pod_glow");
		this.rightWingPod = root.getChild("right_wing_pod");
		this.rightWingPodGlow = root.getChild("right_wing_pod_glow");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		// =========================================================================
		// 1. Sculpted Armored Chassis (Project Bunny 19c / Herrscher of Reason)
		// =========================================================================
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				// Lower dark titanium engine block & belly keel armor
				.texOffs(0, 0).addBox(-2.5F, -9.0F, -9.0F, 5.0F, 6.0F, 18.0F, CubeDeformation.NONE)
				.texOffs(47, 0).addBox(-3.0F, -4.0F, -8.0F, 6.0F, 2.0F, 16.0F, CubeDeformation.NONE)
				// Rear twin swingarms connecting chassis to rear axle (z = 13, y = -5)
				.texOffs(58, 44).addBox(3.0F, -6.5F, 5.0F, 1.0F, 3.0F, 10.0F, CubeDeformation.NONE)
				.texOffs(58, 44).mirror().addBox(-4.0F, -6.5F, 5.0F, 1.0F, 3.0F, 10.0F, CubeDeformation.NONE)
				// Sculpted mecha fuel tank & upper cobalt crest
				.texOffs(92, 0).addBox(-3.5F, -14.0F, -9.0F, 7.0F, 5.0F, 9.0F, CubeDeformation.NONE)
				.texOffs(0, 58).addBox(-2.5F, -15.5F, -8.0F, 5.0F, 2.0F, 6.0F, CubeDeformation.NONE)
				// Layered pearl-white & cobalt side fairing cowls
				.texOffs(0, 25).addBox(2.5F, -13.0F, -11.0F, 2.0F, 7.0F, 11.0F, CubeDeformation.NONE)
				.texOffs(0, 25).mirror().addBox(-4.5F, -13.0F, -11.0F, 2.0F, 7.0F, 11.0F, CubeDeformation.NONE)
				// Sculpted driver saddle, elevated pillion seat, swept tail fin
				.texOffs(0, 44).addBox(-3.0F, -11.5F, -1.0F, 6.0F, 3.0F, 8.0F, CubeDeformation.NONE)
				.texOffs(29, 44).addBox(-3.0F, -13.0F, 7.0F, 6.0F, 4.0F, 8.0F, CubeDeformation.NONE)
				.texOffs(23, 58).addBox(-2.5F, -14.0F, 13.0F, 5.0F, 2.0F, 6.0F, CubeDeformation.NONE)
				// Twin rear quantum exhaust thrusters
				.texOffs(27, 25).addBox(3.5F, -10.0F, 9.0F, 3.0F, 4.0F, 11.0F, CubeDeformation.NONE)
				.texOffs(27, 25).mirror().addBox(-6.5F, -10.0F, 9.0F, 3.0F, 4.0F, 11.0F, CubeDeformation.NONE)
				// Cyber handlebars, steering stem, aero handguards
				.texOffs(92, 15).addBox(-2.0F, -16.0F, -11.0F, 4.0F, 2.0F, 3.0F, CubeDeformation.NONE)
				.texOffs(47, 19).addBox(-8.0F, -16.0F, -10.5F, 16.0F, 1.0F, 2.0F, CubeDeformation.NONE)
				.texOffs(107, 15).addBox(6.0F, -17.0F, -12.0F, 3.0F, 2.0F, 3.0F, CubeDeformation.NONE)
				.texOffs(107, 15).mirror().addBox(-9.0F, -17.0F, -12.0F, 3.0F, 2.0F, 3.0F, CubeDeformation.NONE),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		// Sloped aerodynamic front nose fairing (beak pitched forward toward front wheel)
		body.addOrReplaceChild("front_fairing", CubeListBuilder.create()
				.texOffs(56, 25).addBox(-3.5F, -1.5F, -9.0F, 7.0F, 4.0F, 9.0F, CubeDeformation.NONE)
				.texOffs(46, 58).addBox(-2.5F, -3.0F, -6.0F, 5.0F, 2.0F, 6.0F, CubeDeformation.NONE)
				.texOffs(69, 58).addBox(-2.5F, 2.0F, -8.0F, 5.0F, 2.0F, 6.0F, CubeDeformation.NONE),
				PartPose.offsetAndRotation(0.0F, -13.5F, -10.0F, 0.36F, 0.0F, 0.0F));

		// Twin angled inverted front forks reaching exact front axle (0, -5, -15)
		body.addOrReplaceChild("front_forks", CubeListBuilder.create()
				.texOffs(120, 25).addBox(2.5F, 0.0F, -1.0F, 1.0F, 10.0F, 2.0F, CubeDeformation.NONE)
				.texOffs(120, 25).mirror().addBox(-3.5F, 0.0F, -1.0F, 1.0F, 10.0F, 2.0F, CubeDeformation.NONE)
				.texOffs(92, 58).addBox(-3.0F, 4.0F, -2.5F, 6.0F, 2.0F, 5.0F, CubeDeformation.NONE),
				PartPose.offsetAndRotation(0.0F, -14.0F, -10.5F, -0.463F, 0.0F, 0.0F));

		// =========================================================================
		// 2. Emissive Konstrukt Cyan Energy Veins, Visor, HUD & Thruster Cores
		// =========================================================================
		PartDefinition bodyGlow = root.addOrReplaceChild("body_glow", CubeListBuilder.create()
				// Tank core diamond
				.texOffs(113, 78).addBox(-1.5F, -15.7F, -6.5F, 3.0F, 1.0F, 3.0F, CubeDeformation.NONE)
				// Side fairing energy veins
				.texOffs(0, 78).addBox(3.6F, -11.5F, -10.0F, 1.0F, 2.0F, 9.0F, CubeDeformation.NONE)
				.texOffs(0, 78).mirror().addBox(-4.6F, -11.5F, -10.0F, 1.0F, 2.0F, 9.0F, CubeDeformation.NONE)
				// Handlebar HUD hologram & glowing grips
				.texOffs(96, 84).addBox(-2.0F, -17.0F, -11.5F, 4.0F, 2.0F, 1.0F, CubeDeformation.NONE)
				.texOffs(116, 84).addBox(7.1F, -16.0F, -10.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.08F))
				.texOffs(116, 84).mirror().addBox(-8.1F, -16.0F, -10.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.08F))
				// Rear tail brake-energy strip & twin thruster cores
				.texOffs(44, 87).addBox(-2.0F, -13.6F, 18.2F, 4.0F, 1.0F, 1.0F, CubeDeformation.NONE)
				.texOffs(107, 84).addBox(4.0F, -9.0F, 19.2F, 2.0F, 2.0F, 2.0F, CubeDeformation.NONE)
				.texOffs(107, 84).mirror().addBox(-6.0F, -9.0F, 19.2F, 2.0F, 2.0F, 2.0F, CubeDeformation.NONE),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		// Headlight visor & nose V-crest energy strips (aligned with front_fairing)
		bodyGlow.addOrReplaceChild("front_fairing_glow", CubeListBuilder.create()
				.texOffs(81, 84).addBox(-3.0F, -0.5F, -9.2F, 6.0F, 2.0F, 1.0F, CubeDeformation.NONE)
				.texOffs(61, 78).addBox(1.5F, -1.7F, -8.5F, 1.0F, 1.0F, 6.0F, CubeDeformation.NONE)
				.texOffs(61, 78).mirror().addBox(-2.5F, -1.7F, -8.5F, 1.0F, 1.0F, 6.0F, CubeDeformation.NONE),
				PartPose.offsetAndRotation(0.0F, -13.5F, -10.0F, 0.36F, 0.0F, 0.0F));

		// Dynamic rear plasma boost jets (visible when boosting / charged / cyclone)
		bodyGlow.addOrReplaceChild("boost_flames", CubeListBuilder.create()
				.texOffs(44, 78).addBox(4.0F, -1.0F, 0.0F, 2.0F, 2.0F, 6.0F, CubeDeformation.NONE)
				.texOffs(44, 78).mirror().addBox(-6.0F, -1.0F, 0.0F, 2.0F, 2.0F, 6.0F, CubeDeformation.NONE),
				PartPose.offset(0.0F, -8.0F, 20.0F));

		// =========================================================================
		// 3. Hubless Octagonal Quantum Wheels (Exact ground contact at Y = 24.0F)
		// =========================================================================
		buildTireWheel(root.addOrReplaceChild("front_wheel", CubeListBuilder.create(),
				PartPose.offset(0.0F, 19.0F, -15.0F)), 0, 68, 5.0F);
		buildGlowWheel(root.addOrReplaceChild("front_wheel_glow", CubeListBuilder.create(),
				PartPose.offset(0.0F, 19.0F, -15.0F)), 81, 78, 4.0F);

		buildTireWheel(root.addOrReplaceChild("back_wheel", CubeListBuilder.create(),
				PartPose.offset(0.0F, 19.0F, 13.0F)), 19, 68, 6.0F);
		buildGlowWheel(root.addOrReplaceChild("back_wheel_glow", CubeListBuilder.create(),
				PartPose.offset(0.0F, 19.0F, 13.0F)), 96, 78, 5.0F);

		// =========================================================================
		// 4. Twin Floating "Key of Truth" Flank Wing-Pods
		// =========================================================================
		root.addOrReplaceChild("left_wing_pod", buildWingPod(false),
				PartPose.offsetAndRotation(9.5F, 12.5F, 5.0F, 0.08F, -0.18F, -0.12F));
		root.addOrReplaceChild("left_wing_pod_glow", buildWingPodGlow(false),
				PartPose.offsetAndRotation(9.5F, 12.5F, 5.0F, 0.08F, -0.18F, -0.12F));

		root.addOrReplaceChild("right_wing_pod", buildWingPod(true),
				PartPose.offsetAndRotation(-9.5F, 12.5F, 5.0F, 0.08F, 0.18F, 0.12F));
		root.addOrReplaceChild("right_wing_pod_glow", buildWingPodGlow(true),
				PartPose.offsetAndRotation(-9.5F, 12.5F, 5.0F, 0.08F, 0.18F, 0.12F));

		return LayerDefinition.create(mesh, 128, 128);
	}

	private static void buildTireWheel(PartDefinition wheelRoot, int u, int v, float width) {
		float halfW = width * 0.5F;
		for (int i = 0; i < WHEEL_SEGMENTS; i++) {
			float angle = i * SEGMENT_STEP;
			wheelRoot.addOrReplaceChild("seg_" + i, CubeListBuilder.create()
					.texOffs(u, v).addBox(-halfW, -4.8F, -2.0F, width, 2.0F, 4.0F, CubeDeformation.NONE),
					PartPose.rotation(angle, 0.0F, 0.0F));
		}
	}

	private static void buildGlowWheel(PartDefinition wheelRoot, int u, int v, float width) {
		float halfW = width * 0.5F;
		for (int i = 0; i < WHEEL_SEGMENTS; i++) {
			float angle = i * SEGMENT_STEP;
			wheelRoot.addOrReplaceChild("rim_" + i, CubeListBuilder.create()
					.texOffs(u, v).addBox(-halfW, -3.1F, -1.5F, width, 1.0F, 3.0F, CubeDeformation.NONE),
					PartPose.rotation(angle, 0.0F, 0.0F));
		}
		// Twin spinning cyan energy cross-spokes inside hollow hub
		wheelRoot.addOrReplaceChild("spoke_a", CubeListBuilder.create()
				.texOffs(76, 78).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 6.0F, 1.0F, CubeDeformation.NONE),
				PartPose.ZERO);
		wheelRoot.addOrReplaceChild("spoke_b", CubeListBuilder.create()
				.texOffs(76, 78).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 6.0F, 1.0F, CubeDeformation.NONE),
				PartPose.rotation(Mth.HALF_PI, 0.0F, 0.0F));
	}

	private static CubeListBuilder buildWingPod(boolean mirror) {
		CubeListBuilder builder = CubeListBuilder.create();
		builder.texOffs(89, 25).mirror(mirror).addBox(-1.5F, -2.5F, -6.0F, 3.0F, 4.0F, 12.0F, CubeDeformation.NONE);
		builder.texOffs(81, 44).mirror(mirror).addBox(-1.0F, -4.0F, -4.0F, 2.0F, 2.0F, 10.0F, CubeDeformation.NONE);
		builder.texOffs(106, 44).mirror(mirror).addBox(-0.5F, -5.5F, 2.0F, 1.0F, 3.0F, 6.0F, CubeDeformation.NONE);
		builder.texOffs(120, 15).mirror(mirror).addBox(-1.0F, -1.5F, -8.0F, 2.0F, 2.0F, 2.0F, CubeDeformation.NONE);
		return builder;
	}

	private static CubeListBuilder buildWingPodGlow(boolean mirror) {
		float railX = mirror ? -1.65F : 0.65F;
		return CubeListBuilder.create()
				.texOffs(21, 78).mirror(mirror).addBox(railX, -1.0F, -5.5F, 1.0F, 1.0F, 10.0F, CubeDeformation.NONE)
				.texOffs(55, 87).addBox(-0.5F, -1.0F, -8.3F, 1.0F, 1.0F, 1.0F, CubeDeformation.NONE)
				.texOffs(55, 87).addBox(-0.5F, -1.0F, 5.5F, 1.0F, 1.0F, 1.0F, CubeDeformation.NONE);
	}

	@Override
	public void setupAnim(MotorEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		setupAnim(entity, limbSwing, ageInTicks);
	}

	public void setupAnim(MotorEntity entity, float partialTick, float ageInTicks) {
		float wheelSpin = entity.getWheelSpin(partialTick);
		this.frontWheel.xRot = wheelSpin;
		this.frontWheelGlow.xRot = wheelSpin;
		this.backWheel.xRot = wheelSpin;
		this.backWheelGlow.xRot = wheelSpin;

		boolean charged = entity.isCharged();
		boolean boosting = entity.isBoosting();
		boolean cyclone = entity.getCycloneTicks() > 0;

		float flare = cyclone ? 1.35F : (boosting ? 1.0F : (charged ? 0.65F : 0.0F));
		float leftHover = Mth.sin(ageInTicks * 0.14F) * 0.85F;
		float rightHover = Mth.sin(ageInTicks * 0.14F + 0.55F) * 0.85F;

		applyWingPose(this.leftWingPod, this.leftWingPodGlow,
				9.5F + flare * 2.2F,
				12.5F + leftHover - flare * 0.9F,
				5.0F,
				0.08F - flare * 0.06F,
				-0.18F - flare * 0.22F,
				-0.12F - flare * 0.18F);

		applyWingPose(this.rightWingPod, this.rightWingPodGlow,
				-9.5F - flare * 2.2F,
				12.5F + rightHover - flare * 0.9F,
				5.0F,
				0.08F - flare * 0.06F,
				0.18F + flare * 0.22F,
				0.12F + flare * 0.18F);

		this.boostFlames.visible = boosting || charged || cyclone;
		if (this.boostFlames.visible) {
			this.boostFlames.zScale = boosting
					? (1.15F + 0.35F * Mth.sin(ageInTicks * 1.4F))
					: (cyclone ? 1.35F : (0.55F + 0.18F * Mth.sin(ageInTicks * 0.9F)));
		} else {
			this.boostFlames.zScale = 1.0F;
		}

		float pulse = 0.5F + 0.5F * Mth.sin(ageInTicks * (boosting ? 0.45F : 0.18F));
		int brightness = (boosting || charged || cyclone)
				? (230 + Math.round(25.0F * pulse))
				: (205 + Math.round(25.0F * pulse));
		this.glowRed = brightness / 255.0F;
		this.glowGreen = Math.min(255, brightness + 15) / 255.0F;
		this.glowBlue = 1.0F;
	}

	private static void applyWingPose(ModelPart solid, ModelPart glow, float x, float y, float z, float xRot, float yRot, float zRot) {
		solid.x = x;
		solid.y = y;
		solid.z = z;
		solid.xRot = xRot;
		solid.yRot = yRot;
		solid.zRot = zRot;

		glow.x = x;
		glow.y = y;
		glow.z = z;
		glow.xRot = xRot;
		glow.yRot = yRot;
		glow.zRot = zRot;
	}

	public void renderBase(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		this.body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		this.frontWheel.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		this.backWheel.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		this.leftWingPod.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		this.rightWingPod.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void renderGlow(PoseStack poseStack, VertexConsumer buffer, int packedOverlay,
			float red, float green, float blue, float alpha) {
		int fullBright = 0x00F000F0;
		this.bodyGlow.render(poseStack, buffer, fullBright, packedOverlay, red, green, blue, alpha);
		this.frontWheelGlow.render(poseStack, buffer, fullBright, packedOverlay, red, green, blue, alpha);
		this.backWheelGlow.render(poseStack, buffer, fullBright, packedOverlay, red, green, blue, alpha);
		this.leftWingPodGlow.render(poseStack, buffer, fullBright, packedOverlay, red, green, blue, alpha);
		this.rightWingPodGlow.render(poseStack, buffer, fullBright, packedOverlay, red, green, blue, alpha);
	}

	public void renderGlow(PoseStack poseStack, VertexConsumer buffer, int packedOverlay) {
		renderGlow(poseStack, buffer, packedOverlay, this.glowRed, this.glowGreen, this.glowBlue, 1.0F);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		renderBase(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		renderGlow(poseStack, buffer, packedOverlay, this.glowRed * red, this.glowGreen * green, this.glowBlue * blue, alpha);
	}
}
