package io.github.lounode.extrabotany.common.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class LivingDeathEventWrapper {
	private final LivingEntity entity;
	private final DamageSource source;
	private boolean canceled;

	public LivingDeathEventWrapper(LivingEntity entity, DamageSource source) {
		this.entity = entity;
		this.source = source;
	}

	public LivingEntity getEntity() {
		return entity;
	}

	public DamageSource getSource() {
		return source;
	}

	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}
}
