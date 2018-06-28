package com.ruinscraft.powder.models;

import org.bukkit.Particle;

public class PowderParticle implements Cloneable {

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

	public PowderParticle(PowderParticle powderParticle) {
		this.particleChar = powderParticle.getCharacter();
		this.particle = powderParticle.getParticle();
		this.amount = powderParticle.getAmount();
		this.xOff = powderParticle.getXOff();
		this.yOff = powderParticle.getYOff();
		this.zOff = powderParticle.getZOff();
		this.data = powderParticle.getData();
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

	public void setCharacter(char particleChar) {
		this.particleChar = particleChar;
	}

	public Particle getParticle() {
		return particle;
	}

	public void setParticle(Particle particle) {
		this.particle = particle;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public double getXOff() {
		return xOff;
	}

	public void setXOff(double xOff) {
		this.xOff = xOff;
	}

	public double getYOff() {
		return yOff;
	}

	public void setYOff(double yOff) {
		this.yOff = yOff;
	}

	public double getZOff() {
		return zOff;
	}

	public void setZOff(double zOff) {
		this.zOff = zOff;
	}

	public double getData() {
		return data;
	}

	public void setData(double data) {
		this.data = data;
	}

	@Override
	public PowderParticle clone() {
		return new PowderParticle(this);
	}

}
