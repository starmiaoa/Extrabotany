package io.github.lounode.extrabotany.common.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import io.github.lounode.extrabotany.common.entity.gaia.Gaia;
import io.github.lounode.extrabotany.common.entity.gaia.GaiaIII;
import io.github.lounode.extrabotany.common.lib.LibEntityNames;

import java.util.function.BiConsumer;

public class ExtraBotanyEntityType {
	public static final EntityType<AuraFireEntity> AURA_FIRE = EntityType.Builder.<AuraFireEntity>of(
			AuraFireEntity::new, MobCategory.MISC)
			.sized(0, 0)
			.noSummon()
			.updateInterval(10)
			.clientTrackingRange(10)
			.build(LibEntityNames.AURA_FIRE.toString());

	public static final EntityType<Gaia> GAIA_LEGACY = EntityType.Builder.<Gaia>of(
			Gaia::new, MobCategory.MONSTER)
			.sized(0.6F, 1.8F)
			.fireImmune()
			.clientTrackingRange(10)
			.updateInterval(10)
			.build(LibEntityNames.GAIA_LEGACY.toString());

	public static final EntityType<MagicLandMineEntity> MAGIC_LANDMINE = EntityType.Builder.<MagicLandMineEntity>of(
			MagicLandMineEntity::new, MobCategory.MISC)
			.sized(5F, 0.1F)
			.clientTrackingRange(8)
			.updateInterval(40)
			.build(LibEntityNames.MAGIC_LANDMINE.toString());

	public static final EntityType<GaiaIII> GAIA_III = EntityType.Builder.<GaiaIII>of(
			GaiaIII::new, MobCategory.MONSTER)
			.sized(0.6F, 1.8F)
			.fireImmune()
			.clientTrackingRange(10)
			.updateInterval(10)
			.build(LibEntityNames.GAIA_III.toString());

