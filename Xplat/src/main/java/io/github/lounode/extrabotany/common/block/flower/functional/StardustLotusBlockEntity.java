package io.github.lounode.extrabotany.common.block.flower.functional;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.gui.HUDHandler;

import io.github.lounode.extrabotany.common.block.flower.ExtrabotanyFlowerBlocks;
import io.github.lounode.extrabotany.common.item.equipment.tool.BinderItem;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

import java.util.List;

public class StardustLotusBlockEntity extends FunctionalFlowerBlockEntity {
	private static final String TAG_CONSUMED = "consumedMana";
	private static final String TAG_TARGET_X = "targetX";
	private static final String TAG_TARGET_Y = "targetY";
	private static final String TAG_TARGET_Z = "targetZ";
	private static final String TAG_HAS_TARGET = "hasTarget";
	private static final String TAG_HAS_PAPER = "hasPaper";

	private static final int RANGE = 1;
	public static final int MAX_MANA = 100000;
	public static final int BASE_COST = 20000;
	public static final int COST_PER_BLOCK = 150;
	public static final int CONSUME_SPEED = 800;

	private static final BlockPos[] QUARTZ_LOCATIONS = {
			new BlockPos(2, -1, 2), new BlockPos(2, -1, 1), new BlockPos(2, -1, 0), new BlockPos(2, -1, -1), new BlockPos(2, -1, -2),
			new BlockPos(1, -1, 2), new BlockPos(1, -1, -2),
			new BlockPos(0, -1, 2), new BlockPos(0, -1, -2),
			new BlockPos(-1, -1, 2), new BlockPos(-1, -1, -2),
			new BlockPos(-2, -1, 2), new BlockPos(-2, -1, 1), new BlockPos(-2, -1, 0), new BlockPos(-2, -1, -1), new BlockPos(-2, -1, -2)
	};
	private static final BlockPos[] LAMP_LOCATIONS = {
			new BlockPos(2, 2, 2), new BlockPos(-2, 2, 2), new BlockPos(2, 2, -2), new BlockPos(-2, 2, -2)
	};
	private static final BlockPos[] PILLAR_LOCATIONS = {
			new BlockPos(2, 1, 2), new BlockPos(-2, 1, 2), new BlockPos(2, 1, -2), new BlockPos(-2, 1, -2),
			new BlockPos(2, 0, 2), new BlockPos(-2, 0, 2), new BlockPos(2, 0, -2), new BlockPos(-2, 0, -2)
	};

	private int consumedMana;
	@Nullable
	private BlockPos target;
	private boolean hasPaper;

	public StardustLotusBlockEntity(BlockEntityType<? extends StardustLotusBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public StardustLotusBlockEntity(BlockPos pos, BlockState state) {
		super(ExtrabotanyFlowerBlocks.STARDUST_LOTUS, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();
		if (getLevel().isClientSide()) {
			return;
		}
		if (redstoneSignal > 0 || target == null || !isMultiblockValid()) {
			return;
		}

		int cost = getTeleportCost();
		if (cost <= 0) {
			return;
		}
		if (!hasPaper) {
			consumeNearbyPaper();
		}
		if (hasPaper && consumedMana < cost) {
			int manaToConsume = Math.min(Math.min(getConsumeSpeed(), getMana()), cost - consumedMana);
			if (manaToConsume > 0) {
				addMana(-manaToConsume);
				consumedMana += manaToConsume;
				sync();
			}
			return;
		}
		if (consumedMana >= cost) {
			teleportNearbyLiving();
		}
	}

	public static InteractionResult bindTarget(ItemStack stack, Level level, BlockPos pos, Player player) {
		if (!(level.getBlockEntity(pos) instanceof StardustLotusBlockEntity lotus)) {
			return InteractionResult.PASS;
		}
		ResourceLocation boundDimension = ResourceLocation.tryParse(BinderItem.getDimension(stack));
		if (boundDimension == null || !boundDimension.equals(level.dimension().location()) || BinderItem.getPosY(stack) == -1) {
			if (!level.isClientSide()) {
				player.displayClientMessage(Component.translatable("message.extrabotany.stardust_lotus.invalid_bind"), true);
			}
			return InteractionResult.sidedSuccess(level.isClientSide());
		}

		BlockPos target = new BlockPos(BinderItem.getPosX(stack), BinderItem.getPosY(stack), BinderItem.getPosZ(stack));
		if (!level.isClientSide()) {
			lotus.setTarget(target);
			player.displayClientMessage(Component.translatable("message.extrabotany.stardust_lotus.bind_to_pos",
					target.getX(), target.getY(), target.getZ()), true);
		}
		return InteractionResult.sidedSuccess(level.isClientSide());
	}

	private void consumeNearbyPaper() {
		AABB box = new AABB(getEffectivePos()).inflate(RANGE);
		List<ItemEntity> items = getLevel().getEntitiesOfClass(ItemEntity.class, box, item -> item.isAlive() && item.getItem().is(Items.PAPER));
		for (ItemEntity item : items) {
			ItemStack stack = item.getItem();
			if (!stack.isEmpty()) {
				stack.shrink(1);
				hasPaper = true;
				sync();
				return;
			}
		}
	}

	private void teleportNearbyLiving() {
		BlockPos targetPos = target;
		if (targetPos == null) {
			return;
		}

		AABB box = new AABB(getEffectivePos()).inflate(RANGE);
		List<LivingEntity> entities = getLevel().getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive);
		if (entities.isEmpty()) {
			return;
		}
		for (LivingEntity living : entities) {
			living.teleportTo(targetPos.getX() + 0.5D, targetPos.getY() + 1D, targetPos.getZ() + 0.5D);
		}

		consumedMana = 0;
		hasPaper = false;
		sync();
	}

	public boolean isMultiblockValid() {
		for (BlockPos offset : QUARTZ_LOCATIONS) {
			if (!getLevel().getBlockState(getBlockPos().offset(offset)).is(Blocks.QUARTZ_BLOCK)) {
				return false;
			}
		}
		for (BlockPos offset : LAMP_LOCATIONS) {
			if (!getLevel().getBlockState(getBlockPos().offset(offset)).is(Blocks.SEA_LANTERN)) {
				return false;
			}
		}
		for (BlockPos offset : PILLAR_LOCATIONS) {
			if (!getLevel().getBlockState(getBlockPos().offset(offset)).is(Blocks.LAPIS_BLOCK)) {
				return false;
			}
		}
		return true;
	}

	public void setTarget(BlockPos target) {
		this.target = target.immutable();
		this.consumedMana = 0;
		this.hasPaper = false;
		sync();
	}

	@Nullable
	public BlockPos getTarget() {
		return target;
	}

	public int getConsumedMana() {
		return consumedMana;
	}

	public int getTeleportCost() {
		BlockPos targetPos = target;
		if (targetPos == null) {
			return 0;
		}
		double distance = Math.sqrt(targetPos.distSqr(getBlockPos()));
		return (int) ((ExtraBotanyConfig.common().stardustLotusBaseCost()
				+ distance * ExtraBotanyConfig.common().stardustLotusCostPerBlock())
				* (isValidBinding() ? 0.8F : 1F));
	}

	public int getConsumeSpeed() {
		return ExtraBotanyConfig.common().stardustLotusConsumeSpeed();
	}

	@Override
	public int getMaxMana() {
		return ExtraBotanyConfig.common().stardustLotusMaxMana();
	}

	@Override
	public int getColor() {
		return 0x800080;
	}

	@Override
	public @Nullable RadiusDescriptor getRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
	}

