package com.ruinscraft.powder.model.tracker;

import com.ruinscraft.powder.PowderPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.UUID;

public class EntityTracker implements Tracker {

    private UUID uuid;
    private boolean isPlayer, isLiving;

    private Location location;

    public EntityTracker(Entity entity, boolean isPlayer, boolean isLiving) {
        this.uuid = entity.getUniqueId();
        this.isPlayer = isPlayer;
        this.isLiving = isLiving;
        refreshLocation();
    }

    public EntityTracker(UUID entityId, boolean isPlayer, boolean isLiving) {
        this.uuid = entityId;
        this.isPlayer = isPlayer;
        this.isLiving = isLiving;
        refreshLocation();
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

    // Must be run sync!
    @Override
    public void refreshLocation() {
        this.location = getEntityLocation(uuid);
    }

    @Override
    public Location getCurrentLocation() {
        if (PowderPlugin.getInstance().asyncMode()) {
            refreshLocation();
            return this.location;
        } else {
            return getEntityLocation(uuid);
        }
    }

    public Location getEntityLocation(UUID entityUUID) {
        Entity entity = Bukkit.getEntity(uuid);
        if (isLiving()) {
            return ((LivingEntity) entity).getEyeLocation();
        } else {
            return entity.getLocation().clone().add(0, .5, 0);
        }
    }

}
