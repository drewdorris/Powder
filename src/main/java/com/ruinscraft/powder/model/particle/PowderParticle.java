package com.ruinscraft.powder.model.particle;

import org.bukkit.Particle;

public abstract class PowderParticle implements Cloneable {

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
	private Object data;

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
			double xOff, double yOff, double zOff, Object data) {
		this.particleChar = particleChar;
		this.particle = particle;
		this.amount = amount;
		this.xOff = xOff;
		this.yOff = yOff;
		this.zOff = zOff;
		this.data = data;
	}

	public PowderParticle(Particle particle, int amount,
			double xOff, double yOff, double zOff, Object data) {
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
		this.xOff = 0;
		this.yOff = 0;
		this.zOff = 0;
		this.data = (Void) null;
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

	public Object getData() {
		return data;
	}

	public void setData(double data) {
		this.data = data;
	}

	@Override
	public PowderParticle clone() {
		return this;
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof PowderParticle)) {
			return false;
		}
		PowderParticle particle = (PowderParticle) object;
		if (particle.getAmount() == this.amount && particle.getCharacter() == this.particleChar &&
				particle.getParticle() == this.particle && particle.getXOff() == this.xOff &&
				particle.getYOff() == this.yOff && particle.getZOff() == this.zOff
				&& particle.getData() == particle.getData()) {
			return true;
		}
		return false;
	}

}
