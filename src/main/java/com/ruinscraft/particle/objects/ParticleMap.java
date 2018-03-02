package com.ruinscraft.particle.objects;

import java.util.List;

public class ParticleMap {
	
	private String name;
	private int playerLeft;
	private int playerUp;
	private float spacing;
	private List<String> smap;
	private List<SoundEffect> sounds;
	private boolean pitch;
	private boolean repeating;
	private long delay;
	
	public ParticleMap(String name, int playerLeft, int playerUp, 
			float spacing, List<String> smap, List<SoundEffect> sounds, 
				boolean pitch, boolean repeating, long delay) {
		
		this.name = name;
		this.playerLeft = playerLeft;
		this.playerUp = playerUp;
		this.spacing = spacing;
		this.smap = smap;
		this.sounds = sounds;
		this.pitch = pitch;
		this.repeating = repeating;
		this.delay = delay;
		
	}
	
	public String getName() {
		return name;
	}
	
	/*/
	 * how many units over it should start for creating the map
	 */
	public int getPlayerLeft() {
		return playerLeft;
	}
	
	/*/
	 * how many units up it should start for creating the map
	 */
	public int getPlayerUp() {
		return playerUp;
	}
	
	public float getSpacing() {
		return spacing;
	}
	
	public List<String> getStringMaps() {
		return smap;
	}
	
	public List<SoundEffect> getSounds() {
		return sounds;
	}
	
	public boolean getPitch() {
		return pitch;
	}
	
	public boolean getRepeating() {
		return repeating;
	}
	
	public long getDelay() {
		return delay;
	}

}
