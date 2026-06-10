package io.github.lounode.extrabotany.forge.xplat;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.lounode.extrabotany.common.block.block_entity.LivingrockBarrelBlockEntity;

public class LivingrockBarrelFluidProvider implements ICapabilityProvider {
	private final LazyOptional<LivingrockBarrelFluidHandler> handler;

	public LivingrockBarrelFluidProvider(LivingrockBarrelBlockEntity barrel) {
		this.handler = LazyOptional.of(() -> new LivingrockBarrelFluidHandler(barrel));
	}

	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (cap == ForgeCapabilities.FLUID_HANDLER) {
			return handler.cast();
		}
		return LazyOptional.empty();
	}
}
