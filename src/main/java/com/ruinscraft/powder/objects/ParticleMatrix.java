package com.ruinscraft.powder.objects;

import java.util.List;

public class ParticleMatrix {

	private List<Layer> layers;
	private int tick;
	private int playerLeft;
	private int playerUp;
	private float spacing;

	public ParticleMatrix(List<Layer> layers, int tick, int playerLeft, int playerUp, float spacing) {
		this.layers = layers;
		this.tick = tick;
		this.playerLeft = playerLeft;
		this.playerUp = playerUp;
		this.spacing = spacing;
	}

	public List<Layer> getLayers() {
		return layers;
	}

	public Integer getTick() {
		return tick;
	}

	public Integer getPlayerLeft() {
		return playerLeft;
	}

	public Integer getPlayerUp() {
		return playerUp;
	}

	public Float getSpacing() {
		return spacing;
	}

}
