package com.ruinscraft.particle;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.ruinscraft.particle.objects.ParticleTask;

public class PlayerLeaveEvent implements Listener {
	
	ParticleHandler phandler = RCParticle.getInstance().getParticleHandler();
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLeave(PlayerQuitEvent event) {
		for (ParticleTask ptask : phandler.getParticleTasks(event.getPlayer())) {
			phandler.removeParticleTask(ptask);
		}
	}
	
}
