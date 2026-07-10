package io.github.lounode.extrabotany.forge.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.lounode.extrabotany.common.item.relic.CameraItem;

@Mixin(ItemCooldowns.class)
public abstract class CameraCooldownFinishMixin {

	@Inject(method = "onCooldownEnded", at = @At("TAIL"))
	private void extrabotany$onCooldownEnded(Item item, CallbackInfo ci) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player != null && minecraft.player.getCooldowns() == (ItemCooldowns) (Object) this) {
			CameraItem.onItemCooldownFinish(minecraft.player, item);
		}
	}
}
