package io.github.lounode.extrabotany.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import vazkii.botania.api.block.PetalApothecary;

public class LivingrockBarrelBlockEntity extends BlockEntity {
	public static final int CAPACITY = 16_000;
	public static final int APOTHECARY_WATER_COST = 1_000;
	private static final String TAG_FLUID = "fluid";
	private static final String TAG_AMOUNT = "amount";

	private Fluid fluid = Fluids.EMPTY;
	private int fluidAmount;

	public LivingrockBarrelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public LivingrockBarrelBlockEntity(BlockPos pos, BlockState state) {
		this(ExtraBotanyBlockEntities.LIVINGROCK_BARREL, pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, LivingrockBarrelBlockEntity self) {
		if (!self.hasFluid(Fluids.WATER, APOTHECARY_WATER_COST)) {
			return;
		}

		for (Direction direction : Direction.values()) {
			if (level.getBlockEntity(pos.relative(direction)) instanceof PetalApothecary apothecary
					&& apothecary.getFluid() == PetalApothecary.State.EMPTY) {
				apothecary.setFluid(PetalApothecary.State.WATER);
				self.drainFluid(APOTHECARY_WATER_COST);
				return;
			}
		}
	}

	public Fluid getStoredFluid() {
		return fluid;
	}

	public int getStoredFluidAmount() {
		return fluidAmount;
	}

	public int getFluidCapacity() {
		return CAPACITY;
	}

	public boolean canFill(Fluid fluid) {
		return fluid != Fluids.EMPTY && (isEmpty() || this.fluid == fluid) && fluidAmount < CAPACITY;
	}

	public int fillFluid(Fluid fluid, int amount) {
		if (!canFill(fluid) || amount <= 0) {
			return 0;
		}
		int accepted = Math.min(amount, CAPACITY - fluidAmount);
		this.fluid = fluid;
		this.fluidAmount += accepted;
		notifyChanged();
		return accepted;
	}

	public int drainFluid(int amount) {
		if (isEmpty() || amount <= 0) {
			return 0;
		}
		int drained = Math.min(amount, fluidAmount);
		fluidAmount -= drained;
		if (fluidAmount <= 0) {
			fluid = Fluids.EMPTY;
			fluidAmount = 0;
		}
		notifyChanged();
		return drained;
	}

	public boolean hasFluid(Fluid fluid, int amount) {
		return this.fluid == fluid && fluidAmount >= amount;
	}

	public boolean isEmpty() {
		return fluid == Fluids.EMPTY || fluidAmount <= 0;
	}

	private void notifyChanged() {
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		if (!isEmpty()) {
			tag.putString(TAG_FLUID, BuiltInRegistries.FLUID.getKey(fluid).toString());
			tag.putInt(TAG_AMOUNT, fluidAmount);
		}
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		ResourceLocation fluidId = ResourceLocation.tryParse(tag.getString(TAG_FLUID));
		fluid = fluidId == null ? Fluids.EMPTY : BuiltInRegistries.FLUID.getOptional(fluidId).orElse(Fluids.EMPTY);
		fluidAmount = Math.max(0, Math.min(CAPACITY, tag.getInt(TAG_AMOUNT)));
		if (fluid == Fluids.EMPTY || fluidAmount == 0) {
			fluid = Fluids.EMPTY;
			fluidAmount = 0;
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		saveAdditional(tag, registries);
		return tag;
	}
}
