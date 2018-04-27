package com.ruinscraft.powder.models;

import org.bukkit.Particle;

public class PowderParticle {

	// enum name for the PowderParticle; one single character (name does not have to exist)
	private char particleChar;
	// the Particle assigned with the PowderParticle
	private Particle particle;
	// amount of Particles
	private int amount;
	// x-offset data
	private double xOff;
	// y-offset data
	private double yOff;
	// z-offset data
	private double zOff;
	// extra data 
	private double data;

	public PowderParticle() {
		particleChar = 0;
		particle = null;
	}

	public PowderParticle(char particleChar, Particle particle, int amount, 
			double xOff, double yOff, double zOff, double data) {
		this.particleChar = particleChar;
		this.particle = particle;
		this.amount = amount;
		this.xOff = xOff;
		this.yOff = yOff;
		this.zOff = zOff;
		this.data = data;
	}

	public PowderParticle(Particle particle, int amount, 
			double xOff, double yOff, double zOff, double data) {
		this.particleChar = 0;
		this.particle = particle;
		this.amount = amount;
		this.xOff = xOff;
		this.yOff = yOff;
		this.zOff = zOff;
		this.data = data;
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

	public int getAmount() {
		return amount;
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

	public double getData() {
		return data;
	}

}
