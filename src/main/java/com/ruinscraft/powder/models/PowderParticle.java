package com.ruinscraft.powder.models;

import org.bukkit.Particle;

public class PowderParticle {

	// enum name for the PowderParticle; one single character (name does not have to exist)
	private String particleName;
	// the Particle assigned with the PowderParticle
	private Particle particle;
	// x-offset data
	private double xOff;
	// y-offset data
	private double yOff;
	// z-offset data
	private double zOff;
	// extra data 
	private Object data;

	public PowderParticle(String particleName, Particle particle, 
			double xOff, double yOff, double zOff, Object data) {

		this.particleName = particleName;
		this.particle = particle;
		this.xOff = xOff;
		this.yOff = yOff;
		this.zOff = zOff;
		this.data = data;

	}

	public PowderParticle(String particleName, Particle particle,
			double xOff, double yOff, double zOff) {

		this.particleName = particleName;
		this.particle = particle;
		this.xOff = xOff;
		this.yOff = yOff;
		this.zOff = zOff;

	}

	public PowderParticle(String particleName, Particle particle) {

		this.particleName = particleName;
		this.particle = particle;

	}

	// null
	public PowderParticle(Particle particle) {

		this.particleName = null;
		this.particle = particle;

	}

	public String getEnumName() {
		return particleName;
	}

	public Particle getParticle() {
		return particle;
	}

	public Double getXOff() {
		return xOff;
	}

	public Double getYOff() {
		return yOff;
	}

	public Double getZOff() {
		return zOff;
	}

	public Object getData() {
		return data;
	}

}
