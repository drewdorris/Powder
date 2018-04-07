package com.ruinscraft.powder.models;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;

import com.ruinscraft.powder.util.PowderUtil;

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
	private float spacing;
	private boolean hasPitch;
	// when to start displaying this ParticleMatrix
	private int startTime;
	// set maximum iterations
	private int lockedIterations;
	// iterations (0 if infinite)
	private int iterations;
	// after how many ticks should it repeat?
	private int repeatTime;

	public ParticleMatrix() {
		this.layers = new ArrayList<Layer>();
		startTime = 0;
		playerLeft = 0;
		playerUp = 0;
		spacing = 0;
	}

	public ParticleMatrix(List<Layer> layers, int startTime, int playerLeft, int playerUp, float spacing) {
		this.layers = layers;
		this.startTime = startTime;
		this.playerLeft = playerLeft;
		this.playerUp = playerUp;
		this.spacing = spacing;
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

	public Integer getPlayerLeft() {
		return playerLeft;
	}

	public void setPlayerLeft(int playerLeft) {
		this.playerLeft = playerLeft;
	}

	public Integer getPlayerUp() {
		return playerUp;
	}

	public void setPlayerUp(int playerUp) {
		this.playerUp = playerUp;
	}

	public Float getSpacing() {
		return spacing;
	}

	public void setSpacing(float spacing) {
		this.spacing = spacing;
	}
	
	public Integer getStartTime() {
		return startTime;
	}
	
	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}
	
	public Integer getLockedIterations() {
		return lockedIterations;
	}
	
	public void setLockedIterations(int lockedIterations) {
		this.lockedIterations = lockedIterations;
	}
	
	public Integer getIterations() {
		return iterations;
	}
	
	public Integer getRepeatTime() {
		return repeatTime;
	}

	public void create(Location location) {
		// get rotation (left/right) and pitch (up/down) of the player
		final double playerRotation = ((location.getYaw()) % 360) * (Math.PI / 180);
		final double playerPitch;

		// distance between each row up/down
		final double distanceBetweenRowsY;
		// this would not do anything if the Powder has pitch movement disabled, since it's directly up/down
		final double distanceBetweenRowsXZ;

		// if the Powder moves up/down with the head, set some values that will allow that to happen
		// else, make them do nothing significant
		if (hasPitch) {
			playerPitch = (location.getPitch() * (Math.PI / 180));
			distanceBetweenRowsY = ((Math.sin((Math.PI / 2) - playerPitch)) * spacing);
			distanceBetweenRowsXZ = ((Math.cos((Math.PI / 2) + playerPitch)) * spacing);
		} else {
			playerPitch = 0;
			distanceBetweenRowsY = spacing;
			distanceBetweenRowsXZ = 0;
		}

		// where to start in relation to the player/location
		int left = getPlayerLeft();
		int up = getPlayerUp();

		// amount to add between each individual particle
		final double amountToAddX = PowderUtil.getDirLengthX(playerRotation, spacing);
		final double amountToAddZ = PowderUtil.getDirLengthZ(playerRotation, spacing);
		// how far front/back the layer is
		final double startARowX = PowderUtil.getDirLengthX(playerRotation + (Math.PI / 2), spacing);
		final double startARowZ = PowderUtil.getDirLengthZ(playerRotation + (Math.PI / 2), spacing);
		// if pitch is enabled, this will adjust the x & z for each row, since the Powder isn't top-down
		final double moveWithPitchX = PowderUtil.getDirLengthX(playerRotation - (Math.PI / 2), distanceBetweenRowsXZ);
		final double moveWithPitchZ = PowderUtil.getDirLengthZ(playerRotation - (Math.PI / 2), distanceBetweenRowsXZ);
		// don't remember what this does
		final double moveBackWithPitchX = PowderUtil.getDirLengthX(playerRotation, distanceBetweenRowsXZ);
		final double moveBackWithPitchY = (Math.sin(0 - playerPitch) * spacing);
		final double moveBackWithPitchZ = PowderUtil.getDirLengthZ(playerRotation, distanceBetweenRowsXZ);

		// starts the Powder in relation to the given left & up
		final double startX = location.getX() - 
				(amountToAddX * left) + (moveWithPitchX * up);
		final double startY = location.getY() + (distanceBetweenRowsY * up);
		final double startZ = location.getZ() - 
				(amountToAddZ * left) + (moveWithPitchZ * up);

		// start off
		double newX = startX;
		double newY = startY;
		double newZ = startZ;

		for (Layer layer : getLayers()) {

			// sets the position in relation to the layer's front/back position
			float position = layer.getPosition();
			newX = startX + (startARowX * position) + (moveBackWithPitchZ * position);
			newY = startY + (moveBackWithPitchY * position);
			newZ = startZ + (startARowZ * position) + (moveBackWithPitchX * position);

			// rowsDownSoFar is how many rows that have been processed
			int rowsDownSoFar = 0;
			for (List<PowderParticle> row : layer.getRows()) {

				for (PowderParticle powderParticle : row) {

					// add the amount per particle given
					newX = newX + amountToAddX;
					newZ = newZ + amountToAddZ;

					if (powderParticle.getParticle() == null) {
						continue;
					}

					// spawn the particle
					if (powderParticle.getData() == null) {
						if (powderParticle.getXOff() == null) {
							location.getWorld().spawnParticle(powderParticle.getParticle(), newX, newY, newZ, 1, 
									powderParticle.getXOff() / 255, powderParticle.getYOff() / 255, 
									powderParticle.getZOff() / 255, 1);
							continue;
						}
						location.getWorld().spawnParticle(powderParticle.getParticle(), newX, newY, newZ, 0, 
								powderParticle.getXOff() / 255, powderParticle.getYOff() / 255, 
								powderParticle.getZOff() / 255, 1);
					} else {
						location.getWorld().spawnParticle(powderParticle.getParticle(), newX, newY, newZ, 1, 
								powderParticle.getXOff() / 255, powderParticle.getYOff() / 255,
								powderParticle.getZOff() / 255, (double) powderParticle.getData());
					}

				}

				// move the row down for the next row
				rowsDownSoFar++;
				newX = startX + (startARowX * position) - (moveWithPitchX * rowsDownSoFar);
				newY = newY - distanceBetweenRowsY;
				newZ = startZ + (startARowZ * position) - (moveWithPitchZ * rowsDownSoFar);
			}
		}
		iterations++;
	}

}