	public static final EntityType<SkullMissileEntity> SKULL_MISSILE = EntityType.Builder.<SkullMissileEntity>of(
			SkullMissileEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(2)
			.build(LibEntityNames.SKULL_MISSILE.toString());

	public static final EntityType<SkullLandMineEntity.Default> SKULL_LANDMINE_BLUE = EntityType.Builder.<SkullLandMineEntity.Default>of(
			SkullLandMineEntity.Default::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(8)
			.updateInterval(40)
			.build(LibEntityNames.SKULL_LANDMINE_BLUE.toString());

	public static final EntityType<SkullLandMineEntity.Danger> SKULL_LANDMINE_RED = EntityType.Builder.<SkullLandMineEntity.Danger>of(
			SkullLandMineEntity.Danger::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(8)
			.updateInterval(40)
			.build(LibEntityNames.SKULL_LANDMINE_RED.toString());

	public static final EntityType<SkullLandMineEntity.Disarm> SKULL_LANDMINE_GREEN = EntityType.Builder.<SkullLandMineEntity.Disarm>of(
			SkullLandMineEntity.Disarm::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(8)
			.updateInterval(40)
			.build(LibEntityNames.SKULL_LANDMINE_GREEN.toString());

	public static final EntityType<HolyWaterGrenadeEntity> HOLY_WATER_GRENADE = EntityType.Builder.<HolyWaterGrenadeEntity>of(
			HolyWaterGrenadeEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F)
			.clientTrackingRange(4)
			.updateInterval(10)
			.build(LibEntityNames.HOLY_WATER_GRENADE.toString());

	public static final EntityType<ButterflyProjectileEntity> BUTTERFLY_PROJECTILE = EntityType.Builder.<ButterflyProjectileEntity>of(
			ButterflyProjectileEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F)
			.clientTrackingRange(4)
			.updateInterval(2)
			.build(LibEntityNames.BUTTERFLY_PROJECTILE.toString());

	public static final EntityType<BottledStarEntity> BOTTLED_STAR = EntityType.Builder.<BottledStarEntity>of(
			BottledStarEntity::new, MobCategory.MISC)
			.sized(0.35F, 0.35F)
			.clientTrackingRange(4)
			.updateInterval(2)
			.build(LibEntityNames.BOTTLED_STAR.toString());

	public static final EntityType<PhotonShotgunProjectileEntity> PHOTON_SHOTGUN_PROJECTILE = EntityType.Builder.<PhotonShotgunProjectileEntity>of(
			PhotonShotgunProjectileEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F)
			.clientTrackingRange(4)
			.updateInterval(2)
			.build(LibEntityNames.PHOTON_SHOTGUN_PROJECTILE.toString());

	public static final EntityType<FlowerWeaponEntity> FLOWER_WEAPON = EntityType.Builder.<FlowerWeaponEntity>of(
			FlowerWeaponEntity::new, MobCategory.MISC)
			.sized(0.35F, 0.35F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibEntityNames.FLOWER_WEAPON.toString());

	public static final EntityType<FlamescionSlashEntity> FLAMESCION_SLASH = EntityType.Builder.<FlamescionSlashEntity>of(
			FlamescionSlashEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibEntityNames.FLAMESCION_SLASH.toString());

	public static final EntityType<StrengthenSlashEntity> STRENGTHEN_SLASH = EntityType.Builder.<StrengthenSlashEntity>of(
			StrengthenSlashEntity::new, MobCategory.MISC)
			.sized(0.35F, 0.35F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibEntityNames.STRENGTHEN_SLASH.toString());

	public static final EntityType<FlamescionSwordEntity> FLAMESCION_SWORD = EntityType.Builder.<FlamescionSwordEntity>of(
			FlamescionSwordEntity::new, MobCategory.MISC)
			.sized(0.35F, 0.35F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibEntityNames.FLAMESCION_SWORD.toString());

	public static final EntityType<FlamescionVoidEntity> FLAMESCION_VOID = EntityType.Builder.<FlamescionVoidEntity>of(
			FlamescionVoidEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibEntityNames.FLAMESCION_VOID.toString());

	public static final EntityType<FlamescionUltEntity> FLAMESCION_ULT = EntityType.Builder.<FlamescionUltEntity>of(
			FlamescionUltEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibEntityNames.FLAMESCION_ULT.toString());

	public static final EntityType<TrueTerrabladeProjectileEntity> TRUE_TERRABLADE_PROJECTILE = EntityType.Builder.<TrueTerrabladeProjectileEntity>of(
			TrueTerrabladeProjectileEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(2)
			.build(LibEntityNames.TRUE_TERRABLADE_PROJECTILE.toString());

	public static final EntityType<TrueShadowKatanaProjectileEntity> TRUE_SHADOW_KATANA_PROJECTILE = EntityType.Builder.<TrueShadowKatanaProjectileEntity>of(
			TrueShadowKatanaProjectileEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(2)
			.build(LibEntityNames.TRUE_SHADOW_KATANA_PROJECTILE.toString());

	public static final EntityType<InfluxWaverProjectileEntity> INFLUX_WAVER_PROJECTILE = EntityType.Builder.<InfluxWaverProjectileEntity>of(
			InfluxWaverProjectileEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(2)
			.build(LibEntityNames.INFLUX_WAVER_PROJECTILE.toString());

	public static final EntityType<PhantomSwordEntity> PHANTOM_SWORD = EntityType.Builder.<PhantomSwordEntity>of(
			PhantomSwordEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.clientTrackingRange(4)
			.updateInterval(2)
			.build(LibEntityNames.PHANTOM_SWORD.toString());

	public static final EntityType<StarWrathFallingStarEntity> STAR_WRATH_FALLING_STAR = EntityType.Builder.<StarWrathFallingStarEntity>of(
			StarWrathFallingStarEntity::new, MobCategory.MISC)
			.sized(0.35F, 0.35F)
			.clientTrackingRange(4)
			.updateInterval(2)
			.build(LibEntityNames.STAR_WRATH_FALLING_STAR.toString());

	public static final EntityType<SubspaceEntity> SUBSPACE = EntityType.Builder.<SubspaceEntity>of(
			SubspaceEntity::new, MobCategory.MISC)
			.sized(0.1F, 0.1F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibEntityNames.SUBSPACE.toString());

	public static final EntityType<SubspaceSpearEntity> SUBSPACE_SPEAR = EntityType.Builder.<SubspaceSpearEntity>of(
			SubspaceSpearEntity::new, MobCategory.MISC)
			.sized(0.35F, 0.35F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibEntityNames.SUBSPACE_SPEAR.toString());

	public static final EntityType<JudahOathEntity> JUDAH_OATH = EntityType.Builder.<JudahOathEntity>of(
			JudahOathEntity::new, MobCategory.MISC)
			.sized(0.35F, 0.35F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibEntityNames.JUDAH_OATH.toString());

	public static final EntityType<JudahSpearEntity> JUDAH_SPEAR = EntityType.Builder.<JudahSpearEntity>of(
			JudahSpearEntity::new, MobCategory.MISC)
			.sized(0.35F, 0.35F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibEntityNames.JUDAH_SPEAR.toString());

	public static final EntityType<JudahSwordEntity> JUDAH_SWORD = EntityType.Builder.<JudahSwordEntity>of(
			JudahSwordEntity::new, MobCategory.MISC)
			.sized(0.35F, 0.35F)
			.clientTrackingRange(8)
			.updateInterval(2)
			.build(LibEntityNames.JUDAH_SWORD.toString());

	public static final EntityType<UfoEntity> UFO = EntityType.Builder.<UfoEntity>of(
			UfoEntity::new, MobCategory.MISC)
			.sized(2.5F, 1.2F)
			.clientTrackingRange(10)
			.updateInterval(2)
			.build(LibEntityNames.UFO.toString());

	public static final EntityType<MotorEntity> MOTOR = EntityType.Builder.<MotorEntity>of(
			MotorEntity::new, MobCategory.MISC)
			.sized(1.4F, 1.0F)
			.clientTrackingRange(10)
			.updateInterval(2)
			.build(LibEntityNames.MOTOR.toString());

	public static final EntityType<FlyingBoatEntity> FLYING_BOAT = EntityType.Builder.<FlyingBoatEntity>of(
			FlyingBoatEntity::new, MobCategory.MISC)
			.sized(1.375F, 0.5625F)
			.clientTrackingRange(10)
			.updateInterval(2)
			.build(LibEntityNames.FLYING_BOAT.toString());

	public static void registerEntities(BiConsumer<EntityType<?>, ResourceLocation> r) {
		r.accept(AURA_FIRE, LibEntityNames.AURA_FIRE);
		r.accept(MAGIC_LANDMINE, LibEntityNames.MAGIC_LANDMINE);
		r.accept(GAIA_LEGACY, LibEntityNames.GAIA_LEGACY);
		r.accept(GAIA_III, LibEntityNames.GAIA_III);
		r.accept(SKULL_MISSILE, LibEntityNames.SKULL_MISSILE);
		r.accept(SKULL_LANDMINE_BLUE, LibEntityNames.SKULL_LANDMINE_BLUE);
		r.accept(SKULL_LANDMINE_RED, LibEntityNames.SKULL_LANDMINE_RED);
		r.accept(SKULL_LANDMINE_GREEN, LibEntityNames.SKULL_LANDMINE_GREEN);
		r.accept(HOLY_WATER_GRENADE, LibEntityNames.HOLY_WATER_GRENADE);
		r.accept(BUTTERFLY_PROJECTILE, LibEntityNames.BUTTERFLY_PROJECTILE);
		r.accept(BOTTLED_STAR, LibEntityNames.BOTTLED_STAR);
		r.accept(PHOTON_SHOTGUN_PROJECTILE, LibEntityNames.PHOTON_SHOTGUN_PROJECTILE);
		r.accept(FLOWER_WEAPON, LibEntityNames.FLOWER_WEAPON);
		r.accept(FLAMESCION_SLASH, LibEntityNames.FLAMESCION_SLASH);
		r.accept(STRENGTHEN_SLASH, LibEntityNames.STRENGTHEN_SLASH);
		r.accept(FLAMESCION_SWORD, LibEntityNames.FLAMESCION_SWORD);
		r.accept(FLAMESCION_VOID, LibEntityNames.FLAMESCION_VOID);
		r.accept(FLAMESCION_ULT, LibEntityNames.FLAMESCION_ULT);
		r.accept(TRUE_TERRABLADE_PROJECTILE, LibEntityNames.TRUE_TERRABLADE_PROJECTILE);
		r.accept(TRUE_SHADOW_KATANA_PROJECTILE, LibEntityNames.TRUE_SHADOW_KATANA_PROJECTILE);
		r.accept(INFLUX_WAVER_PROJECTILE, LibEntityNames.INFLUX_WAVER_PROJECTILE);
		r.accept(PHANTOM_SWORD, LibEntityNames.PHANTOM_SWORD);
		r.accept(STAR_WRATH_FALLING_STAR, LibEntityNames.STAR_WRATH_FALLING_STAR);
		r.accept(SUBSPACE, LibEntityNames.SUBSPACE);
		r.accept(SUBSPACE_SPEAR, LibEntityNames.SUBSPACE_SPEAR);
		r.accept(JUDAH_OATH, LibEntityNames.JUDAH_OATH);
		r.accept(JUDAH_SPEAR, LibEntityNames.JUDAH_SPEAR);
		r.accept(JUDAH_SWORD, LibEntityNames.JUDAH_SWORD);
		r.accept(UFO, LibEntityNames.UFO);
		r.accept(MOTOR, LibEntityNames.MOTOR);
		r.accept(FLYING_BOAT, LibEntityNames.FLYING_BOAT);
	}

	public static void registerAttributes(BiConsumer<EntityType<? extends LivingEntity>, AttributeSupplier.Builder> consumer) {
		consumer.accept(GAIA_LEGACY, Gaia.createGaiaAttributes());
		consumer.accept(GAIA_III, GaiaIII.createGaiaAttributes());

	}
}
