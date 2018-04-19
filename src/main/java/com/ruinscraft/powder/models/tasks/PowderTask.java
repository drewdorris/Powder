package com.ruinscraft.powder.models.tasks;

import org.bukkit.Location;

import com.ruinscraft.powder.models.powders.Powder;

public interface PowderTask {

	String getName();
	
	Powder getPowder();
	
	Location getCurrentLocation();

}