	@Override
	public void writeToPacketNBT(CompoundTag tag) {
		super.writeToPacketNBT(tag);
		tag.putInt(TAG_CONSUMED, consumedMana);
		tag.putBoolean(TAG_HAS_PAPER, hasPaper);
		if (target != null) {
			tag.putBoolean(TAG_HAS_TARGET, true);
			tag.putInt(TAG_TARGET_X, target.getX());
			tag.putInt(TAG_TARGET_Y, target.getY());
			tag.putInt(TAG_TARGET_Z, target.getZ());
		} else {
			tag.putBoolean(TAG_HAS_TARGET, false);
		}
	}

	@Override
	public void readFromPacketNBT(CompoundTag tag) {
		super.readFromPacketNBT(tag);
		consumedMana = tag.getInt(TAG_CONSUMED);
		hasPaper = tag.getBoolean(TAG_HAS_PAPER);
		target = tag.getBoolean(TAG_HAS_TARGET)
				? new BlockPos(tag.getInt(TAG_TARGET_X), tag.getInt(TAG_TARGET_Y), tag.getInt(TAG_TARGET_Z))
				: null;
	}

	public static class WandHUD extends BindableFlowerWandHud<StardustLotusBlockEntity> {
		public WandHUD(StardustLotusBlockEntity flower) {
			super(flower);
		}

		@Override
		public void renderHUD(GuiGraphics gui, Minecraft mc, int minLeft, int minRight, int minDown) {
			String name = I18n.get(flower.getBlockState().getBlock().getDescriptionId());
			int centerX = mc.getWindow().getGuiScaledWidth() / 2;
			int centerY = mc.getWindow().getGuiScaledHeight() / 2;
			int left = (Math.max(102, mc.font.width(name)) + 4) / 2;
			int right = left + 20;

			left = Math.max(left, minLeft);
			right = Math.max(right, minRight);

			RenderHelper.renderHUDBox(gui, centerX - left, centerY + 8, centerX + right, centerY + Math.max(30, minDown + 52));
			BotaniaAPIClient.instance().drawComplexManaHUD(gui, flower.getColor(), flower.getConsumedMana(), flower.getTeleportCost(),
					name, flower.getHudIcon(), flower.isValidBinding());

			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			RenderHelper.drawTexturedModalRect(gui, HUDHandler.manaBar, centerX - 11, centerY + 34, 0, 38, 22, 15);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

			Component target = flower.getTarget() == null
					? Component.translatable("message.extrabotany.stardust_lotus.no_target")
					: Component.translatable("message.extrabotany.stardust_lotus.target",
							flower.getTarget().getX(), flower.getTarget().getY(), flower.getTarget().getZ());
			gui.drawString(mc.font, target, centerX - mc.font.width(target) / 2, centerY + 51, 0xFFFFFF);
		}
	}
}
