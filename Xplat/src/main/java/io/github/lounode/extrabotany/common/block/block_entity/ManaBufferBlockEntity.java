package io.github.lounode.extrabotany.common.block.block_entity;

import com.google.common.base.Predicates;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.block.Wandable;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSpark;
import vazkii.botania.api.mana.spark.SparkAttachable;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;

public class ManaBufferBlockEntity extends BlockEntity implements ManaReceiver, SparkAttachable, Wandable {
	public static final int MAX_MANA = 64_000_000;
	public static final int TRANSFER_SPEED = 1_000;
	public static final String TAG_MANA = "mana";

	private static final Direction[] INPUT_DIRECTIONS = {
			Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN
	};

	private int mana;

	public ManaBufferBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public ManaBufferBlockEntity(BlockPos pos, BlockState state) {
		this(ExtraBotanyBlockEntities.MANA_BUFFER, pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, ManaBufferBlockEntity self) {
		boolean changed = false;
		for (Direction direction : INPUT_DIRECTIONS) {
			changed |= self.pullFrom(pos.relative(direction));
			if (self.isFull()) {
				break;
			}
		}

		changed |= self.pushTo(pos.above());

		if (changed) {
			self.setChanged();
			level.sendBlockUpdated(pos, state, state, 3);
		}
	}

	private boolean pullFrom(BlockPos sourcePos) {
		ManaReceiver source = getManaEndpoint(sourcePos);
		if (source == null || source == this || source.getCurrentMana() <= 0 || isFull()) {
			return false;
		}

		int transfer = Math.min(getTransferSpeed(), source.getCurrentMana());
		transfer = Math.min(transfer, getAvailableSpaceForMana());
		if (transfer <= 0) {
			return false;
		}

		source.receiveMana(-transfer);
		receiveMana(transfer);
		return true;
	}

	private boolean pushTo(BlockPos targetPos) {
		ManaReceiver target = getManaEndpoint(targetPos);
		if (target == null || target == this || getCurrentMana() <= 0) {
			return false;
		}

		int space = getAvailableSpace(target);
		int transfer = Math.min(getTransferSpeed(), getCurrentMana());
		transfer = Math.min(transfer, space);
		if (transfer <= 0) {
			return false;
		}

		target.receiveMana(transfer);
		receiveMana(-transfer);
		return true;
	}

	@Nullable
	private ManaReceiver getManaEndpoint(BlockPos pos) {
		if (level == null) {
			return null;
		}

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof ManaPoolBlockEntity pool) {
			return pool;
		}
		if (blockEntity instanceof ManaBufferBlockEntity buffer) {
			return buffer;
		}
		return null;
	}

	private int getAvailableSpace(ManaReceiver receiver) {
		if (receiver instanceof ManaPool pool) {
			return Math.max(0, pool.getMaxMana() - pool.getCurrentMana());
		}
		if (receiver instanceof ManaBufferBlockEntity buffer) {
			return buffer.getAvailableSpaceForMana();
		}
		return 0;
	}

	public int getMaxMana() {
		return MAX_MANA;
	}

	protected int getTransferSpeed() {
		return TRANSFER_SPEED;
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
	public ManaSpark getAttachedSpark() {
		if (level == null) {
			return null;
		}

		var sparks = level.getEntitiesOfClass(Entity.class, new AABB(worldPosition.above(), worldPosition.above().offset(1, 1, 1)), Predicates.instanceOf(ManaSpark.class));
		return sparks.size() == 1 ? (ManaSpark) sparks.get(0) : null;
	}

	@Override
	public boolean areIncomingTranfersDone() {
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
	public boolean onUsedByWand(@Nullable Player player, ItemStack stack, Direction direction) {
		if (level != null && !level.isClientSide()) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
		return true;
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putInt(TAG_MANA, getCurrentMana());
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		mana = Mth.clamp(tag.getInt(TAG_MANA), 0, getMaxMana());
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		tag.putInt(TAG_MANA, getCurrentMana());
		return tag;
	}
}
