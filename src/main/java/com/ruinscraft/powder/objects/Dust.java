package com.ruinscraft.powder.objects;

public class Dust {
	
	private String particle;
	private double radius;
	private double height;
	
	private long frequency;
	
	public Dust(String particle, double radius, double height, long frequency) {
		this.particle = particle;
		this.radius = radius;
		this.height = height;
		this.frequency = frequency;
	}
	
	public String getParticle() {
		return particle;
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
