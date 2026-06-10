package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.block.BotaniaFlowerBlocks;
import vazkii.botania.common.helper.ItemNBTHelper;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.entity.FlowerWeaponEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KingGardenItem extends Item {
	private static final String TAG_WEAPONS_SPAWNED = "weapons_spawned";
	private static final String TAG_CHARGING = "charging";
	private static final String TAG_TYPE = "type";
	private static final int MAX_FLOWERS = 20;
	private static final int MANA_PER_WEAPON = 100;

	public static final int WEAPON_TYPES = 17;
	private static final Block[] FLOWERS = new Block[] {
			ExtrabotanyFlowerBlocks.bloodEnchantress,
			ExtrabotanyFlowerBlocks.sunshineLily,
			ExtrabotanyFlowerBlocks.moonlightLily,
			null,
			ExtrabotanyFlowerBlocks.stonesia,
			BotaniaFlowerBlocks.entropinnyum,
			BotaniaFlowerBlocks.dreadthorn,
			BotaniaFlowerBlocks.medumone,
			BotaniaFlowerBlocks.thermalily,
			BotaniaFlowerBlocks.tigerseye,
			BotaniaFlowerBlocks.bellethorn,
			BotaniaFlowerBlocks.heiseiDream,
			ExtrabotanyFlowerBlocks.annoyingflower,
			ExtrabotanyFlowerBlocks.manalink,
			ExtrabotanyFlowerBlocks.omniviolet,
			ExtrabotanyFlowerBlocks.bellflower,
			ExtrabotanyFlowerBlocks.tinkle
	};

	public KingGardenItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		setCharging(stack, true);
		player.startUsingItem(hand);
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	@Override
	public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remainingUseDuration) {
		int[] types = getTypes(stack);
		int spawned = getWeaponsSpawned(stack);
		if (remainingUseDuration == getUseDuration(stack) || spawned >= types.length || level.isClientSide()) {
			return;
		}
		if (living instanceof Player player && !ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_WEAPON, true)) {
			return;
		}

		Vec3 look = living.getLookAngle().multiply(1D, 0D, 1D);
		if (look.lengthSqr() == 0D) {
			double playerRot = Math.toRadians(living.getYRot() + 90D);
			look = new Vec3(Math.cos(playerRot), 0D, Math.sin(playerRot));
		}
		look = look.normalize().scale(-2D);

		int div = spawned / 5;
		int mod = spawned % 5;
		Vec3 origin = living.position().add(0D, 1.6D, 0D).add(look).add(0D, 0D, div * 0.1D);
		Vec3 axis = look.normalize().cross(new Vec3(-1D, 0D, -1D)).normalize();
		if (axis.lengthSqr() == 0D) {
			axis = new Vec3(1D, 0D, 0D);
		}
		double rot = mod * Math.PI / 4D - Math.PI / 2D;
		Vec3 offset = rotateAroundAxis(axis.scale(div * 3.5D + 5D), look.normalize(), rot);
		if (offset.y < 0D) {
			offset = offset.multiply(1D, -1D, 1D);
		}

		FlowerWeaponEntity weapon = new FlowerWeaponEntity(level, living);
		weapon.setPos(origin.add(offset));
		weapon.setYRot(living.getYRot());
		weapon.setVariety(types[spawned]);
		weapon.setDelay(spawned);
		weapon.setRotation(Mth.wrapDegrees(-living.getYRot() + 180F));
		level.addFreshEntity(weapon);
		level.playSound(null, weapon.getX(), weapon.getY(), weapon.getZ(), SoundEvents.TRIDENT_RETURN, SoundSource.PLAYERS, 1F, 1F + level.random.nextFloat() * 3F);
		setWeaponsSpawned(stack, spawned + 1);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int timeLeft) {
		if (getWeaponsSpawned(stack) >= getTypes(stack).length) {
			setCharging(stack, false);
			setWeaponsSpawned(stack, 0);
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
		if (!level.isClientSide() && !isSelected && isCharging(stack)) {
			setCharging(stack, false);
			setWeaponsSpawned(stack, 0);
		}
		super.inventoryTick(stack, level, entity, slotId, isSelected);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
		tooltip.add(Component.translatable("tooltip.extrabotany.king_garden.configuration", getTypes(stack).length, MAX_FLOWERS).withStyle(ChatFormatting.GRAY));
		List<Component> flowers = getConfiguredFlowerNames(stack);
		if (!flowers.isEmpty()) {
			tooltip.add(Component.translatable("tooltip.extrabotany.king_garden.flowers").withStyle(ChatFormatting.DARK_GRAY));
			flowers.stream().limit(5).forEach(name -> tooltip.add(Component.literal(" - ").append(name).withStyle(ChatFormatting.DARK_GRAY)));
			if (flowers.size() > 5) {
				tooltip.add(Component.translatable("tooltip.extrabotany.king_garden.more", flowers.size() - 5).withStyle(ChatFormatting.DARK_GRAY));
			}
		}
	}

	public static boolean canAddFlower(ItemStack stack) {
		return stack.is(ExtraBotanyItems.kingGarden) && getTypes(stack).length < MAX_FLOWERS;
	}

	public static boolean addFlower(ItemStack stack, ItemStack flowerStack) {
		int type = typeForFlower(flowerStack);
		if (type < 0 || !canAddFlower(stack)) {
			return false;
		}
		int[] current = getTypes(stack);
		int[] updated = Arrays.copyOf(current, current.length + 1);
		updated[updated.length - 1] = type;
		setTypes(stack, updated);
		return true;
	}

	public static int typeForFlower(ItemStack stack) {
		if (stack.isEmpty()) {
			return -1;
		}
		Item item = stack.getItem();
		for (int i = 0; i < FLOWERS.length; i++) {
			Block flower = FLOWERS[i];
			if (flower != null && item == flower.asItem()) {
				return i;
			}
		}
		return -1;
	}

	public static boolean isCharging(ItemStack stack) {
		return ItemNBTHelper.getBoolean(stack, TAG_CHARGING, false);
	}

	public static int[] getTypes(ItemStack stack) {
		return stack.hasTag() ? stack.getOrCreateTag().getIntArray(TAG_TYPE) : new int[0];
	}

	public static void setTypes(ItemStack stack, int[] types) {
		stack.getOrCreateTag().putIntArray(TAG_TYPE, types);
	}

	private static int getWeaponsSpawned(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_WEAPONS_SPAWNED, 0);
	}

	private static void setCharging(ItemStack stack, boolean charging) {
		ItemNBTHelper.setBoolean(stack, TAG_CHARGING, charging);
	}

	private static void setWeaponsSpawned(ItemStack stack, int count) {
		ItemNBTHelper.setInt(stack, TAG_WEAPONS_SPAWNED, count);
	}

	private static List<Component> getConfiguredFlowerNames(ItemStack stack) {
		List<Component> flowers = new ArrayList<>();
		for (int type : getTypes(stack)) {
			Block flower = type >= 0 && type < FLOWERS.length ? FLOWERS[type] : null;
			if (flower != null) {
				flowers.add(Component.translatable(flower.getDescriptionId()));
			} else {
				flowers.add(Component.literal(BuiltInRegistries.BLOCK.getKey(ExtrabotanyFlowerBlocks.bellflower).getNamespace() + ":unknown"));
			}
		}
		return flowers;
	}

	private static Vec3 rotateAroundAxis(Vec3 vector, Vec3 axis, double angle) {
		double cos = Math.cos(angle);
		double sin = Math.sin(angle);
		return vector.scale(cos)
				.add(axis.cross(vector).scale(sin))
				.add(axis.scale(axis.dot(vector) * (1D - cos)));
	}
}
