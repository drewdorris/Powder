package com.ruinscraft.powder;

import com.ruinscraft.powder.model.PowderTask;
import com.ruinscraft.powder.util.ConfigUtil;
import com.ruinscraft.powder.util.PowderUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class EnvironmentListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkLoad(ChunkLoadEvent event) {
		PowderPlugin instance = PowderPlugin.get();
		if (instance.getPowderHandler() == null || !instance.useStorage()) {
			return;
		}

		for (Entity entity : event.getChunk().getEntities()) {
			instance.getPowderHandler().addEntityToLoad(entity.getUniqueId());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (PowderPlugin.isLoading() || !PowderPlugin.get().useStorage()) {
			return;
		}

		Player player = event.getPlayer();
		Bukkit.getServer().getScheduler().runTaskAsynchronously(
				PowderPlugin.get(),
				() -> PowderUtil.loadUUID(player.getUniqueId()));
	}

	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event) {
		if (PowderPlugin.isLoading()) return;

		// put the trail powder on the arrow
		if (event.getProjectile() == null || !(event.getProjectile() instanceof Projectile)) return;
		Projectile projectile = (Projectile) event.getProjectile();

		PowderTask powderTask = ConfigUtil.loadArrowTrail(projectile, event.getEntity().getUniqueId());
		if (powderTask == null) return;
		PowderPlugin.get().getPowderHandler().runPowderTask(powderTask);
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if (PowderPlugin.isLoading()) return;

		Projectile entity = event.getEntity();
		if (entity == null) return;
		if (!(entity.getShooter() instanceof Player)) return;

		Player shooter = (Player) entity.getShooter();
		Entity hit = event.getHitEntity();
		if (hit == null) return;

		PowderTask powderTask = ConfigUtil.loadArrowHit(hit, shooter.getUniqueId());
		if (powderTask == null) return;
		PowderPlugin.get().getPowderHandler().runPowderTask(powderTask);
	}

}