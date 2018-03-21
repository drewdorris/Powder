package com.ruinscraft.powder.objects;

import java.util.List;

public class PowderMap {
	
	private String name;
	private float spacing;
	private List<ParticleMap> map;
	private List<SoundEffect> sounds;
	private List<Dust> dusts;
	private List<ChangedParticle> changedParticles;
	private boolean pitch;
	private boolean repeating;
	private boolean hidden;
	private long delay;
	
	public PowderMap(String name, float spacing, List<ParticleMap> map, List<SoundEffect> sounds, List<Dust> dusts,
				List<ChangedParticle> changedParticles, boolean pitch, boolean repeating, boolean hidden, long delay) {
		
		this.name = name;
		this.spacing = spacing;
		this.map = map;
		this.sounds = sounds;
		this.dusts = dusts;
		this.changedParticles = changedParticles;
		this.pitch = pitch;
		this.repeating = repeating;
		this.hidden = hidden;
		this.delay = delay;
		
	}
	
	public String getName() {
		return name;
	}
	
	public float getDefaultSpacing() {
		return spacing;
	}
	
	public List<ParticleMap> getMaps() {
		return map;
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
	
	public boolean hasPitch() {
		return pitch;
	}
	
	public boolean isRepeating() {
		return repeating;
	}
	
	public boolean isHidden() {
		return hidden;
	}
	
	public long getDelay() {
		return delay;
	}

}
