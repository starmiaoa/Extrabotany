package io.github.lounode.extrabotany.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

import io.github.lounode.extrabotany.common.entity.FlyingBoatEntity;
import io.github.lounode.extrabotany.network.serverbound.FlyingBoatInputPacket;
import io.github.lounode.extrabotany.xplat.ExClientXplatAbstractions;

public final class FlyingBoatInputHandler {
	private static boolean lastForward;
	private static boolean lastBack;
	private static boolean lastLeft;
	private static boolean lastRight;
	private static boolean lastUp;

	private FlyingBoatInputHandler() {}

	public static void tick(Minecraft minecraft) {
		if (minecraft.player == null || !(minecraft.player.getVehicle() instanceof FlyingBoatEntity)) {
			reset();
			return;
		}
		Options options = minecraft.options;
		boolean forward = options.keyUp.isDown();
		boolean back = options.keyDown.isDown();
		boolean left = options.keyLeft.isDown();
		boolean right = options.keyRight.isDown();
		boolean up = options.keyJump.isDown();

		if (forward != lastForward || back != lastBack || left != lastLeft || right != lastRight || up != lastUp) {
			ExClientXplatAbstractions.INSTANCE.sendToServer(new FlyingBoatInputPacket(forward, back, left, right, up));
			lastForward = forward;
			lastBack = back;
			lastLeft = left;
			lastRight = right;
			lastUp = up;
		}
	}

	private static void reset() {
		lastForward = false;
		lastBack = false;
		lastLeft = false;
		lastRight = false;
		lastUp = false;
	}
}
