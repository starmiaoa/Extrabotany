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

import io.github.lounode.extrabotany.common.entity.UfoEntity;

public class UfoModel extends EntityModel<UfoEntity> {
	private static final float QUARTER_PI = Mth.PI / 4.0F;
	private static final float EIGHTH_PI = Mth.PI / 8.0F;

	private final ModelPart hull;
	private final ModelPart outerRing;
	private final ModelPart glow;
	private final ModelPart tractorBeam;

	private int glowLight = 0x00F000F0;
	private float glowTint = 1.0F;
	private float beamAlpha = 187.0F / 255.0F;

	public UfoModel(ModelPart root) {
		this.hull = root.getChild("hull");
		this.outerRing = root.getChild("outer_ring");
		this.glow = root.getChild("glow");
		this.tractorBeam = root.getChild("tractor_beam");
	}

	public static LayerDefinition createLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		// =========================================================================
		// 1. Symmetrically Centered Martian Saucer Hull (100% centered at X=0, Z=0)
		// =========================================================================
		PartDefinition hull = root.addOrReplaceChild("hull", CubeListBuilder.create()
				// Recessed dark cockpit floor pad inside the open cockpit collar
				.texOffs(35, 15).addBox(-5.0F, -11.2F, -5.0F, 10.0F, 1.0F, 10.0F, CubeDeformation.NONE)
				// Open-top armored cockpit collar walls around seated rider
				.texOffs(0, 15).addBox(5.0F, -13.0F, -7.0F, 3.0F, 2.0F, 14.0F, CubeDeformation.NONE)
				.texOffs(0, 15).mirror().addBox(-8.0F, -13.0F, -7.0F, 3.0F, 2.0F, 14.0F, CubeDeformation.NONE)
				.texOffs(76, 23).addBox(-5.0F, -13.0F, -8.0F, 10.0F, 2.0F, 3.0F, CubeDeformation.NONE)
				// Rear backrest console behind player (+Z, zero first-person obstruction)
				.texOffs(76, 15).addBox(-6.0F, -15.0F, 5.0F, 12.0F, 4.0F, 3.0F, CubeDeformation.NONE),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		// True 5-12-13 Pythagorean Octagonal Main Saucer Deck (4x 24x3x10 slices)
		addOctagonSlices(hull, "deck", 0, 0, -12.0F, -11.0F, -5.0F, 24.0F, 3.0F, 10.0F);
		// Octagonal Lower Armored Belly (4x 18x3x8 slices)
		addOctagonSlices(hull, "belly", 69, 0, -9.0F, -8.0F, -4.0F, 18.0F, 3.0F, 8.0F);
		// Octagonal Central Tractor-Beam Emitter Collar (lowest point Y=20 -> world Y=0.0)
		addOctagonSlices(hull, "emitter", 35, 27, -5.0F, -5.0F, -2.0F, 10.0F, 1.0F, 4.0F);

		// 4 Diagonal Cockpit Corner Armor Bolsters
		float[][] corners = { { 6.2F, -6.2F }, { -6.2F, -6.2F }, { 6.2F, 6.2F }, { -6.2F, 6.2F } };
		for (int i = 0; i < corners.length; i++) {
			hull.addOrReplaceChild("rim_corner_" + i, CubeListBuilder.create()
					.texOffs(107, 15).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F, CubeDeformation.NONE),
					PartPose.offsetAndRotation(corners[i][0], -12.0F, corners[i][1], 0.0F, QUARTER_PI, 0.0F));
		}

