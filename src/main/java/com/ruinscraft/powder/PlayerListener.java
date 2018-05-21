package com.ruinscraft.powder;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;

import com.ruinscraft.powder.util.PowderUtil;

public class PlayerListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onChunkLoad(ChunkLoadEvent event) {
		Chunk chunk = event.getChunk();
		for (Entity entity : chunk.getEntities()) {
			PowderPlugin.getInstance().getPowderHandler()
			.addEntityToLoad(entity.getUniqueId());
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if (PowderPlugin.isLoading()) {
			return;
		}

		Bukkit.getServer().getScheduler().runTaskAsynchronously(
				PowderPlugin.getInstance(), () -> {
					PowderUtil.loadUUID(player.getUniqueId());
		});
	}

}