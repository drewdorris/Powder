package com.ruinscraft.powder.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;

import com.ruinscraft.powder.PowderCommand;
import com.ruinscraft.powder.PowderHandler;
import com.ruinscraft.powder.PowderPlugin;
import com.ruinscraft.powder.models.Powder;
import com.ruinscraft.powder.util.PowderUtil;

public class LoadPlayerFromDatabaseTask implements Runnable {

	private final UUID uuid;
	
	public LoadPlayerFromDatabaseTask(final UUID uuid) {
		this.uuid = uuid;
	}
	
	@Override
	public void run() {
		List<String> enabledPowders = PowderPlugin.getInstance().getStorage().getEnabledPowders(uuid);
		
		PowderHandler handler = PowderPlugin.getInstance().getPowderHandler();
		
		for (String powderName : enabledPowders) {
			
			Powder powder = handler.getPowder(powderName);

			if (powder == null) {
				continue;
			}
			
			if (!PowderCommand.hasPermission(Bukkit.getPlayer(uuid), powder)) {
				continue;
			}
			
			List<Integer> tasks = new ArrayList<Integer>();

			tasks.addAll(PowderUtil.createPowder(Bukkit.getPlayer(uuid), powder));
			
			PowderTask powderTask = new PowderTask(Bukkit.getPlayer(uuid), tasks, powder);
			handler.addPowderTask(powderTask);
		}
		
	}
	
}
