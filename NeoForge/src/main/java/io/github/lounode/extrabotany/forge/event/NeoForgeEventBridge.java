package io.github.lounode.extrabotany.forge.event;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import vazkii.botania.api.mana.ManaDiscountEvent;

import io.github.lounode.extrabotany.common.block.flower.generating.ResoncundBlockEntity;
import io.github.lounode.extrabotany.common.brew.effect.EternityMobEffect;
import io.github.lounode.extrabotany.common.util.HerrscherCombatHelper;
import io.github.lounode.extrabotany.common.brew.effect.HealReverseMobEffect;
import io.github.lounode.extrabotany.common.brew.effect.LinkMobEffect;
import io.github.lounode.extrabotany.common.brew.effect.ThirrorMobEffect;
import io.github.lounode.extrabotany.common.brew.effect.WarmMobEffect;
import io.github.lounode.extrabotany.common.brew.effect.WitchCurseMobEffect;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import io.github.lounode.extrabotany.common.impl.WindImpl;
import io.github.lounode.extrabotany.common.handler.OldExbotanyStatRewardHandler;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.item.NightmareFuelItem;
import io.github.lounode.extrabotany.common.item.SpiritFuelItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.goblin_slayer.GoblinSlayerHelmetItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid.PleiadesCombatMaidSuitItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.pleiades_combat_maid.SanguinePleiadesCombatMaidSuitItem;
import io.github.lounode.extrabotany.common.item.equipment.armor.shadow_warrior.ShadowWarriorHelmetItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.AquaStoneItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.CosmeticBaubleItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.FeatherOfJingweiItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.MoonPendantItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.NatureOrbItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.PeaceAmuletItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.PotatoChipsItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.PureDaisyPendantItem;
import io.github.lounode.extrabotany.common.item.equipment.bauble.SilentEternityItem;
import io.github.lounode.extrabotany.common.item.equipment.shield.ManasteelShieldItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.FlamescionWeaponItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.ShadowKatanaItem;
import io.github.lounode.extrabotany.common.item.equipment.tool.hammer.RheinHammerItem;
import io.github.lounode.extrabotany.common.item.relic.ExcaliburItem;
import io.github.lounode.extrabotany.common.item.relic.OldExbotanyRelicSwordItem;
import io.github.lounode.extrabotany.common.item.relic.SpearOfSubspaceItem;
import io.github.lounode.extrabotany.common.item.relic.voidcore.CoreOfTheVoidItem;
import io.github.lounode.extrabotany.common.telemetry.ExtraBotanyTelemetry;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

public final class NeoForgeEventBridge {
	private NeoForgeEventBridge() {}

	public static void register(IEventBus bus) {
		bus.addListener(NeoForgeEventBridge::onLevelLoad);
		bus.addListener(NeoForgeEventBridge::onLevelUnload);
		bus.addListener(NeoForgeEventBridge::onServerStarted);
		bus.addListener(NeoForgeEventBridge::onServerStopping);
		bus.addListener(NeoForgeEventBridge::onFurnaceFuel);
		bus.addListener(NeoForgeEventBridge::onIncomingDamage);
		bus.addListener(NeoForgeEventBridge::onDamagePost);
		bus.addListener(NeoForgeEventBridge::onLivingHeal);
		bus.addListener(NeoForgeEventBridge::onLivingDeath);
		bus.addListener(NeoForgeEventBridge::onManaDiscount);
		bus.addListener(NeoForgeEventBridge::onFinalizeSpawn);
		bus.addListener(NeoForgeEventBridge::onEffectApplicable);
		bus.addListener(NeoForgeEventBridge::onEffectAdded);
		bus.addListener(NeoForgeEventBridge::onEffectRemoved);
		bus.addListener(NeoForgeEventBridge::onEffectExpired);
		bus.addListener(NeoForgeEventBridge::onShieldBlock);
		bus.addListener(NeoForgeEventBridge::onAdvancementEarned);
		bus.addListener(NeoForgeEventBridge::onAttackEntity);
		bus.addListener(NeoForgeEventBridge::onLeftClickEmpty);
		bus.addListener(NeoForgeEventBridge::onLeftClickBlock);
		bus.addListener(NeoForgeEventBridge::onRightClickBlock);
		bus.addListener(NeoForgeEventBridge::onBreakSpeed);
		bus.addListener(NeoForgeEventBridge::onPlayerLoggedOut);
		bus.addListener(NeoForgeEventBridge::onPlayerTickPost);
		bus.addListener(NeoForgeEventBridge::onSoundAtPosition);
		bus.addListener(NeoForgeEventBridge::onSoundAtEntity);
	}

