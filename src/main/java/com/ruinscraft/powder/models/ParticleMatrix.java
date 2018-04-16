package com.ruinscraft.powder.models;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class ParticleMatrix implements PowderElement {

	// [.1;true;2;12;10]
	// [spacing;pitch;startTime;repeatTime;iterations]

	// list of individual Layers associated with this ParticleMatrix
	private List<Layer> layers;
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
	private double addedTilt;
	// when to start displaying this ParticleMatrix
	private int startTime;
	// after how many ticks should it repeat?
	private int repeatTime;
	// set maximum iterations (0 if infinite)
	private int lockedIterations;

	// iterations so far
	private int iterations;

	public ParticleMatrix() {
		this.layers = new ArrayList<Layer>();
		this.playerLeft = 0;
		this.playerUp = 0;
		this.spacing = 0;
		this.startTime = 0;
		this.repeatTime = 0;
		this.lockedIterations = 1;
		this.iterations = 0;
	}

	public ParticleMatrix(ParticleMatrix particleMatrix) {
		this.layers = particleMatrix.getLayers();
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

	public ParticleMatrix(List<Layer> layers, int playerLeft, int playerUp, 
			double spacing, int startTime, int repeatTime, int lockedIterations) {
		this.layers = layers;
		this.playerLeft = playerLeft;
		this.playerUp = playerUp;
		this.spacing = spacing;
		this.startTime = startTime;
		this.repeatTime = repeatTime;
		this.lockedIterations = lockedIterations;
		this.iterations = 0;
	}

	public List<Layer> getLayers() {
		return layers;
	}

	public void setLayers(List<Layer> layers) {
		this.layers = layers;
	}

	public void addLayer(Layer layer) {
		layers.add(layer);
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

	public int getStartTime() {
		return startTime;
	}

	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}

	public int getRepeatTime() {
		return repeatTime;
	}

	public void setRepeatTime(int repeatTime) {
		this.repeatTime = repeatTime;
	}

	public int getLockedIterations() {
		return lockedIterations;
	}

	public void setLockedIterations(int lockedIterations) {
		this.lockedIterations = lockedIterations;
	}

	public int getIterations() {
		return iterations;
	}

	public void iterate() {
		iterations++;
	}

	public ParticleMatrix clone() {
		return new ParticleMatrix(this);
	}

	// creates this ParticleMatrix at the designated location
	public void create(final Location location) {
		double forwardPitch = ((location.clone().getPitch() + getAddedPitch() - 180) * Math.PI) / 180;
		if (forwardPitch == 0) {
			forwardPitch = Math.PI / 180;
		}
		double upwardPitch = ((location.clone().getPitch() + getAddedPitch() - 90) * Math.PI) / 180;
		if (!hasPitch()) {
			forwardPitch = ((getAddedPitch() - 180) * Math.PI) / 180;
			upwardPitch = ((getAddedPitch() - 90) * Math.PI) / 180;
		}
		double forwardYaw = ((location.clone().getYaw() + getAddedRotation() + 90) * Math.PI) / 180;
		double sidewaysYaw = ((location.clone().getYaw() + getAddedRotation() + 180) * Math.PI) / 180;
		double sidewaysTilt = ((getAddedTilt() - 90) * Math.PI) / 180;
		final Vector sideToSideVector = (new Vector(Math.sin(sidewaysTilt) * Math.cos(sidewaysYaw), 
				Math.cos(sidewaysTilt), Math.sin(sidewaysTilt) * Math.sin(sidewaysYaw)).normalize()).multiply(spacing);
		final Vector upAndDownVector = (new Vector(Math.sin(upwardPitch + sidewaysTilt) * Math.cos(forwardYaw), 
				Math.cos(upwardPitch + sidewaysTilt), Math.sin(upwardPitch + sidewaysTilt) * Math.sin(forwardYaw))).normalize().multiply(spacing);
		final Vector forwardVector = (new Vector(Math.sin(forwardPitch + sidewaysTilt) * Math.cos(forwardYaw), 
				Math.cos(forwardPitch - sidewaysTilt), Math.sin(forwardPitch + sidewaysTilt) * Math.sin(forwardYaw)).normalize()).multiply(spacing);

		for (Layer layer : getLayers()) {
			Location startingLocation = location.clone().subtract((upAndDownVector.clone().multiply(getPlayerUp() + 1)))
					.add(sideToSideVector.clone().multiply(getPlayerLeft()))
					.add(forwardVector.clone().multiply(layer.getPosition()));
			for (List<PowderParticle> powderParticles : layer.getRows()) {
				int i = 0;
				for (PowderParticle powderParticle : powderParticles) {
					i--;
					if (powderParticle.getParticle() == null) {
						startingLocation = startingLocation.clone().subtract(sideToSideVector.clone());
						continue;
					}
					// spawn the particle
					if (powderParticle.getData() == null) {
						if (powderParticle.getXOff() == 0) {
							location.getWorld().spawnParticle(powderParticle.getParticle(), startingLocation.clone(), 1, 
									powderParticle.getXOff() / 255, powderParticle.getYOff() / 255, 
									powderParticle.getZOff() / 255, 1);
							continue;
						}
						location.getWorld().spawnParticle(powderParticle.getParticle(), startingLocation.clone(), 0, 
								powderParticle.getXOff() / 255, powderParticle.getYOff() / 255, 
								powderParticle.getZOff() / 255, 1);
					} else {
						location.getWorld().spawnParticle(powderParticle.getParticle(), startingLocation.clone(), 1, 
								powderParticle.getXOff() / 255, powderParticle.getYOff() / 255,
								powderParticle.getZOff() / 255, (double) powderParticle.getData());
					}
					startingLocation = startingLocation.clone().subtract(sideToSideVector.clone());
				}
				startingLocation = startingLocation.clone().subtract(sideToSideVector.clone().multiply(i)).add(upAndDownVector.clone());
			}
		}
	}

}
