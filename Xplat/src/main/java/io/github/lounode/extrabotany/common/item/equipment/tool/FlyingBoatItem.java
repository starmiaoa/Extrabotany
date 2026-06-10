package io.github.lounode.extrabotany.common.item.equipment.tool;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import io.github.lounode.extrabotany.common.entity.FlyingBoatEntity;
import io.github.lounode.extrabotany.common.item.ExtraBotanyItems;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class FlyingBoatItem extends Item {
	private static final Predicate<Entity> COLLISION_CHECK = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
	private final Variant variant;

	public FlyingBoatItem(Variant variant, Properties properties) {
		super(properties);
		this.variant = variant;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		HitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
		if (hit.getType() == HitResult.Type.MISS) {
			return InteractionResultHolder.pass(stack);
		}

		Vec3 look = player.getViewVector(1F);
		List<Entity> entities = level.getEntities(player,
				player.getBoundingBox().expandTowards(look.scale(5D)).inflate(1D),
				COLLISION_CHECK);
		if (!entities.isEmpty()) {
			Vec3 eye = player.getEyePosition(1F);
			for (Entity entity : entities) {
				AABB bounds = entity.getBoundingBox().inflate(entity.getPickRadius());
				if (bounds.contains(eye)) {
					return InteractionResultHolder.pass(stack);
				}
			}
		}

		if (hit.getType() != HitResult.Type.BLOCK) {
			return InteractionResultHolder.pass(stack);
		}

		FlyingBoatEntity boat = new FlyingBoatEntity(level, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, this.variant);
		boat.setYRot(player.getYRot());
		if (!level.noCollision(boat, boat.getBoundingBox().inflate(-0.1D))) {
			return InteractionResultHolder.fail(stack);
		}

		if (!level.isClientSide()) {
			level.addFreshEntity(boat);
			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}
		}
		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}

	public enum Variant implements StringRepresentable {
		MANASTEEL("manasteel", 32, 1D, 1D, () -> ExtraBotanyItems.flyingBoat),
		ELEMENTIUM("elementium", 64, 1.3D, 1.75D, () -> ExtraBotanyItems.elementiumFlyingBoat),
		TERRASTEEL("terrasteel", 255, 1.6D, 2.5D, () -> ExtraBotanyItems.terrasteelFlyingBoat);

		private final String name;
		private final int maxHeight;
		private final double forwardMultiplier;
		private final double backMultiplier;
		private final Supplier<Item> item;

		Variant(String name, int maxHeight, double forwardMultiplier, double backMultiplier, Supplier<Item> item) {
			this.name = name;
			this.maxHeight = maxHeight;
			this.forwardMultiplier = forwardMultiplier;
			this.backMultiplier = backMultiplier;
			this.item = item;
		}

		@Override
		public String getSerializedName() {
			return name;
		}

		public int maxHeight() {
			return maxHeight;
		}

		public double forwardMultiplier() {
			return forwardMultiplier;
		}

		public double backMultiplier() {
			return backMultiplier;
		}

		public Item item() {
			return item.get();
		}

		public static Variant byId(int id) {
			Variant[] values = values();
			if (id < 0 || id >= values.length) {
				return MANASTEEL;
			}
			return values[id];
		}

		public static Variant byName(String name) {
			for (Variant variant : values()) {
				if (variant.name.equals(name)) {
					return variant;
				}
			}
			return MANASTEEL;
		}
	}
}
