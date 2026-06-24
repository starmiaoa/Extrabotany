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

	private UfoInputHandler() {}

	public static void tick(Minecraft minecraft) {
		if (minecraft.player == null || !(minecraft.player.getVehicle() instanceof UfoEntity ufo)) {
			reset();
			return;
		}
		Options options = minecraft.options;
		boolean forward = options.keyUp.isDown();
		boolean back = options.keyDown.isDown();
		boolean left = options.keyLeft.isDown();
		boolean right = options.keyRight.isDown();
		boolean up = options.keyJump.isDown();
		boolean catchPressed = options.keySprint.consumeClick();

		// 客户端权威:本地端每 tick 直接写入输入,自己计算并施加运动(抓取纯服务端,本地不触发)
		// 不再读取潜行键:潜行还原为原版「下坐骑」,下降改为低头前进
		ufo.updateInput(forward, back, left, right, up, false);

		// 仅在输入变化或触发抓取时同步服务端(供抓取 / 被抓实体抬升使用)
		if (forward != lastForward || back != lastBack || left != lastLeft || right != lastRight
				|| up != lastUp || catchPressed) {
			ExClientXplatAbstractions.INSTANCE.sendToServer(new UfoInputPacket(forward, back, left, right, up, catchPressed));
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
