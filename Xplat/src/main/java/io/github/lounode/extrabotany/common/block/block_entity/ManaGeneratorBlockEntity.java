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

import vazkii.botania.api.block.Wandable;
import vazkii.botania.api.mana.ManaSpreader;
import vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity;

import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import io.github.lounode.extrabotany.xplat.ExtraBotanyConfig;

public class ManaGeneratorBlockEntity extends BlockEntity implements Wandable {
	public static final int DEFAULT_MAX_ENERGY = 40_000;
	public static final int DEFAULT_MANA_PER_CONVERSION = 99;
	public static final int DEFAULT_TRANSFER_SPEED = 200;
	public static final int ENERGY_PER_CONVERSION = 1_000;
	public static final int MAX_MANA = 1_000_000;
	public static final String TAG_MANA = "mana";
	public static final String TAG_ENERGY = "energy";

	private static final int SYNC_INTERVAL = 10;

	private int mana;
	private int energy;
	private int ticks;
	private boolean syncPending;

	public ManaGeneratorBlockEntity(BlockPos pos, BlockState state) {
		super(ExtraBotanyBlockEntities.MANA_GENERATOR, pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ManaGeneratorBlockEntity self) {
		if (!self.isEnabled()) {
			return;
		}

		boolean changed = false;
		for (Direction direction : Direction.values()) {
			changed |= self.pullEnergy(pos.relative(direction), direction.getOpposite());
			changed |= self.pushMana(pos.relative(direction));
		}

		changed |= self.convertEnergy();

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

	private boolean pullEnergy(BlockPos sourcePos, Direction sourceSide) {
		if (level == null || energy >= getMaxEnergy()) {
			return false;
		}

		int request = Math.min(ENERGY_PER_CONVERSION, getMaxEnergy() - energy);
		int extracted = EXplatAbstractions.INSTANCE.extractEnergy(level, sourcePos, sourceSide, request, false);
		if (extracted <= 0) {
			return false;
		}

		energy = Mth.clamp(energy + extracted, 0, getMaxEnergy());
		return true;
	}

	private boolean convertEnergy() {
		int manaPerConversion = getManaPerConversion();
		if (energy < ENERGY_PER_CONVERSION || mana >= MAX_MANA || manaPerConversion <= 0) {
			return false;
		}

		int conversions = Math.min(energy / ENERGY_PER_CONVERSION, (MAX_MANA - mana) / manaPerConversion);
		if (conversions <= 0) {
			return false;
		}

		int manaToAdd = conversions * manaPerConversion;
		int energyToSpend = conversions * ENERGY_PER_CONVERSION;
		if (manaToAdd <= 0 || energyToSpend <= 0) {
			return false;
		}

		energy -= energyToSpend;
		mana += manaToAdd;
		return true;
	}

	private boolean pushMana(BlockPos targetPos) {
		if (level == null || mana <= 0 || !(level.getBlockEntity(targetPos) instanceof ManaSpreaderBlockEntity spreader)) {
			return false;
		}

		int space = Math.max(0, spreader.getMaxMana() - spreader.getCurrentMana());
		int transfer = Math.min(getTransferSpeed(), mana);
		transfer = Math.min(transfer, space);
		if (transfer <= 0) {
			return false;
		}

		((ManaSpreader) spreader).receiveMana(transfer);
		mana -= transfer;
		return true;
	}

	private boolean isEnabled() {
		var config = ExtraBotanyConfig.common();
		return config == null || config.enableManaGenerator();
	}

	public int getCurrentMana() {
		return Mth.clamp(mana, 0, MAX_MANA);
	}

	public int getEnergyStored() {
		return Mth.clamp(energy, 0, getMaxEnergy());
	}

	public int getMaxEnergy() {
		var config = ExtraBotanyConfig.common();
		return config == null ? DEFAULT_MAX_ENERGY : config.manaGeneratorMaxEnergy();
	}

	public int getManaPerConversion() {
		var config = ExtraBotanyConfig.common();
		return config == null ? DEFAULT_MANA_PER_CONVERSION : config.manaGeneratorConvertMana();
	}

	public int getTransferSpeed() {
		var config = ExtraBotanyConfig.common();
		return config == null ? DEFAULT_TRANSFER_SPEED : config.manaGeneratorTransferSpeed();
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
		mana = Mth.clamp(tag.getInt(TAG_MANA), 0, MAX_MANA);
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
