package com.ruinscraft.powder.models;

import org.bukkit.Location;

public interface PowderElement {

	void create(Location location); // create this element at this location

	int getRepeatTime(); // iterate after how many ticks

	int getIterations(); // how many times to iterate; 0 if unlimited

}
