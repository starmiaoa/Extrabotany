package io.github.lounode.extrabotany.client.hud;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block.WandHUD;

import io.github.lounode.extrabotany.common.block.block_entity.ManaBufferBlockEntity;

public class ManaBufferWandHud implements WandHUD {
	private final ManaBufferBlockEntity buffer;

	public ManaBufferWandHud(ManaBufferBlockEntity buffer) {
		this.buffer = buffer;
	}

	@Override
	public void renderHUD(GuiGraphics graphics, Window window, Font font, float partialTicks) {
		String name = I18n.get(buffer.getBlockState().getBlock().getDescriptionId());
		BotaniaAPIClient.instance().drawSimpleManaHUD(graphics, window, font, 0x00FFFF, buffer.getCurrentMana(), buffer.getMaxMana(), name);
	}
}
