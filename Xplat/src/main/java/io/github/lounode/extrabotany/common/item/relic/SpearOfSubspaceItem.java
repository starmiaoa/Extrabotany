package io.github.lounode.extrabotany.common.item.relic;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.relic.RelicImpl;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.entity.SubspaceEntity;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.sounds.ExtraBotanySounds;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class SpearOfSubspaceItem extends SwordItem {
	private static final int SPEAR_MANA = 600;
	private static final int DOMAIN_MANA = 10000;
	private static final int USE_DURATION = 200;

	public SpearOfSubspaceItem(Properties properties) {
		super(Tiers.DIAMOND, properties.attributes(SwordItem.createAttributes(Tiers.DIAMOND, 8, -1.6F)));
	}

	@Override
	public ItemAttributeModifiers getDefaultAttributeModifiers() {
		return super.getDefaultAttributeModifiers()
				.withModifierAdded(Attributes.BLOCK_INTERACTION_RANGE,
						new AttributeModifier(prefix("spear_of_subspace_block_reach"), 2D, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.bySlot(EquipmentSlot.MAINHAND))
				.withModifierAdded(Attributes.ENTITY_INTERACTION_RANGE,
						new AttributeModifier(prefix("spear_of_subspace_entity_reach"), 2D, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.bySlot(EquipmentSlot.MAINHAND));
	}

	public static void attackEntity(AttackEntityEvent event) {
		Player player = event.getEntity();
		if (!player.level().isClientSide && player.getMainHandItem().getItem() instanceof SpearOfSubspaceItem spear) {
			spear.trySpawnSpear(player, player.getAttackStrengthScale(0F));
		}
	}

	public static boolean tryUseFromPacket(ServerPlayer player, float attackStrength) {
		if (player.getMainHandItem().getItem() instanceof SpearOfSubspaceItem spear) {
			return spear.trySpawnSpear(player, attackStrength);
		}
		return false;
	}

	private boolean trySpawnSpear(Player player, float attackStrength) {
		ItemStack stack = player.getMainHandItem();
		if (player.isSpectator() || stack.getItem() != this || attackStrength != 1F || !isRightPlayer(player, stack)) {
			return false;
		}
		if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, SPEAR_MANA, true)) {
			return false;
		}

		Level level = player.level();
		SubspaceEntity subspace = new SubspaceEntity(level, player);
		subspace.configure(SubspaceEntity.TYPE_SPEAR, 24, 5, 10, 0.4F + level.random.nextFloat() * 0.15F,
				Mth.wrapDegrees(-player.getYRot() + 180));
		subspace.setPos(player.getX(), player.getY() + 2.5F + level.random.nextFloat() * 0.2F, player.getZ());
		subspace.setYRot(player.getYRot());
		level.addFreshEntity(subspace);
		return true;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		player.startUsingItem(hand);
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
		if (!(entity instanceof Player player) || stack.getItem() != this || !isRightPlayer(player, stack)) {
			return;
		}

		boolean paid = ManaItemHandler.instance().requestManaExactForTool(stack, player, DOMAIN_MANA, true);
		player.getCooldowns().addCooldown(this, paid ? 600 : 1200);
		if (!paid) {
			return;
		}

		player.setSprinting(true);
		player.setDeltaMovement(player.getDeltaMovement().add(0, 1.5D, 0));
		player.hurtMarked = true;
		player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.ETERNITY, 120, 0));
		level.playSound(null, player.getX(), player.getY(), player.getZ(),
				ExtraBotanySounds.SPEAR_OF_SUBSPACE_USE, SoundSource.PLAYERS, 1.6F, 1F);

		if (!level.isClientSide()) {
			spawnSubspaceDomain(level, player);
		}
	}

	private void spawnSubspaceDomain(Level level, Player player) {
		Vec3 look = player.getLookAngle().multiply(1, 0, 1);
		if (look.lengthSqr() == 0) {
			double playerRot = Math.toRadians(player.getYRot() + 90);
			look = new Vec3(Math.cos(playerRot), 0, Math.sin(playerRot));
		}
		look = look.normalize().scale(-2D);
		Vec3 axis = look.normalize().cross(new Vec3(-1, 0, -1)).normalize();
		if (axis.lengthSqr() == 0) {
			axis = new Vec3(1, 0, 0);
		}

		for (int i = 0; i < 24; i++) {
			int div = i / 8;
			int mod = i % 8;
			Vec3 origin = player.position().add(0, 1.6D, 0).add(look).add(0, 0, div * 0.1D);
			double rot = mod * Math.PI / 7D - Math.PI / 2D;
			Vec3 offset = axis.scale(div * 3.5D + 5D).xRot((float) rot);
			if (offset.y < 0) {
				offset = offset.multiply(1, -1, 1);
			}
			Vec3 pos = origin.add(offset);

			SubspaceEntity subspace = new SubspaceEntity(level, player);
			subspace.configure(SubspaceEntity.TYPE_DOMAIN, 120, 15 + level.random.nextInt(12), 10 + level.random.nextInt(10),
					1.0F + level.random.nextFloat(), Mth.wrapDegrees(-player.getYRot() + 180));
			subspace.setPos(pos.x, pos.y - 0.5D + level.random.nextFloat(), pos.z);
			subspace.setYRot(player.getYRot());
			level.addFreshEntity(subspace);
		}
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
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return USE_DURATION;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.NONE;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
		tooltip.add(Component.translatable("tooltip.extrabotany.spear_of_subspace").withStyle(ChatFormatting.GRAY));
		tooltip.add(Component.empty());
		RelicImpl.addDefaultTooltip(stack, tooltip);
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, null);
	}
}