		// Twin Rear Angled Martian Telemetry Antennae (+Z behind rider)
		hull.addOrReplaceChild("left_antenna", CubeListBuilder.create()
				.texOffs(120, 15).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 5.0F, 1.0F, CubeDeformation.NONE),
				PartPose.offsetAndRotation(4.5F, -15.0F, 6.5F, -0.25F, 0.0F, 0.22F));
		hull.addOrReplaceChild("right_antenna", CubeListBuilder.create()
				.texOffs(120, 15).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 5.0F, 1.0F, CubeDeformation.NONE),
				PartPose.offsetAndRotation(-4.5F, -15.0F, 6.5F, -0.25F, 0.0F, -0.22F));

		// =========================================================================
		// 2. Spinning Outer Martian Energy Ring + 8 Alternating Cyan/Crimson Beacons
		// =========================================================================
		PartDefinition outerRing = root.addOrReplaceChild("outer_ring", CubeListBuilder.create(),
				PartPose.offset(0.0F, 14.5F, 0.0F));
		for (int i = 0; i < 8; i++) {
			float angle = i * QUARTER_PI;
			int beaconU = (i % 2 == 0) ? 76 : 85;
			outerRing.addOrReplaceChild("beacon_" + i, CubeListBuilder.create()
					.texOffs(beaconU, 39).addBox(12.2F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, CubeDeformation.NONE),
					PartPose.rotation(0.0F, angle, 0.0F));
			outerRing.addOrReplaceChild("ring_bar_" + i, CubeListBuilder.create()
					.texOffs(42, 35).addBox(12.1F, -0.5F, -4.0F, 1.0F, 1.0F, 8.0F, CubeDeformation.NONE),
					PartPose.rotation(0.0F, angle + EIGHTH_PI, 0.0F));
		}

		// =========================================================================
		// 3. Pulsing Fullbright Martian Tech Glow (Visor, HUD, Core, 3 Thrusters)
		// =========================================================================
		PartDefinition glow = root.addOrReplaceChild("glow", CubeListBuilder.create()
				// Cockpit HUD control strip
				.texOffs(97, 35).addBox(-3.5F, -13.15F, -6.2F, 7.0F, 1.0F, 1.0F, CubeDeformation.NONE)
				// Central glowing cyan anti-gravity core beneath emitter
				.texOffs(0, 35).addBox(-3.0F, -4.9F, -3.0F, 6.0F, 1.0F, 6.0F, CubeDeformation.NONE),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		// Sleek angled low front cyan energy windshield visor (-Z)
		glow.addOrReplaceChild("visor", CubeListBuilder.create()
				.texOffs(76, 35).addBox(-4.5F, -2.0F, -0.5F, 9.0F, 2.0F, 1.0F, CubeDeformation.NONE),
				PartPose.offsetAndRotation(0.0F, -13.0F, -7.8F, -0.28F, 0.0F, 0.0F));

		// Glowing crimson antenna tips aligned with rear antennae
		glow.addOrReplaceChild("left_antenna_tip", CubeListBuilder.create()
				.texOffs(94, 39).addBox(-1.0F, -6.5F, -1.0F, 2.0F, 2.0F, 2.0F, CubeDeformation.NONE),
				PartPose.offsetAndRotation(4.5F, -15.0F, 6.5F, -0.25F, 0.0F, 0.22F));
		glow.addOrReplaceChild("right_antenna_tip", CubeListBuilder.create()
				.texOffs(94, 39).addBox(-1.0F, -6.5F, -1.0F, 2.0F, 2.0F, 2.0F, CubeDeformation.NONE),
				PartPose.offsetAndRotation(-4.5F, -15.0F, 6.5F, -0.25F, 0.0F, -0.22F));

		// 3 Radial Underside Anti-Gravity Thruster Rings (120 degrees apart)
		for (int i = 0; i < 3; i++) {
			float angle = i * (Mth.TWO_PI / 3.0F);
			glow.addOrReplaceChild("thruster_" + i, CubeListBuilder.create()
					.texOffs(25, 35).addBox(-2.0F, -8.3F, -9.8F, 4.0F, 1.0F, 4.0F, CubeDeformation.NONE),
					PartPose.rotation(0.0F, angle, 0.0F));
		}

		// 4 Cardinal Glowing Cyan Saucer Deck Intake Strips
		for (int i = 0; i < 4; i++) {
			float angle = i * Mth.HALF_PI;
			glow.addOrReplaceChild("deck_strip_" + i, CubeListBuilder.create()
					.texOffs(61, 35).addBox(9.4F, -11.2F, -2.5F, 2.0F, 1.0F, 5.0F, CubeDeformation.NONE),
					PartPose.rotation(0.0F, angle, 0.0F));
		}

		// =========================================================================
		// 4. Dynamic Translucent Tractor Beam Cone (Visible only while abducting!)
		// =========================================================================
		PartDefinition beam = root.addOrReplaceChild("tractor_beam", CubeListBuilder.create()
				.texOffs(0, 50).addBox(-3.5F, -4.0F, -3.5F, 7.0F, 6.0F, 7.0F, CubeDeformation.NONE)
				.texOffs(70, 50).addBox(-6.5F, 9.0F, -6.5F, 13.0F, 7.0F, 13.0F, CubeDeformation.NONE)
				.texOffs(0, 65).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 20.0F, 4.0F, CubeDeformation.NONE),
				PartPose.offset(0.0F, 24.0F, 0.0F));
		beam.addOrReplaceChild("beam_mid", CubeListBuilder.create()
				.texOffs(29, 50).addBox(-5.0F, 2.0F, -5.0F, 10.0F, 7.0F, 10.0F, CubeDeformation.NONE),
				PartPose.rotation(0.0F, QUARTER_PI, 0.0F));

		return LayerDefinition.create(mesh, 128, 128);
	}

	private static void addOctagonSlices(PartDefinition parent, String prefix, int u, int v,
			float x, float y, float z, float w, float h, float d) {
		for (int i = 0; i < 4; i++) {
			float angle = i * QUARTER_PI;
			// Slight negative deformation per rotated slice prevents coplanar Z-fighting
			CubeDeformation deform = i == 0 ? CubeDeformation.NONE : new CubeDeformation(-0.015F * i);
			parent.addOrReplaceChild(prefix + "_" + i, CubeListBuilder.create()
					.texOffs(u, v).addBox(x, y, z, w, h, d, deform),
					PartPose.rotation(0.0F, angle, 0.0F));
		}
	}

	@Override
	public void setupAnim(UfoEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		boolean abducting = entity.hasCaughtEntity();

		// Continuous stateless rotation so multiple UFOs never snap or leak animation state
		this.outerRing.yRot = ageInTicks * 0.045F;

		// Pulsing Martian energy lightmap (0xF000A0 .. 0xF000F0) + subtle tint pulse
		float pulse = 0.5F + 0.5F * Mth.sin(ageInTicks * (abducting ? 0.35F : 0.16F));
		int blockLight = 10 + Math.round(5.0F * pulse);
		this.glowLight = 0x00F00000 | (blockLight << 4);
		int c = 215 + Math.round(40.0F * pulse);
		this.glowTint = c / 255.0F;

		// Dynamic tractor beam rotation & pulse when capturing an entity
		this.tractorBeam.visible = abducting;
		if (abducting) {
			this.tractorBeam.yRot = ageInTicks * 0.14F;
			float beamPulse = 0.5F + 0.5F * Mth.sin(ageInTicks * 0.4F);
			this.tractorBeam.xScale = 0.96F + 0.08F * beamPulse;
			this.tractorBeam.zScale = 0.96F + 0.08F * beamPulse;
			this.beamAlpha = (165.0F + 55.0F * beamPulse) / 255.0F;
		} else {
			this.tractorBeam.xScale = 1.0F;
			this.tractorBeam.zScale = 1.0F;
		}
	}

	public float getBeamAlpha() {
		return this.beamAlpha;
	}

	public void renderHull(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		this.hull.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void renderGlow(PoseStack poseStack, VertexConsumer buffer, int packedOverlay) {
		this.outerRing.render(poseStack, buffer, this.glowLight, packedOverlay, this.glowTint, this.glowTint, 1.0F, 1.0F);
		this.glow.render(poseStack, buffer, this.glowLight, packedOverlay, this.glowTint, this.glowTint, 1.0F, 1.0F);
	}

	public void renderTractorBeam(PoseStack poseStack, VertexConsumer buffer, int packedOverlay,
			float red, float green, float blue, float alpha) {
		if (this.tractorBeam.visible) {
			this.tractorBeam.render(poseStack, buffer, 0x00F000F0, packedOverlay, red, green, blue, alpha);
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		renderHull(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
		renderGlow(poseStack, buffer, packedOverlay);
	}
}
