package com.ruinscraft.powder;

import com.ruinscraft.powder.util.PowderUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class EnvironmentListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkLoad(ChunkLoadEvent event) {
		PowderPlugin instance = PowderPlugin.getInstance();
		if (instance.getPowderHandler() == null || !instance.useStorage()) {
			return;
		}

		for (Entity entity : event.getChunk().getEntities()) {
			instance.getPowderHandler().addEntityToLoad(entity.getUniqueId());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (PowderPlugin.isLoading() || !PowderPlugin.getInstance().useStorage()) {
			return;
		}

		Player player = event.getPlayer();
		Bukkit.getServer().getScheduler().runTaskAsynchronously(
				PowderPlugin.getInstance(),
				() -> PowderUtil.loadUUID(player.getUniqueId()));
	}

}