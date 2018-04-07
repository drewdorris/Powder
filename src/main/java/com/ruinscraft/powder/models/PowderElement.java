package com.ruinscraft.powder.models;

import org.bukkit.Location;

public interface PowderElement {

	void create(Location location); // create this element at this location

	Integer getStartTime(); // what tick to start at

	Integer getRepeatTime(); // iterate after how many ticks

	Integer getLockedIterations(); // how many times to iterate; 0 if unlimited

	void setLockedIterations(int iteration); // set how many times iterated so far

	Integer getIterations(); // how many iterations so far

}