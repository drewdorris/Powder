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
	// after how many ticks should it repeat?
	private int repeatTime;
	// set maximum iterations (0 if infinite)
	private int lockedIterations;

	// iterations so far
	private int iterations;

	public Dust(Dust dust) {
		this.powderParticle = dust.getPowderParticle();
		this.radius = dust.getRadius();
		this.height = dust.getHeight();
		this.startTime = dust.getStartTime();
		this.repeatTime = dust.getRepeatTime();
		this.lockedIterations = dust.getLockedIterations();
		this.iterations = 0;
	}

	public Dust(PowderParticle powderParticle, double radius, double height, int startTime, int repeatTime, int lockedIterations) {
		this.powderParticle = powderParticle;
		this.radius = radius;
		this.height = height;
		this.startTime = startTime;
		this.repeatTime = repeatTime;
		this.lockedIterations = lockedIterations;
		this.iterations = 0;
	}

	public PowderParticle getPowderParticle() {
		return powderParticle;
	}

	public double getRadius() {
		return radius;
	}

	public double getHeight() {
		return height;
	}

	public int getStartTime() {
		return startTime;
	}

	public int getRepeatTime() {
		return repeatTime;
	}

	public int getLockedIterations() {
		return lockedIterations;
	}

	public void setLockedIterations(int lockedIterations) {
		this.lockedIterations = lockedIterations;
	}

	public int getIterations() {
		return iterations;
	}

	public void iterate() {
		iterations++;
	}

	public Dust clone() {
		return new Dust(this);
	}

	// creates this Dust at the designated location
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
	}

}
