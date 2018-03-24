package com.ruinscraft.powder.objects;

import org.bukkit.Particle;

public class PowderParticle {

	private String particleName;
	private Particle particle;
	private double xOff;
	private double yOff;
	private double zOff;
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
