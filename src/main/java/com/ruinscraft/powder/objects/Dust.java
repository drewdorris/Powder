package com.ruinscraft.powder.objects;

public class Dust {

	private PowderParticle powderParticle;
	private double radius;
	private double height;

	private long frequency;

	public Dust(PowderParticle powderParticle, double radius, double height, long frequency) {
		this.powderParticle = powderParticle;
		this.radius = radius;
		this.height = height;
		this.frequency = frequency;
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

}
