package io.github.lounode.extrabotany.forge.xplat;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.network.PacketDistributor;

import vazkii.botania.neoforge.xplat.NeoForgeXplatImpl;

import io.github.lounode.extrabotany.api.ExtrabotanyForgeCapabilities;
import io.github.lounode.extrabotany.common.block.block_entity.LivingrockBarrelBlockEntity;
import io.github.lounode.extrabotany.api.item.NatureEnergyItem;
import io.github.lounode.extrabotany.common.lib.LibMisc;
import io.github.lounode.extrabotany.network.ExtrabotanyPacket;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ExForgeXplatImpl extends NeoForgeXplatImpl implements EXplatAbstractions {
	@Override
	public void sendToPlayer(ServerPlayer player, ExtrabotanyPacket packet) {
		PacketDistributor.sendToPlayer(player, packet);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Packet<ClientGamePacketListener> toVanillaClientboundPacket(ExtrabotanyPacket packet) {
		return (Packet<ClientGamePacketListener>) (Packet) packet.toVanillaClientbound();
	}

	@Nullable
	@Override
	public NatureEnergyItem findNatureEnergyItem(ItemStack stack) {
		return stack.getCapability(ExtrabotanyForgeCapabilities.NATURE_ENERGY_ITEM);
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
	public List<ItemStack> getEquippedCurios(Player player) {
		List<ItemStack> stacks = new ArrayList<>();
		top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
			handler.getCurios().values().forEach(slot -> {
				var stackHandler = slot.getStacks();
				for (int i = 0; i < stackHandler.getSlots(); i++) {
					stacks.add(stackHandler.getStackInSlot(i));
				}
			});
		});
		return stacks;
	}

	@Override
	public int getFluidTemperature(Fluid fluid) {
		return fluid.getFluidType().getTemperature();
	}

	@Override
	public float getEnchantPowerBonus(ServerLevel level, BlockPos pos) {
		float j = 0;
		for (BlockPos blockpos : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
			if (EnchantingTableBlock.isValidBookShelf(level, pos, blockpos)) {
				j += level.getBlockState(pos.offset(blockpos)).getEnchantPowerBonus(level, pos.offset(blockpos));
			}
		}
		return j;
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
		var storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, side);
		if (storage == null) {
			storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
		}
		if (storage == null || !storage.canExtract()) {
			return 0;
		}
		return storage.extractEnergy(maxExtract, simulate);
	}

	@Override
	public int drainFluid(Level level, BlockPos pos, Direction side, Fluid fluid, int amount, boolean simulate) {
		var storage = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, side);
		if (storage == null) {
			storage = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
		}
		if (storage == null || amount <= 0) {
			return 0;
		}
		FluidStack drained = storage.drain(new FluidStack(fluid, amount),
				simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
		return drained.isEmpty() ? 0 : drained.getAmount();
	}

	@Override
	public int fillFluid(Level level, BlockPos pos, Direction side, Fluid fluid, int amount, boolean simulate) {
		var storage = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, side);
		if (storage == null) {
			storage = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
		}
		if (storage == null || amount <= 0) {
			return 0;
		}
		return storage.fill(new FluidStack(fluid, amount),
				simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
	}
}
