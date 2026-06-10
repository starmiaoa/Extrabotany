package io.github.lounode.extrabotany.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import vazkii.botania.api.block.Wandable;
import vazkii.botania.api.mana.ManaSpreader;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.SparkAttachable;
import vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity;

import io.github.lounode.extrabotany.common.fluid.ExtraBotanyFluids;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

public class ManaLiquefactionBlockEntity extends BlockEntity implements ManaReceiver, SparkAttachable, Wandable {
	public static final int DEFAULT_MAX_MANA = 1_000_000;
	public static final int DEFAULT_MAX_ENERGY = 16_000;
	public static final int DEFAULT_STORAGE_DRAIN = 1;
	public static final int DEFAULT_STORAGE_DRAIN_CONTAINER = 1;
	public static final int DEFAULT_STORAGE_PUMP = 25;
	public static final int DEFAULT_STORAGE_PUMP_CONTAINER = 25;
	public static final int DEFAULT_MANA_RECEIVE = 2_000;
	public static final int DEFAULT_ENERGY_LOSS = 2;
	public static final int DEFAULT_MANA_GIVE = 2_000;
	public static final int DEFAULT_ENERGY_GAIN = 2;
	public static final String TAG_MANA = "mana";
	public static final String TAG_ENERGY = "energy";

	private int mana;
	private int energy;

