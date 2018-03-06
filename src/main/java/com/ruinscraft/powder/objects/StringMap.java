package com.ruinscraft.powder.objects;

public class StringMap {
	
	// this will allow for more customization within a single Powder
	// change the position, spacing, etc. per each given delay
	
	private String stringMap;
	private int playerLeft;
	private int playerUp;
	private double spacing;
	
	public StringMap(String stringMap, int playerLeft, int playerUp, double spacing) {
		this.stringMap = stringMap;
		this.playerLeft = playerLeft;
		this.playerUp = playerUp;
		this.spacing = spacing;
	}
	
	public String getStringMap() {
		return stringMap;
	}
	
	public Integer getPlayerLeft() {
		return playerLeft;
	}
	
	public Integer getPlayerUp() {
		return playerUp;
	}
	
	public Double getSpacing() {
		return spacing;
	}

}
