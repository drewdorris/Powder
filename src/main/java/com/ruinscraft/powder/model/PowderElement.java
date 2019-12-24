package com.ruinscraft.powder.model;

import org.bukkit.Location;

public interface PowderElement extends Cloneable {

	void create(Location location); // create this element at this location

	/**
	 * Get what tick the element starts at (0 being the initial creation of the Powder)
	 * @return
	 */
	int getStartTime();

	/**
	 * Get how many ticks after the start time to iterate
	 * @return integer
	 */
	int getRepeatTime();

	/**
	 * Get how many ticks after the start time to iterate
	 * @param repeatTime
	 */
	void setRepeatTime(int repeatTime);

	/**
	 * Get how many times to iterate this element
	 * 0 if unlimited
	 * @return integer
	 */
	int getLockedIterations(); // how many times to iterate; 0 if unlimited

	/**
	 * Set how many times to iterate this element
	 * 0 if unlimited
	 * @param lockedIterations
	 */
	void setLockedIterations(int lockedIterations);

	/**
	 * Get how many times this element has iterated so far
	 * @return integer
	 */
	int getIterations();

	/**
	 * Iterates this element
	 */
	void iterate();

	/**
	 * Get the next tick this element will iterate at
	 * @return integer
	 */
	int getNextTick();

	/**
	 * Sets the starting tick using the defined tick in PowdersCreationTask right before the element is used
	 */
	void setStartingTick();

	/**
	 * Clones this PowderElement
	 * @return a cloned PowderElement
	 */
	PowderElement clone();

}