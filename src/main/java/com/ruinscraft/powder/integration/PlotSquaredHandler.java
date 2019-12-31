package com.ruinscraft.powder.integration;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.github.intellectualsites.plotsquared.api.PlotAPI;

public class PlotSquaredHandler implements Listener {

	private PlotAPI plotAPI;

	public PlotSquaredHandler() {
		this.plotAPI = new PlotAPI();
	}

	public boolean checkLocation(Player player, Location location) {
		// check location is within allowed bounds of player
		return false;
	}

	// listen for plot unclaims / clears to see if Powders were in the plot

	// ensure Powder is within limits when created

}
