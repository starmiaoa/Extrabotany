package io.github.lounode.extrabotany.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

import io.github.lounode.extrabotany.common.entity.MotorEntity;
import io.github.lounode.extrabotany.network.serverbound.MotorInputPacket;
import io.github.lounode.extrabotany.xplat.ExClientXplatAbstractions;

public final class MotorInputHandler {
	private static boolean lastForward;
	private static boolean lastBack;
	private static boolean lastLeft;
	private static boolean lastRight;
	private static boolean lastJump;
	private static boolean wasRiding;

	private MotorInputHandler() {}

	public static void tick(Minecraft minecraft) {
		if (minecraft.player == null
				|| !(minecraft.player.getVehicle() instanceof MotorEntity motor)
				|| motor.getControllingPassenger() != minecraft.player) {
			if (wasRiding && minecraft.player != null) {
				ExClientXplatAbstractions.INSTANCE.sendToServer(new MotorInputPacket(false, false, false, false, false, false));
			}
			wasRiding = false;
			reset();
			return;
		}
		wasRiding = true;
		Options options = minecraft.options;
		boolean forward = options.keyUp.isDown();
		boolean back = options.keyDown.isDown();
		boolean left = options.keyLeft.isDown();
		boolean right = options.keyRight.isDown();
		boolean jump = options.keyJump.isDown();
		boolean cyclonePressed = minecraft.screen == null && options.keySprint.consumeClick();

		motor.updateInput(forward, back, left, right, jump, false);

		if (forward != lastForward || back != lastBack || left != lastLeft || right != lastRight
				|| jump != lastJump || cyclonePressed) {
			ExClientXplatAbstractions.INSTANCE.sendToServer(new MotorInputPacket(forward, back, left, right, jump, cyclonePressed));
			lastForward = forward;
			lastBack = back;
			lastLeft = left;
			lastRight = right;
			lastJump = jump;
		}
	}

	private static void reset() {
		lastForward = false;
		lastBack = false;
		lastLeft = false;
		lastRight = false;
		lastJump = false;
	}
}
