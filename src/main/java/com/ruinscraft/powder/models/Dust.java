package com.ruinscraft.powder.models;

public class Dust {

	private PowderParticle powderParticle;
	private double radius;
	private double height;
	private long frequency;
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
