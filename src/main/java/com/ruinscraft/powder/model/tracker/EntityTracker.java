package com.ruinscraft.powder.model.tracker;

import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.integration.PlotSquaredHandler;
import com.ruinscraft.powder.integration.TownyHandler;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EntityTracker implements Tracker {

	private UUID uuid;
	private UUID creator;
	private boolean isPlayer, isLiving;

	private Location location;

	public EntityTracker(Entity entity, Player creator, boolean isPlayer, boolean isLiving) {
		this.uuid = entity.getUniqueId();
		this.creator = creator.getUniqueId();
		this.isPlayer = isPlayer;
		this.isLiving = isLiving;
		refreshLocation();
	}

	public EntityTracker(UUID entityId, UUID creator, boolean isPlayer, boolean isLiving) {
		this.uuid = entityId;
		this.creator = creator;
		this.isPlayer = isPlayer;
		this.isLiving = isLiving;
		refreshLocation();
	}

	public EntityTracker(UUID entityId, UUID creator) {
		this.uuid = entityId;
		this.creator = creator;
		this.isPlayer = Bukkit.getEntity(entityId) instanceof Player;
		this.isLiving = Bukkit.getEntity(entityId) instanceof LivingEntity;
		refreshLocation();
	}

	public Entity getEntity() {
		return Bukkit.getEntity(uuid);
	}

	public UUID getUUID() {
		return uuid;
	}

	public UUID getCreator() {
		return creator;
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

	// Must be run sync!
	@Override
	public void refreshLocation() {
		this.location = getEntityLocation(uuid);
	}

	@Override
	public Location getCurrentLocation() {
		if (PowderPlugin.get().asyncMode()) {
			refreshLocation();
			return this.location;
		} else {
			return getEntityLocation(uuid);
		}
	}

	public Location getEntityLocation(UUID entityUUID) {
		Entity entity = Bukkit.getEntity(uuid);
		if (entity == null) {
			Bukkit.getLogger().info("Entity null!");
		}
		if (isLiving()) {
			return ((LivingEntity) entity).getEyeLocation();
		} else {
			return entity.getLocation().clone().add(0, .5, 0);
		}
	}

	@Override
	public boolean hasControl(Player possibleOwner) {
		if (possibleOwner.hasPermission("powder.removeany")) return true;
		Player owner = null;
		if (isPlayer()) {
			owner = (Player) getEntity();
		}
		if (owner != null) {
			if (owner.equals(possibleOwner)) {
				return true;
			}
			return false;
		}

		Location location = getCurrentLocation();
		if (PowderPlugin.get().hasTowny()) {
			TownyHandler towny = PowderPlugin.get().getTownyHandler();
			return towny.hasPermissionForPowder(possibleOwner, location);
		} else if (PowderPlugin.get().hasPlotSquared()) {
			PlotSquaredHandler plotsquared = PowderPlugin.get().getPlotSquaredHandler();
			return plotsquared.hasPermissionForPowder(possibleOwner.getUniqueId(), location);
		}
		return false;
	}

}
