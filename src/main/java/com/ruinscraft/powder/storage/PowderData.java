package com.ruinscraft.powder.storage;

import java.util.UUID;

/**
 * Will be used to keep track of all necessary DB data
 *
 */
public class PowderData {

	private UUID uuid;
	private String powder;
	private int start;
	private boolean loop;

	public PowderData(UUID uuid, String powder, int start, boolean loop) {
		this.uuid = uuid;
		this.powder = powder;
		this.start = start;
		this.loop = loop;
	}

	/**
	 * UUID of the owner of the Powder
	 * @return UUID
	 */
	public UUID getUUID() {
		return uuid;
	}

	/**
	 * Name of the Powder used
	 * @return Powder name (String)
	 */
	public String getPowder() {
		return powder;
	}

	/**
	 * Get how many ticks the Powder has gone through already
	 * @return int
	 */
	public int getStart() {
		return start;
	}

	/**
	 * Returns true if the Powder is looping
	 * @return boolean
	 */
	public boolean isLooping() {
		return loop;
	}

}
