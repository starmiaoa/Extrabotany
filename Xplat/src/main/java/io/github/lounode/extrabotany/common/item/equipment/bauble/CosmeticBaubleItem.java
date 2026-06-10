package io.github.lounode.extrabotany.common.item.equipment.bauble;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import vazkii.botania.client.render.AccessoryRenderRegistry;
import vazkii.botania.client.render.AccessoryRenderer;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.proxy.Proxy;

import io.github.lounode.eventwrapper.event.entity.living.LivingHurtEventWrapper;
import io.github.lounode.eventwrapper.eventbus.api.EventBusSubscriberWrapper;
import io.github.lounode.eventwrapper.eventbus.api.SubscribeEventWrapper;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.List;

public class CosmeticBaubleItem extends SimpleBaubleItem {
	private final Variant variant;

	public CosmeticBaubleItem(Variant variant, Properties props) {
		super(props);
		this.variant = variant;
		Proxy.INSTANCE.runOnClient(() -> () -> AccessoryRenderRegistry.register(this, new Renderer()));
	}

	@Override
	public boolean hasRender(ItemStack stack, LivingEntity living) {
		return super.hasRender(stack, living) && living instanceof Player;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, world, tooltip, flags);
		if (variant == Variant.FOX_MASK) {
			tooltip.add(Component.translatable("extrabotany.foxmaskinfo0").withStyle(ChatFormatting.ITALIC));
			tooltip.add(Component.translatable("extrabotany.foxmaskinfo1").withStyle(ChatFormatting.ITALIC));
			tooltip.add(Component.translatable("extrabotany.foxmaskinfo2").withStyle(ChatFormatting.ITALIC));
		}
	}

	public enum Variant {
		FOX_EAR(true, 0F, -0.8F, -0.1F, 0.8F, -0.8F, -0.8F),
		FOX_MASK(true, 0.02F, -0.3F, -0.3F, 0.66F, -0.65F, -0.65F),
		PYLON(true, 0F, -0.8F, -0.1F, 0.5F, -0.5F, -0.5F),
		BLACK_GLASSES(true, 0F, -0.2F, -0.3F, 0.55F, 0.55F, -0.55F),
		THUG_LIFE(true, 0F, -0.2F, -0.3F, 0.7F, -0.7F, -0.7F),
		SUPER_CROWN(true, 0F, -0.7F, -0.1F, 0.65F, -0.65F, -0.65F),
		MASK(true, 0F, -0.3F, -0.3F, 0.65F, -0.65F, -0.65F),
		RED_SCARF(false, 0F, 0.16F, -0.15F, 0.55F, -0.55F, -0.55F);

		private final boolean head;
		private final float translateX;
		private final float translateY;
		private final float translateZ;
		private final float scaleX;
		private final float scaleY;
		private final float scaleZ;

		Variant(boolean head, float translateX, float translateY, float translateZ, float scaleX, float scaleY, float scaleZ) {
			this.head = head;
			this.translateX = translateX;
			this.translateY = translateY;
			this.translateZ = translateZ;
			this.scaleX = scaleX;
			this.scaleY = scaleY;
			this.scaleZ = scaleZ;
		}
	}

	@EventBusSubscriberWrapper
	public static class EventHandler {
		private static final float SUPER_CROWN_DAMAGE_REDUCTION = 2.0F;

		@SubscribeEventWrapper
		public static void onLivingHurt(LivingHurtEventWrapper event) {
			if (!(event.getEntity() instanceof Player player)) {
				return;
			}
			if (EquipmentHandler.findOrEmpty(ExtraBotanyItems.superCrown, player).isEmpty()) {
				return;
			}
			if (!hasMaidArmorSet(player)) {
				return;
			}

			event.setAmount(Math.max(0F, event.getAmount() - SUPER_CROWN_DAMAGE_REDUCTION));
		}

		private static boolean hasMaidArmorSet(Player player) {
			return hasMaidArmorSetItem(player, EquipmentSlot.HEAD)
					&& hasMaidArmorSetItem(player, EquipmentSlot.CHEST)
					&& hasMaidArmorSetItem(player, EquipmentSlot.LEGS)
					&& hasMaidArmorSetItem(player, EquipmentSlot.FEET);
		}

		private static boolean hasMaidArmorSetItem(Player player, EquipmentSlot slot) {
			ItemStack stack = player.getItemBySlot(slot);
			if (stack.isEmpty()) {
				return false;
			}

			return switch (slot) {
				case HEAD -> stack.is(ExtraBotanyItems.starryIdolHeadgear) || stack.is(ExtraBotanyItems.pleiadesCombatMaidHeadgear);
				case CHEST -> stack.is(ExtraBotanyItems.starryIdolSuit) || stack.is(ExtraBotanyItems.pleiadesCombatMaidSuit)
						|| stack.is(ExtraBotanyItems.sanguinePleiadesCombatMaidSuit);
				case LEGS -> stack.is(ExtraBotanyItems.starryIdolSkirt) || stack.is(ExtraBotanyItems.pleiadesCombatMaidSkirt);
				case FEET -> stack.is(ExtraBotanyItems.starryIdolBoots) || stack.is(ExtraBotanyItems.pleiadesCombatMaidBoots);
				default -> false;
			};
		}
	}

	private class Renderer implements AccessoryRenderer {
		@Override
		public void doRender(HumanoidModel<?> bipedModel, ItemStack stack, LivingEntity living, PoseStack ms, MultiBufferSource buffers, int light,
				float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
			ms.pushPose();
			if (variant.head) {
				bipedModel.head.translateAndRotate(ms);
			} else {
				bipedModel.body.translateAndRotate(ms);
			}
			ms.translate(variant.translateX, variant.translateY, variant.translateZ);
			ms.scale(variant.scaleX, variant.scaleY, variant.scaleZ);
			Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, light, OverlayTexture.NO_OVERLAY, ms, buffers, living.level(), 0);
			ms.popPose();
		}
	}
}
