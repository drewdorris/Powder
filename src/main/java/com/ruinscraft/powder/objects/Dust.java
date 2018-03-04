package com.ruinscraft.powder.objects;

import org.bukkit.Particle;

public class Dust {
	
	private Particle particle;
	private double radius;
	private double height;
	
	private long frequency;
	
	public Dust(Particle particle, double radius, double height, long frequency) {
		this.particle = particle;
		this.radius = radius;
		this.height = height;
		this.frequency = frequency;
	}
	
	public Particle getParticle() {
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