	public ManaLiquefactionBlockEntity(BlockPos pos, BlockState state) {
		super(ExtraBotanyBlockEntities.MANA_LIQUEFACTION, pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ManaLiquefactionBlockEntity self) {
		if (!self.isEnabled()) {
			return;
		}

		boolean powered = level.hasNeighborSignal(pos);
		boolean changed = false;

		for (Direction direction : Direction.values()) {
			BlockPos neighbor = pos.relative(direction);
			Direction neighborSide = direction.getOpposite();
			changed |= powered
					? self.pushFluid(neighbor, neighborSide)
					: self.pullFluid(neighbor, neighborSide);
			if (!powered) {
				changed |= self.pushMana(neighbor);
			}
		}

		changed |= powered ? self.convertManaToFluidEnergy() : self.convertFluidEnergyToMana();

		if (changed) {
			self.setChanged();
			level.sendBlockUpdated(pos, state, state, 3);
		}
	}

	private boolean pullFluid(BlockPos sourcePos, Direction sourceSide) {
		if (level == null || isFull()) {
			return false;
		}
		int drained = EXplatAbstractions.INSTANCE.drainFluid(level, sourcePos, sourceSide, fluidedMana(),
				getStorageDrainContainer(), false);
		if (drained <= 0) {
			return false;
		}
		energy = Mth.clamp(energy + getStorageDrain(), 0, getMaxEnergy());
		return true;
	}

	private boolean pushFluid(BlockPos targetPos, Direction targetSide) {
		if (level == null || energy < getStoragePump()) {
			return false;
		}
		int filled = EXplatAbstractions.INSTANCE.fillFluid(level, targetPos, targetSide, fluidedMana(),
				getStoragePumpContainer(), false);
		if (filled <= 0) {
			return false;
		}
		energy = Math.max(0, energy - getStoragePump());
		return true;
	}

	private boolean convertFluidEnergyToMana() {
		if (energy <= 0 || getCurrentMana() > getMaxMana() - getManaReceive()) {
			return false;
		}
		receiveMana(getManaReceive());
		energy = Mth.clamp(energy - getEnergyLoss(), 0, getMaxEnergy());
		return true;
	}

	private boolean convertManaToFluidEnergy() {
		if (getCurrentMana() < getManaGive()) {
			return false;
		}
		receiveMana(-getManaGive());
		energy = Mth.clamp(energy + getEnergyGain(), 0, getMaxEnergy());
		return true;
	}

	private boolean pushMana(BlockPos targetPos) {
		if (level == null || getCurrentMana() <= 0 || !(level.getBlockEntity(targetPos) instanceof ManaSpreaderBlockEntity spreader)) {
			return false;
		}

		int speed = getTransferSpeed();
		int space = Math.max(0, spreader.getMaxMana() - spreader.getCurrentMana());
		int transfer = Math.min(speed, getCurrentMana());
		transfer = Math.min(transfer, space);
		if (transfer <= 0) {
			return false;
		}

		((ManaSpreader) spreader).receiveMana(transfer);
		receiveMana(-transfer);
		return true;
	}

	private Fluid fluidedMana() {
		return ExtraBotanyFluids.fluidedMana();
	}

	private boolean isEnabled() {
		var config = ExtraBotanyConfig.common();
		return config == null || config.enableManaLiquefaction();
	}

	private int getMaxEnergy() {
		return DEFAULT_MAX_ENERGY;
	}

	public int getEnergyStored() {
		return Mth.clamp(energy, 0, getMaxEnergy());
	}

	public int getMaxMana() {
		var config = ExtraBotanyConfig.common();
		return config == null ? DEFAULT_MAX_MANA : config.manaLiquefactionMaxMana();
	}

	private int getStorageDrain() {
		var config = ExtraBotanyConfig.common();
		return config == null ? DEFAULT_STORAGE_DRAIN : config.manaLiquefactionStorageDrain();
	}

	private int getStorageDrainContainer() {
		var config = ExtraBotanyConfig.common();
		return config == null ? DEFAULT_STORAGE_DRAIN_CONTAINER : config.manaLiquefactionStorageDrainContainer();
	}

	private int getStoragePump() {
		var config = ExtraBotanyConfig.common();
		return config == null ? DEFAULT_STORAGE_PUMP : config.manaLiquefactionStoragePump();
	}

	private int getStoragePumpContainer() {
		var config = ExtraBotanyConfig.common();
		return config == null ? DEFAULT_STORAGE_PUMP_CONTAINER : config.manaLiquefactionStoragePumpContainer();
	}

	private int getManaReceive() {
		var config = ExtraBotanyConfig.common();
		return config == null ? DEFAULT_MANA_RECEIVE : config.manaLiquefactionManaReceive();
	}

	private int getEnergyLoss() {
		var config = ExtraBotanyConfig.common();
		return config == null ? DEFAULT_ENERGY_LOSS : config.manaLiquefactionEnergyLoss();
	}

	private int getManaGive() {
		var config = ExtraBotanyConfig.common();
		return config == null ? DEFAULT_MANA_GIVE : config.manaLiquefactionManaGive();
	}

	private int getEnergyGain() {
		var config = ExtraBotanyConfig.common();
		return config == null ? DEFAULT_ENERGY_GAIN : config.manaLiquefactionEnergyGain();
	}

	private int getTransferSpeed() {
		var config = ExtraBotanyConfig.common();
		return config == null ? ManaGeneratorBlockEntity.DEFAULT_TRANSFER_SPEED : config.manaGeneratorTransferSpeed();
	}

	@Override
	public boolean canAttachSpark(ItemStack stack) {
		return true;
	}

	@Override
	public int getAvailableSpaceForMana() {
		return Math.max(0, getMaxMana() - getCurrentMana());
	}

	@Override
	public boolean areIncomingTransfersDone() {
		return false;
	}

	@Override
	public Level getManaReceiverLevel() {
		return getLevel();
	}

	@Override
	public BlockPos getManaReceiverPos() {
		return getBlockPos();
	}

	@Override
	public int getCurrentMana() {
		return Mth.clamp(mana, 0, getMaxMana());
	}

	@Override
	public boolean isFull() {
		return getCurrentMana() >= getMaxMana();
	}

	@Override
	public void receiveMana(int amount) {
		int oldMana = mana;
		mana = Mth.clamp(getCurrentMana() + amount, 0, getMaxMana());
		if (oldMana != mana) {
			setChanged();
		}
	}

	@Override
	public boolean canReceiveManaFromBursts() {
		return true;
	}

	@Override
	public boolean onUsedByWand(net.minecraft.world.entity.player.Player player, ItemStack stack, Direction direction) {
		if (level != null && !level.isClientSide()) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
		return true;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putInt(TAG_MANA, getCurrentMana());
		tag.putInt(TAG_ENERGY, getEnergyStored());
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		mana = Mth.clamp(tag.getInt(TAG_MANA), 0, getMaxMana());
		energy = Mth.clamp(tag.getInt(TAG_ENERGY), 0, getMaxEnergy());
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = super.getUpdateTag(registries);
		tag.putInt(TAG_MANA, getCurrentMana());
		tag.putInt(TAG_ENERGY, getEnergyStored());
		return tag;
	}
}
