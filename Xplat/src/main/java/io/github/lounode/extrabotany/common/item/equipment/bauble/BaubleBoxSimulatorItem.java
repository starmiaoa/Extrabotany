package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.item.Relic;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.BaubleBoxItem;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;
import vazkii.botania.common.item.relic.RelicBaubleItem;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.xplat.XplatAbstractions;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Predicate;

/**
 * Ring of Elven King / ALL FOR ONE: simulates wearing the baubles stored in a carried
 * Bauble Box. Rewritten from the 1.12.2 original, which only forwarded onWornTick.
 * This version also forwards the equip/unequip lifecycle, applies equipped attribute
 * modifiers, lets EquipmentHandler lookups see box contents (via mixin), refuses to
 * act on relics not soulbound to the wearer, and never simulates the same bauble twice.
 */
public class BaubleBoxSimulatorItem extends RelicBaubleItem {
	// Soulbinding gates usage exactly like the 1.12.2 original gated on defeating Gaia.
	private static final ResourceLocation BIND_ADVANCEMENT =
			new ResourceLocation("botania", "main/gaia_guardian_kill");
	// Server and client tick on different Player instances, but guard anyway: in
	// singleplayer both threads touch this map.
	private static final Map<Player, SimulationState> STATES =
			Collections.synchronizedMap(new WeakHashMap<>());

	private final int simulatedSlots;

	public BaubleBoxSimulatorItem(int simulatedSlots, Properties props) {
		super(props);
		this.simulatedSlots = simulatedSlots;
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		super.onWornTick(stack, entity);
		if (!(entity instanceof Player player) || !isRightPlayer(stack, player)) {
			return;
		}
		// canEquip blocks a second simulator ring, but legacy stacks could still mean
		// two rings ticking the same box in one game tick — only the first one acts.
		SimulationState state = STATES.computeIfAbsent(player, p -> new SimulationState());
		boolean client = player.level().isClientSide();
		long now = player.level().getGameTime();
		if (state.lastTick(client) == now) {
			return;
		}
		state.setLastTick(client, now);

		BoxView box = findBox(player, simulatedSlots);
		Map<Item, ItemStack> simulated = collectSimulated(player, box);

		if (!client) {
			for (Map.Entry<Item, ItemStack> gone : state.simulated.entrySet()) {
				if (!simulated.containsKey(gone.getKey())) {
					((BaubleItem) gone.getKey()).onUnequipped(gone.getValue(), player);
				}
			}
			for (Map.Entry<Item, ItemStack> added : simulated.entrySet()) {
				if (!state.simulated.containsKey(added.getKey())) {
					((BaubleItem) added.getKey()).onEquipped(added.getValue(), player);
				}
			}
			state.simulated = simulated;
			reconcileAttributes(player, state, simulated);
		}

		for (ItemStack simStack : simulated.values()) {
			((BaubleItem) simStack.getItem()).onWornTick(simStack, player);
		}
		if (!client && box != null && !simulated.isEmpty()) {
			// The container is a fresh copy of the box's NBT; without this, data the
			// simulated baubles wrote (cooldowns, stored mana) is lost.
			box.contents().setChanged();
		}
	}

	@Override
	public void onUnequipped(ItemStack stack, LivingEntity entity) {
		super.onUnequipped(stack, entity);
		if (!(entity instanceof Player player) || player.level().isClientSide()) {
			return;
		}
		SimulationState state = STATES.remove(player);
		if (state == null) {
			return;
		}
		for (Map.Entry<Item, ItemStack> gone : state.simulated.entrySet()) {
			((BaubleItem) gone.getKey()).onUnequipped(gone.getValue(), player);
		}
		for (Map.Entry<UUID, Attribute> applied : state.appliedModifiers.entrySet()) {
			AttributeInstance instance = player.getAttribute(applied.getValue());
			if (instance != null) {
				instance.removeModifier(applied.getKey());
			}
		}
	}

