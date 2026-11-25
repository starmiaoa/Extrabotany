package io.github.lounode.extrabotany.client;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

import org.lwjgl.glfw.GLFW;

import io.github.lounode.extrabotany.common.entity.UfoEntity;
import io.github.lounode.extrabotany.network.serverbound.UfoInputPacket;
import io.github.lounode.extrabotany.xplat.ExClientXplatAbstractions;

public final class UfoInputHandler {
	private static boolean lastForward;
	private static boolean lastBack;
	private static boolean lastLeft;
	private static boolean lastRight;
	private static boolean lastUp;
	private static boolean lastDown;
	private static boolean lastCatchKey;
	private static boolean wasRiding;

	private UfoInputHandler() {}

	public static void tick(Minecraft minecraft) {
		if (minecraft.player == null
				|| !(minecraft.player.getVehicle() instanceof UfoEntity ufo)
				|| ufo.getControllingPassenger() != minecraft.player) {
			if (wasRiding && minecraft.player != null) {
				ExClientXplatAbstractions.INSTANCE.sendToServer(new UfoInputPacket(false, false, false, false, false, false, false));
			}
			wasRiding = false;
			reset();
			return;
		}
		boolean firstRideTick = !wasRiding;
		wasRiding = true;
		Options options = minecraft.options;
		boolean forward = options.keyUp.isDown();
		boolean back = options.keyDown.isDown();
		boolean left = options.keyLeft.isDown();
		boolean right = options.keyRight.isDown();
		boolean up = options.keyJump.isDown();
		boolean down = options.keySprint.isDown();
		boolean catchKeyDown = minecraft.screen == null
				&& InputConstants.isKeyDown(minecraft.getWindow().getWindow(), GLFW.GLFW_KEY_R);
		boolean catchPressed = !firstRideTick && catchKeyDown && !lastCatchKey;
		lastCatchKey = catchKeyDown;

		ufo.updateInput(forward, back, left, right, up, down, false);

		if (forward != lastForward || back != lastBack || left != lastLeft || right != lastRight
				|| up != lastUp || down != lastDown || catchPressed) {
			ExClientXplatAbstractions.INSTANCE.sendToServer(new UfoInputPacket(forward, back, left, right, up, down, catchPressed));
			lastForward = forward;
			lastBack = back;
			lastLeft = left;
			lastRight = right;
			lastUp = up;
			lastDown = down;
		}
	}

	private static void reset() {
		lastForward = false;
		lastBack = false;
		lastLeft = false;
		lastRight = false;
		lastUp = false;
		lastDown = false;
		lastCatchKey = false;
	}
}
