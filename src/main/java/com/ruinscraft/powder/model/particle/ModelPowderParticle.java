package com.ruinscraft.powder.model.particle;

public class ModelPowderParticle extends PowderParticle {

	public ModelPowderParticle() {
		super();
	}

	public ModelPowderParticle(PowderParticle powderParticle) {
		super(powderParticle);
	}

	@Override
	public PowderParticle clone() {
		return new ModelPowderParticle(this);
	}
}
