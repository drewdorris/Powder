package com.ruinscraft.powder.model.particle;

import org.bukkit.Particle;

public class PositionedPowderParticle extends PowderParticle {

	private int x;
	private int y;
	private int z;

	public PositionedPowderParticle(PowderParticle particle, int x, int y, int z) {
		super(particle);
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public PositionedPowderParticle(char particleChar, Particle particle, int x, int y, int z) {
		super(particleChar, particle);
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public PositionedPowderParticle(Particle particle, int amount,
			double xOff, double yOff, double zOff, Object data, int x, int y, int z) {
		super('0', particle, amount, xOff, yOff, zOff, data);
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public PositionedPowderParticle(char particleChar, Particle particle, int amount,
			double xOff, double yOff, double zOff, Object data, int x, int y, int z) {
		super(particleChar, particle, amount, xOff, yOff, zOff, data);
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	@Override
	public PowderParticle clone() {
		return new PositionedPowderParticle(this, getX(), getY(), getZ());
	}

}
