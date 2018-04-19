package com.ruinscraft.powder.models.tasks;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.ruinscraft.powder.models.powders.Powder;

public class EntityPowderTask implements PowderTask {

	// name associated with this PowderTask (null if no player)
	private String name;
	// UUID associated with the Player this PowderTask follows
	private Entity entity;
	// Powder associated with this PowderTask
	private Powder powder;

	public EntityPowderTask(Entity entity, Powder powder) {
		this.name = null;
		this.entity = entity;
		this.powder = powder;
	}

	public EntityPowderTask(String name, Entity entity, Powder powder) {
		this.name = name;
		this.entity = entity;
		this.powder = powder;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Entity getEntity() {
		return entity;
	}

	@Override
	public Powder getPowder() {
		return powder;
	}

	@Override
	public Location getCurrentLocation() {
		if (entity instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity) entity;
			return livingEntity.getEyeLocation();
		}
		return entity.getLocation();
	}

}
