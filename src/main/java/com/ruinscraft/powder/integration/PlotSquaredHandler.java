package com.ruinscraft.powder.integration;

import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.github.intellectualsites.plotsquared.api.PlotAPI;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.github.intellectualsites.plotsquared.plot.object.PlotPlayer;
import com.ruinscraft.powder.model.Powder;

/**
 * Handles PlotSquared-related features and events
 *
 */
public class PlotSquaredHandler implements Listener {

	private PlotAPI plotAPI;

	public PlotSquaredHandler() {
		this.plotAPI = new PlotAPI();
	}

	public boolean checkLocation(Powder powder, Player player) {
		PlotPlayer plotPlayer = plotAPI.wrapPlayer(player.getUniqueId());
		Location location = plotPlayer.getLocation();

		if (!canPlacePowders(plotPlayer)) return false;

		double roadDist = getDistanceFromRoad(plotPlayer);
		double powderWidth = powder.maxWidthDistance();
		if (powderWidth > roadDist) return false;
		
		// check location is within allowed bounds of player
		return false;
	}

	public boolean canPlacePowders(PlotPlayer player) {
		return canPlacePowdersInPlot(player, player.getCurrentPlot());
	}

	public boolean canPlacePowdersInPlot(PlotPlayer player, Plot plot) {
		for (UUID uuid : plot.getTrusted()) {
			if (uuid.equals(player.getUUID())) return true;
		}
		for (UUID uuid : plot.getOwners()) {
			if (uuid.equals(player.getUUID())) return true;
		}
		return false;
	}

	public double getDistanceFromRoad(PlotPlayer player) {
		return getDistanceFromRoad(player.getLocation(), player.getCurrentPlot());
	}

	public double getDistanceFromRoad(Location location, Plot plot) {
		if (location.isPlotRoad()) return 0;
		List<Location> corners = plot.getAllCorners();
		// impl
		return 1;
	}

	// listen for plot unclaims / clears to see if Powders were in the plot

	// ensure Powder is within limits when created

	// remove powders if user who placed them is removed from the plot

}
