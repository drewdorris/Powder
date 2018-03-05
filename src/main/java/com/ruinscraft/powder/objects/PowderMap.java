package com.ruinscraft.powder.objects;

import java.util.List;

public class PowderMap {
	
	private String name;
	private int playerLeft;
	private int playerUp;
	private float spacing;
	private List<String> smap;
	private List<SoundEffect> sounds;
	private List<Dust> dusts;
	private List<ChangedParticle> changedParticles;
	private boolean pitch;
	private boolean repeating;
	private long delay;
	
	public PowderMap(String name, int playerLeft, int playerUp, 
			float spacing, List<String> smap, List<SoundEffect> sounds, List<Dust> dusts,
				List<ChangedParticle> changedParticles, boolean pitch, boolean repeating, long delay) {
		
		this.name = name;
		this.playerLeft = playerLeft;
		this.playerUp = playerUp;
		this.spacing = spacing;
		this.smap = smap;
		this.sounds = sounds;
		this.dusts = dusts;
		this.changedParticles = changedParticles;
		this.pitch = pitch;
		this.repeating = repeating;
		this.delay = delay;
		
	}
	
	public String getName() {
		return name;
	}
	
	/*/
	 * how many units over it should start when creating the map
	 */
	public int getPlayerLeft() {
		return playerLeft;
	}
	
	/*/
	 * how many units up it should start when creating the map
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
	
	public List<Dust> getDusts() {
		return dusts;
	}
	
	public List<ChangedParticle> getChangedParticles() {
		return changedParticles;
	}
	
	public boolean getPitch() {
		return pitch;
	}
	
	public boolean isRepeating() {
		return repeating;
	}
	
	public long getDelay() {
		return delay;
	}

}
