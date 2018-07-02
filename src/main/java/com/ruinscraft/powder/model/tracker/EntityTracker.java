package com.ruinscraft.powder.model.tracker;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class EntityTracker implements Tracker {

	private UUID uuid;
	private boolean isPlayer, isLiving;

	public EntityTracker(Entity entity, boolean isPlayer, boolean isLiving) {
		this.uuid = entity.getUniqueId();
		this.isPlayer = isPlayer;
		this.isLiving = isLiving;
	}

	public EntityTracker(UUID entityId, boolean isPlayer, boolean isLiving) {
		this.uuid = entityId;
		this.isPlayer = isPlayer;
		this.isLiving = isLiving;
	}

	public Entity getEntity() {
		return Bukkit.getEntity(uuid);
	}

	public UUID getUUID() {
		return uuid;
	}

	public boolean isPlayer() {
		return isPlayer;
	}

	public boolean isLiving() {
		return isLiving;
	}

	public boolean isAlive() {
		Entity entity = Bukkit.getEntity(uuid);
		if (entity == null || entity.isDead()) {
			return false;
		}
		return true;
	}

	@Override
	public Tracker.Type getType() {
		return Tracker.Type.ENTITY;
	}

	@Override
	public Location getCurrentLocation() {
		Entity entity = Bukkit.getEntity(uuid);
		if (isLiving()) {
			return ((LivingEntity) entity).getEyeLocation();
		} else {
			return entity.getLocation().clone().add(0, .5, 0);
		}
	}

}
