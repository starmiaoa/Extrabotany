package io.github.lounode.extrabotany.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.LinkedHashMap;
import java.util.Map;

public class CocoonOfDesireBlockEntity extends BlockEntity {
	public static final int HATCH_TIME = 1200;
	private static final String TAG_ITEM = "Item";
	private static final String TAG_TIME_PASSED = "timePassed";
	private static final String TAG_ROTATION = "Rot";

	private static final Map<Item, EntityType<? extends LivingEntity>> HATCHES = createHatches();

	private ItemStack item = ItemStack.EMPTY;
	private int timePassed;
	private int rotation;

	public CocoonOfDesireBlockEntity(BlockPos pos, BlockState state) {
		super(ExtraBotanyBlockEntities.COCOON_OF_DESIRE, pos, state);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, CocoonOfDesireBlockEntity self) {
		if (self.item.isEmpty()) {
			self.absorbNearbyItem();
			return;
		}

		self.timePassed++;
		self.rotation = (self.rotation + 1) % 360;
		if (self.timePassed >= HATCH_TIME) {
			self.hatch();
		}
	}

	public ItemStack getItem() {
		return item;
	}

	public void setItem(ItemStack stack) {
		item = stack.copy();
		item.setCount(1);
		timePassed = 0;
		markUpdated();
	}

	public void clearItem() {
		item = ItemStack.EMPTY;
		timePassed = 0;
		markUpdated();
	}

	public int getRotation() {
		return rotation;
	}

	private void absorbNearbyItem() {
		if (level == null) {
			return;
		}

		AABB box = new AABB(worldPosition).inflate(1.0D);
		for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, box, itemEntity -> itemEntity.isAlive() && !itemEntity.getItem().isEmpty())) {
			ItemStack stack = entity.getItem();
			if (!HATCHES.containsKey(stack.getItem())) {
				continue;
			}

			ItemStack stored = stack.copy();
			stored.setCount(1);
			setItem(stored);
			stack.shrink(1);
			if (stack.isEmpty()) {
				entity.discard();
			}
			level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F,
					(level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
			break;
		}
	}

	private void hatch() {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}

		EntityType<? extends LivingEntity> entityType = HATCHES.get(item.getItem());
		if (entityType == null) {
			timePassed = 0;
			markUpdated();
			return;
		}

		LivingEntity entity = entityType.create(serverLevel);
		if (entity == null) {
			timePassed = 0;
			markUpdated();
			return;
		}

		entity.moveTo(worldPosition.getX() + 0.5D, worldPosition.getY() + 1.0D, worldPosition.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
		if (entity instanceof Animal animal) {
			animal.setAge(-24000);
		}
		if (entity instanceof Mob mob) {
			mob.finalizeSpawn((ServerLevelAccessor) serverLevel, serverLevel.getCurrentDifficultyAt(worldPosition),
					MobSpawnType.MOB_SUMMONED, (SpawnGroupData) null, null);
		}
		if (serverLevel.addFreshEntity(entity)) {
			serverLevel.levelEvent(2004, worldPosition, 0);
			clearItem();
		} else {
			timePassed = 0;
			markUpdated();
		}
	}

	private void markUpdated() {
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putInt(TAG_TIME_PASSED, timePassed);
		tag.putInt(TAG_ROTATION, rotation);
		if (!item.isEmpty()) {
			tag.put(TAG_ITEM, item.save(new CompoundTag()));
		}
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		timePassed = Mth.clamp(tag.getInt(TAG_TIME_PASSED), 0, HATCH_TIME);
		rotation = Mth.clamp(tag.getInt(TAG_ROTATION), 0, 359);
		item = tag.contains(TAG_ITEM) ? ItemStack.of(tag.getCompound(TAG_ITEM)) : ItemStack.EMPTY;
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		saveAdditional(tag);
		return tag;
	}

	// Without this, sendBlockUpdated never carries the block entity data to the client.
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	private static Map<Item, EntityType<? extends LivingEntity>> createHatches() {
		Map<Item, EntityType<? extends LivingEntity>> hatches = new LinkedHashMap<>();
		hatches.put(Items.CHORUS_FRUIT, EntityType.SHULKER);
		hatches.put(Blocks.COAL_BLOCK.asItem(), EntityType.WITHER_SKELETON);
		hatches.put(Blocks.WHITE_WOOL.asItem(), EntityType.SHEEP);
		hatches.put(Items.LEATHER, EntityType.COW);
		hatches.put(Items.BONE, EntityType.SKELETON);
		hatches.put(Items.ROTTEN_FLESH, EntityType.ZOMBIE);
		hatches.put(Items.WHEAT_SEEDS, EntityType.CHICKEN);
		hatches.put(Items.FEATHER, EntityType.CHICKEN);
		hatches.put(Items.BEETROOT_SEEDS, EntityType.CHICKEN);
		hatches.put(Items.MELON_SEEDS, EntityType.CHICKEN);
		hatches.put(Items.PUMPKIN_SEEDS, EntityType.CHICKEN);
		hatches.put(Items.WHEAT, EntityType.PIG);
		hatches.put(Items.ENDER_PEARL, EntityType.ENDERMAN);
		hatches.put(Items.GUNPOWDER, EntityType.CREEPER);
		hatches.put(Items.GOLD_INGOT, EntityType.ZOMBIFIED_PIGLIN);
		hatches.put(Items.BLAZE_ROD, EntityType.BLAZE);
		hatches.put(Items.GHAST_TEAR, EntityType.GHAST);
		hatches.put(Blocks.EMERALD_BLOCK.asItem(), EntityType.VILLAGER);
		return hatches;
	}
}
