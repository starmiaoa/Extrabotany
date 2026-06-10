package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

class TorchPlacementHitResult extends BlockHitResult {
	TorchPlacementHitResult(Vec3 location, Direction direction, BlockPos blockPos, boolean inside) {
		super(location, direction, blockPos, inside);
	}
}
