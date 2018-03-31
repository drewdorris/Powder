package com.ruinscraft.powder.models;

import org.bukkit.Sound;

public class SoundEffect {

	// Sound enum associated with this SoundEffect
	private Sound sound;
	// volume for this SoundEffect
	private float volume;
	// pitch for this SoundEffect (noteblock values)
	private float pitch;
	// waittime in ticks before playing this SoundEffect
	private int wait;

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

}
