package io.github.lounode.extrabotany.common.entity.gaia.behavior;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.common.entity.FallingStarEntity;
import vazkii.botania.common.handler.BotaniaSounds;

import io.github.lounode.extrabotany.common.entity.InfluxWaverProjectileEntity;
import io.github.lounode.extrabotany.common.entity.PhantomSwordEntity;
import io.github.lounode.extrabotany.common.entity.TrueShadowKatanaProjectileEntity;
import io.github.lounode.extrabotany.common.entity.TrueTerrabladeProjectileEntity;

public final class EgoWeaponFire {
	private EgoWeaponFire() {}

	public static void fire(LivingEntity shooter, LivingEntity target, int weapon) {
		switch (weapon) {
			case 0 -> fireShadowKatana(shooter, target);
			case 1 -> fireTerrablade(shooter, target);
			case 2 -> fireInfluxWaver(shooter, target);
			case 3 -> fireStarWrath(shooter, target);
			default -> fireFirstFractal(shooter, target);
		}
	}

	private static void fireTerrablade(LivingEntity shooter, LivingEntity target) {
		Vec3 targetPos = target.position().add(0, 1, 0);
		shooter.level().addFreshEntity(new TrueTerrabladeProjectileEntity(shooter.level(), shooter, targetPos));
	}

	private static void fireShadowKatana(LivingEntity shooter, LivingEntity target) {
		Vec3 targetPos = target.position().add(0, 1, 0);
		Vec3 flat = targetPos.subtract(shooter.position()).multiply(1, 0, 1);
		Vec3 forward = (flat.lengthSqr() < 1.0E-4D ? Vec3.directionFromRotation(0, shooter.getYRot()) : flat.normalize()).scale(1.75D);
		Vec3 side = new Vec3(-forward.z, 0, forward.x).normalize();
		Vec3 base = shooter.position().add(0, shooter.getBbHeight() * 0.55D, 0).add(forward);
		for (int i = -1; i <= 1; i++) {
			Vec3 start = base.add(side.scale(i * 1.4D)).add(0, Math.abs(i) * 0.2D, 0);
			shooter.level().addFreshEntity(new TrueShadowKatanaProjectileEntity(shooter.level(), shooter, start, targetPos));
		}
	}

	private static void fireInfluxWaver(LivingEntity shooter, LivingEntity target) {
		Vec3 targetPos = target.position().add(0, 1, 0);
		shooter.level().addFreshEntity(new InfluxWaverProjectileEntity(shooter.level(), shooter, targetPos, 3));
	}

	private static void fireStarWrath(LivingEntity shooter, LivingEntity target) {
		Vec3 targetPos = target.position().add(0, 1, 0);
		RandomSource rand = shooter.getRandom();
		for (int i = 0; i < 5; i++) {
			Vec3 impact = targetPos.add((0.5D - rand.nextDouble()) * 6D, 0, (0.5D - rand.nextDouble()) * 6D);
			Vec3 motionSource = new Vec3(
					(0.5D * rand.nextDouble() - 0.25D) * 18D,
					24D,
					(0.5D * rand.nextDouble() - 0.25D) * 18D
			);
			Vec3 start = impact.add(motionSource);
			Vec3 velocity = motionSource.normalize().reverse().scale(1.5D);

			FallingStarEntity star = new FallingStarEntity(shooter, shooter.level());
			star.setPos(start.x, start.y, start.z);
			star.setDeltaMovement(velocity);
			shooter.level().addFreshEntity(star);
		}
	}

	private static void fireFirstFractal(LivingEntity shooter, LivingEntity target) {
		Vec3 targetPos = target.position().add(0, 1, 0);
		RandomSource rand = shooter.getRandom();
		double angle = -Math.PI + 2 * Math.PI * rand.nextDouble();
		for (int i = 0; i < 3; i++) {
			Vec3 start = phantomStart(targetPos, angle, rand);
			shooter.level().addFreshEntity(new PhantomSwordEntity(shooter.level(), shooter, start, targetPos, 5 + 5 * i));
			angle += 2 * Math.PI * rand.nextDouble() * 0.08D + 2 * Math.PI * 0.17D;
		}
		Vec3 start = phantomStart(targetPos, angle, rand);
		shooter.level().addFreshEntity(new PhantomSwordEntity(shooter.level(), shooter, start, targetPos, 0, 9));
		shooter.playSound(BotaniaSounds.terraBlade, 0.4F, 1.4F);
	}

	private static Vec3 phantomStart(Vec3 targetPos, double angle, RandomSource rand) {
		double range = 13D;
		double pitch = 0.12D * Math.PI * rand.nextDouble() + 0.28D * Math.PI;
		return new Vec3(
				targetPos.x + range * Math.sin(pitch) * Math.cos(angle),
				targetPos.y + range * Math.cos(pitch),
				targetPos.z + range * Math.sin(pitch) * Math.sin(angle)
		);
	}
}
