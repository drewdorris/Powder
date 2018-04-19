package com.ruinscraft.powder.models.trackers;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class EntityTracker implements Tracker {

	private Entity entity;

	public EntityTracker(Entity entity) {
		this.entity = entity;
	}

	@Override
	public Location getCurrentLocation() {
		if (entity instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity) entity;
			return livingEntity.getEyeLocation();
		} else {
			return entity.getLocation();
		}
	}

}
