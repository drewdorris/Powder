package com.ruinscraft.powder.model;

import com.ruinscraft.powder.PowdersCreationTask;
import com.ruinscraft.powder.model.particle.PositionedPowderParticle;
import com.ruinscraft.powder.model.particle.PowderParticle;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ParticleMatrix implements PowderElement {

	// list of individual Layers associated with this ParticleMatrix
	private Set<PositionedPowderParticle> particles;
	// how far left the ParticleMatrix should be started
	private int playerLeft;
	// same, but how far up
	private int playerUp;
	// spacing for this ParticleMatrix
	private double spacing;
	// does the height of the player's eyes affect the direction of this ParticleMatrix?
	private boolean hasPitch;
	// add values to the pitch
	private double addedPitch;
	// add values to the rotation
	private double addedRotation;
	// add values to the tilt
	// experimental
	private double addedTilt;
	// when to start displaying this ParticleMatrix
	private int startTime;
	// after how many ticks should it repeat?
	private int repeatTime;
	// set maximum iterations (0 if infinite)
	private int lockedIterations;
	// the next tick to iterate
	private int nextTick;

	// iterations so far
	private int iterations;

	public ParticleMatrix() {
		this.particles = new HashSet<>();
		this.playerLeft = 0;
		this.playerUp = 0;
		this.spacing = 0;
		this.startTime = 0;
		this.repeatTime = 0;
		this.lockedIterations = 1;
		this.iterations = 0;
	}

	public ParticleMatrix(ParticleMatrix particleMatrix) {
		this.particles = particleMatrix.getParticles();
		this.playerLeft = particleMatrix.getPlayerLeft();
		this.playerUp = particleMatrix.getPlayerUp();
		this.spacing = particleMatrix.getSpacing();
		this.hasPitch = particleMatrix.hasPitch();
		this.addedPitch = particleMatrix.getAddedPitch();
		this.addedRotation = particleMatrix.getAddedRotation();
		this.addedTilt = particleMatrix.getAddedTilt();
		this.startTime = particleMatrix.getStartTime();
		this.repeatTime = particleMatrix.getRepeatTime();
		this.lockedIterations = particleMatrix.getLockedIterations();
		this.iterations = 0;
	}

	public ParticleMatrix(Set<PositionedPowderParticle> particles, int playerLeft, int playerUp,
			double spacing, int startTime, int repeatTime, int lockedIterations) {
		this.particles = particles;
		this.playerLeft = playerLeft;
		this.playerUp = playerUp;
		this.spacing = spacing;
		this.startTime = startTime;
		this.repeatTime = repeatTime;
		this.lockedIterations = lockedIterations;
		this.iterations = 0;
	}

	public void putPowderParticle(PowderParticle powderParticle, int x, int y, int z) {
		PositionedPowderParticle newParticle =
				new PositionedPowderParticle(powderParticle, x, y, z);
		addParticle(newParticle);
	}

	public int getMaxDistance() {
		return getMaxZ() - getMinZ()
				+ getMaxX() + getMaxY();
	}

	public int getMinZ() {
		int z = 0;
		for (PositionedPowderParticle particle : particles) {
			if (particle.getZ() < z) {
				z = particle.getZ();
			}
		}
		return z;
	}

	public int getMaxZ() {
		int z = 0;
		for (PositionedPowderParticle particle : particles) {
			if (particle.getZ() > z) {
				z = particle.getZ();
			}
		}
		return z;
	}

	public int getMaxX() {
		int x = 0;
		for (PositionedPowderParticle particle : particles) {
			if (particle.getX() > x) {
				x = particle.getX();
			}
		}
		return x;
	}

	public int getMaxY() {
		int y = 0;
		for (PositionedPowderParticle particle : particles) {
			if (particle.getY() > y) {
				y = particle.getY();
			}
		}
		return y;
	}

	public boolean hasParticles() {
		return !particles.isEmpty();
	}

	public Set<PositionedPowderParticle> getParticles() {
		return particles;
	}

	public PositionedPowderParticle getParticleAtLocation(int x, int y, int z) {
		for (PositionedPowderParticle particle : particles) {
			if (particle.getX() == x && particle.getY() == y && particle.getZ() == z) {
				return particle;
			}
		}
		return null;
	}

	public void setParticles(Collection<PositionedPowderParticle> particles) {
		this.particles = new HashSet<>(particles);
	}

	public void addParticles(Collection<PositionedPowderParticle> particles) {
		this.particles.addAll(particles);
	}

	public void addParticle(PositionedPowderParticle particle) {
		particles.add(particle);
	}

	public int getPlayerLeft() {
		return playerLeft;
	}

	public void setPlayerLeft(int playerLeft) {
		this.playerLeft = playerLeft;
	}

	public int getPlayerUp() {
		return playerUp;
	}

	public void setPlayerUp(int playerUp) {
		this.playerUp = playerUp;
	}

	public double getSpacing() {
		return spacing;
	}

	public void setSpacing(double spacing) {
		this.spacing = spacing;
	}

	public boolean hasPitch() {
		return hasPitch;
	}

	public void setIfPitch(boolean hasPitch) {
		this.hasPitch = hasPitch;
	}

	public double getAddedPitch() {
		return addedPitch;
	}

	public void setAddedPitch(double addedPitch) {
		this.addedPitch = addedPitch;
	}

	public double getAddedRotation() {
		return addedRotation;
	}

	public void setAddedRotation(double addedRotation) {
		this.addedRotation = addedRotation;
	}

	public double getAddedTilt() {
		return addedTilt;
	}

	public void setAddedTilt(double addedTilt) {
		this.addedTilt = addedTilt;
	}

	@Override
	public int getStartTime() {
		return startTime;
	}

	@Override
	public void setStartTime(int startTime) {
		this.startTime = startTime;
		this.nextTick = startTime;
	}

	@Override
	public int getRepeatTime() {
		return repeatTime;
	}

	@Override
	public void setRepeatTime(int repeatTime) {
		this.repeatTime = repeatTime;
	}

	@Override
	public int getLockedIterations() {
		return lockedIterations;
	}

	@Override
	public void setLockedIterations(int lockedIterations) {
		this.lockedIterations = lockedIterations;
	}

	@Override
	public int getIterations() {
		return iterations;
	}

	@Override
	public void iterate() {
		iterations++;
		this.nextTick = PowdersCreationTask.getCurrentTick() + getRepeatTime();
	}

	@Override
	public int getNextTick() {
		return nextTick;
	}

	@Override
	public void setStartingTick() {
		this.nextTick = PowdersCreationTask.getCurrentTick() + getStartTime();
	}

	@Override
	public ParticleMatrix clone() {
		return new ParticleMatrix(this);
	}

	// creates this ParticleMatrix at the designated location
	@Override
	public void create(final Location location) {
		World world = location.getWorld();
		double forwardPitch =
				((location.clone().getPitch() + getAddedPitch() - 180) * Math.PI) / 180;
		if (forwardPitch == 0) {
			forwardPitch = Math.PI / 180;
		}
		double upwardPitch =
				((location.clone().getPitch() + getAddedPitch() - 90) * Math.PI) / 180;
		if (!hasPitch()) {
			forwardPitch = ((getAddedPitch() - 180) * Math.PI) / 180;
			upwardPitch = ((getAddedPitch() - 90) * Math.PI) / 180;
		}
		double forwardYaw =
				((location.clone().getYaw() + getAddedRotation() + 90) * Math.PI) / 180;
		double sidewaysYaw =
				((location.clone().getYaw() + getAddedRotation() + 180) * Math.PI) / 180;
		double sidewaysTilt =
				((getAddedTilt() - 90) * Math.PI) / 180;

		final Vector sideToSideVector =
				new Vector(Math.sin(sidewaysTilt) * Math.cos(sidewaysYaw),
						Math.cos(sidewaysTilt),
						Math.sin(sidewaysTilt) * Math.sin(sidewaysYaw))
				.normalize().multiply(spacing);
		final Vector upAndDownVector =
				new Vector(Math.sin(upwardPitch + sidewaysTilt) * Math.cos(forwardYaw),
						Math.cos(upwardPitch + sidewaysTilt),
						Math.sin(upwardPitch + sidewaysTilt) * Math.sin(forwardYaw))
				.normalize().multiply(spacing);
		final Vector forwardVector =
				new Vector(Math.sin(forwardPitch + sidewaysTilt) * Math.cos(forwardYaw),
						Math.cos(forwardPitch - sidewaysTilt),
						Math.sin(forwardPitch + sidewaysTilt) * Math.sin(forwardYaw))
				.normalize().multiply(spacing);

		Location start = location.clone().subtract((upAndDownVector.clone().multiply(getPlayerUp())))
				.subtract(sideToSideVector.clone().multiply(getPlayerLeft()));
		for (PositionedPowderParticle particle : particles) {
			Location position = start.clone()
					.add(sideToSideVector.clone().multiply(particle.getX()))
					.add(upAndDownVector.clone().multiply(particle.getY()))
					.add(forwardVector.clone().multiply(particle.getZ()));
			world.spawnParticle(
					particle.getParticle(), position, particle.getAmount(),
					particle.getXOff() / 255, particle.getYOff() / 255,
					particle.getZOff() / 255, particle.getData());
		}
	}

}
