package com.ruinscraft.powder.model;

import org.bukkit.Location;

public interface PowderElement extends Cloneable {

    void create(Location location); // create this element at this location

    int getStartTime(); // what tick to start at

    int getRepeatTime(); // iterate after how many ticks

    int getLockedIterations(); // how many times to iterate; 0 if unlimited

    void setLockedIterations(int lockedIterations); // set how many times iterated so far

    int getIterations(); // how many iterations so far

    void iterate(); // add to getIterations()

    int getNextTick(); // the tick for the PowderElement to play

    void setStartingTick(); // sets the starting tick, right before the element starts

    PowderElement clone();

}