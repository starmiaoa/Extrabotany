package io.github.lounode.extrabotany.common.item.equipment.bauble;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import io.github.lounode.extrabotany.common.entity.MotorEntity;
import io.github.lounode.extrabotany.common.entity.UfoEntity;

public class MountAccessoryItem extends SimpleBaubleItem {
	private final Variant variant;

	public MountAccessoryItem(Variant variant, Properties props) {
		super(props);
		this.variant = variant;
	}

	public Entity createMount(Level level) {
		return switch (variant) {
			case MOTOR -> {
				MotorEntity motor = new MotorEntity(level, 0D, 0D, 0D);
				motor.setAccessoryMount(true);
				yield motor;
			}
			case COSMIC_CAR_KEY -> {
				UfoEntity ufo = new UfoEntity(level, 0D, 0D, 0D);
				ufo.setAccessoryMount(true);
				yield ufo;
			}
		};
	}

	public enum Variant {
		MOTOR,
		COSMIC_CAR_KEY
	}
}
