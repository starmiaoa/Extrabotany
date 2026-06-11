package io.github.lounode.extrabotany.common.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.SavedData;

import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;
import io.github.lounode.extrabotany.common.lib.LibMisc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LimitedEditionSupplyBagData extends SavedData {
	private static final String DATA_NAME = LibMisc.MOD_ID + "_limited_edition_supply_bag";
	private static final String TAG_POOLS = "Pools";
	private static final String TAG_UUID = "UUID";
	private static final String TAG_POOL = "Pool";
	private static final int EMPTY = -1;
	private static final int[] TEMPLATE = createTemplate();

	private final Map<UUID, int[]> pools = new HashMap<>();

	public static LimitedEditionSupplyBagData get(ServerLevel level) {
		return level.getServer().overworld().getDataStorage()
				.computeIfAbsent(LimitedEditionSupplyBagData::load, LimitedEditionSupplyBagData::new, DATA_NAME);
	}

	public ItemStack draw(UUID playerId, RandomSource random) {
		int[] poolBefore = pools.get(playerId);
		if (poolBefore == null) {
			pools.put(playerId, shuffledPool(random));
			setDirty();
			return new ItemStack(ExtraBotanyItems.emptyCoreOfTheVoid);
		}
		int[] pool = poolBefore;
		int start = random.nextInt(pool.length);

		for (int i = 0; i < pool.length; i++) {
			int index = (start + i) % pool.length;
			int entry = pool[index];
			if (entry != EMPTY) {
				pool[index] = EMPTY;
				setDirty();
				return stackFor(entry);
			}
		}

		pool = shuffledPool(random);
		pools.put(playerId, pool);
		int entry = pool[0];
		pool[0] = EMPTY;
		setDirty();
		return stackFor(entry);
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		ListTag list = new ListTag();
		for (var entry : pools.entrySet()) {
			CompoundTag poolTag = new CompoundTag();
			poolTag.putUUID(TAG_UUID, entry.getKey());
			poolTag.put(TAG_POOL, new IntArrayTag(entry.getValue()));
			list.add(poolTag);
		}
		tag.put(TAG_POOLS, list);
		return tag;
	}

	private static LimitedEditionSupplyBagData load(CompoundTag tag) {
		LimitedEditionSupplyBagData data = new LimitedEditionSupplyBagData();
		ListTag list = tag.getList(TAG_POOLS, Tag.TAG_COMPOUND);
		for (Tag value : list) {
			if (value instanceof CompoundTag poolTag && poolTag.hasUUID(TAG_UUID)) {
				int[] pool = poolTag.getIntArray(TAG_POOL);
				if (pool.length == TEMPLATE.length) {
					data.pools.put(poolTag.getUUID(TAG_UUID), pool);
				}
			}
		}
		return data;
	}

	private static int[] shuffledPool(RandomSource random) {
		int[] pool = Arrays.copyOf(TEMPLATE, TEMPLATE.length);
		for (int i = pool.length - 1; i > 0; i--) {
			int j = random.nextInt(i + 1);
			int value = pool[i];
			pool[i] = pool[j];
			pool[j] = value;
		}
		return pool;
	}

	private static int[] createTemplate() {
		int[] pool = new int[100];
		int index = 0;
		index = fill(pool, index, 1, 8);
		index = fill(pool, index, 4, 1);
		index = fill(pool, index, 10, 2);
		index = fill(pool, index, 10, 3);
		index = fill(pool, index, 20, 4);
		index = fill(pool, index, 30, 5);
		index = fill(pool, index, 10, 6);
		fill(pool, index, 15, 7);
		return pool;
	}

	private static int fill(int[] pool, int index, int count, int value) {
		for (int i = 0; i < count; i++) {
			pool[index++] = value;
		}
		return index;
	}

	private static ItemStack stackFor(int entry) {
		return switch (entry) {
			case 0 -> new ItemStack(ExtraBotanyItems.silentEternity);
			case 1 -> new ItemStack(ExtraBotanyItems.lensSuperconductor);
			case 2 -> new ItemStack(ExtraBotanyItems.heroMedal);
			case 3 -> new ItemStack(ExtraBotanyItems.vierRewardBag, 4);
			case 4 -> new ItemStack(ExtraBotanyItems.dreiRewardBag, 4);
			case 5 -> new ItemStack(ExtraBotanyItems.zweiRewardBag, 6);
			case 6 -> new ItemStack(ExtraBotanyItems.nineAndThreeQuartersRewardBag, 3);
			case 7 -> new ItemStack(Items.DIAMOND, 4);
			case 8 -> new ItemStack(ExtraBotanyItems.emptyCoreOfTheVoid);
			default -> ItemStack.EMPTY;
		};
	}
}
