package com.ruinscraft.powder.models;

import org.bukkit.Location;

public interface PowderElement {

	void create(Location location); // create this element at this location

	int getStartTime(); // what tick to start at

	int getRepeatTime(); // iterate after how many ticks

	int getLockedIterations(); // how many times to iterate; 0 if unlimited

	void setLockedIterations(int iteration); // set how many times iterated so far

	int getIterations(); // how many iterations so far
	
	void iterate(); // add to getIterations()

}