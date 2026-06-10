package io.github.lounode.extrabotany.client.hud;

import net.minecraft.client.Minecraft;
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
	public void renderHUD(GuiGraphics graphics, Minecraft minecraft) {
		String name = I18n.get(buffer.getBlockState().getBlock().getDescriptionId());
		BotaniaAPIClient.instance().drawSimpleManaHUD(graphics, 0x00FFFF, buffer.getCurrentMana(), buffer.getMaxMana(), name);
	}
}
