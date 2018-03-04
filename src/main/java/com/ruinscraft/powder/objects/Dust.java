package com.ruinscraft.powder.objects;

import org.bukkit.Particle;

public class Dust {
	
	private Particle particle;
	private float radius;
	private float height;
	
	private long frequency;
	
	public Dust(Particle particle, float radius, float height, long frequency) {
		this.particle = particle;
		this.radius = radius;
		this.height = height;
		this.frequency = frequency;
	}
	
	public Particle getParticle() {
		return particle;
	}
	
	public Float getRadius() {
		return radius;
	}
	
	public Float getHeight() {
		return height;
	}
	
	public Long getFrequency() {
		return frequency;
	}
	
}
