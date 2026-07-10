package io.github.lounode.extrabotany.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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

	private static final int SYNC_INTERVAL = 10;

	private int mana;
	private int energy;
	private int ticks;
	private boolean syncPending;

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
			self.syncPending = true;
		}

		if (self.syncPending && self.ticks % SYNC_INTERVAL == 0) {
			level.sendBlockUpdated(pos, state, state, 3);
			self.syncPending = false;
		}
		self.ticks++;
	}

	private boolean pullFluid(BlockPos sourcePos, Direction sourceSide) {
		int energyPerOperation = getStorageDrain();
		int fluidPerOperation = getStorageDrainContainer();
		int space = getMaxEnergy() - energy;
		if (level == null || space <= 0 || energyPerOperation <= 0 || fluidPerOperation <= 0) {
			return false;
		}
		int divisor = gcd(energyPerOperation, fluidPerOperation);
		int energyUnit = energyPerOperation / divisor;
		int fluidUnit = fluidPerOperation / divisor;
		int units = Math.min(divisor, space / energyUnit);
		if (units <= 0) {
			return false;
		}
		int request = units * fluidUnit;
		int available = EXplatAbstractions.INSTANCE.drainFluid(level, sourcePos, sourceSide, fluidedMana(), request, true);
		units = Math.min(units, available / fluidUnit);
		if (units <= 0) {
			return false;
		}
		int fluidToDrain = units * fluidUnit;
		int drained = EXplatAbstractions.INSTANCE.drainFluid(level, sourcePos, sourceSide, fluidedMana(),
				fluidToDrain, false);
		int committedUnits = Math.min(units, drained / fluidUnit);
		if (committedUnits <= 0) {
			return false;
		}
		energy += committedUnits * energyUnit;
		return true;
	}

	private boolean pushFluid(BlockPos targetPos, Direction targetSide) {
		int energyPerOperation = getStoragePump();
		int fluidPerOperation = getStoragePumpContainer();
		if (level == null || energy <= 0 || energyPerOperation <= 0 || fluidPerOperation <= 0) {
			return false;
		}
		int divisor = gcd(energyPerOperation, fluidPerOperation);
		int energyUnit = energyPerOperation / divisor;
		int fluidUnit = fluidPerOperation / divisor;
		int units = Math.min(divisor, energy / energyUnit);
		if (units <= 0) {
			return false;
		}
		int request = units * fluidUnit;
		int accepted = EXplatAbstractions.INSTANCE.fillFluid(level, targetPos, targetSide, fluidedMana(), request, true);
		units = Math.min(units, accepted / fluidUnit);
		if (units <= 0) {
			return false;
		}
		int filled = EXplatAbstractions.INSTANCE.fillFluid(level, targetPos, targetSide, fluidedMana(), units * fluidUnit, false);
		int committedUnits = Math.min(units, filled / fluidUnit);
		if (committedUnits <= 0) {
			return false;
		}
		energy -= committedUnits * energyUnit;
		return true;
	}

	private boolean convertFluidEnergyToMana() {
		int manaPerOperation = getManaReceive();
		int energyPerOperation = getEnergyLoss();
		int manaSpace = getMaxMana() - getCurrentMana();
		if (energy <= 0 || manaSpace <= 0 || manaPerOperation <= 0 || energyPerOperation <= 0) {
			return false;
		}
		int divisor = gcd(energyPerOperation, manaPerOperation);
		int energyUnit = energyPerOperation / divisor;
		int manaUnit = manaPerOperation / divisor;
		int units = Math.min(divisor, Math.min(energy / energyUnit, manaSpace / manaUnit));
		if (units <= 0) {
			return false;
		}
		receiveMana(units * manaUnit);
		energy -= units * energyUnit;
		return true;
	}

	private boolean convertManaToFluidEnergy() {
		int manaPerOperation = getManaGive();
		int energyPerOperation = getEnergyGain();
		int energySpace = getMaxEnergy() - energy;
		if (getCurrentMana() <= 0 || energySpace <= 0 || manaPerOperation <= 0 || energyPerOperation <= 0) {
			return false;
		}
		int divisor = gcd(manaPerOperation, energyPerOperation);
		int manaUnit = manaPerOperation / divisor;
		int energyUnit = energyPerOperation / divisor;
		int units = Math.min(divisor, Math.min(getCurrentMana() / manaUnit, energySpace / energyUnit));
		if (units <= 0) {
			return false;
		}
		receiveMana(-units * manaUnit);
		energy += units * energyUnit;
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

	private static int gcd(int first, int second) {
		while (second != 0) {
			int remainder = first % second;
			first = second;
			second = remainder;
		}
		return first;
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
			// Sparks and bursts call this outside serverTick, so the sync flag must live here.
			syncPending = true;
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
			syncPending = false;
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

	// Without this, sendBlockUpdated never carries the block entity data to the client.
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
