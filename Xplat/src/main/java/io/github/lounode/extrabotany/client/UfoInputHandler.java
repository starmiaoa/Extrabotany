package io.github.lounode.extrabotany.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

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
	private static boolean lastCatch;

	private UfoInputHandler() {}

	public static void tick(Minecraft minecraft) {
		if (minecraft.player == null || !(minecraft.player.getVehicle() instanceof UfoEntity)) {
			reset();
			return;
		}
		Options options = minecraft.options;
		boolean forward = options.keyUp.isDown();
		boolean back = options.keyDown.isDown();
		boolean left = options.keyLeft.isDown();
		boolean right = options.keyRight.isDown();
		boolean up = options.keyJump.isDown();
		boolean down = options.keyShift.isDown();
		boolean catchPressed = options.keySprint.consumeClick();

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
		lastCatch = catchPressed;
	}

	private static void reset() {
		lastForward = false;
		lastBack = false;
		lastLeft = false;
		lastRight = false;
		lastUp = false;
		lastDown = false;
		lastCatch = false;
	}
}
