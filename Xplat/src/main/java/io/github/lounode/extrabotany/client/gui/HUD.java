package io.github.lounode.extrabotany.client.gui;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import com.google.common.collect.Iterables;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.xplat.BotaniaConfig;

import io.github.lounode.extrabotany.common.item.relic.CameraItem;
import io.github.lounode.extrabotany.common.item.relic.void_archives.VoidArchivesItem;
import io.github.lounode.extrabotany.common.item.relic.void_archives.variants.Camera;
import io.github.lounode.extrabotany.common.entity.MotorEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class HUD {
	public static HUD INSTANCE;
	private static final ResourceLocation manaBar = ResourceLocation.tryParse("botania:textures/gui/mana_hud.png");


	private final Minecraft minecraft;
	private final ColorfulBossBarOverlay bossOverlay;
	private float scopeScale;

	public HUD(Minecraft minecraft) {
		INSTANCE = this;
		this.minecraft = minecraft;
		this.bossOverlay = new ColorfulBossBarOverlay(minecraft);
	}

	public void onDrawScreenPost(GuiGraphics gui, float partialTicks) {
		PoseStack ms = gui.pose();
		if (minecraft.options.hideGui) {
			return;
		}
		ProfilerFiller profiler = minecraft.getProfiler();
		profiler.push("extrabotany-hud");

		scopeScale = Mth.lerp(0.5F * partialTicks, scopeScale, 1.125F);
		if (minecraft.options.getCameraType().isFirstPerson()) {
			if (shouldRenderCameraGUI()) {
				profiler.push("camera-hud");
				CameraItem.Hud.renderSpyglassOverlay(gui, scopeScale);
				profiler.pop();
			} else {
				scopeScale = 0.5F;
			}
		}

		profiler.push("boss-bar");
		bossOverlay.render(gui);
		profiler.pop();

		profiler.push("manaBar");
		renderManaBar(gui, partialTicks);
		profiler.pop();

		profiler.push("motorHud");
		renderMotorHud(gui);
		profiler.pop();

		profiler.pop();
	}

	private void renderMotorHud(GuiGraphics gui) {
		if (!(minecraft.player.getVehicle() instanceof MotorEntity motor)) {
			return;
		}
		int energy = motor.getTectonicEnergy();
		int barW = 82;
		int barH = 5;
		int cx = minecraft.getWindow().getGuiScaledWidth() / 2;
		int x = cx - barW / 2;
		int y = minecraft.getWindow().getGuiScaledHeight() - 56;

		int fillW = (int) (barW * energy / 800.0D);
		int fillColor = energy >= 400 ? 0xFF00E5FF : energy >= 200 ? 0xFF00ACC1 : 0xFF00838F;

		gui.fill(x - 1, y - 1, x + barW + 1, y + barH + 1, 0xA0000000);
		if (fillW > 0) {
			gui.fill(x, y, x + fillW, y + barH, fillColor);
		}

		String text = energy + " / 800";
		int tw = minecraft.font.width(text);
		gui.drawString(minecraft.font, text, cx - tw / 2, y - 10, 0xFFFFFFFF, true);
	}

	private void renderManaBar(GuiGraphics gui, float partialTicks) {
		Player player = minecraft.player;
		if (player.isSpectator()) {
			return;
		}
		BigDecimal totalMana = BigDecimal.ZERO;
		BigDecimal totalMaxMana = BigDecimal.ZERO;
		boolean anyRequest = false;
		Container mainInv = player.getInventory();
		Container accInv = BotaniaAPI.instance().getAccessoriesInventory(player);
		int invSize = mainInv.getContainerSize();
		int size = invSize + accInv.getContainerSize();

		for (int i = 0; i < size; ++i) {
			boolean useAccessories = i >= invSize;
			Container inv = useAccessories ? accInv : mainInv;
			ItemStack stack = inv.getItem(i - (useAccessories ? invSize : 0));
			if (!stack.isEmpty()) {
				anyRequest = anyRequest || stack.is(BotaniaTags.Items.MANA_USING_ITEMS);
			}
		}

		List<ItemStack> items = ManaItemHandler.instance().getManaItems(player);
		List<ItemStack> acc = ManaItemHandler.instance().getManaAccesories(player);

		for (ItemStack stack : Iterables.concat(items, acc)) {
			ManaItem manaItem = EXplatAbstractions.INSTANCE.findManaItem(stack);
			if (!manaItem.isNoExport()) {
				totalMana = totalMana.add(BigDecimal.valueOf(manaItem.getMana()));
				totalMaxMana = totalMaxMana.add(BigDecimal.valueOf(manaItem.getMaxMana()));
			}
		}

		if (anyRequest && !totalMaxMana.equals(BigDecimal.ZERO)) {
			renderManaInvBar(gui, totalMana.divide(totalMaxMana, 3, RoundingMode.HALF_UP).floatValue());
		}
	}

	private static void renderManaInvBar(GuiGraphics gui, float progress) {
		Minecraft mc = Minecraft.getInstance();
		int width = 182;
		int x = mc.getWindow().getGuiScaledWidth() / 2 - width / 2;
		int y = mc.getWindow().getGuiScaledHeight() - BotaniaConfig.client().manaBarHeight();

		width = (int) ((double) width * progress);

		if (width == 0) {
			width = 1;
		}

		int color = Mth.hsvToRgb(0.55F, (float) Math.min((double) 1.0F, Math.sin((double) Util.getMillis() / (double) 200.0F) * (double) 0.5F + (double) 1.0F), 1.0F);
		int r = color >> 16 & 255;
		int g = color >> 8 & 255;
		int b = color & 255;
		RenderSystem.setShaderColor((float) r / 255.0F, (float) g / 255.0F, (float) b / 255.0F, 1.0F - (float) r / 255.0F);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(770, 771);
		RenderHelper.drawTexturedModalRect(gui, manaBar, x, y, 0, 251, width, 5);
		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private boolean shouldRenderCameraGUI() {
		ItemStack useItem = minecraft.player.getUseItem();
		if (useItem.getItem() instanceof CameraItem) {
			return true;
		}
		if (useItem.getItem() instanceof VoidArchivesItem item) {
			if (VoidArchivesItem.getVariant(useItem).getId().equals(Camera.INSTANCE.getId())) {
				return true;
			}
		}

		return false;
	}

	public static HUD getInstance() {
		return INSTANCE;
	}

	public ColorfulBossBarOverlay getBossOverlay() {
		return bossOverlay;
	}

	//TODO ClientPlayerNetworkEvent
	public static void onDisconnected() {
		HUD.getInstance().getBossOverlay().reset();
	}

}