	private static void onLevelLoad(LevelEvent.Load event) {
		WindImpl.EventHandler.onLevelLoad(event);
	}

	private static void onLevelUnload(LevelEvent.Unload event) {
		WindImpl.EventHandler.onLevelUnLoad(event);
	}

	private static void onServerStarted(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
		ExtraBotanyTelemetry.onServerStarted(event);
	}

	private static void onServerStopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event) {
		ExtraBotanyTelemetry.onServerStopping(event);
	}

	private static void onFurnaceFuel(FurnaceFuelBurnTimeEvent event) {
		SpiritFuelItem.makeFuel(event);
		NightmareFuelItem.makeFuel(event);
	}

	private static void onIncomingDamage(LivingIncomingDamageEvent event) {
		WarmMobEffect.EventHandler.onEntityHurt(event);
		ThirrorMobEffect.EventHandler.onLivingAttack(event);
		MoonPendantItem.EventHandler.onLivingAttack(event);
		FlamescionWeaponItem.onLivingAttack(event);
		CoreOfTheVoidItem.onLivingAttack(event);
		EternityMobEffect.onLivingAttack(event);
		if (event.isCanceled()) {
			return;
		}

		LinkMobEffect.onEntityDamaged(event);
		ShadowWarriorHelmetItem.EventHandler.onPlayerAttacked(event);
		GoblinSlayerHelmetItem.EventHandler.onPlayerAttack(event);
		PleiadesCombatMaidSuitItem.EventHandler.onEntityAttacked(event);
		PleiadesCombatMaidSuitItem.EventHandler.onPlayerAttacked(event);
		PeaceAmuletItem.EventHandler.onLivingHurt(event);
		CosmeticBaubleItem.EventHandler.onLivingHurt(event);
		CoreOfTheVoidItem.onLivingHurt(event);
		if (event.isCanceled()) {
			return;
		}

		CoreOfTheVoidItem.onLivingDamage(event);
	}

	private static void onDamagePost(LivingDamageEvent.Post event) {
		SanguinePleiadesCombatMaidSuitItem.EventHandler.onAttackLiving(event);
	}

	private static void onLivingHeal(LivingHealEvent event) {
		HealReverseMobEffect.onLivingHeal(event);
		WitchCurseMobEffect.onLivingHeal(event);
		SilentEternityItem.EventHandler.onLivingHeal(event);
	}

	private static void onLivingDeath(LivingDeathEvent event) {
		SanguinePleiadesCombatMaidSuitItem.EventHandler.onKilled(event);
		PotatoChipsItem.EventHandler.onPlayerDeath(event);
	}

	private static void onManaDiscount(ManaDiscountEvent event) {
		if (AquaStoneItem.hasDiscount(event.getEntityPlayer())) {
			event.setDiscount(event.getDiscount() + AquaStoneItem.MANA_DISCOUNT);
		}
	}

	private static void onFinalizeSpawn(FinalizeSpawnEvent event) {
		var config = ExtraBotanyConfig.common();
		if (config == null || !config.enableCandyBagMobSpawn()) {
			return;
		}

		var mob = event.getEntity();
		if (!(mob instanceof Zombie || mob instanceof AbstractSkeleton)) {
			return;
		}
		if (!mob.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty()) {
			return;
		}
		if (mob.getRandom().nextDouble() >= config.candyBagMobSpawnChance()) {
			return;
		}

		mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ExtraBotanyItems.candyBag));
		mob.setDropChance(EquipmentSlot.OFFHAND, 1.0F);
	}

	private static void onEffectApplicable(MobEffectEvent.Applicable event) {
		CoreOfTheVoidItem.onEffectAdd(event);
	}

	private static void onEffectAdded(MobEffectEvent.Added event) {
		SanguinePleiadesCombatMaidSuitItem.EventHandler.onEffectAdded(event);
	}

	private static void onEffectRemoved(MobEffectEvent.Remove event) {
		if (event.getEffectInstance() != null) {
			SanguinePleiadesCombatMaidSuitItem.EventHandler.onEffectRemove(event);
		}
	}

	private static void onEffectExpired(MobEffectEvent.Expired event) {
		SanguinePleiadesCombatMaidSuitItem.EventHandler.onEffectExpired(event);
	}

	private static void onShieldBlock(LivingShieldBlockEvent event) {
		ManasteelShieldItem.EventHandler.onShieldBlockDamage(event);
	}

	private static void onAdvancementEarned(AdvancementEvent.AdvancementEarnEvent event) {
		if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
			OldExbotanyStatRewardHandler.onAdvancementEarned(serverPlayer, event.getAdvancement().id());
		}
	}

	private static void onAttackEntity(AttackEntityEvent event) {
		ExcaliburItem.attackEntity(event);
		OldExbotanyRelicSwordItem.attackEntity(event);
		FlamescionWeaponItem.attackEntity(event);
		ShadowKatanaItem.attackEntity(event);
		SpearOfSubspaceItem.attackEntity(event);
		FeatherOfJingweiItem.attackEntity(event);
		io.github.lounode.extrabotany.common.item.relic.void_archives.variants.Excalibur.attackEntity(event);
	}

	private static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
		ExcaliburItem.leftClick(event);
		OldExbotanyRelicSwordItem.leftClick(event);
		FlamescionWeaponItem.leftClick(event);
		FeatherOfJingweiItem.leftClick(event);
		io.github.lounode.extrabotany.common.item.relic.void_archives.variants.Excalibur.leftClick(event);
	}

	private static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		OldExbotanyRelicSwordItem.leftClickBlock(event);
		FeatherOfJingweiItem.leftClickBlock(event);
	}

	private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		NatureOrbItem.onPlayerInteract(event);
		PureDaisyPendantItem.EventHandler.onPlayerInteract(event);
		if (event.isCanceled()) {
			event.setCancellationResult(InteractionResult.SUCCESS);
		}
	}

	private static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
		RheinHammerItem.onDig(event);
	}

	private static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		CoreOfTheVoidItem.playerLoggedOut(event);
	}

	private static void onPlayerTickPost(PlayerTickEvent.Post event) {
		HerrscherCombatHelper.sanitizeHealth(event.getEntity());
		CoreOfTheVoidItem.updatePlayerFlyStatus(event.getEntity());
		FlamescionWeaponItem.onPlayerTick(event.getEntity());
		EternityMobEffect.onPlayerTick(event.getEntity());
		PureDaisyPendantItem.onPlayerTick(event.getEntity());
	}

	private static void onSoundAtPosition(PlayLevelSoundEvent.AtPosition event) {
		for (ResoncundBlockEntity listener : ResoncundBlockEntity.listeners()) {
			listener.onPlayLevelSound(event);
		}
	}

	private static void onSoundAtEntity(PlayLevelSoundEvent.AtEntity event) {
		for (ResoncundBlockEntity listener : ResoncundBlockEntity.listeners()) {
			listener.onPlayLevelSound(event);
		}
	}
}
