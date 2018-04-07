package com.ruinscraft.powder.models;

import org.bukkit.Location;

public class Dust implements PowderElement {
	
	// 'A;2;1;3;3;0'
	// 'PowderParticle;radius;height&depth;startTime;repeatTime;iterations'

	// the PowderParticle associated with this Dust
	private PowderParticle powderParticle;
	// the limit radius that this Dust can be spawned in in
	private double radius;
	// the limit height (up & down) that this Dust can be spawned in
	private double height;
	// when to start displaying this Dust
	private int startTime;
	// set maximum iterations
	private int lockedIterations;
	// iterations (0 if infinite)
	private int iterations;
	// after how many ticks should it repeat?
	private int repeatTime;
	// is this Dust only spawned once?
	private boolean singleOccurrence;

	public Dust(PowderParticle powderParticle, double radius, double height, boolean singleOccurrence) {
		this.powderParticle = powderParticle;
		this.radius = radius;
		this.height = height;
		this.singleOccurrence = singleOccurrence;
	}

	public PowderParticle getPowderParticle() {
		return powderParticle;
	}

	public Double getRadius() {
		return radius;
	}

	public Double getHeight() {
		return height;
	}
	
	public Integer getStartTime() {
		return startTime;
	}

	public Integer getLockedIterations() {
		return lockedIterations;
	}
	
	public void setLockedIterations(int lockedIterations) {
		this.lockedIterations = lockedIterations;
	}
	
	public Integer getIterations() {
		return iterations;
	}

	public Integer getRepeatTime() {
		return repeatTime;
	}

	public boolean isSingleOccurrence() {
		return singleOccurrence;
	}

	public void create(Location location) {
		double radiusZoneX = (Math.random() - .5) * (2 * getRadius());
		double radiusZoneZ = (Math.random() - .5) * (2 * getRadius());
		double heightZone = (Math.random() - .5) * (2 * getHeight());
		Location particleLocation = location.add(radiusZoneX, heightZone + 1, radiusZoneZ);
		// if no block in the way
		if (particleLocation.getBlock().isEmpty()) {
			PowderParticle powderParticle = getPowderParticle();
			if (powderParticle.getData() == null) {
				location.getWorld().spawnParticle(powderParticle.getParticle(), particleLocation, 0, (powderParticle.getXOff() / 255), 
						powderParticle.getYOff() / 255, powderParticle.getZOff() / 255, 1);
			} else {
				location.getWorld().spawnParticle(powderParticle.getParticle(), particleLocation, 1, (powderParticle.getXOff() / 255), 
						powderParticle.getYOff() / 255, powderParticle.getZOff() / 255, 
						(double) powderParticle.getData());
			}
		}
		iterations++;
	}

}
