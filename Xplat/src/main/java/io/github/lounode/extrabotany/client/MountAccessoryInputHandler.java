package io.github.lounode.extrabotany.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;

import io.github.lounode.extrabotany.common.item.equipment.tool.FlamescionWeaponItem;
import io.github.lounode.extrabotany.network.serverbound.FlamescionShiftPacket;
import io.github.lounode.extrabotany.network.serverbound.FlamescionUltPacket;
import io.github.lounode.extrabotany.network.serverbound.MountAccessoryPacket;
import io.github.lounode.extrabotany.xplat.ExClientXplatAbstractions;

import org.lwjgl.glfw.GLFW;

public final class MountAccessoryInputHandler {
	private static boolean lastMount;
	private static boolean lastShift;

	private MountAccessoryInputHandler() {}

	public static void tick(Minecraft minecraft) {
		if (minecraft.player == null || minecraft.screen != null) {
			lastMount = false;
			lastShift = false;
			return;
		}
		boolean shift = InputConstants.isKeyDown(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT);
		if (shift && !lastShift && FlamescionWeaponItem.isFlamescionMode(minecraft.player)
				&& !minecraft.player.getCooldowns().isOnCooldown(minecraft.player.getMainHandItem().getItem())) {
			ExClientXplatAbstractions.INSTANCE.sendToServer(new FlamescionShiftPacket());
		}
		lastShift = shift;

		boolean mount = InputConstants.isKeyDown(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_R);
		if (mount && !lastMount) {
			if (FlamescionWeaponItem.isFlamescionMode(minecraft.player)) {
				ExClientXplatAbstractions.INSTANCE.sendToServer(new FlamescionUltPacket());
			} else if (minecraft.player.getVehicle() == null) {
				ExClientXplatAbstractions.INSTANCE.sendToServer(new MountAccessoryPacket());
			}
		}
		lastMount = mount;
	}
}
