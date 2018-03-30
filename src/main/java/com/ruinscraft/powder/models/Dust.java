package com.ruinscraft.powder.models;

public class Dust {

	// the PowderParticle associated with this Dust
	private PowderParticle powderParticle;
	// the limit radius that this Dust can be spawned in in
	private double radius;
	// the limit height (up & down) that this Dust can be spawned in
	private double height;
	// how many ticks per minute this Dust is spawned
	private long frequency;
	// is this Dust only spawned once?
	private boolean singleOccurrence;

	public Dust(PowderParticle powderParticle, double radius, double height, long frequency, boolean singleOccurrence) {
		this.powderParticle = powderParticle;
		this.radius = radius;
		this.height = height;
		this.frequency = frequency;
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

	public Long getFrequency() {
		return frequency;
	}

	public boolean isSingleOccurrence() {
		return singleOccurrence;
	}

}
