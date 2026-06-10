package io.github.lounode.extrabotany.forge.xplat;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import io.github.lounode.extrabotany.common.block.block_entity.LivingrockBarrelBlockEntity;

public class LivingrockBarrelFluidHandler implements IFluidHandler {
	private final LivingrockBarrelBlockEntity barrel;

	public LivingrockBarrelFluidHandler(LivingrockBarrelBlockEntity barrel) {
		this.barrel = barrel;
	}

	@Override
	public int getTanks() {
		return 1;
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		return barrel.isEmpty()
				? FluidStack.EMPTY
				: new FluidStack(barrel.getStoredFluid(), barrel.getStoredFluidAmount());
	}

	@Override
	public int getTankCapacity(int tank) {
		return barrel.getFluidCapacity();
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return !stack.isEmpty() && barrel.canFill(stack.getFluid());
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		if (resource.isEmpty() || !barrel.canFill(resource.getFluid())) {
			return 0;
		}
		int accepted = Math.min(resource.getAmount(), barrel.getFluidCapacity() - barrel.getStoredFluidAmount());
		if (action.execute() && accepted > 0) {
			barrel.fillFluid(resource.getFluid(), accepted);
		}
		return accepted;
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		if (resource.isEmpty() || !resource.is(barrel.getStoredFluid())) {
			return FluidStack.EMPTY;
		}
		return drain(resource.getAmount(), action);
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		if (barrel.isEmpty() || maxDrain <= 0) {
			return FluidStack.EMPTY;
		}
		int drained = Math.min(maxDrain, barrel.getStoredFluidAmount());
		FluidStack stack = new FluidStack(barrel.getStoredFluid(), drained);
		if (action.execute()) {
			barrel.drainFluid(drained);
		}
		return stack;
	}
}
