package com.ruinscraft.powder.objects;

import java.util.ArrayList;
import java.util.List;

public class ParticleMatrix {

	private List<Layer> layers;
	private int tick;
	private int playerLeft;
	private int playerUp;
	private float spacing;

	public ParticleMatrix() {
		this.layers = new ArrayList<Layer>();
		tick = 0;
		playerLeft = 0;
		playerUp = 0;
		spacing = 0;
	}

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

	public void setLayers(List<Layer> layers) {
		this.layers = layers;
	}

	public void addLayer(Layer layer) {
		layers.add(layer);
	}

	public Integer getTick() {
		return tick;
	}

	public void setTick(int tick) {
		this.tick = tick;
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

}
