package com.ruinscraft.powder.models.powders;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class EntityPowder extends Powder {
	
	private LivingEntity entity;
	
	public EntityPowder(Powder powder) {
		super(powder);
	}
	
	public void setEntity(LivingEntity entity) {
		this.entity = entity;
	}
	
	public LivingEntity getEntity() {
		return entity;
	}
	
	@Override
	public Location getCurrentLocation() {
		return entity.getEyeLocation();
	}

}
