package com.ruinscraft.powder.objects;

import java.util.ArrayList;
import java.util.List;

public class Powder {
	
	private String name;
	private float spacing;
	private List<ParticleMatrix> maps;
	private List<SoundEffect> sounds;
	private List<Dust> dusts;
	private List<ChangedParticle> changedParticles;
	private boolean pitch;
	private boolean repeating;
	private boolean hidden;
	private long delay;
	
	public Powder(String name, float spacing, List<ParticleMatrix> maps, List<SoundEffect> sounds, List<Dust> dusts,
				List<ChangedParticle> changedParticles, boolean pitch, boolean repeating, boolean hidden, long delay) {
		
		this.name = name;
		this.spacing = spacing;
		this.maps = maps;
		this.sounds = sounds;
		this.dusts = dusts;
		this.changedParticles = changedParticles;
		this.pitch = pitch;
		this.repeating = repeating;
		this.hidden = hidden;
		this.delay = delay;
		
	}
	
	public Powder(String name, float spacing, List<ParticleMatrix> maps, 
			boolean pitch, boolean repeating, boolean hidden, long delay) {
	
	this.name = name;
	this.spacing = spacing;
	this.maps = maps;
	this.sounds = new ArrayList<SoundEffect>();
	this.dusts = new ArrayList<Dust>();
	this.changedParticles = new ArrayList<ChangedParticle>();
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
	
	public List<ParticleMatrix> getMaps() {
		return maps;
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
