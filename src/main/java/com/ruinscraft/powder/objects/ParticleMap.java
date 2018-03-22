package com.ruinscraft.powder.objects;

import java.util.List;

public class ParticleMap {

	private List<Object> map;
	private int tick;
	private int playerLeft;
	private int playerUp;
	private float spacing;
	
	public ParticleMap(List<Object> map, int tick, int playerLeft, int playerUp, float spacing) {
		this.map = map;
		this.tick = tick;
		this.playerLeft = playerLeft;
		this.playerUp = playerUp;
		this.spacing = spacing;
	}
	
	public List<Object> getMap() {
		return map;
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
