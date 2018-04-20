package com.ruinscraft.powder.models.trackers;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class EntityTracker implements Tracker {

	private UUID entityId;

	public EntityTracker(Entity entity) {
		this.entityId = entity.getUniqueId();
	}
	
	public EntityTracker(UUID entityId) {
		this.entityId = entityId;
	}

	public Entity getEntity() {
		return Bukkit.getEntity(entityId);
	}
	
	public UUID getEntityUUID() {
		return entityId;
	}

	@Override
	public TrackerType getType() {
		return TrackerType.ENTITY;
	}

	@Override
	public Location getCurrentLocation() {
		Entity entity = Bukkit.getEntity(entityId);
		if (entity instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity) entity;
			return livingEntity.getEyeLocation();
		} else {
			return entity.getLocation();
		}
	}

}
