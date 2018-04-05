package com.ruinscraft.powder.models;

import org.bukkit.Location;
import org.bukkit.Sound;

public class SoundEffect implements PowderElement {

	// Sound enum associated with this SoundEffect
	private Sound sound;
	// volume for this SoundEffect
	private float volume;
	// pitch for this SoundEffect (noteblock values)
	private float pitch;
	// waittime in ticks before playing this SoundEffect
	private int wait;
	// iterations (0 if infinite)
	private int iterations;
	// after how many ticks should it repeat?
	private int repeatTime;

	public SoundEffect(Sound sound, float volume, float pitch, int wait) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
		this.wait = wait;
	}

	public Sound getSound() {
		return sound;
	}

	public Float getVolume() {
		return volume;
	}

	public Float getPitch() {
		return pitch;
	}

	public Integer getWaitTime() {
		return wait;
	}
	
	public Integer getIterations() {
		return iterations;
	}
	
	public Integer getRepeatTime() {
		return repeatTime;
	}

	public void create(Location location) {
		location.getWorld().playSound(location, getSound(), getVolume(), getPitch());
	}

}
