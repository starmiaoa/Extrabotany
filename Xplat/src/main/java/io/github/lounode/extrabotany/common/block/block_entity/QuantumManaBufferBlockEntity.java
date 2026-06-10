package io.github.lounode.extrabotany.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class QuantumManaBufferBlockEntity extends ManaBufferBlockEntity {
	public static final int MAX_MANA = 1_024_000_000;
	public static final int TRANSFER_SPEED = 5_000;

	public QuantumManaBufferBlockEntity(BlockPos pos, BlockState state) {
		super(ExtraBotanyBlockEntities.QUANTUM_MANA_BUFFER, pos, state);
	}

	@Override
	public int getMaxMana() {
		return MAX_MANA;
	}

	@Override
	protected int getTransferSpeed() {
		return TRANSFER_SPEED;
	}
}
