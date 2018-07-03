package com.ruinscraft.powder.model.particle;

import org.bukkit.Particle;

public class ModelPowderParticle extends PowderParticle {

	public ModelPowderParticle(PowderParticle powderParticle) {
		super(powderParticle);
	}

	public ModelPowderParticle(Particle particle, int amount, 
			double xOff, double yOff, double zOff, double data) {
		super('0', particle, amount, xOff, yOff, zOff, data);
	}

	public ModelPowderParticle(char particleChar, Particle particle, int amount, 
			double xOff, double yOff, double zOff, double data) {
		super(particleChar, particle, amount, xOff, yOff, zOff, data);
	}

	public ModelPowderParticle(char particleChar, Particle particle) {
		super(particleChar, particle);
	}

	@Override
	public PowderParticle clone() {
		return new ModelPowderParticle(this);
	}

}
