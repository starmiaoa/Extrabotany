package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.relic.RelicImpl;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.entity.JudahOathEntity;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import io.github.lounode.extrabotany.common.util.HerrscherCombatHelper;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import java.util.List;

public class JudahOathItem extends SwordItem {
	private static final int MANA_PER_USE = 2500;
	private static final int DEPLOY_COOLDOWN = 80;

	private final Variant variant;

	public JudahOathItem(Variant variant, Properties properties) {
		super(Tiers.DIAMOND, properties.attributes(SwordItem.createAttributes(Tiers.DIAMOND, 12, -3.4F)));
		this.variant = variant;
	}

	public Variant getVariant() {
		return this.variant;
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (!attacker.level().isClientSide() && attacker instanceof Player player && isRightPlayer(player, stack)) {
			switch (this.variant) {
				case JUDAH -> {
					// Chains of Binding passive: brief slow/bind + bonus true light damage on bound targets
					boolean alreadyBound = target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) || target.hasEffect(ExtraBotanyMobEffects.IMMOBILIZE);
					target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2));
					if (alreadyBound) {
						HerrscherCombatHelper.dealTrueMagicDamage(target, player, 3.0F);
					}
					if (attacker.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), 8, 0.35D, 0.4D, 0.35D, 0.08D);
					}
				}
				case KIRA -> {
					// Frost-Star Resonance passive: chills target + grants agile starlight speed to wielder
					target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
					target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 80, target.getTicksFrozen() + 70));
					player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, false, false, true));
					if (attacker.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), 10, 0.4D, 0.45D, 0.4D, 0.05D);
					}
				}
				case SAKURA -> {
					// Sakura Brand passive: ignites target + triggers a close-range blossom flame burst
					target.igniteForSeconds(4.0F);
					AABB burstBox = target.getBoundingBox().inflate(2.2D);
					for (LivingEntity nearby : DamageHandler.INSTANCE.getFilteredEntities(attacker.level().getEntitiesOfClass(LivingEntity.class, burstBox), player)) {
						if (nearby != target) {
							HerrscherCombatHelper.dealTrueMagicDamage(nearby, player, 2.5F);
							nearby.igniteForSeconds(3.0F);
						}
					}
					if (attacker.level() instanceof ServerLevel sl) {
						sl.sendParticles(ParticleTypes.CHERRY_LEAVES, target.getX(), target.getY() + target.getBbHeight() * 0.6D, target.getZ(), 12, 0.6D, 0.5D, 0.6D, 0.06D);
						sl.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(), 6, 0.35D, 0.35D, 0.35D, 0.04D);
					}
				}
			}
		}
		return super.hurtEnemy(stack, target, attacker);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (level.isClientSide()) {
			return InteractionResultHolder.sidedSuccess(stack, true);
		}

		Relic relic = EXplatAbstractions.INSTANCE.findRelic(stack);
		if (relic != null) {
			relic.tickBinding(player);
		}
		if (relic == null || !relic.isRightPlayer(player) || !ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_USE, true)) {
			return InteractionResultHolder.fail(stack);
		}

		player.getCooldowns().addCooldown(this, DEPLOY_COOLDOWN);
		level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.9F, 0.78F);
		if (this.variant == Variant.SAKURA) {
			level.playSound(null, player.getX(), player.getY(), player.getZ(), ExtraBotanySounds.FLAMESCION_SLASH, SoundSource.PLAYERS, 0.55F, 1.35F);
		}

		JudahOathEntity judah = new JudahOathEntity(level, player, this.variant);
		judah.setPos(player.getX(), player.getY() + 1.15D, player.getZ());
		judah.setDeployYaw(-player.getYRot());
		judah.setYRot(player.getYRot());
		judah.shootFromRotation(player, Math.max(-15.0F, player.getXRot() - 8.0F), player.getYRot(), 0.0F, 0.68F, 0.0F);
		level.addFreshEntity(judah);

		return InteractionResultHolder.sidedSuccess(stack, false);
	}

	private boolean isRightPlayer(Player player, ItemStack stack) {
		Relic relic = EXplatAbstractions.INSTANCE.findRelic(stack);
		return relic != null && relic.isRightPlayer(player);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		if (!level.isClientSide && entity instanceof Player player) {
			Relic relic = EXplatAbstractions.INSTANCE.findRelic(stack);
			if (relic != null) {
				relic.tickBinding(player);
			}
		}
		super.inventoryTick(stack, level, entity, slot, selected);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		String key = switch (this.variant) {
			case KIRA -> "tooltip.extrabotany.judah_oath_kira";
			case SAKURA -> "tooltip.extrabotany.judah_oath_sakura";
			default -> "tooltip.extrabotany.judah_oath";
		};
		ChatFormatting accent = switch (this.variant) {
			case KIRA -> ChatFormatting.AQUA;
			case SAKURA -> ChatFormatting.LIGHT_PURPLE;
			default -> ChatFormatting.GOLD;
		};
		tooltip.add(Component.translatable(key).withStyle(accent));
		tooltip.add(Component.translatable(key + ".desc").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.empty());
		RelicImpl.addDefaultTooltip(stack, tooltip);
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, null);
	}

	public enum Variant {
		JUDAH(0, 1.0F, 0.80F, 0.18F, 0xFFFFCC33),
		KIRA(1, 0.24F, 0.86F, 1.0F, 0xFF3EDBFF),
		SAKURA(2, 1.0F, 0.56F, 0.72F, 0xFFFF8FB8);

		private final int id;
		private final float red;
		private final float green;
		private final float blue;
		private final int argb;

		Variant(int id, float red, float green, float blue, int argb) {
			this.id = id;
			this.red = red;
			this.green = green;
			this.blue = blue;
			this.argb = argb;
		}

		public int id() {
			return this.id;
		}

		public float red() {
			return this.red;
		}

		public float green() {
			return this.green;
		}

		public float blue() {
			return this.blue;
		}

		public int argb() {
			return this.argb;
		}

		public static Variant byId(int id) {
			for (Variant variant : values()) {
				if (variant.id == id) {
					return variant;
				}
			}
			return JUDAH;
		}
	}
}
