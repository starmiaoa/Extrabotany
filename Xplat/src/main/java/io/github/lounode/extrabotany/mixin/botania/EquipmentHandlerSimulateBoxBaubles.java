package io.github.lounode.extrabotany.mixin.botania;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.handler.EquipmentHandler;

import io.github.lounode.extrabotany.common.item.equipment.bauble.BaubleBoxSimulatorItem;

import java.util.function.Predicate;

/**
 * When a worn Ring of Elven King / ALL FOR ONE simulates baubles out of a Bauble Box,
 * equipment lookups must see them too — most modern Botania bauble effects (Loki/Thor
 * rings, sash step assist, tiny planet...) check "is X equipped" instead of ticking.
 */
@Mixin(value = EquipmentHandler.class, remap = false)
public abstract class EquipmentHandlerSimulateBoxBaubles {

	@Inject(
		method = "findOrEmpty(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;",
		at = @At("RETURN"),
		cancellable = true
	)
	private static void extrabotany$findSimulatedByItem(Item item, LivingEntity living, CallbackInfoReturnable<ItemStack> cir) {
		if (cir.getReturnValue().isEmpty()) {
			ItemStack simulated = BaubleBoxSimulatorItem.findSimulated(stack -> stack.is(item), living);
			if (!simulated.isEmpty()) {
				cir.setReturnValue(simulated);
			}
		}
	}

	@Inject(
		method = "findOrEmpty(Ljava/util/function/Predicate;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;",
		at = @At("RETURN"),
		cancellable = true
	)
	private static void extrabotany$findSimulatedByPredicate(Predicate<ItemStack> predicate, LivingEntity living, CallbackInfoReturnable<ItemStack> cir) {
		if (cir.getReturnValue().isEmpty()) {
			ItemStack simulated = BaubleBoxSimulatorItem.findSimulated(predicate, living);
			if (!simulated.isEmpty()) {
				cir.setReturnValue(simulated);
			}
		}
	}
}
