package com.ruinscraft.powder.models;

import org.bukkit.Particle;

public class PowderParticle {

	// enum name for the PowderParticle; one single character (name does not have to exist)
	private char particleChar;
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
	
	public PowderParticle() {
		particleChar = 0;
		particle = null;
	}

	public PowderParticle(char particleChar, Particle particle, 
			double xOff, double yOff, double zOff, Object data) {
		this.particleChar = particleChar;
		this.particle = particle;
		this.xOff = xOff;
		this.yOff = yOff;
		this.zOff = zOff;
		this.data = data;
	}
	
	public PowderParticle(Particle particle, double xOff, double yOff, double zOff, Object data) {
		this.particleChar = 0;
		this.particle = particle;
		this.xOff = xOff;
		this.yOff = yOff;
		this.zOff = zOff;
		this.data = data;
	}

	public PowderParticle(char particleChar, Particle particle,
			double xOff, double yOff, double zOff) {
		this.particleChar = particleChar;
		this.particle = particle;
		this.xOff = xOff;
		this.yOff = yOff;
		this.zOff = zOff;
	}

	public PowderParticle(char particleChar, Particle particle) {
		this.particleChar = particleChar;
		this.particle = particle;
	}

	public char getCharacter() {
		return particleChar;
	}

	public Particle getParticle() {
		return particle;
	}

	public double getXOff() {
		return xOff;
	}

	public double getYOff() {
		return yOff;
	}

	public double getZOff() {
		return zOff;
	}

	public Object getData() {
		return data;
	}

}
