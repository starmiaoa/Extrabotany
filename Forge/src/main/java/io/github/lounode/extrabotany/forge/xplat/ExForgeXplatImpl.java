package io.github.lounode.extrabotany.forge.xplat;

import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosApi;

import vazkii.botania.forge.xplat.ForgeXplatImpl;

import io.github.lounode.extrabotany.api.ExtrabotanyForgeCapabilities;
import io.github.lounode.extrabotany.api.item.NatureEnergyItem;
import io.github.lounode.extrabotany.common.block.block_entity.LivingrockBarrelBlockEntity;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.forge.network.ForgePacketHandler;
import io.github.lounode.extrabotany.network.ExtrabotanyPacket;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExForgeXplatImpl extends ForgeXplatImpl implements EXplatAbstractions {
	@Override
	public void sendToPlayer(ServerPlayer player, ExtrabotanyPacket packet) {
		ForgePacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Packet<ClientGamePacketListener> toVanillaClientboundPacket(ExtrabotanyPacket packet) {
		return (Packet<ClientGamePacketListener>) ForgePacketHandler.CHANNEL.toVanillaPacket(packet, NetworkDirection.PLAY_TO_CLIENT);
	}

	@Nullable
	@Override
	public NatureEnergyItem findNatureEnergyItem(ItemStack stack) {
		return stack.getCapability(ExtrabotanyForgeCapabilities.NATURE_ENERGY_ITEM).orElse(null);
	}

	@Override
	public String getExtraBotanyVersion() {
		return ModList.get().getModContainerById(LibMisc.MOD_ID).get().getModInfo().getVersion().toString();
	}

	@Override
	public Player createFakePlayer(ServerLevel level, GameProfile userName) {
		return FakePlayerFactory.get(level, userName);
	}

	@Override
	public int getFluidTemperature(Fluid fluid) {
		return fluid.getFluidType().getTemperature();
	}

	@Override
	public float getEnchantPowerBonus(ServerLevel level, BlockPos pos) {
		float j = 0;
		for (BlockPos blockpos : EnchantmentTableBlock.BOOKSHELF_OFFSETS) {
			if (EnchantmentTableBlock.isValidBookShelf(level, pos, blockpos)) {
				j += level.getBlockState(pos.offset(blockpos)).getEnchantPowerBonus(level, pos.offset(blockpos));
			}
		}
		return j;
	}

	@Override
	public void addSunRingReachModifiers(Multimap<Attribute, AttributeModifier> attributes, UUID modifierId) {
		attributes.put(ForgeMod.BLOCK_REACH.get(),
				new AttributeModifier(modifierId, "Sun Ring block reach", 3.5D, AttributeModifier.Operation.ADDITION));
		attributes.put(ForgeMod.ENTITY_REACH.get(),
				new AttributeModifier(modifierId, "Sun Ring entity reach", 3.5D, AttributeModifier.Operation.ADDITION));
	}

	@Override
	public void addEntityReachModifier(Multimap<Attribute, AttributeModifier> attributes, UUID modifierId, String name, double amount) {
		attributes.put(ForgeMod.ENTITY_REACH.get(),
				new AttributeModifier(modifierId, name, amount, AttributeModifier.Operation.ADDITION));
	}

	@Override
	public List<ItemStack> getEquippedCurios(Player player) {
		List<ItemStack> stacks = new ArrayList<>();
		CuriosApi.getCuriosInventory(player).ifPresent(inventory -> inventory.getCurios().forEach((slot, handler) -> {
			var stackHandler = handler.getStacks();
			for (int i = 0; i < stackHandler.getSlots(); i++) {
				stacks.add(stackHandler.getStackInSlot(i));
			}
		}));
		return stacks;
	}

	@Override
	public boolean interactWithLivingrockBarrel(Level level, BlockPos pos, Player player, InteractionHand hand, Direction side) {
		if (!(level.getBlockEntity(pos) instanceof LivingrockBarrelBlockEntity barrel)) {
			return false;
		}
		return FluidUtil.interactWithFluidHandler(player, hand, new LivingrockBarrelFluidHandler(barrel));
	}

	@Override
	public int extractEnergy(Level level, BlockPos pos, Direction side, int maxExtract, boolean simulate) {
		var blockEntity = level.getBlockEntity(pos);
		if (blockEntity == null) {
			return 0;
		}

		var storage = blockEntity.getCapability(ForgeCapabilities.ENERGY, side)
				.orElseGet(() -> blockEntity.getCapability(ForgeCapabilities.ENERGY, null).orElse(null));
		if (storage == null || !storage.canExtract()) {
			return 0;
		}
		return storage.extractEnergy(maxExtract, simulate);
	}

	@Override
	public int drainFluid(Level level, BlockPos pos, Direction side, Fluid fluid, int amount, boolean simulate) {
		var blockEntity = level.getBlockEntity(pos);
		if (blockEntity == null || amount <= 0) {
			return 0;
		}

		var storage = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, side)
				.orElseGet(() -> blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, null).orElse(null));
		if (storage == null) {
			return 0;
		}
		FluidStack drained = storage.drain(new FluidStack(fluid, amount),
				simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
		return drained.isEmpty() ? 0 : drained.getAmount();
	}

	@Override
	public int fillFluid(Level level, BlockPos pos, Direction side, Fluid fluid, int amount, boolean simulate) {
		var blockEntity = level.getBlockEntity(pos);
		if (blockEntity == null || amount <= 0) {
			return 0;
		}

		var storage = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, side)
				.orElseGet(() -> blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, null).orElse(null));
		if (storage == null) {
			return 0;
		}
		return storage.fill(new FluidStack(fluid, amount),
				simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
	}
}
