package io.github.lounode.extrabotany.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;

import io.github.lounode.extrabotany.network.serverbound.MountAccessoryPacket;
import io.github.lounode.extrabotany.xplat.ExClientXplatAbstractions;

import org.lwjgl.glfw.GLFW;

public final class MountAccessoryInputHandler {
	private static boolean lastMount;

	private MountAccessoryInputHandler() {}

	public static void tick(Minecraft minecraft) {
		if (minecraft.player == null || minecraft.screen != null) {
			lastMount = false;
			return;
		}
		boolean mount = InputConstants.isKeyDown(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_R);
		if (mount && !lastMount && minecraft.player.getVehicle() == null) {
			ExClientXplatAbstractions.INSTANCE.sendToServer(new MountAccessoryPacket());
		}
		lastMount = mount;
	}
}
