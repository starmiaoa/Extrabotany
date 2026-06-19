package io.github.lounode.extrabotany.common.item.relic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.item.equipment.tool.ToolCommons;

import io.github.lounode.extrabotany.common.entity.PhantomSwordEntity;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import java.util.UUID;

public class FirstFractalItem extends OldExbotanyRelicSwordItem {
	private static final double RANGE = 13D;
	private static final int MANA_PER_DAMAGE = 160;
	private static final UUID MOVEMENT_SPEED_MODIFIER = UUID.fromString("995829fa-94c0-41bd-b046-0468c509a48a");
	private static final UUID ATTACK_SPEED_MODIFIER = UUID.fromString("a0fca0c6-7a5c-4b93-b62d-41d438ec0846");
	private static final UUID ENTITY_REACH_MODIFIER = UUID.fromString("7d49d7f8-9e0d-4579-8a6d-355c16a7811b");

	public FirstFractalItem(Properties properties) {
		super(Tiers.NETHERITE, 3, -1.6F, 0, true, properties);
	}

	@Override
	protected void useSword(Player player, Entity target) {
		Vec3 targetPos = resolveTargetPos(player, target, 80D);
		double angle = -Math.PI + 2 * Math.PI * player.level().random.nextDouble();

		for (int i = 0; i < 3; i++) {
			ToolCommons.damageItemIfPossible(player.getMainHandItem(), 1, player, MANA_PER_DAMAGE);
			Vec3 start = randomStart(player, targetPos, angle);
			player.level().addFreshEntity(new PhantomSwordEntity(player.level(), player, start, targetPos, 5 + 5 * i));
			angle += 2 * Math.PI * player.level().random.nextDouble() * 0.08D + 2 * Math.PI * 0.17D;
		}

		Vec3 start = randomStart(player, targetPos, angle);
		player.level().addFreshEntity(new PhantomSwordEntity(player.level(), player, start, targetPos, 0, 9));
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(), BotaniaSounds.terraBlade, SoundSource.PLAYERS, 0.4F, 1.4F);
	}

	private Vec3 randomStart(Player player, Vec3 targetPos, double angle) {
		double pitch = 0.12D * Math.PI * player.level().random.nextDouble() + 0.28D * Math.PI;
		return new Vec3(
				targetPos.x + RANGE * Math.sin(pitch) * Math.cos(angle),
				targetPos.y + RANGE * Math.cos(pitch),
				targetPos.z + RANGE * Math.sin(pitch) * Math.sin(angle)
		);
	}

	@Override
	public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
		Multimap<Attribute, AttributeModifier> ret = super.getDefaultAttributeModifiers(slot);
		if (slot == EquipmentSlot.MAINHAND) {
			ret = HashMultimap.create(ret);
			ret.put(Attributes.MOVEMENT_SPEED,
					new AttributeModifier(MOVEMENT_SPEED_MODIFIER, "First Fractal modifier", 0.3D, AttributeModifier.Operation.MULTIPLY_TOTAL));
			ret.put(Attributes.ATTACK_SPEED,
					new AttributeModifier(ATTACK_SPEED_MODIFIER, "First Fractal attack speed", 0.15D, AttributeModifier.Operation.MULTIPLY_BASE));
			EXplatAbstractions.INSTANCE.addEntityReachModifier(ret, ENTITY_REACH_MODIFIER, "First Fractal entity reach", 5.0D);
		}
		return ret;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, level, entity, slot, selected);
		if (!level.isClientSide && entity instanceof Player player && stack.getDamageValue() > 0
				&& ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_DAMAGE * 2, true)) {
			stack.setDamageValue(stack.getDamageValue() - 1);
		}
	}
}
