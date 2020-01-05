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

		double roadDist = this.getDistanceFromRoad(plotPlayer);
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

	/**
	 * Roughly get the distance to the nearest road
	 * @param player
	 * @return distance in blocks to the nearest road
	 */
	public double getDistanceFromRoad(PlotPlayer player) {
		return getDistanceFromRoad(player.getLocation(), player.getCurrentPlot());
	}

	/**
	 * Roughly get the distance to the nearest road
	 * @param location
	 * @param plot
	 * @return distance in blocks to the nearest road
	 */
	public double getDistanceFromRoad(Location location, Plot plot) {
		if (location.isPlotRoad()) return 0;
		if (location.getPlot() != plot) return 0;
		for (int i = 1; i < 5; i++) {
			Location locOne = new Location(location.getWorld(), 
					location.getX() + i, location.getY(), location.getZ() + i);
			Location locTwo = new Location(location.getWorld(), 
					location.getX() + i, location.getY(), location.getZ() - i);
			Location locThree = new Location(location.getWorld(), 
					location.getX() - i, location.getY(), location.getZ() + i);
			Location locFour = new Location(location.getWorld(), 
					location.getX() - i, location.getY(), location.getZ() - i);
			if (!isInPlot(plot, locOne, locTwo, locThree, locFour)) {
				return i;
			}
		}
		// more than this doesn't really matter
		return 5;
	}

	public boolean isInPlot(Plot plot, Location... locations) {
		for (Location location : locations) {
			if (location.isPlotRoad() || location.getPlot() != plot) {
				return false;
			}
		}
		return true;
	}

	
	// listen for plot unclaims / clears to see if Powders were in the plot

	// ensure Powder is within limits when created

	// remove powders if user who placed them is removed from the plot

	// remove powders that are in a road if plots are unlinked

}