	@Override
	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return super.canEquip(stack, entity) && !simulatorWorn(entity);
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flags) {
		tooltip.add(Component.translatable(getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
		super.appendHoverText(stack, world, tooltip, flags);
	}

	/**
	 * Mixin entry point: lets EquipmentHandler.findOrEmpty treat eligible box contents
	 * as worn, so "is bauble X equipped" checks (Loki/Thor rings, sash step assist...)
	 * recognize simulated baubles.
	 */
	public static ItemStack findSimulated(Predicate<ItemStack> predicate, LivingEntity living) {
		if (!(living instanceof Player player)) {
			return ItemStack.EMPTY;
		}
		Container worn = EquipmentHandler.getAllWorn(living);
		if (worn == null) {
			return ItemStack.EMPTY;
		}
		int slots = 0;
		for (int i = 0; i < worn.getContainerSize(); i++) {
			ItemStack wornStack = worn.getItem(i);
			if (wornStack.getItem() instanceof BaubleBoxSimulatorItem simulator && isRightPlayer(wornStack, player)) {
				slots = Math.max(slots, simulator.simulatedSlots);
			}
		}
		if (slots == 0) {
			return ItemStack.EMPTY;
		}
		BoxView box = findBox(player, slots);
		if (box == null) {
			return ItemStack.EMPTY;
		}
		for (int slot = 0; slot < box.slots(); slot++) {
			ItemStack candidate = box.contents().getItem(slot);
			if (canSimulate(candidate, player) && predicate.test(candidate)) {
				return candidate;
			}
		}
		return ItemStack.EMPTY;
	}

	public static boolean canSimulate(ItemStack stack, Player player) {
		if (stack.isEmpty()
				|| !(stack.getItem() instanceof BaubleItem)
				|| stack.getItem() instanceof BaubleBoxSimulatorItem) {
			return false;
		}
		// Relics must be worn for real (and soulbound) once before the box accepts them;
		// the 1.12.2 version let anyone leech foreign relics this way.
		Relic relic = XplatAbstractions.INSTANCE.findRelic(stack);
		return relic == null || relic.isRightPlayer(player);
	}

	private Map<Item, ItemStack> collectSimulated(Player player, @Nullable BoxView box) {
		if (box == null) {
			return Collections.emptyMap();
		}
		Set<Item> worn = actuallyWornItems(player);
		Map<Item, ItemStack> result = new LinkedHashMap<>();
		for (int slot = 0; slot < box.slots(); slot++) {
			ItemStack candidate = box.contents().getItem(slot);
			if (!canSimulate(candidate, player)) {
				continue;
			}
			Item item = candidate.getItem();
			// One instance per bauble type: no stacking a worn ring with a boxed copy,
			// no stacking duplicates across box slots.
			if (worn.contains(item) || result.containsKey(item)) {
				continue;
			}
			result.put(item, candidate);
		}
		return result;
	}

	private static void reconcileAttributes(Player player, SimulationState state, Map<Item, ItemStack> simulated) {
		Map<UUID, AttributeEntry> desired = new HashMap<>();
		for (ItemStack simStack : simulated.values()) {
			BaubleItem bauble = (BaubleItem) simStack.getItem();
			bauble.getEquippedAttributeModifiers(simStack).forEach((attribute, modifier) -> {
				String simName = simulatedModifierName(simStack.getItem(), modifier);
				UUID simId = UUID.nameUUIDFromBytes(simName.getBytes(StandardCharsets.UTF_8));
				desired.putIfAbsent(simId, new AttributeEntry(attribute,
						new AttributeModifier(simId, simName, modifier.getAmount(), modifier.getOperation())));
			});
		}
		for (Map.Entry<UUID, Attribute> stale : state.appliedModifiers.entrySet()) {
			if (!desired.containsKey(stale.getKey())) {
				AttributeInstance instance = player.getAttribute(stale.getValue());
				if (instance != null) {
					instance.removeModifier(stale.getKey());
				}
			}
		}
		Map<UUID, Attribute> applied = new HashMap<>();
		for (Map.Entry<UUID, AttributeEntry> entry : desired.entrySet()) {
			AttributeInstance instance = player.getAttribute(entry.getValue().attribute());
			if (instance != null) {
				if (instance.getModifier(entry.getKey()) == null) {
					// Transient: cleared on relog, rebuilt by the next worn tick.
					instance.addTransientModifier(entry.getValue().modifier());
				}
				applied.put(entry.getKey(), entry.getValue().attribute());
			}
		}
		state.appliedModifiers = applied;
	}

	private static String simulatedModifierName(Item item, AttributeModifier modifier) {
		// Re-namespaced so simulated modifiers never collide with ones applied by a
		// genuinely equipped bauble.
		return "extrabotany:box_sim/" + BuiltInRegistries.ITEM.getKey(item) + "/" + modifier.getId();
	}

	@Nullable
	private static BoxView findBox(Player player, int simulatedSlots) {
		Inventory inventory = player.getInventory();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack candidate = inventory.getItem(i);
			if (candidate.getItem() instanceof BaubleBoxItem) {
				SimpleContainer contents = BaubleBoxItem.getInventory(candidate);
				return new BoxView(contents, Math.min(simulatedSlots, contents.getContainerSize()));
			}
		}
		return null;
	}

	private static Set<Item> actuallyWornItems(Player player) {
		Container worn = EquipmentHandler.getAllWorn(player);
		if (worn == null) {
			return Collections.emptySet();
		}
		Set<Item> items = new HashSet<>();
		for (int i = 0; i < worn.getContainerSize(); i++) {
			ItemStack wornStack = worn.getItem(i);
			if (!wornStack.isEmpty()) {
				items.add(wornStack.getItem());
			}
		}
		return items;
	}

	private static boolean simulatorWorn(LivingEntity entity) {
		Container worn = EquipmentHandler.getAllWorn(entity);
		if (worn == null) {
			return false;
		}
		for (int i = 0; i < worn.getContainerSize(); i++) {
			if (worn.getItem(i).getItem() instanceof BaubleBoxSimulatorItem) {
				return true;
			}
		}
		return false;
	}

	private static boolean isRightPlayer(ItemStack stack, Player player) {
		Relic relic = XplatAbstractions.INSTANCE.findRelic(stack);
		return relic != null && relic.isRightPlayer(player);
	}

	public static Relic makeRelic(ItemStack stack) {
		return new RelicImpl(stack, BIND_ADVANCEMENT);
	}

	private record BoxView(SimpleContainer contents, int slots) {
	}

	private record AttributeEntry(Attribute attribute, AttributeModifier modifier) {
	}

	private static final class SimulationState {
		private Map<Item, ItemStack> simulated = Collections.emptyMap();
		private Map<UUID, Attribute> appliedModifiers = Collections.emptyMap();
		private long lastServerTick = Long.MIN_VALUE;
		private long lastClientTick = Long.MIN_VALUE;

		private long lastTick(boolean client) {
			return client ? lastClientTick : lastServerTick;
		}

		private void setLastTick(boolean client, long value) {
			if (client) {
				lastClientTick = value;
			} else {
				lastServerTick = value;
			}
		}
	}
}
